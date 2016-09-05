package ee.app.conversa.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;

import ee.app.conversa.R;
import ee.app.conversa.extendables.ConversaActivity;

/**
 * Created by edgargomez on 8/30/16.
 */
public class ActivityNotificationSettings extends ConversaActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_notification);
        initialization();
    }

}