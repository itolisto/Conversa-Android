package ee.app.conversa.events.contact;

import ee.app.conversa.model.database.dbBusiness;

/**
 * Created by edgargomez on 10/12/16.
 */

public class ContactUpdateEvent {

    private final dbBusiness contact;

    public ContactUpdateEvent(dbBusiness contact) {
        this.contact = contact;
    }

    public dbBusiness getContact() {
        return contact;
    }

}