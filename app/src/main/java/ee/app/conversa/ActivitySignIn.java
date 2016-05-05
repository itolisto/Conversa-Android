package ee.app.conversa;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import ee.app.conversa.dialog.HookUpDialog;
import ee.app.conversa.management.ConnectionChangeReceiver;
import ee.app.conversa.model.Parse.Account;
import ee.app.conversa.model.Parse.Customer;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Utils;

/**
 * SignInActivity
 * 
 * Allows user to sign in, sign up or receive an email with password if user
 * is already registered with a valid email.
 * 
 */

public class ActivitySignIn extends AppCompatActivity {
	
	public static ActivitySignIn sInstance;

    private Spinner mSpGender;
	private EditText mEtSignInUsername;
	private EditText mEtSignInPassword;
	private EditText mEtSignUpName;
	private EditText mEtSignUpEmail;
	private EditText mEtSignUpPassword;
	private EditText mEtSendPasswordEmail;
    private Button mBtnBirthday;
    private RelativeLayout mLlSignTitle;
	private RelativeLayout mLlSignBody;
	private RelativeLayout mLlSignIn;
	private RelativeLayout mLlSignUp;
    private RelativeLayout mRlNoInternetNotification;
	private LinearLayout mLlForgotPassword;
	private TextView mTvTitle;
    private String mSignUpBirthday;
    private String mSignUpGender;
	private Screen mActiveScreen;

    private enum Screen { SIGN_BODY, SIGN_IN, SIGN_UP, FORGOT_PASSWORD }
	private HookUpDialog mSendPasswordDialog;

    private final IntentFilter mConnectionChangeFilter = new IntentFilter(
            ConnectionChangeReceiver.INTERNET_CONNECTION_CHANGE);

