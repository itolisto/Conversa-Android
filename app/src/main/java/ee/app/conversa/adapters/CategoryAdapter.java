package ee.app.conversa.adapters;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ee.app.conversa.R;
import ee.app.conversa.model.nCategory;
import ee.app.conversa.model.nHeaderTitle;
import ee.app.conversa.view.MediumTextView;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.GenericViewHolder> {

    private final AppCompatActivity mActivity;
    private List<Object> mCategories;
    private OnItemClickListener listener;

    private final int HEADER_TYPE = 1;
    private final int CATEGORY_TYPE = 2;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position, nCategory category);
    }

    public CategoryAdapter(AppCompatActivity mActivity, OnItemClickListener listener) {
        this.mActivity = mActivity;
        this.mCategories = new ArrayList<>(30);
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return (mCategories.get(position) instanceof nHeaderTitle) ? HEADER_TYPE : CATEGORY_TYPE;
    }

    @Override
    public int getItemCount() {
        return mCategories.size();
    }

    @Override
    public GenericViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == CATEGORY_TYPE) {
            return new CategoryViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.category_item, parent, false),
                    new WeakReference<>(this.mActivity));
        } else {
            return new HeaderViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.recyclerview_header, parent, false),
                    new WeakReference<>(this.mActivity));
        }
    }

    @Override
    public void onBindViewHolder(GenericViewHolder holder, int i) {
        if (holder instanceof CategoryViewHolder) {
            ((CategoryViewHolder) holder).setCategory((nCategory)mCategories.get(i));
        } else {
            ((HeaderViewHolder) holder).setHeaderTitle(((nHeaderTitle)mCategories.get(i)).getHeaderName());
        }
    }

    public void addItems(List<nCategory> categories, List<nHeaderTitle> headers) {
        // 0. Clear all objects in list to show new ones
        mCategories.clear();

        // 1. Match headers relevance with categories relevance
        int headersSize = headers.size();
        int categoriesSize = categories.size();

        for (int i = 0; i < headersSize; i++) {
            // 1.1 Add header to list
            nHeaderTitle header = headers.get(i);
            mCategories.add(header);
            // 1.2 Search all categories that match this header relevance
            for (int h = 0; h < categoriesSize; h++) {
                if (categories.get(h).getRelevance() == header.getRelevance()) {
                    mCategories.add(categories.get(h));
                }
            }
        }

        Collections.sort(categories, new Comparator<nCategory>() {
            @Override
            public int compare(final nCategory object1, final nCategory object2) {
                return object1.getCategoryName(mActivity).compareTo(object2.getCategoryName(mActivity));
            }
        });

        mCategories.add(new nHeaderTitle(mActivity.getString(R.string.browse_categories), 0));
        mCategories.addAll(categories);
        this.notifyDataSetChanged();
    }

    public class HeaderViewHolder extends GenericViewHolder {

        public MediumTextView mRtvHeader;

        public HeaderViewHolder(View itemView, WeakReference<AppCompatActivity> activity) {
            super(itemView, activity);
            this.mRtvHeader = (MediumTextView) itemView.findViewById(R.id.rtvHeader);
        }

        public void setHeaderTitle(String title) {
            if (activity.get() != null) {
                mRtvHeader.setText((title == null) ? "Encabezado" : title);
            }
        }

    }

    public class CategoryViewHolder extends GenericViewHolder implements View.OnClickListener {

        public MediumTextView tvCategoryTitle;
        public SimpleDraweeView sdvCategoryImage;

        public CategoryViewHolder(View itemView, WeakReference<AppCompatActivity> activity) {
            super(itemView, activity);
            this.tvCategoryTitle = (MediumTextView) itemView.findViewById(R.id.tvCategoryTitle);
            this.sdvCategoryImage = (SimpleDraweeView) itemView.findViewById(R.id.sdvCategoryImage);
            itemView.setOnClickListener(this);
        }

        public void setCategory(nCategory category) {
            if (activity.get() != null) {
                tvCategoryTitle.setText(category.getCategoryName(activity.get()));
            }

            Uri uri;
            if(category.getAvatarUrl().isEmpty()) {
                uri = Uri.parse("android.resource://ee.app.conversa/" + R.drawable.business_default);
            } else {
                uri = Uri.parse(category.getAvatarUrl());
            }
            sdvCategoryImage.setImageURI(uri);
        }

        @Override
        public void onClick(View view) {
            if (listener != null) {
                listener.onItemClick(itemView, getLayoutPosition(), (nCategory) mCategories.get(getAdapterPosition()));
            }
        }
    }

    public class GenericViewHolder extends RecyclerView.ViewHolder {

        protected final WeakReference<AppCompatActivity> activity;

        public GenericViewHolder(View itemView, WeakReference<AppCompatActivity> activity) {
            super(itemView);
            this.activity = activity;
        }

    }

}

