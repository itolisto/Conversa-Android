package ee.app.conversa.extendables;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.RelativeLayout;

import ee.app.conversa.ActivityChatWall;
import ee.app.conversa.BaseActivity;
import ee.app.conversa.ConversaApp;
import ee.app.conversa.R;
import ee.app.conversa.adapters.MessagesAdapter;
import ee.app.conversa.interfaces.OnMessageTaskCompleted;
import ee.app.conversa.model.Database.Message;
import ee.app.conversa.response.MessageResponse;
import ee.app.conversa.services.NewMessageService;
import ee.app.conversa.utils.Const;

public class ConversaActivity extends BaseActivity implements OnMessageTaskCompleted {

	protected RelativeLayout mRlPushNotification;

    public final static String PUSH = "ee.app.conversa.ConversaActivity.UPDATE";
    protected MessageReceiver receiver = new MessageReceiver();
    protected final IntentFilter newMessageFilter = new IntentFilter(MessageReceiver.ACTION_RESP);
    protected final IntentFilter mPushFilter = new IntentFilter(MessagesAdapter.PUSH);
    protected final Intent mPushBroadcast = new Intent(PUSH);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Register Listener on Database
        ConversaApp.getDB().setMessageListener(this);
    }

    @Override
	protected void onStart() {
		super.onStart();
		ConversaApp.getLocalBroadcastManager().registerReceiver(mPushReceiver, mPushFilter);
	}

	@Override
	protected void onStop() {
		super.onStop();
		ConversaApp.getLocalBroadcastManager().unregisterReceiver(mPushReceiver);
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

	private void handlePushNotification(Intent intent) {

        String pushMessage  = intent.getStringExtra(Const.PUSH_MESSAGE);
        String pushRead     = intent.getStringExtra(Const.PUSH_READ);
        boolean userWallIsOpened = ActivityChatWall.gIsVisible;

        if(pushMessage != null) {
            String fromUserId = intent.getStringExtra(Const.PUSH_FROM_USER_ID);
            String fromUserName = intent.getStringExtra(Const.PUSH_FROM_NAME);

            // Actualizar mensajes
            ConversaApp.getDB().updateReadMessages(fromUserId);

//                if (UsersManagement.getToUser() != null && fromUser != null) {
//                    boolean userIsValidId = fromUserId.equals(UsersManagement.getToUser().getId());
//
//                    if (userIsValidId && userWallIsOpened) {
//                        MessageReceived(message);
//                        MessageReceived(null);
//                    } else {
//                        if (sharedPrefs.getBoolean("in_app_checkbox_preference", true)) {
//                            if (mRlPushNotification != null) {
//                                String messageText = getPushNotificationBody(message);
//                                PushNotification.show(getApplicationContext(), mRlPushNotification, messageText, fromUser);
//                            }
//                        }
//                    }
//                } else {
//                    if (sharedPrefs.getBoolean("in_app_checkbox_preference", true)) {
//                        if (mRlPushNotification != null) {
//                            String messageText = getPushNotificationBody(message);
//                            PushNotification.show(getApplicationContext(), mRlPushNotification, messageText, fromUser);
//                        }
//                    }
//                }

            if(!userWallIsOpened) {
                Intent id = new Intent();
//                    id.putExtra(Const.ID, fromUser.getId());
                mPushBroadcast.replaceExtras(id);
                ConversaApp.getLocalBroadcastManager().sendBroadcast(mPushBroadcast);
            }
        } else {
            if(pushRead != null) {
                String fromId    = intent.getStringExtra(Const.PUSH_TO_USER_ID);
                if(fromId != null) {
                    ConversaApp.getDB().updateReadMessages(fromId);
                    if(userWallIsOpened)
                        MessageUpdated(null);
                }
            }
        }
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
        /* Child activities override this method */
    }

    public class MessageReceiver extends BroadcastReceiver {
        public static final String ACTION_RESP =
                "conversa.conversaactivity.action.MESSAGE_RECEIVED";

        @Override
        public void onReceive(Context context, Intent intent) {
            Message message = intent.getParcelableExtra(NewMessageService.PARAM_OUT_MSG);
            MessageReceived(message);
        }
    }

}
