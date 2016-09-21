package ee.app.conversa.messageshandling;

import android.content.Context;
import android.content.Intent;

import ee.app.conversa.management.message.MessageIntentService;
import ee.app.conversa.model.database.dbBusiness;
import ee.app.conversa.model.database.dbMessage;
import ee.app.conversa.model.parse.Account;
import ee.app.conversa.utils.Const;

/**
 * SendMessageAsync
 * 
 * AsyncTask for sending all sorts of messages
 */

public class SendMessageAsync {

	public static void sendTextMessage(Context context, String text, boolean addContact, dbBusiness business) {
		// 1. Create local message
		dbMessage message = new dbMessage();
		message.setFromUserId(Account.getCurrentUser().getObjectId());
		message.setToUserId(business.getBusinessId());
		message.setMessageType(Const.kMessageTypeText);
		message.setDeliveryStatus(dbMessage.statusUploading);
		message.setBody(text);

		// 2. Save locally on background
		Intent intent = new Intent(context, MessageIntentService.class);
		intent.putExtra(MessageIntentService.INTENT_EXTRA_ACTION_CODE, MessageIntentService.ACTION_MESSAGE_SAVE);
		intent.putExtra(MessageIntentService.INTENT_EXTRA_MESSAGE, message);
		context.startService(intent);

		if (addContact) {
			SaveUserAsync.saveBusinessAsContact(context, business);
		}
	}

	public static void sendLocationMessage(Context context, double lat, double lon, boolean addContact, dbBusiness business) {
		// 1. Create local message
		dbMessage message = new dbMessage();
		message.setFromUserId(Account.getCurrentUser().getObjectId());
		message.setToUserId(business.getBusinessId());
		message.setMessageType(Const.kMessageTypeLocation);
		message.setDeliveryStatus(dbMessage.statusUploading);
		message.setLatitude((float)lat);
		message.setLongitude((float)lon);

		// 2. Save locally on background
		Intent intent = new Intent(context, MessageIntentService.class);
		intent.putExtra(MessageIntentService.INTENT_EXTRA_ACTION_CODE, MessageIntentService.ACTION_MESSAGE_SAVE);
		intent.putExtra(MessageIntentService.INTENT_EXTRA_MESSAGE, message);
		context.startService(intent);

		if (addContact) {
			SaveUserAsync.saveBusinessAsContact(context, business);
		}
	}

	public static void sendImageMessage(Context context, String path, int width, int height, int size, boolean addContact, dbBusiness business) {
		// 1. Create local message
		dbMessage message = new dbMessage();
		message.setFromUserId(Account.getCurrentUser().getObjectId());
		message.setToUserId(business.getBusinessId());
		message.setMessageType(Const.kMessageTypeImage);
		message.setDeliveryStatus(dbMessage.statusUploading);
		message.setFileId(path);
		message.setWidth(width);
		message.setHeight(height);
		message.setBytes(size);

		// 2. Save locally on background
		Intent intent = new Intent(context, MessageIntentService.class);
		intent.putExtra(MessageIntentService.INTENT_EXTRA_ACTION_CODE, MessageIntentService.ACTION_MESSAGE_SAVE);
		intent.putExtra(MessageIntentService.INTENT_EXTRA_MESSAGE, message);
		context.startService(intent);

		if (addContact) {
			SaveUserAsync.saveBusinessAsContact(context, business);
		}
	}

}