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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import ee.app.conversa.ActivityLocation;
import ee.app.conversa.R;
import ee.app.conversa.model.Database.Message;
import ee.app.conversa.utils.Const;

/**
 * MessagesAdapter
 * 
 * Adapter class for chat wall messages.
 */

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {

	private AppCompatActivity mActivity;
	private List<Message> mMessages;

	public final static String PUSH = "ee.app.conversabusiness.chatwallMessage.showImage";
	private static final Intent mPushBroadcast = new Intent(PUSH);

	public MessagesAdapter(AppCompatActivity activity, List<Message> messages) {
		this.mActivity = activity;
		this.mMessages = messages;
	}

	@Override
	public long getItemId(int position) { return super.getItemId(position); }

	@Override
	public int getItemCount() { return (mMessages == null) ? 0 : mMessages.size(); }

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message, parent, false);
		ViewHolder mh = new ViewHolder(v);
		return mh;
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		holder.rlFromMe.clearAnimation();
		holder.rlToMe.clearAnimation();
		holder.rlFromMe.setVisibility(View.GONE);
		holder.rlToMe.setVisibility(View.GONE);
		holder.ivMessagePhotoToMe.setImageBitmap(null);
		holder.ivMessagePhotoFromMe.setImageBitmap(null);

		Message message = mMessages.get(position);

		boolean isMessageFromMe = message.getType().equals(String.valueOf(Const.C_TYPE));

		if (isMessageFromMe) {
			showMessageFromMe(message, holder);
		} else {
			showMessageToMe(message, holder);
		}
	}

	public void addMessage(int position, Message message) {
		mMessages.add(message);
		notifyItemInserted(position);
	}

	public void addMessages(List<Message> messages, int positionStart) {
		mMessages.addAll(messages);
		notifyItemRangeInserted(positionStart, messages.size());
	}

	public void updateMessages() {
//		if(UsersManagement.getToUser() != null) {
//			String id = UsersManagement.getToUser().getId();
//			mMessages = ConversaApp.getDB().getMessagesByContact(id,getItemCount(),0);
//			Collections.reverse(mMessages);
//			notifyItemRangeChanged(0, getItemCount());
//		}
	}

	class ViewHolder extends RecyclerView.ViewHolder {
		//LAYOUT FOR MESSAGES FROM ME
		public RelativeLayout rlFromMe;
		public RelativeLayout rlImageFromMe;
		public RelativeLayout rlMapImageFromMe;
		public RelativeLayout rlImageImageFromMe;
		public TextView tvMessageTextFromMe;
		public TextView tvMessageSubTextFromMe;
		public ImageView unread;
		public ImageView ivMessagePhotoFromMe;
		public ProgressBar pbLoadingForImageFromMe;
		public MapView mapImageViewFromMe;
		//LAYOUT FOR MESSAGES TO ME
		public RelativeLayout rlToMe;
		public RelativeLayout rlImageToMe;
		public RelativeLayout rlMapImageToMe;
		public RelativeLayout rlImageImageToMe;
		public TextView tvMessageTextToMe;
		public TextView tvMessageSubTextToMe;
		public ImageView ivMessagePhotoToMe;
		public ProgressBar pbLoadingForImageToMe;
		public MapView mapImageViewToMe;

		public ViewHolder(View itemView) {
			super(itemView);
			this.rlFromMe 				 = (RelativeLayout) itemView.findViewById(R.id.rlFromMeLayout);
			this.tvMessageTextFromMe     = (TextView) itemView.findViewById(R.id.messageTextFromMe);
			this.ivMessagePhotoFromMe    = (ImageView) itemView.findViewById(R.id.ivMessagePhotoFromMe);
			this.tvMessageSubTextFromMe  = (TextView) itemView.findViewById(R.id.messageSubTextFromMe);
			this.rlImageImageFromMe      = (RelativeLayout) itemView.findViewById(R.id.rlImageImageFromMe);
			this.rlImageFromMe 			 = (RelativeLayout) itemView.findViewById(R.id.rlImageFromMe);
			this.pbLoadingForImageFromMe = (ProgressBar) itemView.findViewById(R.id.pbLoadingForImageFromMe);
			this.rlMapImageFromMe 		 = (RelativeLayout) itemView.findViewById(R.id.rlMapImageFromMe);
			this.mapImageViewFromMe      = (MapView) itemView.findViewById(R.id.mapImageViewFromMe);
			this.unread                  = (ImageView) itemView.findViewById(R.id.unread);

			this.rlToMe 				 = (RelativeLayout) itemView.findViewById(R.id.rlToMeLayout);
			this.tvMessageTextToMe 		 = (TextView) itemView.findViewById(R.id.messageTextToMe);
			this.ivMessagePhotoToMe 	 = (ImageView) itemView.findViewById(R.id.ivMessagePhotoToMe);
			this.tvMessageSubTextToMe 	 = (TextView) itemView.findViewById(R.id.messageSubTextToMe);
			this.rlImageImageToMe        = (RelativeLayout) itemView.findViewById(R.id.rlImageImageToMe);
			this.rlImageToMe 			 = (RelativeLayout) itemView.findViewById(R.id.rlImageToMe);
			this.pbLoadingForImageToMe   = (ProgressBar) itemView.findViewById(R.id.pbLoadingForImageToMe);
			this.rlMapImageToMe 		 = (RelativeLayout) itemView.findViewById(R.id.rlMapImageToMe);
			this.mapImageViewToMe        = (MapView) itemView.findViewById(R.id.mapImageViewToMe);

			//LayoutHelper.scaleWidthAndHeightAbsolute(mActivity, 2.5f, this.ivMessagePhotoFromMe);
//			LayoutHelper.scaleWidthAndHeightAbsolute(mActivity, 2.5f, this.ivMessagePhotoToMe);
			
			this.mapImageViewFromMe.setClickable(false);
			this.mapImageViewToMe.setClickable(false);
		}
	}

	/**
	 * Sets time that has past since message was sent.
	 * @param message
	 * @return
	 */
	private String setSubText(Message message) {
		String subText;

		long timeOfCreationOrUpdate = message.getCreated();
		if (message.getCreated() < message.getModified()) {
			timeOfCreationOrUpdate = message.getModified();
		}

		long diff = System.currentTimeMillis() - (Long.valueOf(timeOfCreationOrUpdate) * 1000);
		long diffm = diff / (1000 * 60);
		long diffh = diff / (1000 * 60 * 60);
		long diffd = diff / (1000 * 60 * 60 * 24);
		long diffw  = diff / (1000 * 60 * 60 * 24 * 7);

		if (diffw >= 2) {
			subText = diffw + " " + mActivity.getString(R.string.weeks_ago);
		} else if (diffw >= 1  && diffw < 2) {
			subText = diffw + " " + mActivity.getString(R.string.week_ago);
		} else if (diffh >= 48 && diffh < 168) {
			subText = diffd + " " + mActivity.getString(R.string.days_ago);
		} else if (diffh >= 24 && diffh < 48) {
			subText = diffd + " " + mActivity.getString(R.string.day_ago);
		} else if (diffh >= 2  && diffh < 24) {
			subText = diffh + " " + mActivity.getString(R.string.hours_ago);
		} else if (diffm >= 60 && diffm < 120) {
			subText = diffh + " " + mActivity.getString(R.string.hour_ago);
		} else if (diffm > 1   && diffm < 60) {
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
					pushExtras.putExtra("message", m);
					mPushBroadcast.replaceExtras(pushExtras);
					LocalBroadcastManager.getInstance(mActivity.getApplicationContext()).sendBroadcast(mPushBroadcast);
				}
			}
		};
	}

	private void showMessageFromMe(final Message m, final ViewHolder holder) {

		holder.rlFromMe.setVisibility(View.VISIBLE);
		holder.tvMessageTextFromMe.setVisibility(View.GONE);
		holder.rlImageFromMe.setClickable(false);
		holder.rlImageFromMe.setOnClickListener(null);
		holder.rlImageFromMe.setVisibility(View.GONE);

		if (m.getMessageType() == 1 || m.getMessageType() == 3) {
			if (m.getMessageType() == 3) {
				holder.rlImageFromMe.setVisibility(View.VISIBLE);
				holder.rlImageFromMe.setClickable(true);
				holder.rlMapImageFromMe.setVisibility(View.VISIBLE);
				holder.rlImageImageFromMe.setVisibility(View.GONE);

				ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.rlImageFromMe.getLayoutParams();
				float scale = mActivity.getResources().getDisplayMetrics().density;
				int pixel =  (int)(5 * scale + 0.5f);
				params.topMargin = pixel;
				holder.rlImageFromMe.setLayoutParams(params);

				ViewGroup.LayoutParams param = (ViewGroup.LayoutParams) holder.rlImageFromMe.getLayoutParams();
				pixel =  (int)(150 * scale + 0.5f);
				param.width  = pixel;
				pixel =  (int)(125 * scale + 0.5f);
				param.height = pixel;
				holder.rlImageFromMe.setLayoutParams(param);
				holder.mapImageViewFromMe.onCreate(null);
				holder.mapImageViewFromMe.onResume();
				holder.mapImageViewFromMe.getMapAsync(new OnMapReadyCallback() {
					@Override
					public void onMapReady(GoogleMap map) {
						MapsInitializer.initialize(mActivity.getApplicationContext());
						map.getUiSettings().setMapToolbarEnabled(false);
						map.getUiSettings().setAllGesturesEnabled(false);
						map.getUiSettings().setMyLocationButtonEnabled(false);
						LatLng sydney = new LatLng(
								Double.parseDouble(m.getLatitude()),
								Double.parseDouble(m.getLongitude()));
						map.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
						map.moveCamera(CameraUpdateFactory.newLatLng(sydney));
					}
				});

                holder.rlImageFromMe.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mActivity, ActivityLocation.class);
                        intent.putExtra(Const.LOCATION, "userLocation");
                        intent.putExtra(Const.LATITUDE, Double.parseDouble(m.getLatitude()));
                        intent.putExtra(Const.LONGITUDE, Double.parseDouble(m.getLongitude()));
                        //intent.putExtra("idOfUser", UsersManagement.getLoginUser().getId());
                        //intent.putExtra("nameOfUser", UsersManagement.getLoginUser().getName());
                        mActivity.startActivity(intent);
                    }
                });
			} else {
				holder.tvMessageTextFromMe.setVisibility(View.VISIBLE);
				holder.tvMessageTextFromMe.setText(m.getBody());
			}
		} else if (m.getMessageType() == 2) {

			holder.rlImageFromMe.setVisibility(View.VISIBLE);
			holder.rlImageFromMe.setClickable(true);
			holder.rlMapImageFromMe.setVisibility(View.GONE);
			holder.rlImageImageFromMe.setVisibility(View.VISIBLE);

			ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) holder.rlImageFromMe.getLayoutParams();
			final float scale = mActivity.getResources().getDisplayMetrics().density;
			// convert the DP into pixel
			int pixel =  (int)(100 * scale + 0.5f);
			params.height = pixel;
			params.width  = pixel;
			holder.rlImageFromMe.setLayoutParams(params);

			holder.rlImageFromMe.setOnClickListener(getPhotoListener(m, holder));
