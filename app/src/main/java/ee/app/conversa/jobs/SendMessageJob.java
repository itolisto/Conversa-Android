package ee.app.conversa.jobs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;

import java.io.File;
import java.util.HashMap;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.delivery.DeliveryStatus;
import ee.app.conversa.management.PubnubConnection;
import ee.app.conversa.model.database.dbMessage;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Logger;
import ee.app.conversa.utils.AppActions;

/**
 * Created by edgargomez on 9/5/16.
 */
public class SendMessageJob extends Job {

    private final String TAG = SendMessageJob.class.getSimpleName();
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
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onRun() throws Throwable {
        dbMessage message = ConversaApp.getInstance(getApplicationContext())
                .getDB()
                .getMessageById(id);

        final HashMap<String, Object> params = new HashMap<>(9);
        params.put("customerId", message.getFromUserId());
        params.put("businessId", message.getToUserId());
        params.put("fromCustomer", true);
        params.put("messageType", Integer.valueOf(message.getMessageType()));

        if (PubnubConnection.getInstance() != null && PubnubConnection.getInstance()
                .getPublicConnectionId() != null)
        {
            params.put("connectionId", PubnubConnection.getInstance().getPublicConnectionId());
        }

        switch (message.getMessageType()) {
            case Const.kMessageTypeAudio:
            case Const.kMessageTypeImage:
            case Const.kMessageTypeVideo: {
                ParseFile file;

                try {
                    file = new ParseFile(new File(message.getLocalUrl()));
                    file.save();
                } catch (NullPointerException|ParseException e) {
                    Logger.error("SendMessageJob", "File couldn't be added to message " + e.getMessage());
                    message.updateMessageStatus(getApplicationContext(), DeliveryStatus.statusParseError);
                    return;
                }

                params.put("file", file);
                params.put("width", message.getWidth());
                params.put("height", message.getHeight());
                params.put("size", message.getBytes());
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
            message.updateMessageStatus(getApplicationContext(), DeliveryStatus.statusAllDelivered);
        } catch (ParseException e) {
            if (AppActions.validateParseException(e)) {
                AppActions.appLogout(getApplicationContext(), true);
            } else {
                message.updateMessageStatus(getApplicationContext(), DeliveryStatus.statusParseError);
            }
        }
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        Logger.error(TAG, "onCancel called. Reason: " + cancelReason);
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.CANCEL;
    }

}