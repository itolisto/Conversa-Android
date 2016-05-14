package ee.app.conversa.adapters;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import ee.app.conversa.FragmentBusiness;
import ee.app.conversa.R;
import ee.app.conversa.model.Parse.bCategory;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder>{

    private AppCompatActivity mActivity;
    private List<bCategory> mCategories = new ArrayList<>();
    private CategoryAdapter adapter;
    private FragmentManager fragment;
//    private PagerAdapter.FirstPageFragmentListener firstPageListener;

    public CategoryAdapter(AppCompatActivity activity, List<bCategory> categories, FragmentManager fragment) {
        mCategories = categories;
        mActivity = activity;
        adapter = this;
        this.fragment = fragment;
//        firstPageListener = listener;
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
            bCategory category = mCategories.get(getAdapterPosition());
            ConversaApp.getPreferences().setCurrentCategory(category.getObjectId());
            ConversaApp.getPreferences().setCurrentCategoryTitle(category.getName());
            mActivity.getSupportActionBar().setTitle(category.getName());
            mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//            FragmentTransaction trans = mActivity.getFragmentManager().beginTransaction();
            FragmentTransaction transaction = fragment.beginTransaction();

            /*
             * IMPORTANT: We use the "root frame" defined in
             * "root_fragment.xml" as the reference to replace fragment
             */
            transaction.replace(R.id.root_frame, new FragmentBusiness(fragment));

            /*
             * IMPORTANT: The following lines allow us to add the fragment
             * to the stack and return to it later, by pressing back
             */
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }
}

