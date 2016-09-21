package ee.app.conversa.model;

import android.content.Context;

import ee.app.conversa.R;

/**
 * Created by edgargomez on 9/15/16.
 */
public class nCategory {

    private final String objectId;
    private final int relevance;
    private final int position;
    private final String avatarUrl;
    private boolean removeDividerMargin;

    public nCategory(String objectId, int relevance, int position, String avatarUrl) {
        this.objectId = objectId;
        this.relevance = relevance;
        this.position = position;
        this.avatarUrl = avatarUrl;
        this.removeDividerMargin = false;
    }

    public void setRemoveDividerMargin(boolean removeDividerMargin) {
        this.removeDividerMargin = removeDividerMargin;
    }

    public boolean getRemoveDividerMargin() {
        return removeDividerMargin;
    }

    public String getObjectId() {
        return objectId;
    }

    public int getRelevance() {
        return relevance;
    }

    public int getPosition() {
        return position;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getCategoryName(Context activity) {
        switch (getObjectId()) {
            case "0i96zRLFyw":
                return activity.getString(R.string.c0i96zRLFyw);
            case "1zZcJ8an9Z":
                return activity.getString(R.string.c1zZcJ8an9Z);
            case "3b8NykTdo3":
                return activity.getString(R.string.c3b8NykTdo3);
            case "462mKRISE7":
                return activity.getString(R.string.c462mKRISE7);
            case "4EsFpxgOu6":
                return activity.getString(R.string.c4EsFpxgOu6);
            case "90Y0ajWUAG":
                return activity.getString(R.string.c90Y0ajWUAG);
            case "DqCLGage9M":
                return activity.getString(R.string.cDqCLGage9M);
            case "Dvl63HGh9j":
                return activity.getString(R.string.cDvl63HGh9j);
            case "FBv7AV5OGU":
                return activity.getString(R.string.cFBv7AV5OGU);
            case "Ji1FwE21F8":
                return activity.getString(R.string.cJi1FwE21F8);
            case "MHbjypaY4R":
                return activity.getString(R.string.cMHbjypaY4R);
            case "QNFkAelMAd":
                return activity.getString(R.string.cQNFkAelMAd);
            case "R7LZJ3Lbj8":
                return activity.getString(R.string.cR7LZJ3Lbj8);
            case "RWcS1d4bqt":
                return activity.getString(R.string.cRWcS1d4bqt);
            case "SYHk42aEOz":
                return activity.getString(R.string.cSYHk42aEOz);
            case "TFau41aZ8E":
                return activity.getString(R.string.cTFau41aZ8E);
            case "VZg31Kxzew":
                return activity.getString(R.string.cVZg31Kxzew);
            case "W0lNHTPV3v":
                return activity.getString(R.string.cW0lNHTPV3v);
            case "aDuBd0GuuH":
                return activity.getString(R.string.caDuBd0GuuH);
            case "aWthcdxIAJ":
                return activity.getString(R.string.caWthcdxIAJ);
            case "ay9ADQjpRT":
                return activity.getString(R.string.cay9ADQjpRT);
            case "cCZl1h0sXf":
                return activity.getString(R.string.ccCZl1h0sXf);
            case "cGWcPxDcB7":
                return activity.getString(R.string.ccGWcPxDcB7);
            case "cQy8YtN7j4":
                return activity.getString(R.string.ccQy8YtN7j4);
            case "gBcgtlLRVw":
                return activity.getString(R.string.cgBcgtlLRVw);
            case "prn5lWuEDc":
                return activity.getString(R.string.cprn5lWuEDc);
            case "r4ZYMKINHp":
                return activity.getString(R.string.cr4ZYMKINHp);
            case "uRzYgo8krw":
                return activity.getString(R.string.cuRzYgo8krw);
            case "ydPTRrUJye":
                return activity.getString(R.string.cydPTRrUJye);
            default:
                return activity.getString(R.string.category);
        }
    }

}
