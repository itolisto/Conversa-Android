package ee.app.conversa.receivers;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ProgressCallback;

import java.io.File;
import java.util.HashMap;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.management.Ably.Connection;
import ee.app.conversa.management.message.MessageIntentService;
import ee.app.conversa.model.Database.dbMessage;

/**
 * Created by edgargomez on 8/17/16.
 */
@SuppressLint("ParcelCreator")
public class FileUploadingReceiver extends ResultReceiver {

    private Receiver mReceiver;
    public static final int UPLOAD_CODE = 100;
    public static final String BUNDLE_MESSAGE = "bundle_message";

    public FileUploadingReceiver(Handler handler) {
        super(handler);
    }

    public interface Receiver {
        void onReceiveResult(dbMessage message, int percentage);
    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        super.onReceiveResult(resultCode, resultData);
        if (mReceiver != null) {
            if (resultCode == UPLOAD_CODE) {
                final dbMessage message = resultData.getParcelable(BUNDLE_MESSAGE);

                if (message == null) {
                    mReceiver.onReceiveResult(null, -1);
                    return;
                }

                String fileUri = message.getFileId();
                if (fileUri != null) {
                    Uri uri = Uri.parse(fileUri);
                    final File localfile = new File(uri.getPath());
                    if (localfile.length() > 0) {
                        final ParseFile file = new ParseFile(localfile);
                        file.saveInBackground(new ProgressCallback() {
                            @Override
                            public void done(Integer percentDone) {
                                if (percentDone == 100) {
                                    // Upload to Parse
                                    HashMap<String, Object> params = new HashMap<>();
                                    params.put("user", message.getFromUserId());
                                    params.put("business", message.getToUserId());
                                    params.put("fromUser", String.valueOf(true));
                                    params.put("messageType", Integer.valueOf(message.getMessageType()));
                                    if (Connection.getInstance().getPublicConnectionId() != null) {
                                        params.put("connectionId", Connection.getInstance().getPublicConnectionId());
                                    }
                                    params.put("size", message.getBytes());
                                    params.put("width", message.getWidth());
                                    params.put("height", message.getHeight());
                                    params.put("file", file);
                                    ParseCloud.callFunctionInBackground("sendUserMessage", params, new FunctionCallback<Integer>() {
                                        @Override
                                        public void done(Integer object, ParseException e) {
                                            if (e == null) {
                                                message.updateDelivery(null, dbMessage.statusAllDelivered);
                                            } else {
                                                message.updateDelivery(null, dbMessage.statusParseError);
                                            }
                                        }
                                    });
                                    // Update on database
                                    ConversaApp.getDB().updateDeliveryStatus(message.getId(), dbMessage.statusAllDelivered);
                                    // Update percentage uploaded
                                    ConversaApp.getDB().notifyMessageListeners(MessageIntentService.ACTION_MESSAGE_UPDATE, message, null, null);
                                } else {
                                    // Notify percentage uploaded
                                    mReceiver.onReceiveResult(null, percentDone);
                                }
                            }
                        });
                    } else {
                        mReceiver.onReceiveResult(null, -1);
                    }
                } else {
                    mReceiver.onReceiveResult(null, -1);
                }
            }
        }
    }
}