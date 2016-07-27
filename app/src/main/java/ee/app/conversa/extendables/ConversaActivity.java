package ee.app.conversa.extendables;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.RelativeLayout;

import ee.app.conversa.BaseActivity;
import ee.app.conversa.ConversaApp;
import ee.app.conversa.R;
import ee.app.conversa.adapters.MessagesAdapter;
import ee.app.conversa.dialog.PushNotification;
import ee.app.conversa.interfaces.OnMessageTaskCompleted;
import ee.app.conversa.model.Database.Message;
import ee.app.conversa.notifications.CustomNotificationExtenderService;
import ee.app.conversa.response.MessageResponse;

public class ConversaActivity extends BaseActivity implements OnMessageTaskCompleted {

	protected RelativeLayout mRlPushNotification;
    private boolean mPushHandledOnNewIntent = false;
    public final static String PUSH = "ee.app.conversa.ConversaActivity.UPDATE";
    protected MessageReceiver receiver = new MessageReceiver();
    protected final IntentFilter newMessageFilter = new IntentFilter(MessageReceiver.ACTION_RESP);
    protected final IntentFilter mPushFilter = new IntentFilter(MessagesAdapter.PUSH);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Register Listener on Database
        ConversaApp.getDB().setMessageListener(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        mPushHandledOnNewIntent = true;
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPushHandledOnNewIntent) {
            mPushHandledOnNewIntent = false;
            final Bundle extras = getIntent().getExtras();
            if (extras != null) {
                openFromNotification(extras);
            }
        }
    }

    @Override
	protected void onStart() {
		super.onStart();
		ConversaApp.getLocalBroadcastManager().registerReceiver(mPushReceiver, mPushFilter);
        ConversaApp.getLocalBroadcastManager().registerReceiver(receiver, newMessageFilter);
	}

	@Override
	protected void onStop() {
		super.onStop();
		ConversaApp.getLocalBroadcastManager().unregisterReceiver(mPushReceiver);
        ConversaApp.getLocalBroadcastManager().unregisterReceiver(receiver);
	}

    @Override
    protected void initialization() {
        super.initialization();
        if (mRlPushNotification == null) {
            mRlPushNotification = (RelativeLayout) findViewById(R.id.rlPushNotification);
        }
    }

    private BroadcastReceiver mPushReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			handlePushNotification(intent);
		}
	};

    protected void openFromNotification(Bundle extras) {
        /* Child activities override this method */
    }

	protected void handlePushNotification(Intent intent) {
        /* Child activities override this method */
	}

    @Override
    public void MessagesGetAll(MessageResponse response) {
        /* Child activities override this method */
    }

    @Override
    public void MessageSent(MessageResponse response) {
        /* Child activities override this method */
    }

    @Override
    public void MessageDeleted(MessageResponse response) {
        /* Child activities override this method */
    }

    @Override
    public void MessageUpdated(MessageResponse response) {
        /* Child activities override this method */
    }

    public void MessageReceived(Message message) {
        // Show in-app notification
        if (mRlPushNotification != null) {
            PushNotification.make(getApplicationContext(), mRlPushNotification).show(message.getBody(), message.getFromUserId());
        }
    }

    public class MessageReceiver extends BroadcastReceiver {
        public static final String ACTION_RESP =
                "conversa.conversaactivity.action.MESSAGE_RECEIVED";

        @Override
        public void onReceive(Context context, Intent intent) {
            Message message = intent.getParcelableExtra(CustomNotificationExtenderService.PARAM_OUT_MSG);
            MessageReceived(message);
        }
    }

}