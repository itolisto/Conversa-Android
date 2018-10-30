package ee.app.conversa.jobs;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import ee.app.conversa.ConversaApp;
import ee.app.conversa.delivery.DeliveryStatus;
import ee.app.conversa.events.message.MessageUpdateEvent;
import ee.app.conversa.messaging.MessageUpdateReason;
import ee.app.conversa.model.database.dbMessage;
import ee.app.conversa.utils.Logger;
import ee.app.conversa.utils.Utils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by edgargomez on 10/12/16.
 */
public class DownloadFileJob extends Job {

    private final String TAG = DownloadFileJob.class.getSimpleName();
    private final long messageId;

    public DownloadFileJob(String group, long messageId) {
        // Order of messages matter, we don't want to send two in parallel
        super(new Params(Priority.HIGH).requireNetwork().persist().groupBy(group).addTags(group));
        this.messageId = messageId;
    }

    @Override
    public void onAdded() {
        // Job has been secured to disk, add item to database
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onRun() throws Throwable {
        dbMessage dbmessage = ConversaApp.getInstance(getApplicationContext())
                .getDB()
                .getMessageById(messageId);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(dbmessage.getRemoteUrl())
                .build();

        try {
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                InputStream inputStream = response.body().byteStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                dbmessage.setLocalUrl(
                        Utils.saveImageToInternalStorage(getApplicationContext(),
                                bitmap, dbmessage.getId())
                );

                ConversaApp.getInstance(getApplicationContext())
                        .getDB()
                        .updateDeliveryStatus(dbmessage.getId(), DeliveryStatus.statusReceived);
                dbmessage.setDeliveryStatus(DeliveryStatus.statusReceived);
                EventBus.getDefault().post(
                        new MessageUpdateEvent(dbmessage, MessageUpdateReason.FILE_DOWNLOAD));
                return;
            } else {
                Logger.error(TAG, "Request received unsuccessful response code: " + response.code());
            }
        } catch (IOException|IllegalStateException e) {
            Logger.error(TAG, "Image download error: " + e.getMessage());
        }

        ConversaApp.getInstance(getApplicationContext())
                .getDB()
                .updateDeliveryStatus(dbmessage.getId(), DeliveryStatus.statusParseError);
        dbmessage.setDeliveryStatus(DeliveryStatus.statusParseError);
        EventBus.getDefault().post(
                new MessageUpdateEvent(dbmessage, MessageUpdateReason.FILE_DOWNLOAD));
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        Logger.error(TAG, "onCancel called. Reason: " + cancelReason);
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        // This method will never be called as all exceptions are being caught in onRun method
        return RetryConstraint.CANCEL;
    }

}