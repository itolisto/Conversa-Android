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
    private static final String USER_ID = "user_id";
    private static final String USER_NAME = "name";
	private static final String USER_EMAIL = "email";
    private static final String USER_TOKEN = "user_token";
	private static final String BUS_LOCATION = "business_location";
    private static final String CURRENT_CATEGORY = "current_category";
    private static final String CURRENT_CATEGORY_TITLE = "current_category_title";
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
	public String getUserEmail() { return sharedPreferences.getString(USER_EMAIL, ""); }
    public String getUserName() { return sharedPreferences.getString(USER_NAME, ""); }
	public String getUserToken() { return sharedPreferences.getString(USER_TOKEN, ""); }
    public String getUserId() { return sharedPreferences.getString(USER_ID, ""); }
    public int getBusLocation() { return sharedPreferences.getInt(BUS_LOCATION, 0); }
    public String getCurrentCategory() { return sharedPreferences.getString(CURRENT_CATEGORY, ""); }
    public String getCurrentCategoryTitle() { return sharedPreferences.getString(CURRENT_CATEGORY_TITLE, ""); }
    public boolean isFirstCallForUsers() { return sharedPreferences.getBoolean(FIRST_USER_SERVER_CALL, true); }

    public SharedPreferences getSharedPreferences(){ return sharedPreferences; }

    public boolean cleanSharedPreferences(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        return editor.commit();
    }
    /* ******************************************************************************** */
	/* ************************************ SETTERS *********************************** */
	/* ******************************************************************************** */
	
	public void setUserEmail(String email) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(USER_EMAIL, email);
		editor.commit();
	}

    public void setUserName(String name) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USER_NAME, name);
        editor.commit();
    }
	
	public void setUserToken(String token) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(USER_TOKEN, token);
		editor.commit();
	}
	
    public void setUserId(String id) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USER_ID, id);
        editor.commit();
    }

    public void setCurrentCategory (String category) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CURRENT_CATEGORY, category);
        editor.commit();
    }

    public void setCurrentCategoryTitle (String category) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CURRENT_CATEGORY_TITLE, category);
        editor.commit();
    }

    public void setIsFirstCallForUsers(boolean isFirst) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(FIRST_USER_SERVER_CALL, isFirst);
        editor.commit();
    }

    public void setBusLocation(int busLocation) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(BUS_LOCATION, busLocation);
        editor.commit();
    }
}
