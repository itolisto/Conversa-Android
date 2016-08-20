package ee.app.conversa.management.message;

import android.app.IntentService;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;

import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.HashMap;
import java.util.List;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.management.Ably.Connection;
import ee.app.conversa.model.Database.dbMessage;
import ee.app.conversa.receivers.FileUploadingReceiver;
import ee.app.conversa.utils.Const;

/**
 * Created by edgargomez on 8/10/16.
 */
public class MessageIntentService extends IntentService {

    public static final String TAG = "MessageIntentService";

    // Intent constants
    public static final String INTENT_EXTRA_ACTION_CODE = "action_code";
    public static final String INTENT_EXTRA_MESSAGE = "message_single";
    public static final String INTENT_EXTRA_MESSAGE_LIST = "message_list";
    public static final String INTENT_EXTRA_RECEIVER = "message_receiver";

    // Specific constants
    public static final String INTENT_EXTRA_UPDATE_STATUS = "update_status";
    public static final String INTENT_EXTRA_CONTACT_ID = "contact_id";
    public static final String INTENT_EXTRA_MESSAGE_COUNT = "message_count";
    public static final String INTENT_EXTRA_MESSAGE_SKIP = "message_skip";

    // MESSAGE ACTIONS
    public static final int ACTION_MESSAGE_SAVE = 1;
    public static final int ACTION_MESSAGE_NEW_MESSAGE = 2;
    public static final int ACTION_MESSAGE_UPDATE = 3;
    public static final int ACTION_MESSAGE_DELETE = 4;
    public static final int ACTION_MESSAGE_RETRIEVE_ALL = 5;
    public static final int ACTION_MESSAGE_UPDATE_UNREAD = 6;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public MessageIntentService() {
        super("MessageIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.getExtras() == null) {
            return;
        }

        int actionCode = intent.getExtras().getInt(INTENT_EXTRA_ACTION_CODE, 0);
        dbMessage message = intent.getExtras().getParcelable(INTENT_EXTRA_MESSAGE);
        List<dbMessage> messages = null;

        try {
            switch (actionCode) {
                case ACTION_MESSAGE_SAVE: {
                    if (message != null) {
                        message = ConversaApp.getDB().saveMessage(message);
                    }
                    break;
                }
                case ACTION_MESSAGE_NEW_MESSAGE: {
                    if (message != null) {
                        message = ConversaApp.getDB().saveMessage(message);
                    }
                    break;
                }
                case ACTION_MESSAGE_UPDATE: {
                    if (message != null) {
                        String status = intent.getExtras().getString(INTENT_EXTRA_UPDATE_STATUS, dbMessage.statusParseError);
                        int result = ConversaApp.getDB().updateDeliveryStatus(message.getId(), status);
                        if (result > 0) {
                            message.setDeliveryStatus(status);
                        }
                    }
                    break;
                }
                case ACTION_MESSAGE_UPDATE_UNREAD: {
                    String contact_id = intent.getExtras().getString(INTENT_EXTRA_CONTACT_ID);
                    if (contact_id != null) {
                        ConversaApp.getDB().updateReadMessages(contact_id);
                    }
                    return;
                }
                case ACTION_MESSAGE_RETRIEVE_ALL: {
                    String contact_id = intent.getExtras().getString(INTENT_EXTRA_CONTACT_ID);

                    if (contact_id != null) {
                        int count = intent.getExtras().getInt(INTENT_EXTRA_MESSAGE_COUNT, 0);
                        int skip = intent.getExtras().getInt(INTENT_EXTRA_MESSAGE_SKIP, 0);
                        messages = ConversaApp.getDB().getMessagesByContact(contact_id, count, skip);
                    }
                    break;
                }
                default: {
                    return;
                }
            }
        } catch (SQLException e) {
            Log.e("MessageAsyncTaskRunner", "No se pudo guardar mensaje porque ocurrio el siguiente error: " + e.getMessage());
        }

        if (actionCode == ACTION_MESSAGE_SAVE) {
            if (message != null && message.getId() != -1) {
                HashMap<String, Object> params = new HashMap<>();
                params.put("user", message.getFromUserId());
                params.put("business", message.getToUserId());
                params.put("fromUser", String.valueOf(true));
                params.put("messageType", Integer.valueOf(message.getMessageType()));
                if (Connection.getInstance().getPublicConnectionId() != null) {
                    params.put("connectionId", Connection.getInstance().getPublicConnectionId());
                }

                switch (message.getMessageType()) {
                    case Const.kMessageTypeAudio:
                    case Const.kMessageTypeImage:
                    case Const.kMessageTypeVideo: {
                        //intent.putExtra("imageUri", imageUri.toString());
                        String fileUri = intent.getExtras().getString(message.getFileId());
                        if (fileUri != null) {
                            // Extract the receiver passed into the service
                            FileUploadingReceiver rec = intent.getParcelableExtra(INTENT_EXTRA_RECEIVER);
                            // To send a message to the Activity, create a pass a Bundle
                            Bundle bundle = new Bundle();
                            bundle.putParcelable(FileUploadingReceiver.BUNDLE_MESSAGE, message);
                            // Here we call send passing a resultCode and the bundle of extras
                            rec.send(FileUploadingReceiver.UPLOAD_CODE, bundle);
                        } else {
                            ConversaApp.getDB().notifyMessageListeners(actionCode, message, null, null);
                        }
                        return;
                    }
                    case Const.kMessageTypeLocation: {
                        params.put("latitude", message.getLatitude());
                        params.put("longitude", message.getLongitude());
                        ConversaApp.getDB().notifyMessageListeners(actionCode, message, null, null);
                        break;
                    }
                    case Const.kMessageTypeText: {
                        params.put("text", message.getBody());
                        ConversaApp.getDB().notifyMessageListeners(actionCode, message, null, null);
                        break;
                    }
                }

                try {
                    ParseCloud.callFunction("sendUserMessage", params);
                    message.updateDelivery(getApplicationContext(), dbMessage.statusAllDelivered);
                } catch (ParseException e) {
                    message.updateDelivery(getApplicationContext(), dbMessage.statusParseError);
                }
            }
        } else {
            if (message == null) {
                ConversaApp.getDB().notifyMessageListeners(actionCode, null, messages, null);
            } else {
                ConversaApp.getDB().notifyMessageListeners(actionCode, message, null, null);
            }
        }
    }

}