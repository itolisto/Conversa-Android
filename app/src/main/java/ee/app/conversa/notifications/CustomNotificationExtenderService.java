package ee.app.conversa.notifications;

import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationDisplayedResult;
import com.onesignal.OSNotificationPayload;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.json.JSONObject;

import java.math.BigInteger;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.FragmentUsersChat;
import ee.app.conversa.extendables.ConversaActivity;
import ee.app.conversa.model.Database.Message;
import ee.app.conversa.model.Database.dBusiness;
import ee.app.conversa.model.Parse.Account;
import ee.app.conversa.model.Parse.Business;
import ee.app.conversa.model.Parse.pMessage;
import ee.app.conversa.utils.Const;

/**
 * Created by edgargomez on 7/21/16.
 */
public class CustomNotificationExtenderService extends NotificationExtenderService {

    private final String TAG = CustomNotificationExtenderService.class.getSimpleName();
    public static final String PARAM_OUT_MSG = "omsg";

    @Override
    protected boolean onNotificationProcessing(OSNotificationPayload notification) {
        JSONObject pushData = notification.additionalData;

        Log.d("NotifExtenderService", "Full additionalData:\n" + pushData.toString());

        switch (pushData.optInt("appAction", 0)) {
            case 1:
                String messageId = pushData.optString("messageId", null);
                String contactId = pushData.optString("contactId", null);
                String messageType = pushData.optString("messageType", null);

                if (messageId == null || contactId == null || messageType == null) {
                    return true;
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
                        Log.e(TAG, "Error consiguiendo informacion de Business " + e.getMessage());
                        return true;
                    }

                    // 3. If Customer was found, save to Local Database
                    dBusiness dbcustomer = new dBusiness();
                    dbcustomer.setBusinessId(contactId);
                    dbcustomer.setDisplayName(customer.getAbout());
                    dbcustomer.setConversaId(customer.getConversaID());
                    dbcustomer.setAbout(customer.getAbout());
                    dbcustomer.setStatusMessage(customer.getStatus());
                    dbcustomer.setAvatarThumbFileId("");
                    dbcustomer = ConversaApp.getDB().saveContact(dbcustomer);

                    if (dbcustomer.getId() == -1) {
                        Log.e(TAG, "Error guardando Business ");
                        return true;
                    } else {
                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction(FragmentUsersChat.UsersReceiver.ACTION_RESP);
                        broadcastIntent.putExtra(PARAM_OUT_MSG, dbcustomer);
                        ConversaApp.getLocalBroadcastManager().sendBroadcast(broadcastIntent);
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
                    return true;
                }

                // 3. If message was found, save to Local Database
                Message dbmessage = new Message();
                dbmessage.setMessageType(Const.kMessageTypeText);
                dbmessage.setBody(message.getText());
                dbmessage.setDeliveryStatus(Message.statusAllDelivered);
                dbmessage.setToUserId(Account.getCurrentUser().getObjectId());
                dbmessage.setFromUserId(contactId);
                dbmessage = ConversaApp.getDB().saveMessage(dbmessage);
                // 4. Broadcast result as from IntentService ain't possible to access ui thread
                if (dbmessage.getId() == -1) {
                    Log.e(TAG, "Error guardando Message ");
                    return true;
                } else {
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction(ConversaActivity.MessageReceiver.ACTION_RESP);
                    broadcastIntent.putExtra(PARAM_OUT_MSG, dbmessage);
                    ConversaApp.getLocalBroadcastManager().sendBroadcast(broadcastIntent);
                }

                break;
            default:
                return true;
        }

        OverrideSettings overrideSettings = new OverrideSettings();
        overrideSettings.extender = new NotificationCompat.Extender() {
            @Override
            public NotificationCompat.Builder extend(NotificationCompat.Builder builder) {
                // Sets the background notification color to Green on Android 5.0+ devices.
                return builder.setColor(new BigInteger("FF00FF00", 16).intValue());
            }
        };

        OSNotificationDisplayedResult result = displayNotification(overrideSettings);
        Log.d("OneSignalExample", "Notification displayed with id: " + result.notificationId);

        // Return true to stop the notifications from displaying.
        return false;
    }
}