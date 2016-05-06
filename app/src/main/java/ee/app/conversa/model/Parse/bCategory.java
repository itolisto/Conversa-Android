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

    public void setName(String value) {
        put("name", value);
    }

    public int getRelevance() {
        return getInt("relevance");
    }

    public void setRelevance(int value) {
        put("relevance", value);
    }

    public ParseFile getThumbnail() {
        return getParseFile("thumbnail");
    }

    public void setThumbnail(ParseFile value) {
        put("thumbnail", value);
    }

}