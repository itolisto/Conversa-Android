package ee.app.conversa;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import ee.app.conversa.model.database.dbBusiness;
import ee.app.conversa.model.parse.Business;
import ee.app.conversa.model.parse.BusinessCategory;
import ee.app.conversa.model.parse.bCategory;
import ee.app.conversa.utils.Const;

public class FragmentBusiness extends Fragment implements BusinessAdapter.OnItemClickListener {

    private RelativeLayout mRlNoBusiness;
    private RecyclerView mRvBusiness;
    private ImageView mIvNoBusiness;
    private AVLoadingIndicatorView mPbLoadingCategory;

    private BusinessAdapter mBusinessListAdapter;

    private String categoryId;
    private int page;

    public FragmentBusiness() {
        page = 0;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(Const.kObjectRowObjectIdKey)) {
            categoryId = getArguments().getString(Const.kObjectRowObjectIdKey);
        }

        String title = "";

        if (getArguments().containsKey(Const.kClassCategory)) {
            title = getArguments().getString(Const.kClassCategory);
        }

        ActionBar actionBar = ((ActivityMain) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_business_categories, container, false);

        mRlNoBusiness = (RelativeLayout) rootView.findViewById(R.id.rlNoBusiness);
        mRvBusiness = (RecyclerView) rootView.findViewById(R.id.rvBusiness);
        mIvNoBusiness = (ImageView) rootView.findViewById(R.id.ivNoBusiness);
        mPbLoadingCategory = (AVLoadingIndicatorView) rootView.findViewById(R.id.pbLoadingCategory);

        mBusinessListAdapter= new BusinessAdapter((AppCompatActivity)getActivity(), this);
        mRvBusiness.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRvBusiness.setHasFixedSize(true);
        mRvBusiness.setAdapter(mBusinessListAdapter);
        mRvBusiness.setItemAnimator(new DefaultItemAnimator());

        page = 0;
        getBusinessByCategoryAsync();

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ((ActivityMain) getActivity()).onBackPressedFromCategory();
                return true;
        }

        return false;
    }

    private void getBusinessByCategoryAsync() {
        if(categoryId.isEmpty()) {
            // Some error while getting category id
            mRvBusiness.setVisibility(View.GONE);
            mRlNoBusiness.setVisibility(View.VISIBLE);
            mPbLoadingCategory.smoothToHide();
            mIvNoBusiness.setVisibility(View.VISIBLE);
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
            query.setLimit(15);
            query.setSkip(page * 15);

            mPbLoadingCategory.smoothToShow();

            query.findInBackground(new FindCallback<BusinessCategory>() {

                @Override
                public void done(List<BusinessCategory> objects, ParseException e) {
                    if (objects != null && objects.size() > 0) {
                        List<Business> business = new ArrayList<>(objects.size());
                        final int size = objects.size();

                        for (int i = 0; i < size; i++) {
                            business.add(objects.get(i).getBusiness());
                        }

                        boolean add = (size == 15);
                        mBusinessListAdapter.addItems(business, add);

                        mPbLoadingCategory.smoothToHide();

                        if(page == 0) {
                            mRvBusiness.setVisibility(View.VISIBLE);
                            mRlNoBusiness.setVisibility(View.GONE);
                        }

                        page++;
                    } else {
                        mRvBusiness.setVisibility(View.GONE);
                        mRlNoBusiness.setVisibility(View.VISIBLE);
                        mPbLoadingCategory.smoothToHide();

                        if (e == null) {
                            mIvNoBusiness.setVisibility(View.GONE);
                        } else {
                            mIvNoBusiness.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onItemClick(View itemView, int position, Business business) {
        dbBusiness dbBusiness = ConversaApp.getInstance(getActivity()).getDB().isContact(business.getObjectId());
        Intent intent = new Intent(getActivity(), ActivityProfile.class);

        if (dbBusiness == null) {
            dbBusiness = new dbBusiness();
            dbBusiness.setBusinessId(business.getObjectId());
            dbBusiness.setDisplayName(business.getDisplayName());
            dbBusiness.setConversaId(business.getConversaID());
            dbBusiness.setAbout(business.getAbout());
            intent.putExtra(Const.kYapDatabaseName, true);
        } else {
            intent.putExtra(Const.kYapDatabaseName, false);
        }

        try {
            if(business.getAvatar() != null && !business.getAvatar().getUrl().isEmpty()) {
                dbBusiness.setAvatarThumbFileId(business.getAvatar().getUrl());
            } else {
                dbBusiness.setAvatarThumbFileId("");
            }
        } catch (IllegalStateException e) {
            dbBusiness.setAvatarThumbFileId("");
        }

        intent.putExtra(Const.kClassBusiness, dbBusiness);
        startActivity(intent);
    }

}