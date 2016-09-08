package ee.app.conversa.jobs.message;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.HashMap;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.events.MessageEvent;
import ee.app.conversa.jobs.Priority;
import ee.app.conversa.management.ably.Connection;
import ee.app.conversa.model.database.dbMessage;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Logger;

/**
 * Created by edgargomez on 9/5/16.
 */
public class SendMessageJob extends Job {

    private final long id;

    public SendMessageJob(long id, String group) {
        // Order of messages matter, we don't want to send two in parallel
        super(new Params(Priority.CRITICAL).requireNetwork().persist().groupBy(group));
        // We have to set variables so they get serialized into job
        this.id = id;
    }

    @Override
    public void onAdded() {
        // Job has been secured to disk
        final dbMessage message = ConversaApp.getInstance(getApplicationContext())
                .getDB()
                .getMessageById(id);

        if (message != null) {
            Log.e("onAdded", "*********************************************");
            EventBus.getDefault().post(MessageEvent.sendMessageEvent(message));
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onRun() throws Throwable {
        final dbMessage message = ConversaApp.getInstance(getApplicationContext())
                .getDB()
                .getMessageById(id);

        final HashMap<String, Object> params = new HashMap<>(6);
        params.put("user", message.getFromUserId());
        params.put("business", message.getToUserId());
        params.put("fromUser", String.valueOf(true));
        params.put("messageType", Integer.valueOf(message.getMessageType()));
        if (Connection.getInstance() != null) {
            params.put("connectionId", Connection.getInstance().getPublicConnectionId());
        }

        switch (message.getMessageType()) {
            case Const.kMessageTypeAudio:
            case Const.kMessageTypeImage:
            case Const.kMessageTypeVideo: {
                try {
                    ParseFile file = new ParseFile(new File(message.getFileId()));
                    file.save();
                    params.put("file", file);
                } catch (NullPointerException|ParseException e) {
                    Logger.error("SendMessageJob", "File couldn't be added to message " + e.getMessage());
                    message.updateMessage(getApplicationContext(), dbMessage.statusParseError);
                    return;
                }
                break;
            }
            case Const.kMessageTypeLocation: {
                params.put("latitude", message.getLatitude());
                params.put("longitude", message.getLongitude());
                break;
            }

            case Const.kMessageTypeText: {
                params.put("text", message.getBody());
                break;
            }
        }

        try {
            ParseCloud.callFunction("sendUserMessage", params);
            message.updateMessage(getApplicationContext(), dbMessage.statusAllDelivered);
        } catch (ParseException e) {
            message.updateMessage(getApplicationContext(), dbMessage.statusParseError);
        }
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {

    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        if (throwable instanceof ParseException) {
            return RetryConstraint.RETRY;
        }

        return RetryConstraint.CANCEL;
    }

}
