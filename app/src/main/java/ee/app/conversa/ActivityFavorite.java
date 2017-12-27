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
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import ee.app.conversa.adapters.FavsAdapter;
import ee.app.conversa.extendables.ConversaActivity;
import ee.app.conversa.interfaces.OnContactClickListener;
import ee.app.conversa.interfaces.OnFavoriteClickListener;
import ee.app.conversa.model.Favorite;
import ee.app.conversa.model.database.dbBusiness;
import ee.app.conversa.utils.AppActions;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Logger;

public class ActivityFavorite extends ConversaActivity implements OnFavoriteClickListener {

    //private int page;
    //private int mPreviousTotal;
    private boolean loadMore;
    //private boolean loadingPage;*/

    //private int visibleThreshold = 20;
    private int currentPage = 0;
    private int previousTotal = 0;
    private boolean loading = false;


    private GridView mRvBusiness;
    private RelativeLayout mRlNoConnection;
    private FavsAdapter mBusinessListAdapter;
    private AVLoadingIndicatorView mPbLoadingCategory;
    private LinearLayout mLoadBusiness;
    private Button mStartBrowsingf;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        checkInternetConnection = false;
        initialization();
    }

    @Override
    protected void initialization() {
        super.initialization();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarFavs);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getResources().getString(R.string.favorites_title));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //page = 0;
        loading = true;
        loadMore = true;
        //mPreviousTotal=0;*/

        mRlNoConnection = (RelativeLayout) findViewById(R.id.rlNoConnectionFavs);
        mPbLoadingCategory = (AVLoadingIndicatorView) findViewById(R.id.pbLoadingCategoryFavs);


        mRvBusiness = (GridView) findViewById(R.id.gvFavoritesGrid);
        mLoadBusiness = (LinearLayout) findViewById(R.id.rlLoadingBusiness);
        mStartBrowsingf = (Button)findViewById(R.id.btnStartBrowsingF);


        mBusinessListAdapter = new FavsAdapter(this, this, mRvBusiness);
        mRvBusiness.setAdapter(mBusinessListAdapter);
        mStartBrowsingf.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                Logger.error("onClick","clickover");
                finish();
            }
        });

        mRvBusiness.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                Logger.error("onScroll", "firstVisible:" + firstVisibleItem + " visibleItems:" + visibleItemCount + " totalItems:" + totalItemCount );
                // 1. If load more is true retrieve more messages otherwise skip
                /*if (loadMore) {
                   // final int lastVisibleItem = 1;//((LinearLayoutManager)view.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                   // final int totalItemCount = 2;//view.getLayoutManager().getItemCount();

                    // 2. Check if app isn't checking for new messages and last visible item is on the top
                   // if (!loadingPage && lastVisibleItem == (totalItemCount - 1)) {
                    //    loadingPage = true;
                        //mBusinessListAdapter.addLoad(true);
                    //    getBusinessByCategoryAsync();
                    if(totalItemCount > mPreviousTotal) {
                        mPreviousTotal = totalItemCount;
                        page++;

                        if(totalItemCount)
                    }
                }*/
                // If loadMore true it's means that we can possible have more items we need to retrieve
                if(loadMore) {
                    if (!loading && ((firstVisibleItem + visibleItemCount) >= totalItemCount)) {
//                        // I load the next page of gigs using a background task,
//                        // but you can call any function here.
                        //mBusinessListAdapter.addLoad(true);
                        mLoadBusiness.setVisibility(View.VISIBLE);
                        getBusinessByCategoryAsync();

                        //Logger.error("onScroll", "Calling for more favs");
                    }
                }
            }
        });

        getBusinessByCategoryAsync();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Logger.error("OnOptionsItemSelected", "recibido, recibido :" + item.getItemId());
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getBusinessByCategoryAsync() {
        if (hasInternetConnection()) {
            if (currentPage == 0)
                mPbLoadingCategory.smoothToShow();

            loading = true;
            HashMap<String, Object> params = new HashMap<>(2);
            params.put("skip", currentPage);
            params.put("customerId", ConversaApp.getInstance(this).getPreferences().getAccountCustomerId());
            ParseCloud.callFunctionInBackground("getCustomerFavs", params, new FunctionCallback<String>() {
                @Override
                public void done(String result, ParseException e) {
                    if (currentPage == 0)
                        mPbLoadingCategory.smoothToHide();

                    if (e != null) {
                        if (AppActions.validateParseException(e)) {
                            AppActions.appLogout(getApplicationContext(), true);
                        } else {
                            if (currentPage == 0)
                                findViewById(R.id.llNoResultsContainer).setVisibility(View.VISIBLE);
                        }
                    } else {
                        try {
                            JSONArray results = new JSONArray(result);
                            int size = results.length();

                            if (size > 0) {
                                List<Favorite> businesses = new ArrayList<>(size);

                                for (int i = 0; i < size; i++) {
                                    JSONObject businessReg = results.getJSONObject(i);

                                    Favorite business = new Favorite(
                                            businessReg.getString("oj"),
                                            businessReg.getString("dn"),
                                            businessReg.getString("av")
                                    );

                                    businesses.add(business);
                                }

                                if (size < 20) {
                                    loadMore = false;
                                }

                                mBusinessListAdapter.addItems(businesses);


                                if (mRlNoConnection.getVisibility() == View.VISIBLE) {
                                    mRlNoConnection.setVisibility(View.GONE);
                                }

                                if (loading) {
                                    loading = false;
                                    //mBusinessListAdapter.addLoad(false);
                                    mLoadBusiness.setVisibility(View.GONE);
                                }

                                if (currentPage == 0) {
                                    mRvBusiness.setVisibility(View.VISIBLE);
                                }
                            } else {
                                if (currentPage == 0) {
                                    findViewById(R.id.llNoResultsContainer).setVisibility(View.VISIBLE);
                                }
                                loadMore = false;
                            }

                            currentPage++;
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
    public void onFavoriteClick(Favorite contact, View v, int position) {
        // Do something
        Logger.error("OnFavoriteClick", "recibido, recibido :" + contact.getBusinessName());

        dbBusiness business = ConversaApp.getInstance(this)
                .getDB()
                .isContact(contact.getObjectId());
        Intent intent = new Intent(this, ActivityProfile.class);

        if (business == null) {
            business = new dbBusiness();
            business.setBusinessId(contact.getObjectId());
            business.setDisplayName(contact.getBusinessName());
            business.setConversaId("");
            intent.putExtra(Const.iExtraAddBusiness, true);
        } else {
            intent.putExtra(Const.iExtraAddBusiness, false);
        }

        if (TextUtils.isEmpty(business.getAvatarThumbFileId())) {
            business.setAvatarThumbFileId(contact.getAvatarUrl());
        } else {
            if (!business.getAvatarThumbFileId().equals(contact.getAvatarUrl())) {
                // Update avatar
                business.setAvatarThumbFileId(contact.getAvatarUrl());
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