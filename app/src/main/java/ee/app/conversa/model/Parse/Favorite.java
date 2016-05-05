package ee.app.conversa.model.Parse;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by edgargomez on 4/15/16.
 */
@ParseClassName("Favorite")
public class Favorite extends ParseObject {

    public boolean getIsCurrentlyFavorite() {
        return getBoolean("isCurrentlyFavorite");
    }

    public void setIsCurrentlyFavorite(boolean value) {
        put("isCurrentlyFavorite", value);
    }

    public Account getFromUser() {
        return (Account)getParseObject("fromUser");
    }

    public void setFromUser(ParseObject value) {
        put("fromUser", value);
    }

    public Account getToBusiness() {
        return (Account)getParseObject("toBusiness");
    }

    public void setToBusiness(ParseObject value) {
        put("toBusiness", value);
    }

}