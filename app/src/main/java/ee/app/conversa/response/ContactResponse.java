package ee.app.conversa.response;

import java.util.List;

import ee.app.conversa.model.Database.dBusiness;

/**
 * Created by edgargomez on 7/8/16.
 */
public class ContactResponse {

    public int getActionCode() {
        return actionCode;
    }

    public dBusiness getBusiness() {
        return customer;
    }

    public List<dBusiness> getBusinesss() {
        return customers;
    }

    private int actionCode;
    private dBusiness customer;
    private List<dBusiness> customers;

    public ContactResponse(int actionCode) {
        this.actionCode = actionCode;
        this.customer = null;
        this.customers = null;
    }

    public ContactResponse(int actionCode, dBusiness customer, List<dBusiness> customers) {
        this.actionCode = actionCode;
        this.customer = customer;
        this.customers = customers;
    }

}
