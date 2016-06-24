/**
 * Search implementation: http://stackoverflow.com/questions/9556795/android-actionbar-search-widget-implementation-in-listfragment
 */

package ee.app.conversa;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ee.app.conversa.utils.Logger;

public class FragmentRoot extends Fragment {

//    private SearchView searchView;

    public FragmentRoot() {}

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_root, container, false);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
		/*
		 * When this container fragment is created, we fill it with our first
		 * "real" fragment
		 */
        if(ConversaApp.getPreferences().getCurrentCategory().isEmpty()) {
            transaction.replace(R.id.root_frame, new FragmentCategory(getFragmentManager()));
        } else {
            transaction.replace(R.id.root_frame, new FragmentBusiness(getFragmentManager()));
        }

        transaction.commit();

        return rootView;
    }

    public void backPressed() {
        ConversaApp.getPreferences().setCurrentCategoryTitle("");

        try {
            ((ActivityMain) getActivity()).getSupportActionBar().setTitle(getActivity().getString(R.string.categories));
            ((ActivityMain) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            FragmentTransaction trans = getFragmentManager().beginTransaction();
            trans.replace(R.id.root_frame, new FragmentCategory());
            trans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
            trans.addToBackStack(null);
            trans.commit();
        }catch (NullPointerException e) {
            Logger.error("", e.getMessage());
        }
        //firstPageListener.onSwitchToNextFragment();
    }

}