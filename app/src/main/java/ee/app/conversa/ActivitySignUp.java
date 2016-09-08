package ee.app.conversa;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.SignUpCallback;

import ee.app.conversa.dialog.CustomDeleteUserDialog;
import ee.app.conversa.model.parse.Account;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Utils;

/**
 * Created by edgargomez on 8/12/16.
 */
public class ActivitySignUp extends BaseActivity implements View.OnClickListener {

    private Button mBtnSignUpUp;
    private EditText mEtSignUpName;
    private EditText mEtSignUpEmail;
    private EditText mEtSignUpPassword;
    private TextInputLayout mTilSignUpUsername;
    private TextInputLayout mTilSignUpEmail;
    private TextInputLayout mTilSignUpPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initialization();
    }

    @Override
    protected void initialization() {
        super.initialization();
        mEtSignUpName = (EditText) findViewById(R.id.etSignUpName);
        mEtSignUpEmail = (EditText) findViewById(R.id.etSignUpEmail);
        mEtSignUpPassword = (EditText) findViewById(R.id.etSignUpPassword);
        mTilSignUpUsername = (TextInputLayout) findViewById(R.id.tilNameSignUp);
        mTilSignUpEmail = (TextInputLayout) findViewById(R.id.tilEmailSignUp);
        mTilSignUpPassword = (TextInputLayout) findViewById(R.id.tilPasswordSignUp);
        mBtnSignUpUp = (Button) findViewById(R.id.btnSignUpUp);

        mEtSignUpName.addTextChangedListener(new MyTextWatcher(mEtSignUpName));
        mEtSignUpEmail.addTextChangedListener(new MyTextWatcher(mEtSignUpEmail));
        mEtSignUpPassword.addTextChangedListener(new MyTextWatcher(mEtSignUpPassword));

        if (mTilSignUpUsername != null) {
            mTilSignUpUsername.setOnClickListener(this);
        }

        if (mTilSignUpEmail != null) {
            mTilSignUpEmail.setOnClickListener(this);
        }

        if (mTilSignUpPassword != null) {
            mTilSignUpPassword.setOnClickListener(this);
        }

        if(mBtnSignUpUp != null) {
            mBtnSignUpUp.setOnClickListener(this);
            mBtnSignUpUp.setTypeface(ConversaApp.getInstance(this).getTfRalewayMedium());
        }
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
            case R.id.tilNameSignUp:
                mEtSignUpName.requestFocus();
                break;
            case R.id.tilEmailSignUp:
                mEtSignUpEmail.requestFocus();
                break;
            case R.id.tilPasswordSignUp:
                mEtSignUpPassword.requestFocus();
                break;
            case R.id.btnSignUpUp:
                if (validateForm()) {
                    Account user = new Account();
                    user.setEmail(mEtSignUpEmail.getText().toString());
                    user.setUsername(mEtSignUpName.getText().toString());
                    user.setPassword(mEtSignUpPassword.getText().toString());
                    user.put(Const.kUserTypeKey, 1);
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
                } else {
                    final CustomDeleteUserDialog dialog = new CustomDeleteUserDialog(this);
                    dialog.setTitle(null)
                            .setMessage("Please enter check username, email and password are ok")
                            .setupPositiveButton("Accept", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //contact.removeContact();
                                    dialog.dismiss();
                                }
                            });
                    dialog.show();
                }
                break;
        }
    }

    private boolean validateForm() {
        if (mEtSignUpEmail.getText().toString().isEmpty() || mEtSignUpName.getText().toString().isEmpty()
                || mEtSignUpPassword.getText().toString().isEmpty()) {
            return false;
        }

        if (mTilSignUpUsername.isErrorEnabled() || mTilSignUpEmail.isErrorEnabled() || mTilSignUpPassword.isErrorEnabled()) {
            return false;
        }

        return true;
    }

    private void isNameValid(String name) {
        if (Utils.checkName(name)) {
            mTilSignUpUsername.setErrorEnabled(false);
            mTilSignUpUsername.setError("");
        } else {
            mTilSignUpUsername.setErrorEnabled(true);
            mTilSignUpUsername.setError(getString(R.string.signup_name_error));
        }
    }

    private void isPasswordValid(String password) {
        TextInputLayout layout = mTilSignUpPassword;

        if (password.isEmpty()) {
            layout.setErrorEnabled(true);
            layout.setError(getString(R.string.signup_password_length_error));
        } else {
            if (Utils.checkPassword(password)) {
                //layout.setErrorEnabled(false);
                //layout.setError("");
            } else {
                //layout.setErrorEnabled(true);
                //layout.setError(getString(R.string.signup_password_regex_error));
            }
        }
    }

    private void isEmailValid(String email) {
        TextInputLayout layout = mTilSignUpEmail;

        if (Utils.checkEmail(email)) {
            layout.setErrorEnabled(false);
            layout.setError("");
        } else {
            if (email.isEmpty()) {
                layout.setErrorEnabled(true);
                layout.setError(getString(R.string.sign_email_length_error));
            } else {
                layout.setErrorEnabled(true);
                layout.setError(getString(R.string.sign_email_not_valid_error));
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
            Toast.makeText(this, getString(R.string.no_user_registered), Toast.LENGTH_SHORT).show();
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
                case R.id.etSignUpName:
                    isNameValid(editable.toString());
                    break;
                case R.id.etSignUpEmail:
                    isEmailValid(editable.toString());
                    break;
                case R.id.etSignUpPassword:
                    //isPasswordValid(editable.toString());
                    break;
            }
        }
    }
}