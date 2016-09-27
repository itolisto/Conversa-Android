package ee.app.conversa.settings;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.onesignal.OneSignal;
import com.parse.ParseException;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Collection;

import ee.app.conversa.ActivitySignIn;
import ee.app.conversa.ConversaApp;
import ee.app.conversa.R;
import ee.app.conversa.dialog.CustomDialog;
import ee.app.conversa.management.ably.Connection;
import ee.app.conversa.model.parse.Account;
import ee.app.conversa.utils.Logger;
import ee.app.conversa.utils.Utils;

/**
 * Created by edgargomez on 9/9/16.
 */
public class FragmentSettingsAccount extends PreferenceFragment implements Preference.OnPreferenceClickListener,
Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        addPreferencesFromResource(R.xml.fragment_settings_account);

        this.findPreference(PreferencesKeys.ACCOUNT_EMAIL_KEY)
                .setOnPreferenceChangeListener(this);
        this.findPreference(PreferencesKeys.ACCOUNT_PASSWORD_KEY)
                .setOnPreferenceChangeListener(this);
        this.findPreference(PreferencesKeys.ACCOUNT_CLEAR_RECENT_KEY)
                .setOnPreferenceClickListener(this);
        this.findPreference(PreferencesKeys.ACCOUNT_BLOCKED_KEY)
                .setOnPreferenceClickListener(this);
        this.findPreference(PreferencesKeys.ACCOUNT_LOGOUT_KEY)
                .setOnPreferenceClickListener(this);

        this.findPreference(PreferencesKeys.ACCOUNT_EMAIL_KEY).setSummary(Account.getCurrentUser().getEmail());
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ActivityPreferences)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((ActivityPreferences)getActivity()).getSupportActionBar().setTitle(R.string.preferences__account);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case PreferencesKeys.ACCOUNT_CLEAR_RECENT_KEY: {
                int colorNegative, colorPositive;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    colorPositive = getActivity().getResources().getColor(android.R.color.holo_red_light, null);
                    colorNegative = getActivity().getResources().getColor(R.color.default_black, null);
                } else {
                    colorPositive = getActivity().getResources().getColor(android.R.color.holo_red_light);
                    colorNegative = getActivity().getResources().getColor(R.color.default_black);
                }

                final CustomDialog dialog = new CustomDialog(getActivity());
                dialog.setTitle(null)
                        .setMessage(getString(R.string.recent_searches_message))
                        .setupNegativeButton(getString(R.string.cancel), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        })
                        .setNegativeColor(colorNegative)
                        .setupPositiveButton(getString(R.string.recent_searches_ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ConversaApp.getInstance(getActivity()).getDB().clearRecentSearches();
                                dialog.dismiss();
                            }
                        })
                        .setPositiveColor(colorPositive);
                dialog.show();
                break;
            }
            case PreferencesKeys.ACCOUNT_BLOCKED_KEY: {

                break;
            }
            case PreferencesKeys.ACCOUNT_LOGOUT_KEY: {
                int colorNegative, colorPositive;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    colorPositive = getActivity().getResources().getColor(android.R.color.holo_red_light, null);
                    colorNegative = getActivity().getResources().getColor(R.color.default_black, null);
                } else {
                    colorPositive = getActivity().getResources().getColor(android.R.color.holo_red_light);
                    colorNegative = getActivity().getResources().getColor(R.color.default_black);
                }

                final CustomDialog dialog = new CustomDialog(getActivity());
                dialog.setTitle(getString(R.string.logout_message))
                        .setMessage(null)
                        .setupNegativeButton(getString(R.string.cancel), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        })
                        .setNegativeColor(colorNegative)
                        .setupPositiveButton(getString(R.string.logout_ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                appLogout();
                            }
                        })
                        .setPositiveColor(colorPositive);
                dialog.show();
                break;
            }
            default:
                return false;
        }

        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case PreferencesKeys.ACCOUNT_EMAIL_KEY: {
                String email = (String) newValue;
                email = email.replaceAll("\\t", "");
                email = email.replaceAll("\\n", "");
                email = email.replaceAll(" ", "");
                final String newEmail = email;

                if (Utils.checkEmail(newEmail)) {
                    Account.getCurrentUser().setEmail(newEmail);
                    Account.getCurrentUser().saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                findPreference(PreferencesKeys.ACCOUNT_EMAIL_KEY).setSummary(newEmail);
                                showSuccessMessage(getString(R.string.settings_email_succesful));
                            } else {
                                showErrorMessage(getString(R.string.settings_email_error));
                            }
                        }
                    });
                } else {
                    showErrorMessage(getString(R.string.sign_email_not_valid_error));
                }

                return false;
            }
            case PreferencesKeys.ACCOUNT_PASSWORD_KEY: {
                String newPassword = (String) newValue;
                if (Utils.checkPassword(newPassword)) {
                    Account.getCurrentUser().setPassword(newPassword);
                    Account.getCurrentUser().saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                showSuccessMessage(getString(R.string.settings_password_succesful));
                            } else {
                                showErrorMessage(getString(R.string.settings_password_error));
                            }
                        }
                    });
                } else {
                    showErrorMessage(getString(R.string.signup_password_regex_error));
                }

                return false;
            }
            default:
                return false;
        }
    }

    private void showSuccessMessage(String message) {
        final CustomDialog dialog = new CustomDialog(getActivity());
        dialog.setTitle(null)
                .setMessage(message)
                .setupNegativeButton(null, null)
                .setupPositiveButton(getString(android.R.string.ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
        dialog.show();
    }

    private void showErrorMessage(String message) {
        final CustomDialog dialog = new CustomDialog(getActivity());
        dialog.setTitle(null)
                .setMessage(message)
                .setupNegativeButton(null, null)
                .setupPositiveButton(getString(android.R.string.ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
        dialog.show();
    }

    private void appLogout() {
        boolean result = ConversaApp.getInstance(getActivity()).getDB().deleteDatabase();
        if(result)
            Logger.error("Logout", getActivity().getString(R.string.settings_logout_succesful));
        else
            Logger.error("Logout", getActivity().getString(R.string.settings_logout_error));

        OneSignal.setSubscription(false);
        Collection<String> tempList = new ArrayList<>();
        tempList.add("upbc");
        tempList.add("upvt");
        OneSignal.deleteTags(tempList);
        OneSignal.clearOneSignalNotifications();
        Connection.getInstance().disconnectAbly();

        Account.logOut();
        AppCompatActivity fromActivity = (AppCompatActivity)getActivity();
        Intent goToSignIn = new Intent(fromActivity, ActivitySignIn.class);
        goToSignIn.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        goToSignIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ConversaApp.getInstance(getActivity()).getPreferences().cleanSharedPreferences();
        fromActivity.startActivity(goToSignIn);
        fromActivity.finish();
    }

}