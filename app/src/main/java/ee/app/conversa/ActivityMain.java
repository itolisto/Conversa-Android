package ee.app.conversa;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.onesignal.OneSignal;

import org.json.JSONObject;

import ee.app.conversa.extendables.ConversaActivity;
import ee.app.conversa.management.ably.Connection;
import ee.app.conversa.model.parse.Account;
import ee.app.conversa.utils.Logger;
import ee.app.conversa.utils.PagerAdapter;
import ee.app.conversa.utils.Utils;

public class ActivityMain extends ConversaActivity {

    private ViewPager mViewPager;
    private TabLayout tabLayout;
    private String titles[];
    private int[] tabIcons = {
            R.drawable.chats_active,
            R.drawable.actuales_inactive,
            R.drawable.settings_inactive
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Connection.getInstance().initAbly();

        // Remove internet connection check
        checkInternetConnection = false;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        titles = getResources().getStringArray(R.array.categories_titles);
        PagerAdapter mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
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
                    case 0:
                        tab.setIcon(R.drawable.chats_active);
                        break;
                    case 1:
                        tab.setIcon(R.drawable.actuales_active);

                        if(getSupportFragmentManager().getBackStackEntryCount() <= 0) {
                            String title = ConversaApp.getInstance(getApplicationContext()).getPreferences().getCurrentCategory();
                            if (!title.isEmpty()) {
                                ConversaApp.getInstance(getApplicationContext()).getPreferences().setCurrentCategory("", false);
                            }
                        } else {
                            String title = ConversaApp.getInstance(getApplicationContext()).getPreferences().getCurrentCategory();
                            if (!title.isEmpty()) {
                                getSupportActionBar().setTitle(title);
                                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                            }
                        }
                        break;
                    case 2:
                        tab.setIcon(R.drawable.settings_active);
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
                        tab.setIcon(R.drawable.settings_inactive);
                        break;
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        // 1. Subscribe to Customer channels if not subscribed already
        OneSignal.getTags(new OneSignal.GetTagsHandler() {
            @Override
            public void tagsAvailable(JSONObject tags) {
                if (tags == null || tags.length() == 0) {
                    OneSignal.setSubscription(true);
                    Utils.subscribeToTags(Account.getCurrentUser().getObjectId());
                }
            }
        });

        initialization();
	}

    @Override
    public void onBackPressed() {
        if(mViewPager.getCurrentItem() == 1) {
            if(getSupportFragmentManager().getBackStackEntryCount() > 0) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(getString(R.string.categories));
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                }
                ConversaApp.getInstance(this).getPreferences().setCurrentCategory("", false);
                getSupportFragmentManager().popBackStack();
                return;
            }
        }

        super.onBackPressed();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.grid_default_search);

        switch (mViewPager.getCurrentItem()) {
            case 1:
                if (item != null) {
                    item.setVisible(true);
                }
                return true;
            default:
                if (item != null) {
                    item.setVisible(false);
                }
                return true;
        }
    }

}