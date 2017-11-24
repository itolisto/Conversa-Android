package ee.app.conversa.notifications;

/**
 * Created by edgargomez on 5/16/17.
 */

/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.birbit.android.jobqueue.JobStatus;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.jobs.ReceiveMessageJob;
import ee.app.conversa.utils.Logger;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FirebaseMsgService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Logger.error(TAG, "Message data payload: " + remoteMessage.getData());

            try {
                String data = remoteMessage.getData().toString();
                JSONObject additionalData = new JSONObject(data);
                Logger.error(TAG, "Full additionalData:\n" + additionalData.toString());

                switch (additionalData.optInt("appAction", 0)) {
                    case 1: {
                        // Check if a job for this message already exists, if it does, skip
                        String messageId = additionalData.optString("messageId", "");

                        if (ConversaApp.getInstance(this)
                                .getJobManager()
                                .getJobStatus(messageId) == JobStatus.UNKNOWN)
                        {
                            Logger.error(TAG, "Create new Job from " + TAG);
                            ConversaApp.getInstance(this)
                                    .getJobManager()
                                    .addJob(new ReceiveMessageJob(additionalData.toString(), messageId));
                        } else {
                            Logger.error(TAG, "A Job for this message already exits, skip creation");
                        }
                    }
                }
            } catch (Exception e) {
                Logger.error(TAG, "onMessageReceived additionalData fail to parse-> " + e.getMessage());
            }
        }
    }

}