package ee.app.conversa;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import ee.app.conversa.events.RefreshEvent;
import ee.app.conversa.extendables.ConversaFragment;
import ee.app.conversa.model.database.dBusiness;
import ee.app.conversa.utils.Const;
import ee.app.conversa.view.RegularTextView;

public class FragmentUsersChat extends ConversaFragment implements ChatsAdapter.OnItemClickListener,
        ChatsAdapter.OnLongClickListener, View.OnClickListener {

    private RecyclerView mRvUsers;
    private RelativeLayout mRlNoUsers;
    private ChatsAdapter mUserListAdapter;
    private boolean refresh;

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
        refresh = false;

        RegularTextView mRtvStartBrowsing = (RegularTextView) rootView.findViewById(R.id.rtvStartBrowsing);
        mRtvStartBrowsing.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        dBusiness.getAllContacts(getContext());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    protected void refresh(RefreshEvent event) {
        if (event.isRefresh()) {
            refresh = true;
            dBusiness.getAllContacts(getContext());
        }
    }

    @Override
    public void ContactGetAll(final List<dBusiness> contacts) {
        if (refresh) {
            mUserListAdapter.clearItems();
            refresh = false;
        }

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
    public void ContactAdded(dBusiness response) {
        // 0. Check business is defined
        if (response == null)
            return;

        // 1. Check visibility
        if (mRlNoUsers.getVisibility() == View.VISIBLE) {
            mRlNoUsers.setVisibility(View.GONE);
            mRvUsers.setVisibility(View.VISIBLE);
        }

        // 2. Add contact to adapter
        mUserListAdapter.newContactInserted(response);
    }

    @Override
    public void ContactDeleted(final dBusiness response) {
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

    @Override
    public void ContactUpdated(dBusiness response) {

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
                        contact.removeContact(getContext());
                        dialog.dismiss();
                    }
                })
                .setupNegativeButton("Decline", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                }
                });
        dialog.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rtvStartBrowsing:
                ((ActivityMain)getActivity()).selectViewPagerTab(1);
                break;
        }
    }
}