    List<String> permissionNeeds = Arrays.asList("public_profile", "user_birthday", "email");


    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        sInstance = this;
        setContentView(R.layout.activity_sign_in);
		initialization();
	}

    @Override
    protected void onResume() {
        super.onResume();

        ConversaApp.getLocalBroadcastManager().registerReceiver(
                mConnectionChangeReceiver, mConnectionChangeFilter);
        if(checkInternetConnection()) {
            yesInternetConnection();
        } else {
            noInternetConnection();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        ConversaApp.getLocalBroadcastManager().unregisterReceiver(mConnectionChangeReceiver);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private BroadcastReceiver mConnectionChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra(ConnectionChangeReceiver.HAS_INTERNET_CONNECTION, true)) {
                yesInternetConnection();
            } else {
                noInternetConnection();
            }
        }
    };

    private boolean checkInternetConnection() {
        return (ConversaApp.hasNetworkConnection());
    }

    protected void noInternetConnection() {
        if (mRlNoInternetNotification != null && mRlNoInternetNotification.getVisibility() == View.VISIBLE) {
            Animation slidein = AnimationUtils.loadAnimation(getApplicationContext(),
                    R.anim.slide_in_top);
            mRlNoInternetNotification.setVisibility(View.VISIBLE);
            mRlNoInternetNotification.startAnimation(slidein);
        }
    }

    protected void yesInternetConnection() {
        if (mRlNoInternetNotification != null && mRlNoInternetNotification.getVisibility() == View.GONE) {
            Animation slideout = AnimationUtils.loadAnimation(getApplicationContext(),
                    R.anim.slide_out_top);
            mRlNoInternetNotification.setVisibility(View.GONE);
            mRlNoInternetNotification.startAnimation(slideout);
        }
    }

	private void initialization() {
		//-------------------------------DECLARATIONS----------------------------------//
        mRlNoInternetNotification = (RelativeLayout) findViewById(R.id.rlNoInternetNotification);

        mTvTitle  = (TextView) findViewById(R.id.tvSignInTitle);

		mEtSignInUsername = (EditText) findViewById(R.id.etSignInEmail);
		mEtSignInPassword    = (EditText) findViewById(R.id.etSignInPassword);
		mEtSignUpName        = (EditText) findViewById(R.id.etSignUpName);
		mEtSignUpEmail       = (EditText) findViewById(R.id.etSignUpEmail);
		mEtSignUpPassword    = (EditText) findViewById(R.id.etSignUpPassword);
		mEtSendPasswordEmail = (EditText) findViewById(R.id.etForgotPasswordEmail);

        mBtnBirthday        = (Button) findViewById(R.id.btnSignUpBirthday);
        mSpGender           = (Spinner)findViewById(R.id.spSignUpGender);

        mLlSignTitle        = (RelativeLayout) findViewById(R.id.rlSignInTitle);
		mLlSignBody         = (RelativeLayout) findViewById(R.id.llSignBody);
		mLlSignIn           = (RelativeLayout) findViewById(R.id.llSignInBody);
		mLlSignUp           = (RelativeLayout) findViewById(R.id.llSignUpBody);
		mLlForgotPassword   = (LinearLayout)   findViewById(R.id.llForgotPasswordBody);

//        LoginButton logInButton    = (LoginButton) findViewById(R.id.btnSignFb);
//        LoginButton signUpButton   = (LoginButton) findViewById(R.id.btnRegFb);
        Button mBtnSignIn          = (Button) findViewById(R.id.btnSignIn);
        Button mBtnSignUp          = (Button) findViewById(R.id.btnSignUp);
        Button mBtnSignInIn        = (Button) findViewById(R.id.btnSignInIn);
        Button mBtnSignUpUp        = (Button) findViewById(R.id.btnSignUpUp);
        Button mBtnForgotPassword  = (Button) findViewById(R.id.btnForgotPassword);
        Button mBtnSendPassword    = (Button) findViewById(R.id.btnSendPassword);
        RelativeLayout mRlSignUpName      = (RelativeLayout) findViewById(R.id.rlSignUpName);
        RelativeLayout mRlSignUpEmail     = (RelativeLayout) findViewById(R.id.rlSignUpEmail);
        RelativeLayout mRlSignUpPassword  = (RelativeLayout) findViewById(R.id.rlSignUpPassword);
        RelativeLayout mRlLogInPassword   = (RelativeLayout) findViewById(R.id.rlSignInPassword);
        RelativeLayout mRlLogInEmail      = (RelativeLayout) findViewById(R.id.rlSignInEmail);
        RelativeLayout mRlForgotPassword  = (RelativeLayout) findViewById(R.id.rlForgotPassword);
		//-------------------------------RESOURCES----------------------------------//
		// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, R.layout.gender_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mSpGender.setAdapter(adapter);
		//---------------------------------ACTIONS------------------------------------//
//        logInButton.setReadPermissions(permissionNeeds);
//        signUpButton.setReadPermissions(permissionNeeds);
//        logInButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
//            @Override
//            public void onSuccess(LoginResult loginResult) {
//                // App code
//                AccessToken accessToken = loginResult.getAccessToken();
//
////                GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
////                    @Override
////                    public void onCompleted(JSONObject user, GraphResponse response) {
////                        if (user != null) {
////                            //profilePictureView.setProfileId(user.optString("id"));
////                        }
////                    }
////                }).executeAsync();
//            }
//
//            @Override
//            public void onCancel() {}
//
//            @Override
//            public void onError(FacebookException exception) {
//                Toast.makeText(getApplicationContext(),getString(R.string.fb_action_error), Toast.LENGTH_SHORT).show();
//            }
//        });
//        signUpButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
//            @Override
//            public void onSuccess(LoginResult loginResult) {
//                // App code
////                AccessToken accessToken = loginResult.getAccessToken();
////
////                GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
////                    @Override
////                    public void onCompleted(JSONObject user, GraphResponse response) {
////                        if (user != null) {
////                            //profilePictureView.setProfileId(user.optString("id"));
////                        }
////                    }
////                }).executeAsync();
//            }
//
//            @Override
//            public void onCancel() {}
//
//            @Override
//            public void onError(FacebookException exception) {
//                Toast.makeText(getApplicationContext(),getString(R.string.fb_action_error), Toast.LENGTH_SHORT).show();
//            }
//        });

        if(mBtnSignInIn != null) {
            mBtnSignInIn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    final String mSignInUsername = mEtSignInUsername.getText().toString();
                    final String mSignInPassword = mEtSignInPassword.getText().toString();
                    if (isNameValid(mSignInUsername)) {
                        if (!mSignInPassword.isEmpty()) {
                            if (checkInternetConnection()) {
                                ParseQuery<Account> subQuery2 = ParseQuery.getQuery(Account.class);
                                subQuery2.whereEqualTo(Const.kUserEmailKey, mSignInUsername);
                                subQuery2.whereEqualTo(Const.kUserTypeKey, true);

                                ParseQuery<Account> subQuery1 = ParseQuery.getQuery(Account.class);
                                subQuery1.whereEqualTo(Const.kUserUsernameKey, mSignInUsername);
                                subQuery1.whereEqualTo(Const.kUserTypeKey, true);

                                List<ParseQuery<Account>> subList = new ArrayList<>();
                                subList.add(subQuery1);
                                subList.add(subQuery2);

                                ParseQuery<Account> query = ParseQuery.or(subList);
                                Collection<String>collection = new ArrayList<>();
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

                                        }
                                    }
                                });
                            }
                        } else {
                            final HookUpDialog dialog = new HookUpDialog(ActivitySignIn.this);
                            dialog.showOnlyOK(getString(R.string.password_error_empty));
                        }
                    }
                }
            });
        }

        if(mBtnSignUpUp != null) {
            mBtnSignUpUp.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    String mSignUpName = mEtSignUpName.getText().toString();
                    String mSignUpEmail = mEtSignUpEmail.getText().toString();
                    String mSignUpPassword = mEtSignUpPassword.getText().toString();
                    if (isNameValid(mSignUpName) && isEmailValid(mSignUpEmail) && isPasswordValid(mSignUpPassword)
                            && isBirthdayValid() && isGenderValid()) {
                        if (checkInternetConnection()) {
                            Account user = new Account();
                            user.setEmail(mSignUpEmail);
                            user.setUsername(mSignUpName);
                            user.setPassword(mSignUpPassword);
                            user.put(Const.kUserTypeKey, true);

                            user.signUpInBackground(new SignUpCallback() {
                                public void done(ParseException e) {
                                    if (e == null) {
                                        // Hooray! Let them use the app now.
                                        Customer newCustomer = new Customer();
                                        newCustomer.put(Const.kCustomerUserInfoKey, Account.getCurrentUser());
                                        newCustomer.put("birthdate", mSignUpBirthday);
                                        newCustomer.put("gender", mSignUpGender);
                                        newCustomer.saveEventually();
                                        CreateUserListener(true, null);
                                    } else {
                                        // Sign up didn't succeed. Look at the ParseException
                                        // to figure out what went wrong
                                        CreateUserListener(false, e);
                                    }
                                }
                            });
                        }
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

        if(mBtnSendPassword != null) {
            mBtnSendPassword.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isEmailValid(mEtSendPasswordEmail.getText().toString())) {
                        mSendPasswordDialog.show(getString(R.string.confirm_email)
                                + "\n" + mEtSendPasswordEmail.getText().toString()
                                + "\n" + getString(R.string.confirm_email_2));
                    } else {
                        final HookUpDialog dialog = new HookUpDialog(ActivitySignIn.this);
                        dialog.showOnlyOK(getString(R.string.email_not_valid));
                    }
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
                    mEtSignInUsername.requestFocus();
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

        mBtnBirthday.setOnClickListener(new OnClickListener() {
            public void setReturnDate(int year, int month, int day) {
                datePicked(year, month, day);
            }

            @Override
            public void onClick(View v) {
                Dialog datePickerDialog = new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {}
                }, 2015, 5, 22);

                datePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        setReturnDate(((DatePickerDialog) dialog).getDatePicker().getYear(),
                                ((DatePickerDialog) dialog).getDatePicker().getMonth(), ((DatePickerDialog) dialog)
                                        .getDatePicker().getDayOfMonth());
                    }
                });
                datePickerDialog.show();
            }
        });

        mSpGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0) {
                    mSignUpGender = "";
                } else {
                    mSignUpGender = String.valueOf(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
		//------------------------INITIAL VISIBILITY FOR LAYOUTS-------------------------//
        setActiveScreen(Screen.SIGN_BODY);
		//--------------------------------SET TYPEFACE-----------------------------------//
		mEtSignInUsername.setTypeface(    ConversaApp.getTfRalewayRegular());
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
		//----------------------------FORGOT PASSWORD DIAG-------------------------------//
		mSendPasswordDialog = new HookUpDialog(this);
		mSendPasswordDialog.setOnButtonClickListener(HookUpDialog.BUTTON_OK,
				new OnClickListener() {
					@Override
					public void onClick(View v) {
                        String sentToEmail = mEtSendPasswordEmail.getText().toString();
                        ParseUser.requestPasswordResetInBackground(sentToEmail, new RequestPasswordResetCallback() {
                            public void done(ParseException e) {
                                mSendPasswordDialog.dismiss();
                                if (e == null) {
                                    SendPasswordListener(true);
                                } else {
                                    SendPasswordListener(false);
                                }
                            }
                        });
					}
				});
		mSendPasswordDialog.setOnButtonClickListener(
				HookUpDialog.BUTTON_CANCEL, new OnClickListener() {
					@Override
					public void onClick(View v) {
						mSendPasswordDialog.dismiss();
					}
				});
	}

    public void datePicked(int year, int month, int day) {
        month++;

        if(month < 10)
            mSignUpBirthday = String.valueOf(year) + "-0" + String.valueOf(month);
        else
            mSignUpBirthday = String.valueOf(year) + "-" + String.valueOf(month);;

        if(day < 10)
            mSignUpBirthday = mSignUpBirthday + "-0" + String.valueOf(day);
        else
            mSignUpBirthday = mSignUpBirthday + "-" + String.valueOf(day);

        mBtnBirthday.setText(mSignUpBirthday);
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

                mSpGender.setSelection(0,true);
                mBtnBirthday.setText(getString(R.string.birthday));
                mSignUpBirthday = "";
                mSignUpGender   = "";

                mEtSignInUsername.setText("");
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
			final HookUpDialog dialog = new HookUpDialog(ActivitySignIn.this);
			dialog.showOnlyOK(nameResult);
			return false;
		} else {
			return true;
		}
	}

	private boolean isPasswordValid(String password) {
		String passwordResult = Utils.checkPassword(this, password);
		if (!passwordResult.equals(getString(R.string.password_ok))) {
			final HookUpDialog dialog = new HookUpDialog(ActivitySignIn.this);
			dialog.showOnlyOK(passwordResult);
			return false;
		} else {
			return true;
		}
	}

	private boolean isEmailValid(String email) {
		String emailResult = Utils.checkEmail(this, email);
		if (!emailResult.equals(getString(R.string.email_ok))) {
			final HookUpDialog dialog = new HookUpDialog(ActivitySignIn.this);
			dialog.showOnlyOK(emailResult);
			return false;
		} else {
			return true;
		}
	}

    private boolean isBirthdayValid() {
        boolean isValid = !mSignUpBirthday.isEmpty();
        if (isValid) {
            try {
                String date[] = mSignUpBirthday.split("-");
                int year1 = Integer.valueOf(date[0]);
                int year2 = Calendar.getInstance().get(Calendar.YEAR);
                isValid = (year2 - year1) > 17;
                if(isValid) {
                    return true;
                } else {
                    final HookUpDialog dialog = new HookUpDialog(ActivitySignIn.this);
                    dialog.showOnlyOK(getString(R.string.birthday_error1));
                    return false;
                }
            } catch(NumberFormatException e) {
                return false;
            }
        } else {
            final HookUpDialog dialog = new HookUpDialog(ActivitySignIn.this);
            dialog.showOnlyOK(getString(R.string.birthday_error));
            return false;
        }
    }

    private boolean isGenderValid() {
        boolean isValid = !mSignUpGender.isEmpty();
        if (isValid) {
            try {
                isValid = Integer.valueOf(mSignUpGender) > 0 && Integer.valueOf(mSignUpGender) < 3;
                if(isValid) {
                    return true;
                } else {
                    final HookUpDialog dialog = new HookUpDialog(ActivitySignIn.this);
                    dialog.showOnlyOK(getString(R.string.gender_error));
                    return false;
                }
            } catch(NumberFormatException e) {
                return false;
            }
        } else {
            final HookUpDialog dialog = new HookUpDialog(ActivitySignIn.this);
            dialog.showOnlyOK(getString(R.string.gender_error));
            return false;
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
	// ********* Log In Listener ********* /
    public void AuthListener(boolean result, ParseException error) {
        if(result) {
            Intent intent = new Intent(ActivitySignIn.this, ActivityMain.class);
            ActivitySignIn.this.startActivity(intent);
            ActivitySignIn.this.finish();
        } else {
            Toast.makeText(ActivitySignIn.this, getString(R.string.no_user_registered), Toast.LENGTH_SHORT).show();
        }
    }

	// ********* Sign Up Listener ********* /
    public void CreateUserListener(boolean result, ParseException error) {
        if (result) {
            AuthListener(result, error);
        } else {
//            if(result.equals("1")) {
//                Toast.makeText(ActivitySignIn.this, getString(R.string.email_already_taken), Toast.LENGTH_SHORT).show();
//            } else {
//                if(result.equals("2")) {
//                    Toast.makeText(ActivitySignIn.this, getString(R.string.name_already_taken), Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(ActivitySignIn.this, getString(R.string.an_internal_error_has_occurred), Toast.LENGTH_SHORT).show();
//                }
//            }
        }
    }

    // ********* Password recovery Listener ********* /
    public void SendPasswordListener(boolean result) {
        if(result) {
            Toast.makeText(ActivitySignIn.this, getString(R.string.email_sent), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(ActivitySignIn.this, getString(R.string.email_fail_sent), Toast.LENGTH_SHORT).show();
        }
    }
}
