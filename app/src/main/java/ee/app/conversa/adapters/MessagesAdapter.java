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
import android.os.Build;
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
import com.google.android.gms.maps.MapView;

import java.util.ArrayList;
import java.util.List;

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

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {

	private final String fromUser;
	private final AppCompatActivity mActivity;
	private List<Message> mMessages;

	public final static String PUSH = "ee.app.conversa.chatwallMessage.showImage";
	private static final Intent mPushBroadcast = new Intent(PUSH);

	public MessagesAdapter(AppCompatActivity activity) {
		this.fromUser = Account.getCurrentUser().getObjectId();
		this.mActivity = activity;
		this.mMessages = new ArrayList<>();
	}

	@Override
	public int getItemCount() {
		return mMessages.size();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, parent, false);
		return new ViewHolder(v);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		Message message = mMessages.get(position);
		if (message.getFromUserId().equals(fromUser)) {
			showMessageFromMe(message, holder, position);
		} else {
			showMessageToMe(message, holder, position);
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

	public void updateMessage(Message message, String status) {
		for (Message m : mMessages) {
			if(m.getId() == message.getId()) {
				m.setDeliveryStatus(status);
				break;
			}
		}
	}

	/**
	 * Sets time that has past since message was sent.
	 * @param message
	 * @return
	 */
	private String setSubText(Message message) {
		String subText = null;

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

	private void showMessageFromMe(final Message m, final ViewHolder holder, final int position) {
		// 1. Change RelativeLayout background
		if (mActivity != null) {
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				holder.mRlBackground.setBackground(mActivity.getResources().getDrawable(R.drawable.wall_msg_you, null));
			} else {
				holder.mRlBackground.setBackground(mActivity.getResources().getDrawable(R.drawable.wall_msg_you));
			}
		}

		// 2. Change RelativeLayout alignment
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			params.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
		}
		params.addRule(RelativeLayout.BELOW, holder.mTvDate.getId());

		holder.mRlBackground.setLayoutParams(params);

		RelativeLayout.LayoutParams paramsTwo = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		paramsTwo.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			paramsTwo.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
		}

		paramsTwo.addRule(RelativeLayout.BELOW, holder.mRlBackground.getId());
		holder.mLtvSubText.setLayoutParams(paramsTwo);

		// 3. Set rest of message details
//		holder.tvMessageTextFromMe.setVisibility(View.GONE);
//		holder.rlImageFromMe.setClickable(false);
//		holder.rlImageFromMe.setOnClickListener(null);
//		holder.rlImageFromMe.setVisibility(View.GONE);
//
		if (m.getMessageType().equals(Const.kMessageTypeText) ) {//|| m.getMessageType().equals(Const.kMessageTypeLocation)) {
			holder.mRtvMessageText.setText(m.getBody());
//			if (m.getMessageType().equals(Const.kMessageTypeLocation)) {
//				holder.rlImageFromMe.setVisibility(View.VISIBLE);
//				holder.rlImageFromMe.setClickable(true);
//				holder.rlMapImageFromMe.setVisibility(View.VISIBLE);
//				holder.rlImageImageFromMe.setVisibility(View.GONE);
//
//				ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.rlImageFromMe.getLayoutParams();
//				float scale = mActivity.getResources().getDisplayMetrics().density;
//				int pixel =  (int)(5 * scale + 0.5f);
//				params.topMargin = pixel;
//				holder.rlImageFromMe.setLayoutParams(params);
//
//				ViewGroup.LayoutParams param = (ViewGroup.LayoutParams) holder.rlImageFromMe.getLayoutParams();
//				pixel =  (int)(150 * scale + 0.5f);
//				param.width  = pixel;
//				pixel =  (int)(125 * scale + 0.5f);
//				param.height = pixel;
//				holder.rlImageFromMe.setLayoutParams(param);
//				holder.mapImageViewFromMe.onCreate(null);
//				holder.mapImageViewFromMe.onResume();
//				holder.mapImageViewFromMe.getMapAsync(new OnMapReadyCallback() {
//					@Override
//					public void onMapReady(GoogleMap map) {
//						MapsInitializer.initialize(mActivity.getApplicationContext());
//						map.getUiSettings().setMapToolbarEnabled(false);
//						map.getUiSettings().setAllGesturesEnabled(false);
//						map.getUiSettings().setMyLocationButtonEnabled(false);
//						LatLng sydney = new LatLng(
//								m.getLatitude(),
//								m.getLongitude());
//						map.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//						map.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//					}
//				});
//
//                holder.rlImageFromMe.setOnClickListener(new OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Intent intent = new Intent(mActivity, ActivityLocation.class);
//                        intent.putExtra(Const.LOCATION, "userLocation");
//                        intent.putExtra(Const.LATITUDE, m.getLatitude());
//                        intent.putExtra(Const.LONGITUDE, m.getLongitude());
//                        //intent.putExtra("idOfUser", UsersManagement.getLoginUser().getId());
//                        //intent.putExtra("nameOfUser", UsersManagement.getLoginUser().getName());
//                        mActivity.startActivity(intent);
//                    }
//                });
//			} else {
//				holder.tvMessageTextFromMe.setVisibility(View.VISIBLE);
//				holder.tvMessageTextFromMe.setText(m.getBody());
//			}
		} else if (m.getMessageType().equals(Const.kMessageTypeImage)) {
//			holder.rlImageFromMe.setVisibility(View.VISIBLE);
//			holder.rlImageFromMe.setClickable(true);
//			holder.rlMapImageFromMe.setVisibility(View.GONE);
//			holder.rlImageImageFromMe.setVisibility(View.VISIBLE);
//
//			ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) holder.rlImageFromMe.getLayoutParams();
//			final float scale = mActivity.getResources().getDisplayMetrics().density;
//			// convert the DP into pixel
//			int pixel =  (int)(100 * scale + 0.5f);
//			params.height = pixel;
//			params.width  = pixel;
//			holder.rlImageFromMe.setLayoutParams(params);
//
//			holder.rlImageFromMe.setOnClickListener(getPhotoListener(m, holder));
//			Utils.displayImage(m.getImageFileId(), Const.IMAGE_FOLDER, holder.ivMessagePhotoFromMe,
//					holder.pbLoadingForImageFromMe, ImageLoader.SMALL,
//					R.drawable.image_stub, false);
		} else {
//			holder.tvMessageTextFromMe.setVisibility(View.VISIBLE);
//			holder.tvMessageTextFromMe.setText(m.getBody());
		}

		holder.mTvDate.setText(setSubText(m));

		if (m.getReadAt() == 0) {
			holder.mLtvSubText.setVisibility(View.VISIBLE);
		} else {
			holder.mLtvSubText.setVisibility(View.GONE);
		}
	}

	private void showMessageToMe(final Message m, final ViewHolder holder, final int position) {
		holder.mRtvMessageText.setText(m.getBody());

//		holder.rlToMe.setVisibility(View.VISIBLE);
//		holder.tvMessageTextToMe.setVisibility(View.GONE);
//		holder.rlImageToMe.setClickable(false);
//		holder.rlImageToMe.setOnClickListener(null);
//		holder.rlImageToMe.setVisibility(View.GONE);
//
//		if (m.getMessageType() == 1 || m.getMessageType() == 3) {
//			if (m.getMessageType() == 3) {
//				holder.rlImageToMe.setVisibility(View.VISIBLE);
//				holder.rlImageToMe.setClickable(true);
//				holder.rlMapImageToMe.setVisibility(View.VISIBLE);
//				holder.rlImageImageToMe.setVisibility(View.GONE);
//
//				ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.rlImageToMe.getLayoutParams();
//				float scale = mActivity.getResources().getDisplayMetrics().density;
//				int pixel =  (int)(5 * scale + 0.5f);
//				params.topMargin = pixel;
//				holder.rlImageToMe.setLayoutParams(params);
//
//				ViewGroup.LayoutParams param = holder.rlImageToMe.getLayoutParams();
//				pixel =  (int)(150 * scale + 0.5f);
//				param.width  = pixel;
//				pixel =  (int)(125 * scale + 0.5f);
//				param.height = pixel;
//				holder.rlImageToMe.setLayoutParams(param);
//				holder.mapImageViewToMe.onCreate(null);
//				holder.mapImageViewToMe.onResume();
//				holder.mapImageViewToMe.getMapAsync(new OnMapReadyCallback() {
//					@Override
//					public void onMapReady(GoogleMap map) {
//						MapsInitializer.initialize(mActivity.getApplicationContext());
//						map.getUiSettings().setMapToolbarEnabled(false);
//						map.getUiSettings().setAllGesturesEnabled(false);
//						map.getUiSettings().setMyLocationButtonEnabled(false);
//						LatLng sydney = new LatLng(
//								m.getLatitude(),
//								m.getLongitude());
//						map.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//						map.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//					}
//				});
//
//				holder.rlImageToMe.setOnClickListener(new OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						Intent intent = new Intent(mActivity, ActivityLocation.class);
//						intent.putExtra(Const.LOCATION, "userLocation");
//						intent.putExtra(Const.LATITUDE, m.getLatitude());
//						intent.putExtra(Const.LONGITUDE, m.getLongitude());
////						try {
////							intent.putExtra("avatarFileId", UsersManagement.getToUser().getAvatarThumbFileId());
////							intent.putExtra("nameOfUser", UsersManagement.getToUser().getName());
////						} catch (NullPointerException e) {}
//						mActivity.startActivity(intent);
//					}
//				});
//			} else {
//				holder.tvMessageTextToMe.setVisibility(View.VISIBLE);
//				holder.tvMessageTextToMe.setText(m.getBody());
//			}
//		} else if (m.getMessageType() == 2) {
//
//			holder.rlImageToMe.setVisibility(View.VISIBLE);
//			holder.rlImageToMe.setClickable(true);
//			holder.rlMapImageToMe.setVisibility(View.GONE);
//			holder.rlImageImageToMe.setVisibility(View.VISIBLE);
//
//			ViewGroup.LayoutParams params = holder.rlImageToMe.getLayoutParams();
//			final float scale = mActivity.getResources().getDisplayMetrics().density;
//			int pixel =  (int)(100 * scale + 0.5f);
//			params.height = pixel;
//			params.width  = pixel;
//			holder.rlImageToMe.setLayoutParams(params);
//
//			holder.rlImageToMe.setOnClickListener(getPhotoListener(m, holder));
////			Utils.displayImage(m.getImageFileId(), Const.IMAGE_FOLDER, holder.ivMessagePhotoToMe,
////					holder.pbLoadingForImageToMe, ImageLoader.LARGE,
////					R.drawable.image_stub, false);
//		} else {
//
//			holder.tvMessageTextToMe.setVisibility(View.VISIBLE);
//			holder.tvMessageTextToMe.setText(m.getBody());
//		}
//
//		holder.tvMessageSubTextToMe.setText(setSubText(m));
	}

	class ViewHolder extends RecyclerView.ViewHolder {
		public TextView mTvDate;
		public RelativeLayout mRlBackground;
		public RegularTextView mRtvMessageText;
		public RelativeLayout mRlImageContainer;
		public MapView mMvMessageMap;
		public SimpleDraweeView mSdvMessageImage;
		public LightTextView mLtvSubText;


		public ViewHolder(View itemView) {
			super(itemView);
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

}
