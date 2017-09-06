package ee.app.conversa.jobs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.parse.ParseCloud;
import com.parse.ParseException;

import org.json.JSONObject;

import java.util.HashMap;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.management.AblyConnection;
import ee.app.conversa.model.parse.Account;
import ee.app.conversa.utils.AppActions;
import ee.app.conversa.utils.Logger;

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
        AblyConnection.getInstance().subscribeToPushChannels();
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