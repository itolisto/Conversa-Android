package ee.app.conversa.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import androidx.annotation.AnyRes;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import ee.app.conversa.ConversaApp;
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

	public static String numberWithFormat(int number) {
		if (number > 999999) {
			return String.format(Locale.getDefault(), "%.1fM", number/1000000.0).replace(',', '.');
		} else if (number > 999) {
			return String.format(Locale.getDefault(), "%.1fK", number/1000.0).replace(',', '.');
		} else {
			return String.valueOf(number);
		}
	}

	public static int checkDate(int year, int month, int day) {
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.set(year, month, day);

			Calendar today = Calendar.getInstance();
			int age = today.get(Calendar.YEAR) - calendar.get(Calendar.YEAR);

			if (today.get(Calendar.DAY_OF_YEAR) < calendar.get(Calendar.DAY_OF_YEAR)){
				age--;
			}

			if (age >= 13) {
				return 0;
			}

			return 2;
		} catch (NullPointerException|IllegalArgumentException e) {
			return 1;
		}
	}

    public static boolean checkEmail(String email) {
		return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

	public static boolean checkPassword(String password) {
		String pattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*\\W).{6,}$";
		return Pattern.compile(pattern).matcher(password).matches();
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
						return DateFormat.format("KK:mm a", cal).toString();
				}
			} else {
				return DateFormat.format("KK:mm a", cal).toString();
			}
		}
	}

	public static boolean deleteFile(File file) {
		boolean deletedAll = true;
		if (file != null) {
			if (file.isDirectory()) {
				String[] children = file.list();
				for (int i = 0; i < children.length; i++) {
					deletedAll = deleteFile(new File(file, children[i])) && deletedAll;
				}
			} else {
				deletedAll = file.delete();
			}
		}

		return deletedAll;
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