package ee.app.conversa.adapters;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ee.app.conversa.R;
import ee.app.conversa.holders.BaseHolder;
import ee.app.conversa.holders.CategoryViewHolder;
import ee.app.conversa.holders.HeaderViewHolder;
import ee.app.conversa.interfaces.OnCategoryClickListener;
import ee.app.conversa.model.nCategory;
import ee.app.conversa.model.nHeaderTitle;

public class CategoryAdapter extends RecyclerView.Adapter<BaseHolder> {

    private final AppCompatActivity mActivity;
    private List<Object> mCategories;
    private OnCategoryClickListener listener;

    private final int HEADER_TYPE = 1;
    private final int CATEGORY_TYPE = 2;

    public CategoryAdapter(AppCompatActivity mActivity, OnCategoryClickListener listener) {
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
    public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == CATEGORY_TYPE) {
            return new CategoryViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.category_item, parent, false),
                    this.mActivity,
                    listener);
        } else {
            return new HeaderViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.category_header, parent, false),
                    this.mActivity);
        }
    }

    @Override
    public void onBindViewHolder(BaseHolder holder, int i) {
        if (holder instanceof CategoryViewHolder) {
            if ((i + 1) < mCategories.size() && mCategories.get(i + 1) instanceof nHeaderTitle) {
                ((nCategory) mCategories.get(i)).setRemoveDividerMargin(true);
            } else {
                ((nCategory) mCategories.get(i)).setRemoveDividerMargin(false);
            }

            ((CategoryViewHolder) holder).setCategory((nCategory)mCategories.get(i));
        } else {
            ((HeaderViewHolder) holder).setHeaderTitle(((nHeaderTitle) mCategories.get(i)).getHeaderName());
        }
    }

    public void addItems(List<Object> list) {
        // 0. Clear all objects in list to show new ones
        mCategories.clear();
        mCategories.addAll(list);
        this.notifyDataSetChanged();
    }

}

