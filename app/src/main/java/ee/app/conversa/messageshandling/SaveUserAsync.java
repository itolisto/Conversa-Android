package ee.app.conversa.messageshandling;

import android.content.Context;
import android.content.Intent;

import ee.app.conversa.management.contact.ContactIntentService;
import ee.app.conversa.model.database.dbBusiness;

/**
 * Created by edgargomez on 7/4/16.
 */
public class SaveUserAsync {

    public static void saveBusinessAsContact(Context context, dbBusiness business) {
        // 1. Save locally on background
        Intent intent = new Intent(context, ContactIntentService.class);
        intent.putExtra(ContactIntentService.INTENT_EXTRA_ACTION_CODE, ContactIntentService.ACTION_MESSAGE_SAVE);
        intent.putExtra(ContactIntentService.INTENT_EXTRA_CUSTOMER, business);
        context.startService(intent);
    }

}
