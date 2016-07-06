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

import android.database.SQLException;
import android.os.AsyncTask;
import android.util.Log;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.adapters.MessagesAdapter;
import ee.app.conversa.interfaces.OnMessageTaskCompleted;
import ee.app.conversa.responses.MessageResponse;

/**
 * pMessage
 * 
 * Model class for messages.
 */

public class Message implements Comparable<Message>, Serializable {

	private long mId;
	private String mMessageType;
	private String mBody;
	private String mDeliveryStatus;
	private String mFromUserId;
    private String mToUserId;
	private float mLatitude;
	private float mLongitude;
    private String mImageFileId;
    private long mReadAt;
	private long mCreated;
	private long mModified;

	private final WeakReference<MessagesAdapter> adapter;

	// MESSAGE STATUS
	// Error
	public static final String statusParseError = "1";
	public static final String statusPubNubError = "2";
	// No error
	public static final String statusAllDelivered = "3";
	public static final String statusReceived = "4";
	public static final String statusReceivedError = "5";
	public static final String statusDownloading = "6";
	public static final String statusUploading = "7";
	// MESSAGE ACTIONS
	public static final int ACTION_MESSAGE_SAVE = 1;
	public static final int ACTION_MESSAGE_UPDATE = 2;
	public static final int ACTION_MESSAGE_DELETE = 3;
	public static final int ACTION_MESSAGE_NONE = 4;
	public static final int ACTION_MESSAGE_RETRIEVE_ALL = 5;
	
	public Message(MessagesAdapter adapter) {
		this.adapter = new WeakReference<>(adapter);
		this.mId = -1;
		this.mReadAt = 0;
		this.mCreated = System.currentTimeMillis();
		this.mModified = 0;
		this.mLatitude = 0;
		this.mLongitude = 0;
		this.mImageFileId = "";
		this.mDeliveryStatus = statusUploading;
	}

    /* ******************************************************************************** */
	/* ************************************ GETTERS *********************************** */
	/* ******************************************************************************** */
	public long getId() { return mId; }
	public String getMessageType() { return mMessageType; }
    public String getBody() { return mBody; }
	public String getDeliveryStatus() { return mDeliveryStatus; }
    public String getFromUserId() { return mFromUserId; }
    public String getToUserId() { return mToUserId; }
    public long getCreated() { return mCreated; }
    public long getModified() { return mModified; }
    public String getImageFileId() { return mImageFileId; }
    public long getReadAt() { return mReadAt; }
    public float getLatitude() { return mLatitude; }
    public float getLongitude() { return mLongitude; }

	/* ******************************************************************************** */
	/* ************************************ SETTERS *********************************** */
	/* ******************************************************************************** */
	public void setId(long id) { this.mId = id; }
	public void setMessageType(String type) { this.mMessageType = type; }
    public void setBody(String body) { this.mBody = body; }
    public void setFromUserId(String fromUserId) { this.mFromUserId = fromUserId; }
    public void setToUserId(String toUserId) { this.mToUserId = toUserId; }
    public void setCreated(long created) { this.mCreated = created; }
    public void setModified(long modified) { this.mModified = modified; }
    public void setImageFileId(String mImageFileId) { this.mImageFileId = mImageFileId; }
    public void setReadAt(long mReadAt) { this.mReadAt = mReadAt; }
    public void setLatitude(float latitude) { this.mLatitude = latitude; }
    public void setLongitude(float longitude) { this.mLongitude = longitude; }
	public void setDeliveryStatus(String status) { this.mDeliveryStatus = status; }

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
				+ "mId= " + mId
				+ ", mMessageType= " + mMessageType
				+ ", mBody=" + mBody
				+ ", mDeliveryStatus= " + mDeliveryStatus
				+ ", mFromUserId=" + mFromUserId
				+ ", mToUserId=" + mToUserId
				+ ", mLatitude=" + mLatitude
				+ ", mLongitude=" + mLongitude
				+ ", mImageFileId=" + mImageFileId
				+ ", mReadAt=" + mReadAt
				+ ", mCreated=" + mCreated
				+ ", mModified=" + mModified + "]";
	}

	/* ******************************************************************************** */
	/* ************************************ SETTERS *********************************** */
	/* ******************************************************************************** */
	public void saveToLocalDatabase(OnMessageTaskCompleted e) {
		MessageAsyncTaskRunner runner = new MessageAsyncTaskRunner(e);
		runner.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, ACTION_MESSAGE_SAVE, this);
	}

	public static void getAllMessageForChat(OnMessageTaskCompleted e, String businessId, int skip) {
		MessageAsyncTaskRunner runner = new MessageAsyncTaskRunner(e);
		runner.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, ACTION_MESSAGE_RETRIEVE_ALL, businessId, skip);
	}

	public void updateDelivery(String status) {
		// 1. Update status on db on a background process
		MessageAsyncTaskRunner runner = new MessageAsyncTaskRunner(null);
		runner.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, ACTION_MESSAGE_UPDATE, this, status);
		// 2. Update this message and try to update on MessagesAdapter
		if(adapter != null) {
			final MessagesAdapter localAdapter = adapter.get();
			if (localAdapter != null) {
				localAdapter.updateMessage(this, status);
			}
		}
	}

	private static class MessageAsyncTaskRunner extends AsyncTask<Object, String, MessageResponse> {

		private OnMessageTaskCompleted taskCompleted;

		public MessageAsyncTaskRunner(OnMessageTaskCompleted activityContext) {
			this.taskCompleted = activityContext;
		}

		@Override
		protected MessageResponse doInBackground(Object... params) {
			try {
				Log.e("MessageAsyncTaskRunner", "INTENTANDO GUARDAR/ACTUALIZAR/ELIMINAR MENSAJE...");
				int val = (int)params[0];

				List<Message> messages = new ArrayList<>();

				switch (val) {
					case ACTION_MESSAGE_SAVE:
						Message message1 = (Message) params[1];
						message1 = ConversaApp.getDB().saveMessage(message1);
						messages.add(message1);
						break;
					case ACTION_MESSAGE_UPDATE:
						Message message2 = (Message) params[1];
						String status = (String)params[2];
						ConversaApp.getDB().updateDeliveryStatus(message2.getId(), status);
						break;
					case ACTION_MESSAGE_RETRIEVE_ALL:
						String businessId = (String) params[1];
						int skip = (int) params[2];
						messages = ConversaApp.getDB().getMessagesByContact(businessId, 20, skip);
						break;
				}

				return new MessageResponse(val, messages);
			} catch (SQLException e) {
				Log.e("MessageAsyncTaskRunner", "No se pudo guardar mensaje porque ocurrio el siguiente error: " + e.getMessage());
				return null;
			}
		}

		@Override
		protected void onPostExecute(MessageResponse message) {
			Log.e("MessageAsyncTaskRunner", "onPostExecute HA FINALIZADO, EL RESULTADO: " + (message != null));
			if (taskCompleted != null) {
				taskCompleted.OnMessageTaskCompleted(message);
			}
		}
	}

	public void addMessageToAdapter() {
		if(adapter != null) {
			final MessagesAdapter localAdapter = adapter.get();
			if (localAdapter != null) {
				localAdapter.addMessage(this);
			}
		}
	}

}
