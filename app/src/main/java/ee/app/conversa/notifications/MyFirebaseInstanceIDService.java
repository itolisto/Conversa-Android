package ee.app.conversa.notifications;

<<<<<<< HEAD
import ee.app.conversa.management.AblyConnection;
import io.ably.lib.fcm.AblyFirebaseInstanceIdService;
import io.ably.lib.realtime.AblyRealtime;
=======
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.management.PubnubConnection;
>>>>>>> pubnub

public class MyFirebaseInstanceIDService extends AblyFirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh(getAblyRealtime());
    }

<<<<<<< HEAD
    @SuppressWarnings("ConstantConditions")
    private AblyRealtime getAblyRealtime() {
        try {
            return AblyConnection.getInstance().getAblyRealtime();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
=======
    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        ConversaApp.getInstance(getApplicationContext()).getPreferences().setPushKey(token);
        PubnubConnection.getInstance().subscribeToPushChannels();
>>>>>>> pubnub
    }
}