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
import android.support.v4.content.LocalBroadcastManager;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.onesignal.OneSignal;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;

import ee.app.conversa.database.MySQLiteHelper;
import ee.app.conversa.model.Parse.Account;
import ee.app.conversa.model.Parse.Business;
import ee.app.conversa.model.Parse.BusinessCategory;
import ee.app.conversa.model.Parse.BusinessOptions;
import ee.app.conversa.model.Parse.Customer;
import ee.app.conversa.model.Parse.Options;
import ee.app.conversa.model.Parse.bCategory;
import ee.app.conversa.model.Parse.pMessage;
import ee.app.conversa.utils.Const;
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
        mDb = new MySQLiteHelper(this);
		Fresco.initialize(this);
		OneSignal.startInit(this).init();
		setPreferences(new Preferences(this));
		setLocalBroadcastManager(LocalBroadcastManager.getInstance(this));

		// Register subclassing for using as Parse objects
		ParseObject.registerSubclass(Options.class);
		ParseObject.registerSubclass(Account.class);
		ParseObject.registerSubclass(bCategory.class);
		ParseObject.registerSubclass(Business.class);
		ParseObject.registerSubclass(Customer.class);
		ParseObject.registerSubclass(pMessage.class);
		ParseObject.registerSubclass(BusinessCategory.class);
		ParseObject.registerSubclass(BusinessOptions.class);

		// [Optional] Power your app with Local Datastore. For more info, go to
		// https://parse.com/docs/ios/guide#local-datastore
		Parse.enableLocalDatastore(this);

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

		ParseInstallation.getCurrentInstallation().saveEventually();

		//Crea las tipografias
		setTfRalewayThin( Typeface.createFromAsset(getAssets(), Const.ROBOTO + "Roboto-Thin.ttf") );
		setTfRalewayLight( Typeface.createFromAsset(getAssets(), Const.ROBOTO + "Roboto-Light.ttf") );
        setTfRalewayRegular( Typeface.createFromAsset(getAssets(), Const.ROBOTO + "Roboto-Regular.ttf") );
        setTfRalewayMedium( Typeface.createFromAsset(getAssets(), Const.ROBOTO + "Roboto-Medium.ttf") );
        setTfRalewayBold( Typeface.createFromAsset(getAssets(), Const.ROBOTO + "Roboto-Bold.ttf") );
	}

	/* ************************************************************************************************ */

	public static Preferences getPreferences() { return mPreferences; }
    public static LocalBroadcastManager getLocalBroadcastManager() { return mLocalBroadcastManager; }
    public static MySQLiteHelper getDB(){ return mDb; }

    private void setPreferences(Preferences preferences) { mPreferences = preferences; }
	private void setLocalBroadcastManager(LocalBroadcastManager localBroadcastManager) {
		mLocalBroadcastManager = localBroadcastManager;
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
