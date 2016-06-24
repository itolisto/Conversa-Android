package ee.app.conversa.model.Parse;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by edgargomez on 4/15/16.
 */
@ParseClassName("Contact")
public class Contact extends ParseObject {

    public boolean getActiveChat() {
        return getBoolean("activeChat");
    }

    public void setActiveChat(boolean value) {
        put("activeChat", value);
    }

    public ParseObject getToBusiness() {
        return getParseObject("toBusiness");
    }

    public void setToBusiness(ParseObject value) {
        put("toBusiness", value);
    }

    public ParseObject getFromUser() {
        return getParseObject("fromUser");
    }

    public void setFromUser(ParseObject value) {
        put("fromUser", value);
    }

}