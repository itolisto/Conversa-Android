package ee.app.conversa.notifications.onesignal;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.onesignal.OSNotification;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONObject;

import ee.app.conversa.ActivityChatWall;
import ee.app.conversa.ActivityMain;
import ee.app.conversa.ConversaApp;
import ee.app.conversa.model.Database.dBusiness;
import ee.app.conversa.utils.Const;

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
        OSNotification notification = result.notification;
        JSONObject additionalData = notification.payload.additionalData;

        if (additionalData == null) {
            return;
        }

        int stackedNotifications = 1;
        if (result.notification.groupedNotifications != null && result.notification.groupedNotifications.size() > 0) {
            stackedNotifications = result.notification.groupedNotifications.size();
        }

        Log.e("NotifOpenedHandler", "Full additionalData:\n" + additionalData.toString());
        Intent intent = null;

        switch (additionalData.optInt("appAction", 0)) {
            case 1:
                String contactId = additionalData.optString("contactId", null);

                if (contactId == null) {
                    break;
                }

                dBusiness user = ConversaApp.getDB().isContact(contactId);
                if (user != null) {
                    // Set extras
                    intent = new Intent(context.getApplicationContext(), ActivityChatWall.class);
                    intent.putExtra(Const.kClassBusiness, user);
                    intent.putExtra(Const.kYapDatabaseName, false);
                    intent.putExtra(Const.kAppVersionKey, stackedNotifications);
                }
                break;
            default:
                break;
        }

        if (notification.isAppInFocus) {
            if (intent == null) {
                intent = new Intent(context.getApplicationContext(), ActivityMain.class);
            }

            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

}