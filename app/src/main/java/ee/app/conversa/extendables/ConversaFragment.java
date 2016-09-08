package ee.app.conversa.extendables;

import android.support.v4.app.Fragment;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import ee.app.conversa.events.ContactEvent;
import ee.app.conversa.events.RefreshEvent;
import ee.app.conversa.interfaces.OnContactTaskCompleted;
import ee.app.conversa.management.contact.ContactIntentService;
import ee.app.conversa.model.database.dBusiness;

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
        dBusiness response = event.getResponse();
        List<dBusiness> list_response = event.getListResponse();

        if (response == null && list_response == null) {
            Log.e("onMessageEvent", "MessageEvent parameters are null");
            return;
        }

        switch (action_code) {
            case ContactIntentService.ACTION_MESSAGE_SAVE:
                ContactAdded(response);
                break;
            case ContactIntentService.ACTION_MESSAGE_UPDATE:
                ContactUpdated(response);
                break;
            case ContactIntentService.ACTION_MESSAGE_DELETE:
                ContactDeleted(response);
                break;
            case ContactIntentService.ACTION_MESSAGE_RETRIEVE_ALL:
                ContactGetAll(list_response);
                break;
        }
    }

    @Override
    public void ContactGetAll(List<dBusiness> response) {
        /* Child activities override this method */
    }

    @Override
    public void ContactAdded(dBusiness response) {
        /* Child activities override this method */
    }

    @Override
    public void ContactDeleted(dBusiness response) {
        /* Child activities override this method */
    }

    @Override
    public void ContactUpdated(dBusiness response) {
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
