package ee.app.conversa.events.contact;

import java.util.List;

/**
 * Created by edgargomez on 10/12/16.
 */

public class ContactDeleteEvent {

    private final List<String> contact_list;

    public ContactDeleteEvent(List<String> contact_list) {
        this.contact_list = contact_list;
    }

    public List<String> getContactList() {
        return contact_list;
    }

}