package ee.app.conversa.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import ee.app.conversa.search.FragmentPopular;
import ee.app.conversa.search.FragmentRecent;
import ee.app.conversa.search.FragmentTop;

/**
 * Created by edgargomez on 7/12/16.
 */
public class SearchPagerAdapter extends FragmentStatePagerAdapter {

    private String titles[];

    public SearchPagerAdapter(FragmentManager fm, String[] titles) {
        super(fm);
        this.titles = titles;
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment;

        switch (i) {
            case 0:
                fragment = new FragmentTop();
                break;
            case 1:
                fragment = new FragmentRecent();
                break;
            case 2:
                fragment = new FragmentPopular();
                break;
            default:
                fragment = new FragmentRecent();
                break;
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

}