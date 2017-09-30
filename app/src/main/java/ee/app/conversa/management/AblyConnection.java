package ee.app.conversa.management;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.events.TypingEvent;
import ee.app.conversa.messaging.CustomMessageService;
import ee.app.conversa.utils.AppActions;
import ee.app.conversa.utils.Logger;
import io.ably.lib.realtime.AblyRealtime;
import io.ably.lib.realtime.Channel;
import io.ably.lib.realtime.ChannelStateListener;
import io.ably.lib.realtime.CompletionListener;
import io.ably.lib.realtime.ConnectionState;
import io.ably.lib.realtime.ConnectionStateListener;
import io.ably.lib.realtime.Presence;
import io.ably.lib.types.AblyException;
import io.ably.lib.types.ClientOptions;
import io.ably.lib.types.ErrorInfo;
import io.ably.lib.types.Message;
import io.ably.lib.types.PresenceMessage;
import io.ably.lib.util.IntentUtils;

/**
 * Created by edgargomez on 8/17/16.
 */
public class AblyConnection implements Channel.MessageListener, Presence.PresenceListener,
        CompletionListener, ConnectionStateListener, ChannelStateListener {

    private final String TAG = AblyConnection.class.getSimpleName();
    private static AblyConnection instance;
    private final Context context;
    private AblyRealtime ablyRealtime;
    private final String clientId;
    private boolean firstLoad;

    public static void initAblyManager(@NonNull Context context) {
        instance = new AblyConnection(context);
    }

    @Nullable
    public static AblyConnection getInstance() {
        if (instance == null) {
            return null;
        }

        return instance;
    }

    private AblyConnection(Context context) {

        this.context = context;
        this.firstLoad = true;
        this.clientId = generateDeviceUUID();

    }

    public AblyRealtime getAblyRealtime() {
        return ablyRealtime;
    }

    public void initAbly()  {
        try {
            ClientOptions clientOptions = new ClientOptions();
            clientOptions.key = "zmxQkA.0hjFJg:-DRtJj8oaEifjs-_";
            clientOptions.logLevel = io.ably.lib.util.Log.ERROR;
            if (this.clientId != null) {
                clientOptions.clientId = clientId;
            }
            // Receive messages that they themselves publish
            clientOptions.echoMessages = false;
            // Ably Realtime library will open and maintain a connection to the Ably realtime servers
            // as soon as it is instanced
            ablyRealtime = new AblyRealtime(clientOptions);
            // Register listener for state changes
            ablyRealtime.connection.on(this);
            // Register local broadcast
            ConversaApp.getInstance(context).getLocalBroadcastManager().registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    ErrorInfo error = IntentUtils.getErrorInfo(intent);
                    if (error != null) {
                        // Handle error
                        return;
                    }
                    // Subscribe to channels / listen for push etc.
                    subscribeToPushChannels();
                }
            }, new IntentFilter("io.ably.broadcast.PUSH_ACTIVATE"));
            ablyRealtime.push.activate(context);
        } catch (AblyException e) {
            Logger.error(TAG, "InitAbly method exception: " + e.getMessage());
        }
    }


    public void subscribeToPushChannels() {
        String channelname = ConversaApp.getInstance(context).getPreferences().getAccountCustomerId();
        ablyRealtime.channels.get("upbc:" + channelname).push.subscribeClientAsync(context, new CompletionListener() {
            @Override
            public void onSuccess() {
                Logger.error("onSuccess", "Public channel subscribed for push");
            }

            @Override
            public void onError(ErrorInfo errorInfo) {
                Logger.error("onError", "Public channel error for push: " + errorInfo.message);
            }
        });

        ablyRealtime.channels.get("upvt:" + channelname).push.subscribeClientAsync(context, new CompletionListener() {
            @Override
            public void onSuccess() {
                Logger.error("onSuccess", "Private channel subscribed for push");
            }

            @Override
            public void onError(ErrorInfo errorInfo) {
                Logger.error("onError", "Private channel error for push: " + errorInfo.message);
            }

        });
    }
    public void subscribeToChannels() {
        String channelname = ConversaApp.getInstance(context).getPreferences().getAccountCustomerId();
        if (!channelname.isEmpty()) {
            for (int i = 0; i < 2; i++) {
                Channel channel;
                if (i == 0) {
                    channel = ablyRealtime.channels.get("upbc:" + channelname);
                } else {
                    channel = ablyRealtime.channels.get("upvt:" + channelname);
                }

                reattach(channel);
            }
        }
    }

    private void reattach(Channel channel) {
        try {
            channel.subscribe(this);
            channel.presence.subscribe(this);
            channel.presence.enter(PresenceMessage.Action.present, this);
        } catch (AblyException e) {
            Logger.error("reattach", "Error while trying to subscribe to channel or presence");
        }
    }

    public void disconnectAbly() {
        if (ablyRealtime != null) {
            ablyRealtime.connection.close();
            ablyRealtime.push.deactivate(context);
        }
    }
