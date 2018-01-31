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
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.viewholders.FlexibleViewHolder;

import static ee.app.conversa.R.id.sdvCategoryImage;
import static ee.app.conversa.R.id.tvCategoryTitle;
import static ee.app.conversa.R.id.vDivider;

/**
 * Created by edgargomez on 10/31/16.
 */

public class CategoryViewHolder extends FlexibleViewHolder {

    protected final AppCompatActivity activity;
    private OnCategoryClickListener listener;
    public SimpleDraweeView mSdvCategoryImage;
    public MediumTextView mTvCategoryTitle;
    public nCategory category;
    public View mVDivider;

    public CategoryViewHolder(View view, FlexibleAdapter adapter, AppCompatActivity activity, OnCategoryClickListener listener) {
        super(view, adapter, false);
        this.mTvCategoryTitle = (MediumTextView) view.findViewById(tvCategoryTitle);
        this.mSdvCategoryImage = (SimpleDraweeView) view.findViewById(sdvCategoryImage);
        this.mVDivider = view.findViewById(vDivider);
        this.listener = listener;
        this.activity = activity;
        getContentView().setOnClickListener(this);
    }

    public void setCategory(nCategory category) {
        this.category = category;

        mTvCategoryTitle.setText(category.getCategoryName(activity));
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                activity.getResources().getDimensionPixelSize(R.dimen.category_item_divider_height));

        if (category.getRemoveDividerMargin()) {
            params.setMargins(0, 0, 0, 0);
        } else {
            params.setMargins(Utils.dpToPixels(activity, 97), 0, 0, 0);
        }

        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mVDivider.setLayoutParams(params);

        Uri uri;

        if (category.getAvatarUrl().isEmpty()) {
            uri = Utils.getDefaultImage(activity, R.drawable.ic_business_default);
        } else {
            uri = Uri.parse(category.getAvatarUrl());
        }

        mSdvCategoryImage.setImageURI(uri);
    }

    public void removeDivider(boolean remove) {
        mVDivider.setVisibility(remove ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        if (listener != null) {
            listener.onCategoryClick(category, itemView, getAdapterPosition());
        }
    }

}
