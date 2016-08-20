package ee.app.conversa.interfaces;

import android.support.annotation.UiThread;

import com.parse.ParseFile;

import java.util.List;

import ee.app.conversa.model.Database.dbMessage;

@UiThread
public interface OnMessageTaskCompleted {
    void MessagesGetAll(List<dbMessage> response);
    void MessageSent(dbMessage response, ParseFile file);
    void MessageDeleted(dbMessage response);
    void MessageUpdated(dbMessage response);
}