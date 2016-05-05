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

package ee.app.conversa.messageshandling;

import android.content.Context;

import ee.app.conversa.model.Database.Message;
import ee.app.conversa.utils.Logger;

/**
 * SendMessageAsync
 * 
 * AsyncTask for sending all sorts of messages
 */

public class SendMessageAsync { //extends ConversaAsync<Object, Void, MessageCode> {

	private int messageType;
	public static final int TYPE_PHOTO = 0;
	public static final int TYPE_TEXT = 1;
	public static final int TYPE_LOCATION = 2;

	public SendMessageAsync(Context context, int messageType) {
//		super(context);
		this.messageType = messageType;
	}

//	@Override
//	protected void onPreExecute() { super.onPreExecute(); }

//	@Override
	protected MessageCode backgroundWork(Object... params) {
		String TAG = "SendMessage";
		int isSuccess = -1;
		Object obj = params[0];

		Message message = null;

        switch (messageType) {
			case TYPE_PHOTO:
				try {
                    String fileId = (String) obj;

//					if (UsersManagement.getToUser() != null) {
//						message = MessageManagement.createMessage(2,
//                                "1", "", "", "", fileId);
//						isSuccess = CouchDB.sendMessageToUser(message);
//					}
				} catch (Exception e) {
					Logger.error(TAG, "params not bitmap!");
				}
				break;
			case TYPE_TEXT:
				try {
					String body = (String) obj;

//					if (UsersManagement.getToUser() != null) {
//						message = MessageManagement.createMessage(1,
//                                "1", body, "", "", "");
//						isSuccess = CouchDB.sendMessageToUser(message);
//					}
				} catch (Exception e) {
                    Logger.error(TAG, "params not string!");
				}
				break;
			case TYPE_LOCATION:
				try {
					String body = (String) obj;
					String latitude = (String) params[1];
					String longitude = (String) params[2];

//					if (UsersManagement.getToUser() != null) {
//						message = MessageManagement.createMessage(3,
//                                "1", body, latitude, longitude, "");
//						isSuccess = CouchDB.sendMessageToUser(message);
//					}
				} catch (Exception e) {
                    Logger.error(TAG, "params not string!");
				}
				break;
			default:
				break;
        }

        return (isSuccess == 0) ? new MessageCode(message, isSuccess) : new MessageCode(null, isSuccess);
	}

//	@Override
//	protected void onPostExecute(MessageCode result) {
//		super.onPostExecute(result);
//		if (result.getCode() == 0) {
//			UpdateMessages.reload(result.getMessage());
//			try {
//				FragmentUsersChat.updateRecentById = Integer.valueOf(result.getMessage().getToUserId());
//			} catch (NumberFormatException e) {
//				FragmentUsersChat.updateRecentById = 0;
//			}
//        } else {
//			if (result.getCode() == 1) {
//				Toast.makeText(mContext, mContext.getString(R.string.cant_send), Toast.LENGTH_SHORT).show();
//				//Snackbar.make(((AppCompatActivity) mContext).getCurrentFocus(), mContext.getString(R.string.cant_send), Snackbar.LENGTH_SHORT).show();
//			} else {
//				if (result.getCode() == 2) {
//					Toast.makeText(mContext, mContext.getString(R.string.an_server_error_has_occurred), Toast.LENGTH_SHORT).show();
//					//Snackbar.make(((AppCompatActivity) mContext).getCurrentFocus(), mContext.getString(R.string.an_server_error_has_occurred), Snackbar.LENGTH_SHORT).show();
//				} else {
//					if(result.getCode() == 3) {
//						Toast.makeText(mContext, mContext.getString(R.string.an_internal_error_has_occurred), Toast.LENGTH_SHORT).show();
//						//Snackbar.make(((AppCompatActivity) mContext).getCurrentFocus(), mContext.getString(R.string.an_internal_error_has_occurred), Snackbar.LENGTH_SHORT).show();
//					} else {
//						Toast.makeText(mContext, mContext.getString(R.string.can_not_connect_to_server), Toast.LENGTH_SHORT).show();
//						//Snackbar.make(((AppCompatActivity) mContext).getCurrentFocus(), mContext.getString(R.string.can_not_connect_to_server), Snackbar.LENGTH_SHORT).show();
//					}
//				}
//			}
//        }
//	}
}