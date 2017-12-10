package ee.app.conversa.model;

import android.content.Context;
import android.text.TextUtils;

import ee.app.conversa.R;

/**
 * Created by root on 3/12/17.
 */

public class Favorite {
    private final String objectId;
    private final String name;
    private final String avatarUrl;

    public Favorite(String objectId, String name, String avatarUrl) {
        this.objectId = objectId;
        this.name = name;
        this.avatarUrl = avatarUrl;
    }

    public String getObjectId() {
        return objectId;
    }

    public String getBusinessName() { return name; }

    public String getAvatarUrl() {
        return avatarUrl;
    }

}