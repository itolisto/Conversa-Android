package ee.app.conversa.extendables;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.instabug.library.InstabugTrackingDelegate;

import org.json.JSONObject;

import ee.app.conversa.ActivityChatWall;
import ee.app.conversa.ConversaApp;
import ee.app.conversa.R;
import ee.app.conversa.management.ConnectionChangeReceiver;
import ee.app.conversa.model.database.dbBusiness;
import ee.app.conversa.settings.language.DynamicLanguage;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Logger;
import ee.app.conversa.utils.Utils;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;

/**
 * Created by edgargomez on 6/3/16.
 */
public class BaseActivity extends AppCompatActivity {

    protected RelativeLayout mRlNoInternetNotification;
    protected boolean checkInternetConnection;
    private final DynamicLanguage dynamicLanguage = new DynamicLanguage();
    private final IntentFilter mConnectionChangeFilter = new IntentFilter(ConnectionChangeReceiver.INTERNET_CONNECTION_CHANGE);

    protected void onPreCreate() {
        dynamicLanguage.onCreate(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        onPreCreate();
        checkInternetConnection = true;
        super.onCreate(savedInstanceState);
    }

    protected void initialization() {
        if (checkInternetConnection) {
            mRlNoInternetNotification = (RelativeLayout) findViewById(R.id.rlNoInternetNotification);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (checkInternetConnection) {
            ConversaApp.getInstance(this).getLocalBroadcastManager().registerReceiver(mConnectionChangeReceiver, mConnectionChangeFilter);
        }

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
                                business.setAbout("");
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
    protected void onResume() {
        super.onResume();
        dynamicLanguage.onResume(this);
        if (checkInternetConnection) {
            if (Utils.hasNetworkConnection(this)) {
                yesInternetConnection();
            } else {
                noInternetConnection();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (checkInternetConnection) {
            ConversaApp.getInstance(this).getLocalBroadcastManager().unregisterReceiver(mConnectionChangeReceiver);
        }
    }

    private BroadcastReceiver mConnectionChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra(ConnectionChangeReceiver.HAS_INTERNET_CONNECTION, true)) {
                yesInternetConnection();
            } else {
                noInternetConnection();
            }
        }
    };

    public void noInternetConnection() {
        if (mRlNoInternetNotification != null && mRlNoInternetNotification.getVisibility() == View.GONE) {
            Animation slidein = AnimationUtils.loadAnimation(getApplicationContext(),
                    R.anim.slide_in_top);
            mRlNoInternetNotification.setVisibility(View.VISIBLE);
            mRlNoInternetNotification.startAnimation(slidein);
        }
    }

    public void yesInternetConnection() {
        if (mRlNoInternetNotification != null && mRlNoInternetNotification.getVisibility() == View.VISIBLE) {
            Animation slideout = AnimationUtils.loadAnimation(getApplicationContext(),
                    R.anim.slide_out_top);
            mRlNoInternetNotification.setVisibility(View.GONE);
            mRlNoInternetNotification.startAnimation(slideout);
        }
    }

    public boolean hasInternetConnection() {
        return Utils.hasNetworkConnection(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        InstabugTrackingDelegate
                .notifyActivityGotTouchEvent(ev,this);
        return super.dispatchTouchEvent(ev);
    }

}