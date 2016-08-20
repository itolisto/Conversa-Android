package ee.app.conversa.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import ee.app.conversa.R;

/**
 * Created by edgargomez on 7/26/16.
 */
public class CustomDeleteUserDialog extends AlertDialog {

    private Context mContext;
    private TextView mTitle;
    private TextView mContent;
    private Button mPositive;
    private Button mNegative;
    private FrameLayout mCustomContainer;
    private ScrollView mScrollText;

    private String title;
    private String contentText;
    private View customView;
    private Integer customResId;

    private Button.OnClickListener mPositiveClickListener;
    private Button.OnClickListener mNegativeClickListener;

    private String positiveText;
    private String negativeText;
    private boolean canDismiss = true;

    public CustomDeleteUserDialog(Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.custom_delete_user_dialog);
        mTitle = (TextView) findViewById(android.R.id.text1);
        mContent = (TextView) findViewById(android.R.id.text2);
        mCustomContainer = (FrameLayout) findViewById(R.id.content);
        mPositive = (Button) findViewById(android.R.id.button2);
        mNegative = (Button) findViewById(android.R.id.button1);
        mScrollText = (ScrollView) findViewById(R.id.scrolltext);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(title != null) {
            mTitle.setText(title);
        }else{
            mTitle.setVisibility(View.GONE);
        }

        if(contentText != null) {
            mContent.setText(contentText);
        }else {
            mScrollText.setVisibility(View.GONE);
        }

        if(customView != null && customResId == null) {
            mCustomContainer.addView(customView);
        }else if(customView == null && customResId != null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            customView = inflater.inflate(customResId, null, false);
            mCustomContainer.addView(customView);
        }else if(customView == null && customResId == null){
            //mContent.setVisibility(View.GONE);
        }

        if(positiveText != null && mPositiveClickListener != null) {
            mPositive.setText(positiveText);
            mPositive.setOnClickListener(mPositiveClickListener);
        }else{
            mPositive.setVisibility(View.GONE);
        }

        if(negativeText != null && mNegativeClickListener != null) {
            mNegative.setText(negativeText);
            mNegative.setOnClickListener(mNegativeClickListener);
        }else {
            mNegative.setVisibility(View.GONE);
        }
        this.setCanceledOnTouchOutside(canDismiss);
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

    }

    public CustomDeleteUserDialog setTitle(String t) {
        this.title = t;
        return this;
    }

    public CustomDeleteUserDialog setMessage(String m) {
        this.contentText = m;
        return this;
    }

    public CustomDeleteUserDialog setupPositiveButton(String text, Button.OnClickListener listener) {
        this.positiveText = text;
        this.mPositiveClickListener = listener;
        return this;
    }

    public CustomDeleteUserDialog setupNegativeButton(String text, Button.OnClickListener listener) {
        this.negativeText = text;
        this.mNegativeClickListener = listener;
        return this;
    }

    public CustomDeleteUserDialog dismissOnTouchOutside(boolean dismiss) {
        this.canDismiss = dismiss;
        return this;
    }

    public View getCustomView() {
        return this.customView;
    }

}