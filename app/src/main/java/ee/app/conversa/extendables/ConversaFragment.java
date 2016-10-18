package ee.app.conversa.extendables;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import ee.app.conversa.events.TypingEvent;
import ee.app.conversa.events.contact.ContactDeleteEvent;
import ee.app.conversa.events.contact.ContactRetrieveEvent;
import ee.app.conversa.events.contact.ContactSaveEvent;
import ee.app.conversa.events.contact.ContactUpdateEvent;
import ee.app.conversa.events.message.MessageDeleteEvent;
import ee.app.conversa.events.message.MessageIncomingEvent;
import ee.app.conversa.events.message.MessageOutgoingEvent;
import ee.app.conversa.events.message.MessageUpdateEvent;
import ee.app.conversa.interfaces.OnContactTaskCompleted;
import ee.app.conversa.interfaces.OnMessageTaskCompleted;
import ee.app.conversa.messaging.MessageUpdateReason;
import ee.app.conversa.model.database.dbBusiness;
import ee.app.conversa.model.database.dbMessage;

/**
 * Created by edgargomez on 9/6/16.
 */
public class ConversaFragment extends Fragment implements OnContactTaskCompleted, OnMessageTaskCompleted {

    protected boolean unregisterListener = true;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        unregisterListener = true;
        return super.onCreateView(inflater, container, savedInstanceState);
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
    public void onContactSaveEvent(ContactSaveEvent event) {
        ContactAdded(event.getContact());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onContactUpdateEvent(ContactUpdateEvent event) {
        ContactUpdated(event.getContact());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onContactDeleteEvent(ContactDeleteEvent event) {
        ContactDeleted(event.getContactList());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onContactRetrieveEvent(ContactRetrieveEvent event) {
        ContactGetAll(event.getListResponse());
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
    public void ContactDeleted(List<String> contacts) {
        /* Child activities override this method */
    }

    @Override
    public void ContactUpdated(dbBusiness response) {
        /* Child activities override this method */
    }

    @Override
    public void MessagesGetAll(List<dbMessage> response) {
        /* Child activities override this method */
    }

    @Override
    public void MessageSent(dbMessage response) {
        /* Child activities override this method */
    }

    @Override
    public void MessageReceived(dbMessage response) {
        /* Child activities override this method */
    }

    @Override
    public void MessageDeleted(List<String> response) {
        /* Child activities override this method */
    }

    @Override
    public void MessageUpdated(dbMessage response, MessageUpdateReason reason) {
        /* Child activities override this method */
    }

    @Override
    public void onTypingMessage(String from, boolean isTyping) {
        /* Child activities override this method */
    }

}