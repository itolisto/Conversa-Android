package ee.app.conversa.adapters;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import ee.app.conversa.R;
import ee.app.conversa.events.MessagePressedEvent;
import ee.app.conversa.model.database.dbMessage;
import ee.app.conversa.utils.Const;
import ee.app.conversa.view.LightTextView;
import ee.app.conversa.view.RegularTextView;

/**
 * MessagesAdapter
 * 
 * Adapter class for chat wall messages.
 */
public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.GenericViewHolder> {

	private final int TO_ME_VIEW_TYPE = 1;
	private final int FROM_ME_VIEW_TYPE = 2;
	private final int LOADER_TYPE = 3;
	private final String toUser;
	private final WeakReference<AppCompatActivity> mActivity;
	private List<Object> mMessages;

	public MessagesAdapter(AppCompatActivity activity, String toUser) {
		this.toUser = toUser;
		this.mActivity = new WeakReference<>(activity);
		this.mMessages = new ArrayList<>(20);
	}

	@Override
	public int getItemViewType(int position) {
		Object object = mMessages.get(position);
		if (object instanceof LoadItem) {
			return LOADER_TYPE;
		} else {
			return (((dbMessage)object).getFromUserId().equals(toUser)) ? TO_ME_VIEW_TYPE : FROM_ME_VIEW_TYPE;
		}
	}

	@Override
	public int getItemCount() {
		return mMessages.size();
	}

	@Override
	public GenericViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == TO_ME_VIEW_TYPE) {
			return new IncomingViewHolder(
					LayoutInflater.from(parent.getContext())
							.inflate(R.layout.message_incoming_item, parent, false),
					this.mActivity);
		} else if (viewType == FROM_ME_VIEW_TYPE) {
			return new ViewHolder(
					LayoutInflater.from(parent.getContext())
							.inflate(R.layout.message_item, parent, false),
					this.mActivity);
		} else {
			return new LoaderViewHolder(
					LayoutInflater.from(parent.getContext())
							.inflate(R.layout.loader_item, parent, false),
					this.mActivity);
		}
	}

	@Override
	public void onBindViewHolder(GenericViewHolder holder, int position) {
		if (holder instanceof MessageViewHolder) {
			if (position > 0) {
				((MessageViewHolder)holder).showMessage(
						(dbMessage)mMessages.get(position),
						(dbMessage)mMessages.get(position - 1));
			} else {
				((MessageViewHolder)holder).showMessage(
						(dbMessage)mMessages.get(position),
						null);
			}
		}
	}

	public void setMessages(List<dbMessage> messages) {
		mMessages.clear();
		mMessages.addAll(messages);
		notifyDataSetChanged();
	}

	public void addLoad(boolean show) {
		int position = mMessages.size();
		if (show) {
			mMessages.add(position, new LoadItem());
			notifyItemInserted(position);
		} else {
			mMessages.remove(position - 1);
			notifyItemRemoved(position - 1);
		}
	}

	public void addMessage(dbMessage message) {
		mMessages.add(0, message);
		notifyItemInserted(0);
	}

	public void addMessages(List<dbMessage> messages) {
		int positionStart = mMessages.size();
		addMessages(messages, positionStart);
	}

	public void addMessages(List<dbMessage> messages, int positionStart) {
		if (positionStart == 0) {
			mMessages.addAll(0, messages);
		} else {
			mMessages.addAll(messages);
		}
		notifyItemRangeInserted(positionStart, messages.size());
	}

	public void updateMessage(dbMessage message, int from, int count) {
		int size = mMessages.size();

		for (int i = 0; i < size; i++) {
			dbMessage m = (dbMessage) mMessages.get(i);
			if (m.getId() == message.getId()) {
				m.setDeliveryStatus(message.getDeliveryStatus());
				mMessages.set(i, m);
				if (i >= from && i <= (from + count)) {
					notifyItemChanged(i);
				}
				break;
			}
		}
	}

	private String setDate(dbMessage message, AppCompatActivity activity) {
		if (activity == null) {
			return "";
		}

		String subText;

		long timeOfCreationOrUpdate = message.getCreated();
		if (message.getCreated() < message.getModified()) {
			timeOfCreationOrUpdate = message.getModified();
		}

		long diff = System.currentTimeMillis() - timeOfCreationOrUpdate;
		long diffm = diff / (1000 * 60);
		long diffh = diff / (1000 * 60 * 60);
		long diffd = diff / (1000 * 60 * 60 * 24);
		long diffw  = diff / (1000 * 60 * 60 * 24 * 7);

		if (diffw >= 2) {
			subText = diffw + " " + activity.getString(R.string.weeks_ago);
		} else if (diffw >= 1 && diffw < 2) {
			subText = diffw + " " + activity.getString(R.string.week_ago);
		} else if (diffh >= 48 && diffh < 168) {
			subText = diffd + " " + activity.getString(R.string.days_ago);
		} else if (diffh >= 24 && diffh < 48) {
			subText = diffd + " " + activity.getString(R.string.day_ago);
		} else if (diffh >= 2 && diffh < 24) {
			subText = diffh + " " + activity.getString(R.string.hours_ago);
		} else if (diffm >= 60 && diffm < 120) {
			subText = diffh + " " + activity.getString(R.string.hour_ago);
		} else if (diffm > 1 && diffm < 60) {
			subText = diffm + " " + activity.getString(R.string.minutes_ago);
		} else if (diffm == 1) {
			subText = diffm + " " + activity.getString(R.string.minute_ago);
		} else {
			subText = activity.getString(R.string.posted_less_than_a_minute_ago);
		}

		return subText;
	}

	class ViewHolder extends MessageViewHolder {

		public ViewHolder(View itemView, WeakReference<AppCompatActivity> activity) {
			super(itemView, activity);
		}

	}

	class IncomingViewHolder extends MessageViewHolder {

		public IncomingViewHolder(View itemView, WeakReference<AppCompatActivity> activity) {
			super(itemView, activity);
		}

	}

	class LoaderViewHolder extends GenericViewHolder {

		public LoaderViewHolder(View itemView, WeakReference<AppCompatActivity> activity) {
			super(itemView, activity);
		}

	}

	class LoadItem {

		public LoadItem(){}

	}

	// Taken from http://stackoverflow.com/questions/26245139/how-to-create-recyclerview-with-multiple-view-type
	public class MessageViewHolder extends GenericViewHolder implements OnClickListener, View.OnLongClickListener, OnMapReadyCallback {
		protected final TextView mTvDate;
		protected final RelativeLayout mRlBackground;
		protected final RegularTextView mRtvMessageText;
		protected final RelativeLayout mRlImageContainer;
		protected final MapView mMvMessageMap;
		protected final SimpleDraweeView mSdvMessageImage;
		protected final LightTextView mLtvSubText;
		protected WeakReference<dbMessage> message;

		public MessageViewHolder(View itemView, WeakReference<AppCompatActivity> activity) {
			super(itemView, activity);

			this.mTvDate = (TextView) itemView.findViewById(R.id.tvDate);
			this.mRlBackground = (RelativeLayout) itemView.findViewById(R.id.rlBackground);
			this.mRtvMessageText = (RegularTextView) itemView.findViewById(R.id.rtvMessageText);
			this.mRlImageContainer = (RelativeLayout) itemView.findViewById(R.id.rlImageContainer);
			this.mMvMessageMap = (MapView) itemView.findViewById(R.id.mvMessageMap);
			this.mSdvMessageImage = (SimpleDraweeView) itemView.findViewById(R.id.sdvMessageImage);
			this.mLtvSubText = (LightTextView) itemView.findViewById(R.id.ltvSubText);

			this.mMvMessageMap.setClickable(false);

			this.mRlBackground.setOnClickListener(this);
			this.mRlBackground.setOnLongClickListener(this);
		}

		public void showMessage(dbMessage message, dbMessage previousMessage) {
			this.message = new WeakReference<>(message);

			// 1. Hide date. Will later check date text string and if it should be visible
			this.mTvDate.setVisibility(View.GONE);

			// 2. Hide message subtext and map/image relative layout
			this.mLtvSubText.setVisibility(View.GONE);

			switch (message.getMessageType()) {
				case Const.kMessageTypeText:
					// 3. Show text and hide container
					this.mRlImageContainer.setVisibility(View.GONE);
					this.mRtvMessageText.setVisibility(View.VISIBLE);
					// 4. Set messaget text
					loadMessage();
					break;
				case Const.kMessageTypeLocation:
					// 3. Show image container and hide text
					this.mRtvMessageText.setVisibility(View.GONE);
					this.mRlImageContainer.setVisibility(View.VISIBLE);
					// 3.1 Decide which view contained in image container should be visible
					this.mMvMessageMap.setVisibility(View.VISIBLE);
					this.mSdvMessageImage.setVisibility(View.GONE);
					// 4. Start map view
					loadMap();
					break;
				case Const.kMessageTypeImage:
					// 3. Show image container and hide text
					this.mRtvMessageText.setVisibility(View.GONE);
					this.mRlImageContainer.setVisibility(View.VISIBLE);
					// 3.1 Decide which view contained in image container should be visible
					this.mMvMessageMap.setVisibility(View.GONE);
					this.mSdvMessageImage.setVisibility(View.VISIBLE);
					// 4. Load image
					loadImage();
					break;
				case Const.kMessageTypeVideo:
					break;
				case Const.kMessageTypeAudio:
					break;
			}

			// 4. Decide if date should be visible
			this.mTvDate.setText(setDate(message, this.activity.get()));

			// 5. Decide whether to show message status
			if (message.getDeliveryStatus().equals(dbMessage.statusParseError)) {
				this.mLtvSubText.setVisibility(View.VISIBLE);
				if (this.activity.get() != null) {
					this.mLtvSubText.setText(activity.get().getString(R.string.app_name));
				}
			} else {
				this.mLtvSubText.setVisibility(View.GONE);
			}
		}

		public void loadMessage() {
			if (message.get() != null) {
				this.mRtvMessageText.setText(message.get().getBody());
			}
		}

		public void loadMap() {
			// 1. Create map
			this.mMvMessageMap.onCreate(null);
			this.mMvMessageMap.onResume();
			// 2. Load map
			this.mMvMessageMap.getMapAsync(this);
		}

		public void loadImage() {
			if (activity.get() != null && message.get() != null) {
				final float density = activity.get().getResources().getDisplayMetrics().density;
				// 1. Resize image to display
				final int width = (message.get().getWidth() < 1) ? 210 : message.get().getWidth();
				final int height = (message.get().getHeight() < 1) ? 100 : message.get().getHeight();
				// 2.1 Convert the DP into pixel
				ViewGroup.LayoutParams params = this.mSdvMessageImage.getLayoutParams();
				params.height = (int) (height / density);
				params.width = (int) (width / density);
				// 2.2 Set image
				this.mSdvMessageImage.setLayoutParams(params);
				if (message.get().getFileId() != null) {
					this.mSdvMessageImage.setImageURI(Uri.fromFile(new File(message.get().getFileId())));
				}
				this.mSdvMessageImage.refreshDrawableState();
			}
		}

		@Override
		public void onClick(View view) {
			if (message.get() != null) {
				if (!message.get().getMessageType().equals(Const.kMessageTypeText)) {
					EventBus.getDefault().post(new MessagePressedEvent(message.get()));
				}
			}
		}

		@Override
		public boolean onLongClick(View v) {
			return false;
		}

		@Override
		public void onMapReady(GoogleMap googleMap) {
			if (activity.get() != null && message.get() != null) {
				double lat = message.get().getLatitude();
				double lon = message.get().getLongitude();
				MapsInitializer.initialize(activity.get().getApplicationContext());
				googleMap.getUiSettings().setMapToolbarEnabled(false);
				googleMap.getUiSettings().setAllGesturesEnabled(false);
				googleMap.getUiSettings().setMyLocationButtonEnabled(false);
				LatLng sydney = new LatLng(lat, lon);
				googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
				googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
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