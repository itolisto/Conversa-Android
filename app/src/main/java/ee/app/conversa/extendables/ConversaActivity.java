package ee.app.conversa.extendables;

import android.content.Intent;
import android.widget.RelativeLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import ee.app.conversa.R;
import ee.app.conversa.contact.ContactUpdateReason;
import ee.app.conversa.dialog.InAppNotification;
import ee.app.conversa.events.TypingEvent;
import ee.app.conversa.events.contact.ContactDeleteEvent;
import ee.app.conversa.events.contact.ContactRetrieveEvent;
import ee.app.conversa.events.contact.ContactSaveEvent;
import ee.app.conversa.events.contact.ContactUpdateEvent;
import ee.app.conversa.events.message.MessageDeleteEvent;
import ee.app.conversa.events.message.MessageIncomingEvent;
import ee.app.conversa.events.message.MessageOutgoingEvent;
import ee.app.conversa.events.message.MessageRetrieveEvent;
import ee.app.conversa.events.message.MessageUpdateEvent;
import ee.app.conversa.interfaces.OnContactTaskCompleted;
import ee.app.conversa.interfaces.OnMessageTaskCompleted;
import ee.app.conversa.messaging.MessageUpdateReason;
import ee.app.conversa.model.database.dbBusiness;
import ee.app.conversa.model.database.dbMessage;
import ee.app.conversa.utils.Logger;

public class ConversaActivity extends BaseActivity implements OnMessageTaskCompleted,
        OnContactTaskCompleted {

    protected boolean unregisterListener = true;
	protected RelativeLayout mRlPushNotification;

    @Override
    protected void onNewIntent(Intent intent) {
        Logger.error("onNewIntent", "\nIntent: " + intent);
        super.onNewIntent(intent);
        if (intent != null) {
            openFromNotification(intent);
        }
    }

    @Override
    protected void initialization() {
        super.initialization();
        if (mRlPushNotification == null) {
            mRlPushNotification = (RelativeLayout) findViewById(R.id.rlPushNotification);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onStop() {
        if (unregisterListener) {
            EventBus.getDefault().unregister(this);
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTypingEvent(TypingEvent event) {
        onTypingMessage(event.getFrom(), event.isTyping());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageOutgoingEvent(MessageOutgoingEvent event) {
        MessageSent(event.getMessage());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageIncomingEvent(MessageIncomingEvent event) {
        MessageReceived(event.getMessage());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageUpdateEvent(MessageUpdateEvent event) {
        MessageUpdated(event.getMessage(), event.getReason());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageDeleteEvent(MessageDeleteEvent event) {
        MessageDeleted(event.getMessageList());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageRetrieveEvent(MessageRetrieveEvent event) {
        MessagesGetAll(event.getMessageList());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onContactSaveEvent(ContactSaveEvent event) {
        ContactAdded(event.getContact());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onContactUpdateEvent(ContactUpdateEvent event) {
        ContactUpdated(event.getContact(), event.getReason());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onContactDeleteEvent(ContactDeleteEvent event) {
        ContactDeleted(event.getContactList());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onContactRetrieveEvent(ContactRetrieveEvent event) {
        ContactGetAll(event.getListResponse());
    }

    /* ********************************************************************** */
    /* ********************************************************************** */

    protected void openFromNotification(Intent intent) {
        /* Child activities override this method */
    }

    @Override
    public void onTypingMessage(String from, boolean isTyping) {
        /* Child activities override this method */
    }

    @Override
    public void MessagesGetAll(final List<dbMessage> response) {
        /* Child activities override this method */
    }

    @Override
    public void MessageSent(final dbMessage response) {
        /* Child activities override this method */
    }

    @Override
    public void MessageReceived(dbMessage response) {
        // Show in-app notification
        if (mRlPushNotification != null) {
            InAppNotification.make(this, mRlPushNotification).show(response);
        }
    }

    @Override
    public void MessageDeleted(final List<String> response) {
        /* Child activities override this method */
    }

    @Override
    public void MessageUpdated(final dbMessage response, MessageUpdateReason reason) {
        /* Child activities override this method */
    }

    @Override
    public void ContactGetAll(List<dbBusiness> response) {
        /* Child activities override this method */
    }

    @Override
    public void ContactAdded(dbBusiness response) {
        /* Child activities override this method */
    }

    @Override
    public void ContactDeleted(List<String> response) {
        /* Child activities override this method */
    }

    @Override
    public void ContactUpdated(dbBusiness response, ContactUpdateReason reason) {
        /* Child activities override this method */
    }

}