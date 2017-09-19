package ee.app.conversa;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ee.app.conversa.extendables.BaseActivity;
import ee.app.conversa.extendables.ConversaFragment;
import ee.app.conversa.view.LightTextView;
import ee.app.conversa.view.MediumTextView;

public class ActivityGetCode extends BaseActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_code);
        initialization();
    }

    @Override
    protected void initialization() {
        super.initialization();

        Button mBtnGetCode = (Button) findViewById(R.id.btnGetCode);
        mBtnGetCode.setOnClickListener(this);




        MediumTextView mLtvClickHere = (MediumTextView) findViewById(R.id.ltvShareThis);
        String text = getResources().getString(R.string.info_get_code2);

        int index=0;
        int endIndex=0;
        switch(ConversaApp.getInstance(getBaseContext()).getPreferences().getLanguage()) {
            case "en":
                index = TextUtils.indexOf(text, "this") ;
                endIndex = index + 4;
                break;
            case "es":
                index = TextUtils.indexOf(text, "esto") ;
                endIndex = index + 4;
                break;
        }
        // Index starts from zero but spannable string starts from one, plus whitespace

        Spannable styledString = new SpannableString(text);
        // url
        //styledString.setSpan(new URLSpanNoUnderline("https://conversa.typeform.com/to/RRg54U"), index, text.length(), 0);
        // change text color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            styledString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.light_blue, null)),
                    index, endIndex, 0);
            styledString.setSpan(new UnderlineSpan(), index,endIndex,0);
        } else {
            styledString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.light_blue)),
                    index, endIndex, 0);
            styledString.setSpan(new UnderlineSpan(), index,endIndex,0);
        }
        // this step is mandated for the url and clickable styles.
        mLtvClickHere.setMovementMethod(LinkMovementMethod.getInstance());
        mLtvClickHere.setText(styledString);
        mLtvClickHere.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.ltvShareThis:
            {
                final Intent intent_one = new Intent(android.content.Intent.ACTION_SEND);
                intent_one.setType("text/plain");
                // Add data to the intent, the receiving app will decide what to do with it.
                intent_one.putExtra(Intent.EXTRA_SUBJECT,
                        this.getString(R.string.app_name));
                intent_one.putExtra(Intent.EXTRA_TEXT,
                        this.getResources().getString(R.string.message_share_code) + " " + Uri.parse("http://codigos.conversachat.com"));

                final List<ResolveInfo> activities = this
                        .getPackageManager().queryIntentActivities(intent_one, 0);

                List<String> appNames = new ArrayList<>(2);
                List<Drawable> appIcons = new ArrayList<>(2);

                for (ResolveInfo info : activities) {
                    appNames.add(info.loadLabel(this.getPackageManager()).toString());
                    String packageName = info.activityInfo.packageName;

                    try {
                        Drawable icon = this.getPackageManager().getApplicationIcon(packageName);
                        appIcons.add(icon);
                    } catch (PackageManager.NameNotFoundException e) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            appIcons.add(getResources().getDrawable(R.drawable.ic_business_default, null));
                        } else {
                            appIcons.add(getResources().getDrawable(R.drawable.ic_business_default));
                        }
                    }
                }

                ListAdapter adapter = new ArrayAdapterWithIcon(this, appNames, appIcons);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString(R.string.settings_share_conversa));
                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ResolveInfo info = activities.get(which);
                        //if (info.activityInfo.packageName.equals("com.facebook.katana")) {
                        // Facebook was chosen
                        //}
                        // Start the selected activity
                        intent_one.setPackage(info.activityInfo.packageName);
                        startActivity(intent_one);
                    }
                });

                AlertDialog share = builder.create();
                share.show();
                return;
            }
            case R.id.btnGetCode: {
                Uri uri = Uri.parse("http://codigos.conversachat.com");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);

                break;
            }


        }
    }

    public class ArrayAdapterWithIcon extends ArrayAdapter<String> {

        private List<Drawable> images;

        ArrayAdapterWithIcon(Context context, List<String> items, List<Drawable> images) {
            super(context, android.R.layout.select_dialog_item, items);
            this.images = images;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView textView = (TextView) view.findViewById(android.R.id.text1);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                textView.setCompoundDrawablesRelativeWithIntrinsicBounds(images.get(position), null, null, null);
            } else {
                textView.setCompoundDrawablesWithIntrinsicBounds(images.get(position), null, null, null);
            }
            textView.setCompoundDrawablePadding(
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getContext().getResources().getDisplayMetrics()));
            return view;
        }
    }
}
