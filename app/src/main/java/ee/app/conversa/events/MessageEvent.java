package ee.app.conversa.events;

import java.util.List;

import ee.app.conversa.management.message.MessageIntentService;
import ee.app.conversa.model.database.dbMessage;

/**
 * Created by edgargomez on 9/6/16.
 */
public class MessageEvent {

    private final int action_code;
    private final dbMessage response;
    private final List<dbMessage> list_response;

    public MessageEvent(int action_code, dbMessage response, List<dbMessage> list_response) {
        this.action_code = action_code;
        this.response = response;
        this.list_response = list_response;
    }

    public int getActionCode() {
        return action_code;
    }

    public dbMessage getResponse() {
        return response;
    }

    public List<dbMessage> getListResponse() {
        return list_response;
    }

    public static MessageEvent sendMessageEvent(dbMessage message) {
        return new MessageEvent(
                MessageIntentService.ACTION_MESSAGE_SAVE,
                message,
                null);
    }

    public static MessageEvent receiveMessageEvent(dbMessage message) {
        return new MessageEvent(
                MessageIntentService.ACTION_MESSAGE_NEW_MESSAGE,
                message,
                null);
    }

}
