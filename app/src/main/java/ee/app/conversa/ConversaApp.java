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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.StrictMode;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;
import com.birbit.android.jobqueue.log.CustomLogger;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.flurry.android.FlurryAgent;
import com.onesignal.OneSignal;
import com.taplytics.sdk.Taplytics;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.CookieManager;
import java.util.concurrent.TimeUnit;

import ee.app.conversa.database.MySQLiteHelper;
import ee.app.conversa.events.MyEventBusIndex;
import ee.app.conversa.management.AblyConnection;
import ee.app.conversa.settings.Preferences;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Foreground;
import ee.app.conversa.utils.Logger;
import io.branch.referral.Branch;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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

		initializeFastNetworking();
		initializeBranch();
		initializeDeveloperBuild();
		initializeJobManager();
		initializeEventBus();
        initializeFlurry();
		initializeTypefaces();
		initializeTaplytics();
		initializeOneSignal();
	}

	private void initializeFastNetworking() {
//		final Interceptor interceptor = new Interceptor() {
//			@Override
//			public void intercept(Chain chain) throws IOException  {
//				Request requestFacade = chain.request();
//				requestFacade.addHeader("User-Agent", "Conversa Android App");
//
//				if (!Util.getSsoToken().isEmpty()) {
//					setupAuthCookie(cookieManager);
//
//					if (BuildConstants.SERVER_URL.contains("qa.conversa.com") ) {
//						requestFacade.addHeader("Authorization", "Basic " + Base64.encodeToString("dev:skyroar".getBytes(), 0));
//					} else if (BuildConstants.SERVER_URL.contains("demo.taskeasy.com") ||
//							BuildConstants.SERVER_URL.contains("test.conversa.com") ||
//							BuildConstants.SERVER_URL.contains("staging.conversa.com")) {
//						requestFacade.addHeader("Authorization", "Basic " + Base64.encodeToString("demo:yellowpig".getBytes(), 0));
//					}
//				}
//			}
//		};

//		final Profiler profiler = new Profiler() {
//			@Override
//			public Object beforeCall() {
//				return null;
//			}
//
//			@Override
//			public void afterCall(final RequestInformation requestInfo, final long elapsedTime, final int statusCode, final Object beforeCallData) {
////				taskEasyApplication.getTracker(TrackerName.APP_TRACKER)
////						.send(new HitBuilders.TimingBuilder()
////								.setCategory(requestInfo.getMethod() + " Timings")
////								.setValue(elapsedTime)
////								.setVariable(requestInfo.getRelativePath())
////								.setLabel(String.valueOf(statusCode))
////								.build());
//			}
//		};

		OkHttpClient client = new OkHttpClient().newBuilder()
				.connectTimeout(30, TimeUnit.SECONDS)
				.readTimeout(1, TimeUnit.MINUTES)
				.build();

		AndroidNetworking.initialize(getApplicationContext(), client);
//		AndroidNetworking.setParserFactory(new JacksonParserFactory());
	}

	private void initializeBranch() {
		Branch.getAutoInstance(this);
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
		EventBus.builder()
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

	private void initializeTaplytics() {
		Taplytics.startTaplytics(this, "1a214e395c9db615a2cf2819a576bd9f17372ca5");
	}

	private void initializeOneSignal() {
		OneSignal.startInit(this)
				.inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
				.unsubscribeWhenNotificationsAreDisabled(true)
				.init();
	}

	private void setupAuthCookie(final CookieManager cookieManager) {
//		for (final HttpCookie httpCookie : cookieManager.getCookieStore().getCookies()) {
//			if (httpCookie.getName().equalsIgnoreCase(Constants.SSO_TICKET)) return;
//		}
//
//		final HttpCookie cookie = new HttpCookie(Constants.SSO_TICKET, Util.getSsoToken());
//		cookie.setDomain(BuildConstants.SERVER_DOMAIN);
//		cookie.setPath("/");
//		cookie.setMaxAge(2592000);
//		cookieManager.getCookieStore().add(URI.create(BuildConstants.SERVER_DOMAIN), cookie);
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

	class LoggingInterceptor implements Interceptor {
		@SuppressLint("DefaultLocale")
		@Override public Response intercept(Interceptor.Chain chain) throws IOException {
			Request request = chain.request();

			long t1 = System.nanoTime();
			Logger.error("LoggingInterceptor", String.format("Sending request %s on %s%n%s",
					request.url(), chain.connection(), request.headers()));

			Response response = chain.proceed(request);

			long t2 = System.nanoTime();
			Logger.error("LoggingInterceptor", String.format("Received response for %s in %.1fms%n%s",
					response.request().url(), (t2 - t1) / 1e6d, response.headers()));

			return response;
		}
	}

}
