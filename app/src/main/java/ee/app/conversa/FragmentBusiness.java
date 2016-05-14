package ee.app.conversa;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ee.app.conversa.adapters.BusinessAdapter;
import ee.app.conversa.adapters.BusinessSearchAdapter;
import ee.app.conversa.model.Parse.Business;
import ee.app.conversa.model.Parse.BusinessCategory;
import ee.app.conversa.model.Parse.bCategory;
import ee.app.conversa.utils.Const;

public class FragmentBusiness extends Fragment implements SearchView.OnQueryTextListener {

    private SearchView searchView;

    private RelativeLayout mRlSearch;
    private RelativeLayout mRlBusiness;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRvBusiness;
//    private RecyclerView mRvNoBusiness;
    private GridView     mRvBusinessSearch;

    private BusinessAdapter         mBusinessListAdapter;
    private BusinessSearchAdapter   mBusinessListAdapterSearch;

//    private ImageView mIvNoBusiness;
    private TextView mTvNoBusiness;
    private FragmentManager fragmentManager;

    private List<Object> mBusiness;
    private List<Object> mBusinessSearch;

    //private SearchView searchView;

    private int page;

    public FragmentBusiness() {}

    @SuppressWarnings("ValidFragment")
    public FragmentBusiness(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
        mBusiness           = new ArrayList<>();
        mBusinessSearch     = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_business_categories, container, false);

        mRlSearch       = (RelativeLayout) rootView.findViewById(R.id.rlSearchBusinessByCategory);
        mRlBusiness     = (RelativeLayout) rootView.findViewById(R.id.rlBody);

        /* Para negocios */
        mSwipeRefreshLayout = (SwipeRefreshLayout)  rootView.findViewById(R.id.srlBusiness);
        mRvBusiness         = (RecyclerView) rootView.findViewById(R.id.rvBusiness);
//        mRvNoBusiness       = (RecyclerView) rootView.findViewById(R.id.rvNoBusiness);
//        mIvNoBusiness       = (ImageView)    rootView.findViewById(R.id.tvNoBusiness);
        mBusinessListAdapter= new BusinessAdapter((AppCompatActivity) getActivity(), mBusiness);

        mSwipeRefreshLayout.setColorSchemeResources(R.color.green, R.color.orange, R.color.blue);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page = 0;
                getBusinessByCategoryAsync();
            }
        });
//        mRvNoBusiness.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRvBusiness.setLayoutManager(new LinearLayoutManager(getActivity()));

        mRvBusiness.setAdapter(mBusinessListAdapter);
//        mRvNoBusiness.setAdapter(new NoBusinessAdapter((AppCompatActivity) getActivity()));

        mRvBusiness.setItemAnimator(new DefaultItemAnimator());
        /* Para busqueda */
//        mRvBusinessSearch    = (GridView) rootView.findViewById(R.id.rvUsersSearchBusinessByCategory);
        mTvNoBusiness        = (TextView) rootView.findViewById(R.id.tvSearchBusinessByCategoryEmpty);
        mBusinessListAdapterSearch = new BusinessSearchAdapter((AppCompatActivity) getActivity(), mBusinessSearch);
        //mRvBusinessSearch.setAdapter(mBusinessListAdapterSearch);

        page = 0;
        getBusinessByCategoryAsync();

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_users, menu);
//        searchView = (SearchView) menu.findItem(R.id.grid_default_search).getActionView();
        MenuItem searchItem = menu.findItem(R.id.grid_default_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint(getActivity().getString(R.string.search_category_hint));
        searchView.setQuery("", false);
        searchView.clearFocus();
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                ConversaApp.getPreferences().setCurrentCategoryTitle("");
                ((ActivityMain) getActivity()).getSupportActionBar().setTitle(getActivity().getString(R.string.categories));
                ((ActivityMain) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                //firstPageListener.onSwitchToNextFragment();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Toast.makeText(getActivity(), "Searching for: " + query, Toast.LENGTH_SHORT).show();
//            getBusinessByIdAsync(query);
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
//                mRvBusinessSearch.setVisibility(View.GONE);
            mBusinessSearch.clear();
            mRlSearch.setVisibility(View.GONE);
            mRlBusiness.setVisibility(View.VISIBLE);
            //Utils.hideKeyboard((AppCompatActivity) getActivity());
        }
        return false;
    }

    private void getBusinessByCategoryAsync() {
        String categoryId = ConversaApp.getPreferences().getCurrentCategory();

        if(categoryId.isEmpty()) {
            mBusiness.clear();
//            mRvNoBusiness.setVisibility(View.VISIBLE);//mIvNoBusiness.setVisibility(View.VISIBLE);
            mRvBusiness.setVisibility(View.GONE);
        } else {
            ParseQuery<BusinessCategory> query = ParseQuery.getQuery(BusinessCategory.class);
            Collection<String> collection = new ArrayList<>();
            collection.add(Const.kBusinessCategoryBusinessKey);
            query.selectKeys(collection);
            String cat = Const.kBusinessCategoryBusinessKey.concat(".").concat(Const.kBusinessBusinessInfoKey);
            query.include(cat);
            query.whereEqualTo(Const.kBusinessCategoryCategoryKey, ParseObject.createWithoutData(bCategory.class, categoryId));
            query.whereEqualTo(Const.kBusinessCategoryActiveKey, true);

            ParseQuery<Business> param1 = ParseQuery.getQuery(Business.class);
            param1.whereEqualTo(Const.kBusinessActiveKey, true);
            param1.whereEqualTo(Const.kBusinessCountryKey, ParseObject.createWithoutData("Country", "QZ31UNerIj"));
            param1.whereDoesNotExist(Const.kBusinessBusinessKey);

            query.whereMatchesKeyInQuery(Const.kBusinessCategoryBusinessKey, Const.kObjectRowObjectIdKey, param1);
            query.orderByAscending(Const.kBusinessCategoryRelevanceKey);
            query.addAscendingOrder(Const.kBusinessCategoryPositionKey);
            query.setLimit(15);
            query.setSkip(page * 15);

            query.findInBackground(new FindCallback<BusinessCategory>() {

                @Override
                public void done(List<BusinessCategory> objects, ParseException e) {
                    boolean reloadAll = mSwipeRefreshLayout.isRefreshing();

                    if (reloadAll) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                    if (e == null && objects != null) {
                        if (reloadAll) {
                            mBusiness.clear();
                            mBusiness.addAll(objects);
                            mBusinessListAdapter.setItems(mBusiness);
                        } else {
                            boolean add = (objects.size() == 15);
                            mBusiness.addAll(objects);
                            mBusinessListAdapter.addItems(mBusiness, add);
                        }

                        page++;
                    }
                }
            });
        }
    }

//    private class GetBusinessByCategoryFinish implements ResultListener<List<dBusiness>> {
//        @Override
//        public void onResultsSuccess(List<dBusiness> result) {
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
//            Collections.sort(mBusiness, new Comparator<dBusiness>() {
//                @Override
//                public int compare(dBusiness lhs, dBusiness rhs) {
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
//    private class SearchBusinessFinish implements ResultListener<List<dBusiness>> {
//        @Override
//        public void onResultsSuccess(List<dBusiness> result) {
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
//            Collections.sort(mBusinessSearch, new Comparator<dBusiness>() {
//                @Override
//                public int compare(dBusiness lhs, dBusiness rhs) {
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

}