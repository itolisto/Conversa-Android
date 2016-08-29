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

import java.util.List;

import ee.app.conversa.adapters.ChatsAdapter;
import ee.app.conversa.dialog.CustomDeleteUserDialog;
import ee.app.conversa.interfaces.OnContactTaskCompleted;
import ee.app.conversa.model.database.dBusiness;
import ee.app.conversa.notifications.onesignal.CustomNotificationExtenderService;
import ee.app.conversa.utils.Const;

public class FragmentUsersChat extends Fragment implements OnContactTaskCompleted, ChatsAdapter.OnItemClickListener, ChatsAdapter.OnLongClickListener {

    public static RecyclerView mRvUsers;
    public static RelativeLayout mRlNoUsers;
    public static ChatsAdapter mUserListAdapter;
    protected UsersReceiver receiver = new UsersReceiver();
    private final IntentFilter mUserFilter = new IntentFilter(UsersReceiver.ACTION_RESP);

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_users, container, false);

        mRvUsers = (RecyclerView) rootView.findViewById(R.id.lvUsers);
        mRlNoUsers = (RelativeLayout) rootView.findViewById(R.id.rlNoChats);

        mUserListAdapter = new ChatsAdapter((AppCompatActivity) getActivity(), this, this);
        mRvUsers.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRvUsers.setItemAnimator(new DefaultItemAnimator());
        mRvUsers.setAdapter(mUserListAdapter);

        // Register Listener on Database
        ConversaApp.getDB().setContactListener(this);
        dBusiness.getAllContacts(getContext());

        // Register receiver
        ConversaApp.getLocalBroadcastManager().registerReceiver(receiver, mUserFilter);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ConversaApp.getLocalBroadcastManager().unregisterReceiver(receiver);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public void ContactGetAll(final List<dBusiness> contacts) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
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
            });
        }
    }

    @Override
    public void ContactAdded(dBusiness response) {
        ContactAddedFromBroadcast(response);
    }

    @Override
    public void ContactDeleted(final dBusiness response) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 1. Get visible items and first visible item position
                    int visibleItemCount = mRvUsers.getChildCount();
                    int firstVisibleItem = ((LinearLayoutManager) mRvUsers.getLayoutManager()).findFirstVisibleItemPosition();
                    // 2. Update message
                    mUserListAdapter.removeContact(response, firstVisibleItem, visibleItemCount);
                    // 3. Check visibility
                    if (mUserListAdapter.getItemCount() == 0) {
                        mRlNoUsers.setVisibility(View.VISIBLE);
                        mRvUsers.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    @Override
    public void ContactUpdated(dBusiness response) {

    }

    public void ContactAddedFromBroadcast(final dBusiness business) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
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
            });
        }
    }

    public class UsersReceiver extends BroadcastReceiver {
        public static final String ACTION_RESP =
                "conversa.fragmentuserschat.action.USER_SAVED";

        @Override
        public void onReceive(Context context, Intent intent) {
            dBusiness contact = intent.getParcelableExtra(CustomNotificationExtenderService.PARAM_OUT_MSG);
            ContactAddedFromBroadcast(contact);
        }
    }

    @Override
    public void onItemClick(dBusiness contact) {
        Intent intent = new Intent(getActivity(), ActivityChatWall.class);
        intent.putExtra(Const.kClassBusiness, contact);
        intent.putExtra(Const.kYapDatabaseName, false);
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(final dBusiness contact) {
        final CustomDeleteUserDialog dialog = new CustomDeleteUserDialog(getContext());
        dialog.setTitle("Test")
                .setMessage("Test Test Test Test Test Test Test Test Test Test Test Test Test Test Test Test Test")
                //.dismissOnTouchOutside(false)
                .setupPositiveButton("Accept", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //contact.removeContact();
                        dialog.dismiss();
                    }
                })
                .setupNegativeButton("Decline", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                }
                });
        dialog.show();
    }
}