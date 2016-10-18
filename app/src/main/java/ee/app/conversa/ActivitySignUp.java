package ee.app.conversa;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.parse.ParseException;
import com.parse.SignUpCallback;

import java.sql.Date;
import java.util.Calendar;

import ee.app.conversa.extendables.BaseActivity;
import ee.app.conversa.model.parse.Account;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Utils;

import static ee.app.conversa.R.id.btnSignUpUp;
import static ee.app.conversa.R.id.tilBirthdaySignUp;

/**
 * Created by edgargomez on 8/12/16.
 */
public class ActivitySignUp extends BaseActivity implements View.OnClickListener,
        View.OnFocusChangeListener {

    private Button mBtnSignUpUp;
    private EditText mEtSignUpEmail;
    private EditText mEtSignUpPassword;
    private EditText mEtSignUpBirthday;
    private TextInputLayout mTilSignUpEmail;
    private TextInputLayout mTilSignUpPassword;
    private TextInputLayout mTilSignUpBirthday;

    private RadioGroup radioSexGroup;

    private int mYear = -1;
    private int mMonth = -1;
    private int mDay = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initialization();
    }

    @Override
    protected void initialization() {
        super.initialization();
        mEtSignUpEmail = (EditText) findViewById(R.id.etSignUpEmail);
        mEtSignUpPassword = (EditText) findViewById(R.id.etSignUpPassword);
        mEtSignUpBirthday = (EditText) findViewById(R.id.etSignUpBirthday);

        mTilSignUpEmail = (TextInputLayout) findViewById(R.id.tilEmailSignUp);
        mTilSignUpPassword = (TextInputLayout) findViewById(R.id.tilPasswordSignUp);
        mTilSignUpBirthday = (TextInputLayout) findViewById(tilBirthdaySignUp);

        mBtnSignUpUp = (Button) findViewById(btnSignUpUp);

        mEtSignUpEmail.addTextChangedListener(new MyTextWatcher(mEtSignUpEmail));
        mEtSignUpPassword.addTextChangedListener(new MyTextWatcher(mEtSignUpPassword));
        mEtSignUpBirthday.addTextChangedListener(new MyTextWatcher(mEtSignUpBirthday));

        mTilSignUpEmail.setOnClickListener(this);
        mTilSignUpPassword.setOnClickListener(this);
        mTilSignUpBirthday.setOnClickListener(this);
        mEtSignUpBirthday.setOnClickListener(this);
        mEtSignUpBirthday.setOnFocusChangeListener(this);

        radioSexGroup = (RadioGroup)findViewById(R.id.rgGender);

        mBtnSignUpUp.setOnClickListener(this);
        mBtnSignUpUp.setTypeface(ConversaApp.getInstance(this).getTfRalewayMedium());
    }

    @Override
    public void yesInternetConnection() {
        super.yesInternetConnection();
        if (validateForm()) {
            mBtnSignUpUp.setEnabled(true);
        }
    }

    @Override
    public void noInternetConnection() {
        super.noInternetConnection();
        mBtnSignUpUp.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tilEmailSignUp: {
                mEtSignUpEmail.requestFocus();
                break;
            }
            case R.id.tilPasswordSignUp: {
                mEtSignUpPassword.requestFocus();
                break;
            }
            case R.id.etSignUpBirthday:
            case R.id.tilBirthdaySignUp: {
                showDate();
                break;
            }
            case btnSignUpUp: {
                Account user = new Account();

                String username = TextUtils.split(mEtSignUpEmail.getText().toString(), "@")[0];

                user.setEmail(mEtSignUpEmail.getText().toString());
                user.setUsername(username);
                user.setPassword(mEtSignUpPassword.getText().toString());
                user.put(Const.kUserTypeKey, 1);

//                    Calendar newDate = Calendar.getInstance();
//                    newDate.set(mYear, mMonth, mDay);
//                    user.put(Const.kUserBirthday, new SimpleDateFormat("dd-MM-yyyy",
//                            DynamicLanguage.getSelectedLocale(getApplicationContext()))
//                            .format(newDate.getTime()));

                int selectedId = radioSexGroup.getCheckedRadioButtonId();

                if (findViewById(selectedId).getId() == R.id.rbMale) {
                    // user.put(Const.kUserGender, selectedGender);
                } else {
                    // user.put(Const.kUserGender, selectedGender);
                }

                final ProgressDialog progress = new ProgressDialog(this);
                progress.show();

                user.signUpInBackground(new SignUpCallback() {
                    public void done(ParseException e) {
                        progress.dismiss();
                        if (e == null) {
                            // Hooray! Let them use the app now.
                            AuthListener(true, null);
                        } else {
                            // Sign up didn't succeed. Look at the ParseException
                            // to figure out what went wrong
                            AuthListener(false, e);
                        }
                    }
                });
                break;
            }
        }
    }

    private void showDate() {
        Utils.hideKeyboard(this);


        if (mYear == -1) {
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth)
                    {
                        mYear = year;
                        mMonth = monthOfYear;
                        mDay = dayOfMonth;
                        mEtSignUpBirthday.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.getDatePicker().setMaxDate(new Date(System.currentTimeMillis()).getTime());
        datePickerDialog.show();
    }

    private boolean validateForm() {
        if (mEtSignUpEmail.getText().toString().isEmpty() || mEtSignUpPassword.getText().toString().isEmpty()
                || mEtSignUpBirthday.getText().toString().isEmpty()) {
            mBtnSignUpUp.setEnabled(false);
        } else if (mTilSignUpEmail.isErrorEnabled() || mTilSignUpPassword.isErrorEnabled() ||
                mTilSignUpBirthday.isErrorEnabled()) {
            mBtnSignUpUp.setEnabled(false);
        } else {
            mBtnSignUpUp.setEnabled(true);
            return true;
        }

        return false;
    }

    private void isEmailValid(String email) {
        TextInputLayout layout = mTilSignUpEmail;

        if (email.isEmpty()) {
            layout.setErrorEnabled(true);
            layout.setError(getString(R.string.sign_email_length_error));
        } else {
            if (Utils.checkEmail(email)) {
                layout.setErrorEnabled(false);
                layout.setError("");
            } else {
                layout.setErrorEnabled(true);
                layout.setError(getString(R.string.sign_email_not_valid_error));
            }
        }
    }

    private void isPasswordValid(String password) {
        TextInputLayout layout = mTilSignUpPassword;

        if (password.isEmpty()) {
            layout.setErrorEnabled(true);
            layout.setError(getString(R.string.signup_password_empty_error));
        } else {
            if (password.length() < 6) {
                layout.setErrorEnabled(true);
                layout.setError(getString(R.string.signup_password_length_error));
            } else {
                if (Utils.checkPassword(password)) {
                    layout.setErrorEnabled(false);
                    layout.setError("");
                } else {
                    layout.setErrorEnabled(true);
                    layout.setError(getString(R.string.signup_password_regex_error));
                }
            }
        }
    }

    private void isBirthdayValid(String birthday) {
        TextInputLayout layout = mTilSignUpBirthday;

        if (birthday.isEmpty()) {
            layout.setErrorEnabled(true);
            layout.setError(getString(R.string.signup_birthday_empty_error));
        } else {
            int result = Utils.checkDate(birthday, this);
            if (result == 0) {
                layout.setErrorEnabled(false);
                layout.setError("");
            } else if (result == 1) {
                layout.setErrorEnabled(true);
                layout.setError(getString(R.string.signup_birthday_invalid_error));
            } else {
                layout.setErrorEnabled(true);
                layout.setError(getString(R.string.signup_birthday_old_error));
            }
        }
    }

    public void AuthListener(boolean result, ParseException error) {
        if(result) {
            Intent intent = new Intent(this, ActivityMain.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            int colorPositive;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                colorPositive = getResources().getColor(R.color.default_black, null);
            } else {
                colorPositive = getResources().getColor(R.color.default_black);
            }

            MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                    .title("")
                    .content(getString(R.string.signup_register_error))
                    .positiveText(getString(android.R.string.ok))
                    .positiveColor(colorPositive)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                        }
                    });

            MaterialDialog dialog = builder.build();
            dialog.show();
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            return;
        }

        switch(v.getId()) {
            case R.id.etSignUpBirthday:
                showDate();
                break;
        }
    }

    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.etSignUpEmail:
                    isEmailValid(editable.toString());
                    break;
                case R.id.etSignUpPassword:
                    isPasswordValid(editable.toString());
                    break;
                case R.id.etSignUpBirthday:
                    isBirthdayValid(editable.toString());
                    break;
            }

            validateForm();
        }
    }
}