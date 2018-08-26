package ee.app.conversa;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import ee.app.conversa.events.account.AccountEvent;
import ee.app.conversa.jobs.CustomerInfoJob;
import ee.app.conversa.view.RegularTextView;

public class ActivityLoading extends AppCompatActivity {

    private RegularTextView mRtvLoadingInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        initialize();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initialize() {
        mRtvLoadingInfo = findViewById(R.id.rtvLoadingInfo);
        // Load token
        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();

        if (current != null) {
            current.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                @Override
                public void onComplete(@NonNull Task<GetTokenResult> task) {
                    ConversaApp.getInstance(getApplicationContext()).getPreferences().setFirebaseToken(task.getResult().getToken());
                    ConversaApp.getInstance(getApplicationContext()).getPreferences().setFirebaseLoadToken(false);

                    ConversaApp.getInstance(getApplicationContext())
                            .getJobManager()
                            .addJobInBackground(new CustomerInfoJob(FirebaseAuth.getInstance().getCurrentUser().getUid()));
                }
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAccountEvent(AccountEvent event) {
        Intent intent = new Intent(this, ActivityMain.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}




