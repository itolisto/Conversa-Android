package ee.app.conversa.jobs.message;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.events.MessageEvent;
import ee.app.conversa.jobs.Priority;
import ee.app.conversa.model.database.dbMessage;
import ee.app.conversa.utils.Const;

/**
 * Created by edgargomez on 9/6/16.
 */
public class ReceiveMessageJob extends Job {

    private final long id;

    public ReceiveMessageJob(long id, String group) {
        // Order of messages matter, we don't want to send two in parallel
        super(new Params(Priority.CRITICAL).requireNetwork().persist().groupBy(group));
        // We have to set variables so they get serialized into job
        this.id = id;
    }

    @Override
    public void onAdded() {
        // Job has been secured to disk, add item to database
        final dbMessage message = ConversaApp.getInstance(getApplicationContext())
                .getDB()
                .getMessageById(id);

        if (message != null) {
            EventBus.getDefault().post(MessageEvent.receiveMessageEvent(message));
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onRun() throws Throwable {
        final dbMessage message = ConversaApp.getInstance(getApplicationContext())
                .getDB()
                .getMessageById(id);

        switch (message.getMessageType()) {
            case Const.kMessageTypeAudio:
            case Const.kMessageTypeImage:
            case Const.kMessageTypeVideo: {
                // Download file
                return;
            }
        }

        message.updateMessage(getApplicationContext(), dbMessage.statusReceived);
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {

    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        return null;
    }

}