package ee.app.conversa;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import ee.app.conversa.extendables.BaseActivity;
import ee.app.conversa.utils.Const;
import ee.app.conversa.view.LightTextView;

/**
 * ActivitySignIn
 * 
 * Allows user to sign in, sign up or receive an email with password if user
 * is already registered with a valid email.
 * 
 */
public class ActivitySignIn extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        checkInternetConnection = false;
		initialization();
	}

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            if (intent.getExtras().getInt(Const.ACTION, -1) != -1) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setTitle(getString(R.string.sett_account_logout_title));
                dialogBuilder.setMessage(getString(R.string.parse_logout_reason));
                dialogBuilder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
                AlertDialog b = dialogBuilder.create();
                b.show();
            }
        }
    }

    protected void initialization() {
        super.initialization();
        Button mBtnSignIn = (Button) findViewById(R.id.btnSignIn);
        Button mBtnSignUp = (Button) findViewById(R.id.btnSignUp);
        ImageView mivLanguage = (ImageView) findViewById(R.id.ivLanguage);

        LightTextView mLtvClickHere = (LightTextView) findViewById(R.id.ltvClickHere);
        if (mLtvClickHere != null) {
            String text = getString(R.string.string_signin_sign_up_business_two);

            int index = TextUtils.indexOf(text, "?") + 2; // Index starts from zero but spannable string starts from one, plus whitespace

            Spannable styledString = new SpannableString(text);
            // url
            styledString.setSpan(new URLSpan("http://www.google.com"), index, text.length(), 0);
            // change text color
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                styledString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.default_green, null)),
                        index, text.length(), 0);
            } else {
                styledString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.default_green)),
                        index, text.length(), 0);
            }
            // this step is mandated for the url and clickable styles.
            mLtvClickHere.setMovementMethod(LinkMovementMethod.getInstance());
            mLtvClickHere.setText(styledString);
        }

        if(mBtnSignIn != null) {
            mBtnSignIn.setOnClickListener(this);
            mBtnSignIn.setTypeface(ConversaApp.getInstance(this).getTfRalewayMedium());
        }

        if(mBtnSignUp != null) {
            mBtnSignUp.setOnClickListener(this);
            mBtnSignUp.setTypeface(ConversaApp.getInstance(this).getTfRalewayMedium());
        }

        mivLanguage.setOnClickListener(this);
	}

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSignIn: {
                Intent intent = new Intent(getApplicationContext(), ActivityLogIn.class);
                startActivity(intent);
                break;
            }
            case R.id.btnSignUp: {
                Intent intent = new Intent(getApplicationContext(), ActivitySignUp.class);
                startActivity(intent);
                break;
            }
            case R.id.ivLanguage: {
                final int index;

                switch(ConversaApp.getInstance(getBaseContext()).getPreferences().getLanguage()) {
                    case "en":
                        index = 1;
                        break;
                    case "es":
                        index = 2;
                        break;
                    default:
                        index = 0;
                        break;
                }

                AlertDialog.Builder b = new AlertDialog.Builder(this);
                b.setTitle(R.string.language_spinner_title);
                b.setSingleChoiceItems(R.array.language_entries, index, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (which != index) {
                            ConversaApp.getInstance(getBaseContext()).getPreferences()
                                    .setLanguage(getResources().getStringArray(R.array.language_values)[which]);
                            recreate();
                        }
                    }
                });
                b.show();
                break;
            }
        }
    }

}