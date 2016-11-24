package ee.app.conversa.interfaces;

import android.view.View;

import ee.app.conversa.model.parse.Business;

/**
 * Created by edgargomez on 10/31/16.
 */

public interface OnBusinessClickListener {
    void onBusinessClick(Business contact, View v, int position);
}
