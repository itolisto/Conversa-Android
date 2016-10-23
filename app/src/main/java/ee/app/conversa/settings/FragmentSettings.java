package ee.app.conversa.settings;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;

import ee.app.conversa.ConversaApp;
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
        this.findPreference(PreferencesKeys.PREFERENCE_MAIN_LANGUAGE_KEY)
                .setOnPreferenceClickListener(this);
        this.findPreference(PreferencesKeys.PREFERENCE_MAIN_HELP)
                .setOnPreferenceClickListener(this);

        this.findPreference(PreferencesKeys.PREFERENCE_MAIN_LANGUAGE_KEY)
                .setSummary(ConversaApp.getInstance(getActivity()).getPreferences().getLanguageName());
    }

    @Override
    public void onStart() {
        super.onStart();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener((ActivityPreferences)getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ActivityPreferences)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((ActivityPreferences)getActivity()).getSupportActionBar().setTitle(R.string.settings);
    }

    @Override
    public void onStop() {
        super.onStop();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener((ActivityPreferences) getActivity());
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        Fragment fragment = null;

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
            case PreferencesKeys.PREFERENCE_MAIN_LANGUAGE_KEY:
                final int index;

                switch(ConversaApp.getInstance(getActivity()).getPreferences().getLanguage()) {
                    case "en":
                        index = 1;
                        break;
                    case "es":
                        index = 2;
                        break;
                    default:
                        index = 0;
                        break;
                }

                AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                b.setTitle(R.string.language_spinner_title);
                b.setSingleChoiceItems(R.array.language_entries, index, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (which != index)
                            ConversaApp.getInstance(getActivity()).getPreferences()
                                .setLanguage(getResources().getStringArray(R.array.language_values)[which]);
                    }
                });
                b.show();
                return true;
            case PreferencesKeys.PREFERENCE_MAIN_SHARE:
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setType("text/plain");
                // Add data to the intent, the receiving app will decide what to do with it.
                String subject = getActivity().getString(R.string.settings_using_conversa);
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);

                String body = getActivity().getString(R.string.settings_body_conversa);

                intent.putExtra(Intent.EXTRA_TEXT, body);
                startActivity(Intent.createChooser(intent, getActivity().getString(R.string.settings_share_conversa)));
                return true;
            case PreferencesKeys.PREFERENCE_MAIN_HELP:
                fragment = new FragmentSettingsHelp();
                break;
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .addToBackStack(null)
                .commit();

        return true;
    }

}