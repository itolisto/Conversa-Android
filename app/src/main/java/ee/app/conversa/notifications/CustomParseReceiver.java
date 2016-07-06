package ee.app.conversa.notifications;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by edgargomez on 7/4/16.
 */
public class CustomParseReceiver extends ParsePushBroadcastReceiver {

    private final String TAG = "CustomParseReceiver";

    @Override
    protected void onPushReceive(Context context, Intent intent) {
//        super.onPushReceive(context, intent);
//        Log.d("Push", "Push received");
//
//        if (intent == null)
//            return ;
//
//        String jsonData = intent.getStringExtra(KEY_PUSH_DATA);
//
//        Log.d("Push", "JSON Data ["+jsonData+"]");
//
//        String data = getData(jsonData);
//
//
//        // Add custom intent
//        Intent cIntent = new Intent(context, NotificationReceiver.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, cIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        // Create custom notification
//        NotificationCompat.Builder  builder = new NotificationCompat.Builder(context)
//                .setSmallIcon(R.drawable.more_btn_off)
//                .setContentText(data)
//                .setContentTitle("Notification from Parse")
//                .setContentIntent(pendingIntent);
//
//        Notification notification = builder.build();
//
//        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//
//        nm.notify(1410, notification);
        String pushDataStr = intent.getStringExtra(KEY_PUSH_DATA);
        if (pushDataStr == null) {
            Log.e(TAG, "Can not get push data from intent.");
            return;
        }
        Log.v(TAG, "Received push data: " + pushDataStr);

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

        Notification notification = getNotification(context, intent);

        if (notification != null) {
            //ParseNotificationManager.getInstance().showNotification(context, notification);
        }
    }

    private String getData(String jsonData) {
        // Parse JSON Data
        try {
            System.out.println("JSON Data ["+jsonData+"]");
            JSONObject obj = new JSONObject(jsonData);

            return obj.getString("message");
        }
        catch(JSONException jse) {
            jse.printStackTrace();
        }

        return "";
    }

}
