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

import android.content.Context;
import android.graphics.Typeface;
import android.os.StrictMode;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;
import com.birbit.android.jobqueue.log.CustomLogger;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.flurry.android.FlurryAgent;
import com.parse.Parse;
import com.parse.ParseObject;

import org.greenrobot.eventbus.EventBus;

import ee.app.conversa.database.MySQLiteHelper;
import ee.app.conversa.events.MyEventBusIndex;
import ee.app.conversa.management.AblyConnection;
import ee.app.conversa.model.parse.Account;
import ee.app.conversa.model.parse.Business;
import ee.app.conversa.settings.Preferences;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Foreground;
import io.branch.referral.Branch;

/**
 * Basic Application class, holds references to often used single instance
 * objects and methods related to application like application background check.
 */
public class ConversaApp extends MultiDexApplication {

	private JobManager jobManager;
	private Typeface mTfRalewayThin;
    private Typeface mTfRalewayLight;
    private Typeface mTfRalewayRegular;
    private Typeface mTfRalewayMedium;
	private Typeface mTfRalewayBold;
	private MySQLiteHelper mDb;
	private Preferences mPreferences;
	private LocalBroadcastManager mLocalBroadcastManager;

	public static ConversaApp getInstance(Context context) {
		return (ConversaApp)context.getApplicationContext();
	}

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
		AblyConnection.initAblyManager(this);
		AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

		initializeBranch();
		initializeParse();
		initializeDeveloperBuild();
		initializeJobManager();
		initializeEventBus();
        initializeFlurry();
		initializeTypefaces();
	}

	private void initializeBranch() {
		Branch.getAutoInstance(this);
	}

	private void initializeParse() {
		// Register subclassing for using as Parse objects
		ParseObject.registerSubclass(Account.class);
		ParseObject.registerSubclass(Business.class);

		// Initialize Parse.
		Parse.initialize(new Parse.Configuration.Builder(this)
//				.applicationId("szLKzjFz66asK9SngeFKnTyN2V596EGNuMTC7YyF4tkFudvY72")
//				.clientKey("CMTFwQPd2wJFXfEQztpapGHFjP5nLZdtZr7gsHKxuFhA9waMgw1")
//				.server("http://ec2-52-71-125-28.compute-1.amazonaws.com:1337/parse/")
				// Localhost
				.applicationId("b15c83")
				.clientKey(null)
				.server("http://10.0.3.2:1337/parse/") // The trailing slash is important.
				.build()
		);
	}

	private void initializeDeveloperBuild() {
		if (BuildConfig.DEV_BUILD) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
					.detectAll()
					.penaltyLog()
					.build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
					.detectAll()
					.penaltyLog()
					.build());
		}
	}

	private void initializeJobManager() {
		Configuration.Builder builder = new Configuration.Builder(this)
				.customLogger(new CustomLogger() {
					private static final String TAG = "JobManager";
					@Override
					public boolean isDebugEnabled() {
						// Make sure your isDebugEnabled returns false on production
						// to avoid unnecessary string generation.
						return BuildConfig.JOB_LOGGER;
					}

					@Override
					public void d(String text, Object... args) {
						//Log.e(TAG, String.format(text, args));
					}

					@Override
					public void e(Throwable t, String text, Object... args) {
						Log.e(TAG, String.format(text, args), t);
					}

					@Override
					public void e(String text, Object... args) {
						Log.e(TAG, String.format(text, args));
					}

					@Override
					public void v(String text, Object... args) {
						//Log.e(TAG, String.format(text, args));
					}
				})
				.id("ConversaAppJobs")
				.minConsumerCount(1)//always keep at least one consumer alive
				.maxConsumerCount(3)//up to 3 consumers at a time
				.loadFactor(3)//3 jobs per consumer
				.consumerKeepAlive(120);//wait 2 minute

		jobManager = new JobManager(builder.build());
	}

	private void initializeEventBus() {
		EventBus
				.builder()
				.addIndex(new MyEventBusIndex())
				.throwSubscriberException(BuildConfig.DEV_BUILD).installDefaultEventBus();
	}

    private void initializeFlurry() {
        new FlurryAgent.Builder()
                .withLogEnabled(true)
                .build(this, "HNFDYJRY88CWYJ9464VB");
    }

	private void initializeTypefaces() {
		mTfRalewayThin = Typeface.createFromAsset(getAssets(), Const.ROBOTO + "Roboto-Thin.ttf");
		mTfRalewayLight = Typeface.createFromAsset(getAssets(), Const.ROBOTO + "Roboto-Light.ttf");
		mTfRalewayRegular = Typeface.createFromAsset(getAssets(), Const.ROBOTO + "Roboto-Regular.ttf");
		mTfRalewayMedium = Typeface.createFromAsset(getAssets(), Const.ROBOTO + "Roboto-Medium.ttf");
		mTfRalewayBold = Typeface.createFromAsset(getAssets(), Const.ROBOTO + "Roboto-Bold.ttf");
	}

	/* ************************************************************************************************ */
	private void setLocalBroadcastManager() {
		mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
	}

	private void setDB() {
		mDb = new MySQLiteHelper(this);
	}

	private void setPreferences() {
		mPreferences = new Preferences(this);
	}

	public LocalBroadcastManager getLocalBroadcastManager() {
		return mLocalBroadcastManager;
	}

	public synchronized MySQLiteHelper getDB() {
		return mDb;
	}

	public synchronized JobManager getJobManager() {
		return jobManager;
	}

	public synchronized Preferences getPreferences() {
		return mPreferences;
	}

	public Typeface getTfRalewayThin() {
		return mTfRalewayThin;
	}

	public Typeface getTfRalewayLight() {
		return mTfRalewayLight;
	}

	public Typeface getTfRalewayRegular() {
		return mTfRalewayRegular;
	}

	public Typeface getTfRalewayMedium() {
		return mTfRalewayMedium;
	}

	public Typeface getTfRalewayBold() {
		return mTfRalewayBold;
	}

}
