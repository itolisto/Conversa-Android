package ee.app.conversa.messaging;

import android.app.IntentService;
import android.content.Intent;

import com.birbit.android.jobqueue.JobStatus;

import org.json.JSONException;
import org.json.JSONObject;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.jobs.ReceiveMessageJob;
import ee.app.conversa.utils.Logger;

/**
 * Created by edgargomez on 7/21/16.
 */
public class CustomMessageService extends IntentService {

    private final String TAG = CustomMessageService.class.getSimpleName();

    public CustomMessageService() {
        super("CustomMessageService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
         if (intent.getExtras() == null) {
             return;
         }

         String json = intent.getExtras().getString("data", "");

         if (json.isEmpty()) {
             return;
         }

         JSONObject additionalData;

         try {
             additionalData = new JSONObject(json);
         } catch (JSONException e) {
             Logger.error(TAG, "onMessageReceived payload fail to parse-> " + e.getMessage());
             return;
         }

        Logger.error(TAG, "Full additionalData:\n" + additionalData.toString());

         switch (additionalData.optInt("appAction", 0)) {
             case 1: {
                 String messageId = additionalData.optString("messageId", null);
                 if (ConversaApp.getInstance(this)
                         .getJobManager()
                         .getJobStatus(messageId) == JobStatus.UNKNOWN)
                 {
                     Logger.error(TAG, "Create new Job from " + TAG);
                     ConversaApp.getInstance(this)
                             .getJobManager()
                             .addJob(new ReceiveMessageJob(json, messageId));
                 } else {
                     Logger.error(TAG, "A Job for this message already exits, skip creation");
                 }

                 break;
             }
         }
    }

}