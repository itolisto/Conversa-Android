package ee.app.conversa.model;

import android.content.Context;
import android.text.TextUtils;

import ee.app.conversa.R;

/**
 * Created by edgargomez on 9/15/16.
 */
public class nCategory {

    private final String objectId;
    private final String name;
    private final String avatarUrl;
    private final boolean isCustom;
    private boolean removeDividerMargin;

    public nCategory(String objectId, String name, String avatarUrl, boolean isCustom) {
        this.objectId = objectId;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.removeDividerMargin = false;
        this.isCustom = isCustom;
    }

    public boolean getRemoveDividerMargin() {
        return removeDividerMargin;
    }

    public String getObjectId() {
        return objectId;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getCategoryName(Context activity) {
        if (activity == null) {
            return "";
        } else {
            return (TextUtils.isEmpty(name)) ? activity.getString(R.string.category) : name;
        }
    }

    public boolean isCustom() {
        return isCustom;
    }

}
