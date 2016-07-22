package ee.app.conversa.notifications;

import android.content.Context;
import android.util.Log;

import com.onesignal.OneSignal;

import org.json.JSONObject;

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

        Log.d("NotifOpenedHandler", "Full additionalData:\n" + additionalData.toString());

        if (additionalData.has("actionSelected")) {
            Log.d("OneSignalExample", "OneSignal notification button with id " + additionalData.optString("actionSelected", null) + " pressed");
        }

        int action = additionalData.optInt("appAction", 0);

        if (action == 0) {
            return;
        }

        // If a push notification is received when the app is being used it does not display in the notification
        // bar so display in the app.
        if (isActive) {
            // Do nothing as we already notified all listener about new message on CustomNotificationExtenderService
        } else {
            // Check action to determine which Activity should be open
            /*
              Intent intent = new Intent(getApplication(), YourActivity.class);
              intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
              startActivity(intent);
              */
        }
    }
}
