package ee.app.conversa.events.message;

import ee.app.conversa.model.database.dbMessage;

/**
 * Created by edgargomez on 10/12/16.
 */

public class MessageIncomingEvent {

    private final dbMessage message;

    public MessageIncomingEvent(dbMessage message) {
        this.message = message;
    }

    public dbMessage getMessage() {
        return message;
    }

}