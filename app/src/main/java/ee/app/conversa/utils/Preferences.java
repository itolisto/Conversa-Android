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
import android.hardware.Camera;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.ArrayRes;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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

    public final String LANGUAGE_PREF                    = "pref_language";

    private final String LAST_VERSION_CODE_PREF           = "last_version_code";
    private final String LAST_EXPERIENCE_VERSION_PREF     = "last_experience_version_code";
    public   final String RINGTONE_PREF                    = "pref_key_ringtone";
    private final String VIBRATE_PREF                     = "pref_key_vibrate";
    private final String NOTIFICATION_PREF                = "pref_key_enable_notifications";
    public   final String LED_COLOR_PREF                   = "pref_led_color";
    public   final String LED_BLINK_PREF                   = "pref_led_blink";
    private final String LED_BLINK_PREF_CUSTOM            = "pref_led_blink_custom";
    private final String ENTER_SENDS_PREF                 = "pref_enter_sends";
    private final String ENTER_PRESENT_PREF               = "pref_enter_key";
    public   final String REGISTERED_GCM_PREF              = "pref_gcm_registered";
    private final String GCM_PASSWORD_PREF                = "pref_gcm_password";
    private final String PROMPTED_PUSH_REGISTRATION_PREF  = "pref_prompted_push_registration";
    private final String PROMPTED_SHARE_PREF              = "pref_prompted_share";
    private final String DIRECTORY_FRESH_TIME_PREF        = "pref_directory_refresh_time";

    private final String LOCAL_REGISTRATION_ID_PREF       = "pref_local_registration_id";
    private final String SIGNED_PREKEY_REGISTERED_PREF    = "pref_signed_prekey_registered";
    private final String WIFI_SMS_PREF                    = "pref_wifi_sms";

    private final String GCM_REGISTRATION_ID_PREF         = "pref_gcm_registration_id";
    private final String GCM_REGISTRATION_ID_VERSION_PREF = "pref_gcm_registration_id_version";
    private final String RATING_LATER_PREF                = "pref_rating_later";
    private final String RATING_ENABLED_PREF              = "pref_rating_enabled";

    public   final String REPEAT_ALERTS_PREF               = "pref_repeat_alerts";
    public   final String NOTIFICATION_PRIVACY_PREF        = "pref_notification_privacy";
    public   final String NEW_CONTACTS_NOTIFICATIONS       = "pref_enable_new_contacts_notifications";

    public   final String MEDIA_DOWNLOAD_MOBILE_PREF       = "pref_media_download_mobile";
    public   final String MEDIA_DOWNLOAD_WIFI_PREF         = "pref_media_download_wifi";
    public   final String MEDIA_DOWNLOAD_ROAMING_PREF      = "pref_media_download_roaming";

    public   final String SYSTEM_EMOJI_PREF                = "pref_system_emoji";
    private final String MULTI_DEVICE_PROVISIONED_PREF    = "pref_multi_device";
    public   final String DIRECT_CAPTURE_CAMERA_ID         = "pref_direct_capture_camera_id";

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
        return getStringPreference(LANGUAGE_PREF, "en");
    }

    public void setLanguage(String language) {
        setStringPreference(LANGUAGE_PREF, language);
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

    private void setIntegerPrefrence(String key, int value) {
        sharedPreferences.edit().putInt(key, value).apply();
    }

    private boolean setIntegerPrefrenceBlocking(String key, int value) {
        return sharedPreferences.edit().putInt(key, value).commit();
    }

    private long getLongPreference(String key, long defaultValue) {
        return sharedPreferences.getLong(key, defaultValue);
    }

    private void setLongPreference(String key, long value) {
        sharedPreferences.edit().putLong(key, value).apply();
    }





































    public void setDirectCaptureCameraId(int value) {
        setIntegerPrefrence(DIRECT_CAPTURE_CAMERA_ID, value);
    }


    public int getDirectCaptureCameraId() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // your code using Camera API here - is between 1-20
        } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // your code using Camera2 API here - is api 21 or higher
        }
        return getIntegerPreference(DIRECT_CAPTURE_CAMERA_ID, Camera.CameraInfo.CAMERA_FACING_FRONT);
    }

    public void setMultiDevice(boolean value) {
        setBooleanPreference(MULTI_DEVICE_PROVISIONED_PREF, value);
    }

    public boolean isMultiDevice() {
        return getBooleanPreference(MULTI_DEVICE_PROVISIONED_PREF, false);
    }