/*
    public void subscribeToPushChannels() {
        if (ablyRealtime == null || ConversaApp.getInstance(context).getPreferences().getPushKey().isEmpty())
            return;

        this.ablyRealtime.addPushNotificationsOnChannels()
                .pushType(PNPushType.GCM)
                .channels(getChannels())
                .deviceId(ConversaApp.getInstance(context).getPreferences().getPushKey())
                .async(new PNCallback<PNPushAddChannelResult>() {
            @Override
            public void onResponse(PNPushAddChannelResult result, PNStatus status) {
                Log.e("onResponse", "Result: " + result + ". Status: " + status);
            }
        });
    }*/
/*
    public void disconnectAbly() {
        if (ablyRealtime != null) {
            this.ablyRealtime.unsubscribeAll();
            this.ablyRealtime.removeAllPushNotificationsFromDeviceWithPushToken()
                    .deviceId(ConversaApp.getInstance(context).getPreferences().getPushKey())
                    .pushType(PNPushType.GCM);
        }
    }
*/
    private List<String> getChannels() {
        String channelname = ConversaApp.getInstance(context).getPreferences().getAccountCustomerId();
        List<String> channels = new ArrayList<>(2);
        channels.add("upbc_" + channelname);
        channels.add("upvt_" + channelname);
        return channels;
    }

    /**
     *
     * HELP METHODS
     *
     */
    public void userHasStartedTyping(String channelName) {
        final HashMap<String, Object> params = new HashMap<>(3);
        params.put("userId", ConversaApp.getInstance(context).getPreferences().getAccountCustomerId());
        params.put("channelName", channelName);
        params.put("fromCustomer", 1);
        params.put("isTyping", true);

        ParseCloud.callFunctionInBackground("sendPresenceMessage", params, new FunctionCallback<Integer>() {
            @Override
            public void done(Integer object, ParseException e) {
                if (e != null) {
                    if (AppActions.validateParseException(e)) {
                        AppActions.appLogout(context, true);
                    }
                }
            }
        });
    }

    public void userHasEndedTyping(String channelName) {
        final HashMap<String, Object> params = new HashMap<>(2);
        params.put("userId", ConversaApp.getInstance(context).getPreferences().getAccountCustomerId());
        params.put("channelName", channelName);
        params.put("fromCustomer", 1);

        ParseCloud.callFunctionInBackground("sendPresenceMessage", params, new FunctionCallback<Integer>() {
            @Override
            public void done(Integer object, ParseException e) {
                if (e != null) {
                    if (AppActions.validateParseException(e)) {
                        AppActions.appLogout(context, true);
                    }
                }
            }
        });
    }

    public final String getPublicConnectionId() {
        if (ablyRealtime != null) {
            return ablyRealtime.connection.key;
        }

        return null;
    }

    /*

    @Override
    public void status(PubNub pubnub, PNStatus status) {
        if (status.getCategory() == PNStatusCategory.PNUnexpectedDisconnectCategory) {
            // This event happens when radio / connectivity is lost
        } else if (status.getCategory() == PNStatusCategory.PNConnectedCategory) {
            // Connect event. You can do stuff like publish, and know you'll get it.
            // Or just use the connected event to confirm you are subscribed for
            // UI / internal notifications, etc
        } else if (status.getCategory() == PNStatusCategory.PNReconnectedCategory) {
            // Happens as part of our regular operation. This event happens when
            // radio / connectivity is lost, then regained.
        } else if (status.getCategory() == PNStatusCategory.PNDecryptionErrorCategory) {
            // Handle messsage decryption error. Probably client configured to
            // encrypt messages and on live data feed it received plain text.
        }
    }*/

    @Override
    public void onMessage(Message messages) {
        JSONObject additionalData;

        try {
            additionalData = new JSONObject(messages.data.toString());
        } catch (JSONException e) {
            Logger.error(TAG, "onMessageReceived additionalData fail to parse-> " + e.getMessage());
            return;
        }

        switch (additionalData.optInt("appAction", 0)) {
            case 1:
                Intent msgIntent = new Intent(context, CustomMessageService.class);
                msgIntent.putExtra("data", additionalData.toString());
                context.startService(msgIntent);
                break;
        }
    }
    /*
    @Override
    public void message(PubNub pubnub, PNMessageResult message) {
        // Handle new message stored in message.message
        if (message.getChannel() != null) {
            // Message has been received on channel group stored in
            // message.getChannel()
        } else {
            // Message has been received on channel stored in
            // message.getSubscription()
        }

        JSONObject additionalData;

        try {
            JsonObject mMessage = message.getMessage().getAsJsonObject();
            JsonObject mmMessage;

            if (mMessage.get("message").isJsonObject()) {
                mmMessage = mMessage.getAsJsonObject("message");
                additionalData = new JSONObject(mmMessage.getAsString());
            } else {
                JsonElement jeMessage = mMessage.get("message");
                additionalData = new JSONObject(jeMessage.getAsString());
            }
        } catch (Exception e) {
            Logger.error(TAG, "onMessageReceived additionalData fail to parse-> " + e.getMessage());
            return;
        }

        switch (additionalData.optInt("appAction", 0)) {
            case 1:
                Intent msgIntent = new Intent(context, CustomMessageService.class);
                msgIntent.putExtra("data", additionalData.toString());
                context.startService(msgIntent);
                break;
            case 2:
                Logger.error("onPresenceMessage", additionalData.toString());

                String jeFrom = additionalData.optString("from", "");
                boolean isUserTyping = additionalData.optBoolean("isTyping", false);
                if (!jeFrom.isEmpty())
                    EventBus.getDefault().post(new TypingEvent(jeFrom, isUserTyping));
                break;
        }
    }*/
