package ee.app.conversa.adapters;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ee.app.conversa.R;
import ee.app.conversa.holders.BaseHolder;
import ee.app.conversa.holders.BusinessViewHolder;
import ee.app.conversa.holders.FixedViewHolder;
import ee.app.conversa.holders.LoaderViewHolder;
import ee.app.conversa.holders.NHeaderViewHolder;
import ee.app.conversa.interfaces.OnContactClickListener;
import ee.app.conversa.model.database.dbBusiness;
import ee.app.conversa.model.nHeaderTitle;

public class BusinessAdapter extends RecyclerView.Adapter<BaseHolder> {

    private final AppCompatActivity mActivity;
    private List<Object> mBusiness;
    private OnContactClickListener localListener;

    private final int HEADER_TYPE = 1;
    private final int BUSINESS_TYPE = 2;
    private final int FIXED_TYPE = 3;
    private final int LOAD_TYPE = 4;

    public BusinessAdapter(AppCompatActivity activity, OnContactClickListener localListener) {
        this.mActivity = activity;
        this.mBusiness = new ArrayList<>();
        this.localListener = localListener;
    }

    @Override
    public int getItemViewType(int position) {
        Object object = mBusiness.get(position);
        if (object instanceof nHeaderTitle) {
            return HEADER_TYPE;
        } else if (object instanceof dbBusiness) {
            return (((dbBusiness) object).getAvatarVisibility() == View.VISIBLE) ? BUSINESS_TYPE : FIXED_TYPE;
        } else {
            return LOAD_TYPE;
        }
    }

    @Override
    public int getItemCount() {
        return mBusiness.size();
    }

    @Override
    public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == HEADER_TYPE) {
            return new NHeaderViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.category_header, parent, false),
                    this.mActivity);
        } else if (viewType == BUSINESS_TYPE) {
            return new BusinessViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.business_item, parent, false),
                    this.mActivity);
        } else if (viewType == FIXED_TYPE) {
            return new FixedViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.fixed_item, parent, false),
                    this.mActivity);
        } else {
            return new LoaderViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.loader_item, parent, false),
                    this.mActivity);
        }
    }

    @Override
    public void onBindViewHolder(BaseHolder holder, int i) {
        Object object = mBusiness.get(i);

        if (object.getClass().equals(dbBusiness.class)) {
            if (((dbBusiness) object).getAvatarVisibility() == View.VISIBLE) {
                ((BusinessViewHolder) holder).setBusiness(object, localListener);
            } else {
                ((FixedViewHolder) holder).setBusiness(object, localListener);
            }
        } else if (object.getClass().equals(nHeaderTitle.class)) {
            ((NHeaderViewHolder) holder).setHeaderTitle(((nHeaderTitle) object).getHeaderName());
        }
    }

    public void addLoad(boolean show) {
        int position = mBusiness.size();
        if (show) {
            mBusiness.add(position, new Object());
            notifyItemInserted(position);
        } else {
            mBusiness.remove(position - 1);
            notifyItemRemoved(position - 1);
        }
    }

    public void addRecents(List<Object> business) {
        mBusiness.clear();
        mBusiness.addAll(business);
        notifyDataSetChanged();
    }

    public void addSearches(List<Object> business) {
        int position = mBusiness.size();
        mBusiness.addAll(business);
        notifyItemRangeInserted(position, business.size());
    }

    public void addItems(List<dbBusiness> business) {
        int position = mBusiness.size();
        mBusiness.addAll(business);
        notifyItemRangeInserted(position, business.size());
    }

    public void clear() {
        int size = mBusiness.size();
        mBusiness.clear();
        notifyItemRangeRemoved(0, size);
    }

}

