package ee.app.conversa.response;

import java.util.List;

import ee.app.conversa.model.Database.dbMessage;

/**
 * Created by edgargomez on 7/8/16.
 */
public class MessageResponse {

    public int getActionCode() {
        return actionCode;
    }

    public dbMessage getMessage() {
        return message;
    }

    public List<dbMessage> getMessages() {
        return messages;
    }

    private int actionCode;
    private dbMessage message;
    private List<dbMessage> messages;

    public MessageResponse(int actionCode) {
        this.actionCode = actionCode;
        this.message = null;
        this.messages = null;
    }

    public MessageResponse(int actionCode, dbMessage message, List<dbMessage> messages) {
        this.actionCode = actionCode;
        this.message = message;
        this.messages = messages;
    }

}
