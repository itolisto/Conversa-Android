package ee.app.conversa.settings;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.R;

/**
 * Created by edgargomez on 9/9/16.
 */
public class FragmentSettingsChat extends PreferenceFragment implements Preference.OnPreferenceClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        addPreferencesFromResource(R.xml.fragment_settings_chat);

        this.findPreference(PreferencesKeys.CHAT_QUALITY_KEY)
                .setOnPreferenceClickListener(this);

        this.findPreference(PreferencesKeys.CHAT_QUALITY_KEY)
                .setSummary(ConversaApp.getInstance(getActivity()).getPreferences().getUploadQuality());
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ActivityPreferences)getActivity()).getSupportActionBar().setTitle(R.string.preferences__chats);
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case PreferencesKeys.CHAT_QUALITY_KEY:
                final int index = ConversaApp.getInstance(getActivity()).getPreferences().getUploadQualityPosition();

                AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                b.setTitle(R.string.sett_chat_quality_title);
                b.setSingleChoiceItems(R.array.sett_chat_quality_entries, index, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (which != index)
                            ConversaApp.getInstance(getActivity())
                                    .getPreferences().setUploadQuality(which);
                    }
                });
                b.show();
                break;
        }

        return false;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PreferencesKeys.CHAT_QUALITY_KEY)) {
            this.findPreference(PreferencesKeys.CHAT_QUALITY_KEY)
                    .setSummary(ConversaApp.getInstance(getActivity())
                            .getPreferences().getUploadQualityFromNewValue(
                                    ConversaApp.getInstance(getActivity())
                                            .getPreferences().getUploadQualityPosition()
                            )
                    );
        }
    }

}