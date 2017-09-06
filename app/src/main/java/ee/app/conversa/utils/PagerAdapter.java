/**
 * Fragment change was implemented using the following code:
 * 1. {@link http://stackoverflow.com/questions/18588944/replace-one-fragment-with-another-in-viewpager}
 * 2. {@link http://stackoverflow.com/questions/7992216/android-fragment-handle-back-button-press}
 */
package ee.app.conversa.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import ee.app.conversa.FragmentPreferences;
import ee.app.conversa.FragmentRoot;
import ee.app.conversa.FragmentUsersChat;

public class PagerAdapter extends SmartFragmentStatePagerAdapter<Fragment> {

	public PagerAdapter(FragmentManager fm) {
        super(fm);
    }

	@Override
    public Fragment getItem(int i) {
    	Fragment fragment;
    	
    	switch (i) {
	        case 0:
	        	fragment = new FragmentUsersChat();
	        	break;
	        case 1:
                fragment = new FragmentRoot();
	        	break;
            case 2:
                fragment = new FragmentPreferences();
                break;
            default:
                fragment = new FragmentUsersChat();
                break;
        }

        return fragment;
    }

    @Override
    public int getCount() { return 3; }

    @Override
    public CharSequence getPageTitle(int position) { return null; }

}