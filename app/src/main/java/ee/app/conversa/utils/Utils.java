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

package ee.app.conversa.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ee.app.conversa.R;

/**
 * Utils
 * 
 * Contains various methods used through the application.
 */
public class Utils {

	public static class ForegroundCheckAsync extends AsyncTask<Context, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Context... params) {
			final Context context = params[0];
			return isAppOnForeground(context);
		}

		private boolean isAppOnForeground(Context context) {
			ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
			if (appProcesses == null) {
				return false;
			}
			final String packageName = context.getPackageName();
			for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
				if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
						&& appProcess.processName.equals(packageName)) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Checks whether this app has mobile or wireless connection
	 *
	 * @return true if connected
	 */
	public static boolean hasNetworkConnection(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnected();
	}

	public static void hideKeyboard(AppCompatActivity activity) {
		activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		try {
			InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE);
			View cur_focus = activity.getCurrentFocus();
			if (cur_focus != null) {
				inputMethodManager.hideSoftInputFromWindow(cur_focus.getWindowToken(), 0);
			}
		} catch (IllegalStateException e) {
			Logger.error(activity.getClass().toString(), e.getMessage());
		}
	}
	
	public static String checkPassword(Activity activity, String password) {
		if (password.length() < 6) {
			return activity.getString(R.string.password_error_number_of_characters);
		} else if (!isAlphaNumeric(password)){
			return activity.getString(R.string.password_error_invalid_characters);
		}
		return activity.getString(R.string.password_ok);
	}

    public static String checkEmail(Activity activity, String email) {
        if (email == null || email.isEmpty()) {
            return activity.getString(R.string.email_length);
        } else if (!isEmailValid(email)) {
            return activity.getString(R.string.email_not_valid);
        }
        return activity.getString(R.string.email_ok);
    }
	
	public static String checkName(Activity activity, String name) {
		if (name != null && name.length() > 1) {
			return activity.getString(R.string.name_ok);
		} else {
			return activity.getString(R.string.name_error);
		}
	}
	
	private static boolean isAlphaNumeric(final String s){
	    String pattern = "/^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*\\W).{6,}$/";
		Pattern mPattern = Pattern.compile(pattern);
        Matcher matcher = mPattern.matcher(s);
        return matcher.matches();
	}

    /**
     * Used for checking valid email format.
     *
     * @param email String that will be checked
     * @return boolean true for valid false for invalid
     */
    private static boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

	public static int getToolbarHeight(Context context) {
		final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
				new int[]{R.attr.actionBarSize});
		int toolbarHeight = (int) styledAttributes.getDimension(0, 0);
		styledAttributes.recycle();

		return toolbarHeight;
	}
}