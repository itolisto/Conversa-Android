package ee.app.conversa.messageshandling;

import ee.app.conversa.adapters.ChatsAdapter;
import ee.app.conversa.interfaces.OnContactTaskCompleted;
import ee.app.conversa.model.Database.dBusiness;

/**
 * Created by edgargomez on 7/4/16.
 */
public class SaveUserAsync {

    public static void saveBusinessAsContact(ChatsAdapter adapter, dBusiness business, OnContactTaskCompleted callback) {
        // 1. Save locally on background
        business.saveToLocalDatabase(callback);
    }

}
