package ee.app.conversa.holders;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.facebook.drawee.view.SimpleDraweeView;

import ee.app.conversa.R;
import ee.app.conversa.interfaces.OnCategoryClickListener;
import ee.app.conversa.model.nCategory;
import ee.app.conversa.utils.Utils;
import ee.app.conversa.view.MediumTextView;

/**
 * Created by edgargomez on 10/31/16.
 */

public class CategoryViewHolder extends BaseHolder {

    private OnCategoryClickListener listener;
    public MediumTextView tvCategoryTitle;
    public SimpleDraweeView sdvCategoryImage;
    public nCategory category;
    public View vDivider;

    public CategoryViewHolder(View itemView, AppCompatActivity activity, OnCategoryClickListener listener) {
        super(itemView, activity);
        this.tvCategoryTitle = (MediumTextView) itemView.findViewById(R.id.tvCategoryTitle);
        this.sdvCategoryImage = (SimpleDraweeView) itemView.findViewById(R.id.sdvCategoryImage);
        this.vDivider = itemView.findViewById(R.id.vDivider);

        this.listener = listener;

        itemView.setOnClickListener(this);
    }

    public void setCategory(nCategory category) {
        this.category = category;

        tvCategoryTitle.setText(category.getCategoryName(activity));
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                activity.getResources().getDimensionPixelSize(R.dimen.category_item_divider_height));

        if (category.getRemoveDividerMargin()) {
            params.setMargins(0, 0, 0, 0);
        } else {
            params.setMargins(Utils.dpToPixels(activity, 35), 0, 0, 0);
        }

        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        vDivider.setLayoutParams(params);

        Uri uri;

        if(category.getAvatarUrl().isEmpty()) {
            uri = Utils.getDefaultImage(activity, R.drawable.ic_business_default);
        } else {
            uri = Uri.parse(category.getAvatarUrl());
        }

        sdvCategoryImage.setImageURI(uri);
    }

    @Override
    public void onClick(View view) {
        if (listener != null) {
            listener.onCategoryClick(category, itemView, getAdapterPosition());
        }
    }

}
