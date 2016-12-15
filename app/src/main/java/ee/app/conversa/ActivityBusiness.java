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

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ee.app.conversa.adapters.BusinessAdapter;
import ee.app.conversa.extendables.ConversaActivity;
import ee.app.conversa.interfaces.OnBusinessClickListener;
import ee.app.conversa.model.database.dbBusiness;
import ee.app.conversa.model.parse.Business;
import ee.app.conversa.model.parse.BusinessCategory;
import ee.app.conversa.model.parse.bCategory;
import ee.app.conversa.utils.AppActions;
import ee.app.conversa.utils.Const;

public class ActivityBusiness extends ConversaActivity implements OnBusinessClickListener {

    private RelativeLayout mRlNoConnection;
    private AVLoadingIndicatorView mPbLoadingCategory;
    private RecyclerView mRvBusiness;

    private BusinessAdapter mBusinessListAdapter;

    private String categoryId;
    private int page;

    public ActivityBusiness() {
        page = 0;
    }

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

        if (getIntent().getExtras().containsKey(Const.kObjectRowObjectIdKey)) {
            categoryId = getIntent().getExtras().getString(Const.kObjectRowObjectIdKey);
        }

        String title = "";

        if (getIntent().getExtras().containsKey(Const.kClassCategory)) {
            title = getIntent().getExtras().getString(Const.kClassCategory);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mRlNoConnection = (RelativeLayout) findViewById(R.id.rlNoConnection);
        mPbLoadingCategory = (AVLoadingIndicatorView) findViewById(R.id.pbLoadingCategory);
        mRvBusiness = (RecyclerView) findViewById(R.id.rvBusiness);

        mBusinessListAdapter= new BusinessAdapter(this, this);
        mRvBusiness.setHasFixedSize(true);
        mRvBusiness.setLayoutManager(new LinearLayoutManager(this));
        mRvBusiness.setAdapter(mBusinessListAdapter);
        mRvBusiness.setItemAnimator(new DefaultItemAnimator());

        page = 0;
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
            if(TextUtils.isEmpty(categoryId)) {
                // This should never happen
                mRlNoConnection.setVisibility(View.GONE);
                mPbLoadingCategory.smoothToHide();
                mRvBusiness.setVisibility(View.GONE);
            } else {
                ParseQuery<BusinessCategory> query = ParseQuery.getQuery(BusinessCategory.class);
                Collection<String> collection = new ArrayList<>();
                collection.add(Const.kBusinessCategoryBusinessKey);
                query.selectKeys(collection);

                String cat = Const.kBusinessCategoryBusinessKey.concat(".").concat(Const.kBusinessBusinessInfoKey);
                query.include(cat);
                query.whereEqualTo(Const.kBusinessCategoryCategoryKey, ParseObject.createWithoutData(bCategory.class, categoryId));
                query.whereEqualTo(Const.kBusinessCategoryActiveKey, true);

                ParseQuery<Business> param1 = ParseQuery.getQuery(Business.class);
                param1.whereEqualTo(Const.kBusinessActiveKey, true);
                param1.whereEqualTo(Const.kBusinessCountryKey, ParseObject.createWithoutData("Country", "QZ31UNerIj"));
                param1.whereDoesNotExist(Const.kBusinessBusinessKey);

                query.whereMatchesKeyInQuery(Const.kBusinessCategoryBusinessKey, Const.kObjectRowObjectIdKey, param1);
                query.orderByAscending(Const.kBusinessCategoryRelevanceKey);
                query.addAscendingOrder(Const.kBusinessCategoryPositionKey);
                query.setLimit(25);
                query.setSkip(page * 25);

                mPbLoadingCategory.smoothToShow();

                query.findInBackground(new FindCallback<BusinessCategory>() {

                    @Override
                    public void done(List<BusinessCategory> objects, ParseException e) {
                        if (e != null) {
                            AppActions.validateParseException(getApplicationContext(), e);
                        }

                        if (objects != null && objects.size() > 0) {
                            List<Business> business = new ArrayList<>(objects.size());
                            final int size = objects.size();

                            for (int i = 0; i < size; i++) {
                                business.add(objects.get(i).getBusiness());
                            }

                            boolean add = (size == 15);
                            mBusinessListAdapter.addItems(business, add);

                            mPbLoadingCategory.smoothToHide();

                            if (mRlNoConnection.getVisibility() == View.VISIBLE) {
                                mRlNoConnection.setVisibility(View.GONE);
                            }

                            if(page == 0) {
                                mRvBusiness.setVisibility(View.VISIBLE);
                            }

                            page++;
                        } else {
                            mPbLoadingCategory.smoothToHide();
                        }
                    }
                });
            }
        } else {
            mPbLoadingCategory.smoothToHide();
            mRvBusiness.setVisibility(View.GONE);
            mRlNoConnection.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBusinessClick(Business business, View itemView, int position) {
        dbBusiness dbBusiness = ConversaApp.getInstance(this).getDB().isContact(business.getObjectId());
        Intent intent = new Intent(this, ActivityProfile.class);

        if (dbBusiness == null) {
            dbBusiness = new dbBusiness();
            dbBusiness.setBusinessId(business.getObjectId());
            dbBusiness.setDisplayName(business.getDisplayName());
            dbBusiness.setConversaId(business.getConversaID());
            dbBusiness.setAbout(business.getAbout());
            intent.putExtra(Const.iExtraAddBusiness, true);
        } else {
            intent.putExtra(Const.iExtraAddBusiness, false);
        }

        if(business.getAvatar() != null && !TextUtils.isEmpty(business.getAvatar().getUrl())) {
            if (TextUtils.isEmpty(dbBusiness.getAvatarThumbFileId())) {
                dbBusiness.setAvatarThumbFileId(business.getAvatar().getUrl());
            } else {
                if (!dbBusiness.getAvatarThumbFileId().equals(business.getAvatar().getUrl())) {
                    // Update avatar
                    dbBusiness.setAvatarThumbFileId(business.getAvatar().getUrl());
                }
            }
        }

        intent.putExtra(Const.iExtraBusiness, dbBusiness);
        startActivity(intent);
        // Override transitions: we don't want the normal window animation in addition
        // to our custom one
        overridePendingTransition(0, 0);
    }

}