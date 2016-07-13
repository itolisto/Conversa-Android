package ee.app.conversa.adapters;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

import ee.app.conversa.R;
import ee.app.conversa.model.Database.dBusiness;
import ee.app.conversa.model.Parse.Account;
import ee.app.conversa.model.Parse.Business;

public class BusinessAdapter extends RecyclerView.Adapter<BusinessAdapter.ViewHolder> {

    private final AppCompatActivity mActivity;
    private List<Business> mBusiness;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position, Business business);
    }

    public BusinessAdapter(AppCompatActivity activity, OnItemClickListener listener) {
        this.mActivity = activity;
        this.mBusiness = new ArrayList<>();
        this.listener = listener;
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
                Uri uri = Uri.parse(((Account) temp.getBusinessInfo()).getAvatar().getUrl());
                holder.sdvCategoryImage.setImageURI(uri);
            } catch (NullPointerException e) {
                Log.e(this.getClass().getSimpleName(), "Business " + temp.getObjectId() + " has no avatar");
            }
        } else if (object.getClass().equals(dBusiness.class)) {
            dBusiness business = (dBusiness) object;
            holder.tvBusiness.setText(business.getDisplayName());
            holder.tvAbout.setText(business.getBusinessId());
        }
    }

    public void setItems(List<Business> business) {
        mBusiness = business;
        this.notifyDataSetChanged();
    }

    public void addItems(List<Business> business, boolean addLoadMoreCell) {
        mBusiness.addAll(business);
        this.notifyItemRangeInserted(mBusiness.size(), business.size());
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
            if (listener != null)
                listener.onItemClick(itemView, getLayoutPosition(), mBusiness.get(getAdapterPosition()));
        }
    }
}

