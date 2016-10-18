package ee.app.conversa.interfaces;

import android.support.annotation.UiThread;

import java.util.List;

import ee.app.conversa.messaging.MessageUpdateReason;
import ee.app.conversa.model.database.dbMessage;

@UiThread
public interface OnMessageTaskCompleted {
    void MessagesGetAll(List<dbMessage> response);
    void MessageSent(dbMessage response);
    void MessageReceived(dbMessage response);
    void MessageDeleted(List<String> response);
    void MessageUpdated(dbMessage response, MessageUpdateReason reason);
    void onTypingMessage(String from, boolean isTyping);
}