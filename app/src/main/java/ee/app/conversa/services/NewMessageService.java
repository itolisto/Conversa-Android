package ee.app.conversa.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseQuery;

import org.json.JSONException;
import org.json.JSONObject;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.FragmentUsersChat;
import ee.app.conversa.extendables.ConversaActivity;
import ee.app.conversa.model.Database.Message;
import ee.app.conversa.model.Database.dBusiness;
import ee.app.conversa.model.Parse.Business;
import ee.app.conversa.model.Parse.pMessage;
import ee.app.conversa.utils.Const;

/**
 * Created by edgargomez on 7/7/16.
 */
public class NewMessageService extends IntentService {

    private final String TAG = NewMessageService.class.getSimpleName();
    public static final String PARAM_OUT_MSG = "omsg";

    // Must create a default constructor
    public NewMessageService() {
        // Used to name the worker thread, important only for debugging.
        super("NewMessageService");
    }

    @Override
    public void onCreate() {
        super.onCreate(); // if you override onCreate(), make sure to call super().
        // If a Context object is needed, call getApplicationContext() here.
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String pushDataStr = intent.getStringExtra(ParsePushBroadcastReceiver.KEY_PUSH_DATA);
        JSONObject pushData = new JSONObject();

        try {
            pushData = new JSONObject(pushDataStr);
        } catch (JSONException e) {
            Log.e(TAG, "Unexpected JSONException when receiving push data: ", e);
        }

        switch (pushData.optInt("appAction", 0)) {
            case 1:
                String messageId = pushData.optString("messageId", null);
                String contactId = pushData.optString("contactId", null);

                if (messageId == null || contactId == null) {
                    return;
                }

                // 1. Find if user is already a contact
                if(ConversaApp.getDB().isContact(contactId) == null) {
                    // 2. Call Parse for User information
                    ParseQuery<Business> query = ParseQuery.getQuery(Business.class);

                    //Collection<String> collection = new ArrayList<>();
                    //collection.add(Const.kUserUsernameKey);
                    //query.selectKeys(collection);

                    Business customer;

                    try {
                        customer = query.get(contactId);
                    } catch (ParseException e) {
                        Log.e(TAG, "Error consiguiendo informacion de Customer " + e.getMessage());
                        return;
                    }

                    // 3. If Customer was found, save to Local Database
                    dBusiness dbcustomer = new dBusiness();
                    dbcustomer.setBusinessId(contactId);
                    dbcustomer.setDisplayName(customer.getAbout());
                    dbcustomer.setAbout(customer.getAbout());
                    dbcustomer.setStatusMessage(customer.getStatus());
                    dbcustomer.setAvatarThumbFileId("");
                    ConversaApp.getDB().saveContact(dbcustomer);

                    if (dbcustomer.getId() == -1) {
                        Log.e(TAG, "Error guardando Contacto ");
                    } else {
                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction(FragmentUsersChat.UsersReceiver.ACTION_RESP);
                        broadcastIntent.putExtra(PARAM_OUT_MSG, dbcustomer);
                        sendBroadcast(broadcastIntent);
                    }
                }

                // 2. Get message information
                ParseQuery<pMessage> query = ParseQuery.getQuery(pMessage.class);

                //Collection<String> collection = new ArrayList<>();
                //collection.add(Const.kUserUsernameKey);
                //query.selectKeys(collection);

                pMessage message;

                try {
                    message = query.get(messageId);
                } catch (ParseException e) {
                    Log.e(TAG, "Error consiguiendo informacion de Message " + e.getMessage());
                    return;
                }

                // 3. If message was found, save to Local Database
                Message dbmessage = new Message();
                dbmessage.setMessageType(Const.kMessageTypeText);
                dbmessage.setBody(message.getText());
                dbmessage.setDeliveryStatus(Message.statusAllDelivered);
                dbmessage.setToUserId(ConversaApp.getPreferences().getCustomerId());
                dbmessage.setFromUserId(contactId);
                dbmessage = ConversaApp.getDB().saveMessage(dbmessage);
                // 4. Broadcast result as from IntentService ain't possible to access ui thread
                if (dbmessage.getId() == -1) {
                    Log.e(TAG, "Error guardando Message ");
                } else {
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction(ConversaActivity.MessageReceiver.ACTION_RESP);
                    broadcastIntent.putExtra(PARAM_OUT_MSG, dbmessage);
                    ConversaApp.getLocalBroadcastManager().sendBroadcast(broadcastIntent);
                }

                break;
        }
    }

}