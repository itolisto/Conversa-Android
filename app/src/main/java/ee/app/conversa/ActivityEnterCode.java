package ee.app.conversa;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import com.chaos.view.PinView;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;
import ee.app.conversa.extendables.BaseActivity;

public class ActivityEnterCode extends BaseActivity implements View.OnClickListener {

    private PinView mPinView;
    private Button mBtnFeelLucky;
    public Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_code);
        initialization();
    }

    @Override
    protected void initialization() {
        super.initialization();
        mBtnFeelLucky = (Button) findViewById(R.id.btnFeelLucky);
        mPinView = (PinView) findViewById(R.id.pinView);

        mBtnFeelLucky.setOnClickListener(this);
        mPinView.addTextChangedListener(pinValidationWatcher);

        mActivity = this;
    }

    public void processPin() {
        String pin = mPinView.getText().toString();
        // Llamar funcion y cambiar a pantalla o mostrar alerta de pin invalido

        HashMap<String, String> params = new HashMap<>(2);
        params.put("code", pin);
        final ProgressDialog progress = new ProgressDialog(this);
        progress.show();


        ParseCloud.callFunctionInBackground("validateConversaCode", params, new FunctionCallback<Integer>() {
            @Override
            public void done(Integer object, ParseException e) {
                progress.dismiss();
                if (e == null) {
                    new SweetAlertDialog(mActivity, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText(mActivity.getResources().getString(R.string.success_code_title) )
                            .setContentText(getResources().getString(R.string.success_code))
                            .setConfirmText(getResources().getString(R.string.btn_success_code))
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    //sDialog.dismissWithAnimation();
                                    Intent intent = new Intent(mActivity, ActivitySignIn.class);
                                    mActivity.startActivity(intent);
                                }
                            })
                            .show();
                } else {
                    new SweetAlertDialog(mActivity, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText(mActivity.getResources().getString(R.string.fail_code_title) )
                            .setContentText(getResources().getString(R.string.fail_code))
                            .setConfirmText(getResources().getString(R.string.btn_fail_code))
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    //sDialog.dismissWithAnimation();
                                    Intent intent = new Intent(mActivity, ActivityGetCode.class);
                                    mActivity.startActivity(intent);
                                }
                            })
                            .show();
                }
            }
        });


    }

    @Override
    public void onClick(View v) {

        processPin();
    }

    private TextWatcher pinValidationWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @SuppressWarnings("ConstantConditions")
        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().length() == 6) {
                mBtnFeelLucky.setEnabled(true);
            } else {
                mBtnFeelLucky.setEnabled(false);
            }
        }
    };

}
