package ee.app.conversa.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Pattern;

import ee.app.conversa.R;

/**
 * Utils
 * 
 * Contains various methods used through the application.
 */
public class Utils {

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

	public static boolean checkName(String name) {
		return (name != null && !name.isEmpty());
	}

    public static boolean checkEmail(String email) {
		return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

	public static boolean checkPassword(String password) {
		String pattern = "/^(?=.*[A-Za-z])(?=.*\\d)(?=.*\\W).{6,}$/";
		return Pattern.compile(pattern).matcher(password).matches();
	}

	public static void subscribeToTags(String channelName) {
		JSONObject tags = new JSONObject();
		try {
			tags.put("upbc", channelName);
			tags.put("upvt", channelName);
			tags.put("UserType", 1);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		OneSignal.sendTags(tags);
	}

	public static int getCurrentApkReleaseVersion(Context context) {
		try {
			return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			throw new AssertionError(e);
		}
	}

	public static String getDate(Context context, long time) {
		Calendar cal = Calendar.getInstance(Locale.getDefault());
		cal.setTimeInMillis(time);
		String date = DateFormat.format("MM/yyyy", cal).toString();
		return context.getString(R.string.member_since, date);
	}
}