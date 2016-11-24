package ee.app.conversa.adapters;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ee.app.conversa.R;
import ee.app.conversa.holders.BaseHolder;
import ee.app.conversa.holders.BusinessViewHolder;
import ee.app.conversa.holders.HeaderViewHolder;
import ee.app.conversa.interfaces.OnBusinessClickListener;
import ee.app.conversa.interfaces.OnContactClickListener;
import ee.app.conversa.model.database.dbBusiness;
import ee.app.conversa.model.nHeaderTitle;
import ee.app.conversa.model.parse.Business;

public class BusinessAdapter extends RecyclerView.Adapter<BaseHolder> {

    private final AppCompatActivity mActivity;
    private List<Object> mBusiness;
    private OnBusinessClickListener listener;
    private OnContactClickListener localListener;

    private final int HEADER_TYPE = 1;
    private final int BUSINESS_TYPE = 2;

    public BusinessAdapter(AppCompatActivity activity, OnBusinessClickListener listener) {
        this.mActivity = activity;
        this.mBusiness = new ArrayList<>();
        this.listener = listener;
    }

    public BusinessAdapter(AppCompatActivity activity, OnContactClickListener localListener) {
        this.mActivity = activity;
        this.mBusiness = new ArrayList<>();
        this.localListener = localListener;
    }

    @Override
    public int getItemViewType(int position) {
        return (mBusiness.get(position) instanceof nHeaderTitle) ? HEADER_TYPE : BUSINESS_TYPE;
    }

    @Override
    public int getItemCount() {
        return mBusiness.size();
    }

    @Override
    public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == BUSINESS_TYPE) {
            return new BusinessViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.business_item, parent, false),
                    this.mActivity);
        } else {
            return new HeaderViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.category_header, parent, false),
                    this.mActivity);
        }
    }

    @Override
    public void onBindViewHolder(BaseHolder holder, int i) {
        Object object = mBusiness.get(i);

        if (object.getClass().equals(Business.class)) {
            ((BusinessViewHolder) holder).setBusiness(object, listener);
        } else if (object.getClass().equals(dbBusiness.class)) {
            ((BusinessViewHolder) holder).setBusiness(object, localListener);
        } else {
            ((HeaderViewHolder) holder).setHeaderTitle(((nHeaderTitle) object).getHeaderName());
        }
    }

    public void setRecents(List<Object> business) {
        mBusiness.clear();
        mBusiness.addAll(business);
        notifyDataSetChanged();
    }

    public void setSearches(List<Object> business, boolean clear) {
        if (clear) {
            mBusiness.clear();
        }

        int position = mBusiness.size();
        mBusiness.addAll(business);

        if (clear) {
            this.notifyDataSetChanged();
        } else {
            this.notifyItemRangeInserted(position, business.size());
        }
    }

    public void addItems(List<Business> business, boolean addLoadMoreCell) {
        mBusiness.addAll(business);
        this.notifyItemRangeInserted(mBusiness.size(), business.size());
    }

    public void addLocalItems(List<dbBusiness> business, boolean addLoadMoreCell) {
        mBusiness.addAll(business);
        this.notifyItemRangeInserted(mBusiness.size(), business.size());
    }

    public void clear() {
        int size = mBusiness.size();
        mBusiness.clear();
        this.notifyItemRangeRemoved(0, size);
    }

}

