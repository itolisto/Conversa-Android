/*
 * The MIT License (MIT)
 * 
 * Copyright � 2013 Clover Studio Ltd. All rights reserved.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ee.app.conversa.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.ArrayRes;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ee.app.conversa.settings.PreferencesKeys;

/**
 * Preferences
 *
 * Holds and managed application's preferences.
 */

public class Preferences {

    private final String TAG = Preferences.class.getSimpleName();

    // Defining SharedPreferences entries
    private final String CATEGORIES_LOAD = "CATEGORIES_LOAD";
    private final String CURRENT_CATEGORY = "CURRENT_CATEGORY";

    private SharedPreferences sharedPreferences;

    /**
     * Gets a SharedPreferences instance that points to the default file that is
     * used by the preference framework in the given context.
     */
    public Preferences(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void cleanSharedPreferences() {
        sharedPreferences.edit().clear().apply();
    }

    public boolean getCategoriesLoad() {
        return getBooleanPreference(CATEGORIES_LOAD, false);
    }

    public String getCurrentCategory() {
        return getStringPreference(CURRENT_CATEGORY, "");
    }

    public void setCategoriesLoad(boolean value, boolean inBackground) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(CATEGORIES_LOAD, value);
        if (inBackground) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    public void setCurrentCategory(String value, boolean inBackground) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CURRENT_CATEGORY, value);
        if (inBackground) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    public String getLanguage() {
        return getStringPreference(PreferencesKeys.PREFERENCE_MAIN_LANGUAGE_KEY, "es");
    }

    public void setLanguage(String language) {
        setStringPreference(PreferencesKeys.PREFERENCE_MAIN_LANGUAGE_KEY, language);
    }

    public void setBooleanPreference(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getBooleanPreference(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public void setStringPreference(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    public String getStringPreference(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    private int getIntegerPreference(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    public void setIntegerPrefrence(String key, int value) {
        sharedPreferences.edit().putInt(key, value).apply();
    }

    public boolean setIntegerPrefrenceBlocking(String key, int value) {
        return sharedPreferences.edit().putInt(key, value).commit();
    }

    private long getLongPreference(String key, long defaultValue) {
        return sharedPreferences.getLong(key, defaultValue);
    }

    private void setLongPreference(String key, long value) {
        sharedPreferences.edit().putLong(key, value).apply();
    }

    private @NonNull Set<String> getMediaDownloadAllowed(Context context, String key, @ArrayRes int defaultValuesRes) {
        return getStringSetPreference(
                key,
                new HashSet<>(Arrays.asList(context.getResources().getStringArray(defaultValuesRes))));
    }

    private Set<String> getStringSetPreference(String key, Set<String> defaultValues) {
        if (sharedPreferences.contains(key)) {
            return sharedPreferences.getStringSet(key, Collections.<String>emptySet());
        } else {
            return defaultValues;
        }
    }

}
