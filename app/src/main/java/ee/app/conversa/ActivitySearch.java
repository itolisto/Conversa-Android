package ee.app.conversa;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ee.app.conversa.adapters.BusinessAdapter;
import ee.app.conversa.extendables.ConversaActivity;
import ee.app.conversa.interfaces.OnContactClickListener;
import ee.app.conversa.model.database.dbBusiness;
import ee.app.conversa.model.database.dbSearch;
import ee.app.conversa.model.nHeaderTitle;
import ee.app.conversa.utils.AppActions;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Logger;

/**
 * Created by edgargomez on 7/12/16.
 */
public class ActivitySearch extends ConversaActivity implements OnContactClickListener {

    private boolean isLoading;

    private final ExecutorService tpe;
    private final ExecutorService callResults;
    Future<String> future;
    Future<?> futureResult;

    private LinearLayout mLlNoResultsContainer;
    private LinearLayout mLlErrorContainer;
    private AVLoadingIndicatorView mPbLoadingResults;
    private RecyclerView mRvSearchResults;
    private FloatingSearchView mSearchView;

    private List<Object> recentList = new ArrayList<>(6);

    private BusinessAdapter mBusinessListAdapter;

    public ActivitySearch() {
        tpe = new ThreadPoolExecutor(
                1,
                1,
                30,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>()
        );

        callResults = new ThreadPoolExecutor(
                1,
                1,
                30,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>()
        );
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        checkInternetConnection = false;
        isLoading = false;
        initialization();
    }

