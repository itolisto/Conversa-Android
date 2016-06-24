package ee.app.conversa.sendbird;

import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdEventHandler;
import com.sendbird.android.model.BroadcastMessage;
import com.sendbird.android.model.Channel;
import com.sendbird.android.model.FileLink;
import com.sendbird.android.model.Message;
import com.sendbird.android.model.MessagingChannel;
import com.sendbird.android.model.ReadStatus;
import com.sendbird.android.model.SystemMessage;
import com.sendbird.android.model.TypeStatus;

import ee.app.conversa.utils.Const;

/**
 * Created by edgargomez on 4/21/16.
 */
public class SendBirdController {

    // Step 1 - This interface defines the type of messages I want to communicate to my owner
    public interface ChatControllerListener {
        public void onMessageReceived();
        public void onMessageDelivery();
        public void onReadReceived();
        public void onTypeStartReceived();
        public void onTypeEndReceived();
    }

    // Step 2 - This variable represents the listener passed in by the owning object
    // The listener must implement the events interface and passes messages up to the parent.
    private ChatControllerListener listener;

    // Constructor where listener events are ignored
    public SendBirdController() {
        // set null or default listener or accept as argument to constructor
        this.listener = null;
    }

    // Assign the listener implementing events interface that will receive the events
    public void setSendBirdControllerListener(ChatControllerListener listener) {
        this.listener = listener;
    }

    public static void init() {
        // Init SendBird
        SendBird.init(Const.sbAppId);
        SendBird.login("userId", "userName");
        // Join all channels

    }

    public void joinChannel(String channelURL, String userId) {
        SendBird.join(channelURL);
        SendBird.setEventHandler(new SendBirdEventHandler() {
            @Override
            public void onConnect(Channel channel) {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onChannelLeft(Channel channel) {

            }

            @Override
            public void onMessageReceived(Message message) {
                if (listener != null)
                    listener.onMessageReceived();
            }

            @Override
            public void onMutedMessageReceived(Message message) {

            }

            @Override
            public void onSystemMessageReceived(SystemMessage systemMessage) {

            }

            @Override
            public void onBroadcastMessageReceived(BroadcastMessage broadcastMessage) {

            }

            @Override
            public void onFileReceived(FileLink fileLink) {

            }

            @Override
            public void onMutedFileReceived(FileLink fileLink) {

            }

            @Override
            public void onReadReceived(ReadStatus readStatus) {
                if (listener != null)
                    listener.onReadReceived();
            }

            @Override
            public void onTypeStartReceived(TypeStatus typeStatus) {
                if (listener != null)
                    listener.onTypeStartReceived();
            }

            @Override
            public void onTypeEndReceived(TypeStatus typeStatus) {
                if (listener != null)
                    listener.onTypeEndReceived();
            }

            @Override
            public void onAllDataReceived(SendBird.SendBirdDataType sendBirdDataType, int i) {

            }


            @Override
            public void onMessageDelivery(boolean b, String s, String s1, String s2) {
                if (listener != null)
                    listener.onMessageDelivery();
            }

            @Override
            public void onMessagingStarted(MessagingChannel messagingChannel) {

            }

            @Override
            public void onMessagingUpdated(MessagingChannel messagingChannel) {

            }

            @Override
            public void onMessagingEnded(MessagingChannel messagingChannel) {

            }

            @Override
            public void onAllMessagingEnded() {

            }

            @Override
            public void onMessagingHidden(MessagingChannel messagingChannel) {

            }

            @Override
            public void onAllMessagingHidden() {

            }
        });

        SendBird.connect();
        SendBird.startMessaging(userId); // Start a 1:1 messaging with given userId.
    }

    public void disconnect() {
        SendBird.disconnect();
    }

    private void sendMessage(String message) {
        SendBird.send(message);
    }

}
