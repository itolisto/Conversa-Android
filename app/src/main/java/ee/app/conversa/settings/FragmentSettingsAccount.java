package ee.app.conversa.settings;

import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;

import com.parse.ParseException;
import com.parse.SaveCallback;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.R;
import ee.app.conversa.model.parse.Account;
import ee.app.conversa.utils.AppActions;
import ee.app.conversa.utils.Utils;

/**
 * Created by edgargomez on 9/9/16.
 */
public class FragmentSettingsAccount extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        addPreferencesFromResource(R.xml.fragment_settings_account);

        this.findPreference(PreferencesKeys.ACCOUNT_EMAIL_KEY)
                .setOnPreferenceClickListener(this);
        this.findPreference(PreferencesKeys.ACCOUNT_PASSWORD_KEY)
                .setOnPreferenceClickListener(this);
        this.findPreference(PreferencesKeys.ACCOUNT_CLEAR_RECENT_KEY)
                .setOnPreferenceClickListener(this);
        this.findPreference(PreferencesKeys.ACCOUNT_LOGOUT_KEY)
                .setOnPreferenceClickListener(this);

        this.findPreference(PreferencesKeys.ACCOUNT_EMAIL_KEY).setSummary(Account.getCurrentUser().getEmail());
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ActivityPreferences)getActivity()).getSupportActionBar().setTitle(R.string.preferences__account);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case PreferencesKeys.ACCOUNT_EMAIL_KEY: {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

                LayoutInflater inflater = getActivity().getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_settings_account, null);
                final TextInputEditText edt = (TextInputEditText) dialogView.findViewById(R.id.edit1);
                edt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

                dialogBuilder.setTitle(getString(R.string.sett_account_email_title));
                dialogBuilder.setPositiveButton(getString(R.string.action_change), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        onPreferenceChange(PreferencesKeys.ACCOUNT_EMAIL_KEY, edt.getText().toString());
                    }
                });
                dialogBuilder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
                dialogBuilder.setView(dialogView);
                AlertDialog b = dialogBuilder.create();
                b.show();
                break;
            }
            case PreferencesKeys.ACCOUNT_PASSWORD_KEY: {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

                LayoutInflater inflater = getActivity().getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_settings_account, null);
                final TextInputEditText edt = (TextInputEditText) dialogView.findViewById(R.id.edit1);
                edt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                dialogBuilder.setTitle(getString(R.string.sett_account_password_title));
                dialogBuilder.setPositiveButton(getString(R.string.action_change), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        onPreferenceChange(PreferencesKeys.ACCOUNT_PASSWORD_KEY, edt.getText().toString());
                    }
                });
                dialogBuilder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
                dialogBuilder.setView(dialogView);
                AlertDialog b = dialogBuilder.create();
                b.show();
                break;
            }
            case PreferencesKeys.ACCOUNT_CLEAR_RECENT_KEY: {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(getString(R.string.recent_searches_message));

                String positiveText = getString(R.string.recent_searches_ok);
                builder.setPositiveButton(positiveText,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ConversaApp.getInstance(getActivity()).getDB().clearRecentSearches();
                                dialog.dismiss();
                            }
                        });

                String negativeText = getString(android.R.string.no);
                builder.setNegativeButton(negativeText,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            }
            case PreferencesKeys.ACCOUNT_LOGOUT_KEY: {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(getString(R.string.logout_message));

                String positiveText = getString(R.string.logout_ok);
                builder.setPositiveButton(positiveText,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                AppActions.appLogout(getActivity(), true);
                            }
                        });

                String negativeText = getString(android.R.string.no);
                builder.setNegativeButton(negativeText,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            }
            default:
                return false;
        }

        return true;
    }

    public boolean onPreferenceChange(String key, String newValue) {
        switch (key) {
            case PreferencesKeys.ACCOUNT_EMAIL_KEY: {
                final String oldEmail = Account.getCurrentUser().getEmail();
                String email = (String) newValue;
                email = email.replaceAll("\\t", "");
                email = email.replaceAll("\\n", "");
                email = email.replaceAll(" ", "");
                final String newEmail = email;

                if (newEmail.isEmpty()) {
                    showErrorMessage(getString(R.string.sign_email_length_error));
                } else {
                    if (Utils.checkEmail(newEmail)) {
                        Account.getCurrentUser().setEmail(newEmail);
                        Account.getCurrentUser().saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    findPreference(PreferencesKeys.ACCOUNT_EMAIL_KEY).setSummary(newEmail);
                                    showSuccessMessage(getString(R.string.settings_email_succesful));
                                } else {
                                    Account.getCurrentUser().setEmail(oldEmail);
                                    showErrorMessage(getString(R.string.settings_email_error));
                                }
                            }
                        });
                    } else {
                        showErrorMessage(getString(R.string.sign_email_not_valid_error));
                    }
                }
                // Return false as we don't wanna save/update this preference
                return false;
            }
            case PreferencesKeys.ACCOUNT_PASSWORD_KEY: {
                String newPassword = (String) newValue;

                if (newPassword.isEmpty()) {
                    showErrorMessage(getString(R.string.signup_password_empty_error));
                } else {
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
                }

                return false;
            }
            default:
                return false;
        }
    }

    private void showSuccessMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message);

        String positiveText = getString(android.R.string.ok);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showErrorMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message);

        String positiveText = getString(android.R.string.ok);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}