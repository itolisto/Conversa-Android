/**
 * Search implementation: http://stackoverflow.com/questions/9556795/android-actionbar-search-widget-implementation-in-listfragment
 */

package ee.app.conversa;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.flurry.android.FlurryAgent;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import ee.app.conversa.extendables.BaseActivity;
import ee.app.conversa.interfaces.FunctionCallback;
import ee.app.conversa.interfaces.OnCategoryClickListener;
import ee.app.conversa.items.HeaderItem;
import ee.app.conversa.items.SectionableItem;
import ee.app.conversa.model.nCategory;
import ee.app.conversa.networking.FirebaseCustomException;
import ee.app.conversa.networking.NetworkingManager;
import ee.app.conversa.utils.AppActions;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Logger;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;

public class FragmentCategory extends Fragment implements OnCategoryClickListener, View.OnClickListener {

    private RelativeLayout mRlNoConnection;
    private RecyclerView mRvCategory;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private AVLoadingIndicatorView mPbLoadingCategories;

    private FlexibleAdapter<AbstractFlexibleItem> mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_category, container, false);

        mRlNoConnection = rootView.findViewById(R.id.rlNoConnection);
        mRvCategory = rootView.findViewById(R.id.rvCategories);
        mSwipeRefreshLayout = rootView.findViewById(R.id.srlCategories);
        mPbLoadingCategories = rootView.findViewById(R.id.pbLoadingCategories);
        Button mBtnRetry = rootView.findViewById(R.id.btnRetryResult);

        mBtnRetry.setOnClickListener(this);

        mAdapter = new FlexibleAdapter<>(new ArrayList<AbstractFlexibleItem>(), null);
        mRvCategory.setLayoutManager(new SmoothScrollLinearLayoutManager(getActivity()));
        mRvCategory.setAdapter(mAdapter);
        mRvCategory.setHasFixedSize(true);
        mRvCategory.setItemAnimator(new DefaultItemAnimator());

        mAdapter.setUnlinkAllItemsOnRemoveHeaders(true)
                .setDisplayHeadersAtStartUp(true) //Show Headers at startUp!
                .setStickyHeaders(true); //Make headers sticky

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
            NetworkingManager.getInstance().post("general/getCategories", params, new FunctionCallback<JSONArray>() {
                @Override
                public void done(JSONArray json, FirebaseCustomException exception) {
                    if (exception == null) {
                        parseResult(json, true);
                    } else {
                        if (AppActions.validateParseException(exception)) {
                            AppActions.appLogout(getActivity(), true);
                        } else {
                            parseResult(null, true);
                        }
                    }
                }
            });
        } else {
            if (mRlNoConnection.getVisibility() == View.GONE) {
                mSwipeRefreshLayout.setEnabled(false);
                parseResult(null, false);
            }
        }
    }

    @Override
    public void onCategoryClick(nCategory category, View itemView, int position) {
        Intent intent = new Intent(getContext(), ActivityBusiness.class);

        intent.putExtra(Const.kObjectRowObjectIdKey, category.getObjectId());
        intent.putExtra("custom", category.isCustom());
        String categoryName = category.getCategoryName(getActivity());
        intent.putExtra(Const.kClassCategory, categoryName);

        Map<String, String> articleParams = new HashMap<>(2);
        articleParams.put("category", category.getObjectId());
        articleParams.put("custom", String.valueOf(category.isCustom()));
        FlurryAgent.logEvent("user_category_selected", articleParams);

        startActivity(intent);
    }

    private void parseResult(JSONArray result, boolean connected) {
        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }

        try {
            if (result == null || result.length() == 0) {
                mRvCategory.setVisibility(View.GONE);
            } else {
                mAdapter.clear();

                int size = result.length() - 1;

                for (int i = size; i >= 0; i--) {
                    JSONObject object = result.getJSONObject(i);

                    HeaderItem headerItem = new HeaderItem(String.valueOf(i), object.optString("tn", ""));

                    mAdapter.addSection(headerItem);

                    JSONArray categories = object.getJSONArray("cat");
                    int categoriesSize = categories.length();

                    for (int h = 0; h < categoriesSize; h++) {
                        JSONObject category = categories.getJSONObject(h);
                        SectionableItem categoryReg = new SectionableItem(
                                headerItem,
                                (AppCompatActivity)getActivity(),
                                this,
                                new nCategory(
                                    category.optString("ob", ""),
                                    category.optString("na", ""),
                                    category.optString("th", ""),
                                    category.optBoolean("cs", false)
                                )
                        );

                        mAdapter.addItemToSection(categoryReg, headerItem, h);
                    }
                }

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