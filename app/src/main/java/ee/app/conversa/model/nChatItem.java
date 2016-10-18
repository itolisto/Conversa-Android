package ee.app.conversa.model;

import ee.app.conversa.model.database.dbMessage;

/**
 * Created by edgargomez on 10/5/16.
 */
public class nChatItem {

    private final dbMessage message;
    private final boolean hasUnreadMessages;

    public nChatItem(dbMessage message, boolean hasUnreadMessages) {
        this.message = message;
        this.hasUnreadMessages = hasUnreadMessages;
    }

    public dbMessage getMessage() {
        return message;
    }

    public boolean hasUnreadMessages() {
        return hasUnreadMessages;
    }

}
