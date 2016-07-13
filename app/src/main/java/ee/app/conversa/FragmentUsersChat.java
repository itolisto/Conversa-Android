package ee.app.conversa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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

import java.util.List;

import ee.app.conversa.adapters.ChatsAdapter;
import ee.app.conversa.interfaces.OnContactTaskCompleted;
import ee.app.conversa.model.Database.dBusiness;
import ee.app.conversa.response.ContactResponse;
import ee.app.conversa.services.NewMessageService;

public class FragmentUsersChat extends Fragment implements OnContactTaskCompleted {

    public static RecyclerView mRvUsers;
    public static RelativeLayout mRlNoUsers;
    public static ChatsAdapter mUserListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_users, container, false);

        mRvUsers = (RecyclerView) rootView.findViewById(R.id.lvUsers);
        mRlNoUsers = (RelativeLayout) rootView.findViewById(R.id.rlNoChats);

        mUserListAdapter = new ChatsAdapter((AppCompatActivity) getActivity());
        mRvUsers.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRvUsers.setItemAnimator(new DefaultItemAnimator());
        mRvUsers.setAdapter(mUserListAdapter);

        // Register Listener on Database
        ConversaApp.getDB().setContactListener(this);
        dBusiness.getAllContacts();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public void ContactGetAll(ContactResponse response) {
        List<dBusiness> contacts = response.getBusinesss();

        if(contacts.size() == 0) {
            mRlNoUsers.setVisibility(View.VISIBLE);
            mRvUsers.setVisibility(View.GONE);
        } else {
            if (mRlNoUsers.getVisibility() == View.VISIBLE) {
                mRlNoUsers.setVisibility(View.GONE);
                mRvUsers.setVisibility(View.VISIBLE);
            }

            mUserListAdapter.addItems(contacts);
        }
    }

    @Override
    public void ContactAdded(ContactResponse response) {
        ContactAddedFromBroadcast(response.getBusiness());
    }

    @Override
    public void ContactDeleted(ContactResponse response) {

    }

    @Override
    public void ContactUpdated(ContactResponse response) {

    }

    public void ContactAddedFromBroadcast(dBusiness business) {
        // 0. Check business is defined
        if (business == null)
            return;

        // 1. Check visibility
        if (mRlNoUsers.getVisibility() == View.VISIBLE) {
            mRlNoUsers.setVisibility(View.GONE);
            mRvUsers.setVisibility(View.VISIBLE);
        }

        // 2. Add contact to adapter
        mUserListAdapter.newContactInserted(business);
    }

    public class UsersReceiver extends BroadcastReceiver {
        public static final String ACTION_RESP =
                "conversa.fragmentuserschat.action.USER_SAVED";

        @Override
        public void onReceive(Context context, Intent intent) {
            dBusiness contact = intent.getParcelableExtra(NewMessageService.PARAM_OUT_MSG);
            ContactAddedFromBroadcast(contact);
        }
    }
}