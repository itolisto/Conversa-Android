package ee.app.conversa.events.message;

import java.util.List;

import ee.app.conversa.messaging.MessageDeleteReason;

/**
 * Created by edgargomez on 10/12/16.
 */

public class MessageDeleteEvent {

    private final List<String> messageList;
    private final MessageDeleteReason reason;

    public MessageDeleteEvent(List<String> messageList, MessageDeleteReason reason) {
        this.messageList = messageList;
        this.reason = reason;
    }

    public List<String> getMessageList() {
        return messageList;
    }

    public MessageDeleteReason getReason() {
        return reason;
    }

}