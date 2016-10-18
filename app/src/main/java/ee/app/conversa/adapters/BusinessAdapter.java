package ee.app.conversa.adapters;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import ee.app.conversa.R;
import ee.app.conversa.model.database.dbBusiness;
import ee.app.conversa.model.nHeaderTitle;
import ee.app.conversa.model.parse.Business;
import ee.app.conversa.utils.Utils;
import ee.app.conversa.view.MediumTextView;

public class BusinessAdapter extends RecyclerView.Adapter<BusinessAdapter.GenericViewHolder> {

    private final AppCompatActivity mActivity;
    private List<Object> mBusiness;
    private OnItemClickListener listener;
    private OnLocalItemClickListener localListener;

    private final int HEADER_TYPE = 1;
    private final int BUSINESS_TYPE = 2;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position, Business business);
    }

    public interface OnLocalItemClickListener {
        void onItemClick(View itemView, int position, dbBusiness business);
    }

    public BusinessAdapter(AppCompatActivity activity, OnItemClickListener listener) {
        this.mActivity = activity;
        this.mBusiness = new ArrayList<>();
        this.listener = listener;
    }

    public BusinessAdapter(AppCompatActivity activity, OnLocalItemClickListener localListener) {
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
    public GenericViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == BUSINESS_TYPE) {
            return new ViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.business_item, parent, false),
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
        Object object = mBusiness.get(i);

        if (object.getClass().equals(Business.class)) {
            Business temp = (Business) object;
            ((ViewHolder) holder).tvBusiness.setText(temp.getDisplayName());
            ((ViewHolder) holder).tvConversaId.setText(temp.getConversaID());
            Uri uri;

            if (temp.getAvatar() != null) {
                uri = Utils.getUriFromString(temp.getAvatar().getUrl());
            } else {
                uri = Uri.parse("android.resource://ee.app.conversa/" + R.drawable.business_default);
            }

            ((ViewHolder) holder).sdvCategoryImage.setImageURI(uri);
        } else if (object.getClass().equals(dbBusiness.class)) {
            dbBusiness business = (dbBusiness) object;
            ((ViewHolder) holder).tvBusiness.setText(business.getDisplayName());
            ((ViewHolder) holder).tvConversaId.setText(business.getConversaId());
            Uri uri = Utils.getUriFromString(business.getAvatarThumbFileId());
            ((ViewHolder) holder).sdvCategoryImage.setImageURI(uri);
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

    public class ViewHolder extends GenericViewHolder implements View.OnClickListener {

        public TextView tvBusiness;
        public TextView tvConversaId;
        public SimpleDraweeView sdvCategoryImage;

        public ViewHolder(View itemView, WeakReference<AppCompatActivity> activity) {
            super(itemView, activity);

            this.tvBusiness = (TextView) itemView.findViewById(R.id.mtvDisplayName);
            this.tvConversaId = (TextView) itemView.findViewById(R.id.ltvConversaId);
            this.sdvCategoryImage = (SimpleDraweeView) itemView.findViewById(R.id.sdvBusinessImage);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Object object = mBusiness.get(getAdapterPosition());

            if (object.getClass().equals(Business.class)) {
                if (listener != null) {
                    listener.onItemClick(itemView, getLayoutPosition(), (Business)object);
                }
            } else if (object.getClass().equals(dbBusiness.class)) {
                if (localListener != null) {
                    localListener.onItemClick(itemView, getLayoutPosition(), (dbBusiness)object);
                }
            }
        }
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

    public class GenericViewHolder extends RecyclerView.ViewHolder {

        protected final WeakReference<AppCompatActivity> activity;

        public GenericViewHolder(View itemView, WeakReference<AppCompatActivity> activity) {
            super(itemView);
            this.activity = activity;
        }

    }

}

