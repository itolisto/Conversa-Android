package ee.app.conversa.adapters;

import android.content.Context;
import android.content.Intent;
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
import ee.app.conversa.management.SettingsManager;
import ee.app.conversa.model.Database.Business;
import ee.app.conversa.model.Database.Message;
import ee.app.conversa.sendbird.SendBirdController;
import ee.app.conversa.utils.Const;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> implements SendBirdController.ChatControllerListener{

	private List<Business> mUsers = new ArrayList<>();
	private AppCompatActivity mActivity;

	public ChatsAdapter(AppCompatActivity activity, List<Business> users) {
		mUsers = users;
		mActivity = activity;
	}

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemCount() {
        return (mUsers == null) ? 0 : mUsers.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int i) {
        Business user = mUsers.get(i);

        if( ConversaApp.getDB().hasUnreadMessagesOrNewMessages(user.getObjectId()) ){
            holder.ivUnread.setBackground(mActivity.getResources().getDrawable(R.drawable.notification));
        } else {
            holder.ivUnread.setBackground(null);
        }

//        Utils.displayImage(user.getAvatarThumbFileId(), Const.BUSINESS_FOLDER, holder.ivUserImage,
//                holder.pbLoading, ImageLoader.SMALL, R.drawable.business_default, false);

//        Picasso.with(holder.mPostImageView.getContext())
//                .load(post.getPostImageUrl())
////                    .placeholder(R.drawable.ic_facebook)
//                .centerCrop()
//                .resize(QuickReturnUtils.dp2px(mContext, 346), //QuickReturnUtils es una clase de la app QuickReturn
//                        QuickReturnUtils.dp2px(mContext, 320))
//                .error(android.R.drawable.stat_notify_error)
//                .into(holder.mPostImageView);

        holder.tvUser.setText(user.getDisplayName());

        Message lastMessage = ConversaApp.getDB().getLastMessage(user.getObjectId());

        if(lastMessage == null) {
            holder.tvLastMessage.setText("");
        } else {
            switch(lastMessage.getMessageType()) {
                case 2:
                    if(lastMessage.getType().equals(String.valueOf(Const.C_TYPE))) {
                        holder.tvLastMessage.setText(mActivity.getString(R.string.you) + ": "
                                + mActivity.getString(R.string.ca_picture));
                    } else {
                        holder.tvLastMessage.setText(user.getDisplayName() + ": "
                                + mActivity.getString(R.string.ca_picture));
                    }
                    break;
                case 3:
                    if(lastMessage.getType().equals(String.valueOf(Const.C_TYPE))) {
                        holder.tvLastMessage.setText(mActivity.getString(R.string.you) + ": "
                                + mActivity.getString(R.string.ca_location));
                    } else {
                        holder.tvLastMessage.setText(user.getDisplayName() + ": "
                                + mActivity.getString(R.string.ca_location));
                    }
                    break;
                case 1:
                    if(lastMessage.getType().equals(String.valueOf(Const.C_TYPE))) {
                        if(lastMessage.getBody().length() > 35) {
                            holder.tvLastMessage.setText(mActivity.getString(R.string.you) + ": "
                                    + lastMessage.getBody().substring(0,35));
                        } else {
                            holder.tvLastMessage.setText(mActivity.getString(R.string.you) + ": "
                                    + lastMessage.getBody());
                        }
                    } else {
                        if(lastMessage.getBody().length() > 35) {
                            holder.tvLastMessage.setText(user.getDisplayName() + ": " + lastMessage.getBody().substring(0,35));
                        } else {
                            holder.tvLastMessage.setText(user.getDisplayName() + ": " + lastMessage.getBody());
                        }
                    }
                    break;
                default:
                    if(lastMessage.getType().equals(String.valueOf(Const.C_TYPE))) {
                        holder.tvLastMessage.setText(mActivity.getString(R.string.you) + ": "
                                + mActivity.getString(R.string.ca_message));
                    } else {
                        holder.tvLastMessage.setText(user.getDisplayName() + ": "
                                + mActivity.getString(R.string.ca_message));
                    }
                    break;
            }
        }
    }

    public void addItem(int position, Business user) {
        mUsers.add(position, user);
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        mUsers.remove(position);
        notifyItemRemoved(position);
    }

    public void setItems(List<Business> users) {
        mUsers = users;
    }

    @Override
    public void onMessageReceived() {

    }

    @Override
    public void onMessageDelivery() {

    }

    @Override
    public void onReadReceived() {

    }

    @Override
    public void onTypeStartReceived() {

    }

    @Override
    public void onTypeEndReceived() {

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

//            LayoutHelper.scaleWidthAndHeightAbsolute(mActivity, 2.5f, this.ivUserImage);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Business user = mUsers.get(getAdapterPosition());
            Context context = mActivity;
            //UsersManagement.setToUser(user);

            SettingsManager.ResetSettings();
            if(ActivityChatWall.gCurrentMessages != null) {
                ActivityChatWall.gCurrentMessages.clear();
            }

            context.startActivity(new Intent(context, ActivityChatWall.class));
        }

        @Override
        public boolean onLongClick(View v) {
            final Business user = mUsers.get(getAdapterPosition());
            //new DeleteUserDialog(adapter, mActivity, user.getId(), getPosition() ).show();
            return true;
        }
    }

}

