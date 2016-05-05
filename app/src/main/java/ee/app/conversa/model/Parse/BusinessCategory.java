package ee.app.conversa.model.Parse;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by edgargomez on 4/15/16.
 */
@ParseClassName("BusinessCategory")
public class BusinessCategory extends ParseObject {

    public Account getBusiness() {
        return (Account)getParseObject("business");
    }

    public void setBusiness(ParseObject value) {
        put("business", value);
    }

}