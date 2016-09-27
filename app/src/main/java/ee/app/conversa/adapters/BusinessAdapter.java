package ee.app.conversa.adapters;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

import ee.app.conversa.R;
import ee.app.conversa.model.database.dbBusiness;
import ee.app.conversa.model.parse.Business;

public class BusinessAdapter extends RecyclerView.Adapter<BusinessAdapter.ViewHolder> {

    private final AppCompatActivity mActivity;
    private List<Object> mBusiness;
    private OnItemClickListener listener;
    private OnLocalItemClickListener localListener;

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
    public int getItemCount() {
        return mBusiness.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.business_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int i) {
        Object object = mBusiness.get(i);

        if (object.getClass().equals(Business.class)) {
            Business temp = (Business) object;
            holder.tvBusiness.setText(temp.getDisplayName());
            holder.tvAbout.setText(temp.getConversaID());
            Uri uri;
            if(temp.getAvatar() != null && !temp.getAvatar().getUrl().isEmpty()) {
                uri = Uri.parse(temp.getAvatar().getUrl());
            } else {
                uri = Uri.parse("android.resource://ee.app.conversa/" + R.drawable.business_default);
            }

            holder.sdvCategoryImage.setImageURI(uri);
        } else if (object.getClass().equals(dbBusiness.class)) {
            dbBusiness business = (dbBusiness) object;
            holder.tvBusiness.setText(business.getDisplayName());
            holder.tvAbout.setText(business.getConversaId());
            Uri uri;
            if(business.getAvatarThumbFileId().isEmpty()) {
                uri = Uri.parse(business.getAvatarThumbFileId());
            } else {
                uri = Uri.parse("android.resource://ee.app.conversa/" + R.drawable.business_default);
            }
            holder.sdvCategoryImage.setImageURI(uri);
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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView tvBusiness;
        public TextView tvAbout;
        public SimpleDraweeView sdvCategoryImage;

        public ViewHolder(View itemView) {
            super(itemView);

            this.tvBusiness = (TextView) itemView.findViewById(R.id.tvConversaId);
            this.tvAbout = (TextView) itemView.findViewById(R.id.tvDisplayName);
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
}

