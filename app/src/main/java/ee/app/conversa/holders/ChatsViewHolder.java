package ee.app.conversa.holders;

import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ImageView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.Calendar;
import java.util.Locale;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.R;
import ee.app.conversa.interfaces.OnContactClickListener;
import ee.app.conversa.interfaces.OnContactLongClickListener;
import ee.app.conversa.model.database.dbBusiness;
import ee.app.conversa.model.database.dbMessage;
import ee.app.conversa.model.nChatItem;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Utils;
import ee.app.conversa.view.MediumTextView;
import ee.app.conversa.view.RegularTextView;

/**
 * Created by edgargomez on 10/31/16.
 */

public class ChatsViewHolder extends BaseHolder {

    private SparseBooleanArray mSelectedPositions;
    private SimpleDraweeView ivUserImage;
    private MediumTextView tvUser;
    private RegularTextView tvDate;
    private RegularTextView tvLastMessage;
    private ImageView ivUnread;
    private dbBusiness user;

    private OnContactClickListener listener;
    private OnContactLongClickListener longlistener;

    public ChatsViewHolder(View itemView, AppCompatActivity activity, OnContactClickListener listener,
                    OnContactLongClickListener longlistener) {
        super(itemView, activity);

        this.ivUserImage = (SimpleDraweeView) itemView
                .findViewById(R.id.sdvContactAvatar);
        this.tvUser = (MediumTextView) itemView
                .findViewById(R.id.mtvUser);
        this.tvDate = (RegularTextView) itemView
                .findViewById(R.id.rtvDate);
        this.tvLastMessage = (RegularTextView) itemView
                .findViewById(R.id.rtvLastMessage);
        this.ivUnread = (ImageView) itemView
                .findViewById(R.id.ivUnread);

        this.listener = listener;
        this.longlistener = longlistener;

        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            listener.onContactClick(user, v, getAdapterPosition());
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (longlistener != null) {
            longlistener.onContactLongClick(user, v, getAdapterPosition());
        }
        return true;
    }

    public void setContact(dbBusiness user, int position, SparseBooleanArray mSelectedPositions) {
        this.user = user;
        this.mSelectedPositions = mSelectedPositions;

        this.tvUser.setText(user.getDisplayName());

        Uri uri = Utils.getUriFromString(user.getAvatarThumbFileId());

        if (uri == null) {
            uri = Utils.getDefaultImage(activity, R.drawable.ic_business_default);
        }

        this.ivUserImage.setImageURI(uri);

        updateLastMessage(user);

        if (mSelectedPositions.get(position, false)) {
            this.itemView.setActivated(true);
        } else {
            this.itemView.setActivated(false);
        }
    }

    public void toggleActivate() {
        if (this.itemView.isActivated()) {
            this.itemView.setActivated(false);
        } else {
            this.itemView.setActivated(true);
        }
    }

    public void updateView() {
        this.ivUnread.setVisibility(View.GONE);
    }

    public void updateLastMessage(dbBusiness user) {
        if (activity == null) {
            return;
        }

        nChatItem info = ConversaApp.getInstance(activity).getDB()
                .getLastMessageAndUnredCount(user.getBusinessId());
        dbMessage lastMessage = info.getMessage();

        if(lastMessage == null) {
            this.tvLastMessage.setText("");
            this.tvDate.setVisibility(View.GONE);
        } else {
            this.tvDate.setVisibility(View.VISIBLE);
            this.tvDate.setText(setDate(activity, lastMessage.getCreated()));

            switch(lastMessage.getMessageType()) {
                case Const.kMessageTypeImage:
                    this.tvLastMessage.setText(activity
                            .getString(R.string.contacts_last_message_image));
                    break;
                case Const.kMessageTypeLocation:
                    this.tvLastMessage.setText(activity
                            .getString(R.string.contacts_last_message_location));
                    break;
                case Const.kMessageTypeAudio:
                    this.tvLastMessage.setText(activity
                            .getString(R.string.contacts_last_message_audio));
                    break;
                case Const.kMessageTypeVideo:
                    this.tvLastMessage.setText(activity
                            .getString(R.string.contacts_last_message_video));
                    break;
                case Const.kMessageTypeText:
                    this.tvLastMessage.setText(lastMessage.getBody().replaceAll("\\n", " "));
                    break;
                default:
                    this.tvLastMessage.setText(activity
                            .getString(R.string.contacts_last_message_default));
                    break;
            }
        }

        if (info.hasUnreadMessages()) {
            this.ivUnread.setVisibility(View.VISIBLE);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                this.ivUnread.setBackground(activity.getResources()
                        .getDrawable(R.drawable.ic_unread_message, null));
            } else {
                this.ivUnread.setBackground(activity.getResources()
                        .getDrawable(R.drawable.ic_unread_message));
            }
        } else {
            this.ivUnread.setVisibility(View.INVISIBLE);
        }
    }

    private String setDate(AppCompatActivity activity, long timeOfCreation) {
        if (activity == null) {
            return "";
        }

        long now = System.currentTimeMillis();

        // Compute start of the day for the timestamp
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(now);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        if (timeOfCreation > cal.getTimeInMillis()) {
            return Utils.getTimeOrDay(activity, timeOfCreation, false);
        } else {
            long diff = now - timeOfCreation;
            long diffd = diff / (1000 * 60 * 60 * 24);

            if (diffd >= 7) {
                return Utils.getDate(activity, timeOfCreation, true);
            } else if (diffd > 0 && diffd < 7){
                return Utils.getTimeOrDay(activity, timeOfCreation, true);
            } else {
                return activity.getString(R.string.chat_day_yesterday);
            }
        }
    }

}