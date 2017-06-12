package ee.app.conversa;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
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
import java.util.List;
import java.util.Map;

import ee.app.conversa.adapters.BusinessAdapter;
import ee.app.conversa.extendables.ConversaActivity;
import ee.app.conversa.interfaces.OnContactClickListener;
import ee.app.conversa.model.database.dbBusiness;
import ee.app.conversa.utils.AppActions;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Logger;

public class ActivityBusiness extends ConversaActivity implements OnContactClickListener {

    private RelativeLayout mRlNoConnection;
    private AVLoadingIndicatorView mPbLoadingCategory;
    private RecyclerView mRvBusiness;
    private BusinessAdapter mBusinessListAdapter;

    private String categoryId;

    private int page;
    private boolean loadingPage;
    private boolean loadMore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business);
        checkInternetConnection = false;
        initialization();
    }

    @Override
    protected void initialization() {
        super.initialization();

        categoryId = getIntent().getExtras().getString(Const.kObjectRowObjectIdKey);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getIntent().getExtras().getString(Const.kClassCategory));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        page = 0;
        loadingPage = false;
        loadMore = true;

        mRlNoConnection = (RelativeLayout) findViewById(R.id.rlNoConnection);
        mPbLoadingCategory = (AVLoadingIndicatorView) findViewById(R.id.pbLoadingCategory);
        mRvBusiness = (RecyclerView) findViewById(R.id.rvBusiness);

        mBusinessListAdapter = new BusinessAdapter(this, this);
        mRvBusiness.setHasFixedSize(true);
        mRvBusiness.setLayoutManager(new LinearLayoutManager(this));
        mRvBusiness.setAdapter(mBusinessListAdapter);
        mRvBusiness.setItemAnimator(new DefaultItemAnimator());
        mRvBusiness.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                // 1. If load more is true retrieve more messages otherwise skip
                if (loadMore) {
                    final int lastVisibleItem = ((LinearLayoutManager)recyclerView.getLayoutManager())
                            .findLastCompletelyVisibleItemPosition();
                    final int totalItemCount = recyclerView.getLayoutManager().getItemCount();

                    // 2. Check if app isn't checking for new messages and last visible item is on the top
                    if (!loadingPage && lastVisibleItem == (totalItemCount - 1)) {
                        loadingPage = true;
                        mBusinessListAdapter.addLoad(true);
                        getBusinessByCategoryAsync();
                    }
                }
            }
        });

        getBusinessByCategoryAsync();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getBusinessByCategoryAsync() {
        if (hasInternetConnection()) {
            if (page == 0)
                mPbLoadingCategory.smoothToShow();

            HashMap<String, Object> params = new HashMap<>(2);
            params.put("page", page);
            params.put("categoryId", categoryId);
            ParseCloud.callFunctionInBackground("getCategoryBusinesses", params, new FunctionCallback<String>() {
                @Override
                public void done(String result, ParseException e) {
                    if (page == 0)
                        mPbLoadingCategory.smoothToHide();

                    if (loadingPage) {
                        loadingPage = false;
                        mBusinessListAdapter.addLoad(false);
                    }

                    if (e != null) {
                        if (AppActions.validateParseException(e)) {
                            AppActions.appLogout(getApplicationContext(), true);
                        } else {
                            if (page == 0)
                                findViewById(R.id.llNoResultsContainer).setVisibility(View.VISIBLE);
                        }
                    } else {
                        try {
                            JSONArray results = new JSONArray(result);
                            int size = results.length();

                            if (size > 0) {
                                List<dbBusiness> businesses = new ArrayList<>(size);

                                for (int i = 0; i < size; i++) {
                                    JSONObject businessReg = results.getJSONObject(i);

                                    dbBusiness business = new dbBusiness();
                                    business.setBusinessId(businessReg.getString("ob"));
                                    business.setDisplayName(businessReg.getString("dn"));
                                    business.setConversaId(businessReg.getString("cn"));
                                    business.setAvatarThumbFileId(businessReg.getString("av"));

                                    businesses.add(business);
                                }

                                if (size < 20) {
                                    loadMore = false;
                                }

                                mBusinessListAdapter.addItems(businesses);

                                if (mRlNoConnection.getVisibility() == View.VISIBLE) {
                                    mRlNoConnection.setVisibility(View.GONE);
                                }

                                if (page == 0) {
                                    mRvBusiness.setVisibility(View.VISIBLE);
                                }
                            } else {
                                if (page == 0) {
                                    findViewById(R.id.llNoResultsContainer).setVisibility(View.VISIBLE);
                                }
                                loadMore = false;
                            }

                            page++;
                        } catch (JSONException f) {
                            Logger.error("parseResult", f.getMessage());
                        }
                    }
                }
            });
        } else {
            mRvBusiness.setVisibility(View.GONE);
            mRlNoConnection.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onContactClick(dbBusiness contact, View v, int position) {
        dbBusiness business = ConversaApp.getInstance(this)
                .getDB()
                .isContact(contact.getBusinessId());
        Intent intent = new Intent(this, ActivityProfile.class);

        if (business == null) {
            business = new dbBusiness();
            business.setBusinessId(contact.getBusinessId());
            business.setDisplayName(contact.getDisplayName());
            business.setConversaId(contact.getConversaId());
            intent.putExtra(Const.iExtraAddBusiness, true);
        } else {
            intent.putExtra(Const.iExtraAddBusiness, false);
        }

        if (TextUtils.isEmpty(business.getAvatarThumbFileId())) {
            business.setAvatarThumbFileId(contact.getAvatarThumbFileId());
        } else {
            if (!business.getAvatarThumbFileId().equals(contact.getAvatarThumbFileId())) {
                // Update avatar
                business.setAvatarThumbFileId(contact.getAvatarThumbFileId());
            }
        }

        Map<String, String> articleParams = new HashMap<>(1);
        articleParams.put("fromCategory", String.valueOf(true));
        FlurryAgent.logEvent("user_profile_open", articleParams);

        intent.putExtra(Const.iExtraBusiness, business);
        startActivity(intent);
        // Override transitions: we don't want the normal window animation in addition
        // to our custom one
        overridePendingTransition(0, 0);
    }
}