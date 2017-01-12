package ee.app.conversa.adapters;

import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ee.app.conversa.R;
import ee.app.conversa.delivery.DeliveryStatus;
import ee.app.conversa.holders.BaseHolder;
import ee.app.conversa.holders.LoaderViewHolder;
import ee.app.conversa.interfaces.OnMessageClickListener;
import ee.app.conversa.model.database.dbMessage;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Utils;
import ee.app.conversa.view.LightTextView;
import ee.app.conversa.view.RegularTextView;

import static ee.app.conversa.utils.Const.kMessageTypeText;

/**
 * MessagesAdapter
 * 
 * Adapter class for chat wall messages.
 */
public class MessagesAdapter extends RecyclerView.Adapter<BaseHolder> {

	private final int TO_ME_VIEW_TYPE = 1;
	private final int FROM_ME_VIEW_TYPE = 2;
	private final int LOADER_TYPE = 3;

	private final OnMessageClickListener listener;
	private final AppCompatActivity mActivity;

	private String fromUser;
	private List<Object> mMessages;

	public MessagesAdapter(AppCompatActivity mActivity, String fromUser, OnMessageClickListener listener) {
		this.fromUser = fromUser;
		this.mActivity = mActivity;
		this.mMessages = new ArrayList<>(20);
		this.listener = listener;
	}

	@Override
	public int getItemViewType(int position) {
		Object object = mMessages.get(position);
		if (object instanceof dbMessage) {
			return (((dbMessage)object).getFromUserId().equals(fromUser)) ? FROM_ME_VIEW_TYPE : TO_ME_VIEW_TYPE;
		} else {
			return LOADER_TYPE;
		}
	}

	@Override
	public int getItemCount() {
		return mMessages.size();
	}

