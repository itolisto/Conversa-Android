package ee.app.conversa.model.Parse;

import android.util.Log;

import com.parse.FunctionCallback;
import com.parse.ParseClassName;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.HashMap;

import ee.app.conversa.ConversaApp;

/**
 * Created by edgargomez on 4/15/16.
 */

@ParseClassName("_User")
public class Account extends ParseUser {

    public static void getCustomerId() {
        HashMap<String, String> params = new HashMap<>();
        ParseCloud.callFunctionInBackground("getCustomerId", params, new FunctionCallback<String>() {
            @Override
            public void done(String result, ParseException e) {
                Log.e("Utils getCustomerId: ", "CUSTOMER_OBJECTID: " + result);
                if(e == null) {
                    // 1. Save Customer object id
                    ConversaApp.getPreferences().setCustomerId(result);
                }
            }
        });
    }

    public String getDisplayName() {
        return getString("displayName");
    }

    public void setDisplayName(String value) {
        put("displayName", value);
    }

    public ParseFile getAvatar() {
        return getParseFile("avatar");
    }

    public void setAvatar(ParseFile value) {
        put("avatar", value);
    }

}
