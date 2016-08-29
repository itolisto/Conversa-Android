package ee.app.conversa.model.parse;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

/**
 * Created by edgargomez on 4/15/16.
 */
@ParseClassName("Business")
public class Business extends ParseObject {

    public String getDisplayName() {
        return getString("displayName");
    }

    public String getConversaID() {
        return getString("conversaID");
    }

    public String getAbout() {
        return getString("about");
    }

    public ParseFile getAvatar() {
        return getParseFile("avatar");
    }

}