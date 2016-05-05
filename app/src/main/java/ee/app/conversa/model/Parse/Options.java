package ee.app.conversa.model.Parse;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by edgargomez on 4/15/16.
 */
@ParseClassName("Options")
public class Options extends ParseObject {

    public String getCode() {
        return getString("code");
    }

    public void setCode(String value) {
        put("code", value);
    }

    public String getName() {
        return getString("name");
    }

    public void setName(String value) {
        put("name", value);
    }

    public String getDefaultValue() {
        return getString("defaultValue");
    }

    public void setDefaultValue(String value) {
        put("defaultValue", value);
    }

}
