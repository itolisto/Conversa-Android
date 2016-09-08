package ee.app.conversa;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.Button;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
		initialization();
	}

    @Override
    protected void onResume() {
        super.onResume();
        Utils.hideKeyboard(this);
    }

    protected void initialization() {
        super.initialization();
        Button mBtnSignIn = (Button) findViewById(R.id.btnSignIn);
        Button mBtnSignUp = (Button) findViewById(R.id.btnSignUp);

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

        if(mBtnSignIn != null) {
            mBtnSignIn.setOnClickListener(this);
            mBtnSignIn.setTypeface(ConversaApp.getInstance(this).getTfRalewayMedium());
        }

        if(mBtnSignUp != null) {
            mBtnSignUp.setOnClickListener(this);
            mBtnSignUp.setTypeface(ConversaApp.getInstance(this).getTfRalewayMedium());
        }
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
        }
    }

}