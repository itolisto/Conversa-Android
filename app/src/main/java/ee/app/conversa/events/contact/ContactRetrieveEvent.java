package ee.app.conversa.events.contact;

import java.util.List;

import ee.app.conversa.model.database.dbBusiness;

/**
 * Created by edgargomez on 10/12/16.
 */

public class ContactRetrieveEvent {

    private final List<dbBusiness> list_response;

    public ContactRetrieveEvent(List<dbBusiness> list_response) {
        this.list_response = list_response;
    }

    public List<dbBusiness> getListResponse() {
        return list_response;
    }

}