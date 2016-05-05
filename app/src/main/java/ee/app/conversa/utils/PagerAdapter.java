/**
 * Fragment change was implemented using the following code:
 * 1. {@link http://stackoverflow.com/questions/18588944/replace-one-fragment-with-another-in-viewpager}
 * 2. {@link http://stackoverflow.com/questions/7992216/android-fragment-handle-back-button-press}
 */
package ee.app.conversa.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import ee.app.conversa.FragmentBusiness;
import ee.app.conversa.FragmentCategory;
//import ee.app.conversa.FragmentSettings;
import ee.app.conversa.FragmentUsersChat;

//Since this is an object collection, use a FragmentStatePagerAdapter,
// and NOT a FragmentPagerAdapter.
public class PagerAdapter extends FragmentStatePagerAdapter {

    public interface FirstPageFragmentListener {
        void onSwitchToNextFragment();
    }

    private final class CategoryListener implements PagerAdapter.FirstPageFragmentListener {
        public void onSwitchToNextFragment() {
            mFragmentManager.beginTransaction().remove(mFragmentAtPos0).commit();
            if (mFragmentAtPos0 instanceof FragmentCategory) {
                mFragmentAtPos0 = new FragmentBusiness(listener);
            } else {
                mFragmentAtPos0 = new FragmentCategory(listener);
            }
            notifyDataSetChanged();
        }
    }

    private final FragmentManager mFragmentManager;
    CategoryListener listener = new CategoryListener();

    public Fragment mFragmentAtPos0;
	private static final int NUM_PAGES = 3;

	public PagerAdapter(FragmentManager fm) {
        super(fm);
        mFragmentManager = fm;
    }

	@Override
    public Fragment getItem(int i) {
    	Fragment fragment;
    	
    	switch (i) {
	        case 0:
	        	fragment = new FragmentUsersChat();
	        	break;
	        case 1:
                if (mFragmentAtPos0 == null) {
                    mFragmentAtPos0 = new FragmentCategory(listener);
                }
                fragment =  mFragmentAtPos0;
	        	break;
	        case 2:
//	        	fragment = new FragmentSettings();
                fragment = new FragmentUsersChat();
	        	break;
            default:
                fragment = new FragmentUsersChat();
                break;
        }

        return fragment;
    }

    @Override
    public int getCount() { return NUM_PAGES; }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }

    @Override
    public int getItemPosition(Object object) {
        if (object instanceof FragmentCategory &&
                mFragmentAtPos0 instanceof FragmentBusiness) {
            return POSITION_NONE;
        }
        if (object instanceof FragmentBusiness &&
                mFragmentAtPos0 instanceof FragmentCategory) {
            return POSITION_NONE;
        }
        return super.getItemPosition(object);
    }

}