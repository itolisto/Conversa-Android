package ee.app.conversa.model.Parse;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

/**
 * Created by edgargomez on 4/15/16.
 */
@ParseClassName("Message")
public class Message extends ParseObject {

    public ParseFile getFile() {
        return getParseFile("file");
    }

    public void setFile(ParseFile value) {
        put("file", value);
    }

    public ParseFile getThumbnail() {
        return getParseFile("thumbnail");
    }

    public void setThumbnail(ParseFile value) {
        put("thumbnail", value);
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint("location");
    }

    public void setLocation(ParseGeoPoint value) {
        put("location", value);
    }

    public String getText() {
        return getString("text");
    }

    public void setText(String value) {
        put("text", value);
    }

    public int getWidth() {
        return getInt("width");
    }

    public void setWidth(int value) {
        put("width", value);
    }

    public int getHeight() {
        return getInt("height");
    }

    public void setHeight(int value) {
        put("height", value);
    }

    public int getDuration() {
        return getInt("duration");
    }

    public void setDuration(int value) {
        put("duration", value);
    }

}