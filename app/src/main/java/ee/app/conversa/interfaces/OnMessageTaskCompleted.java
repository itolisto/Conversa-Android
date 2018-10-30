package ee.app.conversa.interfaces;

import java.util.List;

import androidx.annotation.UiThread;
import ee.app.conversa.messaging.MessageDeleteReason;
import ee.app.conversa.messaging.MessageUpdateReason;
import ee.app.conversa.model.database.dbMessage;

@UiThread
public interface OnMessageTaskCompleted {
    void MessagesGetAll(List<dbMessage> response);
    void MessageSent(dbMessage response);
    void MessageReceived(dbMessage response);
    void MessageDeleted(List<String> response, MessageDeleteReason reason);
    void MessageUpdated(dbMessage response, MessageUpdateReason reason);
    void onTypingMessage(String from, boolean isTyping);
}