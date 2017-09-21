package ee.app.conversa.interfaces;

import android.view.View;

import ee.app.conversa.model.database.dbMessage;

/**
 * Created by edgargomez on 10/31/16.
 */

public interface OnMessageClickListener {
    void onMessageClick(dbMessage message, View view, int position);
}
