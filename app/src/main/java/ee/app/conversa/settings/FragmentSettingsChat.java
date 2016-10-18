package ee.app.conversa.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.R;

/**
 * Created by edgargomez on 9/9/16.
 */
public class FragmentSettingsChat extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        addPreferencesFromResource(R.xml.fragment_settings_chat);

        this.findPreference(PreferencesKeys.CHAT_QUALITY_KEY)
                .setOnPreferenceChangeListener(this);

        this.findPreference(PreferencesKeys.CHAT_QUALITY_KEY)
                .setSummary(ConversaApp.getInstance(getActivity()).getPreferences().getUploadQuality());
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ActivityPreferences)getActivity()).getSupportActionBar().setTitle(R.string.preferences__chats);
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.getKey().equals(PreferencesKeys.CHAT_QUALITY_KEY)) {
            this.findPreference(PreferencesKeys.CHAT_QUALITY_KEY)
                    .setSummary(ConversaApp.getInstance(getActivity())
                            .getPreferences().getUploadQualityFromNewValue((String)newValue));
        }

        return true;
    }
}