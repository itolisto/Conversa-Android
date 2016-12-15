package ee.app.conversa.jobs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.onesignal.OneSignal;
import com.parse.ParseCloud;
import com.parse.ParseException;

import org.json.JSONObject;

import java.util.HashMap;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.management.AblyConnection;
import ee.app.conversa.model.parse.Account;
import ee.app.conversa.utils.AppActions;
import ee.app.conversa.utils.Logger;
import ee.app.conversa.utils.Utils;

import static com.parse.ParseException.CONNECTION_FAILED;
import static com.parse.ParseException.INTERNAL_SERVER_ERROR;
import static com.parse.ParseException.INVALID_SESSION_TOKEN;

/**
 * Created by edgargomez on 10/12/16.
 */

public class CustomerInfoJob extends Job {

    private final String TAG = CustomerInfoJob.class.getSimpleName();

    public CustomerInfoJob(String businessId) {
        // Order of messages matter, we don't want to send two in parallel
        super(new Params(Priority.CRITICAL).requireNetwork().persist().groupBy(businessId).addTags(businessId));
    }

    @Override
    public void onAdded() {
        // Job has been secured to disk, add item to database
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onRun() throws Throwable {
        HashMap<String, String> params = new HashMap<>();

        String json = ParseCloud.callFunction("getCustomerId", params);

        //Logger.error(TAG, "CUSTOMER_OBJECTID: " + json);

        JSONObject jsonRootObject = new JSONObject(json);

        String objectId = jsonRootObject.optString("ob", Account.getCurrentUser().getObjectId());
        String displayName = jsonRootObject.optString("dn", "");
        int gender = jsonRootObject.optInt("gn", 2);
        String birthday = jsonRootObject.optString("bd", "");

        // 1. Save Customer object id
        ConversaApp.getInstance(getApplicationContext()).getPreferences().setAccountCustomerId(objectId, false);
        ConversaApp.getInstance(getApplicationContext()).getPreferences().setAccountDisplayName(displayName, false);
        ConversaApp.getInstance(getApplicationContext()).getPreferences().setAccountGender(gender, false);
        ConversaApp.getInstance(getApplicationContext()).getPreferences().setAccountBirthday(birthday, false);
        // 2. Subscribe to Customer channels
        AblyConnection.getInstance().subscribeToChannels();
        // 3. Subscribe to Customer channels
        OneSignal.setSubscription(true);
        Utils.subscribeToTags(objectId);
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
                    exception.getCode() == CONNECTION_FAILED)
            {
                // An error occurred in onRun.
                // Return value determines whether this job should retry or cancel. You can further
                // specify a backoff strategy or change the job's priority. You can also apply the
                // delay to the whole group to preserve jobs' running order.
                RetryConstraint rtn = RetryConstraint.createExponentialBackoff(runCount, 1000);
                rtn.setNewPriority(Priority.MID);
                return rtn;
            } else if (exception.getCode() == INVALID_SESSION_TOKEN) {
                AppActions.validateParseException(getApplicationContext(), exception);
                return RetryConstraint.CANCEL;
            } else {
                AppActions.validateParseException(getApplicationContext(), exception);
                return RetryConstraint.CANCEL;
            }
        }

        return RetryConstraint.RETRY;
    }

}