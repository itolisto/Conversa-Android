package ee.app.conversa;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParsePush;

import ee.app.conversa.model.Parse.Account;
import ee.app.conversa.utils.Logger;

public class FragmentSettings extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private String email;
    private String username;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.fragment_settings);
    }

    @Override
    public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle) {
        //SwitchPreferenceCompat mSwitchPreferenceNotification = (SwitchPreferenceCompat) getPreferenceManager().findPreference(getString(R.string.in_app_checkbox_preference_key));
        //SwitchPreferenceCompat mSwitchPreferenceNotificationInApp = (SwitchPreferenceCompat) getPreferenceManager().findPreference(getString(R.string.notification_checkbox_preference_key));
        EditTextPreference mEditTextPreferenceEmail = (EditTextPreference) getPreferenceManager().findPreference(getString(R.string.email_edittext_preference_key));
        EditTextPreference mEditTextPreferenceName = (EditTextPreference) getPreferenceManager().findPreference(getString(R.string.name_edittext_preference_key));
        //EditTextPreference mEditTextPreferencePassword = (EditTextPreference) getPreferenceManager().findPreference(getString(R.string.password_edittext_preference_key));
        Preference mPreferenceShare = getPreferenceManager().findPreference(getString(R.string.share_preference_key));
        Preference mPreferenceLogout = getPreferenceManager().findPreference(getString(R.string.logout_preference_key));

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        onSharedPreferenceChanged(sharedPreferences, getString(R.string.in_app_checkbox_preference_key));
        onSharedPreferenceChanged(sharedPreferences, getString(R.string.notification_checkbox_preference_key));
        onSharedPreferenceChanged(sharedPreferences, getString(R.string.email_edittext_preference_key));
        onSharedPreferenceChanged(sharedPreferences, getString(R.string.name_edittext_preference_key));
        onSharedPreferenceChanged(sharedPreferences, getString(R.string.password_edittext_preference_key));

        mPreferenceLogout.setOnPreferenceClickListener(this);
        mPreferenceShare.setOnPreferenceClickListener(this);

        //mEditTextPreferenceEmail.setSummary(ConversaApp.getPreferences().getUserEmail());
        //mEditTextPreferenceName.setSummary(ConversaApp.getPreferences().getUserName());

        return super.onCreateView(paramLayoutInflater, paramViewGroup, paramBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        //unregister the preferenceChange listener
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        //unregister the preference change listener
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(sharedPreferences.getString(key, ""));
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else if (preference instanceof EditTextPreference) {
            EditTextPreference editTextPreference = (EditTextPreference) preference;
            if (key.equals(getString(R.string.email_edittext_preference_key))) {
//                try{
//                    String newEmail     = (String) newValue;
//                    newEmail = newEmail.replaceAll("\\t", "");
//                    newEmail = newEmail.replaceAll("\\n", "");
//                    newEmail = newEmail.replaceAll(" ", "");
//                    String emailResult  = Utils.checkEmail(getActivity(), newEmail);
//                    if (!emailResult.equals(getString(R.string.email_ok))) {
//                        Toast.makeText(getActivity(), emailResult, Toast.LENGTH_SHORT).show();
//                    } else {
//                        email = newEmail;
//                        CouchDB.updateUserAsync(newEmail, Const.UPDATE_EMAIL, new UserEmailUpdateListener(), getActivity(), false);
//                    }
//                } catch(ClassCastException e) {
//                    Logger.error("FragmentSettings Email", e.getMessage());
//                }
            } else if (key.equals(getString(R.string.name_edittext_preference_key))) {
//                try{
//                    String newUsername = (String) newValue;
//                    newUsername = newUsername.replaceAll("\\t", "");
//                    newUsername = newUsername.replaceAll("\\n", "");
//                    newUsername = newUsername.replaceAll(" ", "");
//                    String nameResult  = Utils.checkName(getActivity(), newUsername);
//                    if (!nameResult.equals(getString(R.string.name_ok))) {
//                        Toast.makeText(getActivity(), nameResult, Toast.LENGTH_SHORT).show();
//                    } else {
//                        username = newUsername;
//                        CouchDB.updateUserAsync(newUsername, Const.UPDATE_USERNAME, new UserUsernameUpdateListener(), getActivity(), false);
//                    }
//                } catch(ClassCastException e) {
//                    Logger.error("FragmentSettings Name", e.getMessage());
//                }
            } else {
//                try {
//                    String newPassword    = (String) newValue;
//                    String passwordResult = Utils.checkPassword(getActivity(), newPassword);
//                    if (!passwordResult.equals(getString(R.string.password_ok))) {
//                        Toast.makeText(getActivity(), passwordResult, Toast.LENGTH_SHORT).show();
//                    } else {
//                        CouchDB.updateUserAsync(FileManagement.md5(newPassword), Const.UPDATE_PASSWORD, new UserPasswordUpdateListener(), getActivity(), false);
//                    }
//                } catch (ClassCastException e) {
//                    Logger.error("FragmentSettings Email", e.getMessage());
//                }
            }
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if(preference.getKey().equals(getString(R.string.logout_preference_key))) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.logout_message)
                    .setPositiveButton(R.string.logout_preference, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            appLogout();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            builder.show();
            return true;
        } else if (preference.getKey().equals(getString(R.string.share_preference_key))) {
            Intent intent=new Intent(android.content.Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            // Add data to the intent, the receiving app will decide what to do with it.
            String subject = getActivity().getString(R.string.settings_using_conversa) + " " + getActivity().getString(R.string.app_name);
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            String body = getActivity().getString(R.string.settings_body1_conversa) + " " +
                    getActivity().getString(R.string.app_name) + " " + getActivity().getString(R.string.settings_body2_conversa);
            intent.putExtra(Intent.EXTRA_TEXT, body);
            ((AppCompatActivity)getActivity()).startActivity(Intent.createChooser(intent,
                    getActivity().getString(R.string.settings_share_conversa)));
            return true;
        }

        return false;
    }

    public void appLogout() {
        boolean result = ConversaApp.getDB().deleteDatabase();
        if(result)
            Logger.error("Logout", getActivity().getString(R.string.settings_logout_succesful));
        else
            Logger.error("Logout", getActivity().getString(R.string.settings_logout_error));

//        try {
//            GoogleCloudMessaging.getInstance((AppCompatActivity) getActivity()).unregister();
//        } catch (IOException e) {
//
//        }
        ParsePush.unsubscribeInBackground(ConversaApp.getPreferences().getCustomerId() + "-pbc");
        ParsePush.unsubscribeInBackground(ConversaApp.getPreferences().getCustomerId() + "-pvt");
        Account.logOut();

        AppCompatActivity fromActivity = (AppCompatActivity)getActivity();
        Intent goToSignIn = new Intent(fromActivity, ActivitySignIn.class);
        ConversaApp.getPreferences().cleanSharedPreferences();
        goToSignIn.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //ActivityMain.sInstance.logOut();
        fromActivity.startActivity(goToSignIn);
        fromActivity.finish();

//        ParsePush.unsubscribeInBackground(ConversaApp.getPreferences().getCustomerId() + "-pbc", new SaveCallback() {
//            @Override
//            public void done(ParseException e) {
//                if (e == null) {
//                    ParsePush.unsubscribeInBackground(ConversaApp.getPreferences().getCustomerId() + "-pvt", new SaveCallback() {
//                        @Override
//                        public void done(ParseException e) {
//                            if (e == null) {
//                                Account.logOut();
//                                AppCompatActivity fromActivity = ActivityMain.sInstance;
//                                Intent goToSignIn = new Intent(fromActivity, ActivitySignIn.class);
//                                ConversaApp.getPreferences().cleanSharedPreferences();
//                                goToSignIn.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                //ActivityMain.sInstance.logOut();
//                                fromActivity.startActivity(goToSignIn);
//                                fromActivity.finish();
//                            } else {
//
//                            }
//                        }
//                    });
//                } else {
//
//                }
//            }
//        });
    }


}

//
//    private class UserEmailUpdateListener implements ResultListener<Boolean> {
//        public UserEmailUpdateListener() {}
//        @Override
//        public void onResultsSuccess(Boolean result) {
//            if(result) {
//                if( email != null && !email.isEmpty() ) {
//                    ConversaApp.getDB().setUserLoggedInEmail(email);
//                    ConversaApp.getPreferences().setUserEmail(email);
//                    mEditTextPreferenceEmail.setSummary(email);
//                    email = null;
//                }
//                Toast.makeText(getActivity(), getActivity().getString(R.string.settings_email_succesful), Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(getActivity(), getActivity().getString(R.string.settings_email_error), Toast.LENGTH_SHORT).show();
//            }
//        }
//        @Override
//        public void onResultsFail() {
//            Toast.makeText(getActivity(), getActivity().getString(R.string.settings_email_error), Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private class UserUsernameUpdateListener implements ResultListener<Boolean> {
//        public UserUsernameUpdateListener() {}
//        @Override
//        public void onResultsSuccess(Boolean result) {
//            if(result) {
//                if( username != null && !username.isEmpty() ) {
//                    ConversaApp.getDB().setUserLoggedInUsername(username);
//                    ConversaApp.getPreferences().setUserName(username);
//                    mEditTextPreferenceName.setSummary(username);
//                    username = null;
//                }
//
//                Toast.makeText(getActivity(), getActivity().getString(R.string.settings_name_succesful), Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(getActivity(), getActivity().getString(R.string.settings_name_error), Toast.LENGTH_SHORT).show();
//            }
//        }
//        @Override
//        public void onResultsFail() {
//            Toast.makeText(getActivity(), getActivity().getString(R.string.settings_name_error), Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private class UserPasswordUpdateListener implements ResultListener<Boolean> {
//        public UserPasswordUpdateListener() {}
//        @Override
//        public void onResultsSuccess(Boolean result) {
//            if(result) {
//                Toast.makeText(getActivity(), getActivity().getString(R.string.settings_password_succesful), Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(getActivity(), getActivity().getString(R.string.settings_password_error), Toast.LENGTH_SHORT).show();
//            }
//        }
//        @Override
//        public void onResultsFail() {
//            Toast.makeText(getActivity(), getActivity().getString(R.string.settings_password_error), Toast.LENGTH_SHORT).show();
//        }
//    }
//}