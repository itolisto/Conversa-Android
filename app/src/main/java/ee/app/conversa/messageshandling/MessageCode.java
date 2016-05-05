package ee.app.conversa.messageshandling;

import ee.app.conversa.model.Database.Message;

/**
 * Created by edgargomez on 7/14/15.
 */
public class MessageCode {
    private Message message;
    private int code;

    MessageCode(Message m, int c) {
        message = m;
        code = c;
    }

    public int getCode() { return code; }
    public Message getMessage() { return message; }
}
