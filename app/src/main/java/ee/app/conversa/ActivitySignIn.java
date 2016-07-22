package ee.app.conversa;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SignUpCallback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ee.app.conversa.model.Parse.Account;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Utils;
import ee.app.conversa.view.LightTextView;

/**
 * ActivitySignIn
 * 
 * Allows user to sign in, sign up or receive an email with password if user
 * is already registered with a valid email.
 * 
 */
public class ActivitySignIn extends BaseActivity implements View.OnClickListener {

    private Button mBtnSignInIn;
    private Button mBtnSignUpUp;
    private Button mBtnSendPassword;
	private EditText mEtSignInEmail;
	private EditText mEtSignInPassword;
	private EditText mEtSignUpName;
	private EditText mEtSignUpEmail;
	private EditText mEtSignUpPassword;
	private EditText mEtSendPasswordEmail;
    private LinearLayout mLlForgotPassword;
	private RelativeLayout mLlSignBody;
	private RelativeLayout mLlSignIn;
	private RelativeLayout mLlSignUp;
    private TextInputLayout mTilSignInEmail;
    private TextInputLayout mTilSignInPassword;
    private TextInputLayout mTilSignUpUsername;
    private TextInputLayout mTilSignUpEmail;
    private TextInputLayout mTilSignUpPassword;
    private TextInputLayout mTilForgotPassword;
    private Screen mActiveScreen;
    private enum Screen { SIGN_BODY, SIGN_IN, SIGN_UP, FORGOT_PASSWORD }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
		initialization();
	}

	protected void initialization() {
        super.initialization();
		mEtSignInEmail = (EditText) findViewById(R.id.etSignInEmail);
		mEtSignInPassword = (EditText) findViewById(R.id.etSignInPassword);
		mEtSignUpName = (EditText) findViewById(R.id.etSignUpName);
		mEtSignUpEmail = (EditText) findViewById(R.id.etSignUpEmail);
		mEtSignUpPassword = (EditText) findViewById(R.id.etSignUpPassword);
		mEtSendPasswordEmail = (EditText) findViewById(R.id.etSendEmail);

		mLlSignBody = (RelativeLayout) findViewById(R.id.llSignBody);
		mLlSignIn = (RelativeLayout) findViewById(R.id.llSignInBody);
		mLlSignUp = (RelativeLayout) findViewById(R.id.llSignUpBody);
		mLlForgotPassword = (LinearLayout)   findViewById(R.id.llForgotPasswordBody);

        mTilSignInEmail = (TextInputLayout) findViewById(R.id.tilEmail);
        mTilSignInPassword = (TextInputLayout) findViewById(R.id.tilPassword);
        mTilSignUpUsername = (TextInputLayout) findViewById(R.id.tilNameSignUp);
        mTilSignUpEmail = (TextInputLayout) findViewById(R.id.tilEmailSignUp);
        mTilSignUpPassword = (TextInputLayout) findViewById(R.id.tilPasswordSignUp);
        mTilForgotPassword = (TextInputLayout) findViewById(R.id.tilPasswordForgot);

        Button mBtnSignIn = (Button) findViewById(R.id.btnSignIn);
        Button mBtnSignUp = (Button) findViewById(R.id.btnSignUp);
        Button mBtnForgotPassword = (Button) findViewById(R.id.btnForgotPassword);
        mBtnSignInIn = (Button) findViewById(R.id.btnSignInIn);
        mBtnSignUpUp = (Button) findViewById(R.id.btnSignUpUp);
        mBtnSendPassword = (Button) findViewById(R.id.btnSendPassword);

        LightTextView mLtvClickHere = (LightTextView) findViewById(R.id.ltvClickHere);
        if (mLtvClickHere != null) {
            SpannableString styledString = new SpannableString(getString(R.string.string_signin_sign_up_business_two));
            // change text color
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                styledString.setSpan(new ForegroundColorSpan(Color.RED), 8, 19, 0);
            } else {
                styledString.setSpan(new ForegroundColorSpan(Color.RED), 8, 19, 0);
            }
            // url
            styledString.setSpan(new URLSpan("http://www.google.com"), 8, 19, 0);
            mLtvClickHere.setMovementMethod(LinkMovementMethod.getInstance());
            mLtvClickHere.setText(styledString);
        }

        mEtSignInEmail.addTextChangedListener(new MyTextWatcher(mEtSignInEmail));
        mEtSignInPassword.addTextChangedListener(new MyTextWatcher(mEtSignInPassword));
        mEtSignUpName.addTextChangedListener(new MyTextWatcher(mEtSignUpName));
        mEtSignUpEmail.addTextChangedListener(new MyTextWatcher(mEtSignUpEmail));
        mEtSignUpPassword.addTextChangedListener(new MyTextWatcher(mEtSignUpPassword));
        mEtSendPasswordEmail.addTextChangedListener(new MyTextWatcher(mEtSendPasswordEmail));

        if (mTilSignInEmail != null) {
            mTilSignInEmail.setOnClickListener(this);
        }

        if (mTilSignInPassword != null) {
            mTilSignInPassword.setOnClickListener(this);
        }

        if (mTilSignUpUsername != null) {
            mTilSignUpUsername.setOnClickListener(this);
        }

        if (mTilSignUpEmail != null) {
            mTilSignUpEmail.setOnClickListener(this);
        }

        if (mTilSignUpPassword != null) {
            mTilSignUpPassword.setOnClickListener(this);
        }

        if (mTilForgotPassword != null) {
            mTilForgotPassword.setOnClickListener(this);
        }

        if(mBtnSignInIn != null) {
            mBtnSignInIn.setOnClickListener(this);
            mBtnSignInIn.setTypeface(ConversaApp.getTfRalewayMedium());
        }

        if(mBtnSignUpUp != null) {
            mBtnSignUpUp.setOnClickListener(this);
            mBtnSignUpUp.setTypeface(ConversaApp.getTfRalewayMedium());
        }

        if(mBtnSignIn != null) {
            mBtnSignIn.setOnClickListener(this);
            mBtnSignIn.setTypeface(ConversaApp.getTfRalewayMedium());
        }

        if(mBtnSignUp != null) {
            mBtnSignUp.setOnClickListener(this);
            mBtnSignUp.setTypeface(ConversaApp.getTfRalewayMedium());
        }

        if(mBtnForgotPassword != null) {
            mBtnForgotPassword.setOnClickListener(this);
            mBtnForgotPassword.setTypeface(ConversaApp.getTfRalewayLight());
        }

        if(mBtnSendPassword != null) {
            mBtnSendPassword.setOnClickListener(this);
            mBtnSendPassword.setTypeface(ConversaApp.getTfRalewayMedium());
        }

		// Initial visibility
        setActiveScreen(Screen.SIGN_BODY);
	}

	private void setActiveScreen(Screen activeScreen) {
		mActiveScreen = activeScreen;

        switch (activeScreen) {
			case SIGN_BODY:
                mLlSignBody.requestFocus();

				mLlSignBody.setVisibility(View.VISIBLE);
				mLlSignUp.setVisibility(View.GONE);
				mLlSignIn.setVisibility(View.GONE);
				mLlForgotPassword.setVisibility(View.GONE);
                mEtSignInEmail.setText("");
                mEtSignInPassword.setText("");
				break;
			case SIGN_IN:
                mLlSignIn.requestFocus();

				mLlSignBody.setVisibility(View.GONE);
				mLlSignUp.setVisibility(View.GONE);
				mLlSignIn.setVisibility(View.VISIBLE);
				mLlForgotPassword.setVisibility(View.GONE);
                mEtSignInEmail.setText("");
                mEtSignInPassword.setText("");
				break;
			case SIGN_UP:
                mLlSignUp.requestFocus();

				mLlSignBody.setVisibility(View.GONE);
				mLlSignUp.setVisibility(View.VISIBLE);
				mLlSignIn.setVisibility(View.GONE);
				mLlForgotPassword.setVisibility(View.GONE);
                mEtSignUpName.setText("");
                mEtSignUpEmail.setText("");
                mEtSignUpPassword.setText("");
				break;
			case FORGOT_PASSWORD:
                mLlForgotPassword.requestFocus();

				mLlSignBody.setVisibility(View.GONE);
				mLlSignUp.setVisibility(View.GONE);
				mLlSignIn.setVisibility(View.GONE);
				mLlForgotPassword.setVisibility(View.VISIBLE);
                mEtSendPasswordEmail.setText("");
				break;
		}

        Utils.hideKeyboard(this);
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
        TextInputLayout layout;
        if (mActiveScreen == Screen.SIGN_IN) {
            layout = mTilSignInPassword;
        } else {
            layout = mTilSignUpPassword;
        }

        if (Utils.checkPassword(password)) {
            layout.setErrorEnabled(false);
            layout.setError("");
        } else {
            if (password.isEmpty()) {
                layout.setErrorEnabled(true);
                layout.setError(getString(R.string.signup_password_length_error));
            } else {
                if (mActiveScreen == Screen.SIGN_IN) {
                    // Not checking for error on password regex on sign in
                    layout.setErrorEnabled(false);
                    layout.setError("");
                } else {
                    layout.setErrorEnabled(true);
                    layout.setError(getString(R.string.signup_password_regex_error));
                }
            }
        }
	}

	private void isEmailValid(String email) {
        TextInputLayout layout;
        if (mActiveScreen == Screen.SIGN_IN) {
            layout = mTilSignInEmail;
        } else if (mActiveScreen == Screen.SIGN_UP) {
            layout = mTilSignUpEmail;
        } else {
            layout = mTilForgotPassword;
        }

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

    @Override
    public void yesInternetConnection() {
        super.yesInternetConnection();
        mBtnSignInIn.setEnabled(true);
        mBtnSignUpUp.setEnabled(true);
        mBtnSendPassword.setEnabled(true);
    }

    @Override
    public void noInternetConnection() {
        super.noInternetConnection();
        mBtnSignInIn.setEnabled(false);
        mBtnSignUpUp.setEnabled(false);
        mBtnSendPassword.setEnabled(false);
    }

    @Override
	public void onBackPressed() {
		if (mActiveScreen == Screen.FORGOT_PASSWORD) {
			setActiveScreen(Screen.SIGN_IN);
		} else {
			if (mActiveScreen == Screen.SIGN_IN || mActiveScreen == Screen.SIGN_UP) {
				setActiveScreen(Screen.SIGN_BODY);
			} else {
				super.onBackPressed();
			}
		}
	}

    /********************************************************************************************************/
    /********************************************************************************************************/
    public void AuthListener(boolean result, ParseException error) {
        if(result) {
            Intent intent = new Intent(ActivitySignIn.this, ActivityMain.class);
            ActivitySignIn.this.startActivity(intent);
            ActivitySignIn.this.finish();
        } else {
            Toast.makeText(ActivitySignIn.this, getString(R.string.no_user_registered), Toast.LENGTH_SHORT).show();
        }
    }

    /********************************************************************************************************/
    /********************************************************************************************************/
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tilEmail:
                mEtSignInEmail.requestFocus();
                break;
            case R.id.tilPassword:
                mEtSignInPassword.requestFocus();
                break;
            case R.id.tilNameSignUp:
                mEtSignUpName.requestFocus();
                break;
            case R.id.tilEmailSignUp:
                mEtSignUpEmail.requestFocus();
                break;
            case R.id.tilPasswordSignUp:
                mEtSignUpPassword.requestFocus();
                break;
            case R.id.tilPasswordForgot:
                mEtSendPasswordEmail.requestFocus();
                break;
            case R.id.btnSignInIn:
                if(!mTilSignInEmail.isErrorEnabled() && !mTilSignInPassword.isErrorEnabled()) {
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
                }
                break;
            case R.id.btnSignUpUp:
                if (!mTilSignUpUsername.isErrorEnabled() && !mTilSignUpEmail.isErrorEnabled() && !mTilSignUpPassword.isErrorEnabled()) {
                    Account user = new Account();
                    user.setEmail(mEtSignUpEmail.getText().toString());
                    user.setUsername(mEtSignUpName.getText().toString());
                    user.setPassword(mEtSignUpPassword.getText().toString());
                    user.put(Const.kUserTypeKey, 1);

                    user.signUpInBackground(new SignUpCallback() {
                        public void done(ParseException e) {
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
            case R.id.btnForgotPassword:
                setActiveScreen(Screen.FORGOT_PASSWORD);
                break;
            case R.id.btnSignIn:
                setActiveScreen(Screen.SIGN_IN);
                break;
            case R.id.btnSignUp:
                setActiveScreen(Screen.SIGN_UP);
                break;
            case R.id.btnSendPassword:
                if(!mTilForgotPassword.isErrorEnabled()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(getString(R.string.confirm_email, mEtSendPasswordEmail.getText().toString()))
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    String sentToEmail = mEtSendPasswordEmail.getText().toString();
                                    ParseUser.requestPasswordResetInBackground(sentToEmail, new RequestPasswordResetCallback() {
                                        public void done(ParseException e) {
                                            if(e == null) {
                                                Toast.makeText(ActivitySignIn.this, getString(R.string.email_sent), Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(ActivitySignIn.this, getString(R.string.email_fail_sent), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    builder.show();
                }
                break;
        }
    }

    /********************************************************************************************************/
    /********************************************************************************************************/
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
                    isPasswordValid(editable.toString());
                    break;
                case R.id.etSignUpName:
                    isNameValid(editable.toString());
                    break;
                case R.id.etSignUpEmail:
                    isEmailValid(editable.toString());
                    break;
                case R.id.etSignUpPassword:
                    //isPasswordValid(editable.toString());
                    break;
                case R.id.etSendEmail:
                    isEmailValid(editable.toString());
                    break;
            }
        }
    }

}