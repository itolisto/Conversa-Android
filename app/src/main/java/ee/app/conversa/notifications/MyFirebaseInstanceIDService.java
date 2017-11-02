package ee.app.conversa.notifications;

import ee.app.conversa.management.AblyConnection;
import io.ably.lib.fcm.AblyFirebaseInstanceIdService;
import io.ably.lib.realtime.AblyRealtime;

public class MyFirebaseInstanceIDService extends AblyFirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh(getAblyRealtime());
    }

    @SuppressWarnings("ConstantConditions")
    private AblyRealtime getAblyRealtime() {
        try {
            return AblyConnection.getInstance().getAblyRealtime();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}