/*
    @Override
    public void presence(PubNub pubnub, PNPresenceEventResult presence) {
//        Logger.error("onPresenceMessage", "Member " + presenceMessage.clientId + " : " + presenceMessage.action.toString());
//
//        if (presenceMessage.data != null) {
//            JsonElement jeFrom = ((JsonObject) presenceMessage.data).get("from");
//            if (jeFrom != null) {
//                boolean isUserTyping = ((JsonObject) presenceMessage.data).get("isTyping").getAsBoolean();
//                EventBus.getDefault().post(new TypingEvent(jeFrom.getAsString(), isUserTyping));
//            }
//        }
    }*/
    @Override
    public void onPresenceMessage(PresenceMessage presenceMessage) {
        Logger.error("onPresenceMessage", "Member " + presenceMessage.clientId + " : " + presenceMessage.action.toString());

        if (presenceMessage.data != null) {
            JsonElement jeFrom = ((JsonObject) presenceMessage.data).get("from");
            if (jeFrom != null) {
                boolean isUserTyping = ((JsonObject) presenceMessage.data).get("isTyping").getAsBoolean();
                EventBus.getDefault().post(new TypingEvent(jeFrom.getAsString(), isUserTyping));
            }
        }
    }

    @Override
    public void onChannelStateChanged(ChannelStateChange stateChange) {
        if (stateChange.reason != null) {
            Logger.error("onChannelStateChanged", stateChange.reason.message);
        }

    }

    @Override
    public void onSuccess() {
        Logger.error("PresenceRegistration", "\nsuccess success success\nsuccess");
    }

    @Override
    public void onError(ErrorInfo reason) {
        Logger.error("PresenceRegistration", reason.message);
    }

    @Override
    public void onConnectionStateChanged(ConnectionStateChange connectionStateChange) {
        switch (connectionStateChange.current) {
            case initialized:
                Logger.error("onConnectionStateChgd", "Initialized");
                break;
            case connecting:
                Logger.error("onConnectionStateChgd", "Connecting");
                break;
            case connected:
                Logger.error("onConnectionStateChgd", "Connected");
                if (firstLoad) {
                    // Subscribe to all Channels
                    subscribeToChannels();
                    // Change first load
                    firstLoad = false;
                } else {
                    if (ablyRealtime.channels.values().size() == 0) {
                        subscribeToChannels();
                    } else {
                        for (Channel channel : ablyRealtime.channels.values()) {
                            reattach(channel);
                        }
                    }
                }
                break;
            case disconnected:
                Logger.error("onConnectionStateChgd", "Disconnected");
                break;
            case suspended:
                Logger.error("onConnectionStateChgd", "Suspended");
                break;
            case closing:
                Logger.error("onConnectionStateChgd", "Closing");
                for (Channel channel : ablyRealtime.channels.values()) {
                    channel.unsubscribe();
                    channel.presence.unsubscribe();
                }
                break;
            case closed:
                Logger.error("onConnectionStateChgd", "Closed");
                break;
            case failed:
                Logger.error("onConnectionStateChgd", "Failed" + connectionStateChange.reason);
                break;
        }
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
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : result) {
            sb.append(String.format("%02X", b));
        }

        return sb.toString();
    }
}