package ee.app.conversa;

import android.app.TaskStackBuilder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.birbit.android.jobqueue.JobManager;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;
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
import ee.app.conversa.jobs.FavoriteJob;
import ee.app.conversa.model.database.dbBusiness;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Logger;
import ee.app.conversa.utils.AppActions;
import ee.app.conversa.utils.Utils;
import ee.app.conversa.view.BoldTextView;
import ee.app.conversa.view.LightTextView;
import ee.app.conversa.view.MediumTextView;
import ee.app.conversa.view.RegularTextView;
import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.SharingHelper;
import io.branch.referral.util.BranchEvent;
import io.branch.referral.util.LinkProperties;
import io.branch.referral.util.ShareSheetStyle;

import static ee.app.conversa.R.id.rtvLocationDescription;

public class ActivityProfile extends ConversaActivity implements
        View.OnClickListener, OnLikeListener {

    private final String TAG = ActivityProfile.class.getSimpleName();

    // General
    private dbBusiness businessObject;
    private boolean addAsContact;
    private int followers;
    private int rgb;
    private Toolbar toolbar;
    private JobManager jobManager;
    private static final int PLACE_PICKER_FLAG = 1;
    // Containers
    private RelativeLayout mRlProfileHeader;
    private LinearLayout mLlSpecialPromoContainer;
    private LinearLayout mLlClosedOnContainer;
    private LinearLayout mLlScheduleContainer;
    private LinearLayout mLlDeliveryContainer;
    private LinearLayout mLlLinkContainer;
    private LinearLayout mLlContactNumberContainer;
    private LinearLayout mLlAddressContainer;
    // Profile views
    private SimpleDraweeView mSdvBusinessImage;
    private LikeButton mBtnFavorite;
    private BoldTextView mBtvFollowers;
    private View mIvStatus;
    // Special promo
    private RegularTextView mRtvSpecialPromo;
    private SimpleDraweeView mSdvSpecialPromo;
    // ClosedOn views
    private LightTextView mLtvMonday;
    private LightTextView mLtvTuesday;
    private LightTextView mLtvWednesday;
    private LightTextView mLtvThursday;
    private LightTextView mLtvFriday;
    private LightTextView mLtvSaturday;
    private LightTextView mLtvSunday;
    // Delivery
    private ImageView mIvDelivery;
    private LightTextView mLtvDelivery;
    // Location views
    private LightTextView mRtvLocationDescription;
    private Button mBtnLocation;
    // GeneralInfo views
    private LightTextView mltvSchedule;
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
                businessObject = extras.getParcelable(Const.iExtraBusiness);
                addAsContact = extras.getBoolean(Const.iExtraAddBusiness);
            }
        } else {
            businessObject = savedInstanceState.getParcelable(Const.iExtraBusiness);
            addAsContact = savedInstanceState.getBoolean(Const.iExtraAddBusiness);
        }

        initialization();
    }

    @Override
    public void onNewIntent(Intent intent) {
        this.setIntent(intent);
    }

    @Override
    protected void initialization() {
        super.initialization();

        if (businessObject == null) {
            finish();
        }

        rgb = -1;
        jobManager = ConversaApp.getInstance(this).getJobManager();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        FrameLayout mFlBack = (FrameLayout) toolbar.findViewById(R.id.flBack);
        ImageButton mIbBack = (ImageButton) toolbar.findViewById(R.id.ibBack);
        ImageButton mShareButton = (ImageButton) toolbar.findViewById(R.id.ibShare);
        MediumTextView mTitle = (MediumTextView) toolbar.findViewById(R.id.mtvTitle);
        mFlBack.setOnClickListener(this);
        mIbBack.setOnClickListener(this);
        mShareButton.setOnClickListener(this);
        mTitle.setText(businessObject.getDisplayName());
        setSupportActionBar(toolbar);

        mRlProfileHeader = (RelativeLayout) findViewById(R.id.rlProfileHeader);
        mLlSpecialPromoContainer = (LinearLayout) findViewById(R.id.llSpecialPromoContainer);
        mLlClosedOnContainer = (LinearLayout) findViewById(R.id.llClosedOnContainer);
        mLlScheduleContainer = (LinearLayout) findViewById(R.id.llScheduleContainer);
        mLlDeliveryContainer = (LinearLayout) findViewById(R.id.llDeliveryContainer);
        mLlLinkContainer = (LinearLayout) findViewById(R.id.llLinkContainer);
        mLlContactNumberContainer = (LinearLayout) findViewById(R.id.llContactNumberContainer);
        mLlAddressContainer = (LinearLayout) findViewById(R.id.llAddressContainer);
        // Profile views
        View mVNameContainer = findViewById(R.id.vNameContainer);
        mSdvBusinessImage = (SimpleDraweeView) findViewById(R.id.sdvBusinessImage);
        mBtnFavorite = (LikeButton) findViewById(R.id.btnFavorite);
        mBtvFollowers = (BoldTextView) findViewById(R.id.btvFollowers);
        mIvStatus = findViewById(R.id.vStatus);
        mIvStatus.setVisibility(View.GONE);
        Button mBtnStartChat = (Button) findViewById(R.id.btnStartChat);
        BoldTextView mBtvConversaId = (BoldTextView) findViewById(R.id.btvConversaId);
        RegularTextView mMtvBusinessName = (RegularTextView) findViewById(R.id.rtvBusinessName);
        // Special promo
        mRtvSpecialPromo = (RegularTextView) findViewById(R.id.rtvSpecialPromo);
        mSdvSpecialPromo = (SimpleDraweeView) findViewById(R.id.sdvSpecialPromo);
        // ClosedOn views
        mLtvMonday = (LightTextView) findViewById(R.id.ltvMonday);
        mLtvTuesday = (LightTextView) findViewById(R.id.ltvTuesday);
        mLtvWednesday = (LightTextView) findViewById(R.id.ltvWednesday);
        mLtvThursday = (LightTextView) findViewById(R.id.ltvThursday);
        mLtvFriday = (LightTextView) findViewById(R.id.ltvFriday);
        mLtvSaturday = (LightTextView) findViewById(R.id.ltvSaturday);
        mLtvSunday = (LightTextView) findViewById(R.id.ltvSunday);
        // Delivery views
        mIvDelivery = (ImageView) findViewById(R.id.ivDelivery);
        mLtvDelivery = (LightTextView) findViewById(R.id.ltvDelivery);
        // Location views
        mRtvLocationDescription = (LightTextView) findViewById(rtvLocationDescription);
        mBtnLocation = (Button) findViewById(R.id.btnLocation);
        // GeneralInfo views
        mltvSchedule = (LightTextView) findViewById(R.id.ltvSchedule);
        mltvLink = (LightTextView) findViewById(R.id.ltvLink);
        mltvContactNumber = (LightTextView) findViewById(R.id.ltvContactNumber);

        mMtvBusinessName.setText(businessObject.getDisplayName());
        mBtvConversaId.setText(businessObject.getConversaId());

        Uri uri = Utils.getUriFromString(businessObject.getAvatarThumbFileId());

        if (uri == null) {
            uri = Utils.getDefaultImage(this, R.drawable.business_default);
        }

        Postprocessor redMeshPostprocessor = new BasePostprocessor() {
            @Override
            public String getName() {
                return "redMeshPostprocessor";
            }

            @Override
            public void process(Bitmap bitmap) {
                if (bitmap == null) {
                    bitmap = BitmapFactory.decodeResource(getResources(),
                            R.drawable.business_default);
                    rgb = -2;
                }

                Palette.from(bitmap).generate(paletteListener);
            }
        };

        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                .setPostprocessor(redMeshPostprocessor)
                .build();

        PipelineDraweeController controller = (PipelineDraweeController)
                Fresco.newDraweeControllerBuilder()
                        .setImageRequest(request)
                        .setOldController(mSdvBusinessImage.getController())
                        .build();
        mSdvBusinessImage.setController(controller);

        mVNameContainer.setOnClickListener(this);
        mBtnFavorite.setOnLikeListener(this);
        mBtnStartChat.setOnClickListener(this);
        mBtnLocation.setOnClickListener(this);
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

        mLlScheduleContainer.setOnClickListener(this);
        mLlAddressContainer.setOnClickListener(this);

        // Call Parse for registry
        HashMap<String, String> params = new HashMap<>(1);
        params.put("business", businessObject.getBusinessId());
        ParseCloud.callFunctionInBackground("profileInfo", params, new FunctionCallback<String>() {
            @Override
            public void done(String result, ParseException e) {
                if(e == null) {
                    parseResult(result);
                } else {
                    AppActions.validateParseException(getApplicationContext(), e);
                    parseResult("");
                }
            }
        });
    }

    Palette.PaletteAsyncListener paletteListener = new Palette.PaletteAsyncListener() {
        public void onGenerated(Palette palette) {
            if (palette.getLightVibrantSwatch() != null) {
                rgb = palette.getLightVibrantSwatch().getRgb();
            } else if (palette.getLightMutedSwatch() != null) {
                rgb = palette.getLightMutedSwatch().getRgb();
            } else {
                rgb = -1;
            }

            if (rgb != -1 && rgb != -2) {
                if (android.os.Build.VERSION.SDK_INT >= 21) {
                    Window window = getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    window.setStatusBarColor(rgb);
                }

                toolbar.setBackgroundColor(rgb);
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        Branch branch = Branch.getInstance();
        branch.initSession(new Branch.BranchReferralInitListener(){
            @Override
            public void onInitFinished(JSONObject referringParams, BranchError error) {
                if (error == null) {
                    // params are the deep linked params associated with the link that the user clicked -> was re-directed to this app
                    // params will be empty if no data found
                    // ... insert custom logic here ...
                } else {
                    Logger.error("MyApp", error.getMessage());
                }
            }
        }, this.getIntent().getData(), this);
    }

    @Override
    public void onBackPressed() {
        navigateUp();
    }

    private void navigateUp() {
        Intent upIntent = NavUtils.getParentActivityIntent(this);
        if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
            // This activity is NOT part of this app's task, so create a new task
            // when navigating up, with a synthesized back stack.
            TaskStackBuilder.create(this)
                    // Add all of this activity's parents to the back stack
                    .addNextIntentWithParentStack(upIntent)
                    // Navigate up to the closest parent
                    .startActivities();
        } else {
            // This activity is part of this app's task, so simply
            // navigate up to the logical parent activity.
            NavUtils.navigateUpTo(this, upIntent);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.vNameContainer: {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                LayoutInflater inflater = getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.dialog_profile_status, null);
                dialogBuilder.setView(dialogView);
                dialogBuilder.setPositiveButton(getString(R.string.dialog_profile_status_understood), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
                AlertDialog b = dialogBuilder.create();
                b.show();
                break;
            }
            case R.id.btnLocation: {

                break;
            }
            case R.id.ibShare: {
                Branch.getInstance(getApplicationContext()).userCompletedAction(BranchEvent.SHARE_STARTED);

                BranchUniversalObject branchUniversalObject = new BranchUniversalObject()
                        .setCanonicalIdentifier("item/12345")
                        .setTitle("My Content Title")
                        .setContentDescription("My Content Description")
                        .setContentImageUrl("https://example.com/mycontent-12345.png")
                        .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                        .addContentMetadata("property1", "blue")
                        .addContentMetadata("property2", "red");

                LinkProperties linkProperties = new LinkProperties()
                        .setChannel("facebook")
                        .setFeature("sharing")
                        .addControlParameter("$desktop_url", "http://example.com/home")
                        .addControlParameter("$ios_url", "http://example.com/ios");

//                branchUniversalObject.generateShortUrl(this, linkProperties, new Branch.BranchLinkCreateListener() {
//                    @Override
//                    public void onLinkCreate(String url, BranchError error) {
//                        if (error == null) {
//                            Logger.error("MyApp", "got my Branch link to share: " + url);
//                        }
//                    }
//                });

                ShareSheetStyle shareSheetStyle = new ShareSheetStyle(this, "Check this out!", "This stuff is awesome: ")
                        .setCopyUrlStyle(getResources().getDrawable(android.R.drawable.ic_menu_send), "Copy", "Added to clipboard")
                        .setMoreOptionStyle(getResources().getDrawable(android.R.drawable.ic_menu_search), "Show more")
                        .addPreferredSharingOption(SharingHelper.SHARE_WITH.FACEBOOK)
                        .addPreferredSharingOption(SharingHelper.SHARE_WITH.EMAIL)
                        .setAsFullWidthStyle(true)
                        .setSharingTitle("Share With");

                branchUniversalObject.showShareSheet(this,
                        linkProperties,
                        shareSheetStyle,
                        new Branch.BranchLinkShareListener() {
                            @Override
                            public void onShareLinkDialogLaunched() {
                            }
                            @Override
                            public void onShareLinkDialogDismissed() {
                            }
                            @Override
                            public void onLinkShareResponse(String sharedLink, String sharedChannel, BranchError error) {
                            }
                            @Override
                            public void onChannelSelected(String channelName) {
                            }
                        });

                break;
            }
            case R.id.flBack:
            case R.id.ibBack: {
                navigateUp();
                break;
            }
            case R.id.btnStartChat: {
                Intent intent = new Intent(this, ActivityChatWall.class);
                intent.putExtra(Const.iExtraBusiness, businessObject);
                intent.putExtra(Const.iExtraAddBusiness, addAsContact);
                startActivity(intent);
                break;
            }
            case R.id.llScheduleContainer: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.profile_conversa_time_info_message));

                String positiveText = getString(android.R.string.ok);
                builder.setPositiveButton(positiveText,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            }
            case R.id.llAddressContainer: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.profile_locations_info_message));

                String positiveText = getString(android.R.string.ok);
                builder.setPositiveButton(positiveText,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Make sure the request was successful
        if (resultCode == RESULT_OK) {
            // Check which request we're responding to
            switch (requestCode) {
                case ActivityProfile.PLACE_PICKER_FLAG: {

                    break;
                }
            }
        }
    }

    @Override
    public void liked(final LikeButton likeButton) {
        jobManager.addJobInBackground(new FavoriteJob(TAG, businessObject.getBusinessId(), true));
        followers++;
        mBtvFollowers.setText(String.valueOf(followers));
        likeButton.setLiked(true);
    }

    @Override
    public void unLiked(final LikeButton likeButton) {
        jobManager.addJobInBackground(new FavoriteJob(TAG, businessObject.getBusinessId(), false));
        followers--;
        mBtvFollowers.setText(String.valueOf(followers));
        likeButton.setLiked(false);
    }

    private void parseResult(String result) {
        try {
            if (result.isEmpty()) {
                mBtnFavorite.setLiked(false);
            } else {
                JSONObject jsonRootObject = new JSONObject(result);

                followers = jsonRootObject.optInt("followers", 0);
                String daySpecial = jsonRootObject.optString("daySpecial", null);
                String website = jsonRootObject.optString("website", null);
                boolean delivery = jsonRootObject.optBoolean("delivery", false);
                JSONArray openOn = jsonRootObject.optJSONArray("openOn");
                String number = jsonRootObject.optString("number", null);
                boolean multiple = jsonRootObject.optBoolean("multiple", false);
                boolean online = jsonRootObject.optBoolean("online", false);
                String promo = jsonRootObject.optString("promo", null);
                String promoTextColor = jsonRootObject.optString("promoColor", null);
                String promoBackground = jsonRootObject.optString("promoBack", null);
                JSONArray tags = jsonRootObject.optJSONArray("tags");
                boolean verified = jsonRootObject.optBoolean("verified", false);
                long since = jsonRootObject.optLong("since", 0L);
                boolean favorite = jsonRootObject.optBoolean("favorite", false);
                int status = jsonRootObject.optInt("status", 0);

                if (promo != null || promoBackground != null) {
                    mLlSpecialPromoContainer.setVisibility(View.VISIBLE);

                    if (promo != null) {
                        mRtvSpecialPromo.setVisibility(View.VISIBLE);
                        mSdvSpecialPromo.setVisibility(View.VISIBLE);

                        if (promoTextColor != null) {
                            try {
                                mRtvSpecialPromo.setTextColor(Color.parseColor(promoTextColor));
                            } catch (IllegalArgumentException e) {
                                mRtvSpecialPromo.setTextColor(Color.WHITE);
                            }
                        }
                    }

                    if (promoBackground != null) {
                        mSdvSpecialPromo.setVisibility(View.VISIBLE);

                        Uri uri;

                        if(promoBackground.isEmpty()) {
                            uri = Utils.getDefaultImage(this, R.drawable.specialpromo_dropshadow);
                        } else {
                            uri = Uri.parse(promoBackground);
                        }

                        mSdvSpecialPromo.setImageURI(uri);
                    }
                }

                // Status
                GradientDrawable shapeDrawable;

                switch (status) {
                    case 1: {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            shapeDrawable = (GradientDrawable) getDrawable(R.drawable.circular_status_online);
                        } else {
                            shapeDrawable = (GradientDrawable) getResources().getDrawable(R.drawable.circular_status_online);
                        }
                        break;
                    }
                    case 2: {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            shapeDrawable = (GradientDrawable) getDrawable(R.drawable.circular_status_offline);
                        } else {
                            shapeDrawable = (GradientDrawable) getResources().getDrawable(R.drawable.circular_status_offline);
                        }
                        break;
                    }
                    default: {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            shapeDrawable = (GradientDrawable) getDrawable(R.drawable.circular_status_away);
                        } else {
                            shapeDrawable = (GradientDrawable) getResources().getDrawable(R.drawable.circular_status_away);
                        }
                        break;
                    }
                }

                mIvStatus.setVisibility(View.VISIBLE);
                mIvStatus.setBackground(shapeDrawable);

                // Iterator
                int i, size;

                // Get open days
                if (openOn != null) {
                    size = openOn.length();
                    for (i = 0; i < size; i++) {
                        GradientDrawable drawable;
                        switch (openOn.getInt(i)) {
                            case 1:
                                drawable = (GradientDrawable) mLtvMonday.getBackground();
                                drawable.setStroke(1, Color.GREEN);
                                break;
                            case 2:
                                drawable = (GradientDrawable) mLtvTuesday.getBackground();
                                drawable.setStroke(1, Color.GREEN);
                                break;
                            case 3:
                                drawable = (GradientDrawable) mLtvWednesday.getBackground();
                                drawable.setStroke(1, Color.GREEN);
                                break;
                            case 4:
                                drawable = (GradientDrawable) mLtvThursday.getBackground();
                                drawable.setStroke(1, Color.GREEN);
                                break;
                            case 5:
                                drawable = (GradientDrawable) mLtvFriday.getBackground();
                                drawable.setStroke(1, Color.GREEN);
                                break;
                            case 6:
                                drawable = (GradientDrawable) mLtvSaturday.getBackground();
                                drawable.setStroke(1, Color.GREEN);
                                break;
                            case 7:
                                drawable = (GradientDrawable) mLtvSunday.getBackground();
                                drawable.setStroke(1, Color.GREEN);
                                break;
                        }
                    }
                }

                mBtnFavorite.setLiked(favorite);
                mBtvFollowers.setText(String.valueOf(followers));

                if (website != null) {
                    mltvLink.setText(website);
                } else {
                    mltvLink.setText(R.string.profile_no_website_message);
                }

                if (number != null) {
                    mltvContactNumber.setText(number);
                } else {
                    mltvContactNumber.setText(R.string.profile_no_number_message);
                }

                if (delivery) {
                    mIvDelivery.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check));
                    mLtvDelivery.setText(getString(R.string.profile_delivery_yes));
                } else {
                    mIvDelivery.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_cancel));
                    mLtvDelivery.setText(getString(R.string.profile_delivery_no));
                }

                if (multiple) {
                    // Multiple locations
                    mRtvLocationDescription.setText(R.string.profile_location_multiple_location);
                    //mBtnLocation.setVisibility(View.VISIBLE);
                } else if (online) {
                    // Just online
                    mRtvLocationDescription.setText(R.string.profile_location_online_location);
                    mBtnLocation.setVisibility(View.GONE);
                } else {
                    // One location
                    mRtvLocationDescription.setText(R.string.profile_location_one_location);
                    //mBtnLocation.setVisibility(View.VISIBLE);
                }
            }
        } catch (JSONException e) {
            Logger.error("parseResult", e.getMessage());
        } finally {
            mBtnFavorite.setEnabled(true);
        }
    }

}