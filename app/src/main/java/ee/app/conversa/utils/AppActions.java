package ee.app.conversa.utils;

import android.content.Context;
import android.content.Intent;

import com.parse.ParseException;

import androidx.appcompat.app.AppCompatActivity;
import ee.app.conversa.ActivityMain;
import ee.app.conversa.ActivitySignIn;
import ee.app.conversa.ConversaApp;
import ee.app.conversa.management.AblyConnection;
import ee.app.conversa.model.parse.Account;

/**
 * Created by edgargomez on 10/25/16.
 */
public class AppActions {

    public static void initSession(AppCompatActivity activity) {
        Intent intent = new Intent(activity, ActivityMain.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        ConversaApp.getInstance(activity).getPreferences().setGuideExplore(false);
        ConversaApp.getInstance(activity).getPreferences().setShowTutorial(false);
        ConversaApp.getInstance(activity).getPreferences().setUploadQuality(1);
        ConversaApp.getInstance(activity).getPreferences().setDownloadAutomatically(false);
        ConversaApp.getInstance(activity).getPreferences().setPlaySoundWhenSending(true);
        ConversaApp.getInstance(activity).getPreferences().setPlaySoundWhenReceiving(true);
        ConversaApp.getInstance(activity).getPreferences().setPushNotificationSound(true);
        ConversaApp.getInstance(activity).getPreferences().setPushNotificationPreview(true);
        ConversaApp.getInstance(activity).getPreferences().setInAppNotificationSound(true);
        ConversaApp.getInstance(activity).getPreferences().setInAppNotificationPreview(true);

        activity.startActivity(intent);
        activity.finish();
    }

    public static boolean validateParseException(ParseException e) {
        return (e.getCode() == ParseException.INVALID_SESSION_TOKEN ||
                e.getCode() == ParseException.INVALID_LINKED_SESSION);
    }

    public static void appLogout(final Context context, boolean invalidSession) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ConversaApp.getInstance(context).getJobManager().clear();
                if (!ConversaApp.getInstance(context).getDB().deleteDatabase())
                    Logger.error("Logout", "An error has occurred while removing databased. Database not removed");
                ConversaApp.getInstance(context).getDB().refreshDbHelper();
                // Clean shared preferences
                ConversaApp.getInstance(context).getPreferences().cleanSharedPreferences();
            }
        }).start();

        AblyConnection.getInstance().disconnectAbly();
        Account.logOut();

        Intent goToSignIn = new Intent(context, ActivitySignIn.class);

        goToSignIn.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        goToSignIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (invalidSession)
            goToSignIn.putExtra(Const.ACTION, -1);

        context.startActivity(goToSignIn);
    }

}