package ee.app.conversa.extendables;

import android.content.Intent;
import android.util.Log;
import android.widget.RelativeLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import ee.app.conversa.BaseActivity;
import ee.app.conversa.R;
import ee.app.conversa.dialog.InAppPushNotification;
import ee.app.conversa.events.MessageEvent;
import ee.app.conversa.interfaces.OnMessageTaskCompleted;
import ee.app.conversa.management.message.MessageIntentService;
import ee.app.conversa.model.database.dbMessage;
import ee.app.conversa.utils.Logger;

public class ConversaActivity extends BaseActivity implements OnMessageTaskCompleted {

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
	protected void onStart() {
		super.onStart();
        EventBus.getDefault().register(this);
	}

    @Override
	protected void onStop() {
        EventBus.getDefault().unregister(this);
		super.onStop();
	}

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        int action_code = event.getActionCode();
        dbMessage response = event.getResponse();
        List<dbMessage> list_response = event.getListResponse();

        if (response == null && list_response == null) {
            Log.e("onMessageEvent", "MessageEvent parameters are null");
            return;
        }

        switch (action_code) {
            case MessageIntentService.ACTION_MESSAGE_SAVE:
                MessageSent(response);
                break;
            case MessageIntentService.ACTION_MESSAGE_NEW_MESSAGE:
                MessageReceived(response);
                break;
            case MessageIntentService.ACTION_MESSAGE_UPDATE:
            case MessageIntentService.ACTION_MESSAGE_UPDATE_UNREAD:
                MessageUpdated(response);
                break;
            case MessageIntentService.ACTION_MESSAGE_DELETE:
                MessageDeleted(response);
                break;
            case MessageIntentService.ACTION_MESSAGE_RETRIEVE_ALL:
                MessagesGetAll(list_response);
                break;
        }
    }

    @Override
    protected void initialization() {
        super.initialization();
        if (mRlPushNotification == null) {
            mRlPushNotification = (RelativeLayout) findViewById(R.id.rlPushNotification);
        }
    }

    protected void openFromNotification(Intent intent) {
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
            InAppPushNotification.make(getApplicationContext(), mRlPushNotification).show(response.getBody(), response.getFromUserId());
        }
    }

    @Override
    public void MessageDeleted(final dbMessage response) {
        /* Child activities override this method */
    }

    @Override
    public void MessageUpdated(final dbMessage response) {
        /* Child activities override this method */
    }

}