package ee.app.conversa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import ee.app.conversa.extendables.ConversaActivity;
import ee.app.conversa.management.SettingsManager;
import ee.app.conversa.model.Database.User;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Logger;
import ee.app.conversa.utils.PagerAdapter;

public class ActivityMain extends ConversaActivity {

    public static ActivityMain sInstance;
    private boolean mPushHandledOnNewIntent = false;
    private ViewPager mViewPager;
    private TabLayout tabLayout;
    private PagerAdapter mPagerAdapter;
    private String titles[];
    private int[] tabIcons = {
            R.drawable.chats_active,
            R.drawable.actuales_inactive,
            R.drawable.settings_inactive
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // ViewPager and its adapters use support library fragments, so use getSupportFragmentManager.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        titles = getResources().getStringArray(R.array.categories_titles);
        mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(mViewPager);

        // Initial state of tabs and titles
        getSupportActionBar().setTitle(titles[0]);
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
                int p = tab.getPosition();

                try {
                    getSupportActionBar().setTitle(titles[p]);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    supportInvalidateOptionsMenu();
                } catch (NullPointerException e) {
                    Logger.error(this.toString(), e.getMessage());
                }

                switch (p) {
                    case 2:
                        tab.setIcon(R.drawable.settings_active);
                        getSupportActionBar().setBackgroundDrawable(
                                new ColorDrawable(getResources().getColor(R.color.settings_tab)));

                        if (Build.VERSION.SDK_INT >= 23) {
                            tabLayout.setBackground(new ColorDrawable(getResources().getColor(R.color.settings_tab, null)));
                            mViewPager.setBackground(new ColorDrawable(getResources().getColor(R.color.settings_background, null)));
                        } else {
                            tabLayout.setBackgroundColor(getResources().getColor(R.color.settings_tab));
                            mViewPager.setBackgroundColor(getResources().getColor(R.color.settings_background));
                        }
                        break;
                    default:
                        if (p == 1) {
                            String title = ConversaApp.getPreferences().getCurrentCategoryTitle();
                            if (!title.isEmpty()) {
                                getSupportActionBar().setTitle(title);
                                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                            }

                            tab.setIcon(R.drawable.actuales_active);
                        } else {
                            tab.setIcon(R.drawable.chats_active);
                        }

                        getSupportActionBar().setBackgroundDrawable(
                                new ColorDrawable(getResources().getColor(R.color.regular_tabs))
                        );
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 0:
                        tab.setIcon(R.drawable.chats_inactive);
                        break;
                    case 1:
                        tab.setIcon(R.drawable.actuales_inactive);
                        break;
                    case 2:
                        if (Build.VERSION.SDK_INT >= 16) {
                            tabLayout.setBackground(new ColorDrawable(getResources().getColor(R.color.regular_tabs)));
                            mViewPager.setBackground(new ColorDrawable(getResources().getColor(R.color.normal_background)));
                        } else {
                            tabLayout.setBackgroundColor(getResources().getColor(R.color.regular_tabs));
                            mViewPager.setBackgroundColor(getResources().getColor(R.color.normal_background));
                        }
                        tab.setIcon(R.drawable.settings_inactive);

                        break;
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(Const.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    //mInformationTextView.setText(getString(R.string.gcm_send_message));
                } else {
                    //mInformationTextView.setText(getString(R.string.token_error_message));
                }
            }
        };

        /* QUITAR CON EMULADOR DE ECLIPSE*/
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        } else {
            Logger.error(TAG_GCM, "No valid Google Play Services APK found.");
        }
        sInstance = this;
	}
	
	@Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        mPushHandledOnNewIntent = false;
        if (getIntent().getBooleanExtra(Const.PUSH_INTENT, false)) {
            mPushHandledOnNewIntent = true;
            getIntent().removeExtra(Const.PUSH_INTENT);
            openWallFromNotification(intent);
        }
        super.onNewIntent(intent);
    }

    // You need to do the Play Services APK check here too.
    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Const.REGISTRATION_COMPLETE));

        if (!mPushHandledOnNewIntent) {
            if (getIntent().getBooleanExtra(Const.PUSH_INTENT, false)) {
                mPushHandledOnNewIntent = false;
                getIntent().removeExtra(Const.PUSH_INTENT);
                new Thread(new Runnable() {
                    public void run() {
                        openWallFromNotification(getIntent());
                    }
                }).start();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
    }

    @Override
    public void onBackPressed() {
        if(mViewPager.getCurrentItem() == 1) {
            if (mPagerAdapter.getItem(1) instanceof FragmentRoot) {
                ((FragmentRoot) mPagerAdapter.getItem(1)).backPressed();
                return;
            }
        }

        super.onBackPressed();
    }

    private void openWallFromNotification(Intent intent) {
        String fromUserId = intent.getStringExtra(Const.PUSH_FROM_USER_ID);
        User fromUser     = ConversaApp.getDB().isContact(fromUserId);

        if(fromUser == null) {
//            try {
//                fromUser = new ConversaAsyncTask<Void, Void, User>(
//                        new CouchDB.FindBusinessById(fromUserId), null, getApplicationContext(), true
//                ).execute().get();
//            } catch (InterruptedException | ExecutionException e) {
//                e.printStackTrace();
//            }
        } else {
            //UsersManagement.setToUser(fromUser);
            SettingsManager.ResetSettings();
            if (ActivityChatWall.gCurrentMessages != null)
                ActivityChatWall.gCurrentMessages.clear();

            startActivity(new Intent(this, ActivityChatWall.class));
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        int pagePosition = mViewPager.getCurrentItem();

        MenuItem item = menu.findItem(R.id.grid_default_search);

        switch (pagePosition) {
            case 0:
                if (item != null) {
                    item.setVisible(false);
                }
                break;
            case 1:
                if (item != null) {
                    item.setVisible(false);
                }
                break;
            case 2:
                if (item != null) {
                    item.setVisible(false);
                }
                break;
        }

        return true;
    }

    public void logOut() {
//        mAuth.signOut();
    }

    /*********************************************************************************************/
	/***********************************GOOGLE CLOUD MESSAGING************************************/
	/********************************************* GCM *******************************************/
	/*********************************************************************************************/
    private BroadcastReceiver mRegistrationBroadcastReceiver;
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG_GCM = "GCM Conversa";

	/**
	 * Revisa el dispositivo para asegurarse que tiene la APK de Google Play Services.
	 * Si no lo tiene, despliega un dialogo que permite al usuario descargar la APK
	 * desde la Google Play Store o activarlo en los ajustes del sistema del dispositivo.
	 */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Logger.error(TAG_GCM, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

}
