package ee.app.conversa.utils;

import android.content.Context;
import android.content.Intent;

import com.onesignal.OneSignal;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.Collection;

import ee.app.conversa.ActivitySignIn;
import ee.app.conversa.ConversaApp;
import ee.app.conversa.management.AblyConnection;
import ee.app.conversa.model.parse.Account;

/**
 * Created by edgargomez on 10/25/16.
 */
public class AppActions {

    public static boolean validateParseException(ParseException e) {
        return (e.getCode() == ParseException.INVALID_SESSION_TOKEN ||
                e.getCode() == ParseException.INVALID_LINKED_SESSION);
    }

    public static void appLogout(Context context, boolean invalidSession) {
        ConversaApp.getInstance(context).getPreferences().cleanSharedPreferences();

        if (!ConversaApp.getInstance(context).getDB().deleteDatabase())
            Logger.error("Logout", "An error has occurred while removing databased. Database not removed");

        Collection<String> tempList = new ArrayList<>(2);
        tempList.add("upbc");
        tempList.add("upvt");
        OneSignal.deleteTags(tempList);
        OneSignal.clearOneSignalNotifications();
        OneSignal.setSubscription(false);
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