package ee.app.conversa.interfaces;

import android.view.View;

import ee.app.conversa.model.nCategory;

/**
 * Created by edgargomez on 10/31/16.
 */

public interface OnCategoryClickListener {
    void onCategoryClick(nCategory category, View itemView, int position);
}