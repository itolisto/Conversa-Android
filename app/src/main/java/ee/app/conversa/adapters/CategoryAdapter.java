package ee.app.conversa.adapters;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.R;
import ee.app.conversa.model.Database.Category;
import ee.app.conversa.utils.PagerAdapter;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder>{

    private AppCompatActivity mActivity;
    private List<Category> mCategories = new ArrayList<>();
    private CategoryAdapter adapter;
    private PagerAdapter.FirstPageFragmentListener firstPageListener;

    public CategoryAdapter(AppCompatActivity activity, List<Category> categories, PagerAdapter.FirstPageFragmentListener listener) {
        mCategories = categories;
        mActivity = activity;
        adapter = this;
        firstPageListener = listener;
    }

    @Override
    public long getItemId(int position) { return super.getItemId(position); }

    @Override
    public int getItemCount() { return (mCategories == null) ? 0 : mCategories.size(); }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, parent, false);
        ViewHolder mh = new ViewHolder(v);
        return mh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int i) {
        Category category = mCategories.get(i);
        holder.tvCategoryTitle.setText(category.getmTitle(mActivity));

        if (Build.VERSION.SDK_INT >= 16) {
            holder.ivUserImage.setBackground(category.getDrawable(mActivity));
        } else {
            holder.ivUserImage.setBackgroundDrawable(category.getDrawable(mActivity));
        }
    }

    public void setItems(List<Category> categories) { mCategories = categories; }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView tvCategoryTitle;
        public ImageView ivUserImage;
        public ProgressBar pbLoading;

        public ViewHolder(View itemView) {
            super(itemView);

            this.tvCategoryTitle = (TextView) itemView
                    .findViewById(R.id.tvCategoryTitle);
            this.tvCategoryTitle.setTypeface(ConversaApp.getTfRalewayRegular());

            this.ivUserImage = (ImageView) itemView
                    .findViewById(R.id.ivCategoryImage);
            this.pbLoading = (ProgressBar) itemView
                    .findViewById(R.id.pbLoadingForImage);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Category category = (Category) mCategories.get(getPosition());
            ConversaApp.getPreferences().setCurrentCategory(category.getmId());
            ConversaApp.getPreferences().setCurrentCategoryTitle(category.getmTitle(mActivity));
            ((AppCompatActivity) mActivity).getSupportActionBar().setTitle(category.getmTitle(mActivity));
            ((AppCompatActivity) mActivity).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            firstPageListener.onSwitchToNextFragment();
        }
    }
}

