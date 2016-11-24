package ee.app.conversa.interfaces;

import android.view.View;

import ee.app.conversa.model.database.dbBusiness;

/**
 * Created by edgargomez on 7/4/16.
 */
public interface OnContactClickListener {
    void onContactClick(dbBusiness contact, View v, int position);
}