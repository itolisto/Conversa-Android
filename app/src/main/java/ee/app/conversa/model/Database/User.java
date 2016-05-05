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

/**
 * User
 * 
 * Model class for User.
 */

public class User extends Object{

    public long mId;
    public String mObjectId;
    public String mUsername;
    public String mDisplayName;
    public String mEmail;
    public String mAvatarThumbFileId;

	public User(long mId, String mUsername, String mEmail, String mAvatarThumbFileId) {
		super();
        this.mId                = mId;
        this.mUsername          = mUsername;
		this.mEmail             = mEmail;
        this.mAvatarThumbFileId = mAvatarThumbFileId;
	}

    public User(){}
	/* ******************************************************************************** */
	/* ************************************ GETTERS *********************************** */
	/* ******************************************************************************** */
    public String getId() {
		return Long.toString(mId);
	}

    public String getObjectId() {
        return mObjectId;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getAvatarThumbFileId() {
        return mAvatarThumbFileId;
    }
	/* ******************************************************************************** */
	/* ************************************ SETTERS *********************************** */
	/* ******************************************************************************** */

    public void setId(long id) {
        this.mId = id;
    }

    public void setObjectId(String mObjectId) {
        this.mObjectId = mObjectId;
    }

	public void setUsername(String name) {
        this.mUsername = name;
    }

    public void setDisplayName(String mDisplayName) {
        this.mDisplayName = mDisplayName;
    }

	public void setEmail(String email) {
        this.mEmail = email;
    }

    public void setAvatarThumbFileId(String mAvatarThumbFileId) {
        this.mAvatarThumbFileId = mAvatarThumbFileId;
    }
	/* ******************************************************************************** */
	/* *********************************** CONTACTS *********************************** */
	/* ******************************************************************************** */

	@Override
	public String toString() {
		return "User [mId=" + mId
                + ", mUsername=" + mUsername
                + ", mDisplayName=" + mDisplayName
                + ", mEmail=" + mEmail
                + ", mAvatarThumbFileId=" + mAvatarThumbFileId + "]";
	}
}
