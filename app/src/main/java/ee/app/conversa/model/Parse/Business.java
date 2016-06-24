package ee.app.conversa.model.Parse;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by edgargomez on 4/15/16.
 */
@ParseClassName("Business")
public class Business extends ParseObject {

    public String getConversaID() {
        return getString("conversaID");
    }

    public void setConversaID(String value) {
        put("conversaID", value);
    }

    public String getAbout() {
        return getString("about");
    }

    public void setAbout(String value) {
        put("about", value);
    }

    public ParseObject getBusinessInfo() {
        return getParseObject("businessInfo");
    }

    public void setBusinessInfo(ParseObject value) {
        put("businessInfo", value);
    }

}