package ee.app.conversa.notifications.onesignal;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.onesignal.OSNotification;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONObject;

import ee.app.conversa.ActivityMain;

/**
 * Created by edgargomez on 8/19/16.
 */
public class CustomNotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {

    Context context;

    public CustomNotificationOpenedHandler(Context context) {
        this.context = context;
    }

    @Override
    public void notificationOpened(OSNotificationOpenResult result) {
        Log.e("NotifOpenedHandler", "notificationOpened");
        OSNotification notification = result.notification;
        JSONObject additionalData = notification.payload.additionalData;

        if (additionalData == null) {
            return;
        }

        Log.e("NotifOpenedHandler", "Full additionalData:\n" + additionalData.toString());
        Intent intent;
        intent = new Intent(context.getApplicationContext(), ActivityMain.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

}