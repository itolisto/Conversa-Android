package ee.app.conversa.model.parse;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

/**
 * Created by edgargomez on 4/15/16.
 */
@ParseClassName("Customer")
public class Customer extends ParseObject {

    public void setName(String name) {
        put("name", name);
    }

    public void setDiplayName(String displayName) {
        put("displayName", displayName);
    }

    public void setAvatar(ParseFile avatar) {
        put("avatar", avatar);
    }

    public void setGender(boolean gender) {
        put("gender", gender);
    }

    public void setStatus(String status) {
        put("status", status);
    }

    public void setBirthday(String birthday) {
        put("birthday", birthday);
    }

}