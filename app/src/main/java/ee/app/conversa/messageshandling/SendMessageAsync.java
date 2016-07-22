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

import ee.app.conversa.model.Database.Message;
import ee.app.conversa.model.Parse.Account;
import ee.app.conversa.utils.Const;

/**
 * SendMessageAsync
 * 
 * AsyncTask for sending all sorts of messages
 */

public class SendMessageAsync {

	public static void sendTextMessage(String businessId, String text) {
		// 1. Create local message
		Message message = new Message();
		message.setFromUserId(Account.getCurrentUser().getObjectId());
		message.setToUserId(businessId);
		message.setBody(text);
		message.setMessageType(Const.kMessageTypeText);

		// 2. Save locally on background
		message.saveToLocalDatabase(Message.ACTION_MESSAGE_SAVE);
	}

	public static void sendLocationMessage(String businessId, float lat, float lon) {
		// 1. Create local message
		Message message = new Message();
		message.setFromUserId(Account.getCurrentUser().getObjectId());
		message.setToUserId(businessId);
		message.setLatitude(lat);
		message.setLongitude(lon);
		message.setMessageType(Const.kMessageTypeLocation);

		// 2. Save locally on background
		message.saveToLocalDatabase(Message.ACTION_MESSAGE_SAVE);
	}

}