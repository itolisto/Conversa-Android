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

package ee.app.conversa;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import com.crashlytics.android.Crashlytics;
import com.parse.Parse;
import com.parse.ParseObject;

import java.util.List;

import ee.app.conversa.management.MySQLiteHelper;
import ee.app.conversa.model.Parse.Account;
import ee.app.conversa.model.Parse.Business;
import ee.app.conversa.model.Parse.BusinessCategory;
import ee.app.conversa.model.Parse.BusinessOptions;
import ee.app.conversa.model.Parse.Contact;
import ee.app.conversa.model.Parse.Customer;
import ee.app.conversa.model.Parse.Favorite;
import ee.app.conversa.model.Parse.Message;
import ee.app.conversa.model.Parse.Options;
import ee.app.conversa.model.Parse.PopularSearch;
import ee.app.conversa.model.Parse.bCategory;
import ee.app.conversa.sendbird.SendBirdController;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Preferences;
import io.fabric.sdk.android.Fabric;

/**
 * Basic Application class, holds references to often used single instance
 * objects and methods related to application like application background check.
 */

public class ConversaApp extends Application {

	private static ConversaApp sInstance;
    private MySQLiteHelper mDb;
	private Typeface mTfRalewayThin;
    private Typeface mTfRalewayLight;
    private Typeface mTfRalewayRegular;
    private Typeface mTfRalewayMedium;
	private Typeface mTfRalewayBold;
	private Preferences mPreferences;
	private LocalBroadcastManager mLocalBroadcastManager;

	public static ConversaApp getInstance() { return sInstance; }

	/**
	 * Called when the application is starting, before any other application objects have been created
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		Fabric.with(this, new Crashlytics());
        sInstance = this;
        mDb = new MySQLiteHelper( this );
		setPreferences( new Preferences(this) );

		// Register subclassing for using as Parse objects
		ParseObject.registerSubclass(Options.class);
		ParseObject.registerSubclass(Account.class);
		ParseObject.registerSubclass(bCategory.class);
		ParseObject.registerSubclass(Business.class);
		ParseObject.registerSubclass(Contact.class);
		ParseObject.registerSubclass(Customer.class);
		ParseObject.registerSubclass(Favorite.class);
		ParseObject.registerSubclass(Message.class);
		ParseObject.registerSubclass(PopularSearch.class);
		ParseObject.registerSubclass(BusinessCategory.class);
		ParseObject.registerSubclass(BusinessOptions.class);

		// [Optional] Power your app with Local Datastore. For more info, go to
		// https://parse.com/docs/ios/guide#local-datastore
		Parse.enableLocalDatastore(this);

		// Initialize Parse.
		Parse.initialize(this, "39H1RFC1jalMV3cv8pmDGPRh93Bga1mB4dyxbLwl", "YC3vORNGt6I4f8yEsO6TyGF97XbmitofOrrS5PCC");

//		Parse.initialize(new Parse.Configuration.Builder(this)
//				.applicationId("39H1RFC1jalMV3cv8pmDGPRh93Bga1mB4dyxbLwl")
////				.server("http://YOUR_PARSE_SERVER:1337/parse")
////				.build()
//		);

		setLocalBroadcastManager( LocalBroadcastManager.getInstance(this) );

		//Crea las tipografias
		setTfRalewayThin( Typeface.createFromAsset(getAssets(), Const.ROBOTO + "Roboto-Thin.ttf") );
		setTfRalewayLight( Typeface.createFromAsset(getAssets(), Const.ROBOTO + "Roboto-Light.ttf") );
        setTfRalewayRegular( Typeface.createFromAsset(getAssets(), Const.ROBOTO + "Roboto-Regular.ttf") );
        setTfRalewayMedium( Typeface.createFromAsset(getAssets(), Const.ROBOTO + "Roboto-Medium.ttf") );
        setTfRalewayBold( Typeface.createFromAsset(getAssets(), Const.ROBOTO + "Roboto-Bold.ttf") );

		SendBirdController.init();
		
		//Iniciar apropiadamente los valores por defecto de la
		//aplicacion. Es necesario ya que la aplicacion podria
		//necesitar leer los ajustes para comportarse de cierta manera
        //PreferenceManager.setDefaultValues(this, R.layout.fragment_settings, false);
	}

    /* ************************************************************************************************ */
	/* **********************************PREFERENCES/FILE/BROADCAST INIT******************************* */
	/* ************************************************************************************************ */
	public static Preferences getPreferences() { return sInstance.mPreferences; }
    public static LocalBroadcastManager getLocalBroadcastManager() { return sInstance.mLocalBroadcastManager; }
    public static MySQLiteHelper getDB(){ return sInstance.mDb; }

    private void setPreferences(Preferences preferences) { mPreferences = preferences; }
	private void setLocalBroadcastManager(LocalBroadcastManager localBroadcastManager) {
		mLocalBroadcastManager = localBroadcastManager;
	}

    /* ************************************************************************************************ */
	/* *********************************************FONTS********************************************** */
	/* ************************************************************************************************ */
	public static Typeface getTfRalewayThin() { return sInstance.mTfRalewayThin; }
    public static Typeface getTfRalewayLight() { return sInstance.mTfRalewayLight; }
    public static Typeface getTfRalewayRegular() { return sInstance.mTfRalewayRegular; }
    public static Typeface getTfRalewayMedium() { return sInstance.mTfRalewayMedium; }
    public static Typeface getTfRalewayBold() { return sInstance.mTfRalewayBold; }

	private void setTfRalewayThin(Typeface tfRaleway) { mTfRalewayThin = tfRaleway; }
    private void setTfRalewayLight(Typeface tfRaleway) { mTfRalewayLight = tfRaleway; }
    private void setTfRalewayRegular(Typeface tfRaleway) { mTfRalewayRegular = tfRaleway; }
    private void setTfRalewayMedium(Typeface tfRaleway) { mTfRalewayMedium = tfRaleway; }
    private void setTfRalewayBold(Typeface tfRaleway) { mTfRalewayBold = tfRaleway; }

    /* ************************************************************************************************ */
	/* **********************************************UTILS********************************************* */
	/* ************************************************************************************************ */
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
	public static boolean hasNetworkConnection() {
		final ConnectivityManager connectivityManager = (ConnectivityManager) sInstance
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo[] netInfo = connectivityManager.getAllNetworkInfo();
		for (NetworkInfo ni : netInfo) {
			if (ni.getTypeName().equalsIgnoreCase("WIFI")) {
                if (ni.isConnected()) {
                    //Logger.error("internet", "wifi on");
                    return true;
                }
            }
			if (ni.getTypeName().equalsIgnoreCase("MOBILE")) {
                if (ni.isConnected()) {
                    //Logger.error("internet", "mobile on");
                    return true;
                }
            }
		}
		return false;
	}
}
