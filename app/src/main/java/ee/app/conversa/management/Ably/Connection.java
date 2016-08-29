package ee.app.conversa.management.ably;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;

import ee.app.conversa.management.message.CustomMessageService;
import ee.app.conversa.model.parse.Account;
import ee.app.conversa.utils.Logger;
import io.ably.lib.realtime.AblyRealtime;
import io.ably.lib.realtime.Channel;
import io.ably.lib.realtime.ChannelState;
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


public class Connection implements Channel.MessageListener, Presence.PresenceListener, CompletionListener, ConnectionStateListener, ChannelStateListener {

    private final String TAG = Connection.class.getSimpleName();
    private static Connection instance;
    private final Context context;
    private boolean firstLoad;
    private AblyRealtime ablyRealtime;

    public static void initAblyManager(Context context) {
        instance = new Connection(context);
    }

    public static Connection getInstance() {
        if (instance == null) {
            return null;
        }

        return instance;
    }

    private Connection(Context context) {
        this.context = context;
        this.firstLoad = true;
    }

    public void initAbly()  {
        try {
            ClientOptions clientOptions = new ClientOptions();
            clientOptions.key = "zmxQkA.0hjFJg:-DRtJj8oaEifjs-_";
            clientOptions.logLevel = io.ably.lib.util.Log.ERROR;
            clientOptions.clientId = generateDeviceUUID();
            // Ably Realtime library will open and maintain a connection to the Ably realtime servers
            // as soon as it is instanced
            ablyRealtime = new AblyRealtime(clientOptions);
            // Register listener for state changes
            ablyRealtime.connection.on(this);
        } catch (AblyException e) {
            Log.e(TAG, "InitAbly method exception: " + e.getMessage());
        }
    }

    public void disconnectAbly() {
        if (ablyRealtime != null) {
            ablyRealtime.close();
        }
    }

    /**
     *
     * MESSAGE LISTENER METHOD
     *
     */
    @Override
    public void onMessage(Message messages) {
        Log.e("onMessage", "message received:  " + messages.toString());

        JSONObject additionalData;

        try {
            additionalData = new JSONObject(messages.data.toString());
        } catch (JSONException e) {
            Logger.error(TAG, "onMessageReceived additionalData fail to parse-> " + e.getMessage());
            return;
        }

        Log.e("NotifOpenedHandler", "Full additionalData:\n" + additionalData.toString());

        switch (additionalData.optInt("appAction", 0)) {
            case 1:
                Intent msgIntent = new Intent(context, CustomMessageService.class);
                msgIntent.putExtra("data", additionalData.toString());
                context.startService(msgIntent);
                break;
        }
    }

    /**
     *
     * PRESENCE LISTENER METHOD
     *
     */
    @Override
    public void onPresenceMessage(PresenceMessage messages) {
        Log.e("onPresenceMessage", "Member " + messages.clientId + " : " + messages.action.toString());

        switch (messages.action) {
            case enter:
                //adapter.addItem(presenceMessage);
                //presentUsers.add(presenceMessage.clientId);
                //updatePresentUsersBadge();
                break;
            case leave:
                //adapter.addItem(presenceMessage);
                //presentUsers.remove(presenceMessage.clientId);
                //updatePresentUsersBadge();
                break;
            case update:
//                if (!messages.clientId.equals(Connection.getInstance().userName)) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            boolean isUserTyping = ((JsonObject) presenceMessage.data).get("isTyping").getAsBoolean();
//                            if (isUserTyping) {
//                                usersCurrentlyTyping.add(presenceMessage.clientId);
//                            } else {
//                                usersCurrentlyTyping.remove(presenceMessage.clientId);
//                            }
//
//                            if (usersCurrentlyTyping.size() > 0) {
//                                StringBuilder messageToShow = new StringBuilder();
//                                switch (usersCurrentlyTyping.size()) {
//                                    case 1:
//                                        messageToShow.append(usersCurrentlyTyping.get(0) + " is typing");
//                                        break;
//                                    case 2:
//                                        messageToShow.append(usersCurrentlyTyping.get(0) + " and ");
//                                        messageToShow.append(usersCurrentlyTyping.get(1) + " are typing");
//                                        break;
//                                    default:
//                                        if (usersCurrentlyTyping.size() > 4) {
//                                            messageToShow.append(usersCurrentlyTyping.get(0) + ", ");
//                                            messageToShow.append(usersCurrentlyTyping.get(1) + ", ");
//                                            messageToShow.append(usersCurrentlyTyping.get(2) + " and other are typing");
//                                        } else {
//                                            int i;
//                                            for (i = 0; i < usersCurrentlyTyping.size() - 1; ++i) {
//                                                messageToShow.append(usersCurrentlyTyping.get(i) + ", ");
//                                            }
//                                            messageToShow.append(" and " + usersCurrentlyTyping.get(i) + " are typing");
//                                        }
//                                }
//                            } else {
//
//                            }
//                        }
//                    });
//                }
                break;
        }
    }

