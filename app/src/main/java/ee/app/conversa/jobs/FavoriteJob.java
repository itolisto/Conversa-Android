package ee.app.conversa.jobs;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import ee.app.conversa.ConversaApp;
import ee.app.conversa.utils.AppActions;
import ee.app.conversa.utils.Logger;

/**
 * Created by edgargomez on 10/12/16.
 */

public class FavoriteJob extends Job {

    private final String TAG = FavoriteJob.class.getSimpleName();
    private final String businessId;
    private final boolean favorite;

    public FavoriteJob(String group, String businessId, boolean favorite) {
        // Order of messages matter, we don't want to send two in parallel
        super(new Params(Priority.CRITICAL).requireNetwork().persist().groupBy(group).addTags(group));
        this.businessId = businessId;
        this.favorite = favorite;
    }

    @Override
    public void onAdded() {
        // Job has been secured to disk
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onRun() throws Throwable {
        HashMap<String, Object> params = new HashMap<>(3);
        params.put("businessId", businessId);
        params.put("customerId", ConversaApp
                .getInstance(getApplicationContext())
                .getPreferences()
                .getAccountCustomerId());

        if (favorite) {
            params.put("favorite", true);
        }

        ParseCloud.callFunction("setCustomerFavorite", params);
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        Logger.error(TAG, "onCancel called. Reason: " + cancelReason);
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        if (throwable instanceof ParseException) {
            if (AppActions.validateParseException((ParseException) throwable)) {
                AppActions.appLogout(getApplicationContext(), true);
                return RetryConstraint.CANCEL;
            }
        }

        // An error occurred in onRun.
        // Return value determines whether this job should retry or cancel. You can further
        // specify a backoff strategy or change the job's priority. You can also apply the
        // delay to the whole group to preserve jobs' running order.
        RetryConstraint rtn = RetryConstraint.createExponentialBackoff(runCount, 1000);
        rtn.setNewPriority(Priority.MID);
        return rtn;
    }

}