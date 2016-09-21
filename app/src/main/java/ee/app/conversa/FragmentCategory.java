/**
 * Search implementation: http://stackoverflow.com/questions/9556795/android-actionbar-search-widget-implementation-in-listfragment
 */

package ee.app.conversa;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ee.app.conversa.adapters.CategoryAdapter;
import ee.app.conversa.model.nCategory;
import ee.app.conversa.model.nHeaderTitle;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Logger;

public class FragmentCategory extends Fragment implements CategoryAdapter.OnItemClickListener {

    private RelativeLayout mRlCategoriesNoCategories;
    private RecyclerView mRvCategory;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CategoryAdapter mCategoryListAdapter;
    private AVLoadingIndicatorView mPbLoadingCategories;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_category, container, false);

        mRlCategoriesNoCategories = (RelativeLayout) rootView.findViewById(R.id.rlNoCategories);
        mRvCategory = (RecyclerView) rootView.findViewById(R.id.rvCategories);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.srlCategories);
        mPbLoadingCategories = (AVLoadingIndicatorView) rootView.findViewById(R.id.pbLoadingCategories);

        mCategoryListAdapter = new CategoryAdapter((ActivityMain)getActivity(), this);
        mRvCategory.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRvCategory.setAdapter(mCategoryListAdapter);
        mRvCategory.setHasFixedSize(true);
        mRvCategory.setItemAnimator(new DefaultItemAnimator());

        mSwipeRefreshLayout.setColorSchemeResources(R.color.green, R.color.orange, R.color.blue);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getCategoriesAsync();
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        getCategoriesAsync();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_category, menu);
        MenuItem searchItem = menu.findItem(R.id.grid_default_search);
        searchItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(getActivity(), ActivitySearch.class);
                startActivity(intent);
                return false;
            }
        });
    }

    private void getCategoriesAsync() {
        mSwipeRefreshLayout.setEnabled(false);
        mPbLoadingCategories.smoothToShow();

        // Call Parse for registry
        HashMap<String, Object> params = new HashMap<>(1);
        params.put("skip", 0);
        ParseCloud.callFunctionInBackground("getCategories", params, new FunctionCallback<String>() {
            @Override
            public void done(String result, ParseException e) {
                if(e == null) {
                    parseResult(result);
                } else {
                    parseResult("");
                }
            }
        });
    }

    @Override
    public void onItemClick(View itemView, int position, nCategory category) {
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
            String categoryName = category.getCategoryName(getActivity());
            b.putString(Const.kClassCategory, categoryName);
            ConversaApp.getInstance(getActivity()).getPreferences().setCurrentCategory(categoryName, true);
            fragment.setArguments(b);
            transaction.add(R.id.root_frame, fragment).hide(this);
            /*
             * IMPORTANT: The following lines allow us to add the fragment
             * to the stack and return to it later, by popBackStack
             */
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.addToBackStack("FragmentCategory");
            transaction.commit();
            ((ActivityMain) getActivity()).toggleTabLayoutVisibility();
        } else {
            Log.e("onItemClick", "Fragmento no se pudo reemplazar");
        }
    }

    private void parseResult(String result) {
        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }

        try {
            if (result.isEmpty()) {
                mRlCategoriesNoCategories.setVisibility(View.VISIBLE);
                mRvCategory.setVisibility(View.GONE);
            } else {
                JSONObject jsonRootObject = new JSONObject(result);
                JSONArray categories = jsonRootObject.optJSONArray("results");

                int size = categories.length();
                List<nCategory> categoryList = new ArrayList<>(30);
                List<nHeaderTitle> headerList = new ArrayList<>(2);

                for (int i = 0; i < size; i++) {
                    JSONObject jsonCategory = categories.getJSONObject(i);
                    String headerTitle = jsonCategory.optString("tn", "");

                    if (headerTitle.isEmpty()) {
                        String objectId = jsonCategory.optString("oj", "");
                        int relevance = jsonCategory.optInt("re", 0);
                        int position = jsonCategory.optInt("po", 0);
                        String avatarUrl = jsonCategory.optString("th", "");
                        categoryList.add(new nCategory(objectId, relevance, position, avatarUrl));
                    } else {
                        int relevance = jsonCategory.optInt("re", 0);
                        headerList.add(new nHeaderTitle(headerTitle, relevance));
                    }
                }

                mCategoryListAdapter.addItems(categoryList, headerList);
                mRlCategoriesNoCategories.setVisibility(View.GONE);
                mRvCategory.setVisibility(View.VISIBLE);
            }
        } catch (JSONException e) {
            Logger.error("parseResult", e.getMessage());
        } finally {
            mPbLoadingCategories.smoothToHide();
            mSwipeRefreshLayout.setEnabled(true);
        }
    }
}