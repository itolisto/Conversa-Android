/*
 * The MIT License (MIT)
 * 
 * Copyright � 2013 Clover Studio Ltd. All rights reserved.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ee.app.conversa.adapters;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import ee.app.conversa.ActivityLocation;
import ee.app.conversa.R;
import ee.app.conversa.model.Database.dbMessage;
import ee.app.conversa.model.Parse.Account;
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
	private final String fromUser;
	private final AppCompatActivity mActivity;
	private List<dbMessage> mMessages;

	public final static String PUSH = "ee.app.conversa.chatwallMessage.showImage";
	private static final Intent mPushBroadcast = new Intent(PUSH);

	public MessagesAdapter(AppCompatActivity activity) {
		this.fromUser = Account.getCurrentUser().getObjectId();
		this.mActivity = activity;
		this.mMessages = new ArrayList<>(20);
	}

	@Override
	public int getItemViewType(int position) {
		return (mMessages.get(position).getFromUserId().equals(fromUser)) ? FROM_ME_VIEW_TYPE : TO_ME_VIEW_TYPE;
	}

	@Override
	public int getItemCount() {
		return mMessages.size();
	}

	@Override
	public GenericViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == FROM_ME_VIEW_TYPE) {
			return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, parent, false));
		} else {
			return new IncomingViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.message_incoming_item, parent, false));
		}
	}

	@Override
	public void onBindViewHolder(GenericViewHolder holder, int position) {
		if (position > 0) {
			holder.showMessage(mMessages.get(position), mMessages.get(position - 1), mActivity);
		} else {
			holder.showMessage(mMessages.get(position), null, mActivity);
		}
	}

	public void setMessages(List<dbMessage> messages) {
		mMessages = messages;
		notifyDataSetChanged();
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
			dbMessage m = mMessages.get(i);
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

	/**
	 * Sets time that has past since message was sent.
	 * @param message
	 * @return
	 */
	private String setDate(dbMessage message) {
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
			subText = diffw + " " + mActivity.getString(R.string.weeks_ago);
		} else if (diffw >= 1 && diffw < 2) {
			subText = diffw + " " + mActivity.getString(R.string.week_ago);
		} else if (diffh >= 48 && diffh < 168) {
			subText = diffd + " " + mActivity.getString(R.string.days_ago);
		} else if (diffh >= 24 && diffh < 48) {
			subText = diffd + " " + mActivity.getString(R.string.day_ago);
		} else if (diffh >= 2 && diffh < 24) {
			subText = diffh + " " + mActivity.getString(R.string.hours_ago);
		} else if (diffm >= 60 && diffm < 120) {
			subText = diffh + " " + mActivity.getString(R.string.hour_ago);
		} else if (diffm > 1 && diffm < 60) {
			subText = diffm + " " + mActivity.getString(R.string.minutes_ago);
		} else if (diffm == 1) {
			subText = diffm + " " + mActivity.getString(R.string.minute_ago);
		} else {
			subText = mActivity.getString(R.string.posted_less_than_a_minute_ago);
		}

		return subText;
	}

	class ViewHolder extends GenericViewHolder implements OnClickListener, View.OnLongClickListener, OnMapReadyCallback {
		public TextView mTvDate;
		public RelativeLayout mRlBackground;
		public RegularTextView mRtvMessageText;
		public RelativeLayout mRlImageContainer;
		public MapView mMvMessageMap;
		public SimpleDraweeView mSdvMessageImage;
		public LightTextView mLtvSubText;
		private WeakReference<dbMessage> message;
		private WeakReference<AppCompatActivity> activity;

		public ViewHolder(View itemView) {
			super(itemView, ViewHolder.class.getSimpleName());
			this.mTvDate = (TextView) itemView.findViewById(R.id.tvDate);
			this.mRlBackground = (RelativeLayout) itemView.findViewById(R.id.rlBackground);
			this.mRtvMessageText = (RegularTextView) itemView.findViewById(R.id.rtvMessageText);
			this.mRlImageContainer = (RelativeLayout) itemView.findViewById(R.id.rlImageContainer);
			this.mMvMessageMap = (MapView) itemView.findViewById(R.id.mvMessageMap);
			this.mSdvMessageImage = (SimpleDraweeView) itemView.findViewById(R.id.sdvMessageImage);
			this.mLtvSubText = (LightTextView) itemView.findViewById(R.id.ltvSubText);

			this.mMvMessageMap.setClickable(false);

			itemView.setOnClickListener(this);
			itemView.setOnLongClickListener(this);
		}

		@Override
		public void onClick(View view) {
			if (view.getId() == R.id.sdvMessageImage) {
				if (activity.get() != null && message.get() != null) {
					Intent pushExtras = new Intent();
					pushExtras.putExtra("message", message.get());
					mPushBroadcast.replaceExtras(pushExtras);
					LocalBroadcastManager.getInstance(mActivity.getApplicationContext()).sendBroadcast(mPushBroadcast);
				}
			} else if (view.getId() == R.id.mvMessageMap) {
				if (activity.get() != null && message.get() != null) {
					Intent intent = new Intent(activity.get(), ActivityLocation.class);
					intent.putExtra(Const.LOCATION, "userLocation");
					intent.putExtra(Const.LATITUDE, message.get().getLatitude());
					intent.putExtra(Const.LONGITUDE, message.get().getLongitude());
					activity.get().startActivity(intent);
				}
			}
		}

		@Override
		public boolean onLongClick(View view) {
			return false;
		}

		@Override
		public void onMapReady(GoogleMap googleMap) {
			if (activity.get() != null && message.get() != null) {
				double lat = message.get().getLatitude();
				double lon = message.get().getLongitude();
				MapsInitializer.initialize(mActivity.getApplicationContext());
				googleMap.getUiSettings().setMapToolbarEnabled(false);
				googleMap.getUiSettings().setAllGesturesEnabled(false);
				googleMap.getUiSettings().setMyLocationButtonEnabled(false);
				LatLng sydney = new LatLng(lat, lon);
				googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
				googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
			}
		}

		@Override
		public void showMessage(dbMessage message, dbMessage previousMessage, AppCompatActivity activity) {
			this.message = new WeakReference<>(message);
			this.activity = new WeakReference<>(activity);

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
			this.mTvDate.setText(setDate(message));

			// 5. Decide whether to show message status
			if (message.getDeliveryStatus().equals(dbMessage.statusParseError)) {
				this.mLtvSubText.setVisibility(View.VISIBLE);
				this.mLtvSubText.setText(mActivity.getString(R.string.app_name));
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
			// 1. Resize image to display
			ViewGroup.LayoutParams params = this.mSdvMessageImage.getLayoutParams();
			final float scale = mActivity.getResources().getDisplayMetrics().density;
			// 1.1 Convert the DP into pixel
			int pixel =  (int)(100 * scale + 0.5f);
			params.height = pixel;
			params.width  = pixel;
			this.mSdvMessageImage.setLayoutParams(params);
			// 2. Load image
			if (activity.get() != null && message.get() != null) {
				Uri uri = Uri.parse(message.get().getFileId());
				this.mSdvMessageImage.setImageURI(uri);
			}
		}
	}

	class IncomingViewHolder extends GenericViewHolder implements OnClickListener, View.OnLongClickListener, OnMapReadyCallback {
		public TextView mTvDate;
		public RelativeLayout mRlBackground;
		public RegularTextView mRtvMessageText;
		public RelativeLayout mRlImageContainer;
		public MapView mMvMessageMap;
		public SimpleDraweeView mSdvMessageImage;
		public LightTextView mLtvSubText;
		private WeakReference<dbMessage> message;
		private WeakReference<AppCompatActivity> activity;

		public IncomingViewHolder(View itemView) {
			super(itemView, ViewHolder.class.getSimpleName());
			this.mTvDate = (TextView) itemView.findViewById(R.id.tvDate);
			this.mRlBackground = (RelativeLayout) itemView.findViewById(R.id.rlBackground);
			this.mRtvMessageText = (RegularTextView) itemView.findViewById(R.id.rtvMessageText);
			this.mRlImageContainer = (RelativeLayout) itemView.findViewById(R.id.rlImageContainer);
			this.mMvMessageMap = (MapView) itemView.findViewById(R.id.mvMessageMap);
			this.mSdvMessageImage = (SimpleDraweeView) itemView.findViewById(R.id.sdvMessageImage);
			this.mLtvSubText = (LightTextView) itemView.findViewById(R.id.ltvSubText);

			this.mMvMessageMap.setClickable(false);

			itemView.setOnClickListener(this);
			itemView.setOnLongClickListener(this);
		}

		@Override
		public void onClick(View view) {
			if (view.getId() == R.id.sdvMessageImage) {
				if (activity.get() != null && message.get() != null) {
					Intent pushExtras = new Intent();
					pushExtras.putExtra("message", message.get());
					mPushBroadcast.replaceExtras(pushExtras);
					LocalBroadcastManager.getInstance(mActivity.getApplicationContext()).sendBroadcast(mPushBroadcast);
				}
			} else if (view.getId() == R.id.mvMessageMap) {
				if (activity.get() != null && message.get() != null) {
					Intent intent = new Intent(activity.get(), ActivityLocation.class);
					intent.putExtra(Const.LOCATION, "userLocation");
					intent.putExtra(Const.LATITUDE, message.get().getLatitude());
					intent.putExtra(Const.LONGITUDE, message.get().getLongitude());
					activity.get().startActivity(intent);
				}
			}
		}

		@Override
		public boolean onLongClick(View view) {
			return false;
		}

		@Override
		public void onMapReady(GoogleMap googleMap) {
			if (activity.get() != null && message.get() != null) {
				double lat = message.get().getLatitude();
				double lon = message.get().getLongitude();
				MapsInitializer.initialize(mActivity.getApplicationContext());
				googleMap.getUiSettings().setMapToolbarEnabled(false);
				googleMap.getUiSettings().setAllGesturesEnabled(false);
				googleMap.getUiSettings().setMyLocationButtonEnabled(false);
				LatLng sydney = new LatLng(lat, lon);
				googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
				googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
			}
		}

		@Override
		public void showMessage(dbMessage message, dbMessage previousMessage, AppCompatActivity activity) {
			this.message = new WeakReference<>(message);
			this.activity = new WeakReference<>(activity);

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
			this.mTvDate.setText(setDate(message));

			// 5. Decide whether to show message status
			if (message.getDeliveryStatus().equals(dbMessage.statusParseError)) {
				this.mLtvSubText.setVisibility(View.VISIBLE);
				this.mLtvSubText.setText(mActivity.getString(R.string.app_name));
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
			// 1. Resize image to display
			ViewGroup.LayoutParams params = this.mSdvMessageImage.getLayoutParams();
			final float scale = mActivity.getResources().getDisplayMetrics().density;
			// 1.1 Convert the DP into pixel
			int pixel =  (int)(100 * scale + 0.5f);
			params.height = pixel;
			params.width  = pixel;
			this.mSdvMessageImage.setLayoutParams(params);
			// 2. Load image
			if (activity.get() != null && message.get() != null) {
				Uri uri = Uri.parse(message.get().getFileId());
				this.mSdvMessageImage.setImageURI(uri);
			}
		}
	}

	// Taken from http://stackoverflow.com/questions/26245139/how-to-create-recyclerview-with-multiple-view-type
	public class GenericViewHolder extends RecyclerView.ViewHolder {
		protected final String tag;

		public GenericViewHolder(View itemView, String tag) {
			super(itemView);
			this.tag = tag;
		}

		public void showMessage(dbMessage message, dbMessage previousMessage, AppCompatActivity activity) {
			throw new RuntimeException("showMessage method must be override");
		}
	}

}
