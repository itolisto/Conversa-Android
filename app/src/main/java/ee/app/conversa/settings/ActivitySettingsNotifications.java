package ee.app.conversa.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.widget.CompoundButton;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.R;
import ee.app.conversa.extendables.ConversaActivity;

/**
 * Created by edgargomez on 9/9/16.
 */
public class ActivitySettingsNotifications extends ConversaActivity implements SwitchCompat.OnCheckedChangeListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_notifications);
        initialization();
    }

    @Override
    protected void initialization() {
        super.initialization();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.preferences__notifications);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SwitchCompat mScPushSound = findViewById(R.id.scPushSound);
        SwitchCompat mScPushPreview = findViewById(R.id.scPushPreview);
        SwitchCompat mScInAppSound = findViewById(R.id.scInAppSound);
        SwitchCompat mScInAppPreview = findViewById(R.id.scInAppPreview);

        mScPushSound.setChecked(
                ConversaApp.getInstance(this).getPreferences().getPushNotificationSound()
        );

        mScPushPreview.setChecked(
                ConversaApp.getInstance(this).getPreferences().getPushNotificationPreview()
        );

        mScInAppSound.setChecked(
                ConversaApp.getInstance(this).getPreferences().getInAppNotificationSound()
        );

        mScInAppPreview.setChecked(
                ConversaApp.getInstance(this).getPreferences().getInAppNotificationPreview()
        );

        mScPushSound.setOnCheckedChangeListener(this);
        mScPushPreview.setOnCheckedChangeListener(this);
        mScInAppSound.setOnCheckedChangeListener(this);
        mScInAppPreview.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.scPushSound:
                ConversaApp.getInstance(this).getPreferences().setPushNotificationSound(isChecked);
                break;
            case R.id.scPushPreview:
                ConversaApp.getInstance(this).getPreferences().setPushNotificationPreview(isChecked);
                break;
            case R.id.scInAppSound:
                ConversaApp.getInstance(this).getPreferences().setInAppNotificationSound(isChecked);
                break;
            case R.id.scInAppPreview:
                ConversaApp.getInstance(this).getPreferences().setInAppNotificationPreview(isChecked);
                break;
        }
    }
}