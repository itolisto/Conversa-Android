package ee.app.conversa.response;

import java.util.List;

import ee.app.conversa.model.Database.Message;

/**
 * Created by edgargomez on 7/8/16.
 */
public class MessageResponse {

    public int getActionCode() {
        return actionCode;
    }

    public Message getMessage() {
        return message;
    }

    public List<Message> getMessages() {
        return messages;
    }

    private int actionCode;
    private Message message;
    private List<Message> messages;

    public MessageResponse(int actionCode) {
        this.actionCode = actionCode;
        this.message = null;
        this.messages = null;
    }

    public MessageResponse(int actionCode, Message message, List<Message> messages) {
        this.actionCode = actionCode;
        this.message = message;
        this.messages = messages;
    }

}
