package ee.app.conversa.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Random;

import ee.app.conversa.management.Foreground;
import ee.app.conversa.services.NewMessageService;

/**
 * Created by edgargomez on 7/4/16.
 */
public class CustomParseReceiver extends ParsePushBroadcastReceiver {

    private final String TAG = CustomParseReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        super.onPushOpen(context, intent);
//        // Send a Parse Analytics "push opened" event
//        ParseAnalytics.trackAppOpenedInBackground(intent);
//
//        String uriString = null;
//        try {
//            JSONObject pushData = new JSONObject(intent.getStringExtra(KEY_PUSH_DATA));
//            uriString = pushData.optString("uri", null);
//        } catch (JSONException e) {
//            Log.e(TAG, "Unexpected JSONException when receiving push data: ", e);
//        }
//
//        Class<? extends Activity> cls = getActivity(context, intent);
//        Intent activityIntent;
//        if (uriString != null) {
//            activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
//        } else {
//            activityIntent = new Intent(context, cls);
//        }
//
//        activityIntent.putExtras(intent.getExtras());
//
//        /*
//          In order to remove dependency on android-support-library-v4
//          The reason why we differentiate between versions instead of just using context.startActivity
//          for all devices is because in API 11 the recommended conventions for app navigation using
//          the back key changed.
//         */
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            //TaskStackBuilderHelper.startActivities(context, cls, activityIntent);
//        } else {
//            //activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            //activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            //context.startActivity(activityIntent);
//        }
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        String pushDataStr = intent.getStringExtra(KEY_PUSH_DATA);

        if (pushDataStr == null) {
            Log.e(TAG, "Can not get push data from intent.");
            return;
        }

        Log.e(TAG, "Received push data: " + pushDataStr);

        JSONObject pushData = null;

        try {
            pushData = new JSONObject(pushDataStr);
        } catch (JSONException e) {
            Log.e(TAG, "Unexpected JSONException when receiving push data: ", e);
        }

        // If the push data includes an action string, that broadcast intent is fired.
        String action = null;

        if (pushData != null) {
            action = pushData.optString("action", null);
        }

        if (action != null) {
            Bundle extras = intent.getExtras();
            Intent broadcastIntent = new Intent();
            broadcastIntent.putExtras(extras);
            broadcastIntent.setAction(action);
            broadcastIntent.setPackage(context.getPackageName());
            context.sendBroadcast(broadcastIntent);
        }

        boolean isBackground;

        try {
            isBackground = Foreground.get().isBackground();
        } catch (IllegalStateException e) {
            isBackground = false;
        }

        if (isBackground) {
            Notification notification = showNotification(context, intent, pushData);

            if (notification != null) {
                // Fire off the notification
                NotificationManager nm =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                // Pick an id that probably won't overlap anything
                int notificationId = (int)System.currentTimeMillis();

                try {
                    nm.notify(notificationId, notification);
                } catch (SecurityException e) {
                    // Some phones throw an exception for unapproved vibration
                    notification.defaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND;
                    nm.notify(notificationId, notification);
                }
            }
        } else {
            // Construct our Intent specifying the Service
            Intent i = new Intent(context, NewMessageService.class);
            // Add extras to the bundle
            i.putExtras(intent);
            // Start the service
            context.startService(i);
        }
    }

    private Notification showNotification(Context context, Intent intent, JSONObject pushData) {
        if (pushData == null)
            return null;

        Log.e(TAG, "Push received: " + pushData.toString());

        String title = pushData.optString("title", "Conversa Business");
        String alert = pushData.optString("alert", "Notification received.");
        String tickerText = String.format(Locale.getDefault(), "%s: %s", title, alert);

        Bundle extras = intent.getExtras();

        Random random = new Random();
        int contentIntentRequestCode = random.nextInt();
        int deleteIntentRequestCode = random.nextInt();

        // Security consideration: To protect the app from tampering, we require that intent filters
        // not be exported. To protect the app from information leaks, we restrict the packages which
        // may intercept the push intents.
        String packageName = context.getPackageName();

        Intent contentIntent = new Intent(ParsePushBroadcastReceiver.ACTION_PUSH_OPEN);
        contentIntent.putExtras(extras);
        contentIntent.setPackage(packageName);

        Intent deleteIntent = new Intent(ParsePushBroadcastReceiver.ACTION_PUSH_DELETE);
        deleteIntent.putExtras(extras);
        deleteIntent.setPackage(packageName);

        PendingIntent pContentIntent = PendingIntent.getBroadcast(context, contentIntentRequestCode,
                contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pDeleteIntent = PendingIntent.getBroadcast(context, deleteIntentRequestCode,
                deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // The purpose of setDefaults(Notification.DEFAULT_ALL) is to inherit notification properties
        // from system defaults
        NotificationCompat.Builder parseBuilder = new NotificationCompat.Builder(context);
        parseBuilder.setContentTitle(title)
                .setContentText(alert)
                .setTicker(tickerText)
                .setSmallIcon(this.getSmallIconId(context, intent))
                .setLargeIcon(this.getLargeIcon(context, intent))
                .setContentIntent(pContentIntent)
                .setDeleteIntent(pDeleteIntent)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL);

        if (alert != null
                && alert.length() > ParsePushBroadcastReceiver.SMALL_NOTIFICATION_MAX_CHARACTER_LIMIT) {
            parseBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(alert));
        }

        return parseBuilder.build();
    }

}