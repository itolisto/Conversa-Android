package ee.app.conversa;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
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

/**
 * SignInActivity
 * 
 * Allows user to sign in, sign up or receive an email with password if user
 * is already registered with a valid email.
 * 
 */

public class ActivitySignIn extends BaseActivity {

	private EditText mEtSignInEmail;
	private EditText mEtSignInPassword;
	private EditText mEtSignUpName;
	private EditText mEtSignUpEmail;
	private EditText mEtSignUpPassword;
	private EditText mEtSendPasswordEmail;
    private RelativeLayout mLlSignTitle;
	private RelativeLayout mLlSignBody;
	private RelativeLayout mLlSignIn;
	private RelativeLayout mLlSignUp;
	private LinearLayout mLlForgotPassword;
	private TextView mTvTitle;
    private Screen mActiveScreen;

    private enum Screen { SIGN_BODY, SIGN_IN, SIGN_UP, FORGOT_PASSWORD }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
		initialization();
	}

	protected void initialization() {
		//-------------------------------DECLARATIONS----------------------------------//
        mTvTitle = (TextView) findViewById(R.id.tvSignInTitle);

		mEtSignInEmail = (EditText) findViewById(R.id.etSignInEmail);
		mEtSignInPassword = (EditText) findViewById(R.id.etSignInPassword);
		mEtSignUpName = (EditText) findViewById(R.id.etSignUpName);
		mEtSignUpEmail = (EditText) findViewById(R.id.etSignUpEmail);
		mEtSignUpPassword = (EditText) findViewById(R.id.etSignUpPassword);
		mEtSendPasswordEmail = (EditText) findViewById(R.id.etForgotPasswordEmail);

        mLlSignTitle = (RelativeLayout) findViewById(R.id.rlSignInTitle);
		mLlSignBody = (RelativeLayout) findViewById(R.id.llSignBody);
		mLlSignIn = (RelativeLayout) findViewById(R.id.llSignInBody);
		mLlSignUp = (RelativeLayout) findViewById(R.id.llSignUpBody);
		mLlForgotPassword = (LinearLayout)   findViewById(R.id.llForgotPasswordBody);

        Button mBtnSignIn = (Button) findViewById(R.id.btnSignIn);
        Button mBtnSignUp = (Button) findViewById(R.id.btnSignUp);
        Button mBtnSignInIn = (Button) findViewById(R.id.btnSignInIn);
        Button mBtnSignUpUp = (Button) findViewById(R.id.btnSignUpUp);
        Button mBtnForgotPassword = (Button) findViewById(R.id.btnForgotPassword);
        Button mBtnSendPassword = (Button) findViewById(R.id.btnSendPassword);
        RelativeLayout mRlSignUpName = (RelativeLayout) findViewById(R.id.rlSignUpName);
        RelativeLayout mRlSignUpEmail = (RelativeLayout) findViewById(R.id.rlSignUpEmail);
        RelativeLayout mRlSignUpPassword = (RelativeLayout) findViewById(R.id.rlSignUpPassword);
        RelativeLayout mRlLogInPassword = (RelativeLayout) findViewById(R.id.rlSignInPassword);
        RelativeLayout mRlLogInEmail = (RelativeLayout) findViewById(R.id.rlSignInEmail);
        RelativeLayout mRlForgotPassword = (RelativeLayout) findViewById(R.id.rlForgotPassword);
		//---------------------------------ACTIONS------------------------------------//
        final AppCompatActivity activity = this;

        if(mBtnSignInIn != null) {
            mBtnSignInIn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if(checkInternetConnection()) {
                        final String mSignInEmail = mEtSignInEmail.getText().toString();
                        final String mSignInPassword = mEtSignInPassword.getText().toString();

                        if(isNameValid(mSignInEmail) || !mSignInPassword.isEmpty()) {
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
                            query.findInBackground(new FindCallback<Account>() {
                                @Override
                                public void done(List<Account> objects, ParseException e) {
                                    if (e == null && objects.size() > 0) {
                                        String username = objects.get(0).getUsername();
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
                    }
                }
            });
        }

        if(mBtnSignUpUp != null) {
            mBtnSignUpUp.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (checkInternetConnection()) {
                        final String mSignUpName = mEtSignUpName.getText().toString();
                        final String mSignUpEmail = mEtSignUpEmail.getText().toString();
                        final String mSignUpPassword = mEtSignUpPassword.getText().toString();

                        if (isNameValid(mSignUpName) && isEmailValid(mSignUpEmail) && isPasswordValid(mSignUpPassword)) {
                            Account user = new Account();
                            user.setEmail(mSignUpEmail);
                            user.setUsername(mSignUpName);
                            user.setPassword(mSignUpPassword);
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
                    }
                }
            });
        }

        if(mBtnSendPassword != null) {
            mBtnSendPassword.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String mResetPasswordEmail = mEtSendPasswordEmail.getText().toString();
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);

                    if(isEmailValid(mResetPasswordEmail)) {
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
                    } else {
                        builder.setMessage(getString(R.string.email_not_valid))
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                        builder.show();
                    }
                }
            });
        }

        if(mBtnSignIn != null) {
            mBtnSignIn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mLlSignIn.getVisibility() == View.GONE) {
                        mEtSignUpName.setText("");
                        mEtSignUpEmail.setText("");
                        mEtSignUpPassword.setText("");
                        setActiveScreen(Screen.SIGN_IN);
                    }
                }
            });
        }

        if(mBtnSignUp != null) {
            mBtnSignUp.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mLlSignUp.getVisibility() == View.GONE) {
                        setActiveScreen(Screen.SIGN_UP);
                    }
                }
            });
        }

        if(mBtnForgotPassword != null) {
            mBtnForgotPassword.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mEtSendPasswordEmail.setText("");
                    setActiveScreen(Screen.FORGOT_PASSWORD);
                }
            });
        }

        if(mRlSignUpName != null) {
            mRlSignUpName.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mEtSignUpName.requestFocus();
                }
            });
        }

        if(mRlSignUpEmail != null) {
            mRlSignUpEmail.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mEtSignUpEmail.requestFocus();
                }
            });
        }

        if(mRlSignUpPassword != null) {
            mRlSignUpPassword.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mEtSignUpPassword.requestFocus();
                }
            });
        }

        if(mRlLogInPassword != null) {
            mRlLogInPassword.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mEtSignInPassword.requestFocus();
                }
            });
        }

        if(mRlLogInEmail != null) {
            mRlLogInEmail.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mEtSignInEmail.requestFocus();
                }
            });
        }

        if(mRlForgotPassword != null) {
            mRlForgotPassword.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mEtSendPasswordEmail.requestFocus();
                }
            });
        }
		//------------------------INITIAL VISIBILITY FOR LAYOUTS-------------------------//
        setActiveScreen(Screen.SIGN_BODY);
		//--------------------------------SET TYPEFACE-----------------------------------//
		mEtSignInEmail.setTypeface(    ConversaApp.getTfRalewayRegular());
		mEtSignInPassword.setTypeface( ConversaApp.getTfRalewayRegular());
		mEtSignUpName.setTypeface(     ConversaApp.getTfRalewayRegular());
		mEtSignUpEmail.setTypeface(    ConversaApp.getTfRalewayRegular());
		mEtSignUpPassword.setTypeface( ConversaApp.getTfRalewayRegular());
        mEtSendPasswordEmail.setTypeface( ConversaApp.getTfRalewayRegular());

