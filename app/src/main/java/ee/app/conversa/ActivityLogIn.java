package ee.app.conversa;

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

import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ee.app.conversa.dialog.CustomDeleteUserDialog;
import ee.app.conversa.model.Parse.Account;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Utils;

/**
 * Created by edgargomez on 8/12/16.
 */
public class ActivityLogIn extends BaseActivity implements View.OnClickListener {

    private Button mBtnSignInIn;
    private EditText mEtSignInEmail;
    private EditText mEtSignInPassword;
    private TextInputLayout mTilSignInEmail;
    private TextInputLayout mTilSignInPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initialization();
    }

    @Override
    protected void initialization() {
        super.initialization();
        mEtSignInEmail = (EditText) findViewById(R.id.etSignInEmail);
        mEtSignInPassword = (EditText) findViewById(R.id.etSignInPassword);
        mTilSignInEmail = (TextInputLayout) findViewById(R.id.tilEmail);
        mTilSignInPassword = (TextInputLayout) findViewById(R.id.tilPassword);
        mBtnSignInIn = (Button) findViewById(R.id.btnSignInIn);
        Button mBtnForgotPassword = (Button) findViewById(R.id.btnForgotPassword);

        mEtSignInEmail.addTextChangedListener(new MyTextWatcher(mEtSignInEmail));
        mEtSignInPassword.addTextChangedListener(new MyTextWatcher(mEtSignInPassword));

        if (mTilSignInEmail != null) {
            mTilSignInEmail.setOnClickListener(this);
        }

        if (mTilSignInPassword != null) {
            mTilSignInPassword.setOnClickListener(this);
        }

        if(mBtnSignInIn != null) {
            mBtnSignInIn.setOnClickListener(this);
            mBtnSignInIn.setTypeface(ConversaApp.getTfRalewayMedium());
        }

        if(mBtnForgotPassword != null) {
            mBtnForgotPassword.setOnClickListener(this);
            mBtnForgotPassword.setTypeface(ConversaApp.getTfRalewayLight());
        }
    }

    @Override
    public void yesInternetConnection() {
        super.yesInternetConnection();
        mBtnSignInIn.setEnabled(true);
    }

    @Override
    public void noInternetConnection() {
        super.noInternetConnection();
        mBtnSignInIn.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tilEmail:
                mEtSignInEmail.requestFocus();
                break;
            case R.id.tilPassword:
                mEtSignInPassword.requestFocus();
                break;
            case R.id.btnForgotPassword:
                Intent intent = new Intent(this, ActivityForgotPassword.class);
                startActivity(intent);
                break;
            case R.id.btnSignInIn:
                if(validateForm()) {
                    final String mSignInEmail = mEtSignInEmail.getText().toString();
                    final String mSignInPassword = mEtSignInPassword.getText().toString();

                    ParseQuery<Account> subQuery2 = ParseQuery.getQuery(Account.class);
                    subQuery2.whereEqualTo(Const.kUserEmailKey, mSignInEmail);
                    subQuery2.whereEqualTo(Const.kUserTypeKey, 1);

                    ParseQuery<Account> subQuery1 = ParseQuery.getQuery(Account.class);
                    subQuery1.whereEqualTo(Const.kUserUsernameKey, mSignInEmail);
                    subQuery1.whereEqualTo(Const.kUserTypeKey, 1);

                    List<ParseQuery<Account>> subList = new ArrayList<>();
                    subList.add(subQuery1);
                    subList.add(subQuery2);

                    ParseQuery<Account> query = ParseQuery.or(subList);
                    Collection<String> collection = new ArrayList<>();
                    collection.add(Const.kUserUsernameKey);
                    query.selectKeys(collection);
                    query.getFirstInBackground(new GetCallback<Account>() {
                        @Override
                        public void done(Account object, ParseException e) {
                            if (e == null) {
                                String username = object.getUsername();
                                ParseUser.logInInBackground(username, mSignInPassword, new LogInCallback() {
                                    public void done(ParseUser user, ParseException e) {
                                        if (user != null) {
                                            AuthListener(true, null);
                                        } else {
                                            AuthListener(false, e);
                                        }
                                    }
                                });
                            } else {
                                AuthListener(false, e);
                            }
                        }
                    });
                } else {
                    final CustomDeleteUserDialog dialog = new CustomDeleteUserDialog(this);
                    dialog.setTitle(null)
                            .setMessage("Please enter check email and password are ok")
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
        if (mEtSignInEmail.getText().toString().isEmpty() || mEtSignInPassword.getText().toString().isEmpty()) {
            return false;
        }

        if (mTilSignInEmail.isErrorEnabled() || mTilSignInPassword.isErrorEnabled()) {
            return false;
        }

        return true;
    }

    private void isPasswordValid(String password) {
        TextInputLayout layout = mTilSignInPassword;

        if (password.isEmpty()) {
            layout.setErrorEnabled(true);
            layout.setError(getString(R.string.signup_password_length_error));
        } else {
            if (Utils.checkPassword(password)) {
                //layout.setErrorEnabled(false);
                //layout.setError("");
            } else {
                layout.setErrorEnabled(false);
                layout.setError("");
            }
        }
    }

    private void isEmailValid(String email) {
        TextInputLayout layout = mTilSignInEmail;

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
//            if (editable.toString().isEmpty())
//                return;

            switch (view.getId()) {
                case R.id.etSignInEmail:
                    isEmailValid(editable.toString());
                    break;
                case R.id.etSignInPassword:
                    //isPasswordValid(editable.toString());
                    break;
            }
        }
    }
}