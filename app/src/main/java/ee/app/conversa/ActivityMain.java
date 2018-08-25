package ee.app.conversa;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.crashlytics.android.Crashlytics;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.taplytics.sdk.Taplytics;

import org.json.JSONObject;

import ee.app.conversa.extendables.ConversaActivity;
import ee.app.conversa.jobs.CustomerInfoJob;
import ee.app.conversa.management.AblyConnection;
import ee.app.conversa.model.database.dbBusiness;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Logger;
import ee.app.conversa.utils.PagerAdapter;
import ee.app.conversa.view.MediumTextView;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.fabric.sdk.android.Fabric;
import okhttp3.Request;
import tourguide.tourguide.Overlay;
import tourguide.tourguide.Pointer;
import tourguide.tourguide.ToolTip;
import tourguide.tourguide.TourGuide;

public class ActivityMain extends ConversaActivity implements View.OnClickListener {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final String TAG = ActivityMain.class.getSimpleName();
    private ViewPager mViewPager;
    private boolean resetNotifications;
    private TourGuide mTourGuideHandler;
    //private boolean hasTutorialBeenDisplay;
    private ImageView mIvConversa;
    private RelativeLayout mRlCategoryToolbar;
    private MediumTextView mRtvTitle;
    private Activity mActivity;

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
        mActivity = this;

        Fabric.with(this, new Crashlytics());
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            // Verify if deep link was opened when no session is active
            Intent go = new Intent(this, ActivitySignIn.class);
            startActivity(go);
            finish();
        } else {
            AblyConnection.getInstance().initAbly();

            Toolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setTitle("");
            mRlCategoryToolbar = toolbar.findViewById(R.id.rlCategoryToolbar);
            mIvConversa = toolbar.findViewById(R.id.ivConversa);
            mRtvTitle = toolbar.findViewById(R.id.rtvTitle);
            setSupportActionBar(toolbar);

            mViewPager = findViewById(R.id.pager);
            final PagerAdapter mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
            mViewPager.setAdapter(mPagerAdapter);
            mViewPager.setOffscreenPageLimit(2);

            resetNotifications = true;

            TabLayout tabLayout = findViewById(R.id.tabLayout);
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
                    if (p == 1) { // is tab explore is selected.
                        if (mTourGuideHandler != null) {
                            mTourGuideHandler.cleanUp();
                            mTourGuideHandler = TourGuide.init((mActivity)).with(TourGuide.Technique.CLICK)
                                    .setPointer(new Pointer())
                                    .setToolTip(new ToolTip().setTitle(getResources().getString(R.string.guide_two_title)).setDescription(getResources().getString(R.string.guide_two_description)))
                                    .setOverlay(new Overlay())
                                    .playOn(findViewById(R.id.fsvSearch));
                        }
                    }
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

            if (!ConversaApp.getInstance(this).getPreferences().getGuideExplore()) {
                View exploreTab = ((ViewGroup) tabLayout.getChildAt(0)).getChildAt(1);
                mTourGuideHandler = TourGuide.init(this).with(TourGuide.Technique.CLICK)
                        .setPointer(new Pointer())
                        .setToolTip(new ToolTip().setTitle(getString(R.string.tutorial_title_one)).setDescription(getString(R.string.highlight_explore)))
                        .setOverlay(new Overlay())
                        .playOn(exploreTab);
            }

            // 1. Subscribe to Customer channels if not subscribed already
            if (ConversaApp.getInstance(this).getPreferences().getAccountCustomerId().isEmpty()) {
                // 1. Get Customer Id
                ConversaApp.getInstance(this)
                        .getJobManager()
                        .addJobInBackground(new CustomerInfoJob(currentUser.getUid()));
            } else {
                AblyConnection.getInstance().subscribeToChannels();
            }

            initialization();
        }
	}

    @Override
    protected void initialization() {
        super.initialization();
        findViewById(R.id.fsvSearch).setOnClickListener(this);
        findViewById(R.id.ivFavs).setOnClickListener(this);

        Taplytics.startTaplytics(this, "1a214e395c9db615a2cf2819a576bd9f17372ca5");

        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();

        if (current != null) {
            current.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                @Override
                public void onComplete(@NonNull Task<GetTokenResult> task) {
                    ConversaApp.getInstance(getApplicationContext()).getPreferences().setFirebaseToken(task.getResult().getToken());
                }
            });
        }
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
                mRlCategoryToolbar.setVisibility(View.VISIBLE);
                mRtvTitle.setVisibility(View.GONE);
                break;
            }
            case 0: {
                mRlCategoryToolbar.setVisibility(View.GONE);
                mIvConversa.setVisibility(View.VISIBLE);
                mRtvTitle.setVisibility(View.GONE);
                break;
            }
            default: {
                mRlCategoryToolbar.setVisibility(View.GONE);
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
            if (mTourGuideHandler != null) {
                mTourGuideHandler.cleanUp();
                mTourGuideHandler = null;
                ConversaApp.getInstance(this).getPreferences().setGuideExplore(true);
            } else {
                Intent intent = new Intent(getApplicationContext(), ActivitySearch.class);
                startActivity(intent);
            }
        } else if (view.getId() == R.id.ivFavs) {
            // Accion
            Intent intent = new Intent(getApplicationContext(), ActivityFavorite.class);
            FlurryAgent.logEvent("user_favorites_selected");
            startActivity(intent);
        }
    }

}