	@Override
	public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == TO_ME_VIEW_TYPE) {
			return new MessageViewHolder(
					LayoutInflater.from(parent.getContext())
							.inflate(R.layout.message_incoming_item, parent, false),
					this.mActivity);
		} else if (viewType == FROM_ME_VIEW_TYPE) {
			return new MessageViewHolder(
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
	public void onBindViewHolder(BaseHolder holder, int position, List<Object> payloads) {
		if (payloads.isEmpty()) {
			super.onBindViewHolder(holder, position, payloads);
		} else {
			if (holder instanceof MessageViewHolder) {
				if (payloads.size() > 0) {
					if (payloads.get(0) instanceof String) {
						switch ((String)payloads.get(0)) {
							case "update": {
								dbMessage current, next = null, previous = null;

								if (position + 1 < mMessages.size() && mMessages.get(position + 1) instanceof dbMessage) {
									next = (dbMessage) mMessages.get(position + 1);
								}

								if (position > 0) {
									previous = (dbMessage) mMessages.get(position - 1);
								}

								current = (dbMessage) mMessages.get(position);

								((MessageViewHolder)holder).updateDate(current, previous, position);
								((MessageViewHolder)holder).updateSubText(current, next);
								break;
							}
							case "updateImageView": {
								((MessageViewHolder)holder).updateImageView();
							}
							case "updateStatus": {
								dbMessage current, next = null;

								if (position + 1 < mMessages.size() && mMessages.get(position + 1) instanceof dbMessage) {
									next = (dbMessage) mMessages.get(position + 1);
								}

								current = (dbMessage) mMessages.get(position);

								((MessageViewHolder)holder).updateSubText(current, next);
								break;
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void onBindViewHolder(BaseHolder holder, int position) {
		if (holder instanceof MessageViewHolder) {
			boolean hasNext = true;
			boolean hasPrevious = true;

			if (position + 1 >= mMessages.size())
				hasNext = false;

			if (position == 0) {
				hasPrevious = false;
			} else {
				if (!(mMessages.get(position - 1) instanceof dbMessage))
					hasPrevious = false;
			}

			((MessageViewHolder) holder).showMessage(
					(dbMessage) mMessages.get(position),
					(hasNext) ? (dbMessage) mMessages.get(position + 1) : null,
					(hasPrevious) ? (dbMessage) mMessages.get(position - 1) : null,
					position);
		}
	}

	public void clearMessages() {
		int size = mMessages.size();
		mMessages.clear();
		notifyItemRangeRemoved(0, size);
	}

	public void setMessages(List<dbMessage> messages) {
		mMessages.addAll(messages);
		notifyDataSetChanged();
	}

	public void addLoad(boolean show) {
		if (show) {
			mMessages.add(0, new Object());
			notifyItemInserted(0);
		} else {
			mMessages.remove(0);
			notifyItemRemoved(0);
		}
	}

	public void addMessage(dbMessage message, int from, int count) {
		int position = mMessages.size();
		mMessages.add(message);
		notifyItemInserted(position);
		if (mMessages.size() > 1) {
			notifyItemRangeChanged(from, count, "update");
		}
	}

	public void addMessages(List<dbMessage> messages) {
		mMessages.addAll(0, messages);
		notifyItemRangeInserted(0, messages.size());
	}

	public void updateImageView(dbMessage message, int from, int count) {
		int size = mMessages.size();

		for (int i = 0; i < size; i++) {
			dbMessage m = (dbMessage) mMessages.get(i);
			if (m.getId() == message.getId()) {
				m.setLocalUrl(message.getLocalUrl());
				m.setDeliveryStatus(message.getDeliveryStatus());
				if (i >= from && i <= (from + count)) {
					notifyItemChanged(i, "updateImageView");
				}
				break;
			}
		}
	}

	public void updateStatus(dbMessage message, int from, int count) {
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

	private Spannable setDate(dbMessage message, AppCompatActivity activity) {
		long now = System.currentTimeMillis();
		long timeOfCreation = message.getCreated();

		// Compute start of the day for the timestamp
		Calendar cal = Calendar.getInstance(Locale.getDefault());
		cal.setTimeInMillis(now);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		String date, hour;

		if (timeOfCreation > now) {
			date = Utils.getDate(activity, timeOfCreation, true);
		} else {
			long diff = now - timeOfCreation;
			long diffd = diff / (1000 * 60 * 60 * 24);

			if (diffd > 7) {
				date = Utils.getDate(activity, timeOfCreation, true);
			} else if (diffd > 1 && diffd <= 7){
				date = Utils.getTimeOrDay(activity, timeOfCreation, true);
			} else if (diffd > 0 && diffd <= 1) {
				date = activity.getString(R.string.chat_day_yesterday);
			} else {
				date = activity.getString(R.string.chat_day_today);
			}
		}

		hour = Utils.getTimeOrDay(activity, timeOfCreation, false);

		Spannable styledString = new SpannableString(date + " " + hour);

		int size = date.length();
		int timeSize = hour.length();

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			styledString.setSpan(new ForegroundColorSpan(activity.getResources().getColor(R.color.black, null)),
					0, size, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
			styledString.setSpan(new ForegroundColorSpan(activity.getResources().getColor(R.color.gray, null)),
					size + 1, size + timeSize + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		} else {
			styledString.setSpan(new ForegroundColorSpan(activity.getResources().getColor(R.color.black)),
					0, size, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
			styledString.setSpan(new ForegroundColorSpan(activity.getResources().getColor(R.color.gray)),
					size + 1, size + timeSize + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		}

		return styledString;
	}

	// Taken from http://stackoverflow.com/questions/26245139/how-to-create-recyclerview-with-multiple-view-type
	private class MessageViewHolder extends BaseHolder implements OnMapReadyCallback {

		private final LightTextView mTvDate;
		private final RelativeLayout mRlBackground;
		private final RegularTextView mRtvMessageText;
		//private final RelativeLayout mRlImageContainer;
		private final MapView mMvMessageMap;
		private final SimpleDraweeView mSdvMessageImage;
		private final SimpleDraweeView mSdvMessageImageLand;
		private final LightTextView mLtvSubText;
		protected dbMessage message;

		MessageViewHolder(View itemView, AppCompatActivity activity) {
			super(itemView, activity);

			this.mTvDate = (LightTextView) itemView.findViewById(R.id.tvDate);
			this.mRlBackground = (RelativeLayout) itemView.findViewById(R.id.rlBackground);
			this.mRtvMessageText = (RegularTextView) itemView.findViewById(R.id.rtvMessageText);
			//this.mRlImageContainer = (RelativeLayout) itemView.findViewById(R.id.rlImageContainer);
			this.mMvMessageMap = (MapView) itemView.findViewById(R.id.mvMessageMap);
			this.mSdvMessageImage = (SimpleDraweeView) itemView.findViewById(R.id.sdvMessageImage);
			this.mSdvMessageImageLand = (SimpleDraweeView) itemView.findViewById(R.id.sdvMessageImageLand);
			this.mLtvSubText = (LightTextView) itemView.findViewById(R.id.ltvSubText);

			this.mMvMessageMap.setClickable(false);

			this.mRlBackground.setOnClickListener(this);
			this.mRlBackground.setOnLongClickListener(this);
		}

		void showMessage(dbMessage message, dbMessage nextMessage, dbMessage previousMessage, int position) {
			this.message = message;

			switch (message.getMessageType()) {
				case kMessageTypeText:
					// 3. Show view, hide other views
					this.mRtvMessageText.setVisibility(View.VISIBLE);
					this.mMvMessageMap.setVisibility(View.GONE);
					this.mSdvMessageImage.setVisibility(View.GONE);
					this.mSdvMessageImageLand.setVisibility(View.GONE);
					// 4. Set messaget text
					loadMessage();
					break;
				case Const.kMessageTypeLocation:
					// 3. Show view, hide other views
					this.mRtvMessageText.setVisibility(View.GONE);
					this.mMvMessageMap.setVisibility(View.VISIBLE);
					this.mSdvMessageImage.setVisibility(View.GONE);
					this.mSdvMessageImageLand.setVisibility(View.GONE);
					// 4. Start map view
					loadMap();
					break;
				case Const.kMessageTypeImage:
					// 3. Show view, hide other views
					this.mRtvMessageText.setVisibility(View.GONE);
					this.mMvMessageMap.setVisibility(View.GONE);
					if (message.getWidth() >= message.getHeight()) {
						this.mSdvMessageImage.setVisibility(View.GONE);
						this.mSdvMessageImageLand.setVisibility(View.VISIBLE);
					} else {
						this.mSdvMessageImage.setVisibility(View.VISIBLE);
						this.mSdvMessageImageLand.setVisibility(View.GONE);
					}
					// 4. Load image
					loadImage();
					break;
				case Const.kMessageTypeVideo:
					break;
				case Const.kMessageTypeAudio:
					break;
			}

			updateDate(message, previousMessage, position);
			updateSubText(message, nextMessage);
		}

		private void updateSubText(dbMessage message, dbMessage nextMessage) {
			// 5. Decide whether to show message status
			if (message.getDeliveryStatus() == DeliveryStatus.statusParseError) {
				this.mLtvSubText.setVisibility(View.VISIBLE);
				this.mLtvSubText.setText(activity.getString(R.string.message_sent_error));
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					this.mLtvSubText.setTextColor(activity.getResources()
							.getColor(R.color.red, null));
				} else {
					this.mLtvSubText.setTextColor(activity.getResources()
							.getColor(R.color.red));
				}
			} else if (message.getDeliveryStatus() == DeliveryStatus.statusUploading) {
				this.mLtvSubText.setVisibility(View.VISIBLE);
				if (message.getMessageType().equals(kMessageTypeText) ||
						message.getMessageType().equals(Const.kMessageTypeLocation)) {
					this.mLtvSubText.setText(activity.getString(R.string.message_sent_sending));
				} else {
					this.mLtvSubText.setText(activity.getString(R.string.message_sent_uploading));
				}
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					this.mLtvSubText.setTextColor(activity.getResources()
							.getColor(R.color.gray, null));
				} else {
					this.mLtvSubText.setTextColor(activity.getResources()
							.getColor(R.color.gray));
				}
			} else if (message.getDeliveryStatus() == DeliveryStatus.statusDownloading) {
				this.mLtvSubText.setVisibility(View.VISIBLE);
				this.mLtvSubText.setText(activity.getString(R.string.message_sent_downloading));
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					this.mLtvSubText.setTextColor(activity.getResources()
							.getColor(R.color.gray, null));
				} else {
					this.mLtvSubText.setTextColor(activity.getResources()
							.getColor(R.color.gray));
				}
			} else if (!message.getFromUserId().equals(fromUser)) {
				this.mLtvSubText.setVisibility(View.GONE);
			} else if (nextMessage != null) {
				if (nextMessage.getFromUserId().equals(fromUser)) {
					this.mLtvSubText.setVisibility(View.GONE);
				} else {
					this.mLtvSubText.setVisibility(View.VISIBLE);
					this.mLtvSubText.setText(activity.getString(R.string.message_sent));
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
						this.mLtvSubText.setTextColor(activity.getResources()
								.getColor(R.color.gray, null));
					} else {
						this.mLtvSubText.setTextColor(activity.getResources()
								.getColor(R.color.gray));
					}
				}
			} else {
				if (message.getFromUserId().equals(fromUser)) {
					this.mLtvSubText.setVisibility(View.VISIBLE);
					this.mLtvSubText.setText(activity.getString(R.string.message_sent));
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
						this.mLtvSubText.setTextColor(activity.getResources()
								.getColor(R.color.gray, null));
					} else {
						this.mLtvSubText.setTextColor(activity.getResources()
								.getColor(R.color.gray));
					}
				} else {
					this.mLtvSubText.setVisibility(View.GONE);
				}
			}
		}

		public void updateDate(dbMessage message, dbMessage previousMessage, int position) {
			if (position == 0) {
				this.mTvDate.setText(setDate(message, activity));
				this.mTvDate.setVisibility(View.VISIBLE);
			} else {
				if (previousMessage != null) {
					long diff = message.getCreated() - previousMessage.getCreated();
					long diffd = diff / (1000 * 60 * 60 * 24);
					long diffm = diff / (1000 * 60);

					if (diffd >= 1 || diffm >= 20 || (position != 1 && position % 20 == 0)) {
						this.mTvDate.setText(setDate(message, activity));
						this.mTvDate.setVisibility(View.VISIBLE);
					} else {
						this.mTvDate.setVisibility(View.GONE);
					}
				} else {
					this.mTvDate.setVisibility(View.GONE);
				}
			}
		}

		public void updateImageView() {
			loadImage();
		}

		void loadMessage() {
			if (message != null) {
				this.mRtvMessageText.setText(message.getBody());
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
			if (message.getLocalUrl() != null) {
				if (message.getWidth() >= message.getHeight())
					this.mSdvMessageImageLand.setImageURI(Uri.fromFile(new File(message.getLocalUrl())));
				else
					this.mSdvMessageImage.setImageURI(Uri.fromFile(new File(message.getLocalUrl())));
			}
		}

		@Override
		public void onClick(View view) {
			if (!message.getMessageType().equals(kMessageTypeText)) {
				if (listener != null) {
					listener.onMessageClick(message, view, getAdapterPosition());
				}
			}
		}

		@Override
		public boolean onLongClick(View v) {
			return false;
		}

		@Override
		public void onMapReady(GoogleMap googleMap) {
			double lat = message.getLatitude();
			double lon = message.getLongitude();
			MapsInitializer.initialize(activity.getApplicationContext());
			googleMap.getUiSettings().setMapToolbarEnabled(false);
			googleMap.getUiSettings().setAllGesturesEnabled(false);
			googleMap.getUiSettings().setMyLocationButtonEnabled(false);
			LatLng sydney = new LatLng(lat, lon);
			googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
			googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
		}

	}

}