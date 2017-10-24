package ee.app.conversa;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

import com.parse.ParseUser;
import com.taplytics.sdk.Taplytics;

import net.hockeyapp.android.CrashManager;

import org.json.JSONObject;

import ee.app.conversa.extendables.ConversaActivity;
import ee.app.conversa.jobs.CustomerInfoJob;
import ee.app.conversa.management.AblyConnection;
import ee.app.conversa.model.database.dbBusiness;
import ee.app.conversa.model.parse.Account;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Logger;
import ee.app.conversa.utils.PagerAdapter;
import ee.app.conversa.view.MediumTextView;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;

public class ActivityMain extends ConversaActivity implements View.OnClickListener {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final String TAG = ActivityMain.class.getSimpleName();
    private ViewPager mViewPager;
    private boolean resetNotifications;

    private ImageView mIvConversa;
    private CardView mFsvSearch;
    private MediumTextView mRtvTitle;

    private final int[] tabIcons = {
            R.drawable.tab_chat_inactive,
            R.drawable.tab_explore_inactive,
            R.drawable.tab_settings_inactive
    };

    private final int[] tabSelectedIcons = {
            R.drawable.tab_chat_active,
            R.drawable.tab_explore_active,
            R.drawable.tab_settings_active
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            // If to verify if deep link was opened when no session is
            // active,
            Intent go = new Intent(this, ActivitySignIn.class);
            startActivity(go);
            finish();
        } else {
            AblyConnection.getInstance().initAbly();

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setTitle("");
            mFsvSearch = (CardView) toolbar.findViewById(R.id.fsvSearch);
            mIvConversa = (ImageView) toolbar.findViewById(R.id.ivConversa);
            mRtvTitle = (MediumTextView) toolbar.findViewById(R.id.rtvTitle);
            setSupportActionBar(toolbar);

            mViewPager = (ViewPager) findViewById(R.id.pager);
            final PagerAdapter mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
            mViewPager.setAdapter(mPagerAdapter);
            mViewPager.setOffscreenPageLimit(2);

            resetNotifications = true;

            TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
            tabLayout.setupWithViewPager(mViewPager);
            tabLayout.getTabAt(0).setIcon(tabSelectedIcons[0]);
            tabLayout.getTabAt(1).setIcon(tabIcons[1]);
            tabLayout.getTabAt(2).setIcon(tabIcons[2]);

            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    final int p = tab.getPosition();
                    mViewPager.setCurrentItem(p);
                    supportInvalidateOptionsMenu();
                    tab.setIcon(tabSelectedIcons[p]);
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
                public void onTabReselected(TabLayout.Tab tab) { }
            });

            // 1. Subscribe to Customer channels if not subscribed already
            if (ConversaApp.getInstance(this).getPreferences().getAccountCustomerId().isEmpty()) {
                // 1. Get Customer Id
                ConversaApp.getInstance(this)
                        .getJobManager()
                        .addJobInBackground(new CustomerInfoJob(Account.getCurrentUser().getObjectId()));
            } else {
                AblyConnection.getInstance().subscribeToChannels();
                AblyConnection.getInstance().subscribeToPushChannels();
            }

            initialization();
        }
	}

    @Override
    protected void initialization() {
        super.initialization();
        mFsvSearch.setOnClickListener(this);
        checkForCrashes();
        Taplytics.startTaplytics(this, "1a214e395c9db615a2cf2819a576bd9f17372ca5");
    }

    private void checkForCrashes() {
        CrashManager.register(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        Branch branch = Branch.getInstance();
        branch.initSession(new Branch.BranchReferralInitListener(){
            @Override
            public void onInitFinished(JSONObject referringParams, BranchError error) {
                if (error == null) {
                    // params will be empty if no data found
                    if (referringParams.optString("goConversa", null) != null) {
                        String objectId = referringParams.optString(Const.kBranchBusinessIdKey, "");

                        if (!TextUtils.isEmpty(objectId)) {
                            dbBusiness business = ConversaApp.getInstance(getApplicationContext())
                                    .getDB()
                                    .isContact(objectId);

                            String name = referringParams.optString(Const.kBranchBusinessNameKey, "");
                            String id = referringParams.optString(Const.kBranchBusinessConversaIdKey, "");
                            String avatar = referringParams.optString(Const.kBranchBusinessAvatarKey, "");
                            boolean add = false;

                            if (business == null) {
                                business = new dbBusiness();
                                business.setBusinessId(objectId);
                                business.setDisplayName(name);
                                business.setConversaId(id);
                                business.setComposingMessage("");
                                business.setAvatarThumbFileId(avatar);
                                business.setBlocked(false);
                                business.setMuted(false);
                                add = true;
                            }

                            Intent intent = new Intent(getApplicationContext(), ActivityChatWall.class);
                            intent.putExtra(Const.iExtraBusiness, business);
                            intent.putExtra(Const.iExtraAddBusiness, add);
                            startActivity(intent);
                        }
                    }
                } else {
                    Logger.error("ActivityChatWall branch", error.getMessage());
                }
            }
        }, this.getIntent().getData(), this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        this.setIntent(intent);
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        switch (mViewPager.getCurrentItem()) {
            case 1: {
                mIvConversa.setVisibility(View.GONE);
                mFsvSearch.setVisibility(View.VISIBLE);
                mRtvTitle.setVisibility(View.GONE);
                break;
            }
            case 0: {
                mFsvSearch.setVisibility(View.GONE);
                mIvConversa.setVisibility(View.VISIBLE);
                mRtvTitle.setVisibility(View.GONE);
                break;
            }
            default: {
                mFsvSearch.setVisibility(View.GONE);
                mIvConversa.setVisibility(View.GONE);
                mRtvTitle.setVisibility(View.VISIBLE);
                break;
            }
        }

        return true;
    }

    public void selectViewPagerTab(int tab) {
        if (tab > 2 || tab < 0) {
            return;
        }

        mViewPager.setCurrentItem(tab);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fsvSearch) {
            Intent intent = new Intent(getApplicationContext(), ActivitySearch.class);
            startActivity(intent);
        }
    }

}