package ee.app.conversa.interfaces;

import android.support.annotation.UiThread;

import java.util.List;

import ee.app.conversa.model.database.dBusiness;

/**
 * Created by edgargomez on 7/4/16.
 */
@UiThread
public interface OnContactTaskCompleted {
    void ContactGetAll(List<dBusiness> response);
    void ContactAdded(dBusiness response);
    void ContactDeleted(dBusiness response);
    void ContactUpdated(dBusiness response);
}