package ee.app.conversa.jobs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.HashMap;

import ee.app.conversa.utils.Logger;

import static com.parse.ParseException.CONNECTION_FAILED;
import static com.parse.ParseException.INTERNAL_SERVER_ERROR;
import static com.parse.ParseException.INVALID_SESSION_TOKEN;

/**
 * Created by edgargomez on 10/12/16.
 */

public class FavoriteJob extends Job {

    private final String TAG = ReceiveMessageJob.class.getSimpleName();
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
        HashMap<String, Object> params = new HashMap<>();
        params.put("business", businessId);

        if (favorite) {
            params.put("favorite", true);
        }

        ParseCloud.callFunction("favorite", params);
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        Logger.error(TAG, "onCancel called. Reason: " + cancelReason);
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        if (throwable instanceof ParseException) {
            ParseException exception = (ParseException) throwable;
            Logger.error(TAG, exception.getMessage());

            if (exception.getCode() == INTERNAL_SERVER_ERROR ||
                    exception.getCode() == CONNECTION_FAILED ||
                    exception.getCode() == INVALID_SESSION_TOKEN )
            {
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