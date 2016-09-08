package ee.app.conversa.events;

import ee.app.conversa.model.database.dbMessage;

/**
 * Created by edgargomez on 9/7/16.
 */
public class MessagePressedEvent {

    private final dbMessage message;

    public MessagePressedEvent(dbMessage message) {
        this.message = message;
    }

    public dbMessage getMessage() {
        return message;
    }
}
