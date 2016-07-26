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

import java.util.ArrayList;
import java.util.List;

import ee.app.conversa.ActivityLocation;
import ee.app.conversa.R;
import ee.app.conversa.model.Database.Message;
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
	private List<Message> mMessages;

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
		if (holder.tag.equals(ViewHolder.class.getSimpleName())) {
			showMessage(mMessages.get(position), (ViewHolder)holder, position);
		} else {
			showIncomingMessage(mMessages.get(position), (IncomingViewHolder)holder, position);
		}
	}

	public void addMessage(Message message) {
		mMessages.add(0, message);
		notifyItemInserted(0);
	}

	public void addMessages(List<Message> messages, int positionStart) {
		mMessages.addAll(messages);
		notifyItemRangeInserted(positionStart, messages.size());
	}

	public void updateMessage(Message message, int from, int count) {
		int size = mMessages.size();

		for (int i = 0; i < size; i++) {
			Message m = mMessages.get(i);
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

	private void showMessage(final Message m, final ViewHolder holder, final int position) {
		// 1. Hide date. Will later check date text string and if it should be visible
		holder.mTvDate.setVisibility(View.GONE);

		// 2. Hide message subtext and map/image relative layout
		holder.mRlImageContainer.setVisibility(View.GONE);
		holder.mLtvSubText.setVisibility(View.GONE);

		switch (m.getMessageType()) {
			case Const.kMessageTypeText:
				// 3. Set messaget text
				holder.mRtvMessageText.setText(m.getBody());
				break;
			case Const.kMessageTypeLocation:
				// 3. Hide text and show image container relative layout
				holder.mRtvMessageText.setVisibility(View.GONE);
				holder.mRlImageContainer.setVisibility(View.VISIBLE);
				// 3.1 Decide which view contained in image container should be visible
				holder.mMvMessageMap.setVisibility(View.VISIBLE);
				holder.mSdvMessageImage.setVisibility(View.GONE);
				// 3.2 Start map view
				holder.mMvMessageMap.onCreate(null);
				holder.mMvMessageMap.onResume();
				holder.mMvMessageMap.getMapAsync(new OnMapReadyCallback() {
					@Override
					public void onMapReady(GoogleMap map) {
						MapsInitializer.initialize(mActivity.getApplicationContext());
						map.getUiSettings().setMapToolbarEnabled(false);
						map.getUiSettings().setAllGesturesEnabled(false);
						map.getUiSettings().setMyLocationButtonEnabled(false);
						LatLng sydney = new LatLng(
								m.getLatitude(),
								m.getLongitude());
						map.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
						map.moveCamera(CameraUpdateFactory.newLatLng(sydney));
					}
				});
				// 3.4 Set map click listener
				holder.mMvMessageMap.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(mActivity, ActivityLocation.class);
						intent.putExtra(Const.LOCATION, "userLocation");
						intent.putExtra(Const.LATITUDE, m.getLatitude());
						intent.putExtra(Const.LONGITUDE, m.getLongitude());
						mActivity.startActivity(intent);
					}
				});
				break;
			case Const.kMessageTypeImage:
				break;
			case Const.kMessageTypeVideo:
				break;
			case Const.kMessageTypeAudio:
				break;
		}

		// 4. Decide if date should be visible
		holder.mTvDate.setText(setDate(m));

		// 5. Decide whether to show message status
		if (m.getDeliveryStatus().equals(Message.statusParseError)) {
			holder.mLtvSubText.setVisibility(View.VISIBLE);
			holder.mLtvSubText.setText(mActivity.getString(R.string.app_name));
		} else {
			holder.mLtvSubText.setVisibility(View.GONE);
		}
	}

	private void showIncomingMessage(final Message m, final IncomingViewHolder holder, final int position) {
		// 1. Hide date. Will later check date text string and if it should be visible
		holder.mTvDate.setVisibility(View.GONE);

		// 2. Hide message subtext and map/image relative layout
		holder.mRlImageContainer.setVisibility(View.GONE);
		holder.mLtvSubText.setVisibility(View.GONE);

		switch (m.getMessageType()) {
			case Const.kMessageTypeText:
				// 3. Set messaget text
				holder.mRtvMessageText.setText(m.getBody());
				break;
			case Const.kMessageTypeLocation:
				// 3. Hide text and show image container relative layout
				holder.mRtvMessageText.setVisibility(View.GONE);
				holder.mRlImageContainer.setVisibility(View.VISIBLE);
				// 3.1 Decide which view contained in image container should be visible
				holder.mMvMessageMap.setVisibility(View.VISIBLE);
				holder.mSdvMessageImage.setVisibility(View.GONE);
				// 3.2 Start map view
				holder.mMvMessageMap.onCreate(null);
				holder.mMvMessageMap.onResume();
				holder.mMvMessageMap.getMapAsync(new OnMapReadyCallback() {
					@Override
					public void onMapReady(GoogleMap map) {
						MapsInitializer.initialize(mActivity.getApplicationContext());
						map.getUiSettings().setMapToolbarEnabled(false);
						map.getUiSettings().setAllGesturesEnabled(false);
						map.getUiSettings().setMyLocationButtonEnabled(false);
						LatLng sydney = new LatLng(
								m.getLatitude(),
								m.getLongitude());
						map.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
						map.moveCamera(CameraUpdateFactory.newLatLng(sydney));
					}
				});
				// 3.4 Set map click listener
				holder.mMvMessageMap.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(mActivity, ActivityLocation.class);
						intent.putExtra(Const.LOCATION, "userLocation");
						intent.putExtra(Const.LATITUDE, m.getLatitude());
						intent.putExtra(Const.LONGITUDE, m.getLongitude());
						mActivity.startActivity(intent);
					}
				});
				break;
			case Const.kMessageTypeImage:
				break;
			case Const.kMessageTypeVideo:
				break;
			case Const.kMessageTypeAudio:
				break;
		}

		// 4. Decide if date should be visible
		holder.mTvDate.setText(setDate(m));

		// 5. Decide whether to show message status
		if (m.getDeliveryStatus().equals(Message.statusParseError)) {
			holder.mLtvSubText.setVisibility(View.VISIBLE);
			holder.mLtvSubText.setText(mActivity.getString(R.string.app_name));
		} else {
			holder.mLtvSubText.setVisibility(View.GONE);
		}
	}

	/**
	 * Sets time that has past since message was sent.
	 * @param message
	 * @return
	 */
	private String setDate(Message message) {
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

	private OnClickListener getPhotoListener(final Message m, final ViewHolder holder) {
		return new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (m != null) {
					Intent pushExtras = new Intent();
					//pushExtras.putExtra("message", m);
					mPushBroadcast.replaceExtras(pushExtras);
					LocalBroadcastManager.getInstance(mActivity.getApplicationContext()).sendBroadcast(mPushBroadcast);
				}
			}
		};
	}

	class ViewHolder extends GenericViewHolder {
		public TextView mTvDate;
		public RelativeLayout mRlBackground;
		public RegularTextView mRtvMessageText;
		public RelativeLayout mRlImageContainer;
		public MapView mMvMessageMap;
		public SimpleDraweeView mSdvMessageImage;
		public LightTextView mLtvSubText;

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
		}
	}

	class IncomingViewHolder extends GenericViewHolder {
		public TextView mTvDate;
		public RelativeLayout mRlBackground;
		public RegularTextView mRtvMessageText;
		public RelativeLayout mRlImageContainer;
		public MapView mMvMessageMap;
		public SimpleDraweeView mSdvMessageImage;
		public LightTextView mLtvSubText;

		public IncomingViewHolder(View itemView) {
			super(itemView, IncomingViewHolder.class.getSimpleName());
			this.mTvDate = (TextView) itemView.findViewById(R.id.tvDate);
			this.mRlBackground = (RelativeLayout) itemView.findViewById(R.id.rlBackground);
			this.mRtvMessageText = (RegularTextView) itemView.findViewById(R.id.rtvMessageText);
			this.mRlImageContainer = (RelativeLayout) itemView.findViewById(R.id.rlImageContainer);
			this.mMvMessageMap = (MapView) itemView.findViewById(R.id.mvMessageMap);
			this.mSdvMessageImage = (SimpleDraweeView) itemView.findViewById(R.id.sdvMessageImage);
			this.mLtvSubText = (LightTextView) itemView.findViewById(R.id.ltvSubText);

			this.mMvMessageMap.setClickable(false);
		}
	}

	// Taken from http://stackoverflow.com/questions/26245139/how-to-create-recyclerview-with-multiple-view-type
	public class GenericViewHolder extends RecyclerView.ViewHolder {
		protected final String tag;

		public GenericViewHolder(View itemView, String tag) {
			super(itemView);
			this.tag = tag;
		}
	}

}
