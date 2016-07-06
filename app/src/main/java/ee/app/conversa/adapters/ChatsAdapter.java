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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import ee.app.conversa.ActivityChatWall;
import ee.app.conversa.ConversaApp;
import ee.app.conversa.R;
import ee.app.conversa.model.Database.dBusiness;
import ee.app.conversa.model.Database.Message;
import ee.app.conversa.utils.Const;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {

    private final WeakReference<AppCompatActivity> mActivity;
	private List<dBusiness> mUsers;

	public ChatsAdapter(AppCompatActivity activity) {
        this.mUsers = new ArrayList<>();
        this.mActivity = new WeakReference<>(activity);
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
        AppCompatActivity activity = mActivity.get();
        final int sdk = Build.VERSION.SDK_INT;

        if (ConversaApp.getDB().hasUnreadMessagesOrNewMessages(user.getBusinessId())) {
            holder.ivUnread.setVisibility(View.VISIBLE);
            if (activity != null) {
                if(sdk >= 21) {
                    holder.ivUnread.setBackground(activity.getResources().getDrawable(R.drawable.notification, null));
                } else {
                    holder.ivUnread.setBackground(activity.getResources().getDrawable(R.drawable.notification));
                }
            }
        } else {
            holder.ivUnread.setVisibility(View.GONE);
        }

        holder.tvUser.setText(user.getDisplayName());

        Message lastMessage = ConversaApp.getDB().getLastMessage(user.getBusinessId());

        if(lastMessage == null) {
            holder.tvLastMessage.setText("");
        } else {
//            switch(lastMessage.getMessageType()) {
//                case Const.kMessageTypeImage:
//                    if(lastMessage.getFromUserId().equals(String.valueOf(Const.B_TYPE))) {
//                        holder.tvLastMessage.setText(mActivity.getString(R.string.you) + ": "
//                                + mActivity.getString(R.string.ca_picture));
//                    } else {
//                        holder.tvLastMessage.setText(user.getDisplayName() + ": "
//                                + mActivity.getString(R.string.ca_picture));
//                    }
//                    break;
//                case Const.kMessageTypeLocation:
//                    if(lastMessage.getFromUserId().equals(String.valueOf(Const.B_TYPE))) {
//                        holder.tvLastMessage.setText(mActivity.getString(R.string.you) + ": "
//                                + mActivity.getString(R.string.ca_location));
//                    } else {
//                        holder.tvLastMessage.setText(user.getDisplayName() + ": "
//                                + mActivity.getString(R.string.ca_location));
//                    }
//                    break;
//                case Const.kMessageTypeText:
//                    if(lastMessage.getFromUserId().equals(String.valueOf(Const.B_TYPE))) {
//                        if(lastMessage.getBody().length() > 35) {
//                            holder.tvLastMessage.setText(mActivity.getString(R.string.you) + ": "
//                                    + lastMessage.getBody().substring(0,35));
//                        } else {
//                            holder.tvLastMessage.setText(mActivity.getString(R.string.you) + ": "
//                                    + lastMessage.getBody());
//                        }
//                    } else {
//                        if(lastMessage.getBody().length() > 35) {
//                            holder.tvLastMessage.setText(user.getDisplayName() + ": " + lastMessage.getBody().substring(0,35));
//                        } else {
//                            holder.tvLastMessage.setText(user.getDisplayName() + ": " + lastMessage.getBody());
//                        }
//                    }
//                    break;
//                default:
//                    if(lastMessage.getFromUserId().equals(String.valueOf(Const.B_TYPE))) {
//                        holder.tvLastMessage.setText(mActivity.getString(R.string.you) + ": "
//                                + mActivity.getString(R.string.ca_message));
//                    } else {
//                        holder.tvLastMessage.setText(user.getDisplayName() + ": "
//                                + mActivity.getString(R.string.ca_message));
//                    }
//                    break;
//            }
        }
    }

    public void addItem(int position, dBusiness user) {
        mUsers.add(position, user);
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        mUsers.remove(position);
        notifyItemRemoved(position);
    }

    public void setItems(List<dBusiness> users) {
        mUsers = users;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
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
            AppCompatActivity activity = mActivity.get();
            if (activity != null) {
                dBusiness user = mUsers.get(getAdapterPosition());
                Intent intent = new Intent(activity, ActivityChatWall.class);
                intent.putExtra(Const.kClassBusiness, user);
                intent.putExtra(Const.kYapDatabaseName, false);
                activity.startActivity(intent);
                //UsersManagement.setToUser(user);
                //SettingsManager.ResetSettings();
                //if(ActivityChatWall.gCurrentMessages != null) {
                    //ActivityChatWall.gCurrentMessages.clear();
                //}
            }
        }

        @Override
        public boolean onLongClick(View v) {
            final dBusiness user = mUsers.get(getAdapterPosition());
            //new DeleteUserDialog(adapter, mActivity, user.getId(), getPosition() ).show();
            return true;
        }
    }

}

