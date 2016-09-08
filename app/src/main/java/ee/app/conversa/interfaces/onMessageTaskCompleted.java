package ee.app.conversa.interfaces;

import android.support.annotation.UiThread;

import com.parse.ParseFile;

import java.util.List;

import ee.app.conversa.model.database.dbMessage;

@UiThread
public interface OnMessageTaskCompleted {
    void MessagesGetAll(List<dbMessage> response);
    void MessageSent(dbMessage response);
    void MessageReceived(dbMessage response);
    void MessageDeleted(dbMessage response);
    void MessageUpdated(dbMessage response);
}