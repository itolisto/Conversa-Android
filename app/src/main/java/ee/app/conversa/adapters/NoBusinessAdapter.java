package ee.app.conversa.adapters;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import ee.app.conversa.R;

public class NoBusinessAdapter extends RecyclerView.Adapter<NoBusinessAdapter.ViewHolder>{

    private AppCompatActivity mActivity;
    private NoBusinessAdapter adapter;

    public NoBusinessAdapter(AppCompatActivity activity) {
        mActivity = activity;
        adapter = this;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.empty_business_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int i) {

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView ivStartChat;

        public ViewHolder(View itemView) {
            super(itemView);

            this.ivStartChat = (ImageView) itemView
                    .findViewById(R.id.ivNoBusiness);
        }
    }
}

