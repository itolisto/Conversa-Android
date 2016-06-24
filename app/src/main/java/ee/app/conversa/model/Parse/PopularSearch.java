package ee.app.conversa.model.Parse;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by edgargomez on 4/15/16.
 */
@ParseClassName("PopularSearch")
public class PopularSearch extends ParseObject {

    public Business getBusiness() {
        return (Business)getParseObject("business");
    }

    public void setBusiness(ParseObject value) {
        put("business", value);
    }

}