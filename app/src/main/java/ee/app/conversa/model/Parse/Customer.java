package ee.app.conversa.model.Parse;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by edgargomez on 4/15/16.
 */
@ParseClassName("Customer")
public class Customer extends ParseObject {

    public boolean getGender() {
        return getBoolean("gender");
    }

    public void setGender(boolean value) {
        put("gender", value);
    }

    public String getName() {
        return getString("name");
    }

    public void setName(String value) {
        put("name", value);
    }

    public String getStatus() {
        return getString("status");
    }

    public void setStatus(String value) {
        put("status", value);
    }

}