package ee.app.conversa;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.google.android.material.textfield.TextInputLayout;
import com.parse.ParseException;
import com.parse.SignUpCallback;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import ee.app.conversa.extendables.BaseActivity;
import ee.app.conversa.model.parse.Account;
import ee.app.conversa.settings.language.DynamicLanguage;
import ee.app.conversa.utils.AppActions;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Utils;
import ee.app.conversa.view.LightTextView;
import ee.app.conversa.view.URLSpanNoUnderline;

import static ee.app.conversa.R.id.btnSignUpUp;
import static ee.app.conversa.R.id.tilBirthdaySignUp;

/**
 * Created by edgargomez on 8/12/16.
 */
public class ActivitySignUp extends BaseActivity implements View.OnClickListener, View.OnFocusChangeListener {

    private Button mBtnSignUpUp;
    private EditText mEtSignUpEmail;
    private EditText mEtSignUpPassword;
    private EditText mEtSignUpBirthday;

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
        mBtnSignUpUp = (Button) findViewById(btnSignUpUp);
        radioSexGroup = (RadioGroup)findViewById(R.id.rgGender);
        mEtSignUpEmail = (EditText) findViewById(R.id.etSignUpEmail);
        mEtSignUpPassword = (EditText) findViewById(R.id.etSignUpPassword);
        mEtSignUpBirthday = (EditText) findViewById(R.id.etSignUpBirthday);

        TextInputLayout mTilSignUpEmail = (TextInputLayout) findViewById(R.id.tilEmailSignUp);
        TextInputLayout mTilSignUpPassword = (TextInputLayout) findViewById(R.id.tilPasswordSignUp);
        TextInputLayout mTilSignUpBirthday = (TextInputLayout) findViewById(tilBirthdaySignUp);

        mTilSignUpEmail.setOnClickListener(this);
        mTilSignUpPassword.setOnClickListener(this);
        mTilSignUpBirthday.setOnClickListener(this);
        mEtSignUpBirthday.setOnClickListener(this);
        mEtSignUpBirthday.setOnFocusChangeListener(this);

        mBtnSignUpUp.setOnClickListener(this);
        mBtnSignUpUp.setTypeface(ConversaApp.getInstance(this).getTfRalewayMedium());

        LightTextView mLtvTermsPrivacy = (LightTextView) findViewById(R.id.ltvTermsPrivacy);
        String text = mLtvTermsPrivacy.getText().toString();

        String language = ConversaApp.getInstance(this).getPreferences().getLanguage();

        if (language.equals("zz")) {
            if (Locale.getDefault().getLanguage().startsWith("es")) {
                language = "es";
            } else {
                language = "en";
            }
        }

        int indexTerms;
        int indexPrivacy;

        if (language.equals("es")) {
            indexTerms = TextUtils.indexOf(text, "TERMINOS");
            indexPrivacy = TextUtils.indexOf(text, "POLITICAS");
        } else {
            indexTerms = TextUtils.indexOf(text, "TERMS");
            indexPrivacy = TextUtils.indexOf(text, "PRIVACY");
        }

        Spannable styledString = new SpannableString(text);
        // url
        styledString.setSpan(new URLSpanNoUnderline("http://conversachat.com/terms"), indexTerms, indexTerms + (language.equals("es") ? 8 : 5), 0);
        styledString.setSpan(new URLSpanNoUnderline("http://conversachat.com/privacy"), indexPrivacy, text.length(), 0);
        // change text color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            styledString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.green, null)),
                    indexTerms, indexTerms + (language.equals("es") ? 8 : 5), 0);
            styledString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.green, null)),
                    indexPrivacy, text.length(), 0);
        } else {
            styledString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.green)),
                    indexTerms, indexTerms + (language.equals("es") ? 8 : 5), 0);
            styledString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.green)),
                    indexPrivacy, text.length(), 0);
        }
        // this step is mandated for the url and clickable styles.
        mLtvTermsPrivacy.setMovementMethod(LinkMovementMethod.getInstance());
        mLtvTermsPrivacy.setText(styledString);
    }

    @Override
    public void yesInternetConnection() {
        super.yesInternetConnection();
        mBtnSignUpUp.setEnabled(true);
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
                if (validateForm()) {
                    Account user = new Account();
                    String email = mEtSignUpEmail.getText().toString();
                    user.setEmail(email);
                    user.setUsername(email);
                    user.setPassword(mEtSignUpPassword.getText().toString());
                    user.put(Const.kUserTypeKey, 1);

                    Calendar newDate = Calendar.getInstance();
                    newDate.set(mYear, mMonth, mDay);

                    user.put(Const.kUserBirthday, newDate.getTimeInMillis());

                    int selectedId = radioSexGroup.getCheckedRadioButtonId();

                    if (findViewById(selectedId).getId() == R.id.rbFemale) {
                        user.put(Const.kUserGender, 0);
                    } else {
                        user.put(Const.kUserGender, 1);
                    }

                    final ProgressDialog progress = ProgressDialog.show(this, null, null, true, false);
                    progress.setContentView(R.layout.progress_layout);

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
                }
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

                        Calendar newDate = Calendar.getInstance();
                        newDate.set(year, monthOfYear, dayOfMonth);

                        mEtSignUpBirthday.setText(
                                new SimpleDateFormat("dd-MM-yyyy",
                                        DynamicLanguage.getSelectedLocale(getApplicationContext()))
                                        .format(newDate.getTime())
                        );
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.getDatePicker().setMaxDate(new Date(System.currentTimeMillis()).getTime());
        datePickerDialog.show();
    }

    private boolean validateForm() {
        String title = null;
        EditText select = null;

        if (mEtSignUpEmail.getText().toString().isEmpty()) {
            select = mEtSignUpEmail;
            title = getString(R.string.common_field_required);
        } else if (!Utils.checkEmail(mEtSignUpEmail.getText().toString())) {
            select = mEtSignUpEmail;
            title = getString(R.string.common_field_invalid);
        } else if (mEtSignUpPassword.getText().toString().isEmpty()) {
            select = mEtSignUpPassword;
            title = getString(R.string.common_field_required);
        } else if (mEtSignUpBirthday.getText().toString().isEmpty()) {
            select = mEtSignUpBirthday;
            title = getString(R.string.common_field_required);
        } else {
            int result = Utils.checkDate(mYear, mMonth, mDay);
            if (result == 1) {
                title = getString(R.string.common_field_invalid);
            } else if (result == 2) {
                title = getString(R.string.signup_birthday_old_error);
            }
        }

        if (title != null) {
            final EditText active = select;
            new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            if (active != null)
                                active.requestFocus();
                        }
                    })
                    .show();
            return false;
        }

        return true;
    }

    public void AuthListener(boolean result, ParseException error) {
        if (result) {
            AppActions.initSession(this);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.signup_register_error));

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

}