/**
 * Search implementation: http://stackoverflow.com/questions/9556795/android-actionbar-search-widget-implementation-in-listfragment
 */

package ee.app.conversa;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import ee.app.conversa.adapters.CategoryAdapter;
import ee.app.conversa.extendables.BaseActivity;
import ee.app.conversa.interfaces.OnCategoryClickListener;
import ee.app.conversa.model.nCategory;
import ee.app.conversa.model.nHeaderTitle;
import ee.app.conversa.utils.AppActions;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Logger;

public class FragmentCategory extends Fragment implements OnCategoryClickListener, View.OnClickListener {

    private RelativeLayout mRlNoConnection;
    private RecyclerView mRvCategory;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CategoryAdapter mCategoryListAdapter;
    private AVLoadingIndicatorView mPbLoadingCategories;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_category, container, false);

        mRlNoConnection = (RelativeLayout) rootView.findViewById(R.id.rlNoConnection);
        mRvCategory = (RecyclerView) rootView.findViewById(R.id.rvCategories);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.srlCategories);
        mPbLoadingCategories = (AVLoadingIndicatorView) rootView.findViewById(R.id.pbLoadingCategories);
        Button mBtnRetry = (Button) rootView.findViewById(R.id.btnRetryResult);

        mBtnRetry.setOnClickListener(this);

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
        getCategoriesAsync();
    }

    private void getCategoriesAsync() {
        if (((BaseActivity)getActivity()).hasInternetConnection()) {
            mSwipeRefreshLayout.setEnabled(false);
            mRvCategory.setVisibility(View.GONE);
            mRlNoConnection.setVisibility(View.GONE);
            mPbLoadingCategories.smoothToShow();

            String language = ConversaApp.getInstance(getActivity()).getPreferences().getLanguage();

            if (language.equals("zz")) {
                if (Locale.getDefault().getLanguage().startsWith("es")) {
                    language = "es";
                } else {
                    language = "en";
                }
            }

            // Call Parse for registry
            HashMap<String, Object> params = new HashMap<>(1);
            params.put("language", language);
            ParseCloud.callFunctionInBackground("getCategories", params, new FunctionCallback<String>() {
                @Override
                public void done(String result, ParseException e) {
                    if (e == null) {
                        parseResult(result, true);
                    } else {
                        AppActions.validateParseException(getActivity(), e);
                        parseResult("", true);
                    }
                }
            });
        } else {
            if (mRlNoConnection.getVisibility() == View.GONE) {
                mSwipeRefreshLayout.setEnabled(false);
                parseResult("", false);
            }
        }
    }

    @Override
    public void onCategoryClick(nCategory category, View itemView, int position) {
        Intent intent = new Intent(getContext(), ActivityBusiness.class);
        intent.putExtra(Const.kObjectRowObjectIdKey, category.getObjectId());
        String categoryName = category.getCategoryName(getActivity());
        intent.putExtra(Const.kClassCategory, categoryName);
        startActivity(intent);
    }

    private void parseResult(String result, boolean connected) {
        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }

        try {
            if (result.isEmpty()) {
                mRvCategory.setVisibility(View.GONE);
            } else {
                JSONObject jsonRootObject = new JSONObject(result);
                JSONArray categories = jsonRootObject.optJSONArray("results");

                int size = categories.length();
                List<nCategory> alphabetically = null;
                List<Object> categoriesList = new ArrayList<>(30);

                for (int i = 0; i < size; i++) {
                    JSONObject jsonCategory = categories.getJSONObject(i);
                    String headerTitle = jsonCategory.optString("tn", null);

                    if (headerTitle != null) {
                        int relevance = jsonCategory.optInt("re", 0);
                        categoriesList.add(new nHeaderTitle(headerTitle, relevance));

                        if (jsonCategory.optBoolean("al", false)) {
                            alphabetically = new ArrayList<>(1);
                        }
                    } else {
                        nCategory category = new nCategory(
                                jsonCategory.optString("ob", ""),
                                jsonCategory.optString("th", ""));

                        if (alphabetically != null) {
                            alphabetically.add(category);

                            if (i + 1 < size) {
                                jsonCategory = categories.getJSONObject(i + 1);

                                if (jsonCategory.optString("tn", null) != null) {
                                    Collections.sort(alphabetically, new Comparator<nCategory>() {
                                        @Override
                                        public int compare(final nCategory object1, final nCategory object2) {
                                            return object1.getCategoryName(getActivity()).compareTo(object2.getCategoryName(getActivity()));
                                        }
                                    });
                                    categoriesList.addAll(alphabetically);
                                    alphabetically.clear();
                                    alphabetically = null;
                                }
                            } else {
                                Collections.sort(alphabetically, new Comparator<nCategory>() {
                                    @Override
                                    public int compare(final nCategory object1, final nCategory object2) {
                                        return object1.getCategoryName(getActivity()).compareTo(object2.getCategoryName(getActivity()));
                                    }
                                });
                                categoriesList.addAll(alphabetically);
                                alphabetically.clear();
                                alphabetically = null;
                            }
                        } else {
                            categoriesList.add(category);
                        }
                    }
                }

                mCategoryListAdapter.addItems(categoriesList);
                mRvCategory.setVisibility(View.VISIBLE);
            }
        } catch (JSONException e) {
            Logger.error("parseResult", e.getMessage());
        } finally {
            mPbLoadingCategories.smoothToHide();

            if (connected) {
                mSwipeRefreshLayout.setEnabled(true);
                mRlNoConnection.setVisibility(View.GONE);
            } else {
                mRlNoConnection.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnRetryResult) {
            getCategoriesAsync();
        }
    }
}