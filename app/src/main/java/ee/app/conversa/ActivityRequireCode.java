 package ee.app.conversa;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import ee.app.conversa.R;
import ee.app.conversa.extendables.BaseActivity;
import ee.app.conversa.view.LightTextView;
import ee.app.conversa.view.URLSpanNoUnderline;

 public class ActivityRequireCode extends BaseActivity implements View.OnClickListener {


     private Button mBtnEnterCode;
     private Button mBtnGetOne;
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

         mBtnGetOne = (Button) findViewById(R.id.btnGetOne);
         mBtnGetOne.setOnClickListener(this);

         ImageView mivLanguage = (ImageView) findViewById(R.id.ivLanguage);
         mivLanguage.setOnClickListener(this);




         LightTextView mLtvClickHere = (LightTextView) findViewById(R.id.ltvTapHere);
         String text = getString(R.string.tap_here);

         int index = TextUtils.indexOf(text, "?") + 2; // Index starts from zero but spannable string starts from one, plus whitespace

         Spannable styledString = new SpannableString(text);
         // url
         //styledString.setSpan(new URLSpanNoUnderline("https://conversa.typeform.com/to/RRg54U"), index, text.length(), 0);
         // change text color
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
             styledString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.light_blue, null)),
                     index, text.length(), 0);
         } else {
             styledString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.light_blue)),
                     index, text.length(), 0);
         }
         // this step is mandated for the url and clickable styles.
         mLtvClickHere.setMovementMethod(LinkMovementMethod.getInstance());
         mLtvClickHere.setText(styledString);
         mLtvClickHere.setOnClickListener(this);


     }

     @Override
     public void onClick(View v) {

         switch(v.getId()) {
             case R.id.btnEnterCode : {
                Intent intent = new Intent(this, ActivityEnterCode.class);
                 this.startActivity(intent);
                break;

             }
             case R.id.btnGetOne:{
                 Intent intent = new Intent(this, ActivityGetCode.class);
                 this.startActivity(intent);
                 break;
             }
             case R.id.ltvTapHere: {
                Intent intent = new Intent(this, ActivityLogIn.class);
                 startActivity(intent);

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
