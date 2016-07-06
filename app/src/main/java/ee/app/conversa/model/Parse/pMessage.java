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

    public void setCustomer(Customer fromUser) {
        put(Const.kMessageUserKey, fromUser);
    }

    public void setBusiness(Business toUser) {
        put(Const.kMessageBusinessKey, toUser);
    }

    public void setSizeInBytes(int sizeInBytes) {
        put(Const.kMessageSizeInBytesKey, sizeInBytes);
    }

    public void setWidth(int value) {
        put(Const.kMessageWidthKey, value);
    }

    public void setHeight(int value) {
        put(Const.kMessageHeightKey, value);
    }

    public void setThumbnail(ParseFile value) {
        put(Const.kMessageThumbKey, value);
    }

    public void setFile(ParseFile value) {
        put(Const.kMessageFileKey, value);
    }

    public void setText(String value) {
        put(Const.kMessageTextKey, value);
    }

    public void setDuration(int value) {
        put(Const.kMessageDurationKey, value);
    }

    public void setLocation(ParseGeoPoint value) {
        put(Const.kMessageLocationKey, value);
    }

    public int getSizeInBytes(int sizeInBytes) {
        return getInt(Const.kMessageSizeInBytesKey);
    }

    public int getWidth(int value) {
        return getInt(Const.kMessageWidthKey);
    }

    public int getHeight(int value) {
        return getInt(Const.kMessageHeightKey);
    }

    public ParseFile getThumbnail(ParseFile value) {
        return getParseFile(Const.kMessageThumbKey);
    }

    public ParseFile getFile(ParseFile value) {
        return getParseFile(Const.kMessageFileKey);
    }

    public String getText(String value) {
        return getString(Const.kMessageTextKey);
    }

    public int getDuration(int value) {
        return getInt(Const.kMessageDurationKey);
    }

    public ParseGeoPoint getLocation(ParseGeoPoint value) {
        return getParseGeoPoint(Const.kMessageLocationKey);
    }
}