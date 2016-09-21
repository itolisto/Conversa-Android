package ee.app.conversa.dialog;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import ee.app.conversa.ActivityMain;
import ee.app.conversa.R;
import ee.app.conversa.model.database.NotificationInformation;
import ee.app.conversa.model.database.dbMessage;
import ee.app.conversa.notifications.NotificationDeleted;
import ee.app.conversa.notifications.NotificationPressed;
import ee.app.conversa.utils.Const;

/**
 * Created by edgargomez on 8/22/16.
 */
public class PushNotification {

    public static void showMessageNotification(Context context, String from, String jsonData, dbMessage message, NotificationInformation summary) {
        // Press intent
        Intent intent = new Intent(context, NotificationPressed.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("notificationId", summary.getNotificationId());
        intent.putExtra("data", jsonData);
        // The stack builder object will contain an artificial back stack for `the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ActivityMain.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Delete intent
        Intent delIntent = new Intent(context, NotificationDeleted.class);
        delIntent.putExtra("notificationId", summary.getNotificationId());
        PendingIntent delPendingIntent = PendingIntent.getBroadcast(context, 0 /* Request code */, delIntent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.icon_notification)
                .setGroup(summary.getGroupId())
                .setContentTitle(from)
                .setContentText(getMessage(context, message))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setDeleteIntent(delPendingIntent)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (summary.getCount() > 1) {
            notificationBuilder.setGroupSummary(true).setContentText("You have " + summary.getCount() + " new messages from " + from);
        }

        Log.e("PushNotification", "Notification displayed with id: " + summary.getAndroidNotificationId());
        notificationManager.notify(summary.getAndroidNotificationId(), notificationBuilder.build());
    }

    private static String getMessage(Context context, dbMessage message) {
        switch (message.getMessageType()) {
            case Const.kMessageTypeText:
                return message.getBody();
            case Const.kMessageTypeAudio:
                return context.getString(R.string.google_app_id);
            case Const.kMessageTypeVideo:
                return context.getString(R.string.google_app_id);
            case Const.kMessageTypeImage:
                return context.getString(R.string.google_app_id);
            case Const.kMessageTypeLocation:
                return context.getString(R.string.google_app_id);
            default:
                return context.getString(R.string.google_app_id);
        }
    }

}
