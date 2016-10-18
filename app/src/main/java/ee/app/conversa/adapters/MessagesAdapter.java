package ee.app.conversa.adapters;

import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import ee.app.conversa.R;
import ee.app.conversa.delivery.DeliveryStatus;
import ee.app.conversa.model.database.dbMessage;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Logger;
import ee.app.conversa.utils.Utils;
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
	private final OnItemClickListener listener;
	private final WeakReference<AppCompatActivity> mActivity;

	private String toUser;
	private List<Object> mMessages;

	public interface OnItemClickListener {
		void onItemClick(dbMessage message, View view, int position);
	}

	public MessagesAdapter(AppCompatActivity activity, String toUser, OnItemClickListener listener) {
		this.toUser = toUser;
		this.mActivity = new WeakReference<>(activity);
		this.mMessages = new ArrayList<>(20);
		this.listener = listener;
	}

	@Override
	public int getItemViewType(int position) {
		Object object = mMessages.get(position);
		if (object instanceof dbMessage) {
			return (((dbMessage)object).getFromUserId().equals(toUser)) ? TO_ME_VIEW_TYPE : FROM_ME_VIEW_TYPE;
		} else {
			return LOADER_TYPE;
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
	public void onBindViewHolder(GenericViewHolder holder, int position, List<Object> payloads) {
		if (payloads.isEmpty()) {
			super.onBindViewHolder(holder, position, payloads);
		} else {
			if (holder instanceof MessageViewHolder) {
				if (payloads.size() > 0) {
					if (payloads.get(0) instanceof String) {
						switch ((String)payloads.get(0)) {
							case "updateTime": {
								((MessageViewHolder)holder).updateLastMessage((dbMessage)mMessages.get(position));
								break;
							}
							case "updateStatus": {
								((MessageViewHolder)holder).updateDeliveryStatus();
								break;
							}
							case "updateImageView": {
								((MessageViewHolder)holder).updateImageView();
								break;
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void onBindViewHolder(GenericViewHolder holder, int position) {
		if (holder instanceof MessageViewHolder) {
			if (position + 1 < mMessages.size()) {
				if (mMessages.get(position + 1) instanceof dbMessage) {
					((MessageViewHolder) holder).showMessage(
							(dbMessage) mMessages.get(position),
							(dbMessage) mMessages.get(position + 1));
				} else {
					((MessageViewHolder)holder).showMessage(
							(dbMessage)mMessages.get(position),
							null);
				}
			} else {
				((MessageViewHolder)holder).showMessage(
						(dbMessage)mMessages.get(position),
						null);
			}
		}
	}

	public void updateFromId(String toUser) {
		this.toUser = toUser;
	}

	public void setMessages(List<dbMessage> messages) {
		mMessages.clear();
		mMessages.addAll(messages);
		notifyDataSetChanged();
	}

	public void addLoad(boolean show) {
		int position = mMessages.size();
		if (show) {
			mMessages.add(position, new Object());
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

	public void updateImageView(dbMessage message, int from, int count) {
		int size = mMessages.size();

		for (int i = 0; i < size; i++) {
			dbMessage m = (dbMessage) mMessages.get(i);
			if (m.getId() == message.getId()) {
				m.setLocalUrl(message.getLocalUrl());
				if (i >= from && i <= (from + count)) {
					notifyItemChanged(i, "updateImageView");
				}
				break;
			}
		}
	}

	public void updateStatus(dbMessage message, int from, int count) {
		if (message.getDeliveryStatus().equals(DeliveryStatus.statusParseError)) {
			int size = mMessages.size();

			for (int i = 0; i < size; i++) {
				dbMessage m = (dbMessage) mMessages.get(i);
				if (m.getId() == message.getId()) {
					m.setDeliveryStatus(message.getDeliveryStatus());
					if (i >= from && i <= (from + count)) {
						notifyItemChanged(i, "updateStatus");
					}
					break;
				}
			}
		}
	}

	public void updateTime(int position, int count) {
		notifyItemRangeChanged(position, count, "updateTime");
	}

	private String setDate(dbMessage message, AppCompatActivity activity) {
		if (activity == null) {
			return "";
		}

		long timeOfCreation = message.getCreated();
		long diff = System.currentTimeMillis() - timeOfCreation;
		long diffm = diff / (1000 * 60);
		long diffh = diff / (1000 * 60 * 60);
		long diffd = diff / (1000 * 60 * 60 * 24);
		long diffw  = diff / (1000 * 60 * 60 * 24 * 7);

		if (diffw >= 2) {
			return activity.getString(R.string.weeks_ago, diffw);
		} else if (diffw >= 1 && diffw < 2) {
			return activity.getString(R.string.week_ago);
		} else if (diffh >= 48 && diffh < 168) {
			return activity.getString(R.string.days_ago, diffd);
		} else if (diffh >= 24 && diffh < 48) {
			return activity.getString(R.string.day_ago);
		} else if (diffh >= 2 && diffh < 24) {
			return activity.getString(R.string.hours_ago, diffh);
		} else if (diffm >= 60 && diffm < 120) {
			return activity.getString(R.string.hour_ago);
		} else if (diffm > 1 && diffm < 60) {
			return activity.getString(R.string.minutes_ago, diffm);
		} else if (diffm == 1) {
			return activity.getString(R.string.minute_ago);
		} else {
			return activity.getString(R.string.posted_less_than_a_minute_ago);
		}
	}

	private class ViewHolder extends MessageViewHolder {

		ViewHolder(View itemView, WeakReference<AppCompatActivity> activity) {
			super(itemView, activity);
		}

	}

	private class IncomingViewHolder extends MessageViewHolder {

		IncomingViewHolder(View itemView, WeakReference<AppCompatActivity> activity) {
			super(itemView, activity);
		}

	}

	private class LoaderViewHolder extends GenericViewHolder {

		LoaderViewHolder(View itemView, WeakReference<AppCompatActivity> activity) {
			super(itemView, activity);
		}

	}

	// Taken from http://stackoverflow.com/questions/26245139/how-to-create-recyclerview-with-multiple-view-type
	class MessageViewHolder extends GenericViewHolder implements OnClickListener, View.OnLongClickListener, OnMapReadyCallback {
		private final LightTextView mTvDate;
		private final RelativeLayout mRlBackground;
		private final RegularTextView mRtvMessageText;
		private final RelativeLayout mRlImageContainer;
		private final MapView mMvMessageMap;
		private final SimpleDraweeView mSdvMessageImage;
		private final LightTextView mLtvSubText;
		protected WeakReference<dbMessage> message;

		MessageViewHolder(View itemView, WeakReference<AppCompatActivity> activity) {
			super(itemView, activity);

			this.mTvDate = (LightTextView) itemView.findViewById(R.id.tvDate);
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

		void showMessage(dbMessage message, dbMessage previousMessage) {
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
			if (previousMessage == null) {
				this.mTvDate.setText(setDate(message, this.activity.get()));
				this.mTvDate.setVisibility(View.VISIBLE);
			} else if ((previousMessage.getCreated() + (15 * 60 * 1000)) <= message.getCreated()) {
				Logger.error("message", "previous" + (previousMessage.getCreated() + (15 * 60 * 1000)) +"\ncurrent" + message.getCreated());
				this.mTvDate.setText(setDate(message, this.activity.get()));
				this.mTvDate.setVisibility(View.VISIBLE);
			}

			// 5. Decide whether to show message status
			if (message.getDeliveryStatus().equals(DeliveryStatus.statusParseError)) {
				this.mLtvSubText.setVisibility(View.VISIBLE);
				if (this.activity.get() != null) {
					this.mLtvSubText.setText(activity.get().getString(R.string.message_sent_error));
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
						this.mLtvSubText.setTextColor(activity.get().getResources()
								.getColor(R.color.default_red, null));
					} else {
						this.mLtvSubText.setTextColor(activity.get().getResources()
								.getColor(R.color.default_red));
					}
				}
			}
		}

		public void updateLastMessage(dbMessage message) {
			if (mTvDate.getVisibility() == View.VISIBLE) {
				this.mTvDate.setText(setDate(message, this.activity.get()));
			}
		}

		public void updateDeliveryStatus() {
			if (this.activity.get() != null) {
				this.mLtvSubText.setVisibility(View.VISIBLE);
				this.mLtvSubText.setText(activity.get().getString(R.string.message_sent_error));
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					this.mLtvSubText.setTextColor(activity.get().getResources()
							.getColor(R.color.default_red, null));
				} else {
					this.mLtvSubText.setTextColor(activity.get().getResources()
							.getColor(R.color.default_red));
				}
			}
		}

		public void updateImageView() {
			loadImage();
		}

		void loadMessage() {
			if (message.get() != null) {
				this.mRtvMessageText.setText(message.get().getBody());
			}
		}

		void loadMap() {
			// 1. Create map
			this.mMvMessageMap.onCreate(null);
			this.mMvMessageMap.onResume();
			// 2. Load map
			this.mMvMessageMap.getMapAsync(this);
		}

		void loadImage() {
			if (activity.get() != null && message.get() != null) {
				final float density = activity.get().getResources().getDisplayMetrics().density;
				// 1. Resize image to display
				final int width = (message.get().getWidth() < 1)
						? Utils.dpToPixels(mActivity.get(), 210) : (int) (message.get().getWidth() / density);
				final int height = (message.get().getHeight() < 1)
						? Utils.dpToPixels(mActivity.get(), 100) : (int) (message.get().getHeight() / density);
				// 2.1 Convert the DP into pixel
				ViewGroup.LayoutParams params = this.mSdvMessageImage.getLayoutParams();
				params.height = height;
				params.width = width;
				// 2.2 Set image
				this.mSdvMessageImage.setLayoutParams(params);
				if (message.get().getLocalUrl() != null) {
					this.mSdvMessageImage.setImageURI(Uri.fromFile(new File(message.get().getLocalUrl())));
				}
				this.mSdvMessageImage.refreshDrawableState();
			}
		}

		@Override
		public void onClick(View view) {
			if (message.get() != null && !message.get().getMessageType().equals(Const.kMessageTypeText)) {
				if (listener != null) {
					listener.onItemClick(message.get(), view, getAdapterPosition());
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

	class GenericViewHolder extends RecyclerView.ViewHolder {

		protected final WeakReference<AppCompatActivity> activity;

		GenericViewHolder(View itemView, WeakReference<AppCompatActivity> activity) {
			super(itemView);
			this.activity = activity;
		}

	}

}