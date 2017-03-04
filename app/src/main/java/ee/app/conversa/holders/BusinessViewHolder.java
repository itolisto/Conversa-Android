package ee.app.conversa.holders;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import ee.app.conversa.R;
import ee.app.conversa.interfaces.OnContactClickListener;
import ee.app.conversa.model.database.dbBusiness;
import ee.app.conversa.utils.Utils;

/**
 * Created by edgargomez on 10/31/16.
 */

public class BusinessViewHolder extends BaseHolder {

    public TextView tvBusiness;
    public TextView tvConversaId;
    public SimpleDraweeView sdvCategoryImage;
    public Object object;

    private OnContactClickListener localListener;

    public BusinessViewHolder(View itemView, AppCompatActivity activity) {
        super(itemView, activity);

        this.tvBusiness = (TextView) itemView.findViewById(R.id.mtvDisplayName);
        this.tvConversaId = (TextView) itemView.findViewById(R.id.ltvConversaId);
        this.sdvCategoryImage = (SimpleDraweeView) itemView.findViewById(R.id.sdvBusinessImage);

        itemView.setOnClickListener(this);
    }

    public void setBusiness(Object object, OnContactClickListener localListener) {
        this.object = object;
        this.localListener = localListener;

        dbBusiness business = (dbBusiness) object;
        this.tvBusiness.setText(business.getDisplayName());
        this.tvConversaId.setText(business.getFormattedConversaId());

        Uri uri = Utils.getUriFromString(business.getAvatarThumbFileId());

        if (uri == null) {
            uri = Utils.getDefaultImage(activity, R.drawable.ic_business_default);
        }

        this.sdvCategoryImage.setImageURI(uri);
    }

    @Override
    public void onClick(View view) {
        if (localListener != null) {
            localListener.onContactClick((dbBusiness)object, view, getAdapterPosition());
        }
    }

}
