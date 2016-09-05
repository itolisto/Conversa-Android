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
import ee.app.conversa.model.database.dBusiness;
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
        void onItemClick(View itemView, int position, dBusiness business);
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
            holder.tvBusiness.setText(temp.getConversaID());
            holder.tvAbout.setText(temp.getAbout());
            try {
                if(temp.getAvatar() != null && !temp.getAvatar().getUrl().isEmpty()) {
                    Uri uri = Uri.parse(temp.getAvatar().getUrl());
                    holder.sdvCategoryImage.setImageURI(uri);
                } else {
                    Uri path = Uri.parse("android.resource://ee.app.conversa/" + R.drawable.business_default);
                    holder.sdvCategoryImage.setImageURI(path);
                }
            } catch (IllegalStateException e) {
                Uri path = Uri.parse("android.resource://ee.app.conversa/" + R.drawable.business_default);
                holder.sdvCategoryImage.setImageURI(path);
            }
        } else if (object.getClass().equals(dBusiness.class)) {
            dBusiness business = (dBusiness) object;
            holder.tvBusiness.setText(business.getDisplayName());
            holder.tvAbout.setText(business.getConversaId());
            if(business.getAvatarThumbFileId().isEmpty()) {
                Uri path = Uri.parse("android.resource://ee.app.conversa/" + R.drawable.business_default);
                holder.sdvCategoryImage.setImageURI(path);
            } else {
                Uri uri = Uri.parse(business.getAvatarThumbFileId());
                holder.sdvCategoryImage.setImageURI(uri);
            }
        }
    }

    public void addItems(List<Business> business, boolean addLoadMoreCell) {
        mBusiness.addAll(business);
        this.notifyItemRangeInserted(mBusiness.size(), business.size());
    }

    public void addLocalItems(List<dBusiness> business, boolean addLoadMoreCell) {
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
            } else if (object.getClass().equals(dBusiness.class)) {
                if (localListener != null) {
                    localListener.onItemClick(itemView, getLayoutPosition(), (dBusiness)object);
                }
            }
        }
    }
}

