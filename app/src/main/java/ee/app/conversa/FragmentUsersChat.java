package ee.app.conversa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import ee.app.conversa.adapters.ChatsAdapter;
import ee.app.conversa.extendables.ConversaActivity;
import ee.app.conversa.interfaces.OnContactTaskCompleted;
import ee.app.conversa.model.Database.dBusiness;
import ee.app.conversa.responses.ContactResponse;
import ee.app.conversa.utils.Const;

public class FragmentUsersChat extends Fragment implements OnContactTaskCompleted {

	public static RecyclerView mLvUsers;
	public static RelativeLayout mRlNoUsers;
	public static ChatsAdapter mUserListAdapter;
//    public static int updateRecentById;
    public static boolean updateListAdapter;
    private final IntentFilter mPushFilter = new IntentFilter(ConversaActivity.PUSH);

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_users, container, false);

        mLvUsers = (RecyclerView) rootView.findViewById(R.id.lvUsers);
        mRlNoUsers = (RelativeLayout) rootView.findViewById(R.id.rlNoChats);

        mUserListAdapter = new ChatsAdapter((AppCompatActivity) getActivity());
        mLvUsers.setLayoutManager(new LinearLayoutManager(getActivity()));
        mLvUsers.setItemAnimator(new DefaultItemAnimator());
        mLvUsers.setAdapter(mUserListAdapter);

        dBusiness.getAllContacts(this);

        return rootView;
	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public void onResume() {
        super.onResume();

//        if (mUsers.size() == 0) {
//            mIvNoUsers.setVisibility(View.VISIBLE);
//            mLvUsers.setVisibility(View.GONE);
//        } else {
//            mIvNoUsers.setVisibility(View.GONE);
//            mLvUsers.setVisibility(View.VISIBLE);
//            if(updateListAdapter && mUserListAdapter != null) {
//                mUserListAdapter.notifyDataSetChanged();
//                updateListAdapter = false;
//            }
//        }

//        if(updateRecentById != 0) {
//            boolean change = false;
//            int iterator = 0;
//            for (dBusiness commerce : mUsers) {
//                if(commerce.getObjectId().equals(String.valueOf(updateRecentById))) {
//                    change = true;
//                    break;
//                }
//                iterator++;
//            }
//
//            if(change) {
//                //Update position
//                mUserListAdapter.notifyItemMoved(iterator,0);
//                mUsers = ConversaApp.getDB().getAllContacts();
//                //Collections.sort(mUsers);
//                mUserListAdapter.setItems(mUsers);
//                mUserListAdapter.notifyItemChanged(0);
//            }
//
//            updateRecentById = 0;
//        }

        ConversaApp.getLocalBroadcastManager().registerReceiver(
                mPushReceiver, mPushFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        ConversaApp.getLocalBroadcastManager().unregisterReceiver(mPushReceiver);
    }

    private BroadcastReceiver mPushReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            handlePushNotification(intent);
        }
    };

    @Override
    public void OnContactTaskCompleted(ContactResponse response) {
        if(response.getResponse().size() == 0) {
            mRlNoUsers.setVisibility(View.VISIBLE);
            mLvUsers.setVisibility(View.GONE);
        } else {
            mRlNoUsers.setVisibility(View.GONE);
            mLvUsers.setVisibility(View.VISIBLE);
            mUserListAdapter.setItems(response.getResponse());
        }
    }

    private void handlePushNotification(Intent intent) {
        String id = intent.getStringExtra(Const.ID);
//        if (id != null) {
//            boolean change = false;
//            int iterator = 0;
//            for (dBusiness commerce : mUsers) {
//                if(commerce.getObjectId().equals(id)) {
//                    change = true;
//                    break;
//                }
//                iterator++;
//            }
//
//            if(change) {
//                //Update position
//                mUserListAdapter.notifyItemMoved(iterator,0);
//                //mUsers = ConversaApp.getDB().getAllContacts();
//                // Cambiar de posicion dentro de arreglo
//                dBusiness tempNew = mUsers.get(iterator);
//                mUsers.remove(iterator);
//                mUsers.add(0, tempNew);
//                // Actualizar lista en Adaptador
//                mUserListAdapter.setItems(mUsers);
//                // Notificar cambio estructural
//                mUserListAdapter.notifyItemMoved(iterator,0);
//            }
//        }
    }
}