//			Utils.displayImage(m.getImageFileId(), Const.IMAGE_FOLDER, holder.ivMessagePhotoFromMe,
//					holder.pbLoadingForImageFromMe, ImageLoader.SMALL,
//					R.drawable.image_stub, false);
		} else {

			holder.tvMessageTextFromMe.setVisibility(View.VISIBLE);
			holder.tvMessageTextFromMe.setText(m.getBody());
		}

		holder.tvMessageSubTextFromMe.setText(setSubText(m));

		if (m.getReadAt() == 0) {
			holder.unread.setVisibility(View.VISIBLE);
		} else {
			holder.unread.setVisibility(View.GONE);
		}
	}

	private void showMessageToMe(final Message m, ViewHolder holder) {
		holder.rlToMe.setVisibility(View.VISIBLE);
		holder.tvMessageTextToMe.setVisibility(View.GONE);
		holder.rlImageToMe.setClickable(false);
		holder.rlImageToMe.setOnClickListener(null);
		holder.rlImageToMe.setVisibility(View.GONE);

		if (m.getMessageType() == 1 || m.getMessageType() == 3) {
			if (m.getMessageType() == 3) {
				holder.rlImageToMe.setVisibility(View.VISIBLE);
				holder.rlImageToMe.setClickable(true);
				holder.rlMapImageToMe.setVisibility(View.VISIBLE);
				holder.rlImageImageToMe.setVisibility(View.GONE);

				ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.rlImageToMe.getLayoutParams();
				float scale = mActivity.getResources().getDisplayMetrics().density;
				int pixel =  (int)(5 * scale + 0.5f);
				params.topMargin = pixel;
				holder.rlImageToMe.setLayoutParams(params);

				ViewGroup.LayoutParams param = holder.rlImageToMe.getLayoutParams();
				pixel =  (int)(150 * scale + 0.5f);
				param.width  = pixel;
				pixel =  (int)(125 * scale + 0.5f);
				param.height = pixel;
				holder.rlImageToMe.setLayoutParams(param);
				holder.mapImageViewToMe.onCreate(null);
				holder.mapImageViewToMe.onResume();
				holder.mapImageViewToMe.getMapAsync(new OnMapReadyCallback() {
					@Override
					public void onMapReady(GoogleMap map) {
						MapsInitializer.initialize(mActivity.getApplicationContext());
						map.getUiSettings().setMapToolbarEnabled(false);
						map.getUiSettings().setAllGesturesEnabled(false);
						map.getUiSettings().setMyLocationButtonEnabled(false);
						LatLng sydney = new LatLng(
								Double.parseDouble(m.getLatitude()),
								Double.parseDouble(m.getLongitude()));
						map.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
						map.moveCamera(CameraUpdateFactory.newLatLng(sydney));
					}
				});

				holder.rlImageToMe.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(mActivity, ActivityLocation.class);
						intent.putExtra(Const.LOCATION, "userLocation");
						intent.putExtra(Const.LATITUDE, Double.parseDouble(m.getLatitude()));
						intent.putExtra(Const.LONGITUDE, Double.parseDouble(m.getLongitude()));
//						try {
//							intent.putExtra("avatarFileId", UsersManagement.getToUser().getAvatarThumbFileId());
//							intent.putExtra("nameOfUser", UsersManagement.getToUser().getName());
//						} catch (NullPointerException e) {}
						mActivity.startActivity(intent);
					}
				});
			} else {
				holder.tvMessageTextToMe.setVisibility(View.VISIBLE);
				holder.tvMessageTextToMe.setText(m.getBody());
			}
		} else if (m.getMessageType() == 2) {

			holder.rlImageToMe.setVisibility(View.VISIBLE);
			holder.rlImageToMe.setClickable(true);
			holder.rlMapImageToMe.setVisibility(View.GONE);
			holder.rlImageImageToMe.setVisibility(View.VISIBLE);

			ViewGroup.LayoutParams params = holder.rlImageToMe.getLayoutParams();
			final float scale = mActivity.getResources().getDisplayMetrics().density;
			int pixel =  (int)(100 * scale + 0.5f);
			params.height = pixel;
			params.width  = pixel;
			holder.rlImageToMe.setLayoutParams(params);

			holder.rlImageToMe.setOnClickListener(getPhotoListener(m, holder));
//			Utils.displayImage(m.getImageFileId(), Const.IMAGE_FOLDER, holder.ivMessagePhotoToMe,
//					holder.pbLoadingForImageToMe, ImageLoader.LARGE,
//					R.drawable.image_stub, false);
		} else {

			holder.tvMessageTextToMe.setVisibility(View.VISIBLE);
			holder.tvMessageTextToMe.setText(m.getBody());
		}

		holder.tvMessageSubTextToMe.setText(setSubText(m));
	}
}
