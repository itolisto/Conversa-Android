package ee.app.conversa.search;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;

import ee.app.conversa.adapters.BusinessAdapter;
import ee.app.conversa.model.parse.Business;

public class FragmentPopular extends Fragment implements BusinessAdapter.OnItemClickListener {

    private final String TAG = this.getClass().getSimpleName();

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
//        if(updateRecentById != 0) {
//            boolean change = false;
//            int iterator = 0;
//            for (dCustomer commerce : mUsers) {
//                if(commerce.getObjectId().equals(String.valueOf(updateRecentById))) {
//                    change = true;
//                    break;
//                }
//                iterator++;
//            }
//
//            if(change) {
//                //Update position
//                mBusinessAdapter.notifyItemMoved(iterator,0);
//                mUsers = ConversaApp.getDB().getAllContacts();
//                //Collections.sort(mUsers);
//                mBusinessAdapter.setItems(mUsers);
//                mBusinessAdapter.notifyItemChanged(0);
//            }
//
//            updateRecentById = 0;
//        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "onResume");
    }

    @Override
    public void onItemClick(View itemView, int position, Business business) {

    }

}