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

package ee.app.conversa.dialog;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import ee.app.conversa.ActivityChatWall;
import ee.app.conversa.ConversaApp;
import ee.app.conversa.R;
import ee.app.conversa.model.database.dbBusiness;
import ee.app.conversa.model.database.dbMessage;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Utils;

/**
 * InAppNotification
 * 
 * Animates push notification on the top of the screen.
 */
public class InAppNotification implements OnClickListener {

	public static final int SHORT_ANIM_DURATION = 0;
	public static final int MEDIUM_ANIM_DURATION = 1;
	public static final int LONG_ANIM_DURATION = 2;

	private RelativeLayout mPushLayout;
	private Context mContext;
	private dbBusiness user;

	private final TranslateAnimation mSlideFromTop = new TranslateAnimation(
			TranslateAnimation.RELATIVE_TO_PARENT, 0,
			TranslateAnimation.RELATIVE_TO_PARENT, 0,
			TranslateAnimation.RELATIVE_TO_SELF, (float) -1.0,
			TranslateAnimation.RELATIVE_TO_SELF, (float) 0);

	private final TranslateAnimation mSlideOutTop = new TranslateAnimation(
			TranslateAnimation.RELATIVE_TO_PARENT, 0,
			TranslateAnimation.RELATIVE_TO_PARENT, 0,
			TranslateAnimation.RELATIVE_TO_SELF, (float) 0,
			TranslateAnimation.RELATIVE_TO_SELF, (float) -1.0);

	private final TranslateAnimation mSlideOutTopOnClose = new TranslateAnimation(
			TranslateAnimation.RELATIVE_TO_PARENT, 0,
			TranslateAnimation.RELATIVE_TO_PARENT, 0,
			TranslateAnimation.RELATIVE_TO_SELF, (float) 0,
			TranslateAnimation.RELATIVE_TO_SELF, (float) -1.0);

	public static InAppNotification make(Context context, RelativeLayout layout) {
		InAppNotification notification = new InAppNotification();
		notification.mContext = context;
		notification.mPushLayout = layout;
		return notification;
	}

	public void show(dbMessage message) {
		show(message, MEDIUM_ANIM_DURATION);
	}

	public void show(dbMessage message, int duration) {
		showNotification(message, duration);
	}

	private void showNotification(dbMessage message, int duration) {
		if (ConversaApp.getInstance(mContext).getPreferences().getInAppNotificationPreview()) {
			addView(message);
			setTranslateAnimations(getDuration(duration));
			startTranslateAnimations();
		}

		if (ConversaApp.getInstance(mContext).getPreferences().getInAppNotificationSound()) {
			playSound();
		}
	}

	private int getDuration(int duration) {
		switch (duration) {
			case SHORT_ANIM_DURATION:
				return mContext.getResources().getInteger(
						android.R.integer.config_shortAnimTime);
			case MEDIUM_ANIM_DURATION:
				return mContext.getResources().getInteger(
						android.R.integer.config_mediumAnimTime);
			case LONG_ANIM_DURATION:
				return mContext.getResources().getInteger(
						android.R.integer.config_longAnimTime);
			default:
				return mContext.getResources().getInteger(
						android.R.integer.config_mediumAnimTime);
		}
	}

