package ee.app.conversa.holders;

import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import ee.app.conversa.interfaces.OnContactClickListener;
import ee.app.conversa.model.database.dbBusiness;

/**
 * Created by edgargomez on 10/31/16.
 */

public class FixedViewHolder extends BaseHolder {

    private Object object;

    private OnContactClickListener localListener;

    public FixedViewHolder(View itemView, AppCompatActivity activity) {
        super(itemView, activity);
        itemView.setOnClickListener(this);
    }

    public void setBusiness(Object object, OnContactClickListener localListener) {
        this.object = object;
        this.localListener = localListener;
    }

    @Override
    public void onClick(View view) {
        if (localListener != null) {
            localListener.onContactClick((dbBusiness)object, view, getAdapterPosition());
        }
    }

}
