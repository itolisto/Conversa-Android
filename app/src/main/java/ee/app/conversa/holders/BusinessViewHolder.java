package ee.app.conversa.holders;

import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import androidx.appcompat.app.AppCompatActivity;
import ee.app.conversa.R;
import ee.app.conversa.interfaces.OnContactClickListener;
import ee.app.conversa.model.database.dbBusiness;
import ee.app.conversa.utils.Utils;

/**
 * Created by edgargomez on 10/31/16.
 */

public class BusinessViewHolder extends BaseHolder {

    private View vDividerTwo;
    private TextView tvBusiness;
    private TextView tvConversaId;
    private SimpleDraweeView sdvCategoryImage;
    private RelativeLayout mrlBusinessLayout;
    private Object object;

    private OnContactClickListener localListener;

    public BusinessViewHolder(View itemView, AppCompatActivity activity) {
        super(itemView, activity);

        this.vDividerTwo = itemView.findViewById(R.id.vDividerTwo);
        this.tvBusiness = (TextView) itemView.findViewById(R.id.mtvDisplayName);
        this.tvConversaId = (TextView) itemView.findViewById(R.id.ltvConversaId);
        this.sdvCategoryImage = (SimpleDraweeView) itemView.findViewById(R.id.sdvBusinessImage);
        this.mrlBusinessLayout = (RelativeLayout)  itemView.findViewById(R.id.rlBusinessItem) ;

        itemView.setOnClickListener(this);
    }

    public void setBusiness(Object object, OnContactClickListener localListener) {
        this.object = object;
        this.localListener = localListener;

        dbBusiness business = (dbBusiness) object;

        if (business.getAvatarVisibility() == View.VISIBLE) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)this.vDividerTwo.getLayoutParams();
            params.leftMargin = Utils.dpToPixels(activity, 84);
            this.vDividerTwo.requestLayout();

            this.mrlBusinessLayout.getLayoutParams().height = Utils.dpToPixels(activity, 80);
            this.mrlBusinessLayout.requestLayout();

            this.sdvCategoryImage.setVisibility(View.VISIBLE);
            this.tvBusiness.setText(business.getDisplayName());
            this.tvConversaId.setText(business.getFormattedConversaId());

            Uri uri = Utils.getUriFromString(business.getAvatarThumbFileId());

            if (uri == null) {
                uri = Utils.getDefaultImage(activity, R.drawable.ic_business_default);
                this.sdvCategoryImage.setImageURI(uri);
            }

            this.sdvCategoryImage.setImageURI(uri);
        } else {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)this.vDividerTwo.getLayoutParams();
            params.leftMargin = 0;
            this.vDividerTwo.requestLayout();

            this.mrlBusinessLayout.getLayoutParams().height = Utils.dpToPixels(activity, 50);
            this.mrlBusinessLayout.requestLayout();

            this.sdvCategoryImage.setVisibility(View.INVISIBLE);
            this.tvBusiness.setText(activity.getResources().getString(R.string.conversa_agent_cell));
            this.tvConversaId.setText(activity.getResources().getString(R.string.conversa_agent_subtitle));
        }
    }

    @Override
    public void onClick(View view) {
        if (localListener != null) {
            localListener.onContactClick((dbBusiness)object, view, getAdapterPosition());
        }
    }

}
