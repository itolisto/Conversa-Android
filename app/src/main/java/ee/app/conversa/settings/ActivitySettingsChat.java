package ee.app.conversa.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;

import com.afollestad.materialdialogs.MaterialDialog;

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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.preferences__chats);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.llQualityUpload).setOnClickListener(this);
        SwitchCompat mScSoundSending = findViewById(R.id.scSoundSending);
        SwitchCompat mScSoundReceiving = findViewById(R.id.scSoundReceiving);
        mLtvQualitySummary = findViewById(R.id.ltvQualitySummary);

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

                new MaterialDialog.Builder(this)
                        .title(R.string.sett_chat_quality_title)
                        .items(R.array.sett_chat_quality_entries)
                        .alwaysCallSingleChoiceCallback()
                        .itemsCallbackSingleChoice(index, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                dialog.dismiss();
                                if (which != index) {
                                    ConversaApp.getInstance(getApplicationContext())
                                            .getPreferences().setUploadQuality(which);

                                    mLtvQualitySummary.setText(
                                            ConversaApp.getInstance(getApplicationContext())
                                                    .getPreferences().getUploadQuality());
                                }
                                return true;
                            }
                        })
                        .show();
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