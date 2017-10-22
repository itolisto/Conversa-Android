package ee.app.conversa.notifications;

import android.app.IntentService;
import android.content.Intent;

import com.google.firebase.iid.FirebaseInstanceId;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.management.SkygearConnection;

/**
 * Created by edgargomez on 5/16/17.
 */

public class RegistrationIntentService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public RegistrationIntentService() {
        super("RegistrationIntentService");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        try {
            String token = FirebaseInstanceId.getInstance().getToken();
            ConversaApp.getInstance(getApplicationContext()).getPreferences().setPushKey(token);
            SkygearConnection.getInstance().subscribeToPushChannels();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}