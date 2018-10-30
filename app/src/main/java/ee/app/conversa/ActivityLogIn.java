package ee.app.conversa;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import ee.app.conversa.extendables.BaseActivity;
import ee.app.conversa.utils.AppActions;
import ee.app.conversa.utils.Utils;

/**
 * Created by edgargomez on 8/12/16.
 */
public class ActivityLogIn extends BaseActivity implements View.OnClickListener {

    private Button mBtnSignInIn;
    private EditText mEtSignInEmail;
    private EditText mEtSignInPassword;

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
        mBtnSignInIn = (Button) findViewById(R.id.btnSignInIn);

        Button mBtnForgotPassword = (Button) findViewById(R.id.btnForgotPassword);
        TextInputLayout mTilSignInEmail = (TextInputLayout) findViewById(R.id.tilEmail);
        TextInputLayout mTilSignInPassword = (TextInputLayout) findViewById(R.id.tilPassword);

        mTilSignInEmail.setOnClickListener(this);
        mTilSignInPassword.setOnClickListener(this);

        if (mBtnSignInIn != null) {
            mBtnSignInIn.setOnClickListener(this);
            mBtnSignInIn.setTypeface(ConversaApp.getInstance(this).getTfRalewayMedium());
        }

        if (mBtnForgotPassword != null) {
            mBtnForgotPassword.setOnClickListener(this);
            mBtnForgotPassword.setTypeface(ConversaApp.getInstance(this).getTfRalewayLight());
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
                if (validateForm()) {
                    final String mSignInEmail = mEtSignInEmail.getText().toString();
                    final String mSignInPassword = mEtSignInPassword.getText().toString();

                    final ProgressDialog progress = ProgressDialog.show(this, null, null, true, false);
                    progress.setContentView(R.layout.progress_layout);

                    ParseUser.logInInBackground(mSignInEmail, mSignInPassword, new LogInCallback() {
                        public void done(ParseUser user, ParseException e) {
                            progress.dismiss();
                            if (user != null) {
                                AuthListener(true, null);
                            } else {
                                AuthListener(false, e);
                            }
                        }
                    });
                }
                break;
        }
    }

    private boolean validateForm() {
        String title = null;
        EditText select = null;

        if (mEtSignInEmail.getText().toString().isEmpty()) {
            select = mEtSignInEmail;
            title = getString(R.string.common_field_required);
        } else if (!Utils.checkEmail(mEtSignInEmail.getText().toString())) {
            select = mEtSignInEmail;
            title = getString(R.string.common_field_invalid_email);
        } else if (mEtSignInPassword.getText().toString().isEmpty()) {
            select = mEtSignInPassword;
            title = getString(R.string.common_field_required);
        }

        if (title != null) {
            final EditText active = select;
            new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
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
            builder.setMessage(getString(R.string.no_user_registered));

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

}