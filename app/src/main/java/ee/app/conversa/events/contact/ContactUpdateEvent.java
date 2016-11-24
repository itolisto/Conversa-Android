package ee.app.conversa.events.contact;

import ee.app.conversa.contact.ContactUpdateReason;
import ee.app.conversa.model.database.dbBusiness;

/**
 * Created by edgargomez on 10/12/16.
 */

public class ContactUpdateEvent {

    private final dbBusiness contact;
    private final ContactUpdateReason reason;

    public ContactUpdateEvent(dbBusiness contact, ContactUpdateReason reason) {
        this.contact = contact;
        this.reason = reason;
    }

    public dbBusiness getContact() {
        return contact;
    }

    public ContactUpdateReason getReason() {
        return reason;
    }

}