package ee.app.conversa.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import ee.app.conversa.R;

/**
 * Created by edgargomez on 9/9/16.
 */
public class FragmentSettingsNotifications extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        addPreferencesFromResource(R.xml.fragment_settings_notification);

        this.findPreference(PreferencesKeys.NOTIFICATION_SOUND_KEY)
                .setOnPreferenceClickListener(this);
        this.findPreference(PreferencesKeys.NOTIFICATION_PREVIEW_KEY)
                .setOnPreferenceClickListener(this);
        this.findPreference(PreferencesKeys.NOTIFICATION_INAPP_SOUND_KEY)
                .setOnPreferenceClickListener(this);
        this.findPreference(PreferencesKeys.NOTIFICATION_INAPP_PREVIEW_KEY)
                .setOnPreferenceClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ActivityPreferences)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((ActivityPreferences)getActivity()).getSupportActionBar().setTitle(R.string.preferences__notifications);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {

        }

        return true;
    }

}