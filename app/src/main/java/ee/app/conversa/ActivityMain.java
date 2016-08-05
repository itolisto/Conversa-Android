package ee.app.conversa;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.onesignal.OneSignal;

import org.json.JSONObject;

import ee.app.conversa.extendables.ConversaActivity;
import ee.app.conversa.model.Parse.Account;
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
                    case 2:
                        tab.setIcon(R.drawable.settings_active);

                        if (Build.VERSION.SDK_INT >= 23) {
                            getSupportActionBar().setBackgroundDrawable(
                                    new ColorDrawable(getResources().getColor(R.color.settings_tab, null)));
                            tabLayout.setBackground(new ColorDrawable(getResources().getColor(R.color.settings_tab, null)));
                            mViewPager.setBackground(new ColorDrawable(getResources().getColor(R.color.settings_background, null)));
                        } else {
                            getSupportActionBar().setBackgroundDrawable(
                                    new ColorDrawable(getResources().getColor(R.color.settings_tab)));
                            tabLayout.setBackgroundColor(getResources().getColor(R.color.settings_tab));
                            mViewPager.setBackgroundColor(getResources().getColor(R.color.settings_background));
                        }
                        break;
                    default:
                        if (p == 1) {
                            tab.setIcon(R.drawable.actuales_active);
                        } else {
                            tab.setIcon(R.drawable.chats_active);
                        }

                        if (Build.VERSION.SDK_INT >= 23) {
                            getSupportActionBar().setBackgroundDrawable(
                                    new ColorDrawable(getResources().getColor(R.color.regular_tabs, null)));
                            tabLayout.setBackground(new ColorDrawable(getResources().getColor(R.color.regular_tabs, null)));
                            mViewPager.setBackground(new ColorDrawable(getResources().getColor(R.color.normal_background, null)));
                        } else {
                            getSupportActionBar().setBackgroundDrawable(
                                    new ColorDrawable(getResources().getColor(R.color.regular_tabs))
                            );
                            tabLayout.setBackgroundColor(getResources().getColor(R.color.regular_tabs));
                            mViewPager.setBackgroundColor(getResources().getColor(R.color.normal_background));
                        }
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
                        if (Build.VERSION.SDK_INT >= 23) {
                            tabLayout.setBackground(new ColorDrawable(getResources().getColor(R.color.regular_tabs, null)));
                            mViewPager.setBackground(new ColorDrawable(getResources().getColor(R.color.normal_background, null)));
                        } else {
                            tabLayout.setBackgroundColor(getResources().getColor(R.color.regular_tabs));
                            mViewPager.setBackgroundColor(getResources().getColor(R.color.normal_background));
                        }
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
                    Utils.subscribeToTags(Account.getCurrentUser().getObjectId());
                }
            }
        });

        super.initialization();
	}

    @Override
    public void onBackPressed() {
        if(mViewPager.getCurrentItem() == 1) {
            Log.e(this.getClass().getSimpleName(), "getBackStackEntryCount: " + getSupportFragmentManager().getBackStackEntryCount());
            if(getSupportFragmentManager().getBackStackEntryCount() > 1) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(getString(R.string.categories));
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                }
                getSupportFragmentManager().popBackStack();
                return;
            } else {
                // We need to pop immediate because root fragment is being pop when
                // back is pressed. This way we exit application as expected.
                getSupportFragmentManager().popBackStack();
            }
        }

        super.onBackPressed();
    }

    @Override
    protected void openFromNotification(Bundle extras) {

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
