/**
 * Search implementation: http://stackoverflow.com/questions/9556795/android-actionbar-search-widget-implementation-in-listfragment
 */

package ee.app.conversa;

import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import ee.app.conversa.adapters.BusinessSearchAdapter;
import ee.app.conversa.adapters.CategoryAdapter;
import ee.app.conversa.decorations.SimpleDividerItemDecoration;
import ee.app.conversa.extendables.ConversaFragment;
import ee.app.conversa.model.Parse.bCategory;
import ee.app.conversa.utils.Const;

public class FragmentCategory extends ConversaFragment implements SearchView.OnQueryTextListener, CategoryAdapter.OnItemClickListener {

    private SearchView searchView;

    private RelativeLayout mRlSearchCategories;
//    private RelativeLayout mRlCategories;
    private RelativeLayout mRlCategoriesNoCategories;

    private RecyclerView mRvCategory;
    private GridView     mRvBusiness;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private BusinessSearchAdapter   mBusinessListAdapter;
    private CategoryAdapter         mCategoryListAdapter;

    private FragmentManager fragmentManager;
    private TextView mTvNoBusiness;

    private List<Object> mBusiness;

    private boolean isLoading;

    public FragmentCategory(){}
//    @Override
//    public void onSwitchToNextFragment() {}

    @SuppressWarnings("ValidFragment")
    public FragmentCategory(FragmentManager fragmentManager) {
        mBusiness           = new ArrayList<>();
        this.fragmentManager = fragmentManager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_category, container, false);

        mRlSearchCategories = (RelativeLayout) rootView.findViewById(R.id.rlSearchCategories);
        //mRlCategories = (RelativeLayout) rootView.findViewById(R.id.rlCategories);
        mRlCategoriesNoCategories = (RelativeLayout) rootView.findViewById(R.id.rlNoCategories);
        mRvCategory = (RecyclerView) rootView.findViewById(R.id.rvCategories);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.srlCategories);
        mRvBusiness = (GridView) rootView.findViewById(R.id.rvUsersSearch);
        mTvNoBusiness = (TextView) rootView.findViewById(R.id.tvSearchEmpty);

        mCategoryListAdapter = new CategoryAdapter(this);//, firstPageListener);
        mRvCategory.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRvCategory.setAdapter(mCategoryListAdapter);
        mRvCategory.setHasFixedSize(true);
        mRvCategory.setItemAnimator(new DefaultItemAnimator());
        mRvCategory.addItemDecoration(new SimpleDividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));

        mSwipeRefreshLayout.setColorSchemeResources(R.color.green, R.color.orange, R.color.blue);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                bCategory.unpinAllInBackground();
                ConversaApp.getPreferences().setCategoriesLoad(false, false);
                getCategoriesAsync();
            }
        });

        /* Para busqueda */
        mBusinessListAdapter = new BusinessSearchAdapter((AppCompatActivity) getActivity(), mBusiness);
        mRvBusiness.setAdapter(mBusinessListAdapter);

        getCategoriesAsync();

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private void getCategoriesAsync() {
        isLoading = true;
        mSwipeRefreshLayout.setEnabled(false);

        ParseQuery<bCategory> query = ParseQuery.getQuery(bCategory.class);

        query.orderByAscending(Const.kCategoryRelevance);
        query.addAscendingOrder(Const.kCategoryPosition);
        query.setLimit(30);

        if(ConversaApp.getPreferences().getCategoriesLoad()) {
            query.fromLocalDatastore();
        }

        query.findInBackground(new FindCallback<bCategory>() {

            @Override
            public void done(List<bCategory> objects, ParseException e) {
                mSwipeRefreshLayout.setRefreshing(false);
                mSwipeRefreshLayout.setEnabled(true);

                if (e == null && objects != null) {
                    // Save to local datastore and change future searches
                    // to be direct to local datastore
                    if(!ConversaApp.getPreferences().getCategoriesLoad()) {
                        bCategory.pinAllInBackground(objects);
                        ConversaApp.getPreferences().setCategoriesLoad(true, true);
                    }

                    // Add to adapter and refresh
                    if(objects.size() > 0) {
                        mCategoryListAdapter.setItems(objects);
                        mRlCategoriesNoCategories.setVisibility(View.GONE);
                        mRvCategory.setVisibility(View.VISIBLE);
                    } else {
                        mRlCategoriesNoCategories.setVisibility(View.VISIBLE);
                        mRvCategory.setVisibility(View.GONE);
                    }
                }
            }
        });
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
    public boolean onQueryTextSubmit(String query) {
        // Text has changed, apply filtering?
        Toast.makeText(getActivity(), "Searching for: " + query, Toast.LENGTH_SHORT).show();
//        getBusinessByIdAsync(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // Perform the final search
//        if(mRlSearchCategories.getVisibility() == View.GONE) {
//            mRlSearchCategories.setVisibility(View.VISIBLE);
//            mRlCategories.setVisibility(View.GONE);
//        }
//
//        if(newText.isEmpty()) {
//            mTvNoBusiness.setVisibility(View.VISIBLE);
//            mRvBusiness.setVisibility(View.GONE);
//            mBusiness.clear();
//            mRlSearchCategories.setVisibility(View.GONE);
//            mRlCategories.setVisibility(View.VISIBLE);
//            //Utils.hideKeyboard((AppCompatActivity) getActivity());
//        }
        return true;
    }

    @Override
    public void onItemClick(View itemView, int position, bCategory category) {
        Toast.makeText(getActivity(), "Category: " + category + "\nNombre: " + category.getName(), Toast.LENGTH_SHORT).show();
//        ConversaApp.getPreferences().setCurrentCategory(category.getObjectId());
        ConversaApp.getPreferences().setCurrentCategoryTitle(category.getName());
//        mActivity.getSupportActionBar().setTitle(category.getName());
//        mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//
//        FragmentTransaction trans = mActivity.getFragmentManager().beginTransaction();
//        FragmentTransaction transaction = fragment.beginTransaction();
//
//        /*
//         * IMPORTANT: We use the "root frame" defined in
//         * "root_fragment.xml" as the reference to replace fragment
//         */
//        transaction.replace(R.id.root_frame, new FragmentBusiness(fragment));
//
//        /*
//         * IMPORTANT: The following lines allow us to add the fragment
//         * to the stack and return to it later, by pressing back
//         */
//        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//        transaction.addToBackStack(null);
//        transaction.commit();
    }
}