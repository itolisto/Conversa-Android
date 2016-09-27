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
import android.widget.RelativeLayout;

import java.util.List;

import ee.app.conversa.adapters.ChatsAdapter;
import ee.app.conversa.events.RefreshEvent;
import ee.app.conversa.extendables.ConversaFragment;
import ee.app.conversa.management.contact.ContactIntentService;
import ee.app.conversa.model.database.dbBusiness;
import ee.app.conversa.settings.ActivityPreferences;
import ee.app.conversa.utils.Const;
import ee.app.conversa.view.RegularTextView;

public class FragmentUsersChat extends ConversaFragment implements ChatsAdapter.OnItemClickListener,
        ChatsAdapter.OnLongClickListener, View.OnClickListener, ActionMode.Callback {

    private RecyclerView mRvUsers;
    private RelativeLayout mRlNoUsers;
    private ChatsAdapter mUserListAdapter;
    private boolean refresh;
    private ActionMode actionMode;
//    private ImageFetcher mImageFetcher;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_users, container, false);

        mRvUsers = (RecyclerView) rootView.findViewById(R.id.lvUsers);
        mRlNoUsers = (RelativeLayout) rootView.findViewById(R.id.rlNoChats);
//        mImageFetcher = ConversaApp.getInstance(getContext()).getImageFetcher();
//        ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(getContext(), ConversaApp.IMAGE_CACHE_DIR);
//        cacheParams.setMemCacheSizePercent(0.20f); // Set memory cache to 25% of app memory
//        mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);

        mUserListAdapter = new ChatsAdapter((AppCompatActivity) getActivity(), this, this);
        mRvUsers.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRvUsers.setItemAnimator(new DefaultItemAnimator());
        mRvUsers.setAdapter(mUserListAdapter);
        mRvUsers.setHasFixedSize(true);
//        mRvUsers.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
//                    // Before Honeycomb pause image loading on scroll to help with performance
//                    if (!Utils.hasHoneycomb()) {
//                        mImageFetcher.setPauseWork(true);
//                    }
//                } else {
//                    mImageFetcher.setPauseWork(false);
//                }
//            }
//        });

        refresh = false;

        RegularTextView mRtvStartBrowsing = (RegularTextView) rootView.findViewById(R.id.rtvStartBrowsing);
        mRtvStartBrowsing.setOnClickListener(this);

        return rootView;
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        mImageFetcher.setExitTasksEarly(false);
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        mImageFetcher.setPauseWork(false);
//        mImageFetcher.setExitTasksEarly(true);
//        mImageFetcher.flushCache();
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        mImageFetcher.closeCache();
//    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        dbBusiness.getAllContacts(getContext());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent preferencesIntent = new Intent(getActivity(), ActivityPreferences.class);
            startActivity(preferencesIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void refresh(RefreshEvent event) {
        if (event.isRefresh()) {
            refresh = true;
            dbBusiness.getAllContacts(getContext());
        }
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

            mUserListAdapter.addItems(contacts);
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
        mUserListAdapter.newContactInserted(response);
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
    public void ContactUpdated(dbBusiness response) {

    }

    @Override
    public void onItemClick(dbBusiness contact, int position) {
        if (actionMode == null) {
            Intent intent = new Intent(getActivity(), ActivityChatWall.class);
            intent.putExtra(Const.kClassBusiness, contact);
            intent.putExtra(Const.kYapDatabaseName, false);
            startActivity(intent);
        } else {
            myToggleSelection(position);
        }
    }

    @Override
    public void onItemLongClick(final dbBusiness contact, int position) {
        if (actionMode == null) {
            myToggleSelection(position);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rtvStartBrowsing:
                ((ActivityMain)getActivity()).selectViewPagerTab(1);
                break;
        }
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
                intent.putExtra(ContactIntentService.INTENT_EXTRA_ACTION_CODE, ContactIntentService.ACTION_MESSAGE_DELETE);
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

        mUserListAdapter.clearSelections(true);
    }

}