package ee.app.conversa.management.contact;

import android.app.IntentService;
import android.content.Intent;
import android.database.SQLException;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.events.ContactEvent;
import ee.app.conversa.events.RefreshEvent;
import ee.app.conversa.model.database.dBusiness;

/**
 * Created by edgargomez on 8/10/16.
 */
public class ContactIntentService extends IntentService {

    public static final String TAG = "ContactIntentService";

    // Intent constants
    public static final String INTENT_EXTRA_ACTION_CODE = "action_code";
    public static final String INTENT_EXTRA_CUSTOMER = "customer_single";
    public static final String INTENT_EXTRA_CUSTOMER_LIST = "customer_list";

    // MESSAGE ACTIONS
    public static final int ACTION_MESSAGE_SAVE = 1;
    public static final int ACTION_MESSAGE_UPDATE = 2;
    public static final int ACTION_MESSAGE_DELETE = 3;
    public static final int ACTION_MESSAGE_RETRIEVE_ALL = 4;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public ContactIntentService() {
        super("ContactIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.getExtras() == null) {
            return;
        }

        int actionCode = intent.getExtras().getInt(INTENT_EXTRA_ACTION_CODE, 0);
        dBusiness user = intent.getExtras().getParcelable(INTENT_EXTRA_CUSTOMER);
        List<dBusiness> users = new ArrayList<>(20);
        boolean refresh = true;

        try {
            switch (actionCode) {
                case ACTION_MESSAGE_SAVE:
                    if (user != null) {
                        user = ConversaApp.getInstance(this).getDB().saveContact(user);
                    }
                    break;
                case ACTION_MESSAGE_UPDATE:
                    break;
                case ACTION_MESSAGE_DELETE:
                    if (user != null) {
                        user = ConversaApp.getInstance(this).getDB().deleteContactById(user);
                    }
                    break;
                case ACTION_MESSAGE_RETRIEVE_ALL:
                    users = ConversaApp.getInstance(this).getDB().getAllContacts();
                    refresh = false;
                    break;
                default:
                    return;
            }
        } catch (SQLException e) {
            Log.e("ContactAsyncTaskRunner", "No se pudo guardar usuario porque ocurrio el siguiente error: " + e.getMessage());
            return;
        }

        // 5. Notify listeners
        EventBus.getDefault().post(new ContactEvent(actionCode, user, users));

        if (refresh) {
            EventBus.getDefault().postSticky(new RefreshEvent(true));
        }
    }

}
