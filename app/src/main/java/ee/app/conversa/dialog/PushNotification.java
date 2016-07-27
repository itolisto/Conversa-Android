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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.R;
import ee.app.conversa.model.Database.dBusiness;

/**
 * PushNotification
 * 
 * Animates push notification on the top of the screen.
 */
public class PushNotification implements OnClickListener {

	public static final int SHORT_ANIM_DURATION = 0;
	public static final int MEDIUM_ANIM_DURATION = 1;
	public static final int LONG_ANIM_DURATION = 2;
	private final String TAG = PushNotification.class.getSimpleName();
	private int mAnimationDuration;

	private RelativeLayout mPushLayout;
	private Context mContext;

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

	public static PushNotification make(Context context, RelativeLayout layout) {
		PushNotification notification = new PushNotification();
		notification.mContext = context;
		notification.mPushLayout = layout;
		return notification;
	}

	public void show(String message, String contactId) {
		showNotification(message, contactId, MEDIUM_ANIM_DURATION, 4000);
	}

	public void show(String message, String contactId, int duration, int timeBeforeHiding) {
		showNotification(message, contactId, duration, timeBeforeHiding);
	}

	private void showNotification(String message, String contactId, int duration, int timeBeforeHiding) {
		if (contactId == null || contactId.isEmpty()) {
			Log.e(TAG, "Contact id cannot be null nor empty");
			return;
		}

		addView(message, contactId);
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

	private void addView(String message, String contactId) {
		mPushLayout.setVisibility(View.VISIBLE);
		dBusiness fromUser = ConversaApp.getDB().isContact(contactId);

		if (fromUser == null) {
			Log.e(TAG, "Contact doesn't exits, notification can't be displayed");
			return;
		}

		final TextView mTvUserName = (TextView) mPushLayout.findViewById(R.id.tvUserName);
		final TextView mTvNotification = (TextView) mPushLayout.findViewById(R.id.tvNotification);
		final SimpleDraweeView mSdvAvatar = (SimpleDraweeView) mPushLayout.findViewById(R.id.sdvPushAvatar);
		final ImageButton btnClose = (ImageButton) mPushLayout.findViewById(R.id.btnClose);
		mTvUserName.setText(fromUser.getDisplayName());
		mTvNotification.setText(message);
		//mSdvAvatar.setImageURI();
		btnClose.setOnClickListener(this);
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
		if (v.getId() == R.id.btnClose) {
			hideNotification();
		}
	}
}
