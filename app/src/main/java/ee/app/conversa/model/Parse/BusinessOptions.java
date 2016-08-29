package ee.app.conversa.model.parse;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by edgargomez on 4/15/16.
 */
@ParseClassName("BusinessOptions")
public class BusinessOptions extends ParseObject {

    public String getValue() {
        return getString("value");
    }

    public void setValue(String value) {
        put("value", value);
    }

    public Options getOption() {
        return (Options)getParseObject("option");
    }

    public void setOption(ParseObject value) {
        put("option", value);
    }

}