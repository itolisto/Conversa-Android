package ee.app.conversa.notifications;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseQuery;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.FragmentUsersChat;
import ee.app.conversa.extendables.ConversaActivity;
import ee.app.conversa.model.Database.dBusiness;
import ee.app.conversa.model.Database.dbMessage;
import ee.app.conversa.model.Parse.Account;
import ee.app.conversa.model.Parse.Business;
import ee.app.conversa.model.Parse.pMessage;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Foreground;
import ee.app.conversa.utils.Logger;

/**
 * Created by edgargomez on 7/21/16.
 */
public class CustomNotificationExtenderService extends IntentService {

    private final String TAG = getClass().getSimpleName();
    public static final String PARAM_OUT_MSG = "omsg";

    public CustomNotificationExtenderService() {
        super("CustomNotificationExtenderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.getExtras() == null) {
            return;
        }

        String json = intent.getExtras().getString("data", "");

        JSONObject additionalData;

        try {
            additionalData = new JSONObject(json);
        } catch (JSONException e) {
            Logger.error(TAG, "onMessageReceived payload fail to parse-> " + e.getMessage());
            return;
        }

        Log.e("NotifOpenedHandler", "Full additionalData:\n" + additionalData.toString());

        switch (additionalData.optInt("appAction", 0)) {
            case 1:
                String messageId = additionalData.optString("messageId", null);
                String contactId = additionalData.optString("contactId", null);
                String messageType = additionalData.optString("messageType", null);

                if (messageId == null || contactId == null || messageType == null) {
                    return;
                }

                dBusiness dbcustomer = null;

                // 1. Find if user is already a contact
                if(ConversaApp.getDB().isContact(contactId) == null) {
                    // 2. Call Parse for User information
                    ParseQuery<Business> query = ParseQuery.getQuery(Business.class);
                    query.whereEqualTo(Const.kBusinessActiveKey, true);

                    Collection<String> collection = new ArrayList<>();
                    collection.add(Const.kBusinessDisplayNameKey);
                    collection.add(Const.kBusinessConversaIdKey);
                    collection.add(Const.kBusinessAboutKey);
                    collection.add(Const.kBusinessStatusKey);
                    collection.add(Const.kBusinessAvatarKey);
                    query.selectKeys(collection);

                    Business customer;

                    try {
                        customer = query.get(contactId);
                    } catch (ParseException e) {
                        Log.e(TAG, "Error consiguiendo informacion de Business " + e.getMessage());
                        return;
                    }

                    // 3. If Customer was found, save to Local Database
                    dbcustomer = new dBusiness();
                    dbcustomer.setBusinessId(contactId);
                    dbcustomer.setDisplayName(customer.getDisplayName());
                    dbcustomer.setConversaId(customer.getConversaID());
                    dbcustomer.setAbout(customer.getAbout());
                    dbcustomer.setStatusMessage(customer.getStatus());
                    try {
                        if (customer.getAvatar() != null) {
                            dbcustomer.setAvatarThumbFileId(customer.getAvatar().getUrl());
                        } else {
                            dbcustomer.setAvatarThumbFileId("");
                        }
                    } catch (IllegalStateException e) {
                        dbcustomer.setAvatarThumbFileId("");
                    }

                    dbcustomer = ConversaApp.getDB().saveContact(dbcustomer);

                    if (dbcustomer.getId() == -1) {
                        Log.e(TAG, "Error guardando Business");
                        return;
                    }
                }

                // 2. Get message information
                pMessage parseMessage = null;

                if (additionalData.optBoolean("callParse", false)) {
                    ParseQuery<pMessage> query = ParseQuery.getQuery(pMessage.class);
                    Collection<String> collection = new ArrayList<>();

                    switch (messageType) {
                        case Const.kMessageTypeText:
                            collection.add(Const.kMessageTextKey);
                            break;
                        case Const.kMessageTypeAudio:
                        case Const.kMessageTypeVideo:
                        case Const.kMessageTypeImage:
                            collection.add(Const.kMessageFileKey);
                            break;
                        case Const.kMessageTypeLocation:
                            collection.add(Const.kMessageLocationKey);
                            break;
                    }

                    query.selectKeys(collection);

                    try {
                        parseMessage = query.get(messageId);
                    } catch (ParseException e) {
                        Log.e(TAG, "Error consiguiendo informacion de Message " + e.getMessage());
                        return;
                    }
                }

                // 3. If message was found, save to Local Database
                dbMessage dbmessage = new dbMessage();
                dbmessage.setFromUserId(contactId);
                dbmessage.setToUserId(Account.getCurrentUser().getObjectId());
                dbmessage.setMessageType(messageType);
                dbmessage.setDeliveryStatus(dbMessage.statusAllDelivered);
                dbmessage.setMessageId(messageId);

                switch (messageType) {
                    case Const.kMessageTypeText:
                        if (parseMessage == null) {
                            dbmessage.setBody(additionalData.optString("message", ""));
                        } else {
                            dbmessage.setBody(parseMessage.getText());
                        }
                        break;
                    case Const.kMessageTypeAudio:
                    case Const.kMessageTypeVideo:
                        dbmessage.setBytes(additionalData.optInt("size", 0));
                        dbmessage.setDuration(additionalData.optInt("duration", 0));
                        if (parseMessage == null) {
                            dbmessage.setFileId(additionalData.optString("file", ""));
                        } else {
                            try {
                                if (parseMessage.getFile() != null) {
                                    dbmessage.setFileId(parseMessage.getFile().getUrl());
                                } else {
                                    dbmessage.setFileId("");
                                }
                            } catch (IllegalStateException e) {
                                dbmessage.setFileId("");
                            }
                        }
                        break;
                    case Const.kMessageTypeImage:
                        dbmessage.setBytes(additionalData.optInt("size", 0));
                        dbmessage.setWidth(additionalData.optInt("width", 0));
                        dbmessage.setHeight(additionalData.optInt("height", 0));
                        if (parseMessage == null) {
                            dbmessage.setFileId(additionalData.optString("file", ""));
                        } else {
                            try {
                                if (parseMessage.getFile() != null) {
                                    dbmessage.setFileId(parseMessage.getFile().getUrl());
                                } else {
                                    dbmessage.setFileId("");
                                }
                            } catch (IllegalStateException e) {
                                dbmessage.setFileId("");
                            }
                        }
                        break;
                    case Const.kMessageTypeLocation:
                        dbmessage.setLatitude((float)additionalData.optDouble("latitude", 0));
                        dbmessage.setLongitude((float)additionalData.optDouble("longitude", 0));
                        break;
                }

                dbmessage = ConversaApp.getDB().saveMessage(dbmessage);

                if (dbmessage.getId() == -1) {
                    Log.e(TAG, "Error guardando Message");
                    return;
                }

                // 4. Broadcast results as from IntentService ain't possible to access ui thread
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(ConversaActivity.MessageReceiver.ACTION_RESP);
                broadcastIntent.putExtra(PARAM_OUT_MSG, dbmessage);
                ConversaApp.getLocalBroadcastManager().sendBroadcast(broadcastIntent);

                if (dbcustomer != null) {
                    broadcastIntent = new Intent();
                    broadcastIntent.setAction(FragmentUsersChat.UsersReceiver.ACTION_RESP);
                    broadcastIntent.putExtra(PARAM_OUT_MSG, dbcustomer);
                    ConversaApp.getLocalBroadcastManager().sendBroadcast(broadcastIntent);
                }
                break;
            default:
                return;
        }

        if (Foreground.get().isBackground()) {
            Log.e("OneSignalExample", "Notification displayed with id: " + 1);

        } else {

        }
    }

}