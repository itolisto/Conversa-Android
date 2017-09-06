package ee.app.conversa.model.parse;

import com.parse.ParseClassName;
import com.parse.ParseUser;

/**
 * Created by edgargomez on 4/15/16.
 */

@ParseClassName("_User")
public class Account extends ParseUser {

    public void setEmail(String email) {
        put("email", email);
    }

    public void setPassword(String password) {
        put("password", password);
    }

}
