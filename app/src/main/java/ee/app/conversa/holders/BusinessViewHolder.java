package ee.app.conversa.holders;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import ee.app.conversa.R;
import ee.app.conversa.interfaces.OnBusinessClickListener;
import ee.app.conversa.interfaces.OnContactClickListener;
import ee.app.conversa.model.database.dbBusiness;
import ee.app.conversa.model.parse.Business;
import ee.app.conversa.utils.Utils;

/**
 * Created by edgargomez on 10/31/16.
 */

public class BusinessViewHolder extends BaseHolder {

    public TextView tvBusiness;
    public TextView tvConversaId;
    public SimpleDraweeView sdvCategoryImage;
    public Object object;

    private OnBusinessClickListener listener;
    private OnContactClickListener localListener;

    public BusinessViewHolder(View itemView, AppCompatActivity activity) {
        super(itemView, activity);

        this.tvBusiness = (TextView) itemView.findViewById(R.id.mtvDisplayName);
        this.tvConversaId = (TextView) itemView.findViewById(R.id.ltvConversaId);
        this.sdvCategoryImage = (SimpleDraweeView) itemView.findViewById(R.id.sdvBusinessImage);

        itemView.setOnClickListener(this);
    }

    public void setBusiness(Object object, OnBusinessClickListener listener) {
        this.object = object;
        this.listener = listener;
        this.localListener = null;

        Business temp = (Business) object;
        this.tvBusiness.setText(temp.getDisplayName());
        this.tvConversaId.setText("@".concat(temp.getConversaID()));
        Uri uri;

        if (temp.getAvatar() != null) {
            uri = Utils.getUriFromString(temp.getAvatar().getUrl());

            if (uri == null) {
                uri = Utils.getDefaultImage(activity, R.drawable.ic_business_default);
            }
        } else {
            uri = Utils.getDefaultImage(activity, R.drawable.ic_business_default);
        }

        this.sdvCategoryImage.setImageURI(uri);
    }

    public void setBusiness(Object object, OnContactClickListener localListener) {
        this.object = object;
        this.listener = null;
        this.localListener = localListener;

        dbBusiness business = (dbBusiness) object;
        this.tvBusiness.setText(business.getDisplayName());
        this.tvConversaId.setText("@".concat(business.getConversaId()));

        Uri uri = Utils.getUriFromString(business.getAvatarThumbFileId());

        if (uri == null) {
            uri = Utils.getDefaultImage(activity, R.drawable.ic_business_default);
        }

        this.sdvCategoryImage.setImageURI(uri);
    }

    @Override
    public void onClick(View view) {
        if (object.getClass().equals(Business.class)) {
            if (listener != null) {
                listener.onBusinessClick((Business)object, view, getAdapterPosition());
            }
        } else if (object.getClass().equals(dbBusiness.class)) {
            if (localListener != null) {
                localListener.onContactClick((dbBusiness)object, view, getAdapterPosition());
            }
        }
    }

}
