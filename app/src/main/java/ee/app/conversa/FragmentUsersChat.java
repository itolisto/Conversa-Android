package ee.app.conversa;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

import ee.app.conversa.actions.ContactAction;
import ee.app.conversa.actions.MessageAction;
import ee.app.conversa.adapters.ChatsAdapter;
import ee.app.conversa.contact.ContactIntentService;
import ee.app.conversa.contact.ContactUpdateReason;
import ee.app.conversa.extendables.ConversaFragment;
import ee.app.conversa.interfaces.OnContactClickListener;
import ee.app.conversa.interfaces.OnContactLongClickListener;
import ee.app.conversa.messaging.MessageDeleteReason;
import ee.app.conversa.messaging.MessageIntentService;
import ee.app.conversa.messaging.MessageUpdateReason;
import ee.app.conversa.model.database.dbBusiness;
import ee.app.conversa.model.database.dbMessage;
import ee.app.conversa.utils.Const;

public class FragmentUsersChat extends ConversaFragment implements OnContactClickListener,
        OnContactLongClickListener, View.OnClickListener, ActionMode.Callback {

    private RecyclerView mRvUsers;
    private LinearLayout mRlNoUsers;
    private ChatsAdapter mUserListAdapter;
    private boolean refresh;
    private ActionMode actionMode;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_users, container, false);

        mRvUsers = (RecyclerView) rootView.findViewById(R.id.lvUsers);
        mRlNoUsers = (LinearLayout) rootView.findViewById(R.id.rlNoChats);

        mUserListAdapter = new ChatsAdapter((AppCompatActivity) getActivity(), this, this);
        mRvUsers.setHasFixedSize(true);
        mRvUsers.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRvUsers.setItemAnimator(new DefaultItemAnimator());
        mRvUsers.setAdapter(mUserListAdapter);

        refresh = false;

        rootView.findViewById(R.id.btnStartBrowsing).setOnClickListener(this);

        unregisterListener = false;

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        dbBusiness.getAllContacts(getContext());
    }

    @Override
    public void ContactGetAll(final List<dbBusiness> contacts) {
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

            mUserListAdapter.setItems(contacts);
        }
    }

    @Override
    public void ContactAdded(dbBusiness response) {
        // 0. Check business is defined
        if (response == null)
            return;

        // 1. Check visibility
        if (mRlNoUsers.getVisibility() == View.VISIBLE) {
            mRlNoUsers.setVisibility(View.GONE);
            mRvUsers.setVisibility(View.VISIBLE);
        }

        // 2. Add contact to adapter
        mUserListAdapter.addContact(response);
    }

    @Override
    public void ContactDeleted(List<String> contacts) {
        // 1. Update chats
        mUserListAdapter.removeContacts();

        if (actionMode != null) {
            actionMode.finish();
        }

        // 2. Check visibility
        if (mUserListAdapter.getItemCount() == 0) {
            mRlNoUsers.setVisibility(View.VISIBLE);
            mRvUsers.setVisibility(View.GONE);
        }
    }

    @Override
    public void ContactUpdated(dbBusiness response, ContactUpdateReason reason) {

    }

    @Override
    public void MessageReceived(dbMessage response) {
        mUserListAdapter.updateContactPosition(response.getFromUserId());
    }

    @Override
    public void MessageDeleted(List<String> response, MessageDeleteReason reason) {
        switch (reason) {
            case ALL: {
                mUserListAdapter.updateContactLastMessage(response.get(0));
                break;
            }
            case MULTIPLE: {
                break;
            }
            case SINGLE: {
                break;
            }
        }
    }

    @Override
    public void MessageSent(dbMessage response) {
        mUserListAdapter.updateContactPosition(response.getToUserId());
    }

    @Override
    public void MessageUpdated(dbMessage response, MessageUpdateReason reason) {
        if (reason == MessageUpdateReason.VIEW) {
            mUserListAdapter.updateContactView(response.getFromUserId());
        }
    }

    @Override
    public void onContactClick(dbBusiness contact, View v, int position) {
        if (actionMode == null) {
            Intent intent = new Intent(getActivity(), ActivityChatWall.class);
            intent.putExtra(Const.iExtraBusiness, contact);
            intent.putExtra(Const.iExtraAddBusiness, false);
            intent.putExtra(Const.iExtraPosition, position);
            startActivity(intent);
        } else {
            myToggleSelection(position);
        }
    }

    @Override
    public void onContactLongClick(dbBusiness contact, View v, int position) {
        if (actionMode == null) {
            initToggleSelection(contact, position);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnStartBrowsing: {
                ((ActivityMain) getActivity()).selectViewPagerTab(1);
                break;
            }
        }
    }

    private void initToggleSelection(final dbBusiness contact, final int position) {
        final FragmentUsersChat context = this;
        final AppCompatActivity activity = (AppCompatActivity)getActivity();

        final MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .customView(R.layout.dialog_user_options, true)
                .autoDismiss(true)
                .build();

        RelativeLayout select = (RelativeLayout) dialog.getCustomView().findViewById(R.id.rlDialogUserSelect);
        RelativeLayout clear = (RelativeLayout) dialog.getCustomView().findViewById(R.id.rlDialogUserClear);
        RelativeLayout delete = (RelativeLayout) dialog.getCustomView().findViewById(R.id.rlDialogUserDelete);

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                // Select more users
                // 1. First add/remove the position to the selected items list
                mUserListAdapter.toggleSelection(position);
                // 2. Check selected items list count
                boolean hasCheckedItems = mUserListAdapter.getSelectedItemCount() > 0;

                if (hasCheckedItems && actionMode == null) {
                    getActivity().startActionMode(context);
                } else if (!hasCheckedItems && actionMode != null) {
                    actionMode.finish();
                }

                if (actionMode != null) {
                    actionMode.setTitle(getString(R.string.selected_count, mUserListAdapter.getSelectedItemCount()));
                }
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent broadcastIntent = new Intent(activity, MessageIntentService.class);
                broadcastIntent.putExtra(MessageIntentService.INTENT_EXTRA_ACTION_CODE, MessageAction.ACTION_MESSAGE_DELETE_ALL);
                broadcastIntent.putExtra(MessageIntentService.INTENT_EXTRA_CONTACT_ID, contact.getBusinessId());
                activity.startService(broadcastIntent);
                dialog.dismiss();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUserListAdapter.toggleSelection(position);
                ArrayList<String> items = new ArrayList<>(1);
                items.add(Long.toString(contact.getId()));
                // Delete user
                Intent intent = new Intent(getActivity(), ContactIntentService.class);
                intent.putExtra(ContactIntentService.INTENT_EXTRA_ACTION_CODE, ContactAction.ACTION_CONTACT_DELETE);
                intent.putStringArrayListExtra(
                        ContactIntentService.INTENT_EXTRA_CUSTOMER_LIST,
                        items);
                getActivity().startService(intent);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void myToggleSelection(int position) {
        // 1. First add/remove the position to the selected items list
        mUserListAdapter.toggleSelection(position);
        // 2. Check selected items list count
        boolean hasCheckedItems = mUserListAdapter.getSelectedItemCount() > 0;

        if (hasCheckedItems && actionMode == null) {
            getActivity().startActionMode(this);
        } else if (!hasCheckedItems && actionMode != null) {
            actionMode.finish();
        }

        if (actionMode != null) {
            actionMode.setTitle(getString(R.string.selected_count, mUserListAdapter.getSelectedItemCount()));
        }
    }

    public void finishActionMode() {
        if (actionMode != null) {
            actionMode.finish();
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        actionMode = mode;
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.menu_items_selected, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_delete:
                Intent intent = new Intent(getActivity(), ContactIntentService.class);
                intent.putExtra(ContactIntentService.INTENT_EXTRA_ACTION_CODE, ContactAction.ACTION_CONTACT_DELETE);
                intent.putStringArrayListExtra(ContactIntentService.INTENT_EXTRA_CUSTOMER_LIST,
                        mUserListAdapter.getSelectedItems());
                getActivity().startService(intent);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        if (actionMode != null) {
            actionMode = null;
        }

        mUserListAdapter.clearSelections();
    }

}