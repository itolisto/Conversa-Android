package ee.app.conversa;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.facebook.drawee.view.SimpleDraweeView;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import ee.app.conversa.extendables.ConversaActivity;
import ee.app.conversa.model.database.dbBusiness;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Logger;
import ee.app.conversa.utils.Utils;
import ee.app.conversa.view.LightTextView;
import ee.app.conversa.view.MediumTextView;
import ee.app.conversa.view.RegularTextView;

public class ActivityProfile extends ConversaActivity implements View.OnClickListener, OnLikeListener {

    // General
    private dbBusiness businessObject;
    private boolean addAsContact;
    // Containers
    private RelativeLayout mRlProfileHeader;
    private RelativeLayout mRlSpecialPromoContainer;
    private LinearLayout mLlClosedOnContainer;
    private LinearLayout mLlGeneralInfo;
    private LinearLayout mLlAddressContainer;
    // Profile views
    private SimpleDraweeView mSdvBusinessImage;
    private LikeButton mBtnFavorite;
    private RegularTextView mRtvFollowers;
    private LightTextView mLtvMemberSince;
    // ClosedOn views
    private LightTextView mLtvMonday;
    private LightTextView mLtvTuesday;
    private LightTextView mLtvWednesday;
    private LightTextView mLtvThursday;
    private LightTextView mLtvFriday;
    private LightTextView mLtvSaturday;
    private LightTextView mLtvSunday;
    // GeneralInfo views
    private LightTextView mltvSchedule;
    private LightTextView mltvDelivery;
    private LightTextView mltvLink;
    private LightTextView mltvContactNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        checkInternetConnection = false;

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                finish();
            } else {
                businessObject = extras.getParcelable(Const.kClassBusiness);
                addAsContact = extras.getBoolean(Const.kYapDatabaseName);
            }
        } else {
            businessObject = savedInstanceState.getParcelable(Const.kClassBusiness);
            addAsContact = savedInstanceState.getBoolean(Const.kYapDatabaseName);
        }

        initialization();
    }

    @Override
    protected void initialization() {
        super.initialization();

        mRlProfileHeader = (RelativeLayout) findViewById(R.id.rlProfileHeader);
        mRlSpecialPromoContainer = (RelativeLayout) findViewById(R.id.rlSpecialPromoContainer);
        mLlClosedOnContainer = (LinearLayout) findViewById(R.id.llClosedOnContainer);
        mLlGeneralInfo = (LinearLayout) findViewById(R.id.llGeneralInfo);
        mLlAddressContainer = (LinearLayout) findViewById(R.id.rlAddressContainer);
        // Profile views
        mSdvBusinessImage = (SimpleDraweeView) findViewById(R.id.sdvBusinessImage);
        mBtnFavorite = (LikeButton) findViewById(R.id.btnFavorite);
        mRtvFollowers = (RegularTextView) findViewById(R.id.rtvFollowers);
        mLtvMemberSince = (LightTextView) findViewById(R.id.ltvMemberSince);
        Button mBtnStartChat = (Button) findViewById(R.id.btnStartChat);
        MediumTextView mMtvBusinessName = (MediumTextView) findViewById(R.id.mtvBusinessName);
        // ClosedOn views
        mLtvMonday = (LightTextView) findViewById(R.id.ltvMonday);
        mLtvTuesday = (LightTextView) findViewById(R.id.ltvTuesday);
        mLtvWednesday = (LightTextView) findViewById(R.id.ltvWednesday);
        mLtvThursday = (LightTextView) findViewById(R.id.ltvThursday);
        mLtvFriday = (LightTextView) findViewById(R.id.ltvFriday);
        mLtvSaturday = (LightTextView) findViewById(R.id.ltvSaturday);
        mLtvSunday = (LightTextView) findViewById(R.id.ltvSunday);
        // GeneralInfo views
        mltvSchedule = (LightTextView) findViewById(R.id.ltvSchedule);
        mltvDelivery = (LightTextView) findViewById(R.id.ltvDelivery);
        mltvLink = (LightTextView) findViewById(R.id.ltvLink);
        mltvContactNumber = (LightTextView) findViewById(R.id.ltvContactNumber);

        mMtvBusinessName.setText(businessObject.getDisplayName());

        Uri uri;
        if(businessObject.getAvatarThumbFileId().isEmpty()) {
            uri = Uri.parse("android.resource://ee.app.conversa/" + R.drawable.business_default);
        } else {
            uri = Uri.parse(businessObject.getAvatarThumbFileId());
        }

        mSdvBusinessImage.setImageURI(uri);
        mBtnFavorite.setOnLikeListener(this);
        mBtnStartChat.setOnClickListener(this);
        mBtnFavorite.setEnabled(false);

        GradientDrawable drawable;
        drawable = (GradientDrawable)mLtvMonday.getBackground();
        drawable.setStroke(1, Color.RED); // set stroke width and stroke color
        drawable = (GradientDrawable)mLtvTuesday.getBackground();
        drawable.setStroke(1, Color.RED);
        drawable = (GradientDrawable)mLtvWednesday.getBackground();
        drawable.setStroke(1, Color.RED);
        drawable = (GradientDrawable)mLtvThursday.getBackground();
        drawable.setStroke(1, Color.RED);
        drawable = (GradientDrawable)mLtvFriday.getBackground();
        drawable.setStroke(1, Color.RED);
        drawable = (GradientDrawable)mLtvSaturday.getBackground();
        drawable.setStroke(1, Color.RED);
        drawable = (GradientDrawable)mLtvSunday.getBackground();
        drawable.setStroke(1, Color.RED);

        // Call Parse for registry
        HashMap<String, String> params = new HashMap<>(1);
        params.put("business", businessObject.getBusinessId());
        ParseCloud.callFunctionInBackground("profileInfo", params, new FunctionCallback<String>() {
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
    public void onClick(View v) {
        if (v instanceof Button) {
            switch (v.getId()) {
                case R.id.btnStartChat:
                    Intent intent = new Intent(this, ActivityChatWall.class);
                    intent.putExtra(Const.kClassBusiness, businessObject);
                    intent.putExtra(Const.kYapDatabaseName, addAsContact);
                    startActivity(intent);
                    break;
            }
        }
    }

    @Override
    public void liked(final LikeButton likeButton) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("business", businessObject.getBusinessId());
        params.put("favorite", true);
        ParseCloud.callFunctionInBackground("favorite", params, new FunctionCallback<Object>() {
            @Override
            public void done(Object object, ParseException e) {
                likeButton.setLiked(true);
            }
        });
    }

    @Override
    public void unLiked(final LikeButton likeButton) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("business", businessObject.getBusinessId());
        ParseCloud.callFunctionInBackground("favorite", params, new FunctionCallback<Object>() {
            @Override
            public void done(Object object, ParseException e) {
                likeButton.setLiked(false);
            }
        });
    }

    private void parseResult(String result) {
        try {
            if (result.isEmpty()) {
                mBtnFavorite.setLiked(false);
            } else {
                JSONObject jsonRootObject = new JSONObject(result);

                JSONArray options = jsonRootObject.optJSONArray("options");
                JSONArray tags = jsonRootObject.optJSONArray("tags");
                JSONArray openOn = jsonRootObject.optJSONArray("openOn");
                int followers = jsonRootObject.optInt("followers", 0);
                boolean verified = jsonRootObject.optBoolean("verified", false);
                long since = jsonRootObject.optLong("since", 0L);
                boolean favorite = jsonRootObject.optBoolean("favorite", false);

                JSONObject specialPromo = jsonRootObject.optJSONObject("promo");
                if (specialPromo != null) {
                    mRlSpecialPromoContainer.setVisibility(View.VISIBLE);
                }

                // Iterator
                int i;

                // Get options
                int size = options.length();
                for (i = 0; i < size; i++) {
                    JSONObject object = options.optJSONObject(i);
                    String value = object.optString("value");
                    Logger.error("Options", value);

                    switch (object.optString("code")) {
                        case Const.kOptionsContactNumber:
                            break;
                        case Const.kOptionsAddress:
                            break;
                        case Const.kOptionsClosedOn:
                            break;
                        case Const.kOptionsDelivery:
                            mltvDelivery.setText(value);
                            break;
                        case Const.kOptionsLink:
                            mltvLink.setText(value);
                            break;
                        case Const.kOptionsSpecialPromo:
                            break;
                        case Const.kOptionsDaySpecial:
                            break;
                        default:
                            break;
                    }
                }

                // Get tags
                size = tags.length();
                for (i = 0; i < size; i++) {
                    String tag = tags.getString(i);
                    Logger.error("Tags", tag);
                }

                // Get open days
                size = openOn.length();
                for (i = 0; i < size; i++) {
                    GradientDrawable drawable;
                    switch (openOn.getInt(i)) {
                        case 1:
                            drawable = (GradientDrawable)mLtvMonday.getBackground();
                            drawable.setStroke(1, Color.GREEN);
                            break;
                        case 2:
                            drawable = (GradientDrawable)mLtvTuesday.getBackground();
                            drawable.setStroke(1, Color.GREEN);
                            break;
                        case 3:
                            drawable = (GradientDrawable)mLtvWednesday.getBackground();
                            drawable.setStroke(1, Color.GREEN);
                            break;
                        case 4:
                            drawable = (GradientDrawable)mLtvThursday.getBackground();
                            drawable.setStroke(1, Color.GREEN);
                            break;
                        case 5:
                            drawable = (GradientDrawable)mLtvFriday.getBackground();
                            drawable.setStroke(1, Color.GREEN);
                            break;
                        case 6:
                            drawable = (GradientDrawable)mLtvSaturday.getBackground();
                            drawable.setStroke(1, Color.GREEN);
                            break;
                        case 7:
                            drawable = (GradientDrawable)mLtvSunday.getBackground();
                            drawable.setStroke(1, Color.GREEN);
                            break;
                    }
                }

                mBtnFavorite.setLiked(favorite);
                mRtvFollowers.setText(getString(R.string.number_of_followers, followers));
                mLtvMemberSince.setText(Utils.getDate(this, since));
            }
        } catch (JSONException e) {
            Logger.error("parseResult", e.getMessage());
        } finally {
            mBtnFavorite.setEnabled(true);
        }
    }
}