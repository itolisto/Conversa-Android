package ee.app.conversa.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import ee.app.conversa.R;

/**
 * Created by edgargomez on 9/9/16.
 */
public class FragmentSettingsChat extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        addPreferencesFromResource(R.xml.fragment_settings_chat);

        this.findPreference(PreferencesKeys.CHAT_QUALITY_KEY)
                .setOnPreferenceClickListener(this);
        this.findPreference(PreferencesKeys.CHAT_SOUND_SENDING_KEY)
                .setOnPreferenceClickListener(this);
        this.findPreference(PreferencesKeys.CHAT_SOUND_RECEIVING_KEY)
                .setOnPreferenceClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ActivityPreferences)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((ActivityPreferences)getActivity()).getSupportActionBar().setTitle(R.string.preferences__chats);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return false;
    }

}