    /**
     *
     * COMPLETION LISTENER METHOD
     *
     */
    @Override
    public void onSuccess() {
        //callback.onConnectionCallback(null);
        Log.e("PresenceRegistration", "\nsuccess success success\nsuccess");
    }

    @Override
    public void onError(ErrorInfo reason) {
        //callback.onConnectionCallback(new Exception(errorInfo.message));
        Log.e("PresenceRegistration", reason.message);
    }

    /**
     *
     * CONNECTION STATE LISTENER METHOD
     *
     */
    @Override
    public void onConnectionStateChanged(ConnectionStateChange connectionStateChange) {
        switch (connectionStateChange.current) {
            case initialized:
                Log.e("onConnectionStateChgd", "Initialized");
                break;
            case connecting:
                Log.e("onConnectionStateChgd", "Connecting");
                break;
            case connected:
                Log.e("onConnectionStateChgd", "Connected");
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
                Log.e("onConnectionStateChgd", "Disconnected");
                //callback.onConnectionCallback(new Exception("Ably connection was disconnected. We will retry connecting again in 30 seconds."));
                break;
            case suspended:
                Log.e("onConnectionStateChgd", "Suspended");
                //callback.onConnectionCallback(new Exception("Ably connection was suspended. We will retry connecting again in 60 seconds."));
                break;
            case closing:
                Log.e("onConnectionStateChgd", "Closing");
                for (Channel channel : ablyRealtime.channels.values()) {
                    channel.unsubscribe();
                    channel.presence.unsubscribe();
                }
                break;
            case closed:
                Log.e("onConnectionStateChgd", "Closed");
                break;
            case failed:
                Log.e("onConnectionStateChgd", "Failed");
                //callback.onConnectionCallback(new Exception("We're sorry, Ably connection failed. Please restart the app."));
                break;
        }
    }

    public void subscribeToChannels() {
        String channelname = Account.getCurrentUser().getObjectId();
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
            Log.e("reattach", "Error while trying to subscribe to channel or presence");
        }
    }

    /**
     *
     * CHANNEL STATE LISTENER METHOD
     *
     */
    @Override
    public void onChannelStateChanged(ChannelState state, ErrorInfo reason) {
        if (reason != null) {
            Log.e("fasdf", reason.message);
            return;
        }

        switch (state) {
            case initialized:
                break;
            case attaching:
                break;
            case attached:
                break;
            case detaching:
                break;
            case detached:
                break;
            case failed:
                break;
        }
    }

    /**
     *
     * HELP METHODS
     *
     */
    public PresenceMessage[] getPresentUsers(String channel) {
        return ablyRealtime.channels.get(channel).presence.get();
    }

    public ConnectionState ablyConnectionStatus() {
        if (ablyRealtime == null) {
            return ConnectionState.disconnected;
        } else {
            return ablyRealtime.connection.state;
        }
    }

    public final String getPublicConnectionId() {
        if (ablyRealtime != null) {
            return ablyRealtime.connection.key;
        }

        return null;
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
}