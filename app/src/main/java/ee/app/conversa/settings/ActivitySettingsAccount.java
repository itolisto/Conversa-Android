package ee.app.conversa.settings;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.SaveCallback;

import java.util.HashMap;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.R;
import ee.app.conversa.extendables.ConversaActivity;
import ee.app.conversa.model.parse.Account;
import ee.app.conversa.utils.AppActions;
import ee.app.conversa.utils.Utils;
import ee.app.conversa.view.LightTextView;

/**
 * Created by edgargomez on 9/9/16.
 */
public class ActivitySettingsAccount extends ConversaActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_account);
        initialization();
    }

    @Override
    protected void initialization() {
        super.initialization();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.preferences__account);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LightTextView mLtvEmail = (LightTextView) findViewById(R.id.ltvEmail);
        LightTextView mLtvName = (LightTextView) findViewById(R.id.ltvName);

        mLtvEmail.setText(Account.getCurrentUser().getEmail());
        mLtvName.setText(ConversaApp.getInstance(getApplicationContext()).getPreferences().getAccountDisplayName());

        findViewById(R.id.rlEmail).setOnClickListener(this);
        findViewById(R.id.rlName).setOnClickListener(this);
        findViewById(R.id.rlPassword).setOnClickListener(this);
        findViewById(R.id.rlCleanRecentSearches).setOnClickListener(this);
        findViewById(R.id.rlLogOut).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rlEmail: {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_settings_account, null);
                final TextInputEditText edt = (TextInputEditText) dialogView.findViewById(R.id.edit1);
                edt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

                dialogBuilder.setTitle(getString(R.string.sett_account_email_alert_title));
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
            case R.id.rlName: {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_settings_account, null);
                final TextInputEditText edt = (TextInputEditText) dialogView.findViewById(R.id.edit1);
                edt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

                dialogBuilder.setTitle(getString(R.string.sett_account_name_alert_title));
                dialogBuilder.setPositiveButton(getString(R.string.action_change), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        onPreferenceChange(PreferencesKeys.ACCOUNT_NAME_KEY, edt.getText().toString());
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
            case R.id.rlPassword: {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_settings_account, null);
                final TextInputEditText edt = (TextInputEditText) dialogView.findViewById(R.id.edit1);
                edt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                dialogBuilder.setTitle(getString(R.string.sett_account_password_alert_title));
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
            case R.id.rlCleanRecentSearches: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.recent_searches_message));

                String positiveText = getString(R.string.recent_searches_ok);
                builder.setPositiveButton(positiveText,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ConversaApp.getInstance(getApplicationContext()).getDB().clearRecentSearches();
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
            case R.id.rlLogOut: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.logout_message));

                String positiveText = getString(R.string.logout_ok);
                builder.setPositiveButton(positiveText,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                AppActions.appLogout(getApplicationContext(), true);
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
        }
    }


    public boolean onPreferenceChange(String key, String newValue) {
        switch (key) {
            case PreferencesKeys.ACCOUNT_EMAIL_KEY: {
                final String oldEmail = Account.getCurrentUser().getEmail();
                String email = newValue;
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
            case PreferencesKeys.ACCOUNT_NAME_KEY: {
                String name = newValue;
                name = name.replaceAll("\\t", "");
                name = name.replaceAll("\\n", "");
                final String newName = name;

                if (newName.isEmpty()) {
                    showErrorMessage(getString(R.string.sign_name_length_error));
                } else {
                    HashMap<String, String> params = new HashMap<>();
                    params.put("displayName", newName);
                    ParseCloud.callFunctionInBackground("updateDisplayName", params, new FunctionCallback<Integer>() {
                        @Override
                        public void done(Integer object, ParseException e) {
                            if (e == null) {
                                ConversaApp.getInstance(getApplicationContext())
                                        .getPreferences()
                                        .setAccountDisplayName(newName, true);
                                showSuccessMessage(getString(R.string.settings_name_succesful));
                            } else {
                                showErrorMessage(getString(R.string.settings_name_error));
                            }
                        }
                    });
                }
                // Return false as we don't wanna save/update this preference
                return false;
            }
            case PreferencesKeys.ACCOUNT_PASSWORD_KEY: {
                if (newValue.isEmpty()) {
                    showErrorMessage(getString(R.string.signup_password_empty_error));
                } else {
                    if (Utils.checkPassword(newValue)) {
                        Account.getCurrentUser().setPassword(newValue);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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