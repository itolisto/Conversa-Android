package ee.app.conversa;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdEventHandler;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.SendBirdNotificationHandler;
import com.sendbird.android.SendBirdSystemEventHandler;
import com.sendbird.android.handler.RegisterPushTokenHandler;
import com.sendbird.android.model.BroadcastMessage;
import com.sendbird.android.model.Channel;
import com.sendbird.android.model.FileLink;
import com.sendbird.android.model.Mention;
import com.sendbird.android.model.Message;
import com.sendbird.android.model.MessagingChannel;
import com.sendbird.android.model.ReadStatus;
import com.sendbird.android.model.SystemEvent;
import com.sendbird.android.model.SystemMessage;
import com.sendbird.android.model.TypeStatus;
import com.sendbird.android.shadow.com.google.gson.JsonElement;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.List;

import ee.app.conversa.model.Parse.Account;
import ee.app.conversa.notifications.CustomNotificationExtenderService;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Logger;

/**
 * Created by edgargomez on 8/8/16.
 */
public class SendBirdManager implements SendBirdEventHandler, SendBirdNotificationHandler, SendBirdSystemEventHandler, RegisterPushTokenHandler {

    private final String TAG = this.getClass().getSimpleName();
    private static SendBirdManager sInstance;
    private final Context context;

    public SendBirdManager(Context context) {
        this.context = context;
    }

    public static void initSendBirdManager(Context context) {
        // Initialize SendBird SDK using APP ID.
        SendBird.init(context, Const.sbAppId);
        sInstance = new SendBirdManager(context);
    }

    public static SendBirdManager getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("SendBird instance is not created");
        }

        return sInstance;
    }

    public void setGCMToken(String gcmToken, boolean isNewToken) {
        if (sInstance == null) {
            throw new IllegalStateException("SendBird must be initialize before joining a channel");
        }

        if (gcmToken == null) {
            SendBird.login(SendBird.LoginOption.build(generateDeviceUUID()));
        } else {
            SendBird.login(SendBird.LoginOption.build(generateDeviceUUID()).setGCMRegToken(gcmToken));
        }

        SendBird.setEventHandler(this);
        SendBird.registerNotificationHandler(this);
        SendBird.registerSystemEventHandler(this);
        SendBird.registerPushToken(this);

        if (isNewToken) {
            ConversaApp.getPreferences().setCustomerId("", false);
        }

        if (ConversaApp.getPreferences().getCustomerId().isEmpty()) {
            // 1. Get Customer Id
            Account.getCustomerId();
        } else {

        }
    }

    public void connectSendBird() {
        if (sInstance == null) {
            throw new IllegalStateException("SendBird must be initialize before trying to connect");
        }

        SendBird.connect();
    }

    public void disconnectSendBird() {
        if (sInstance == null) {
            throw new IllegalStateException("SendBird must be initialize before trying to disconnect");
        }

        SendBird.disconnect();
    }

    public void joinChannels(List<String> channels) {
        if (sInstance == null) {
            throw new IllegalStateException("SendBird must be initialize before joining a list of channels");
        }

        SendBird.join(channels);
        SendBird.connect();
    }

    private static String generateDeviceUUID() {
        String serial = android.os.Build.SERIAL;
        String androidID = Settings.Secure.ANDROID_ID;
        String deviceUUID = serial + androidID;

        MessageDigest digest;
        byte[] result;
        try {
            digest = MessageDigest.getInstance("SHA-1");
            result = digest.digest(deviceUUID.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : result) {
            sb.append(String.format("%02X", b));
        }

        return sb.toString();
    }

    /** SendBirdEventHandler **/
    @Override
    public void onConnect(Channel channel) {
        Logger.error(TAG, "On connect-> " + channel);
    }

    @Override
    public void onError(int i) {
        Logger.error(TAG, "Error code-> " + i);
    }

    @Override
    public void onChannelLeft(Channel channel) {

    }

    @Override
    public void onMessageReceived(Message message) {
        Logger.error(TAG, "onMessageReceived-> " + message.toJson());
        JSONObject additionalData;

        try {
            additionalData = new JSONObject(message.getData());
            additionalData.put("message", message.getMessage());
        } catch (JSONException e) {
            Logger.error(TAG, "onMessageReceived additionalData fail to parse-> " + e.getMessage());
            return;
        }

        Log.e("NotifOpenedHandler", "Full additionalData:\n" + additionalData.toString());

        switch (additionalData.optInt("appAction", 0)) {
            case 1:
                Intent msgIntent = new Intent(context, CustomNotificationExtenderService.class);
                msgIntent.putExtra("data", additionalData.toString());
                context.startService(msgIntent);
                break;
            default:
                return;
        }
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

    }

    @Override
    public void onTypeStartReceived(TypeStatus typeStatus) {

    }

    @Override
    public void onTypeEndReceived(TypeStatus typeStatus) {

    }

    @Override
    public void onAllDataReceived(SendBird.SendBirdDataType sendBirdDataType, int i) {

    }

    @Override
    public void onMessageDelivery(boolean b, String s, String s1, String s2) {

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

    /** SendBirdNotificationHandler **/
    @Override
    public void onMessagingChannelUpdated(MessagingChannel messagingChannel) {

    }

    @Override
    public void onMentionUpdated(Mention mention) {

    }

    /** SendBirdSystemEventHandler **/
    @Override
    public void onSystemEventReceived(SystemEvent systemEvent) {
        if(systemEvent.getCategory() == SystemEvent.CATEGORY_CHANNEL_JOIN) {
            Logger.error(TAG, "onSystemEventReceived-> CATEGORY_CHANNEL_JOIN");
            String channelUrl = SendBird.getChannelUrl();
            String userId = systemEvent.getDataAsString("user_id");
            String nickname = systemEvent.getDataAsString("nickname");
            String profile_url = systemEvent.getDataAsString("profile_url");
            String is_muted = systemEvent.getDataAsString("is_muted");

        } else if(systemEvent.getCategory() == SystemEvent.CATEGORY_CHANNEL_LEAVE) {
            Logger.error(TAG, "onSystemEventReceived-> CATEGORY_CHANNEL_LEAVE");
        } else if(systemEvent.getCategory() == SystemEvent.CATEGORY_USER_CHANNEL_MUTE) {
            Logger.error(TAG, "onSystemEventReceived-> CATEGORY_USER_CHANNEL_MUTE");
        }
    }

    /** RegisterPushTokenHandler **/
    @Override
    public void onError(SendBirdException e) {
        Logger.error(TAG, "OnError-> " + e.getMessage());
    }

    @Override
    public void onSuccess(JsonElement jsonElement) {
        Logger.error(TAG, "onSuccess-> " + jsonElement.toString());
    }

}