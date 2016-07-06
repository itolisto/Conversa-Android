package ee.app.conversa.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import ee.app.conversa.ActivitySplashScreen;
import ee.app.conversa.utils.Const;

/**
 * Created by edgargomez on 5/5/15.
 */
public class NotificationReceiver extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getBooleanExtra(Const.PUSH_INTENT, false)) {
                intent.removeExtra(Const.PUSH_INTENT);
                openWallFromNotification(intent);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (getIntent().getBooleanExtra(Const.PUSH_INTENT, false)) {
            getIntent().removeExtra(Const.PUSH_INTENT);
            openWallFromNotification(intent);
        }
    }

    private void openWallFromNotification(Intent intent) {
        final String pushMessageId  = intent.getStringExtra(Const.PUSH_MESSAGE);

        if(pushMessageId != null) {
            final String fromUserId   = intent.getStringExtra(Const.PUSH_FROM_USER_ID);

            if(fromUserId != null) {
//                User fromUser = ConversaApp.getDB().isContact(fromUserId);
//                if (fromUser == null) {
//                    try {
//                        fromUser = new GetUserByIdAsync(this).execute(fromUserId).get(6500, TimeUnit.MILLISECONDS);
//                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
//                        return;
//                    }
//                }

//                if (fromUser != null) {
                    //UsersManagement.setToUser(fromUser);
                    //SettingsManager.ResetSettings();

//                    if (ActivityChatWall.gCurrentMessages != null)
//                        ActivityChatWall.gCurrentMessages.clear();

//                    startActivity(new Intent(getApplicationContext(), ActivityChatWall.class));
//                    finish();
//                } else {
//                    startActivity(new Intent(getApplicationContext(), ActivityMain.class));
//                    finish();
//                }
            }
        } else {
            startActivity(new Intent(getApplicationContext(), ActivitySplashScreen.class));
            finish();
        }
    }

//    private class GetUserByIdAsync extends ConversaAsync<String, Void, User> {
//        @Override
//        protected User backgroundWork(String... params) throws JSONException, IOException, ConversaException, IllegalStateException, ConversaForbiddenException {
//            String userId = params[0];
//            return CouchDB.addContact(userId);
//        }
//        @Override
//        protected void onPreExecute()               { super.onPreExecute(); }
//        @Override
//        protected void onPostExecute(User result)   { super.onPostExecute(result); }
//        protected GetUserByIdAsync(Context context) { super(context); }
//    }
}