//    public NotificationPrivacyPreference getNotificationPrivacy() {
//        return new NotificationPrivacyPreference(getStringPreference(NOTIFICATION_PRIVACY_PREF, "all"));
//    }

    public boolean isNewContactsNotificationEnabled() {
        return getBooleanPreference(NEW_CONTACTS_NOTIFICATIONS, true);
    }

    public long getRatingLaterTimestamp() {
        return getLongPreference(RATING_LATER_PREF, 0);
    }

    public void setRatingLaterTimestamp(long timestamp) {
        setLongPreference(RATING_LATER_PREF, timestamp);
    }

    public boolean isRatingEnabled() {
        return getBooleanPreference(RATING_ENABLED_PREF, true);
    }

    public void setRatingEnabled(boolean enabled) {
        setBooleanPreference(RATING_ENABLED_PREF, enabled);
    }

    public boolean isWifiSmsEnabled() {
        return getBooleanPreference(WIFI_SMS_PREF, false);
    }

    public int getRepeatAlertsCount() {
        try {
            return Integer.parseInt(getStringPreference(REPEAT_ALERTS_PREF, "0"));
        } catch (NumberFormatException e) {
            Log.w(TAG, e);
            return 0;
        }
    }

    public void setRepeatAlertsCount(int count) {
        setStringPreference(REPEAT_ALERTS_PREF, String.valueOf(count));
    }

    public boolean isSignedPreKeyRegistered() {
        return getBooleanPreference(SIGNED_PREKEY_REGISTERED_PREF, false);
    }

    public void setSignedPreKeyRegistered(boolean value) {
        setBooleanPreference(SIGNED_PREKEY_REGISTERED_PREF, value);
    }

    public void setGcmRegistrationId(Context context, String registrationId) {
        setStringPreference(GCM_REGISTRATION_ID_PREF, registrationId);
        setIntegerPrefrence(GCM_REGISTRATION_ID_VERSION_PREF, Utils.getCurrentApkReleaseVersion(context));
    }

    public String getGcmRegistrationId(Context context) {
        int storedRegistrationIdVersion = getIntegerPreference(GCM_REGISTRATION_ID_VERSION_PREF, 0);

        if (storedRegistrationIdVersion != Utils.getCurrentApkReleaseVersion(context)) {
            return null;
        } else {
            return getStringPreference(GCM_REGISTRATION_ID_PREF, null);
        }
    }

    public int getLocalRegistrationId() {
        return getIntegerPreference(LOCAL_REGISTRATION_ID_PREF, 0);
    }

    public void setLocalRegistrationId(int registrationId) {
        setIntegerPrefrence(LOCAL_REGISTRATION_ID_PREF, registrationId);
    }

    public long getDirectoryRefreshTime() {
        return getLongPreference(DIRECTORY_FRESH_TIME_PREF, 0L);
    }

    public void setDirectoryRefreshTime(long value) {
        setLongPreference(DIRECTORY_FRESH_TIME_PREF, value);
    }

    public String getPushServerPassword() {
        return getStringPreference(GCM_PASSWORD_PREF, null);
    }

    public void setPushServerPassword(String password) {
        setStringPreference(GCM_PASSWORD_PREF, password);
    }

    public boolean isEnterImeKeyEnabled() {
        return getBooleanPreference(ENTER_PRESENT_PREF, false);
    }

    public boolean isEnterSendsEnabled() {
        return getBooleanPreference(ENTER_SENDS_PREF, false);
    }

    public int getLastVersionCode() {
        return getIntegerPreference(LAST_VERSION_CODE_PREF, 0);
    }

    public void setLastVersionCode(int versionCode) throws IOException {
        if (!setIntegerPrefrenceBlocking(LAST_VERSION_CODE_PREF, versionCode)) {
            throw new IOException("couldn't write version code to sharedpreferences");
        }
    }

    public int getLastExperienceVersionCode() {
        return getIntegerPreference(LAST_EXPERIENCE_VERSION_PREF, 0);
    }

    public void setLastExperienceVersionCode(int versionCode) {
        setIntegerPrefrence(LAST_EXPERIENCE_VERSION_PREF, versionCode);
    }

    public boolean isPushRegistered() {
        return getBooleanPreference(REGISTERED_GCM_PREF, false);
    }

    public void setPushRegistered(boolean registered) {
        Log.w("TextSecurePreferences", "Setting push registered: " + registered);
        setBooleanPreference(REGISTERED_GCM_PREF, registered);
    }

    public boolean hasPromptedPushRegistration() {
        return getBooleanPreference(PROMPTED_PUSH_REGISTRATION_PREF, false);
    }

    public void setPromptedPushRegistration(boolean value) {
        setBooleanPreference(PROMPTED_PUSH_REGISTRATION_PREF, value);
    }

    public boolean hasPromptedShare() {
        return getBooleanPreference(PROMPTED_SHARE_PREF, false);
    }

    public void setPromptedShare(boolean value) {
        setBooleanPreference(PROMPTED_SHARE_PREF, value);
    }

    public boolean isNotificationsEnabled() {
        return getBooleanPreference(NOTIFICATION_PREF, true);
    }

    public String getNotificationRingtone() {
        return getStringPreference(RINGTONE_PREF, Settings.System.DEFAULT_NOTIFICATION_URI.toString());
    }

    public boolean isNotificationVibrateEnabled() {
        return getBooleanPreference(VIBRATE_PREF, true);
    }

    public String getNotificationLedColor() {
        return getStringPreference(LED_COLOR_PREF, "blue");
    }

    public String getNotificationLedPattern() {
        return getStringPreference(LED_BLINK_PREF, "500,2000");
    }

    public String getNotificationLedPatternCustom() {
        return getStringPreference(LED_BLINK_PREF_CUSTOM, "500,2000");
    }

    public void setNotificationLedPatternCustom(String pattern) {
        setStringPreference(LED_BLINK_PREF_CUSTOM, pattern);
    }

    public boolean isSystemEmojiPreferred() {
        return getBooleanPreference(SYSTEM_EMOJI_PREF, false);
    }

//    public @NonNull
//    Set<String> getMobileMediaDownloadAllowed() {
//        return getMediaDownloadAllowed(MEDIA_DOWNLOAD_MOBILE_PREF, R.array.pref_media_download_mobile_data_default);
//    }
//
//    public @NonNull Set<String> getWifiMediaDownloadAllowed() {
//        return getMediaDownloadAllowed(MEDIA_DOWNLOAD_WIFI_PREF, R.array.pref_media_download_wifi_default);
//    }
//
//    public @NonNull Set<String> getRoamingMediaDownloadAllowed() {
//        return getMediaDownloadAllowed(MEDIA_DOWNLOAD_ROAMING_PREF, R.array.pref_media_download_roaming_default);
//    }

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
