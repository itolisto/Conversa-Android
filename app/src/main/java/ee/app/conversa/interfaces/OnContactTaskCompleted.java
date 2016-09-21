package ee.app.conversa.interfaces;

import android.support.annotation.UiThread;

import java.util.List;

import ee.app.conversa.model.database.dbBusiness;

/**
 * Created by edgargomez on 7/4/16.
 */
@UiThread
public interface OnContactTaskCompleted {
    void ContactGetAll(List<dbBusiness> response);
    void ContactAdded(dbBusiness response);
    void ContactDeleted(List<String> response);
    void ContactUpdated(dbBusiness response);
}