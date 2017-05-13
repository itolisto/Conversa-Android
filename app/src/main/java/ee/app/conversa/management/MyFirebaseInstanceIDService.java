package ee.app.conversa.management;

import io.ably.lib.fcm.AblyFirebaseInstanceIdService;
import io.ably.lib.realtime.AblyRealtime;

public class MyFirebaseInstanceIDService extends AblyFirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh(getAblyRealtime());
    }

    private static AblyRealtime getAblyRealtime() {
        try {
            return AblyConnection.getInstance().getAblyRealtime();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}