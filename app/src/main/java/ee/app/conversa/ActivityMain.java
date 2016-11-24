package ee.app.conversa;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.onesignal.OneSignal;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

import ee.app.conversa.extendables.ConversaActivity;
import ee.app.conversa.management.AblyConnection;
import ee.app.conversa.model.parse.Account;
import ee.app.conversa.utils.Foreground;
import ee.app.conversa.utils.Logger;
import ee.app.conversa.utils.PagerAdapter;
import ee.app.conversa.utils.Utils;

public class ActivityMain extends ConversaActivity implements Foreground.Listener {

    private final String TAG = ActivityMain.class.getSimpleName();
    private ViewPager mViewPager;
    private TabLayout tabLayout;
    private String titles[];
    private boolean insideCategory;
    private boolean resetNotifications;

    private final int[] tabIcons = {
            R.drawable.tab_chat_inactive,
            R.drawable.tab_explore_inactive
    };

    private final int[] tabSelectedIcons = {
            R.drawable.tab_chat_active,
            R.drawable.tab_explore_active
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AblyConnection.getInstance().initAbly();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        titles = getResources().getStringArray(R.array.categories_titles);
        final PagerAdapter mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(mViewPager);
        }

        // Initial state of tabs and titles
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(titles[0]);
        }

        insideCategory = false;
        resetNotifications = true;
        ConversaApp.getInstance(getApplicationContext()).getPreferences().setCurrentCategory("", false);

        tabLayout.getTabAt(0).setIcon(tabSelectedIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                final int p = tab.getPosition();
                mViewPager.setCurrentItem(p);
                tab.setIcon(tabSelectedIcons[p]);
                getSupportActionBar().setTitle(titles[p]);
                supportInvalidateOptionsMenu();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int position = tab.getPosition();

                if (position == 0) {
                    if (mPagerAdapter.getRegisteredFragment(0) != null)
                        ((FragmentUsersChat)mPagerAdapter.getRegisteredFragment(0)).finishActionMode();
                }

                tab.setIcon(tabIcons[position]);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // 1. Subscribe to Customer channels if not subscribed already
        if (ConversaApp.getInstance(this).getPreferences().getCustomerId().isEmpty()) {
            // 1. Get Customer Id
            Account.getCustomerId(new WeakReference<AppCompatActivity>(this));
        } else {
            OneSignal.getTags(new OneSignal.GetTagsHandler() {
                @Override
                public void tagsAvailable(JSONObject tags) {
                    if (tags == null || tags.length() == 0) {
                        OneSignal.setSubscription(true);
                        Utils.subscribeToTags(ConversaApp.getInstance(getApplicationContext())
                                .getPreferences().getCustomerId());
                    }
                }
            });
        }

        initialization();
	}

    @Override
    protected void initialization() {
        super.initialization();
        Foreground.get(this).addListener(this);
    }

    public void onDestroy() {
        super.onDestroy();
        Foreground.get(this).removeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Clear notifications
        if (resetNotifications) {
            resetNotifications = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Logger.error(TAG, "Resetting counts and clear all notifications");
                    ConversaApp.getInstance(getApplicationContext())
                            .getDB()
                            .resetAllCounts();
                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancelAll();
                }
            }).start();
        }
    }

    @Override
    public void onBackPressed() {
        if(mViewPager.getCurrentItem() == 1) {
            if(getSupportFragmentManager().getBackStackEntryCount() > 0) {
                onBackPressedFromCategory();
                return;
            }
        }

        super.onBackPressed();
    }

    /*
     * 1. Add setHasOptionsMenu(true) method in your Fragment's onCreate(Bundle savedInstanceState) method.
     * 2. Override onCreateOptionsMenu(Menu menu) (if you want to do something different in your Fragment's menu)
     * and onOptionsItemSelected(MenuItem item) methods in your Fragment.
     * 3. Inside your onOptionsItemSelected(MenuItem item) Activity's method, make sure you return false when the
     * menu item action would be implemented in onOptionsItemSelected(MenuItem item) Fragment's method.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        switch (mViewPager.getCurrentItem()) {
            case 1: {
                menu.findItem(R.id.action_search).setVisible(true);
                menu.findItem(R.id.action_settings).setVisible(false);
                break;
            }
            default: {
                menu.findItem(R.id.action_search).setVisible(false);
                menu.findItem(R.id.action_settings).setVisible(true);
                break;
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    public void onBackPressedFromCategory() {
        ConversaApp.getInstance(this).getPreferences().setCurrentCategory("", false);
        FragmentManager fm = getSupportFragmentManager();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getResources().getStringArray(R.array.categories_titles)[1]);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        toggleTabLayoutVisibility();

        if (fm != null) {
            if(fm.getBackStackEntryCount() > 0) {
                fm.popBackStack();
            } else {
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.root_frame, new FragmentCategory());
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.commit();
            }
        } else {
            Logger.error(this.getClass().getSimpleName(), "Fragmento no se pudo reemplazar");
        }
    }

    public void selectViewPagerTab(int tab) {
        if (tab > 2 || tab < 0) {
            return;
        }

        mViewPager.setCurrentItem(tab);
    }

    public void toggleTabLayoutVisibility() {
        if (!insideCategory) {
            // Hide tabLayout
            if (tabLayout.getVisibility() != View.GONE) {
                tabLayout.setVisibility(View.GONE);
                mViewPager.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });

                insideCategory = true;
            }
        } else {
            // Show tabLayout
            if (tabLayout.getVisibility() != View.VISIBLE) {
                tabLayout.setVisibility(View.VISIBLE);
                mViewPager.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return false;
                    }
                });

                insideCategory = false;
            }
        }
    }

    @Override
    public void onBecameForeground() {
        AblyConnection.getInstance().connectAbly();
    }

    @Override
    public void onBecameBackground() {
        AblyConnection.getInstance().disconnectAbly();
    }

}