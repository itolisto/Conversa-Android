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

package ee.app.conversa.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.ArrayRes;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ee.app.conversa.R;

/**
 * Preferences
 *
 * Holds and managed application's preferences.
 */
public class Preferences {

    private final Context context;
    private final SharedPreferences sharedPreferences;

    /**
     * Gets a SharedPreferences instance that points to the default file that is
     * used by the preference framework in the given context.
     */
    public Preferences(Context context) {
        this.context = context;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public SharedPreferences getSharePreferences() {
        return sharedPreferences;
    }

    public void cleanSharedPreferences() {
        sharedPreferences.edit().clear().apply();
    }

    /* ******************************************************************************** */
	/* ************************************ GETTERS *********************************** */
	/* ******************************************************************************** */
    public String getPushKey() {
        return getStringPreference(PreferencesKeys.PUSH_KEY, "");
    }

    public boolean getShowTutorial() {
        return getBooleanPreference(PreferencesKeys.TUTORIAL_KEY, true);
    }

    public boolean getGuideExplore(){
        return getBooleanPreference(PreferencesKeys.GUIDE_EXPLORE_KEY,false);
    }

    public String getAccountCustomerId() {
        return getStringPreference(PreferencesKeys.ACCOUNT_CUSTOMER_ID_KEY, "");
    }

    public String getAccountDisplayName() {
        return getStringPreference(PreferencesKeys.ACCOUNT_DISPLAY_NAME_KEY, "");
    }

    public int getAccountGender() {
        return getIntegerPreference(PreferencesKeys.ACCOUNT_GENDER_KEY, 2);
    }

    public String getAccountBirthday() {
        return getStringPreference(PreferencesKeys.ACCOUNT_BIRTHDAY_KEY, "");
    }


    public String getLanguage() {
        return getStringPreference(PreferencesKeys.MAIN_LANGUAGE_KEY, "es");
    }

    public String getLanguageName() {
        String language = getStringPreference(PreferencesKeys.MAIN_LANGUAGE_KEY, "es");
        if (language.equals("zz")) {
            return context.getResources().getStringArray(R.array.language_entries)[0];
        } else if (language.equals("en")) {
            return context.getResources().getStringArray(R.array.language_entries)[1];
        } else {
            return context.getResources().getStringArray(R.array.language_entries)[2];
        }
    }

    public int getUploadQualityPosition() {
        int position = getIntegerPreference(PreferencesKeys.CHAT_QUALITY_KEY, -1);
        if (position == -1) {
            return 1;
        } else {
            return position;
        }
    }

    public String getUploadQuality() {
        int i = getIntegerPreference(PreferencesKeys.CHAT_QUALITY_KEY, -1);
        if (i == -1) {
            return context.getResources().getStringArray(R.array.sett_chat_quality_entries)[1];
        } else {
            return context.getResources().getStringArray(R.array.sett_chat_quality_entries)[i];
        }
    }

    public String getUploadQualityFromNewValue(int position) {
        if (position >= 0 || position < context.getResources().getStringArray(R.array.sett_chat_quality_entries).length) {
            return context.getResources().getStringArray(R.array.sett_chat_quality_entries)[position];
        } else {
            return context.getResources().getStringArray(R.array.sett_chat_quality_entries)[1];
        }
    }

    public boolean getDownloadAutomatically() {
        return getBooleanPreference(PreferencesKeys.CHAT_DOWNLOAD_KEY, true);
    }

    public boolean getPlaySoundWhenSending() {
        return getBooleanPreference(PreferencesKeys.CHAT_SOUND_SENDING_KEY, true);
    }

    public boolean getPlaySoundWhenReceiving() {
        return getBooleanPreference(PreferencesKeys.CHAT_SOUND_RECEIVING_KEY, true);
    }

    public boolean getPushNotificationSound() {
        return getBooleanPreference(PreferencesKeys.NOTIFICATION_SOUND_KEY, true);
    }

    public boolean getPushNotificationPreview() {
        return getBooleanPreference(PreferencesKeys.NOTIFICATION_PREVIEW_KEY, true);
    }

    public boolean getInAppNotificationSound() {
        return getBooleanPreference(PreferencesKeys.NOTIFICATION_INAPP_SOUND_KEY, true);
    }

    public boolean getInAppNotificationPreview() {
        return getBooleanPreference(PreferencesKeys.NOTIFICATION_INAPP_PREVIEW_KEY, true);
    }

    public String getFirebaseToken() {
        return getStringPreference(PreferencesKeys.FIREBASE_TOKEN_ID, "");
    }

    /* ******************************************************************************** */
	/* ************************************ SETTERS *********************************** */
	/* ******************************************************************************** */
    public void setPushKey(String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PreferencesKeys.PUSH_KEY, value);
        editor.apply();
    }

    public void setShowTutorial(boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PreferencesKeys.TUTORIAL_KEY, value);
        editor.apply();
    }

    public void setGuideExplore(boolean value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PreferencesKeys.GUIDE_EXPLORE_KEY, value);
        editor.apply();
    }

    public void setAccountCustomerId(String id, boolean onBackground) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PreferencesKeys.ACCOUNT_CUSTOMER_ID_KEY, id);
        if (onBackground) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    public void setAccountDisplayName(String value, boolean inBackground) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PreferencesKeys.ACCOUNT_DISPLAY_NAME_KEY, value);
        if (inBackground) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    public void setAccountGender(int value, boolean inBackground) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(PreferencesKeys.ACCOUNT_GENDER_KEY, value);
        if (inBackground) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    public void setAccountBirthday(String value, boolean inBackground) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PreferencesKeys.ACCOUNT_BIRTHDAY_KEY, value);
        if (inBackground) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    public void setLanguage(String language) {
        setStringPreference(PreferencesKeys.MAIN_LANGUAGE_KEY, language);
    }

    public void setUploadQuality(int position) {
        if (position >= 0 && position < context.getResources().getStringArray(R.array.sett_chat_quality_entries).length) {
            setIntegerPrefrence(PreferencesKeys.CHAT_QUALITY_KEY, position);
        } else {
            setIntegerPrefrence(PreferencesKeys.CHAT_QUALITY_KEY, 1);
        }
    }

    public void setDownloadAutomatically(boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PreferencesKeys.CHAT_DOWNLOAD_KEY, value);
        editor.apply();
    }

    public void setPlaySoundWhenSending(boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PreferencesKeys.CHAT_SOUND_SENDING_KEY, value);
        editor.apply();
    }

    public void setPlaySoundWhenReceiving(boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PreferencesKeys.CHAT_SOUND_RECEIVING_KEY, value);
        editor.apply();
    }

    public void setPushNotificationSound(boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PreferencesKeys.NOTIFICATION_SOUND_KEY, value);
        editor.apply();
    }

    public void setPushNotificationPreview(boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PreferencesKeys.NOTIFICATION_PREVIEW_KEY, value);
        editor.apply();
    }

    public void setInAppNotificationSound(boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PreferencesKeys.NOTIFICATION_INAPP_SOUND_KEY, value);
        editor.apply();
    }

    public void setInAppNotificationPreview(boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PreferencesKeys.NOTIFICATION_INAPP_PREVIEW_KEY, value);
        editor.apply();
    }

    public void setFirebaseToken(String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PreferencesKeys.FIREBASE_TOKEN_ID, value);
        editor.apply();
    }

    /* ******************************************************************************** */
	/* ******************************************************************************** */
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