    @Override
    protected void initialization() {
        super.initialization();

        mSearchView = (FloatingSearchView) findViewById(R.id.floating_search_view);
        mSearchView.setOnHomeActionClickListener(new FloatingSearchView.OnHomeActionClickListener() {
            @Override
            public void onHomeClicked() {
                if (TextUtils.isEmpty(mSearchView.getQuery())) {
                    finish();
                } else {
                    mSearchView.clearQuery();
                    isLoading = false;
                    showRecents();
                }
            }
        });

        mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, final String newQuery) {
                searchBusiness(newQuery, 0);
            }
        });

        mLlNoResultsContainer = (LinearLayout) findViewById(R.id.llNoResultsContainer);
        mLlErrorContainer = (LinearLayout) findViewById(R.id.llErrorContainer);
        mPbLoadingResults = (AVLoadingIndicatorView) findViewById(R.id.pbLoadingResults);
        mRvSearchResults = (RecyclerView) findViewById(R.id.rvSearchResults);

        mBusinessListAdapter= new BusinessAdapter(this, this);
        mRvSearchResults.setHasFixedSize(true);
        mRvSearchResults.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mRvSearchResults.setAdapter(mBusinessListAdapter);
        mRvSearchResults.setItemAnimator(new DefaultItemAnimator());

        recentList.add(new nHeaderTitle(getString(R.string.recent_searches_title), 0));

        recent.execute();
    }

    public synchronized void searchBusiness(final String text, final int skip) {
        // future
        if (future != null) {
            future.cancel(true);
        }

        if (futureResult != null) {
            futureResult.cancel(true);
        }

        if (text.isEmpty()) {
            isLoading = false;
            showRecents();
            return;
        }

        if (skip == 0) {
            mLlNoResultsContainer.setVisibility(View.GONE);
            mLlErrorContainer.setVisibility(View.GONE);
            mRvSearchResults.setVisibility(View.GONE);
        }

        mPbLoadingResults.smoothToShow();
        isLoading = true;

        future = tpe.submit(new Callable<String>() {
            public String call() throws Exception {
                HashMap<String, Object> params = new HashMap<>();
                params.put("search", text);
                params.put("skip", skip);
                try {
                    return ParseCloud.callFunction("searchBusiness", params);
                } catch (ParseException e) {
                    AppActions.validateParseException(getApplicationContext(), e);
                    Logger.error("Future task error: ", e.getMessage());
                    return "";
                }
            }
        });

        futureResult = callResults.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!future.isCancelled()) {
                        final String result = future.get(30, TimeUnit.SECONDS);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showResults(result, false, skip);
                            }
                        });
                    }
                } catch (InterruptedException|ExecutionException|TimeoutException e) {
                    Logger.error("Future task result error: ", e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showResults("", true, 0);
                        }
                    });
                }
            }
        });
    }

    private void showResults(String response, boolean error, int skip) {
        JSONObject jsonRootObject;
        boolean toJSONError = false;

        try {
            if (!error) {
                jsonRootObject = new JSONObject(response);
                JSONArray results = jsonRootObject.optJSONArray("results");
                int size = results.length();

                if (size == 0) {
                    mBusinessListAdapter.clear();
                    return;
                }

                List<Object> allResults = new ArrayList<>(size);

                if (skip == 0) {
                    allResults.add(new nHeaderTitle(getString(R.string.searches_top_results_title), 0));
                }

                for (int i = 0; i < size; i++) {
                    JSONObject object = results.getJSONObject(i);
                    dbBusiness business = new dbBusiness();
                    business.setBusinessId(object.optString("oj"));
                    business.setAvatarThumbFileId(object.optString("av"));
                    business.setConversaId(object.optString("id"));
                    business.setDisplayName(object.optString("dn"));
                    allResults.add(business);
                }

                if (skip == 0) {
                    mBusinessListAdapter.setSearches(allResults, true);
                } else {
                    //mBusinessListAdapter.addLocalItems(allResults, true);
                }
            }
        } catch (JSONException e) {
            toJSONError = true;
        } finally {
            mPbLoadingResults.smoothToHide();
            isLoading = false;

            if (error || toJSONError) {
                // Clear all results and show error
                mLlNoResultsContainer.setVisibility(View.GONE);
                mLlErrorContainer.setVisibility(View.VISIBLE);
            } else if (response.isEmpty() || !response.startsWith("{")) {
                mLlNoResultsContainer.setVisibility(View.GONE);
                mLlErrorContainer.setVisibility(View.GONE);
            } else if (mBusinessListAdapter.getItemCount() <= 1) {
                mLlNoResultsContainer.setVisibility(View.VISIBLE);
                mLlErrorContainer.setVisibility(View.GONE);
            } else {
                mLlNoResultsContainer.setVisibility(View.GONE);
                mLlErrorContainer.setVisibility(View.GONE);
                mRvSearchResults.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onContactClick(final dbBusiness business, View itemView, int position) {
        Intent intent = new Intent(this, ActivityProfile.class);

        if (business.getId() < 0) {
            intent.putExtra(Const.iExtraAddBusiness, true);
        } else {
            intent.putExtra(Const.iExtraAddBusiness, false);
        }

        // Save to recent searches
        new Thread(new Runnable() {
            @Override
            public void run() {
                ConversaApp.getInstance(getApplicationContext())
                        .getDB()
                        .addSearch(new dbSearch(
                                -1,
                                business.getBusinessId(),
                                business.getDisplayName(),
                                business.getConversaId(),
                                business.getAvatarThumbFileId()
                        ));
            }
        }).start();

        intent.putExtra(Const.iExtraBusiness, business);
        startActivity(intent);
        // Override transitions: we don't want the normal window animation in addition
        // to our custom one
        overridePendingTransition(0, 0);
    }

    AsyncTask<Void, Void, List<dbBusiness>> recent = new AsyncTask<Void, Void, List<dbBusiness>>() {
        @Override
        protected List<dbBusiness> doInBackground(Void... params) {
            List<dbSearch> list = ConversaApp.getInstance(getApplicationContext()).getDB()
                    .getRecentSearches();
            List<dbBusiness> searches = new ArrayList<>(list.size());

            for (int i = 0; i < list.size(); i++) {
                dbSearch search = list.get(i);
                dbBusiness business = ConversaApp.getInstance(getApplicationContext()).getDB()
                        .isContact(search.getBusinessId());

                if (business == null) {
                    business = new dbBusiness();
                    business.setBusinessId(search.getBusinessId());
                }

                business.setDisplayName(search.getDisplayName());
                business.setConversaId(search.getConversaId());
                business.setAvatarThumbFileId(search.getAvatarUrl());
                searches.add(business);
            }

            if (searches.size() > 0) {
                recentList.addAll(searches);
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<dbBusiness> dbSearches) {
            showRecents();
        }
    };

    private void showRecents() {
        if (!isLoading) {
            if (recentList.size() > 1) {
                mBusinessListAdapter.setRecents(recentList);
                mPbLoadingResults.smoothToHide();

                if (mRvSearchResults.getVisibility() != View.VISIBLE) {
                    mLlNoResultsContainer.setVisibility(View.GONE);
                    mLlErrorContainer.setVisibility(View.GONE);
                    mRvSearchResults.setVisibility(View.VISIBLE);
                }
            } else {
                mLlNoResultsContainer.setVisibility(View.GONE);
                mLlErrorContainer.setVisibility(View.GONE);
                mRvSearchResults.setVisibility(View.GONE);
            }
        }
    }

}