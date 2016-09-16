package ee.app.conversa.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;

import ee.app.conversa.R;

/**
 * Created by edgargomez on 9/15/16.
 */
public class SwitchCompatPreference extends CheckBoxPreference {

    public SwitchCompatPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SwitchCompatPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public SwitchCompatPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SwitchCompatPreference(Context context) {
        super(context, null);
        init();
    }

    private void init() {
        setWidgetLayoutResource(R.layout.preference_switch_layout);
    }

}