package ee.app.conversa.settings;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
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
public class ActivitySettingsAccount extends ConversaActivity implements View.OnClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private LightTextView mLtvName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_account);
        initialization();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ConversaApp.getInstance(this)
                .getPreferences()
                .getSharePreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ConversaApp.getInstance(this)
                .getPreferences()
                .getSharePreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void initialization() {
        super.initialization();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.preferences__account);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LightTextView mLtvEmail = (LightTextView) findViewById(R.id.ltvEmail);
        mLtvName = (LightTextView) findViewById(R.id.ltvName);

        mLtvEmail.setText(Account.getCurrentUser().getEmail());
        mLtvName.setText(ConversaApp.getInstance(getApplicationContext()).getPreferences().getAccountDisplayName());

        findViewById(R.id.rlName).setOnClickListener(this);
        findViewById(R.id.rlPassword).setOnClickListener(this);
        findViewById(R.id.rlCleanRecentSearches).setOnClickListener(this);
        findViewById(R.id.rlLogOut).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rlName: {
                new MaterialDialog.Builder(this)
                        .title(getString(R.string.sett_account_name_alert_title))
                        .positiveText(getString(R.string.action_change))
                        .positiveColorRes(R.color.green)
                        .negativeText(getString(android.R.string.cancel))
                        .negativeColorRes(R.color.black)
                        .inputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
                        .input(getString(R.string.name), "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                dialog.dismiss();
                                onPreferenceChange(PreferencesKeys.ACCOUNT_NAME_KEY, input.toString());
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                break;
            }
            case R.id.rlPassword: {
                new MaterialDialog.Builder(this)
                        .title(getString(R.string.sett_account_password_alert_title))
                        .positiveText(getString(R.string.action_change))
                        .positiveColorRes(R.color.green)
                        .negativeText(getString(android.R.string.cancel))
                        .negativeColorRes(R.color.black)
                        .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                        .input(getString(R.string.password), "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                dialog.dismiss();
                                onPreferenceChange(PreferencesKeys.ACCOUNT_PASSWORD_KEY, input.toString());
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                break;
            }
            case R.id.rlCleanRecentSearches: {
                new MaterialDialog.Builder(this)
                        .content(getString(R.string.recent_searches_message))
                        .positiveText(getString(R.string.recent_searches_ok))
                        .negativeText(getString(android.R.string.no))
                        .positiveColorRes(R.color.green)
                        .negativeColorRes(R.color.black)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                ConversaApp.getInstance(getApplicationContext()).getDB().clearRecentSearches();
                                dialog.dismiss();
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                break;
            }
            case R.id.rlLogOut: {
                new MaterialDialog.Builder(this)
                        .content(getString(R.string.logout_message))
                        .positiveText(getString(R.string.logout_ok))
                        .negativeText(getString(android.R.string.no))
                        .positiveColorRes(R.color.red)
                        .negativeColorRes(R.color.black)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                                AppActions.appLogout(getApplicationContext(), false);
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                break;
            }
        }
    }


    public boolean onPreferenceChange(String key, String newValue) {
        switch (key) {
            case PreferencesKeys.ACCOUNT_NAME_KEY: {
                String name = newValue;
                name = name.replaceAll("\\t", "");
                name = name.replaceAll("\\n", "");
                final String newName = name;

                if (newName.isEmpty()) {
                    showErrorMessage(getString(R.string.common_field_required));
                } else {
                    HashMap<String, String> params = new HashMap<>();
                    params.put("displayName", newName);
                    params.put("objectId", ConversaApp.getInstance(this).getPreferences().getAccountCustomerId());
                    ParseCloud.callFunctionInBackground("updateCustomerName", params, new FunctionCallback<Integer>() {
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
                    showErrorMessage(getString(R.string.common_field_required));
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PreferencesKeys.ACCOUNT_DISPLAY_NAME_KEY)) {
            mLtvName.setText(ConversaApp.getInstance(getApplicationContext())
                    .getPreferences()
                    .getAccountDisplayName());
        }
    }
}