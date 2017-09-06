package ee.app.conversa.settings.language;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;

import java.util.Locale;

import ee.app.conversa.ConversaApp;

/**
 *
 * Copyright 2011 Whisper Systems
 * Copyright 2013-2016 Open Whisper Systems
 * Licensed under the GPLv3: http://www.gnu.org/licenses/gpl-3.0.html
 *
 * DynamicLanguage was taken from Signal-Android open source app. Signal-Android can be
 * found at https://github.com/WhisperSystems/Signal-Android
 */
public class DynamicLanguage {

    private static final String DEFAULT = "es";

    private Locale currentLocale;

    public void onCreate(Activity activity) {
        currentLocale = getSelectedLocale(activity);
        setContextLocale(activity, currentLocale);
    }

    public void onResume(Activity activity) {
        if (currentLocale == null) {
            return;
        }

        if (!currentLocale.equals(getSelectedLocale(activity))) {
            Intent intent = activity.getIntent();
            activity.finish();
            OverridePendingTransition.invoke(activity);
            activity.startActivity(intent);
            OverridePendingTransition.invoke(activity);
        }
    }

    // http://stackoverflow.com/a/2900144/5349296
    private static void setContextLocale(Context context, Locale selectedLocale) {
        Resources res = context.getResources();

        if (res != null) {
            Configuration configuration = res.getConfiguration();

            if (!configuration.locale.equals(selectedLocale)) {
                configuration.locale = selectedLocale;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    configuration.setLayoutDirection(selectedLocale);
                }
                context.getResources().updateConfiguration(configuration, res.getDisplayMetrics());
            }
        }
    }

    public static Locale getSelectedLocale(Context context) {
        String language[] = TextUtils.split(ConversaApp.getInstance(context).getPreferences().getLanguage(), "_");
        Resources res = context.getResources();

        if (res != null) {
            if (res.getConfiguration().locale.getLanguage().equals(new Locale(language[0]).getLanguage())) {
                return res.getConfiguration().locale;
            } else if (language.length == 2) {
                return new Locale(language[0], language[1]);
            } else {
                if (language[0].equals("zz")) {
                    return Locale.getDefault();
                } else {
                    return new Locale(language[0]);
                }
            }
        } else {
            return Locale.getDefault();
        }
    }

    private static final class OverridePendingTransition {
        static void invoke(Activity activity) {
            activity.overridePendingTransition(0, 0);
        }
    }

}