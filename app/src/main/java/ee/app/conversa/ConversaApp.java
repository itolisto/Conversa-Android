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

import android.app.Application;
import android.graphics.Typeface;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.onesignal.OneSignal;
import com.parse.Parse;
import com.parse.ParseObject;

import java.io.File;

import ee.app.conversa.database.MySQLiteHelper;
import ee.app.conversa.management.ably.Connection;
import ee.app.conversa.management.FileManager;
import ee.app.conversa.model.parse.Account;
import ee.app.conversa.model.parse.Business;
import ee.app.conversa.model.parse.BusinessCategory;
import ee.app.conversa.model.parse.BusinessOptions;
import ee.app.conversa.model.parse.Customer;
import ee.app.conversa.model.parse.Options;
import ee.app.conversa.model.parse.bCategory;
import ee.app.conversa.model.parse.pMessage;
import ee.app.conversa.notifications.onesignal.CustomNotificationOpenedHandler;
import ee.app.conversa.notifications.onesignal.CustomNotificationReceivedHandler;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Foreground;
import ee.app.conversa.utils.Preferences;

/**
 * Basic Application class, holds references to often used single instance
 * objects and methods related to application like application background check.
 */

public class ConversaApp extends Application {

	private static Typeface mTfRalewayThin;
    private static Typeface mTfRalewayLight;
    private static Typeface mTfRalewayRegular;
    private static Typeface mTfRalewayMedium;
	private static Typeface mTfRalewayBold;
	private static MySQLiteHelper mDb;
	private static Preferences mPreferences;
	private static LocalBroadcastManager mLocalBroadcastManager;

	/**
	 * Called when the application is starting, before any other application objects have been created
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		Foreground.init(this);
		setDB();
		setPreferences();
		setLocalBroadcastManager();

		Fresco.initialize(this);
		Connection.initAblyManager(this);
		OneSignal
				// Initializes OneSignal to register the device for push notifications
				.startInit(this)
				// Prompts the user for location permissions. This allows for geotagging so you can
				// send notifications to users based on location.
				.autoPromptLocation(true)
				// How OneSignal notifications will be shown when one is received while your app is
				// in focus
				.inFocusDisplaying(OneSignal.OSInFocusDisplayOption.None)
				// Sets a notification opened handler. The instance will be called when a notification
				// is tapped on from the notification shade or when closing an Alert notification
				// shown in the app.
				.setNotificationOpenedHandler(new CustomNotificationOpenedHandler(this))
				// Sets a notification received handler. The instance will be called when a
				// notification is received whether it was displayed or not.
				.setNotificationReceivedHandler(new CustomNotificationReceivedHandler(this))
				// Initializes OneSignal to register the device for push notifications
				.init();

		Parse.enableLocalDatastore(this);

		// Register subclassing for using as Parse objects
		ParseObject.registerSubclass(Options.class);
		ParseObject.registerSubclass(Account.class);
		ParseObject.registerSubclass(bCategory.class);
		ParseObject.registerSubclass(Business.class);
		ParseObject.registerSubclass(Customer.class);
		ParseObject.registerSubclass(pMessage.class);
		ParseObject.registerSubclass(BusinessCategory.class);
		ParseObject.registerSubclass(BusinessOptions.class);

		// Initialize Parse.
		Parse.initialize(this, "39H1RFC1jalMV3cv8pmDGPRh93Bga1mB4dyxbLwl", "YC3vORNGt6I4f8yEsO6TyGF97XbmitofOrrS5PCC");

//		You need to enable the local datastore inside your initialization command, not before like it used to be.
//		Parse.initialize(new Parse.Configuration.Builder(this)
//			.applicationId("yourappid")
//			.clientKey("yourclientkey")
//			.server("serverurl")
//			.enableLocalDataStore()
//			.build()
//		);

		//Crea las tipografias
		setTfRalewayThin(Typeface.createFromAsset(getAssets(), Const.ROBOTO + "Roboto-Thin.ttf"));
		setTfRalewayLight(Typeface.createFromAsset(getAssets(), Const.ROBOTO + "Roboto-Light.ttf"));
		setTfRalewayRegular(Typeface.createFromAsset(getAssets(), Const.ROBOTO + "Roboto-Regular.ttf"));
		setTfRalewayMedium(Typeface.createFromAsset(getAssets(), Const.ROBOTO + "Roboto-Medium.ttf"));
		setTfRalewayBold(Typeface.createFromAsset(getAssets(), Const.ROBOTO + "Roboto-Bold.ttf"));

		File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "MyCache");
		Log.d(this.getClass().getSimpleName(), FileManager.storageSize(file.getTotalSpace())); // dumps "12.9 GB" for me
		Log.d(this.getClass().getSimpleName(), FileManager.storageSize(file.getFreeSpace())); // dumps "10.6 GB" for me
	}

	/* ************************************************************************************************ */
	public static Preferences getPreferences() { return mPreferences; }
	public static LocalBroadcastManager getLocalBroadcastManager() { return mLocalBroadcastManager; }
	public static MySQLiteHelper getDB() { return mDb; }

    private void setPreferences() {
		mPreferences = new Preferences(this);
	}

	private void setLocalBroadcastManager() {
		mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
	}

	private void setDB() {
		mDb = new MySQLiteHelper(this);
	}

    /* ************************************************************************************************ */
	public static Typeface getTfRalewayThin() { return mTfRalewayThin; }
    public static Typeface getTfRalewayLight() { return mTfRalewayLight; }
    public static Typeface getTfRalewayRegular() { return mTfRalewayRegular; }
    public static Typeface getTfRalewayMedium() { return mTfRalewayMedium; }
    public static Typeface getTfRalewayBold() { return mTfRalewayBold; }

	private void setTfRalewayThin(Typeface tfRaleway) { mTfRalewayThin = tfRaleway; }
    private void setTfRalewayLight(Typeface tfRaleway) { mTfRalewayLight = tfRaleway; }
    private void setTfRalewayRegular(Typeface tfRaleway) { mTfRalewayRegular = tfRaleway; }
    private void setTfRalewayMedium(Typeface tfRaleway) { mTfRalewayMedium = tfRaleway; }
    private void setTfRalewayBold(Typeface tfRaleway) { mTfRalewayBold = tfRaleway; }

}
