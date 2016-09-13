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

package ee.app.conversa.model.database;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import ee.app.conversa.management.contact.ContactIntentService;

/**
 * Emoticon
 * 
 * Model class for business.
 */

public class dBusiness implements Parcelable {

    private long mId;
    private String mBusinessId;
    private String mDisplayName;
    private String mConversaId;
    private String mAbout;
    private String mComposingMessageString;
    private String mAvatarThumbFileId;
    private boolean mBlocked;
    private boolean mMuted;
    private long mRecent;
    private long mCreated;

    public dBusiness() {
        this.mId = -1;
        this.mComposingMessageString = "";
        this.mBlocked = false;
        this.mMuted = false;
        this.mCreated = System.currentTimeMillis();
        this.mRecent = this.mCreated;
    }

    public long getId() { return mId; }
    public String getBusinessId() { return mBusinessId; }
    public String getDisplayName() { return mDisplayName; }
    public String getConversaId() { return mConversaId; }
    public String getAbout() { return mAbout; }
    public String getComposingMessage() { return mComposingMessageString; }
    public String getAvatarThumbFileId() { return mAvatarThumbFileId; }
    public boolean isBlocked() { return mBlocked; }
    public boolean isMuted() { return mMuted; }
    public long getRecent() { return mRecent; }
    public long getCreated() { return mCreated; }

    public void setId(long mId) { this.mId = mId; }
    public void setBusinessId(String mBusinessId) { this.mBusinessId = mBusinessId; }
    public void setDisplayName(String mDisplayName) { this.mDisplayName = mDisplayName; }
    public void setConversaId(String mConversaId) { this.mConversaId = mConversaId; }
    public void setAbout(String about) { this.mAbout = about; }
    public void setComposingMessage(String mComposingMessageString) { this.mComposingMessageString = mComposingMessageString; }
    public void setAvatarThumbFileId(String mAvatarThumbFileId) { this.mAvatarThumbFileId = mAvatarThumbFileId; }
    public void setBlocked(boolean mBlocked) { this.mBlocked = mBlocked; }
    public void setMuted(boolean mMuted) { this.mMuted = mMuted; }
    public void setRecent(long mRecent) { this.mRecent = mRecent; }
    public void setCreated(long mCreated) { this.mCreated = mCreated; }

    /* ******************************************************************************************* */
    /* ******************************************************************************************* */
    public static void getAllContacts(Context context) {
        Intent broadcastIntent = new Intent(context, ContactIntentService.class);
        broadcastIntent.putExtra(ContactIntentService.INTENT_EXTRA_ACTION_CODE, ContactIntentService.ACTION_MESSAGE_RETRIEVE_ALL);
        context.startService(broadcastIntent);
    }

    public void removeContact(Context context) {
        Intent broadcastIntent = new Intent(context, ContactIntentService.class);
        broadcastIntent.putExtra(ContactIntentService.INTENT_EXTRA_ACTION_CODE, ContactIntentService.ACTION_MESSAGE_DELETE);
        broadcastIntent.putExtra(ContactIntentService.INTENT_EXTRA_CUSTOMER, this);
        context.startService(broadcastIntent);
    }

    /* ******************************************************************************************* */
    /* ******************************************************************************************* */
    // In the vast majority of cases you can simply return 0 for this.
    // There are cases where you need to use the constant `CONTENTS_FILE_DESCRIPTOR`
    // But this is out of scope of this tutorial
    @Override
    public int describeContents() {
        return 0;
    }

    // This is where you write the values you want to save to the `Parcel`.
    // The `Parcel` class has methods defined to help you save all of your values.
    // Note that there are only methods defined for simple values, lists, and other Parcelable objects.
    // You may need to make several classes Parcelable to send the data you want.
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mId);
        dest.writeString(this.mBusinessId);
        dest.writeString(this.mDisplayName);
        dest.writeString(this.mConversaId);
        dest.writeString(this.mAbout);
        dest.writeString(this.mComposingMessageString);
        dest.writeString(this.mAvatarThumbFileId);
        dest.writeByte(this.mBlocked ? (byte) 1 : (byte) 0);
        dest.writeByte(this.mMuted ? (byte) 1 : (byte) 0);
        dest.writeLong(this.mRecent);
        dest.writeLong(this.mCreated);
    }

    // Using the `in` variable, we can retrieve the values that
    // we originally wrote into the `Parcel`.  This constructor is usually
    // private so that only the `CREATOR` field can access.
    protected dBusiness(Parcel in) {
        this.mId = in.readLong();
        this.mBusinessId = in.readString();
        this.mDisplayName = in.readString();
        this.mConversaId = in.readString();
        this.mAbout = in.readString();
        this.mComposingMessageString = in.readString();
        this.mAvatarThumbFileId = in.readString();
        this.mBlocked = in.readByte() != 0;
        this.mMuted = in.readByte() != 0;
        this.mRecent = in.readLong();
        this.mCreated = in.readLong();
    }

    // After implementing the `Parcelable` interface, we need to create the
    // `Parcelable.Creator<MyParcelable> CREATOR` constant for our class;
    // Notice how it has our class specified as its type.
    public static final Parcelable.Creator<dBusiness> CREATOR = new Parcelable.Creator<dBusiness>() {
        // This simply calls our new constructor (typically private) and
        // passes along the unmarshalled `Parcel`, and then returns the new object!
        @Override
        public dBusiness createFromParcel(Parcel source) {
            return new dBusiness(source);
        }
        // We just need to copy this and change the type to match our class.
        @Override
        public dBusiness[] newArray(int size) {
            return new dBusiness[size];
        }
    };
}
