package ee.app.conversa;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
        mBtnSendPassword = (Button) findViewById(R.id.btnSendPassword);

        TextInputLayout mTilForgotPassword = (TextInputLayout) findViewById(R.id.tilPasswordForgot);
        mTilForgotPassword.setOnClickListener(this);

        if (mBtnSendPassword != null) {
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
                if (validateForm()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(getString(R.string.confirm_email, mEtSendPasswordEmail.getText().toString()))
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    String sentToEmail = mEtSendPasswordEmail.getText().toString();
                                    ParseUser.requestPasswordResetInBackground(sentToEmail, new RequestPasswordResetCallback() {
                                        public void done(ParseException e) {
                                            String title;

                                            if (e == null) {
                                                title = getString(R.string.email_sent);
                                            } else {
                                                title = getString(R.string.email_fail_sent);
                                            }

                                            new AlertDialog.Builder(getApplicationContext())
                                                    .setTitle(title)
                                                    .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                        }
                                                    })
                                                    .show();
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

    private boolean validateForm() {
        String title = null;

        if (mEtSendPasswordEmail.getText().toString().isEmpty()) {
            title = getString(R.string.common_field_required);
        } else if (!Utils.checkEmail(mEtSendPasswordEmail.getText().toString())) {
            title = getString(R.string.common_field_invalid);
        }

        if (title != null) {
            new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            mEtSendPasswordEmail.requestFocus();
                        }
                    })
                    .show();
            return false;
        }

        return true;
    }

}