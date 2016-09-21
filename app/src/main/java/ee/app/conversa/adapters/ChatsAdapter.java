package ee.app.conversa.adapters;

import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.R;
import ee.app.conversa.model.database.dbBusiness;
import ee.app.conversa.model.database.dbMessage;
import ee.app.conversa.model.parse.Account;
import ee.app.conversa.utils.Const;
import ee.app.conversa.view.MediumTextView;
import ee.app.conversa.view.RegularTextView;


/**
 * ChatsAdapter class was implemented using https://github.com/writtmeyer/recyclerviewdemo
 * along with the post from Wolfram Rittmeyer which you could find at
 * http://www.grokkingandroid.com/first-glance-androids-recyclerview/
 *
 */
public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {

    private AppCompatActivity mActivity;
    private List<dbBusiness> mUsers;
    private OnItemClickListener listener;
    private OnLongClickListener longlistener;
    private SparseBooleanArray mSelectedPositions;

    public interface OnItemClickListener {
        void onItemClick(dbBusiness contact, int position);
    }

    public interface OnLongClickListener {
        void onItemLongClick(dbBusiness contact, int position);
    }

    public ChatsAdapter(AppCompatActivity activity, OnItemClickListener listener, OnLongClickListener longlistener) {
        this.mUsers = new ArrayList<>();
        this.mActivity = activity;
        this.listener = listener;
        this.longlistener = longlistener;
        this.mSelectedPositions = new SparseBooleanArray();
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
        dbBusiness user = mUsers.get(position);

        if (ConversaApp.getInstance(mActivity).getDB().hasUnreadMessagesOrNewMessages(user.getBusinessId())) {
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

        Uri uri;
        if(user.getAvatarThumbFileId().isEmpty()) {
            uri = Uri.parse("android.resource://ee.app.conversa/" + R.drawable.business_default);
        } else {
            uri = Uri.parse(user.getAvatarThumbFileId());
        }

        holder.ivUserImage.setImageURI(uri);

        dbMessage lastMessage = ConversaApp.getInstance(mActivity).getDB().getLastMessage(user.getBusinessId());

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

        if (mSelectedPositions.get(position, false)) {
            holder.itemView.setActivated(true);
        }
    }

    // item changes its selection state
    public void toggleSelection(int position) {
        if (mSelectedPositions.get(position, false)) {
            mSelectedPositions.delete(position);
        } else {
            mSelectedPositions.put(position, true);
        }
        notifyItemChanged(position);
    }

    // clear all selections
    public void clearSelections(boolean updateViews) {
        if (updateViews) {
            for (int i = 0; i < mSelectedPositions.size(); i++) {
                notifyItemChanged(mSelectedPositions.keyAt(i));
            }
        }

        mSelectedPositions.clear();
    }

    // get the number of currently selected items
    public int getSelectedItemCount() {
        return mSelectedPositions.size();
    }

    // Currently selected items.
    public ArrayList<String> getSelectedItems() {
        ArrayList<String> items = new ArrayList<>(mSelectedPositions.size());
        for (int i = 0; i < mSelectedPositions.size(); i++) {
            items.add(Long.toString(mUsers.get(mSelectedPositions.keyAt(i)).getId()));
        }
        return items;
    }

    public void clearItems() {
        mUsers.clear();
        notifyDataSetChanged();
    }

    public void addItems(List<dbBusiness> users) {
        mUsers = users;
        notifyItemRangeInserted(0, users.size());
    }

    public void newContactInserted(dbBusiness user) {
        mUsers.add(0, user);
        notifyItemInserted(0);
    }

    public void changeContactPosition(int oldposition, int newposition) {
        dbBusiness customer = mUsers.get(oldposition);
        mUsers.remove(oldposition);
        mUsers.add(newposition, customer);
        notifyItemMoved(oldposition, newposition);
    }

    public void removeContacts() {
        for (int i = 0; i < mSelectedPositions.size(); i++) {
            int position = mSelectedPositions.keyAt(i);
            mUsers.remove(position);
            notifyItemRemoved(position);
        }

        clearSelections(false);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public SimpleDraweeView ivUserImage;
        public MediumTextView tvUser;
        public RegularTextView tvLastMessage;
        public ImageView ivUnread;

        public ViewHolder(View itemView) {
            super(itemView);
            this.ivUserImage = (SimpleDraweeView) itemView
                    .findViewById(R.id.sdvContactAvatar);
            this.tvUser = (MediumTextView) itemView
                    .findViewById(R.id.tvUser);
            this.tvLastMessage = (RegularTextView) itemView
                    .findViewById(R.id.tvLastMessage);
            this.ivUnread = (ImageView) itemView
                    .findViewById(R.id.ivUnread);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (listener != null) {
                int position = getAdapterPosition();
                listener.onItemClick(mUsers.get(position), position);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (longlistener != null) {
                int position = getAdapterPosition();
                longlistener.onItemLongClick(mUsers.get(position), position);
            }
            return true;
        }
    }

}

