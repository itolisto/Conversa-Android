package ee.app.conversa;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import com.chaos.view.PinView;

import cn.pedant.SweetAlert.SweetAlertDialog;
import ee.app.conversa.extendables.BaseActivity;
import ee.app.conversa.management.AblyConnection;
import ee.app.conversa.utils.Logger;

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
        if(pin.equals("123456")){

            new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText(this.getResources().getString(R.string.success_code_title) )
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




        }else{

            new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(this.getResources().getString(R.string.fail_code_title) )
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
