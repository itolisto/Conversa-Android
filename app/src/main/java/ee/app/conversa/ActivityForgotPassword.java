package ee.app.conversa;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

import ee.app.conversa.extendables.BaseActivity;
import ee.app.conversa.utils.Utils;

/**
 * Created by edgargomez on 8/12/16.
 */
public class ActivityForgotPassword extends BaseActivity implements View.OnClickListener {

    private Button mBtnSendPassword;
    private EditText mEtSendPasswordEmail;
    private TextInputLayout mTilForgotPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        initialization();
    }

    @Override
    protected void initialization() {
        super.initialization();
        mEtSendPasswordEmail = (EditText) findViewById(R.id.etSendEmail);
        mTilForgotPassword = (TextInputLayout) findViewById(R.id.tilPasswordForgot);
        mBtnSendPassword = (Button) findViewById(R.id.btnSendPassword);
        mEtSendPasswordEmail.addTextChangedListener(new MyTextWatcher(mEtSendPasswordEmail));

        if (mTilForgotPassword != null) {
            mTilForgotPassword.setOnClickListener(this);
        }

        if(mBtnSendPassword != null) {
            mBtnSendPassword.setOnClickListener(this);
            mBtnSendPassword.setTypeface(ConversaApp.getInstance(this).getTfRalewayMedium());
        }
    }

    @Override
    public void yesInternetConnection() {
        super.yesInternetConnection();
        mBtnSendPassword.setEnabled(true);
    }

    @Override
    public void noInternetConnection() {
        super.noInternetConnection();
        mBtnSendPassword.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tilPasswordForgot:
                mEtSendPasswordEmail.requestFocus();
                break;
            case R.id.btnSendPassword:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.confirm_email, mEtSendPasswordEmail.getText().toString()))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String sentToEmail = mEtSendPasswordEmail.getText().toString();
                                ParseUser.requestPasswordResetInBackground(sentToEmail, new RequestPasswordResetCallback() {
                                    public void done(ParseException e) {
                                        if(e == null) {
                                            Toast.makeText(getApplicationContext(), getString(R.string.email_sent), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(), getString(R.string.email_fail_sent), Toast.LENGTH_SHORT).show();
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
                break;
        }
    }

    private void isEmailValid(String email) {
        TextInputLayout layout = mTilForgotPassword;

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

    private boolean validateForm() {
        if (mEtSendPasswordEmail.getText().toString().isEmpty()) {
            mBtnSendPassword.setEnabled(false);
        } else if (mTilForgotPassword.isErrorEnabled()) {
            mBtnSendPassword.setEnabled(false);
        } else {
            mBtnSendPassword.setEnabled(true);
            return true;
        }

        return false;
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
                case R.id.etSendEmail:
                    isEmailValid(editable.toString());
                    break;
            }

            validateForm();
        }
    }
}