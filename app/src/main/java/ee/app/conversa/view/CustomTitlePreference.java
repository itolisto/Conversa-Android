package ee.app.conversa.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import ee.app.conversa.R;

/**
 * Created by edgargomez on 9/28/16.
 */

public class CustomTitlePreference extends Preference {

    private int titleColor;
    private int summaryColor;

    public CustomTitlePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CustomTitlePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomTitlePreference, 0, 0);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            titleColor = a.getColor(R.styleable.CustomTitlePreference_titleColor, getContext().getResources().getColor(android.R.color.black, null));
            summaryColor = a.getColor(R.styleable.CustomTitlePreference_titleColor, getContext().getResources().getColor(android.R.color.black, null));
        } else {
            titleColor = a.getColor(R.styleable.CustomTitlePreference_titleColor, getContext().getResources().getColor(android.R.color.black));
            summaryColor = a.getColor(R.styleable.CustomTitlePreference_titleColor, getContext().getResources().getColor(android.R.color.black));
        }

        a.recycle();
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        TextView tvTitle = (TextView) view.findViewById(android.R.id.title);
        TextView tvSummary = (TextView) view.findViewById(android.R.id.summary);

        if (titleColor != -1) {
            tvTitle.setTextColor(titleColor);
        }

        if (summaryColor != -1) {
            tvSummary.setTextColor(summaryColor);
        }
    }

}