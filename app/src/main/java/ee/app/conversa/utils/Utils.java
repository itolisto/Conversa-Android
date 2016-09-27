package ee.app.conversa.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.R;

import static ee.app.conversa.settings.language.DynamicLanguage.getSelectedLocale;

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

	public static int checkDate(String date, Context context) {
		try {
			SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", getSelectedLocale(context));
			df.setLenient(false);
			Date dDate = df.parse(date);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(dDate);

			if (getAge(calendar)) {
				return 0;
			}

			return 2;
		} catch (NullPointerException|IllegalArgumentException|ParseException e) {
			return 1;
		}
	}

	private static boolean getAge(Calendar dob) {
		Calendar today = Calendar.getInstance();
		int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

		if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
			age--;
		}

		return (age >= 18);
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

	public static Uri getUriFromString(String path) {
		Uri uri;
		if(path.isEmpty()) {
			uri = Uri.parse("android.resource://ee.app.conversa/" + R.drawable.business_default);
		} else {
			try {
				new URL(path);
				uri = Uri.parse(path);
			} catch (MalformedURLException e) {
				uri = Uri.fromFile(new File(path));
			}
		}

		return uri;
	}

	// As described in StackOverflow answer: http://stackoverflow.com/a/9274868/5349296
	public static int dpToPixels(Context context, int dp) {
		int afsd = (int) (dp * context.getResources().getDisplayMetrics().density);
		return afsd;
	}

	public static String getDate(Context context, long time) {
		Calendar cal = Calendar.getInstance(Locale.getDefault());
		cal.setTimeInMillis(time);
		String date = DateFormat.format("MM/yyyy", cal).toString();
		return context.getString(R.string.member_since, date);
	}

	public static File getMediaDirectory(Context context) throws Exception {
		ContextWrapper cw = new ContextWrapper(context);
		// path to /data/data/yourapp/app_data/imageDir
		// Create the File where the photo should go //
		// External sdcard location
		File directory = cw.getDir("avatars", Context.MODE_PRIVATE);

		if (directory == null) {
			throw new Exception("Failed to get media directory");
		}

		// Create the storage directory if it does not exist
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				Logger.error("getMediaDirectory", "Oops! Failed create "
						+ directory.getAbsolutePath() + " directory");

				throw new Exception("Failed to get media directory");
			}
		}

		return directory;
	}

	public static String getResourceName(File mediaDirectory) {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
				Locale.getDefault()).format(new Date(System.currentTimeMillis()));

		return mediaDirectory.getPath() + File.separator
				+ "IMG_" + timeStamp + ".jpg";
	}

	public static void saveToInternalStorage(Context context, Bitmap bitmapImage, long id) {
		new AsyncTask<Object, Void, Bitmap>() {
			@Override
			protected Bitmap doInBackground(Object... params) {
				if (params.length == 0) {
					return null;
				}

				Bitmap bitmap = (Bitmap) params[0];
				Context context = (Context) params[1];

				if (bitmap == null || context == null) {
					return null;
				}

				String path;

				try {
					path = Utils.getResourceName(Utils.getMediaDirectory(context));
					// Create imageDir
					File mypath = new File(path);

					FileOutputStream fos = new FileOutputStream(mypath);
					// Use the compress method on the BitMap object to write image to the OutputStream
					bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
					fos.close();
				} catch (Exception e) {
					Logger.error("saveToInternalStorage", e.getMessage());
					return null;
				}

				long id = (long) params[2];

				if (id != -1) {
					// Update contact url
					ConversaApp.getInstance(context).getDB().updateContactAvatar(id, path);
				}

				return bitmap;
			}

			@Override
			protected void onPostExecute(Bitmap bitmap) {

			}
		}.execute(bitmapImage, context, id);
	}

}