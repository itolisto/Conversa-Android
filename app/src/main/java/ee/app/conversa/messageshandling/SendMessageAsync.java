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
import android.content.Intent;

import ee.app.conversa.management.message.MessageIntentService;
import ee.app.conversa.model.Database.dbMessage;
import ee.app.conversa.model.Parse.Account;
import ee.app.conversa.receivers.FileUploadingReceiver;
import ee.app.conversa.utils.Const;

/**
 * SendMessageAsync
 * 
 * AsyncTask for sending all sorts of messages
 */

public class SendMessageAsync {

	public static void sendTextMessage(Context context, String businessId, String text) {
		// 1. Create local message
		dbMessage message = new dbMessage();
		message.setFromUserId(Account.getCurrentUser().getObjectId());
		message.setToUserId(businessId);
		message.setMessageType(Const.kMessageTypeText);
		message.setDeliveryStatus(dbMessage.statusUploading);
		message.setBody(text);

		// 2. Save locally on background
		Intent intent = new Intent(context, MessageIntentService.class);
		intent.putExtra(MessageIntentService.INTENT_EXTRA_ACTION_CODE, MessageIntentService.ACTION_MESSAGE_SAVE);
		intent.putExtra(MessageIntentService.INTENT_EXTRA_MESSAGE, message);
		context.startService(intent);
	}

	public static void sendLocationMessage(Context context, String businessId, double lat, double lon) {
		// 1. Create local message
		dbMessage message = new dbMessage();
		message.setFromUserId(Account.getCurrentUser().getObjectId());
		message.setToUserId(businessId);
		message.setMessageType(Const.kMessageTypeLocation);
		message.setDeliveryStatus(dbMessage.statusUploading);
		message.setLatitude((float)lat);
		message.setLongitude((float)lon);

		// 2. Save locally on background
		Intent intent = new Intent(context, MessageIntentService.class);
		intent.putExtra(MessageIntentService.INTENT_EXTRA_ACTION_CODE, MessageIntentService.ACTION_MESSAGE_SAVE);
		intent.putExtra(MessageIntentService.INTENT_EXTRA_MESSAGE, message);
		context.startService(intent);
	}

	public static void sendImageMessage(Context context, String businessId, int width, int height, int size, FileUploadingReceiver receiver) {
		// 1. Create local message
		dbMessage message = new dbMessage();
		message.setFromUserId(Account.getCurrentUser().getObjectId());
		message.setToUserId(businessId);
		message.setMessageType(Const.kMessageTypeLocation);
		message.setDeliveryStatus(dbMessage.statusUploading);
		message.setWidth(width);
		message.setHeight(height);
		message.setBytes(size);

		// 2. Save locally on background
		Intent intent = new Intent(context, MessageIntentService.class);
		intent.putExtra(MessageIntentService.INTENT_EXTRA_ACTION_CODE, MessageIntentService.ACTION_MESSAGE_SAVE);
		intent.putExtra(MessageIntentService.INTENT_EXTRA_MESSAGE, message);
		intent.putExtra(MessageIntentService.INTENT_EXTRA_RECEIVER, receiver);
		context.startService(intent);
	}

}