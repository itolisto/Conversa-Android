package ee.app.conversa.events;

import java.util.List;

import ee.app.conversa.model.database.dBusiness;

/**
 * Created by edgargomez on 9/6/16.
 */
public class ContactEvent {

    private final int action_code;
    private final dBusiness response;
    private final List<dBusiness> list_response;

    public ContactEvent(int action_code, dBusiness response, List<dBusiness> list_response) {
        this.action_code = action_code;
        this.response = response;
        this.list_response = list_response;
    }

    public int getActionCode() {
        return action_code;
    }

    public dBusiness getResponse() {
        return response;
    }

    public List<dBusiness> getListResponse() {
        return list_response;
    }

}
