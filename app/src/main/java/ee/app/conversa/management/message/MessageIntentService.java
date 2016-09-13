package ee.app.conversa.management.message;

import android.app.IntentService;
import android.content.Intent;
import android.database.SQLException;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.events.MessageEvent;
import ee.app.conversa.jobs.message.SendMessageJob;
import ee.app.conversa.model.database.dbMessage;

/**
 * Created by edgargomez on 8/10/16.
 */
public class MessageIntentService extends IntentService {

    public static final String TAG = "MessageIntentService";

    // Intent constants
    public static final String INTENT_EXTRA_ACTION_CODE = "action_code";
    public static final String INTENT_EXTRA_MESSAGE = "message_single";

    // Specific Intent constants
    public static final String INTENT_EXTRA_UPDATE_STATUS = "update_status";
    public static final String INTENT_EXTRA_CONTACT_ID = "contact_id";
    public static final String INTENT_EXTRA_MESSAGE_COUNT = "message_count";
    public static final String INTENT_EXTRA_MESSAGE_SKIP = "message_skip";

    // MESSAGE ACTIONS
    public static final int ACTION_MESSAGE_SAVE = 1;
    public static final int ACTION_MESSAGE_NEW_MESSAGE = 2;
    public static final int ACTION_MESSAGE_UPDATE = 3;
    public static final int ACTION_MESSAGE_DELETE = 4;
    public static final int ACTION_MESSAGE_RETRIEVE_ALL = 5;
    public static final int ACTION_MESSAGE_UPDATE_UNREAD = 6;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public MessageIntentService() {
        super("MessageIntentService");
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.getExtras() == null) {
            return;
        }

        int actionCode = intent.getExtras().getInt(INTENT_EXTRA_ACTION_CODE, 0);
        dbMessage message = intent.getExtras().getParcelable(INTENT_EXTRA_MESSAGE);
        List<dbMessage> messages = null;

        try {
            switch (actionCode) {
                case ACTION_MESSAGE_SAVE: {
                    message = ConversaApp.getInstance(this).getDB().saveMessage(message);
                    break;
                }
                case ACTION_MESSAGE_NEW_MESSAGE: {
                    message = ConversaApp.getInstance(this).getDB().saveMessage(message);
                    break;
                }
                case ACTION_MESSAGE_UPDATE: {
                    String status = intent.getExtras().getString(INTENT_EXTRA_UPDATE_STATUS, dbMessage.statusParseError);
                    int result = ConversaApp.getInstance(this).getDB().updateDeliveryStatus(message.getId(), status);
                    if (result > 0) {
                        message.setDeliveryStatus(status);
                    }
                    break;
                }
                case ACTION_MESSAGE_UPDATE_UNREAD: {
                    String contact_id = intent.getExtras().getString(INTENT_EXTRA_CONTACT_ID);
                    ConversaApp.getInstance(this).getDB().updateReadMessages(contact_id);
                    return;
                }
                case ACTION_MESSAGE_RETRIEVE_ALL: {
                    String contact_id = intent.getExtras().getString(INTENT_EXTRA_CONTACT_ID);
                    int count = intent.getExtras().getInt(INTENT_EXTRA_MESSAGE_COUNT, 0);
                    int skip = intent.getExtras().getInt(INTENT_EXTRA_MESSAGE_SKIP, 0);
                    messages = ConversaApp.getInstance(this).getDB().getMessagesByContact(contact_id, count, skip);
                    break;
                }
                default: {
                    return;
                }
            }
        } catch (SQLException e) {
            Log.e("MessageAsyncTaskRunner", "No se pudo guardar mensaje porque ocurrio el siguiente error: " + e.getMessage());
            return;
        }

        if (actionCode == ACTION_MESSAGE_SAVE) {
            if (message != null && message.getId() != -1) {
                ConversaApp.getInstance(getApplicationContext())
                        .getJobManager()
                        .addJob(new SendMessageJob(message.getId(), message.getToUserId()));
            }
        } else if (actionCode == ACTION_MESSAGE_NEW_MESSAGE) {
            if (message != null && message.getId() != -1) {
                ConversaApp.getInstance(getApplicationContext())
                        .getJobManager()
                        .addJob(new SendMessageJob(message.getId(), message.getToUserId()));
            }
        } else {
            EventBus.getDefault().post(new MessageEvent(
                    actionCode,
                    message,
                    messages));
        }
    }

}