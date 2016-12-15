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
import android.net.Uri;
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
import ee.app.conversa.utils.Logger;
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
	private final String TAG = InAppNotification.class.getSimpleName();
	private int mAnimationDuration;
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
		showNotification(message, MEDIUM_ANIM_DURATION, 4000);
	}

	public void show(dbMessage message, int duration, int timeBeforeHiding) {
		showNotification(message, duration, timeBeforeHiding);
	}

	private void showNotification(dbMessage message, int duration, int timeBeforeHiding) {
		if (message == null) {
			Logger.error(TAG, "Message cannot be null nor empty");
			return;
		}

		addView(message);
		setDuration(duration);
		setTranslateAnimations(timeBeforeHiding);
		startTranslateAnimations();
	}

	private void setDuration(int duration) {
		switch (duration) {
			case SHORT_ANIM_DURATION:
				mAnimationDuration = mContext.getResources().getInteger(
						android.R.integer.config_shortAnimTime);
				break;
			case MEDIUM_ANIM_DURATION:
				mAnimationDuration = mContext.getResources().getInteger(
						android.R.integer.config_mediumAnimTime);
				break;
			case LONG_ANIM_DURATION:
				mAnimationDuration = mContext.getResources().getInteger(
						android.R.integer.config_longAnimTime);
				break;
			default:
				mAnimationDuration = mContext.getResources().getInteger(
						android.R.integer.config_mediumAnimTime);
				break;
		}
	}

	private void addView(dbMessage message) {
		user = ConversaApp.getInstance(mContext)
				.getDB()
				.isContact(message.getFromUserId());

		if (user == null) {
			Logger.error(TAG, "Contact doesn't exits, notification can't be displayed");
			return;
		}


		TextView mTvUserName = (TextView) mPushLayout.findViewById(R.id.tvUserName);
		TextView mTvNotification = (TextView) mPushLayout.findViewById(R.id.tvNotification);
		SimpleDraweeView mSdvAvatar = (SimpleDraweeView) mPushLayout.findViewById(R.id.sdvPushAvatar);
		mTvUserName.setText(user.getDisplayName());

		switch(message.getMessageType()) {
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

		mPushLayout.findViewById(R.id.btnClose).setOnClickListener(this);
		mPushLayout.setOnClickListener(this);
	}

	private void hideNotification() {
		if (!mSlideOutTopOnClose.hasStarted() && !mSlideOutTop.hasEnded()){
			mPushLayout.clearAnimation();
			mPushLayout.startAnimation(mSlideOutTopOnClose);
		}
	}

	private void setTranslateAnimations(int timeBeforeHiding) {
		mSlideFromTop.setFillAfter(false);
		mSlideFromTop.setFillEnabled(false);
		mSlideFromTop.setDuration(mAnimationDuration);
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
		mSlideOutTop.setStartOffset(timeBeforeHiding);
		mSlideOutTop.setDuration(mAnimationDuration);
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
		mSlideOutTopOnClose.setDuration(mAnimationDuration);
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