package ee.app.conversa.events.message;

import java.util.List;

/**
 * Created by edgargomez on 10/12/16.
 */

public class MessageDeleteEvent {

    private final List<String> messageList;

    public MessageDeleteEvent(List<String> messageList) {
        this.messageList = messageList;
    }

    public List<String> getMessageList() {
        return messageList;
    }

}