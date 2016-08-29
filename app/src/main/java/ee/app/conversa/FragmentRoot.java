/**
 * Search implementation: http://stackoverflow.com/questions/9556795/android-actionbar-search-widget-implementation-in-listfragment
 */

package ee.app.conversa;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentRoot extends Fragment {

    public FragmentRoot() {}

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_root, container, false);
        FragmentManager fm = getFragmentManager();

        if (fm != null) {
            FragmentTransaction transaction = fm.beginTransaction();
            /*
             * When this container fragment is created, we fill it with our first
             * "real" fragment
             */
            transaction.replace(R.id.root_frame, new FragmentCategory());
            transaction.commit();
        } else {
            Log.e("toggleFragment", "fm is null");
        }

        return rootView;
    }

}