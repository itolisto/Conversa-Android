package ee.app.conversa;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import ee.app.conversa.extendables.ConversaActivity;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.SearchPagerAdapter;

/**
 * Created by edgargomez on 7/12/16.
 */
public class ActivitySearch extends ConversaActivity {

    public static ActivitySearch sInstance;
    private boolean mPushHandledOnNewIntent = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        checkInternetConnection = false;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        String titles[] = getResources().getStringArray(R.array.search_titles);
        SearchPagerAdapter mPagerAdapter = new SearchPagerAdapter(getSupportFragmentManager(), titles);
        if (mViewPager != null) {
            mViewPager.setAdapter(mPagerAdapter);
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(mViewPager);
        }

        // Initial state of tabs and titles
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(titles[0]);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void openWallFromNotification(Intent intent) {
        String fromUserId = intent.getStringExtra(Const.PUSH_FROM_USER_ID);
//        User fromUser     = ConversaApp.getDB().isContact(fromUserId);

//        if(fromUser == null) {
//            try {
//                fromUser = new ConversaAsyncTask<Void, Void, User>(
//                        new CouchDB.FindBusinessById(fromUserId), null, getApplicationContext(), true
//                ).execute().get();
//            } catch (InterruptedException | ExecutionException e) {
//                e.printStackTrace();
//            }
//        } else {
        //UsersManagement.setBusiness(fromUser);
        //SettingsManager.ResetSettings();
//            if (ActivityChatWall.gCurrentMessages != null)
//                ActivityChatWall.gCurrentMessages.clear();

//            startActivity(new Intent(this, ActivityChatWall.class));
//        }
    }

}