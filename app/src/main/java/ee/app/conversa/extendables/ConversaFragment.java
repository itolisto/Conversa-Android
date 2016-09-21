package ee.app.conversa.extendables;

import android.support.v4.app.Fragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import ee.app.conversa.events.ContactEvent;
import ee.app.conversa.events.RefreshEvent;
import ee.app.conversa.interfaces.OnContactTaskCompleted;
import ee.app.conversa.management.contact.ContactIntentService;
import ee.app.conversa.model.database.dbBusiness;

/**
 * Created by edgargomez on 9/6/16.
 */
public class ConversaFragment extends Fragment implements OnContactTaskCompleted {

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onContactEvent(ContactEvent event) {
        int action_code = event.getActionCode();

        switch (action_code) {
            case ContactIntentService.ACTION_MESSAGE_SAVE:
                ContactAdded(event.getResponse());
                break;
            case ContactIntentService.ACTION_MESSAGE_UPDATE:
                ContactUpdated(event.getResponse());
                break;
            case ContactIntentService.ACTION_MESSAGE_DELETE:
                ContactDeleted(event.getContactList());
                break;
            case ContactIntentService.ACTION_MESSAGE_RETRIEVE_ALL:
                ContactGetAll(event.getListResponse());
                break;
        }
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

    private void refresh() {
        RefreshEvent stickyEvent = EventBus.getDefault().removeStickyEvent(RefreshEvent.class);
        // Better check that an event was actually posted before
        if(stickyEvent != null) {
            refresh(stickyEvent);
        }
    }

    protected void refresh(RefreshEvent event) {
        /* Child activities override this method */
    }

}
