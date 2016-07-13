package ee.app.conversa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import ee.app.conversa.management.ConnectionChangeReceiver;

/**
 * Created by edgargomez on 6/3/16.
 */
public class BaseActivity extends AppCompatActivity {

    protected RelativeLayout mRlNoInternetNotification;
    protected boolean checkInternetConnection;
    protected boolean hasInternetConnection;

    private final IntentFilter mConnectionChangeFilter = new IntentFilter(ConnectionChangeReceiver.INTERNET_CONNECTION_CHANGE);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkInternetConnection = true;
        hasInternetConnection = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (checkInternetConnection) {
            ConversaApp.getLocalBroadcastManager().registerReceiver(mConnectionChangeReceiver, mConnectionChangeFilter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkInternetConnection) {
            if (hasInternetConnection) {
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
            ConversaApp.getLocalBroadcastManager().unregisterReceiver(mConnectionChangeReceiver);
        }
    }

    private BroadcastReceiver mConnectionChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra(ConnectionChangeReceiver.HAS_INTERNET_CONNECTION, true)) {
                yesInternetConnection();
                hasInternetConnection = true;
            } else {
                noInternetConnection();
                hasInternetConnection = false;
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

    protected void initialization() {
        if (checkInternetConnection) {
            mRlNoInternetNotification = (RelativeLayout) findViewById(R.id.rlNoInternetNotification);
        }
    }

}
