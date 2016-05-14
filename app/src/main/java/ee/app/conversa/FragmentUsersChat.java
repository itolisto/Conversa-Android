package ee.app.conversa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
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
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import ee.app.conversa.adapters.ChatsAdapter;
import ee.app.conversa.extendables.ConversaActivity;
import ee.app.conversa.model.Database.dBusiness;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Logger;

public class FragmentUsersChat extends Fragment {

    private LongOperation getUsersAsynTask;
	public static RecyclerView mLvUsers;
	public static ImageView mIvNoUsers;
	public static List<dBusiness> mUsers;
	public static ChatsAdapter mUserListAdapter;
//    public static int updateRecentById;
    public static boolean updateListAdapter;
    private final IntentFilter mPushFilter = new IntentFilter(ConversaActivity.PUSH);

    public FragmentUsersChat() {
        mUsers = new ArrayList<>();
        getUsersAsynTask = new LongOperation();
    }

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_users, container, false);

        mLvUsers   = (RecyclerView) rootView.findViewById(R.id.lvUsers);
        mIvNoUsers = (ImageView) rootView.findViewById(R.id.tvNoUsers);

        mUserListAdapter = new ChatsAdapter((AppCompatActivity) getActivity(), mUsers);
        mLvUsers.setLayoutManager(new LinearLayoutManager(getActivity()));
        mLvUsers.setItemAnimator(new DefaultItemAnimator());
        mLvUsers.setAdapter(mUserListAdapter);

        getUserContactsAsync();

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

        if (mUsers.size() == 0) {
            mIvNoUsers.setVisibility(View.VISIBLE);
            mLvUsers.setVisibility(View.GONE);
        } else {
            mIvNoUsers.setVisibility(View.GONE);
            mLvUsers.setVisibility(View.VISIBLE);
            if(updateListAdapter && mUserListAdapter != null) {
                mUserListAdapter.notifyDataSetChanged();
                updateListAdapter = false;
            }
        }

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
        if (!getUsersAsynTask.isCancelled()) {
            getUsersAsynTask.cancel(true);
        }
        ConversaApp.getLocalBroadcastManager().unregisterReceiver(mPushReceiver);
    }

    private BroadcastReceiver mPushReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            handlePushNotification(intent);
        }
    };

    private void getUserContactsAsync () {
        getUsersAsynTask.execute();
    }

    private class LongOperation extends AsyncTask<String, Void, List<dBusiness>> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected List<dBusiness> doInBackground(String... params) {
            if(isCancelled()){
                return null;
            }

            return ConversaApp.getDB().getAllContacts();
        }

        @Override
        protected void onProgressUpdate(Void... values) {

        }

        @Override
        protected void onPostExecute(List<dBusiness> result) {
            mUsers = result;

            if(mUsers.size() == 0) {
                mIvNoUsers.setVisibility(View.VISIBLE);
                mLvUsers.setVisibility(View.GONE);
            } else {
                mIvNoUsers.setVisibility(View.GONE);
                mLvUsers.setVisibility(View.VISIBLE);
                mUserListAdapter.setItems(mUsers);
            }
        }

        @Override
        protected void onCancelled(List<dBusiness> businesses) {
            // Task was cancelled before completed. This method will always get
            // called from doInBackground when a task is cancelled by user
            Logger.error("FragmentUsersChat", "getUserContactsAsync was cancelled");
        }
    }

    private void handlePushNotification(Intent intent) {
        String id = intent.getStringExtra(Const.ID);
        if (id != null) {
            boolean change = false;
            int iterator = 0;
            for (dBusiness commerce : mUsers) {
                if(commerce.getObjectId().equals(id)) {
                    change = true;
                    break;
                }
                iterator++;
            }

            if(change) {
                //Update position
                mUserListAdapter.notifyItemMoved(iterator,0);
                //mUsers = ConversaApp.getDB().getAllContacts();
                // Cambiar de posicion dentro de arreglo
                dBusiness tempNew = mUsers.get(iterator);
                mUsers.remove(iterator);
                mUsers.add(0, tempNew);
                // Actualizar lista en Adaptador
                mUserListAdapter.setItems(mUsers);
                // Notificar cambio estructural
                mUserListAdapter.notifyItemMoved(iterator,0);
            }
        }
    }

}