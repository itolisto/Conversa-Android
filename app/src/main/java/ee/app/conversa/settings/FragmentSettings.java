package ee.app.conversa.settings;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import ee.app.conversa.R;

public class FragmentSettings extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_settings);
        this.findPreference(PreferencesKeys.PREFERENCE_MAIN_ACCOUNT)
                .setOnPreferenceClickListener(this);
        this.findPreference(PreferencesKeys.PREFERENCE_MAIN_CHATS)
                .setOnPreferenceClickListener(this);
        this.findPreference(PreferencesKeys.PREFERENCE_MAIN_NOTIFICATIONS)
                .setOnPreferenceClickListener(this);
        this.findPreference(PreferencesKeys.PREFERENCE_MAIN_SHARE)
                .setOnPreferenceClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ActivityPreferences)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((ActivityPreferences)getActivity()).getSupportActionBar().setTitle(R.string.settings);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        Fragment fragment;

        switch (preference.getKey()) {
            case PreferencesKeys.PREFERENCE_MAIN_ACCOUNT:
                fragment = new FragmentSettingsAccount();
                break;
            case PreferencesKeys.PREFERENCE_MAIN_CHATS:
                fragment = new FragmentSettingsChat();
                break;
            case PreferencesKeys.PREFERENCE_MAIN_NOTIFICATIONS:
                fragment = new FragmentSettingsNotifications();
                break;
            case PreferencesKeys.PREFERENCE_MAIN_SHARE:
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setType("text/plain");
                // Add data to the intent, the receiving app will decide what to do with it.
                String subject = getActivity().getString(R.string.settings_using_conversa) + " " + getActivity().getString(R.string.app_name);
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);

                String body = getActivity().getString(R.string.settings_body1_conversa) + " " +
                        getActivity().getString(R.string.app_name) + " " + getActivity().getString(R.string.settings_body2_conversa);

                intent.putExtra(Intent.EXTRA_TEXT, body);
                startActivity(Intent.createChooser(intent, getActivity().getString(R.string.settings_share_conversa)));
                return true;
            case PreferencesKeys.PREFERENCE_MAIN_HELP:
                fragment = new FragmentSettingsNotifications();
                break;
            default:
                throw new AssertionError();
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .addToBackStack(null)
                .hide(this)
                .commit();

        return true;
    }

}