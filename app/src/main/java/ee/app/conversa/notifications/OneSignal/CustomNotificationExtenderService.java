package ee.app.conversa.notifications.onesignal;

import android.util.Log;

import com.birbit.android.jobqueue.JobStatus;
import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationPayload;
import com.onesignal.OSNotificationReceivedResult;

import org.json.JSONObject;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.jobs.ReceiveMessageJob;
import ee.app.conversa.management.AblyConnection;
import ee.app.conversa.utils.Logger;
import io.ably.lib.realtime.ConnectionState;

/**
 * Created by edgargomez on 7/21/16.
 */
public class CustomNotificationExtenderService extends NotificationExtenderService {

    private final String TAG = CustomNotificationExtenderService.class.getSimpleName();

    @Override
    @SuppressWarnings("ConstantConditions")
    protected boolean onNotificationProcessing(OSNotificationReceivedResult result) {
        OSNotificationPayload notification = result.payload;
        JSONObject additionalData = notification.additionalData;

        if (additionalData == null || additionalData.length() == 0) {
            Logger.error(TAG, "onNotificationProcessing additionalData is null or empty");
            return true;
        }

        Logger.error(TAG, "Full additionalData:\n" + additionalData.toString());

        switch (additionalData.optInt("appAction", 0)) {
            case 1: {
                if (result.restoring) {
                    Log.e(TAG, "Returning as 'restoring' is true");
                    return true;
                }

                // TODO: This verification was removed due to a lock with Ably
                /**
                 * The lock is triggered when the app recovers internet connection and Ably
                 * connects, but cannot receive the message, therefore OneSignal will be
                 * the only one who receives the message but if it check for Ably, it will
                 * skip the message thus the user won't be notified about it. Instead if Ably
                 * is connected where only sleeping this thread for 900ms
                 */
                if (AblyConnection.getInstance() != null &&
                        AblyConnection.getInstance().ablyConnectionStatus() == ConnectionState.connected) {
                    try {
                        Thread.sleep(900);
                    } catch (IllegalArgumentException | InterruptedException e) {
                        Logger.error(TAG, e.getMessage());
                    }
                    //Log.e(TAG, "Returning as Ably client is connected");
                    //return true;
                }

                // Check if a job for this message already exists, if it does, skip
                String messageId = additionalData.optString("messageId", null);
                if (ConversaApp.getInstance(this)
                        .getJobManager()
                        .getJobStatus(messageId) == JobStatus.UNKNOWN)
                {
                    Logger.error(TAG, "Create new Job from " + TAG);
                    ConversaApp.getInstance(this)
                            .getJobManager()
                            .addJob(new ReceiveMessageJob(additionalData.toString(), messageId));
                } else {
                    Logger.error(TAG, "A Job for this message already exits, skip creation");
                }

                return true;
            }
        }

        // Return true to stop the notifications from displaying.
        return false;
    }

}