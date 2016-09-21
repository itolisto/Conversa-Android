package ee.app.conversa.model.database;

/**
 * Created by edgargomez on 9/20/16.
 */

public class NotificationInformation {

    private long notification_id;
    private int android_notification_id;
    private String groupId;
    private int count;

    public NotificationInformation(String groupId) {
        // Set default values
        this.notification_id = -1;
        this.android_notification_id = (int) System.currentTimeMillis() / 1000;
        this.groupId = groupId;
        this.count = 1;
    }

    public long getNotificationId() {
        return notification_id;
    }

    public void setNotificationId(long notification_id) {
        this.notification_id = notification_id;
    }

    public int getAndroidNotificationId() {
        return android_notification_id;
    }

    public void setAndroidNotificationId(long android_notification_id) {
        if (android_notification_id < Integer.MIN_VALUE || android_notification_id > Integer.MAX_VALUE) {
            this.android_notification_id = -1;
        }

        this.android_notification_id = (int)android_notification_id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}