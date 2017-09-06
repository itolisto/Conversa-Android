 package ee.app.conversa;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import ee.app.conversa.R;
import ee.app.conversa.extendables.BaseActivity;

 public class ActivityRequireCode extends BaseActivity implements View.OnClickListener {


     private Button mBtnEnterCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_require_code);
        initialization();
    }


     @Override
     protected void initialization() {
         //super.initialization();
         mBtnEnterCode = (Button) findViewById(R.id.btnEnterCode);
         mBtnEnterCode.setOnClickListener(this);
     }

     @Override
     public void onClick(View v) {

         switch(v.getId()) {
             case R.id.btnEnterCode : {
                Intent intent = new Intent(this, ActivityEnterCode.class);
                 this.startActivity(intent);


             }

         }


     }
 }
