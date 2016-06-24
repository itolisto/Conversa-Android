package ee.app.conversa.model.Parse;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

/**
 * Created by edgargomez on 4/15/16.
 */
@ParseClassName("Category")
public class bCategory extends ParseObject {

    public String getName() {
        return getString("name");
    }

    public ParseFile getThumbnail() {
        return getParseFile("thumbnail");
    }

}