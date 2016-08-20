package ee.app.conversa.notifications.onesignal;

import android.content.Context;
import android.util.Log;

import com.onesignal.OSNotification;
import com.onesignal.OneSignal;

/**
 * Created by edgargomez on 8/19/16.
 */
public class CustomNotificationReceivedHandler implements OneSignal.NotificationReceivedHandler {

    Context context;

    public CustomNotificationReceivedHandler(Context context) {
        this.context = context;
    }

    @Override
    public void notificationReceived(OSNotification notification) {
        Log.e("NotifReceivedHandler", "Payload: " + notification.payload + "\nAppInFocus: " + notification.isAppInFocus);
    }

}