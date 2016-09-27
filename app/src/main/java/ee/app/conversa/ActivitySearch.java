package ee.app.conversa;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

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
import ee.app.conversa.model.database.dbBusiness;
import ee.app.conversa.model.database.dbSearch;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Logger;
import ee.app.conversa.utils.Utils;

/**
 * Created by edgargomez on 7/12/16.
 */
public class ActivitySearch extends ConversaActivity implements SearchView.OnQueryTextListener,
        BusinessAdapter.OnLocalItemClickListener, View.OnTouchListener {

    private final ExecutorService tpe;
    private final ExecutorService callResults;
    Future<String> future;
    Future<?> futureResult;

    private LinearLayout mLlNoResultsContainer;
    private LinearLayout mLlErrorContainer;
    private AVLoadingIndicatorView mPbLoadingResults;
    private RecyclerView mRvSearchResults;

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
        initialization();
    }

    @Override
    protected void initialization() {
        super.initialization();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initial state of tabs and titles
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mLlNoResultsContainer = (LinearLayout) findViewById(R.id.llNoResultsContainer);
        mLlErrorContainer = (LinearLayout) findViewById(R.id.llErrorContainer);
        mPbLoadingResults = (AVLoadingIndicatorView) findViewById(R.id.pbLoadingResults);
        mRvSearchResults = (RecyclerView) findViewById(R.id.rvSearchResults);

        mBusinessListAdapter= new BusinessAdapter(this, this);
        mRvSearchResults.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mRvSearchResults.setAdapter(mBusinessListAdapter);
        mRvSearchResults.setItemAnimator(new DefaultItemAnimator());
        mRvSearchResults.setOnTouchListener(this);
    }

    public synchronized void searchBusiness(final String text, final int skip) {
        // future
        if(future != null) {
            future.cancel(true);
            while(true) {
                if (future.isCancelled() || future.isDone()) {
                    break;
                }
            }
        }

        if(futureResult != null) {
            futureResult.cancel(true);
            while(true) {
                if (futureResult.isCancelled() || futureResult.isDone()) {
                    break;
                }
            }
        }

        mBusinessListAdapter.clear();
        mLlNoResultsContainer.setVisibility(View.GONE);
        mLlErrorContainer.setVisibility(View.GONE);
        mRvSearchResults.setVisibility(View.GONE);
        mPbLoadingResults.smoothToShow();

        if (text.isEmpty()) {
            showResults("", false);
        }

        future = tpe.submit(new Callable<String>() {
            public String call() throws Exception {
                HashMap<String, Object> params = new HashMap<>();
                params.put("search", text);
                params.put("skip", skip);
                try {
                    return ParseCloud.callFunction("searchBusiness", params);
                } catch (ParseException e) {
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
                                showResults(result, result.isEmpty());
                            }
                        });
                    }
                } catch (InterruptedException|ExecutionException|TimeoutException e) {
                    Logger.error("Future task result error: ", e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showResults("", true);
                        }
                    });
                }
            }
        });
    }

    private void showResults(String response, boolean error) {
        Logger.error("Future get result: ", response);
        JSONObject jsonRootObject;
        boolean toJSONError = false;

        try {
            if (!error) {
                if (!response.isEmpty() && response.startsWith("{")) {
                    jsonRootObject = new JSONObject(response);
                    JSONArray results = jsonRootObject.optJSONArray("results");
                    int size = results.length();

                    if (size == 0) {
                        return;
                    }

                    List<dbBusiness> allResults = new ArrayList<>();
                    dbBusiness business = new dbBusiness();

                    for (int i = 0; i < size; i++) {
                        JSONObject object = results.getJSONObject(i);
                        business.setBusinessId(object.optString("oj"));
                        business.setAvatarThumbFileId(object.optString("av"));
                        business.setConversaId(object.optString("id"));
                        business.setDisplayName(object.optString("dn"));
                        allResults.add(business);
                    }

                    mBusinessListAdapter.addLocalItems(allResults, false);
                }
            }
        } catch (JSONException e) {
            toJSONError = true;
        } finally {
            mPbLoadingResults.smoothToHide();

            if (error || toJSONError) {
                // Clear all results and show error
                mLlNoResultsContainer.setVisibility(View.GONE);
                mLlErrorContainer.setVisibility(View.VISIBLE);
            } else if (response.isEmpty() || !response.startsWith("{")) {
                mLlNoResultsContainer.setVisibility(View.GONE);
                mLlErrorContainer.setVisibility(View.GONE);
            } else if (mBusinessListAdapter.getItemCount() == 0) {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
        //SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        //searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, MainActivity.class)));
        searchView.setIconifiedByDefault(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // User pressed the search button
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        searchBusiness(newText, 0);
        return false;
    }

    @Override
    public void onItemClick(View itemView, int position, final dbBusiness business) {
        final dbBusiness dbbusiness = ConversaApp.getInstance(this)
                .getDB()
                .isContact(business.getBusinessId());

        Intent intent = new Intent(this, ActivityProfile.class);

        if (dbbusiness == null) {
            intent.putExtra(Const.kYapDatabaseName, true);
        } else {
            intent.putExtra(Const.kYapDatabaseName, false);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                ConversaApp.getInstance(getApplicationContext())
                        .getDB()
                        .addSearch(new dbSearch(-1,
                                business.getBusinessId(),
                                business.getDisplayName(),
                                business.getConversaId(),
                                business.getAvatarThumbFileId()
                        ));
            }
        }).start();

        intent.putExtra(Const.kClassBusiness, business);
        startActivity(intent);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Utils.hideKeyboard(this);
        return false;
    }
}