/**
 * Search implementation: http://stackoverflow.com/questions/9556795/android-actionbar-search-widget-implementation-in-listfragment
 */

package ee.app.conversa;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import ee.app.conversa.adapters.BusinessSearchAdapter;
import ee.app.conversa.adapters.CategoryAdapter;
import ee.app.conversa.decorations.SimpleDividerItemDecoration;
import ee.app.conversa.model.Parse.bCategory;
import ee.app.conversa.utils.PagerAdapter;
import ee.app.conversa.utils.Utils;

public class FragmentCategory extends Fragment implements PagerAdapter.FirstPageFragmentListener{

    private RelativeLayout mRlSearch;
    private RelativeLayout mRlCategories;

    private RecyclerView mRvCategory;
    private GridView     mRvBusiness;

    private BusinessSearchAdapter   mBusinessListAdapter;
    private CategoryAdapter         mCategoryListAdapter;

    private PagerAdapter.FirstPageFragmentListener firstPageListener;
    private TextView mTvNoBusiness;

    private List<bCategory> mCategories;
    private List<Object> mBusiness;

    private SearchView searchView;

    public FragmentCategory()            {}
    @Override
    public void onSwitchToNextFragment() {}

    @SuppressWarnings("ValidFragment")
    public FragmentCategory(PagerAdapter.FirstPageFragmentListener listener) {
        firstPageListener   = listener;
        mBusiness           = new ArrayList<>();
        mCategories         = new ArrayList<>();
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
                mRlCategories.setVisibility(View.GONE);
            }

            if(newText.isEmpty()) {
                mTvNoBusiness.setVisibility(View.VISIBLE);
                mRvBusiness.setVisibility(View.GONE);
                mBusiness.clear();
                mRlSearch.setVisibility(View.GONE);
                mRlCategories.setVisibility(View.VISIBLE);
                Utils.hideKeyboard((AppCompatActivity) getActivity());
            }
            return false;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_category, container, false);

        mRlSearch       = (RelativeLayout) rootView.findViewById(R.id.rlSearchCategories);
        mRlCategories   = (RelativeLayout) rootView.findViewById(R.id.rlCategories);

        /* Para categorias */
        mRvCategory          = (RecyclerView)       rootView.findViewById(R.id.lvCategories);
        mCategoryListAdapter = new CategoryAdapter((AppCompatActivity) getActivity(), mCategories, firstPageListener);
        mRvCategory.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRvCategory.setAdapter(mCategoryListAdapter);
        mRvCategory.setItemAnimator(new DefaultItemAnimator());
        mRvCategory.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

        /* Para busqueda */
        mRvBusiness          = (GridView) rootView.findViewById(R.id.rvUsersSearch);
        mTvNoBusiness        = (TextView) rootView.findViewById(R.id.tvSearchEmpty);
        mBusinessListAdapter = new BusinessSearchAdapter((AppCompatActivity) getActivity(), mBusiness);
        mRvBusiness.setAdapter(mBusinessListAdapter);

        ParseQuery<bCategory> query = ParseQuery.getQuery(bCategory.class);

        query.orderByAscending("relevance");
        query.addAscendingOrder("createdAt");

        if(ConversaApp.getPreferences().getCategoriesLoad()) {
            query.fromLocalDatastore();
        }

        query.findInBackground(new FindCallback<bCategory>() {

            @Override
            public void done(List<bCategory> objects, ParseException e) {
                if (e == null) {
                    // Save to local datastore and change future searches
                    // to be direct to local datastore
                    if(!ConversaApp.getPreferences().getCategoriesLoad()) {
                        bCategory.pinAllInBackground(objects);
                        ConversaApp.getPreferences().setCategoriesLoad(true);
                    }
                    // Add to adapter and refresh
                    mCategories.addAll(objects);
                    mCategoryListAdapter.setItems(mCategories);
                }
            }
        });

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
        searchView = (SearchView) menu.findItem(R.id.grid_default_search).getActionView();
        searchView.setOnQueryTextListener(queryListener);
        searchView.setQueryHint(getActivity().getString(R.string.search_hint));
        searchView.setQuery("", false);
        searchView.clearFocus();
    }

    private void getBusinessByIdAsync(String name) {
//        CouchDB.searchBusinessByIdAsync(name,
//                new SearchBusinessFinish(), getActivity(), true
//        );
    }

//    private class GetCategoriesFinish implements ResultListener<List<Category>> {
//        @Override
//        public void onResultsSuccess(List<Category> result) {
//
//            if(mCategoryListAdapter != null)
//                mCategoryListAdapter.notifyDataSetChanged();
//
//            mCategories = result;
//
//            // sorting users by name
//            Collections.sort(mCategories, new Comparator<Category>() {
//                @Override
//                public int compare(Category lhs, Category rhs) {
//                    return lhs.getmTitle((AppCompatActivity)getActivity()).
//                            compareToIgnoreCase(rhs.getmTitle((AppCompatActivity)getActivity()));
//                }
//            });
//
//            mRvCategory.setAdapter(mCategoryListAdapter);
//            mCategoryListAdapter.setItems(mCategories);
//        }
//
//        @Override
//        public void onResultsFail() {
//            if(mCategoryListAdapter != null)
//                mCategoryListAdapter.notifyDataSetChanged();
//        }
//    }
//
//    private class SearchBusinessFinish implements ResultListener<List<dBusiness>> {
//        @Override
//        public void onResultsSuccess(List<dBusiness> result) {
//
//            mBusiness = result;
//
//            if(mBusiness == null || mBusiness.size() == 0) {
//                mTvNoBusiness.setVisibility(View.VISIBLE);
//                mRvBusiness.setVisibility(View.GONE);
//                return;
//            } else {
//                mTvNoBusiness.setVisibility(View.GONE);
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
//            mBusinessListAdapter.setItems(mBusiness);
//            mBusinessListAdapter.notifyDataSetChanged();
//        }
//
//        @Override
//        public void onResultsFail() {
//            if(mBusiness == null || mBusiness.size() == 0) {
//                mTvNoBusiness.setVisibility(View.VISIBLE);
//                mRvBusiness.setVisibility(View.GONE);
//            } else {
//                mTvNoBusiness.setVisibility(View.GONE);
//                mRvBusiness.setVisibility(View.VISIBLE);
//            }
//        }
//    }

}