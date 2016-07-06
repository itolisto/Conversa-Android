/**
 * Search implementation: http://stackoverflow.com/questions/9556795/android-actionbar-search-widget-implementation-in-listfragment
 */

package ee.app.conversa;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Collection;
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
    private RelativeLayout mRlCategoriesNoCategories;
    private RecyclerView mRvCategory;
    private GridView mRvBusiness;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private BusinessSearchAdapter mBusinessListAdapter;
    private CategoryAdapter mCategoryListAdapter;
    private ProgressBar mPbLoadingCategories;

    private TextView mTvNoBusiness;

    private List<Object> mBusiness;

    public FragmentCategory() {
        mBusiness = new ArrayList<>();
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
        mPbLoadingCategories = (ProgressBar) rootView.findViewById(R.id.pbLoadingCategories);
        mTvNoBusiness = (TextView) rootView.findViewById(R.id.tvSearchEmpty);

        mCategoryListAdapter = new CategoryAdapter(getActivity(), this);
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

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        getCategoriesAsync();
    }

    private void getCategoriesAsync() {
        mSwipeRefreshLayout.setEnabled(false);
        mPbLoadingCategories.setVisibility(View.VISIBLE);

        ParseQuery<bCategory> query = ParseQuery.getQuery(bCategory.class);
        query.orderByDescending(Const.kCategoryRelevance);
        query.addDescendingOrder(Const.kCategoryPosition);
        query.setLimit(30);

        Collection<String> collection = new ArrayList<>();
        collection.add(Const.kCategoryThumbnail);
        collection.add(Const.kCategoryRelevance);
        collection.add(Const.kCategoryPosition);
        query.selectKeys(collection);

        if(ConversaApp.getPreferences().getCategoriesLoad()) {
//            query.fromLocalDatastore();
        }

        query.findInBackground(new FindCallback<bCategory>() {

            @Override
            public void done(List<bCategory> objects, ParseException e) {
                if (mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }

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
                        mPbLoadingCategories.setVisibility(View.GONE);
                    }
                }

                mSwipeRefreshLayout.setEnabled(true);
            }
        });
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_users, menu);
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
        //Toast.makeText(getActivity(), "Searching for: " + query, Toast.LENGTH_SHORT).show();
//        getBusinessByIdAsync(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Toast.makeText(getActivity(), "Searching for: " + newText, Toast.LENGTH_SHORT).show();
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
        FragmentManager fm = getFragmentManager();

        if (fm != null) {
            FragmentTransaction transaction = fm.beginTransaction();
            /*
             * When this container fragment is created, we fill it with our first
             * "real" fragment
             */
            FragmentBusiness fragment = new FragmentBusiness();
            Bundle b = new Bundle();
            b.putString(Const.kObjectRowObjectIdKey, category.getObjectId());
            b.putString(Const.kClassCategory, category.getCategoryName(getActivity()));
            fragment.setArguments(b);
            transaction.add(R.id.root_frame, fragment);
            /*
             * IMPORTANT: The following lines allow us to add the fragment
             * to the stack and return to it later, by popBackStack
             */
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.addToBackStack("FragmentCategory");
            transaction.commit();
        } else {
            Log.e("onItemClick", "Fragmento no se pudo reemplazar");
        }
    }
}