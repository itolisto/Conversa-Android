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

/**
 * Preferences
 *
 * Holds and managed application's preferences.
 */

public class Preferences {

    // Defining SharedPreferences entries
    private static final String CATEGORIES_LOAD = "CATEGORIES_LOAD";
    private static final String FIRST_USER_SERVER_CALL = "first_users_server_call";

    private SharedPreferences sharedPreferences;

    /**
     * Gets a SharedPreferences instance that points to the default file that is
     * used by the preference framework in the given context.
     */
    public Preferences(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /* ******************************************************************************** */
	/* ************************************ GETTERS *********************************** */
	/* ******************************************************************************** */
    public boolean getCategoriesLoad() {
        return sharedPreferences.getBoolean(CATEGORIES_LOAD, false);
    }

    public boolean isFirstCallForUsers() {
        return sharedPreferences.getBoolean(FIRST_USER_SERVER_CALL, true);
    }

    public boolean cleanSharedPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        return editor.commit();
    }
    /* ******************************************************************************** */
	/* ************************************ SETTERS *********************************** */
	/* ******************************************************************************** */
    public void setCategoriesLoad(boolean value, boolean inBackground) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(CATEGORIES_LOAD, value);
        if (inBackground) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    public void setIsFirstCallForUsers(boolean isFirst) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(FIRST_USER_SERVER_CALL, isFirst);
        editor.apply();
    }

}
