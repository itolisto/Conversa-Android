package ee.app.conversa.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;

import ee.app.conversa.R;
import ee.app.conversa.extendables.ConversaActivity;
import ee.app.conversa.view.LightTextView;

/**
 * Created by edgargomez on 8/30/16.
 */
public class ActivityChatSettings extends ConversaActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private LightTextView mLtvSelectedQuality;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_chat);
        initialization();
    }

    @Override
    protected void initialization() {
        super.initialization();
        SwitchCompat mScMessageSentSound = (SwitchCompat) findViewById(R.id.scMessageSentSound);
        SwitchCompat mScMessageReceivedSound = (SwitchCompat) findViewById(R.id.scMessageReceivedSound);
        RelativeLayout mRlImageQuality = (RelativeLayout) findViewById(R.id.rlImageQuality);
        mLtvSelectedQuality = (LightTextView) findViewById(R.id.ltvSelectedQuality);

        mScMessageSentSound.setOnCheckedChangeListener(this);
        mScMessageReceivedSound.setOnCheckedChangeListener(this);
        mRlImageQuality.setOnClickListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.scMessageSentSound:
                break;
            case R.id.scMessageReceivedSound:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rlImageQuality:

                break;
        }
    }
}