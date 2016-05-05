package ee.app.conversa;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ee.app.conversa.adapters.BusinessAdapter;
import ee.app.conversa.adapters.BusinessSearchAdapter;
import ee.app.conversa.adapters.NoBusinessAdapter;
import ee.app.conversa.model.Database.Business;
import ee.app.conversa.utils.PagerAdapter;

public class FragmentBusiness extends Fragment {

    private RelativeLayout mRlSearch;
    private RelativeLayout mRlBusiness;

    //private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRvBusiness;
    private RecyclerView mRvNoBusiness;
    private GridView     mRvBusinessSearch;

    private BusinessAdapter         mBusinessListAdapter;
    private BusinessSearchAdapter   mBusinessListAdapterSearch;

    //private ImageView mIvNoBusiness;
    private TextView mTvNoBusiness;
    private PagerAdapter.FirstPageFragmentListener firstPageListener;

    private List<Business> mBusiness;
    private List<Business> mBusinessSearch;

    private SearchView searchView;

    public FragmentBusiness() {}

    @SuppressWarnings("ValidFragment")
    public FragmentBusiness(PagerAdapter.FirstPageFragmentListener listener) {
        firstPageListener   = listener;
        mBusiness           = new ArrayList<>();
        mBusinessSearch     = new ArrayList<>();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(searchView != null) {
            searchView.setQuery("", false);
            searchView.setIconified(true);
        }
    }

    final private SearchView.OnQueryTextListener queryListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            Toast.makeText(getActivity(), "Searching for: " + query, Toast.LENGTH_SHORT).show();
            getBusinessByIdAsync(query);
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            if(mRlSearch.getVisibility() == View.GONE) {
                mRlSearch.setVisibility(View.VISIBLE);
                mRlBusiness.setVisibility(View.GONE);
            }

            if(newText.isEmpty()) {
                mTvNoBusiness.setVisibility(View.VISIBLE);
                mRvBusinessSearch.setVisibility(View.GONE);
                mBusinessSearch.clear();
                mRlSearch.setVisibility(View.GONE);
                mRlBusiness.setVisibility(View.VISIBLE);
                hideKeyboard((AppCompatActivity) getActivity());
            }
            return false;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_business_categories, container, false);

        mRlSearch       = (RelativeLayout) rootView.findViewById(R.id.rlSearchBusinessByCategory);
        mRlBusiness     = (RelativeLayout) rootView.findViewById(R.id.rlBody);

        /* Para negocios */
        //mSwipeRefreshLayout = (SwipeRefreshLayout)  rootView.findViewById(R.id.srlBusiness);
        mRvBusiness         = (RecyclerView) rootView.findViewById(R.id.rvBusiness);
        mRvNoBusiness       = (RecyclerView) rootView.findViewById(R.id.rvNoBusiness);
//        mIvNoBusiness       = (ImageView)    rootView.findViewById(R.id.tvNoBusiness);
        mBusinessListAdapter= new BusinessAdapter((AppCompatActivity) getActivity(), mBusiness);

        //mSwipeRefreshLayout.setColorSchemeResources(R.color.green, R.color.orange, R.color.blue);
