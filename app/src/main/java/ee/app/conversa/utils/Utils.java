package ee.app.conversa.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.AnyRes;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.onesignal.OneSignal;

import org.greenrobot.eventbus.EventBus;
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
import ee.app.conversa.contact.ContactUpdateReason;
import ee.app.conversa.events.contact.ContactUpdateEvent;

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
		String pattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*\\W).{6,}$";
		return Pattern.compile(pattern).matcher(password).matches();
	}

	public static void subscribeToTags(String channelName) {
		JSONObject tags = new JSONObject();
		try {
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
		if (TextUtils.isEmpty(path)) {
			return null;
		} else {
			try {
				new URL(path);
				return Uri.parse(path);
			} catch (MalformedURLException e) {
				File file = new File(path);
				if (file.exists()) {
					return Uri.fromFile(file);
				} else {
					return null;
				}
			}
		}
	}

	/**
	 * Get uri to any resource type [from: http://stackoverflow.com/a/36062748/5349296]
	 * @param context - context
	 * @param resId - resource id
	 * @throws Resources.NotFoundException if the given ID does not exist.
	 * @return - Uri to resource by given id
	 */
	public static Uri getDefaultImage(@NonNull Context context, @AnyRes int resId)
			throws Resources.NotFoundException, NullPointerException {
		/** Return a Resources instance for your application's package. */
		Resources res = context.getResources();
		/**
		 * Creates a Uri which parses the given encoded URI string.
		 * @param uriString an RFC 2396-compliant, encoded URI
		 * @throws NullPointerException if uriString or context is null
		 * @throws Resources.NotFoundException if resId couldn't be found
		 * @return Uri for this given uri string
		 */
		return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
				"://" + res.getResourcePackageName(resId)
				+ '/' + res.getResourceTypeName(resId)
				+ '/' + res.getResourceEntryName(resId));
	}

	// As described in StackOverflow answer: http://stackoverflow.com/a/9274868/5349296
	public static int dpToPixels(Context context, int dp) {
		return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
	}

	public static int dpToPixels(Context context, float dp) {
		return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
//		return  (int) TypedValue.applyDimension(
//				TypedValue.COMPLEX_UNIT_DIP,
//				dp,
//				context.getResources().getDisplayMetrics()
//		);
	}

	public static int dpFromPx(final Context context, final int px) {
		return Math.round((px - 0.5f) / context.getResources().getDisplayMetrics().density);
	}

	public static String getTimeOrDay(Context context, long time, boolean onlyDay) {
		return getDateOrTime(context, time, false, false, onlyDay);
	}

	public static String getDate(Context context, long time, boolean withYear) {
		return getDateOrTime(context, time, true, withYear, false);
	}

	private static String getDateOrTime(Context context, long time, boolean isDate,
										boolean withYear, boolean onlyDay)
	{
		Calendar cal = Calendar.getInstance(Locale.getDefault());
		cal.setTimeInMillis(time);

		if (isDate) {
			if (withYear) {
				return DateFormat.format("dd/MM/yyyy", cal).toString();
			} else {
				return DateFormat.format("dd/MM", cal).toString();
			}
		} else {
			if (onlyDay) {
				switch (cal.get(Calendar.DAY_OF_WEEK)) {
					case Calendar.SUNDAY:
						return context.getString(R.string.chat_date_sunday);
					case Calendar.MONDAY:
						return context.getString(R.string.chat_date_monday);
					case Calendar.TUESDAY:
						return context.getString(R.string.chat_date_tuesday);
					case Calendar.WEDNESDAY:
						return context.getString(R.string.chat_date_wednesday);
					case Calendar.THURSDAY:
						return context.getString(R.string.chat_date_thursday);
					case Calendar.FRIDAY:
						return context.getString(R.string.chat_date_friday);
					case Calendar.SATURDAY:
						return context.getString(R.string.chat_date_saturday);
					default:
						return DateFormat.format("HH:mm", cal).toString();
				}
			} else {
				return DateFormat.format("HH:mm", cal).toString();
			}
		}
	}

	public static File getMediaDirectory(Context context, String folder) throws Exception {
		ContextWrapper cw = new ContextWrapper(context);
		// path to /data/data/yourapp/app_data/imageDir
		// Create the File where the photo should go //
		// External sdcard location
		File directory = cw.getDir(folder, Context.MODE_PRIVATE);

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

	public static void saveAvatarToInternalStorage(Context context, Bitmap bitmap, long id) {
		if (context == null || bitmap == null || id < 0) {
			return;
		}

		String path;

		try {
			path = Utils.getResourceName(Utils.getMediaDirectory(context, "avatars"));
			// Create imageDir
			File mypath = new File(path);

			FileOutputStream fos = new FileOutputStream(mypath);
			// Use the compress method on the BitMap object to write image to the OutputStream
			bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
			fos.close();
		} catch (Exception e) {
			Logger.error("saveAvatarToInternalStorage", e.getMessage());
			return;
		}

		// Update contact url
		ConversaApp.getInstance(context).getDB().updateContactAvatar(id, path);
	}

	public static void saveAvatarToInternalStorageAsync(Context context, Bitmap bitmapImage, long id) {
		new AsyncTask<Object, Void, Bitmap>() {
			@Override
			protected Bitmap doInBackground(Object... params) {
				if (params.length == 0) {
					return null;
				}

				Bitmap bitmap = (Bitmap) params[0];
				Context context = (Context) params[1];
				long id = (long) params[2];

				if (context == null || bitmap == null || id < 0) {
					return null;
				}

				String path;

				try {
					path = Utils.getResourceName(Utils.getMediaDirectory(context, "avatars"));
					// Create imageDir
					File mypath = new File(path);

					FileOutputStream fos = new FileOutputStream(mypath);
					// Use the compress method on the BitMap object to write image to the OutputStream
					bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
					fos.close();
				} catch (Exception e) {
					Logger.error("saveAvatarToInternalStorageAsync", e.getMessage());
					return null;
				}

				// Update contact url
				ConversaApp.getInstance(context).getDB().updateContactAvatar(id, path);
				EventBus.getDefault().post(new ContactUpdateEvent(
						ConversaApp.getInstance(context).getDB().getContactById(id),
						ContactUpdateReason.AVATAR_DOWNLOAD
				));

				return bitmap;
			}
		}.execute(bitmapImage, context, id);
	}

	@WorkerThread
	public static String saveImageToInternalStorage(Context context, Bitmap bitmap, long id) {
		if (context == null || bitmap == null || id < 0) {
			return "";
		}

		String path;

		try {
			path = Utils.getResourceName(Utils.getMediaDirectory(context, "images"));
			// Create imageDir
			File mypath = new File(path);

			FileOutputStream fos = new FileOutputStream(mypath);
			// Use the compress method on the BitMap object to write image to the OutputStream
			bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
			fos.close();
		} catch (Exception e) {
			Logger.error("saveImageToInternalStorage", e.getMessage());
			return "";
		}

		// Update contact url
		ConversaApp.getInstance(context).getDB().updateLocalUrl(id, path);

		return path;
	}

}