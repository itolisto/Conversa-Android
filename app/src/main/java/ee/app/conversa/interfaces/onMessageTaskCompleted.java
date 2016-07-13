package ee.app.conversa.interfaces;

import ee.app.conversa.response.MessageResponse;


public interface OnMessageTaskCompleted {
    void MessagesGetAll(MessageResponse response);
    void MessageSent(MessageResponse response);
    void MessageDeleted(MessageResponse response);
    void MessageUpdated(MessageResponse response);
}