package ee.app.conversa.interfaces;

import ee.app.conversa.responses.MessageResponse;

public interface OnMessageTaskCompleted {
    void OnMessageTaskCompleted(MessageResponse response);
}