	private void addView(dbMessage message) {
		user = ConversaApp.getInstance(mContext)
				.getDB()
				.isContact(message.getFromUserId());

		TextView mTvUserName = (TextView) mPushLayout.findViewById(R.id.tvUserName);
		TextView mTvNotification = (TextView) mPushLayout.findViewById(R.id.tvNotification);
		SimpleDraweeView mSdvAvatar = (SimpleDraweeView) mPushLayout.findViewById(R.id.sdvPushAvatar);

		if (user != null) {
			mTvUserName.setText(user.getDisplayName());

			switch (message.getMessageType()) {
				case Const.kMessageTypeImage:
					mTvNotification.setText(mContext
							.getString(R.string.contacts_last_message_image));
					break;
				case Const.kMessageTypeLocation:
					mTvNotification.setText(mContext
							.getString(R.string.contacts_last_message_location));
					break;
				case Const.kMessageTypeAudio:
					mTvNotification.setText(mContext
							.getString(R.string.contacts_last_message_audio));
					break;
				case Const.kMessageTypeVideo:
					mTvNotification.setText(mContext
							.getString(R.string.contacts_last_message_video));
					break;
				case Const.kMessageTypeText:
					mTvNotification.setText(message.getBody().replaceAll("\\n", " "));
					break;
				default:
					mTvNotification.setText(mContext
							.getString(R.string.contacts_last_message_default));
					break;
			}

			Uri uri = Utils.getUriFromString(user.getAvatarThumbFileId());

			if (uri == null) {
				uri = Utils.getDefaultImage(mContext, R.drawable.ic_business_default);
			}

			mSdvAvatar.setImageURI(uri);

			mPushLayout.findViewById(R.id.btnClose).setVisibility(View.VISIBLE);
			mPushLayout.findViewById(R.id.btnClose).setOnClickListener(this);
			mPushLayout.setOnClickListener(this);
		} else {
			mTvUserName.setText("");
			mTvNotification.setText(mContext
					.getString(R.string.contacts_last_message_default));

			Uri uri = Utils.getUriFromString(user.getAvatarThumbFileId());

			if (uri == null) {
				uri = Utils.getDefaultImage(mContext, R.drawable.ic_business_default);
			}

			mSdvAvatar.setImageURI(uri);
			mPushLayout.findViewById(R.id.btnClose).setVisibility(View.GONE);
		}
	}

	private void playSound() {
		final SoundPool sounds;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			AudioAttributes attributes = new AudioAttributes.Builder()
					.setUsage(AudioAttributes.USAGE_NOTIFICATION)
					.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
					.build();
			sounds = new SoundPool.Builder()
					.setAudioAttributes(attributes)
					.build();
		} else {
			sounds = new SoundPool(15, AudioManager.STREAM_NOTIFICATION, 0);
		}

		sounds.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int soundId, int status) {
				/** soundId for Later handling of sound pool **/
				// in 2nd param u have to pass your desire ringtone
				sounds.play(soundId, 0.99f, 0.99f, 1, 0, 0.99f);
			}
		});

		sounds.load(mContext, R.raw.sound_notification, 1);
	}

	private void hideNotification() {
		if (!mSlideOutTopOnClose.hasStarted() && !mSlideOutTop.hasEnded()){
			mPushLayout.clearAnimation();
			mPushLayout.startAnimation(mSlideOutTopOnClose);
		}
	}

	private void setTranslateAnimations(int duration) {
		mSlideFromTop.setFillAfter(false);
		mSlideFromTop.setFillEnabled(false);
		mSlideFromTop.setDuration(duration);
		mSlideFromTop.setAnimationListener(new AnimationListener() {

			public void onAnimationStart(Animation animation) {
				mPushLayout.setVisibility(View.VISIBLE);
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationEnd(Animation animation) {
				mPushLayout.startAnimation(mSlideOutTop);
			}
		});
		mSlideOutTop.setStartOffset(duration * 3);
		mSlideOutTop.setDuration(duration);
		mSlideOutTop.setAnimationListener(new AnimationListener() {

			public void onAnimationStart(Animation animation) {
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationEnd(Animation animation) {
				mPushLayout.setVisibility(View.GONE);
			}
		});

		mSlideOutTopOnClose.setStartOffset(0);
		mSlideOutTopOnClose.setDuration(duration);
		mSlideOutTopOnClose.setAnimationListener(new AnimationListener() {

			public void onAnimationStart(Animation animation) {
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationEnd(Animation animation) {
				mPushLayout.setVisibility(View.GONE);
			}
		});
	}

	private void startTranslateAnimations() {
		mPushLayout.startAnimation(mSlideFromTop);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.rlPushNotification:
				mPushLayout.clearAnimation();
				mPushLayout.setVisibility(View.GONE);
				Intent intent = new Intent(mContext, ActivityChatWall.class);
				intent.putExtra(Const.iExtraBusiness, user);
				intent.putExtra(Const.iExtraAddBusiness, false);
				mContext.startActivity(intent);
				break;
			case R.id.btnClose:
				hideNotification();
				break;
		}
	}

}