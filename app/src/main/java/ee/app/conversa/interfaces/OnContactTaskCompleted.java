package ee.app.conversa.interfaces;

import ee.app.conversa.responses.ContactResponse;

/**
 * Created by edgargomez on 7/4/16.
 */
public interface OnContactTaskCompleted {
    void OnContactTaskCompleted(ContactResponse response);
}