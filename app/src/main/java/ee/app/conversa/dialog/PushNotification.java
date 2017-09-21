package ee.app.conversa.dialog;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.R;
import ee.app.conversa.model.database.dbNotificationInformation;
import ee.app.conversa.model.database.dbMessage;
import ee.app.conversa.notifications.NotificationDeleted;
import ee.app.conversa.notifications.NotificationPressed;
import ee.app.conversa.utils.Const;

/**
 * Created by edgargomez on 8/22/16.
 */
public class PushNotification {

    public static void showMessageNotification(Context context, String from, String jsonData, dbMessage message, dbNotificationInformation summary) {
        if (ConversaApp.getInstance(context).getPreferences().getPushNotificationPreview()) {
            Intent intentChatWallView = new Intent(context, NotificationPressed.class);
            intentChatWallView.putExtra("notificationId", summary.getNotificationId());
            intentChatWallView.putExtra("data", jsonData);
            intentChatWallView.putExtra("count", summary.getCount());
            // Sets the Activity to start in a new, empty task
            intentChatWallView.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent =
                    PendingIntent.getActivity(
                            context,
                            0,
                            intentChatWallView,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            // Delete intent
            Intent delIntent = new Intent(context, NotificationDeleted.class);
            delIntent.putExtra("notificationId", summary.getNotificationId());
            PendingIntent delPendingIntent = PendingIntent.getBroadcast(context, 0 /* Request code */, delIntent,
                    PendingIntent.FLAG_ONE_SHOT);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setGroup(summary.getGroupId())
                    .setContentTitle(from)
                    .setContentText(getMessage(context, message))
                    .setAutoCancel(true)
                    .setDeleteIntent(delPendingIntent)
                    .setContentIntent(pendingIntent);

            if (ConversaApp.getInstance(context).getPreferences().getPushNotificationSound()) {
                notificationBuilder.setSound(Uri.parse("android.resource://"
                        + context.getPackageName() + "/" + R.raw.sound_notification));
            }

            if (summary.getCount() > 1) {
                notificationBuilder.setGroupSummary(true)
                        .setContentText(context.getString(R.string.notification_stack_text, summary.getCount(),from));
            }

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(summary.getAndroidNotificationId(), notificationBuilder.build());
        }
    }

    private static String getMessage(Context context, dbMessage message) {
        switch (message.getMessageType()) {
            case Const.kMessageTypeText:
                return message.getBody();
            case Const.kMessageTypeAudio:
                return context.getString(R.string.contacts_last_message_audio);
            case Const.kMessageTypeVideo:
                return context.getString(R.string.contacts_last_message_video);
            case Const.kMessageTypeImage:
                return context.getString(R.string.contacts_last_message_image);
            case Const.kMessageTypeLocation:
                return context.getString(R.string.contacts_last_message_location);
            default:
                return context.getString(R.string.contacts_last_message_default);
        }
    }

}
