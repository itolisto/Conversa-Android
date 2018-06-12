package ee.app.conversa;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

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
        mEtSignInEmail = findViewById(R.id.etSignInEmail);
        mEtSignInPassword = findViewById(R.id.etSignInPassword);
        mBtnSignInIn = findViewById(R.id.btnSignInIn);

        Button mBtnForgotPassword = findViewById(R.id.btnForgotPassword);
        TextInputLayout mTilSignInEmail = findViewById(R.id.tilEmail);
        TextInputLayout mTilSignInPassword = findViewById(R.id.tilPassword);

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
                    final String email = mEtSignInEmail.getText().toString();
                    final String password = mEtSignInPassword.getText().toString();

                    final ProgressDialog progress = ProgressDialog.show(this, null, null, true, false);
                    progress.setContentView(R.layout.progress_layout);

                    FirebaseAuth mAuth = FirebaseAuth.getInstance();

                    mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progress.dismiss();
                                if (task.isSuccessful()) {
                                    AuthListener(true, null);
                                } else {
                                    AuthListener(false, task.getException());
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

    public void AuthListener(boolean result, Exception error) {
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