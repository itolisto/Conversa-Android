package ee.app.conversa.notifications;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.onesignal.OneSignal;

import org.json.JSONArray;
import org.json.JSONObject;

import ee.app.conversa.ActivityChatWall;
import ee.app.conversa.ActivityMain;
import ee.app.conversa.ConversaApp;
import ee.app.conversa.model.Database.dBusiness;
import ee.app.conversa.utils.Const;

/**
 * Created by edgargomez on 7/21/16.
 */
public class CustomNotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {

    private final String TAG = CustomNotificationOpenedHandler.class.getSimpleName();
    Context context;

    public CustomNotificationOpenedHandler(Context context) {
        this.context = context;
    }

    @Override
    public void notificationOpened(String message, JSONObject additionalData, boolean isActive) {
        if (additionalData == null) {
            return;
        }

        JSONArray stackedNotifications = additionalData.optJSONArray("stacked_notifications");
        if (stackedNotifications != null && stackedNotifications.length() > 0) {
            // We just need the first object of this array
            additionalData = stackedNotifications.optJSONObject(0);
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
                    if (stackedNotifications == null) {
                        intent.putExtra(Const.kAppVersionKey, 1);
                    } else {
                        intent.putExtra(Const.kAppVersionKey, stackedNotifications.length());
                    }
                }
                break;
            default:
                break;
        }

        // If a push notification is received when the app is being used it does not display in the notification
        // bar so display in the app.
        if (isActive) {
            // Do nothing as we already saved and notified all listeners
            // about new message on CustomNotificationExtenderService
        } else {
            if (intent == null) {
                intent = new Intent(context.getApplicationContext(), ActivityMain.class);
            }

            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
