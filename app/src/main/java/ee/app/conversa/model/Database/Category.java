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

package ee.app.conversa.model.Database;

import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;

import ee.app.conversa.R;

/**
 * Category
 * 
 * Model class for categories.
 */

public class Category {

    private String mId;

	public Category() {}

	public Category(String id) {
		super();
		this.mId = id;
	}

    public String getmId() { return mId; }
    /* ******************************************************************************** */
	/* ******************************************************************************** */
    public String getmTitle(AppCompatActivity activity) {
        int id;
        try {
            id = Integer.valueOf(mId);
        } catch(NumberFormatException e) {
            return null;
        }

        String category = null;
        switch(id) {
            case 1:
                category =  activity.getString(R.string.category_1);
                break;
            case 2:
                category =  activity.getString(R.string.category_2);
                break;
            case 3:
                category =  activity.getString(R.string.category_3);
                break;
            case 4:
                category =  activity.getString(R.string.category_4);
                break;
            case 5:
                category =  activity.getString(R.string.category_5);
                break;
            case 6:
                category =  activity.getString(R.string.category_6);
                break;
            case 7:
                category =  activity.getString(R.string.category_7);
                break;
            case 8:
                category =  activity.getString(R.string.category_8);
                break;
            case 9:
                category =  activity.getString(R.string.category_9);
                break;
            case 10:
                category =  activity.getString(R.string.category_10);
                break;
            case 11:
                category =  activity.getString(R.string.category_11);
                break;
            case 12:
                category =  activity.getString(R.string.category_12);
                break;
            case 13:
                category =  activity.getString(R.string.category_13);
                break;
            case 14:
                category =  activity.getString(R.string.category_14);
                break;
            case 15:
                category =  activity.getString(R.string.category_15);
                break;
            case 16:
                category =  activity.getString(R.string.category_16);
                break;
            case 17:
                category =  activity.getString(R.string.category_17);
                break;
            case 18:
                category =  activity.getString(R.string.category_18);
                break;
            case 19:
                category =  activity.getString(R.string.category_19);
                break;
            case 20:
                category =  activity.getString(R.string.category_20);
                break;
            case 21:
                category =  activity.getString(R.string.category_21);
                break;
            case 22:
                category =  activity.getString(R.string.category_22);
                break;
            case 23:
                category =  activity.getString(R.string.category_23);
                break;
            case 24:
                category =  activity.getString(R.string.category_24);
                break;
        }

        return category;
    }
    /* ******************************************************************************** */
	/* ******************************************************************************** */
    public Drawable getDrawable(AppCompatActivity activity) {
        int id;
        try {
            id = Integer.valueOf(mId);
        } catch(NumberFormatException e) {
            return null;
        }

        Drawable category = null;
        switch(id) {
            case 1:
                category =  activity.getResources().getDrawable(R.drawable.category1);
                break;
            case 2:
                category =  activity.getResources().getDrawable(R.drawable.category2);
                break;
            case 3:
                category =  activity.getResources().getDrawable(R.drawable.category3);
                break;
            case 4:
                category =  activity.getResources().getDrawable(R.drawable.category4);
                break;
            case 5:
                category =  activity.getResources().getDrawable(R.drawable.category5);
                break;
            case 6:
                category =  activity.getResources().getDrawable(R.drawable.category6);
                break;
            case 7:
                category =  activity.getResources().getDrawable(R.drawable.category7);
                break;
            case 8:
                category =  activity.getResources().getDrawable(R.drawable.category8);
                break;
            case 9:
                category =  activity.getResources().getDrawable(R.drawable.category9);
                break;
            case 10:
                category =  activity.getResources().getDrawable(R.drawable.category10);
                break;
            case 11:
                category =  activity.getResources().getDrawable(R.drawable.category11);
                break;
            case 12:
                category =  activity.getResources().getDrawable(R.drawable.category12);
                break;
            case 13:
                category =  activity.getResources().getDrawable(R.drawable.category13);
                break;
            case 14:
                category =  activity.getResources().getDrawable(R.drawable.category14);
                break;
            case 15:
                category =  activity.getResources().getDrawable(R.drawable.category15);
                break;
            case 16:
                category =  activity.getResources().getDrawable(R.drawable.category16);
                break;
            case 17:
                category =  activity.getResources().getDrawable(R.drawable.category17);
                break;
            case 18:
                category =  activity.getResources().getDrawable(R.drawable.category18);
                break;
            case 19:
                category =  activity.getResources().getDrawable(R.drawable.category19);
                break;
            case 20:
                category =  activity.getResources().getDrawable(R.drawable.category20);
                break;
            case 21:
                category =  activity.getResources().getDrawable(R.drawable.category21);
                break;
            case 22:
                category =  activity.getResources().getDrawable(R.drawable.category22);
                break;
            case 23:
                category =  activity.getResources().getDrawable(R.drawable.category23);
                break;
            case 24:
                category =  activity.getResources().getDrawable(R.drawable.category24);
                break;
        }

        return category;
    }
}
