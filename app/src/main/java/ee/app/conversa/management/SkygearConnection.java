package ee.app.conversa.management;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
import ee.app.conversa.notifications.RegistrationIntentService;
import ee.app.conversa.utils.AppActions;
import ee.app.conversa.utils.Logger;
import io.skygear.plugins.chat.*;
import io.skygear.skygear.Container;
import io.skygear.skygear.PubsubContainer;
import io.skygear.skygear.PubsubHandler;
import io.skygear.skygear.SkygearApplication;

/**
 * Created by edgargomez on 8/17/16.
 */
public class SkygearConnection implements PubsubHandler {

    private final String TAG = SkygearConnection.class.getSimpleName();
    private static SkygearConnection instance;
    private final Context context;
    private PubsubContainer skygearInstance;
    private final String clientId;
    private boolean firstLoad;

    public static void initSkygearManager(@NonNull Context context) {
        instance = new SkygearConnection(context);
    }

    @Nullable
    public static SkygearConnection getInstance() {
        if (instance == null) {
            return null;
        }

        return instance;
    }

    private SkygearConnection(Context context) {
        this.context = context;
        this.firstLoad = true;
        this.clientId = generateDeviceUUID();
    }

    public PubsubContainer getSkygearInstance() {
        return skygearInstance;
    }

    public void initAbly()  {
        skygearInstance = Container.defaultContainer(context).getPubsub();
    }

    public void subscribeToPushChannels() {
        if (Container.defaultContainer(context).getPush().getGcmSenderId() != null) {
            Intent gcmTokenRegisterIntent = new Intent(context  , RegistrationIntentService.class);
            context.startService(gcmTokenRegisterIntent);
        }
    }

    public void subscribeToChannels() {
        String channelname = ConversaApp.getInstance(context).getPreferences().getAccountCustomerId();
        skygearInstance.subscribe("upvt:" + channelname, this);
    }

    public void disconnectAbly() {
        if (skygearInstance != null) {
            String channelname = ConversaApp.getInstance(context).getPreferences().getAccountCustomerId();
            skygearInstance.unsubscribeAll("upvt:" + channelname);
        }
    }

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
        return clientId;
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

    @Override
    public void handle(JSONObject additionalData) {
        switch (additionalData.optInt("appAction", 0)) {
            case 1:
                Intent msgIntent = new Intent(context, CustomMessageService.class);
                msgIntent.putExtra("data", additionalData.toString());
                context.startService(msgIntent);
                break;
        }
    }

}