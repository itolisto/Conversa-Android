package ee.app.conversa.adapters;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ee.app.conversa.ActivityChatWall;
import ee.app.conversa.ConversaApp;
import ee.app.conversa.R;
import ee.app.conversa.model.Database.Message;
import ee.app.conversa.model.Database.dBusiness;
import ee.app.conversa.model.Parse.Account;
import ee.app.conversa.utils.Const;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {

    private AppCompatActivity mActivity;
    private List<dBusiness> mUsers;

    public ChatsAdapter(AppCompatActivity activity) {
        this.mUsers = new ArrayList<>();
        this.mActivity = activity;
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        dBusiness user = mUsers.get(position);

        if (ConversaApp.getDB().hasUnreadMessagesOrNewMessages(user.getBusinessId())) {
            holder.ivUnread.setVisibility(View.VISIBLE);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.ivUnread.setBackground(mActivity.getResources().getDrawable(R.drawable.notification, null));
            } else {
                holder.ivUnread.setBackground(mActivity.getResources().getDrawable(R.drawable.notification));
            }
        } else {
            holder.ivUnread.setVisibility(View.GONE);
        }

        holder.tvUser.setText(user.getDisplayName());

        Message lastMessage = ConversaApp.getDB().getLastMessage(user.getBusinessId());

        if(lastMessage == null) {
            holder.tvLastMessage.setText("");
        } else {
            String from;
            if(lastMessage.getFromUserId().equals(Account.getCurrentUser().getObjectId())) {
                from = mActivity.getString(R.string.me);
            } else {
                from = user.getDisplayName();
            }

            switch(lastMessage.getMessageType()) {
                case Const.kMessageTypeImage:
                    holder.tvLastMessage.setText(mActivity.getString(R.string.contacts_last_message_image, from));
                    break;
                case Const.kMessageTypeLocation:
                    holder.tvLastMessage.setText(mActivity.getString(R.string.contacts_last_message_location, from));
                    break;
                case Const.kMessageTypeText:
                    holder.tvLastMessage.setText(mActivity.getString(R.string.contacts_last_message_text, from, lastMessage.getBody()));
                    break;
                default:
                    holder.tvLastMessage.setText(mActivity.getString(R.string.contacts_last_message_default, from));
                    break;
            }
        }
    }

    public void addItems(List<dBusiness> users) {
        mUsers = users;
        notifyItemRangeInserted(0, users.size());
    }

    public void newContactInserted(dBusiness user) {
        mUsers.add(0, user);
        notifyItemInserted(0);
    }

    public void changeContactPosition(int oldposition, int newposition) {
        dBusiness customer = mUsers.get(oldposition);
        mUsers.remove(oldposition);
        mUsers.add(newposition, customer);
        notifyItemMoved(oldposition, newposition);
    }

    public void removeContact(int position) {
        mUsers.remove(position);
        notifyItemRemoved(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public RelativeLayout rlUserLayout;
        public ImageView ivUserImage;
        public TextView tvUser;
        public ProgressBar pbLoading;
        public TextView tvLastMessage;
        public ImageView ivUnread;

        public ViewHolder(View itemView) {
            super(itemView);
            this.rlUserLayout = (RelativeLayout) itemView
                    .findViewById(R.id.rlUserLayout);
            this.ivUserImage = (ImageView) itemView
                    .findViewById(R.id.ivUserImage);
            this.tvUser = (TextView) itemView
                    .findViewById(R.id.tvUser);
            this.tvLastMessage = (TextView) itemView
                    .findViewById(R.id.tvLastMessage);
            this.pbLoading = (ProgressBar) itemView
                    .findViewById(R.id.pbLoadingForImage);
            this.ivUnread = (ImageView) itemView
                    .findViewById(R.id.ivUnread);

            this.tvUser.setTypeface(ConversaApp.getTfRalewayMedium());
            this.tvLastMessage.setTypeface(ConversaApp.getTfRalewayRegular());

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            dBusiness user = mUsers.get(getAdapterPosition());
            Intent intent = new Intent(mActivity, ActivityChatWall.class);
            intent.putExtra(Const.kClassBusiness, user);
            intent.putExtra(Const.kYapDatabaseName, false);
            mActivity.startActivity(intent);
        }

        @Override
        public boolean onLongClick(View v) {
            dBusiness user = mUsers.get(getAdapterPosition());
            //new DeleteUserDialog(adapter, mActivity, user.getId(), getPosition() ).show();
            return true;
        }
    }

}

