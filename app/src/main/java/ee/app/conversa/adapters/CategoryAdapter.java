package ee.app.conversa.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.R;
import ee.app.conversa.model.Parse.bCategory;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder>{

    private Context activity;
    private List<bCategory> mCategories;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position, bCategory category);
    }

    public CategoryAdapter(Context activity, OnItemClickListener listener) {
        this.activity = activity;
        this.mCategories = new ArrayList<>();
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return mCategories.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int i) {
        final bCategory category = mCategories.get(i);
        holder.tvCategoryTitle.setText(category.getCategoryName(activity));

        if(category.getThumbnail().getUrl().length() > 0) {
            Uri uri = Uri.parse(category.getThumbnail().getUrl());
            holder.sdvCategoryImage.setImageURI(uri);
        } else {
            Uri path = Uri.parse("android.resource://ee.app.conversa/" + R.drawable.business_default);
            holder.sdvCategoryImage.setImageURI(path);
        }
    }

    public void setItems(List<bCategory> categories) {
        mCategories = categories;
        this.notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView tvCategoryTitle;
        public SimpleDraweeView sdvCategoryImage;

        public ViewHolder(View itemView) {
            super(itemView);

            this.tvCategoryTitle = (TextView) itemView.findViewById(R.id.tvCategoryTitle);
            this.sdvCategoryImage = (SimpleDraweeView) itemView.findViewById(R.id.sdvCategoryImage);
            this.tvCategoryTitle.setTypeface(ConversaApp.getTfRalewayRegular());
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (listener != null)
                listener.onItemClick(itemView, getLayoutPosition(), mCategories.get(getAdapterPosition()));
        }
    }
}

