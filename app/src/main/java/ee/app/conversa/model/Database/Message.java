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

import java.io.Serializable;

/**
 * Message
 * 
 * Model class for messages.
 */

public class Message implements Comparable<Message>, Serializable {

	private static final long serialVersionUID = 1L;

	private String mRev;
	private String mType;
	private int mMessageType;
	private String mMessageTargetType;
	private String mBody;
	private String mFromUserId;
    private String mToUserId;
	private long mCreated;
	private long mModified;
	private boolean mValid;
	private String mLatitude;
	private String mLongitude;
    private String mImageFileId;
    private long mReadAt;
	
	public Message() {}
		
	public Message(String rev, String type, int messageType,
			String messageTargetType, String body, String fromUserId, String toUserId,
			long created, long modified, boolean valid,
			String latitude, String longitude, String imageFileId) {
		this.mRev 				= rev;
		this.mType 				= type;
		this.mMessageType 		= messageType;
		this.mMessageTargetType = messageTargetType;
		this.mBody 				= body;
		this.mFromUserId 		= fromUserId;
		this.mToUserId 			= toUserId;
		this.mCreated 			= created;
		this.mModified 			= modified;
		this.mValid 			= valid;
		this.mLatitude 			= latitude;
		this.mLongitude 		= longitude;
		this.mImageFileId 		= imageFileId;
		this.mReadAt 			= 0;
	}

    /* ******************************************************************************** */
	/* ************************************ GETTERS *********************************** */
	/* ******************************************************************************** */
    public String getRev() { return mRev; }
    public String getType() { return mType; }
    public int    getMessageType() { return mMessageType; }
    public String getMessageTargetType() { return mMessageTargetType; }
    public String getBody() { return mBody; }
    public String getFromUserId() { return mFromUserId; }
    public String getToUserId() { return mToUserId; }
    public long getCreated() { return mCreated; }
    public long getModified() { return mModified; }
    public boolean isValid() { return mValid; }
    public String getImageFileId() { return mImageFileId; }
    public long getReadAt() { return mReadAt; }
    public String getLatitude() { return mLatitude; }
    public String getLongitude() { return mLongitude; }

	/* ******************************************************************************** */
	/* ************************************ SETTERS *********************************** */
	/* ******************************************************************************** */
    public void setRev(String rev) { this.mRev = rev; }
    public void setType(String type) { this.mType = type; }
    public void setMessageType(String messageType) { try {this.mMessageType = Integer.valueOf(messageType);} catch(NumberFormatException e) {this.mMessageType = 0;} }
    public void setMessageTargetType(String messageTargetType) { this.mMessageTargetType = messageTargetType; }
    public void setBody(String body) { this.mBody = body; }
    public void setFromUserId(String fromUserId) { this.mFromUserId = fromUserId; }
    public void setToUserId(String toUserId) { this.mToUserId = toUserId; }
    public void setCreated(long created) { this.mCreated = created; }
    public void setModified(long modified) { this.mModified = modified; }
    public void setValid(boolean valid) { this.mValid = valid; }
    public void setImageFileId(String mImageFileId) { this.mImageFileId = mImageFileId; }
    public void setReadAt(long mReadAt) { this.mReadAt = mReadAt; }
    public void setLatitude(String latitude) { this.mLatitude = latitude; }
    public void setLongitude(String longitude) { this.mLongitude = longitude; }

	@Override
	public int compareTo(Message another) {
		if(this.getCreated() < another.getCreated()) {
			return -1;
		} else if(this.getCreated() > another.getCreated()) {
			return 1;
		} else {
			return 0;
		}
	}

	@Override
	public String toString() {
		return "Message ["
				+ "mFromUserType= " + mType
				+ ", mMessageType=" + mMessageType
				+ ", mMessageTargetType=" + mMessageTargetType
				+ ", mBody=" + mBody
				+ ", mFromUserId=" + mFromUserId
				+ ", mToUserId=" + mToUserId
				+ ", mCreated=" + mCreated
				+ ", mModified=" + mModified
				+ ", mValid=" + mValid
				+ ", mLatitude=" + mLatitude
				+ ", mLongitude=" + mLongitude
				+ ", mImageFileId=" + mImageFileId
				+ ", mReadAt=" + mReadAt + "]";
	}
}
