/**
 * Search implementation: http://stackoverflow.com/questions/9556795/android-actionbar-search-widget-implementation-in-listfragment
 */

package ee.app.conversa;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.flurry.android.FlurryAgent;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import ee.app.conversa.extendables.BaseActivity;
import ee.app.conversa.interfaces.OnCategoryClickListener;
import ee.app.conversa.items.HeaderItem;
import ee.app.conversa.items.SectionableItem;
import ee.app.conversa.model.nCategory;
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

        mRlNoConnection = (RelativeLayout) rootView.findViewById(R.id.rlNoConnection);
        mRvCategory = (RecyclerView) rootView.findViewById(R.id.rvCategories);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.srlCategories);
        mPbLoadingCategories = (AVLoadingIndicatorView) rootView.findViewById(R.id.pbLoadingCategories);
        Button mBtnRetry = (Button) rootView.findViewById(R.id.btnRetryResult);

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
            ParseCloud.callFunctionInBackground("getCategories", params, new FunctionCallback<String>() {
                @Override
                public void done(String result, ParseException e) {
                    if (e == null) {
                        parseResult(result, true);
                    } else {
                        if (AppActions.validateParseException(e)) {
                            AppActions.appLogout(getActivity(), true);
                        } else {
                            parseResult("", true);
                        }
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
        intent.putExtra("custom", category.isCustom());
        String categoryName = category.getCategoryName(getActivity());
        intent.putExtra(Const.kClassCategory, categoryName);

        Map<String, String> articleParams = new HashMap<>(2);
        articleParams.put("category", category.getObjectId());
        articleParams.put("custom", String.valueOf(category.isCustom()));
        FlurryAgent.logEvent("user_category_selected", articleParams);

        startActivity(intent);
    }

    private void parseResult(String result, boolean connected) {
        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }

        try {
            if (result == null || result.isEmpty()) {
                mRvCategory.setVisibility(View.GONE);
            } else {
                mAdapter.clear();

                JSONArray results = new JSONArray(result);
                int size = results.length() - 1;

                for (int i = size; i >= 0; i--) {
                    JSONObject object = results.getJSONObject(i);

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