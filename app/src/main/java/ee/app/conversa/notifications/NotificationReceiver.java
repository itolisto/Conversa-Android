package ee.app.conversa.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import ee.app.conversa.ActivityChatWall;
import ee.app.conversa.ActivityMain;
import ee.app.conversa.ConversaApp;
import ee.app.conversa.model.Database.dBusiness;
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
                openFromNotification(intent);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (getIntent().getBooleanExtra(Const.PUSH_INTENT, false)) {
            getIntent().removeExtra(Const.PUSH_INTENT);
            openFromNotification(intent);
        }
    }

    private void openFromNotification(Intent intent) {
        JSONObject additionalData = null;

        try {
            additionalData = new JSONObject(intent.getDataString());
        } catch (JSONException e) {
            return;
        }

        switch (additionalData.optInt("appAction", 0)) {
            case 1:
                String contactId = additionalData.optString("contactId", null);

                if (contactId == null) {
                    break;
                }

                dBusiness user = ConversaApp.getDB().isContact(contactId);
                if (user != null) {
                    // Set extras
                    intent = new Intent(this, ActivityChatWall.class);
                    intent.putExtra(Const.kClassBusiness, user);
                    intent.putExtra(Const.kYapDatabaseName, false);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                break;
            default:
                intent = new Intent(this.getApplicationContext(), ActivityMain.class);
                break;
        }

        startActivity(intent);
    }

}