//        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                getBusinessByCategoryAsync();
//            }
//        });
        mRvNoBusiness.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRvBusiness.setLayoutManager(new LinearLayoutManager(getActivity()));

        mRvBusiness.setAdapter(mBusinessListAdapter);
        mRvNoBusiness.setAdapter(new NoBusinessAdapter((AppCompatActivity) getActivity()));

        mRvBusiness.setItemAnimator(new DefaultItemAnimator());
        /* Para busqueda */
        mRvBusinessSearch    = (GridView) rootView.findViewById(R.id.rvUsersSearchBusinessByCategory);
        mTvNoBusiness        = (TextView) rootView.findViewById(R.id.tvSearchBusinessByCategoryEmpty);
        mBusinessListAdapterSearch = new BusinessSearchAdapter((AppCompatActivity) getActivity(), mBusinessSearch);

        mRvBusinessSearch.setAdapter(mBusinessListAdapterSearch);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        this.getView().setFocusableInTouchMode(true);
        this.getView().requestFocus();
        this.getView().setOnKeyListener( new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if( keyCode == KeyEvent.KEYCODE_BACK ) {
                    ConversaApp.getPreferences().setCurrentCategoryTitle("");
                    ((ActivityMain) getActivity()).getSupportActionBar().setTitle(getActivity().getString(R.string.categories));
                    ((ActivityMain) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    firstPageListener.onSwitchToNextFragment();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        getBusinessByCategoryAsync();
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_users, menu);
        searchView = (SearchView) menu.findItem(R.id.grid_default_search).getActionView();
        searchView.setOnQueryTextListener(queryListener);
        searchView.setQueryHint(getActivity().getString(R.string.search_category_hint));
        searchView.setQuery("", false);
        searchView.clearFocus();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                ConversaApp.getPreferences().setCurrentCategoryTitle("");
                ((ActivityMain) getActivity()).getSupportActionBar().setTitle(getActivity().getString(R.string.categories));
                ((ActivityMain) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                firstPageListener.onSwitchToNextFragment();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getBusinessByCategoryAsync() {
        String categoryId = ConversaApp.getPreferences().getCurrentCategory();

        if(categoryId.isEmpty() || categoryId.equals("")) {
            mRvNoBusiness.setVisibility(View.VISIBLE);//mIvNoBusiness.setVisibility(View.VISIBLE);
            mRvBusiness.setVisibility(View.GONE);
        } else {
//            CouchDB.findAllBusinessByCategory( categoryId,
//                    new GetBusinessByCategoryFinish(), getActivity(), true
//            );
        }
    }

    private void getBusinessByIdAsync(String name) {
        String categoryId = ConversaApp.getPreferences().getCurrentCategory();

        if(!categoryId.isEmpty()) {
//            CouchDB.searchBusinessAsync(name, categoryId,
//                    new SearchBusinessFinish(), getActivity(), true
//            );
        }
    }

//    private class GetBusinessByCategoryFinish implements ResultListener<List<Business>> {
//        @Override
//        public void onResultsSuccess(List<Business> result) {
//
////            if(mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
////                mSwipeRefreshLayout.setRefreshing(false);
//                if(mBusinessListAdapter != null)
//                    mBusinessListAdapter.notifyDataSetChanged();
////            }
//
//            mBusiness = result;
//
//            if(mBusiness == null || mBusiness.size() == 0) {
//                mRvNoBusiness.setVisibility(View.VISIBLE);//mIvNoBusiness.setVisibility(View.VISIBLE);//mIvNoBusiness
//                mRvBusiness.setVisibility(View.GONE);
//                //mSwipeRefreshLayout.setEnabled(false);
//                return;
//            } else {
//                //mSwipeRefreshLayout.setEnabled(true);
//                mRvNoBusiness.setVisibility(View.GONE);//mIvNoBusiness.setVisibility(View.GONE);//mIvNoBusiness
//                mRvBusiness.setVisibility(View.VISIBLE);
//            }
//
//            // sorting users by name
//            Collections.sort(mBusiness, new Comparator<Business>() {
//                @Override
//                public int compare(Business lhs, Business rhs) {
//                    return lhs.getmName().compareToIgnoreCase(rhs.getmName());
//                }
//            });
//
//            mBusinessListAdapter.clearFavBusiness();
//            mRvBusiness.setAdapter(mBusinessListAdapter);
//            mBusinessListAdapter.setItems(mBusiness);
//
//        }
//
//        @Override
//        public void onResultsFail() {
//
////            if(mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
////                mSwipeRefreshLayout.setRefreshing(false);
//                if(mBusinessListAdapter != null)
//                    mBusinessListAdapter.notifyDataSetChanged();
////            }
//
//            if(mBusiness == null || mBusiness.size() == 0) {
//                mRvNoBusiness.setVisibility(View.VISIBLE);//mIvNoBusiness.setVisibility(View.VISIBLE);//mIvNoBusiness
//                mRvBusiness.setVisibility(View.GONE);
//                //mSwipeRefreshLayout.setEnabled(false);
//            } else {
//                //mSwipeRefreshLayout.setEnabled(true);
//                mRvNoBusiness.setVisibility(View.GONE);//mIvNoBusiness.setVisibility(View.GONE);//mIvNoBusiness
//                mRvBusiness.setVisibility(View.VISIBLE);
//            }
//        }
//    }
//
//    private class SearchBusinessFinish implements ResultListener<List<Business>> {
//        @Override
//        public void onResultsSuccess(List<Business> result) {
//
//            mBusinessSearch = result;
//
//            if(mBusinessSearch == null || mBusinessSearch.size() == 0) {
//                mTvNoBusiness.setVisibility(View.VISIBLE);
//                mRvBusinessSearch.setVisibility(View.GONE);
//                return;
//            } else {
//                mTvNoBusiness.setVisibility(View.GONE);
//                mRvBusinessSearch.setVisibility(View.VISIBLE);
//            }
//
//            // sorting users by name
//            Collections.sort(mBusinessSearch, new Comparator<Business>() {
//                @Override
//                public int compare(Business lhs, Business rhs) {
//                    return lhs.getmName().compareToIgnoreCase(rhs.getmName());
//                }
//            });
//
//            mBusinessListAdapterSearch.clearFavBusiness();
//            mBusinessListAdapterSearch.setItems(mBusinessSearch);
//            mBusinessListAdapterSearch.notifyDataSetChanged();
//        }
//
//        @Override
//        public void onResultsFail() {
//            if(mBusinessSearch == null || mBusinessSearch.size() == 0) {
//                mTvNoBusiness.setVisibility(View.VISIBLE);
//                mRvBusinessSearch.setVisibility(View.GONE);
//            } else {
//                mTvNoBusiness.setVisibility(View.GONE);
//                mRvBusinessSearch.setVisibility(View.VISIBLE);
//            }
//        }
//    }

    //  Gets any view which has focus
    public void hideKeyboard(AppCompatActivity activity) {
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE);
        View cur_focus = activity.getCurrentFocus();
        if (cur_focus != null) {
            inputMethodManager.hideSoftInputFromWindow(cur_focus.getWindowToken(), 0);
        }
    }
}