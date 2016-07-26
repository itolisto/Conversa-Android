package ee.app.conversa.model.Parse;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import ee.app.conversa.utils.Const;

/**
 * Created by edgargomez on 4/15/16.
 */
@ParseClassName("Message")
public class pMessage extends ParseObject {

    public String getText() {
        return getString(Const.kMessageTextKey);
    }

    public int getSizeInBytes() {
        return getInt(Const.kMessageSizeInBytesKey);
    }

    public int getDuration() {
        return getInt(Const.kMessageDurationKey);
    }

    public int getWidth() {
        return getInt(Const.kMessageWidthKey);
    }

    public int getHeight() {
        return getInt(Const.kMessageHeightKey);
    }

    public ParseFile getThumbnail() {
        return getParseFile(Const.kMessageThumbKey);
    }

    public ParseFile getFile() {
        return getParseFile(Const.kMessageFileKey);
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint(Const.kMessageLocationKey);
    }
}