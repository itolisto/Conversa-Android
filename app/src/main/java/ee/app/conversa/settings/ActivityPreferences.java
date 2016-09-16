package ee.app.conversa.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import ee.app.conversa.R;
import ee.app.conversa.extendables.ConversaActivity;
import ee.app.conversa.settings.language.DynamicLanguage;
import ee.app.conversa.utils.Preferences;

/**
 * Created by edgargomez on 9/14/16.
 */
public class ActivityPreferences extends ConversaActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final DynamicLanguage dynamicLanguage = new DynamicLanguage();

    @Override
    protected void onPreCreate() {
        dynamicLanguage.onCreate(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame,
                        new FragmentSettings()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack();
            } else {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        dynamicLanguage.onResume(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Preferences.LANGUAGE_PREF)) {
            recreate();
        }
    }

}
