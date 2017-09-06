package ee.app.conversa.jobs;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.contact.ContactUpdateReason;
import ee.app.conversa.events.contact.ContactUpdateEvent;
import ee.app.conversa.utils.Logger;
import ee.app.conversa.utils.Utils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by edgargomez on 10/12/16.
 */

public class AvatarJob extends Job {

    private final String TAG = AvatarJob.class.getSimpleName();
    private final String url;
    private final long id;

    public AvatarJob(String businessId, String url, long id) {
        // Order of messages matter, we don't want to send two in parallel
        super(new Params(Priority.HIGH).requireNetwork().persist().groupBy(businessId).addTags(businessId));
        this.url = url;
        this.id = id;
    }

    @Override
    public void onAdded() {
        // Job has been secured to disk, add item to database
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onRun() throws Throwable {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();

        if (response.isSuccessful()) {
            Logger.error(TAG, "Avatar successful: " + response.toString());
            InputStream inputStream = response.body().byteStream();
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            Utils.saveAvatarToInternalStorage(
                    getApplicationContext(),
                    bitmap,
                    id);
            EventBus.getDefault().post(new ContactUpdateEvent(
                    ConversaApp.getInstance(getApplicationContext())
                            .getDB().getContactById(id),
                    ContactUpdateReason.AVATAR_DOWNLOAD
            ));
        } else {
            // Clean avatar url
            Logger.error(TAG, "Request received unsuccessful response code: " + response.code());
            ConversaApp.getInstance(getApplicationContext()).getDB().updateContactAvatar(id, null);
            EventBus.getDefault().post(new ContactUpdateEvent(
                    ConversaApp.getInstance(getApplicationContext()).getDB().getContactById(id),
                    ContactUpdateReason.AVATAR_DOWNLOAD_FAIL
            ));
        }
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        Logger.error(TAG, "onCancel called. Reason: " + cancelReason);
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        if (throwable instanceof IOException) {
            Logger.error(TAG, "Image download error: " + throwable.getMessage());
            RetryConstraint rtn = RetryConstraint.createExponentialBackoff(runCount, 1000);
            rtn.setNewPriority(Priority.MID);
            return rtn;
        }

        return RetryConstraint.CANCEL;
    }

}