package ee.app.conversa.holders;

import android.view.View;

import ee.app.conversa.R;
import ee.app.conversa.view.MediumTextView;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * Created by edgargomez on 10/31/16.
 */

public class HeaderViewHolder extends FlexibleViewHolder {

    public MediumTextView mRtvHeader;

    public HeaderViewHolder(View view, FlexibleAdapter adapter) {
        super(view, adapter, true);//True for sticky
        this.mRtvHeader = view.findViewById(R.id.rtvHeader);
    }

    public void setHeaderTitle(String title) {
        mRtvHeader.setText((title == null) ? "Encabezado" : title);
    }

}
