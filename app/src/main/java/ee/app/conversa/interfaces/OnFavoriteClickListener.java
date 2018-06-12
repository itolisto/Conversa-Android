package ee.app.conversa.interfaces;

import android.view.View;

import ee.app.conversa.model.Favorite;

/**
 * Created by edgargomez on 12/03/17.
 */

public interface OnFavoriteClickListener {
    void onFavoriteClick(Favorite category, View itemView, int position);
}