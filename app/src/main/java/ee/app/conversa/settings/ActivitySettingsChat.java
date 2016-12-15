package ee.app.conversa.settings;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.R;
import ee.app.conversa.extendables.ConversaActivity;
import ee.app.conversa.view.LightTextView;

/**
 * Created by edgargomez on 9/9/16.
 */
public class ActivitySettingsChat extends ConversaActivity implements View.OnClickListener,
        SwitchCompat.OnCheckedChangeListener {

    LightTextView mLtvQualitySummary;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_chats);
        initialization();
    }

    @Override
    protected void initialization() {
        super.initialization();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.preferences__chats);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.llQualityUpload).setOnClickListener(this);
        SwitchCompat mScSoundSending = (SwitchCompat) findViewById(R.id.scSoundSending);
        SwitchCompat mScSoundReceiving = (SwitchCompat) findViewById(R.id.scSoundReceiving);
        mLtvQualitySummary = (LightTextView) findViewById(R.id.ltvQualitySummary);

        mScSoundSending.setChecked(
                ConversaApp.getInstance(this).getPreferences().getPlaySoundWhenSending()
        );

        mScSoundReceiving.setChecked(
                ConversaApp.getInstance(this).getPreferences().getPlaySoundWhenReceiving()
        );

        mScSoundSending.setOnCheckedChangeListener(this);
        mScSoundReceiving.setOnCheckedChangeListener(this);

        mLtvQualitySummary.setText(
                ConversaApp.getInstance(this).getPreferences().getUploadQuality()
        );
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llQualityUpload:
                final int index = ConversaApp.getInstance(this).getPreferences().getUploadQualityPosition();

                AlertDialog.Builder b = new AlertDialog.Builder(this);
                b.setTitle(R.string.sett_chat_quality_title);
                b.setSingleChoiceItems(R.array.sett_chat_quality_entries, index, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (which != index) {
                            ConversaApp.getInstance(getApplicationContext())
                                    .getPreferences().setUploadQuality(which);

                            mLtvQualitySummary.setText(
                                    ConversaApp.getInstance(getApplicationContext())
                                            .getPreferences().getUploadQuality());
                        }
                    }
                });
                b.show();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.scSoundSending:
                ConversaApp.getInstance(this).getPreferences().setPlaySoundWhenSending(isChecked);
                break;
            case R.id.scSoundReceiving:
                ConversaApp.getInstance(this).getPreferences().setPlaySoundWhenReceiving(isChecked);
                break;
        }
    }

}