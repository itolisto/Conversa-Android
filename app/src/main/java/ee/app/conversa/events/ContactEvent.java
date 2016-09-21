package ee.app.conversa.events;

import java.util.List;

import ee.app.conversa.model.database.dbBusiness;

/**
 * Created by edgargomez on 9/6/16.
 */
public class ContactEvent {

    private final int action_code;
    private final dbBusiness response;
    private final List<dbBusiness> list_response;
    private final List<String> contact_list;

    public ContactEvent(int action_code, dbBusiness response, List<dbBusiness> list_response, List<String> contact_list) {
        this.action_code = action_code;
        this.response = response;
        this.list_response = list_response;
        this.contact_list = contact_list;
    }

    public int getActionCode() {
        return action_code;
    }

    public dbBusiness getResponse() {
        return response;
    }

    public List<dbBusiness> getListResponse() {
        return list_response;
    }

    public List<String> getContactList() {
        return contact_list;
    }

}
