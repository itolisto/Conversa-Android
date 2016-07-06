package ee.app.conversa.responses;

import java.util.List;

import ee.app.conversa.model.Database.Message;

/**
 * Created by edgargomez on 7/4/16.
 */
public class MessageResponse {

    private int actionCode;
    private List<Message> response;

    public MessageResponse(int actionCode, List<Message> response) {
        this.actionCode = actionCode;
        this.response = response;
    }

    public int getActionCode() { return actionCode; }
    public List<Message> getResponse() { return response; }
}
