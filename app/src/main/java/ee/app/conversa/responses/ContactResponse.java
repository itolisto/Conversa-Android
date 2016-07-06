package ee.app.conversa.responses;

import java.util.List;

import ee.app.conversa.model.Database.dBusiness;

/**
 * Created by edgargomez on 7/4/16.
 */
public class ContactResponse {

    private int actionCode;
    private List<dBusiness> response;

    public ContactResponse(int actionCode, List<dBusiness> response) {
        this.actionCode = actionCode;
        this.response = response;
    }

    public int getActionCode() { return actionCode; }
    public List<dBusiness> getResponse() { return response; }

}
