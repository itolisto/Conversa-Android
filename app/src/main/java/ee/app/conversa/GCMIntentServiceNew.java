/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ee.app.conversa;

import android.app.IntentService;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.concurrent.ExecutionException;

import ee.app.conversa.notifications.NotificationReceiver;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Logger;


/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GCMIntentServiceNew extends IntentService {
	private static int mNotificationCounter = 0;
    private static int NOTIFICATION_ID = 0;
    public static final String TAG = "GCM Demo";
    public final static String PUSH = "ee.app.conversa.GCMIntentServiceNew.PUSH";
    private static final Intent mPushBroadcast = new Intent(PUSH);

    private NotificationManager mNotificationManager;

    public GCMIntentServiceNew() { super("GCMIntentServiceNew"); }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " + extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // Service doing some work. Post notification of received message.
                Bundle pushExtras   = intent.getExtras();

                try {
                    boolean appIsInForeground = new ConversaApp.ForegroundCheckAsync().execute(getApplication()).get();
                    boolean screenLocked = ((KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE)).inKeyguardRestrictedInputMode();

                    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                    if(appIsInForeground && !screenLocked){
                        mPushBroadcast.replaceExtras(pushExtras);
                        ConversaApp.getLocalBroadcastManager().sendBroadcast(mPushBroadcast);
                    } else {
                        // Ver si es mensaje o actualizacion de mensajes
                        String pushMessage  = intent.getStringExtra(Const.PUSH_MESSAGE);
                        String pushRead     = intent.getStringExtra(Const.PUSH_READ);
                        if(pushMessage != null) {
                            String pushMessageName    = intent.getStringExtra(Const.PUSH_FROM_NAME);
                            String pushMessageContent = intent.getStringExtra(Const.PUSH_MESSAGE_CONTENT);
                            String fromUserId         = intent.getStringExtra(Const.PUSH_FROM_USER_ID);

                            if (pushMessageName != null && pushMessageContent != null && fromUserId != null) {
                                ConversaApp.getDB().setHasPendingMessages(fromUserId, 1);
                                if(sharedPrefs.getBoolean("notification_checkbox_preference", true)) {
                                    triggerNotification(pushMessageName, pushMessageContent, pushExtras);
                                }
                            }
                        } else {
                            if(pushRead != null) {
                                String fromId    = intent.getStringExtra(Const.PUSH_TO_USER_ID);
                                if(fromId != null) {
                                    ConversaApp.getDB().updateReadMessagesMe(fromId);
                                }
                            }
                        }
                    }
                } catch(InterruptedException|ExecutionException e) {
                    Logger.error(TAG, "Couldn't create notification");
                }
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }

    public void triggerNotification(String fromName, String content, Bundle pushExtras) {
        if (pushExtras != null) {
            mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

            Intent intent = new Intent(this, NotificationReceiver.class);
            intent.replaceExtras(pushExtras);
            intent.putExtra(Const.PUSH_INTENT, true);

            PendingIntent contentIntent = PendingIntent.getActivity(this,
                    mNotificationCounter, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            content = fromName + ": " + content;

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.icon_notification)
                    .setContentTitle(Const.APP_NAME)
                    .setWhen(System.currentTimeMillis())
                    .setContentText(content)
                    .setContentIntent(contentIntent)
                    .setGroup(pushExtras.getString(Const.PUSH_FROM_USER_ID))
                    .setAutoCancel(true);

            mNotificationCounter++;

            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(mNotificationCounter, mBuilder.build());
        }
    }

    // Put the message into a notification and post it.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, ActivitySplashScreen.class), 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon_notification)
                .setContentTitle(Const.APP_NAME)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Error: " + msg))
                .setContentText(msg);

        NOTIFICATION_ID++;

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}