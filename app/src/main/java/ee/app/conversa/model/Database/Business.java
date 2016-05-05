/*
 * The MIT License (MIT)
 * 
 * Copyright � 2013 Clover Studio Ltd. All rights reserved.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ee.app.conversa.model.Database;

import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;

import ee.app.conversa.R;

/**
 * Emoticon
 * 
 * Model class for business.
 */

public class Business extends User {

    public String mBusinessId;
    public String mConversaId;
    public String mAbout;
    public String mStatusMessage;
    public String mComposingMessageString;
    public boolean mBlocked;
    public boolean mMuted;
    public boolean mFavorite;
    public long mRecent; // Used for order chat view

    public Business() {}

    public Business(String mObjectId, String mBusinessId, String mDisplayName, String mConversaId, String mAbout, String mStatusMessage, String mComposingMessageString, boolean mBlocked, boolean mMuted, boolean mFavorite) {
        this.mObjectId = mObjectId;
        this.mBusinessId = mBusinessId;
        this.mDisplayName = mDisplayName;
        this.mConversaId = mConversaId;
        this.mAbout             = mAbout;
        this.mStatusMessage = mStatusMessage;
        this.mComposingMessageString = mComposingMessageString;
        this.mBlocked = mBlocked;
        this.mMuted = mMuted;
        this.mFavorite = mFavorite;
        this.mRecent = SystemClock.currentThreadTimeMillis() / 1000;
    }

    public String getBusinessId() {
        return mBusinessId;
    }

    public String getConversaId() {
        return mConversaId;
    }

    public String getAbout() {
        return mAbout;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public String getComposingMessage() {
        return mComposingMessageString;
    }

    public String getStatusMessage() {
        return mStatusMessage;
    }

    public boolean isFavorite() {
        return mFavorite;
    }

    public boolean isBlocked() {
        return mBlocked;
    }

    public boolean isMuted() {
        return mMuted;
    }

    public long getRecent() {
        return mRecent;
    }

    public String getmTitle(AppCompatActivity activity) {
        int id;
        try {
            id = Integer.valueOf("1");
        } catch(NumberFormatException e) {
            return "";
        }

        String category = "";
        switch(id) {
            case 1:
                category =  activity.getString(R.string.category_1);
                break;
            case 2:
                category =  activity.getString(R.string.category_2);
                break;
            case 3:
                category =  activity.getString(R.string.category_3);
                break;
            case 4:
                category =  activity.getString(R.string.category_4);
                break;
            case 5:
                category =  activity.getString(R.string.category_5);
                break;
            case 6:
                category =  activity.getString(R.string.category_6);
                break;
            case 7:
                category =  activity.getString(R.string.category_7);
                break;
            case 8:
                category =  activity.getString(R.string.category_8);
                break;
            case 9:
                category =  activity.getString(R.string.category_9);
                break;
            case 10:
                category =  activity.getString(R.string.category_10);
                break;
            case 11:
                category =  activity.getString(R.string.category_11);
                break;
            case 12:
                category =  activity.getString(R.string.category_12);
                break;
            case 13:
                category =  activity.getString(R.string.category_13);
                break;
            case 14:
                category =  activity.getString(R.string.category_14);
                break;
            case 15:
                category =  activity.getString(R.string.category_15);
                break;
            case 16:
                category =  activity.getString(R.string.category_16);
                break;
            case 17:
                category =  activity.getString(R.string.category_17);
                break;
            case 18:
                category =  activity.getString(R.string.category_18);
                break;
            case 19:
                category =  activity.getString(R.string.category_19);
                break;
            case 20:
                category =  activity.getString(R.string.category_20);
                break;
            case 21:
                category =  activity.getString(R.string.category_21);
                break;
            case 22:
                category =  activity.getString(R.string.category_22);
                break;
            case 23:
                category =  activity.getString(R.string.category_23);
                break;
            case 24:
                category =  activity.getString(R.string.category_24);
                break;
        }

        return category;
    }


    public void setBusinessId(String mBusinessId) {
        this.mBusinessId = mBusinessId;
    }

    public void setConversaId(String mConversaId) {
        this.mConversaId = mConversaId;
    }

    public void setComposingMessage(String mComposingMessageString) {
        this.mComposingMessageString = mComposingMessageString;
    }

    public void setRecent(long mRecent) {
        this.mRecent = mRecent;
    }

    public void setStatusMessage(String mStatusMessage) {
        this.mStatusMessage = mStatusMessage;
    }

    public void setAbout(String about) { this.mAbout = about; }

    public void setBlocked(boolean mBlocked) {
        this.mBlocked = mBlocked;
    }

    public void setMuted(boolean mMuted) {
        this.mMuted = mMuted;
    }
}
