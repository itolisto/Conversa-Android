package ee.app.conversa.model.Parse;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseUser;

/**
 * Created by edgargomez on 4/15/16.
 */

@ParseClassName("_User")
public class Account extends ParseUser {

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