//        logInButton.setTypeface(        ConversaApp.getTfRalewayMedium());
//        signUpButton.setTypeface(       ConversaApp.getTfRalewayMedium());
        mBtnSignIn.setTypeface(         ConversaApp.getTfRalewayMedium());
        mBtnSignUp.setTypeface(         ConversaApp.getTfRalewayMedium());
        mBtnSignInIn.setTypeface(       ConversaApp.getTfRalewayMedium());
        mBtnSignUpUp.setTypeface(       ConversaApp.getTfRalewayMedium());
        mBtnForgotPassword.setTypeface( ConversaApp.getTfRalewayLight());
        mBtnSendPassword.setTypeface(   ConversaApp.getTfRalewayMedium());
	}

	private void setActiveScreen(Screen activeScreen) {
		mActiveScreen = activeScreen;
		
		switch (activeScreen) {
			case SIGN_BODY:
                mLlSignTitle.setVisibility(View.GONE);
                Utils.hideKeyboard(this);
				mTvTitle.setText(getString(R.string.SIGN_BODY));

				mLlSignBody.setVisibility(View.VISIBLE);
				mLlSignUp.setVisibility(View.GONE);
				mLlSignIn.setVisibility(View.GONE);
				mLlForgotPassword.setVisibility(View.GONE);

                mEtSignInEmail.setText("");
                mEtSignInPassword.setText("");
                mEtSignUpName.setText("");
                mEtSignUpEmail.setText("");
                mEtSignUpPassword.setText("");
                mEtSendPasswordEmail.setText("");

				break;
			case SIGN_IN:
                Utils.hideKeyboard(this);
                mLlSignTitle.setVisibility(View.VISIBLE);
				mTvTitle.setText(getString(R.string.SIGN_IN));
				
				mLlSignBody.setVisibility(View.GONE);
				mLlSignUp.setVisibility(View.GONE);
				mLlSignIn.setVisibility(View.VISIBLE);
				mLlForgotPassword.setVisibility(View.GONE);
				break;
			case SIGN_UP:
                Utils.hideKeyboard(this);
                mLlSignTitle.setVisibility(View.VISIBLE);
				mTvTitle.setText(getString(R.string.SIGN_UP));

				mLlSignBody.setVisibility(View.GONE);
				mLlSignUp.setVisibility(View.VISIBLE);
				mLlSignIn.setVisibility(View.GONE);
				mLlForgotPassword.setVisibility(View.GONE);

				break;
			case FORGOT_PASSWORD:
                Utils.hideKeyboard(this);
                mLlSignTitle.setVisibility(View.VISIBLE);
				mTvTitle.setText(getString(R.string.FORGOT_PASSWORD));
				
				mLlSignBody.setVisibility(View.GONE);
				mLlSignUp.setVisibility(View.GONE);
				mLlSignIn.setVisibility(View.GONE);
				mLlForgotPassword.setVisibility(View.VISIBLE);

				break;
		}
	}

	private boolean isNameValid(String name) {
		String nameResult = Utils.checkName(this, name);
		if (!nameResult.equals(getString(R.string.name_ok))) {
//			final HookUpDialog dialog = new HookUpDialog(ActivitySignIn.this);
//			dialog.showOnlyOK(nameResult);
			return false;
		} else {
			return true;
		}
	}

	private boolean isPasswordValid(String password) {
//		String passwordResult = Utils.checkPassword(this, password);
//		if (!passwordResult.equals(getString(R.string.password_ok))) {
//			final HookUpDialog dialog = new HookUpDialog(ActivitySignIn.this);
//			dialog.showOnlyOK(passwordResult);
//			return false;
//		} else {
//			return true;
//		}
        return true;
	}

	private boolean isEmailValid(String email) {
		String emailResult = Utils.checkEmail(this, email);
		if (!emailResult.equals(getString(R.string.email_ok))) {
//			final HookUpDialog dialog = new HookUpDialog(ActivitySignIn.this);
//			dialog.showOnlyOK(emailResult);
			return false;
		} else {
			return true;
		}
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

    /**
     *********************************************************
     *********************** LISTENERS ***********************
     *********************************************************
     */
    public void AuthListener(boolean result, ParseException error) {
        if(result) {
            // Do intent
            Intent intent = new Intent(ActivitySignIn.this, ActivityMain.class);
            ActivitySignIn.this.startActivity(intent);
            ActivitySignIn.this.finish();
        } else {
            Toast.makeText(ActivitySignIn.this, getString(R.string.no_user_registered), Toast.LENGTH_SHORT).show();
        }
    }

}