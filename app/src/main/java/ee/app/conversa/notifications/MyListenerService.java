package ee.app.conversa.notifications;

import android.os.Bundle;

import com.birbit.android.jobqueue.JobStatus;

import org.json.JSONException;
import org.json.JSONObject;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.jobs.ReceiveMessageJob;
import ee.app.conversa.utils.Logger;

/**
 * Created by root on 4/10/17.
 */

public class MyListenerService extends com.google.android.gms.gcm.GcmListenerService {

    private final String TAG = MyListenerService.class.getSimpleName();

    @Override
    public void onMessageReceived(String data, Bundle bundle) {
        super.onMessageReceived(data, bundle);
        // Log para ver que entra cuando recibis una push
        // Convertis a un JSONObject el s, ya copias el codigo

        try {
            JSONObject additionalData = new JSONObject(data);
            Logger.error(TAG, "Full additionalData:\n" + additionalData.toString());

            switch (additionalData.optInt("appAction", 0)) {
                case 1: {
                    // Check if a job for this message already exists, if it does, skip
                    String messageId = additionalData.optString("messageId", "");

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
                }
            }
        } catch (JSONException ignored) {}
    }

}