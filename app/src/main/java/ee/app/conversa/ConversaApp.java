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
import android.media.SoundPool;
import android.os.StrictMode;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;
import com.birbit.android.jobqueue.log.CustomLogger;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.github.stkent.bugshaker.BugShaker;
import com.github.stkent.bugshaker.flow.dialog.AlertDialogType;
import com.onesignal.OneSignal;
import com.parse.Parse;
import com.parse.ParseObject;

import org.greenrobot.eventbus.EventBus;

import ee.app.conversa.database.MySQLiteHelper;
import ee.app.conversa.events.MyEventBusIndex;
import ee.app.conversa.management.AblyConnection;
import ee.app.conversa.model.parse.Account;
import ee.app.conversa.model.parse.Business;
import ee.app.conversa.model.parse.BusinessCategory;
import ee.app.conversa.model.parse.Customer;
import ee.app.conversa.model.parse.bCategory;
import ee.app.conversa.model.parse.pMessage;
import ee.app.conversa.notifications.onesignal.CustomNotificationOpenedHandler;
import ee.app.conversa.notifications.onesignal.CustomNotificationReceivedHandler;
import ee.app.conversa.settings.Preferences;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Foreground;
import io.branch.referral.Branch;
import io.fabric.sdk.android.Fabric;

/**
 * Basic Application class, holds references to often used single instance
 * objects and methods related to application like application background check.
 */
public class ConversaApp extends MultiDexApplication {

	private SoundPool soundsSent;
	private SoundPool soundsReceived;
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

		initializeFabric();
		initializeBranch();
		initializeOneSignal();
		initializeParse();
		initializeDeveloperBuild();
		initializeJobManager();
		initializeEventBus();
		initializeBugShaker();
		initializeTypefaces();

//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//			AudioAttributes attributes = new AudioAttributes.Builder()
//					.setUsage(AudioAttributes.USAGE_NOTIFICATION)
//					.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
//					.build();
//			soundsSent = new SoundPool.Builder()
//					.setAudioAttributes(attributes)
//					.build();
//			soundsReceived = new SoundPool.Builder()
//					.setAudioAttributes(attributes)
//					.build();
//		} else {
//			soundsSent = new SoundPool(15, AudioManager.STREAM_NOTIFICATION, 0);
//			soundsReceived = new SoundPool(15, AudioManager.STREAM_NOTIFICATION, 0);
//		}
//
//		soundsSent.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
//			@Override
//			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
//				/** soundId for Later handling of sound pool **/
//				// in 2nd param u have to pass your desire ringtone
//				int soundId;
//
//				if (sound) {
//					soundId = sounds.load(getApplicationContext(), R.raw.message_sent, 1);
//				} else {
//					soundId = sounds.load(getApplicationContext(), R.raw.message_received, 1);
//				}
//
//				sounds.play(soundId, 0.99f, 0.99f, 1, 0, 0.99f);
//			}
//		});
	}

	private void initializeFabric() {
		Fabric.with(this, new Crashlytics());
		Fabric.with(this, new Answers());
	}

	private void initializeBranch() {
		Branch.getAutoInstance(this);
	}

	private void initializeOneSignal() {
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
	}

	private void initializeParse() {
		// Register subclassing for using as Parse objects
		ParseObject.registerSubclass(Account.class);
		ParseObject.registerSubclass(bCategory.class);
		ParseObject.registerSubclass(Business.class);
		ParseObject.registerSubclass(Customer.class);
		ParseObject.registerSubclass(pMessage.class);
		ParseObject.registerSubclass(BusinessCategory.class);

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

//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//			builder.scheduler(FrameworkJobSchedulerService.createSchedulerFor(this,
//					MyJobService.class), true);
//		} else {
//			int enableGcm = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
//			if (enableGcm == ConnectionResult.SUCCESS) {
//				builder.scheduler(GcmJobSchedulerService.createSchedulerFor(this,
//						MyGcmJobService.class), true);
//			}
//		}

		jobManager = new JobManager(builder.build());
	}

	private void initializeEventBus() {
		EventBus
				.builder()
				.addIndex(new MyEventBusIndex())
				.throwSubscriberException(BuildConfig.DEV_BUILD).installDefaultEventBus();
	}

	private void initializeBugShaker() {
		BugShaker.get(this)
				.setEmailAddresses("appconversa@gmail.com")   // required
				.setEmailSubjectLine("Bug reported") // optional
				.setAlertDialogType(AlertDialogType.NATIVE) // optional
				.setLoggingEnabled(BuildConfig.DEV_BUILD)   // optional
				.setIgnoreFlagSecure(true)                  // optional
				.assemble()                                 // required
				.start();                                   // required
	}

	private void initializeTypefaces() {
		setTfRalewayThin(Typeface.createFromAsset(getAssets(), Const.ROBOTO + "Roboto-Thin.ttf"));
		setTfRalewayLight(Typeface.createFromAsset(getAssets(), Const.ROBOTO + "Roboto-Light.ttf"));
		setTfRalewayRegular(Typeface.createFromAsset(getAssets(), Const.ROBOTO + "Roboto-Regular.ttf"));
		setTfRalewayMedium(Typeface.createFromAsset(getAssets(), Const.ROBOTO + "Roboto-Medium.ttf"));
		setTfRalewayBold(Typeface.createFromAsset(getAssets(), Const.ROBOTO + "Roboto-Bold.ttf"));
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

    /* ************************************************************************************************ */

	private void setTfRalewayThin(Typeface tfRaleway) { mTfRalewayThin = tfRaleway; }
    private void setTfRalewayLight(Typeface tfRaleway) { mTfRalewayLight = tfRaleway; }
    private void setTfRalewayRegular(Typeface tfRaleway) { mTfRalewayRegular = tfRaleway; }
    private void setTfRalewayMedium(Typeface tfRaleway) { mTfRalewayMedium = tfRaleway; }
    private void setTfRalewayBold(Typeface tfRaleway) { mTfRalewayBold = tfRaleway; }

}
