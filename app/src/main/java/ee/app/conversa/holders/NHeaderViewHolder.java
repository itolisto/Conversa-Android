package ee.app.conversa.holders;

import android.support.v7.app.AppCompatActivity;
import android.view.View;

import ee.app.conversa.R;
import ee.app.conversa.view.MediumTextView;

/**
 * Created by edgargomez on 10/31/16.
 */

public class NHeaderViewHolder extends BaseHolder {

    public MediumTextView mRtvHeader;

    public NHeaderViewHolder(View itemView, AppCompatActivity activity) {
        super(itemView, activity);
        this.mRtvHeader = (MediumTextView) itemView.findViewById(R.id.rtvHeader);
    }

    public void setHeaderTitle(String title) {
        mRtvHeader.setText((title == null) ? "Encabezado" : title);
    }

}
