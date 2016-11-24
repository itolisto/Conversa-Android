package ee.app.conversa.messaging;

import android.content.Context;
import android.content.Intent;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.actions.MessageAction;
import ee.app.conversa.contact.SaveContactAsync;
import ee.app.conversa.delivery.DeliveryStatus;
import ee.app.conversa.model.database.dbBusiness;
import ee.app.conversa.model.database.dbMessage;
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
		message.setFromUserId(ConversaApp.getInstance(context).getPreferences().getCustomerId());
		message.setToUserId(business.getBusinessId());
		message.setMessageType(Const.kMessageTypeText);
		message.setDeliveryStatus(DeliveryStatus.statusUploading);
		message.setBody(text);

		// 2. Save locally on background
		Intent intent = new Intent(context, MessageIntentService.class);
		intent.putExtra(MessageIntentService.INTENT_EXTRA_ACTION_CODE, MessageAction.ACTION_MESSAGE_OUTGOING);
		intent.putExtra(MessageIntentService.INTENT_EXTRA_MESSAGE, message);
		context.startService(intent);

		if (addContact) {
			SaveContactAsync.saveBusinessAsContact(context, business);
		}
	}

	public static void sendLocationMessage(Context context, double lat, double lon, boolean addContact, dbBusiness business) {
		// 1. Create local message
		dbMessage message = new dbMessage();
		message.setFromUserId(ConversaApp.getInstance(context).getPreferences().getCustomerId());
		message.setToUserId(business.getBusinessId());
		message.setMessageType(Const.kMessageTypeLocation);
		message.setDeliveryStatus(DeliveryStatus.statusUploading);
		message.setLatitude((float)lat);
		message.setLongitude((float)lon);

		// 2. Save locally on background
		Intent intent = new Intent(context, MessageIntentService.class);
		intent.putExtra(MessageIntentService.INTENT_EXTRA_ACTION_CODE, MessageAction.ACTION_MESSAGE_OUTGOING);
		intent.putExtra(MessageIntentService.INTENT_EXTRA_MESSAGE, message);
		context.startService(intent);

		if (addContact) {
			SaveContactAsync.saveBusinessAsContact(context, business);
		}
	}

	public static void sendImageMessage(Context context, String path, int width, int height,
										long size, boolean addContact, dbBusiness business) {
		if (width <= 0 || height <= 0) {
			return;
		}

		// 1. Create local message
		dbMessage message = new dbMessage();
		message.setFromUserId(ConversaApp.getInstance(context).getPreferences().getCustomerId());
		message.setToUserId(business.getBusinessId());
		message.setMessageType(Const.kMessageTypeImage);
		message.setDeliveryStatus(DeliveryStatus.statusUploading);
		message.setLocalUrl(path);
		message.setWidth(width);
		message.setHeight(height);
		message.setBytes(size);

		// 2. Save locally on background
		Intent intent = new Intent(context, MessageIntentService.class);
		intent.putExtra(MessageIntentService.INTENT_EXTRA_ACTION_CODE, MessageAction.ACTION_MESSAGE_OUTGOING);
		intent.putExtra(MessageIntentService.INTENT_EXTRA_MESSAGE, message);
		context.startService(intent);

		if (addContact) {
			SaveContactAsync.saveBusinessAsContact(context, business);
		}
	}

}