package ee.app.conversa.extendables;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.RelativeLayout;

import ee.app.conversa.ActivityChatWall;
import ee.app.conversa.BaseActivity;
import ee.app.conversa.ConversaApp;
import ee.app.conversa.R;
import ee.app.conversa.adapters.MessagesAdapter;
import ee.app.conversa.model.Database.Message;
import ee.app.conversa.utils.Const;

public class ConversaActivity extends BaseActivity {

	protected RelativeLayout mRlPushNotification;

    public final static String PUSH = "ee.app.conversa.ConversaActivity.UPDATE";
    private final IntentFilter mPushFilter = new IntentFilter(MessagesAdapter.PUSH);
    private static final Intent mPushBroadcast = new Intent(PUSH);

    protected void refreshWallMessages(Message message) {
        /*Child activities override this method */
    }

    @Override
	protected void onResume() {
		super.onResume();
        if (mRlPushNotification == null) {
            mRlPushNotification = (RelativeLayout) findViewById(R.id.rlPushNotification);
        }
		ConversaApp.getLocalBroadcastManager().registerReceiver(mPushReceiver, mPushFilter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		ConversaApp.getLocalBroadcastManager().unregisterReceiver(mPushReceiver);
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

            if(fromUserId != null) {

                Message message = null;

//                try {
//                    message = new GetMessageByIdAsync(this).execute(pushMessage).get();
//                } catch (InterruptedException|ExecutionException e) {
//                    return;
//                }

//                User fromUser = ConversaApp.getDB().isContact(fromUserId);
//                if(fromUser == null) {
//                    try {
//                        fromUser = new GetUserByIdAsync(this).execute(fromUserId).get();
//                    } catch (InterruptedException|ExecutionException e) {
//                        return;
//                    }
//                }

                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                // Actualizar mensajes
                ConversaApp.getDB().updateReadMessagesMe(fromUserId);
//                ConversaApp.getDB().updateReadMessages(fromUserId);

//                if (UsersManagement.getToUser() != null && fromUser != null) {
//                    boolean userIsValidId = fromUserId.equals(UsersManagement.getToUser().getId());
//
//                    if (userIsValidId && userWallIsOpened) {
//                        refreshWallMessages(message);
//                        refreshWallMessages(null);
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
            }
        } else {
            if(pushRead != null) {
                String fromId    = intent.getStringExtra(Const.PUSH_TO_USER_ID);
                if(fromId != null) {
                    ConversaApp.getDB().updateReadMessagesMe(fromId);
                    if(userWallIsOpened)
                        refreshWallMessages(null);
                }
            }
        }
	}

    private String getPushNotificationBody(Message lastMessage) {
        String value = "";
        if(lastMessage == null) {
            return value;
        } else {
            switch(lastMessage.getMessageType()) {
                case Const.kMessageTypeImage:
                    value = getString(R.string.ca_picture);
                    break;
                case Const.kMessageTypeLocation:
                    value = getString(R.string.ca_location);
                    break;
                case Const.kMessageTypeText:
                    if(lastMessage.getBody().length() > 19) {
                        value = lastMessage.getBody().substring(0,19).concat("...");
                    } else {
                        value = lastMessage.getBody();
                    }
                    break;
                default:
                    value = getString(R.string.ca_message);
                    break;
            }
        }
        return value;
    }

}
