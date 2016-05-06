package ee.app.conversa.adapters;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseImageView;

import java.util.ArrayList;
import java.util.List;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.R;
import ee.app.conversa.model.Parse.bCategory;
import ee.app.conversa.utils.PagerAdapter;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder>{

    private AppCompatActivity mActivity;
    private List<bCategory> mCategories = new ArrayList<>();
    private CategoryAdapter adapter;
    private PagerAdapter.FirstPageFragmentListener firstPageListener;

    public CategoryAdapter(AppCompatActivity activity, List<bCategory> categories, PagerAdapter.FirstPageFragmentListener listener) {
        mCategories = categories;
        mActivity = activity;
        adapter = this;
        firstPageListener = listener;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemCount() {
        return (mCategories == null) ? 0 : mCategories.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int i) {
        bCategory category = mCategories.get(i);
        holder.tvCategoryTitle.setText(category.getName());
        holder.ivUserImage.setParseFile(category.getThumbnail());
        holder.ivUserImage.loadInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                holder.pbLoading.setVisibility(View.GONE);
                holder.ivUserImage.setVisibility(View.VISIBLE);
            }
        });
    }

    public void setItems(List<bCategory> categories) {
        mCategories = categories;
        this.notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView tvCategoryTitle;
        public ParseImageView ivUserImage;
        public ProgressBar pbLoading;

        public ViewHolder(View itemView) {
            super(itemView);

            this.tvCategoryTitle = (TextView) itemView.findViewById(R.id.tvCategoryTitle);
            this.ivUserImage = (ParseImageView) itemView.findViewById(R.id.ivCategoryImage);
            this.pbLoading = (ProgressBar) itemView.findViewById(R.id.pbLoadingForImage);

            this.tvCategoryTitle.setTypeface(ConversaApp.getTfRalewayRegular());

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
//            Category category = (Category) mCategories.get(getPosition());
//            ConversaApp.getPreferences().setCurrentCategory(category.getmId());
//            ConversaApp.getPreferences().setCurrentCategoryTitle(category.getmTitle(mActivity));
//            ((AppCompatActivity) mActivity).getSupportActionBar().setTitle(category.getmTitle(mActivity));
//            ((AppCompatActivity) mActivity).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            firstPageListener.onSwitchToNextFragment();
        }
    }
}

