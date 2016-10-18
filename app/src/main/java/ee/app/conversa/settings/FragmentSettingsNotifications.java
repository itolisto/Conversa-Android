package ee.app.conversa.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import ee.app.conversa.R;

/**
 * Created by edgargomez on 9/9/16.
 */
public class FragmentSettingsNotifications extends PreferenceFragment {

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        addPreferencesFromResource(R.xml.fragment_settings_notification);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ActivityPreferences)getActivity()).getSupportActionBar().setTitle(R.string.preferences__notifications);
    }

}