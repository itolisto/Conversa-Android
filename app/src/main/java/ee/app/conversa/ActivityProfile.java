package ee.app.conversa;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;

import com.birbit.android.jobqueue.JobManager;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;
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
import ee.app.conversa.utils.AppActions;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Logger;
import ee.app.conversa.utils.Utils;
import ee.app.conversa.view.MediumTextView;
import ee.app.conversa.view.RegularTextView;
import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.SharingHelper;
import io.branch.referral.util.BranchEvent;
import io.branch.referral.util.LinkProperties;
import io.branch.referral.util.ShareSheetStyle;

/**
 * Created by edgargomez on 11/24/16.
 */

public class ActivityProfile extends ConversaActivity implements View.OnClickListener {

    private final String TAG = ActivityProfile.class.getSimpleName();

    private static final TimeInterpolator sDecelerator = new DecelerateInterpolator();
    private static final TimeInterpolator sAccelerator = new AccelerateInterpolator();
    static float sAnimatorScale = 2;

    // General
    private ColorDrawable mBackground;
    private CardView mCvContainer;
    private View mVwStatus;
    private dbBusiness businessObject;
    private JobManager jobManager;
    private boolean addAsContact;
    private boolean liked;
    private int followers;
    private int rgb;
    private final int ANIM_DURATION = 500;
    // Profile views
    private SimpleDraweeView mSdvBusinessHeader;
    private RegularTextView mBtvFollowers;
    // Action buttons
    private Button mBtnFavorite;

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

        // Only run the animation if we're coming from the parent activity, not if
        // we're recreated automatically by the window manager (e.g., device rotation)
        if (savedInstanceState == null) {
            ViewTreeObserver observer = mCvContainer.getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mCvContainer.getViewTreeObserver().removeOnPreDrawListener(this);
                    runEnterAnimation();
                    return true;
                }
            });
        }
    }

    /**
     * The enter animation scales the picture in from its previous thumbnail
     * size/location, colorizing it in parallel. In parallel, the background of the
     * activity is fading in. When the pictue is in place, the text description
     * drops down.
     */
    public void runEnterAnimation() {
        final long duration = (long) (ANIM_DURATION * sAnimatorScale) / 2;

        // Set starting values for properties we're going to animate. These
        // values scale and position the full size version down to the thumbnail
        // size/location, from which we'll animate it back up
//        mCvContainer.setPivotX(0);
//        mCvContainer.setPivotY(0);
//        mCvContainer.setScaleX(1);
//        mCvContainer.setScaleY(1);
//        mCvContainer.setTranslationX(0);
//        mCvContainer.setTranslationY(0);

        // Animate scale and translation to go from thumbnail to full size
        mCvContainer.animate().setDuration(duration).
                scaleX(1).scaleY(1).
                translationX(0).translationY(0).
                setInterpolator(sDecelerator);

        // Fade in the black background
        ObjectAnimator bgAnim = ObjectAnimator.ofInt(mBackground, "alpha", 0, 255);
        bgAnim.setDuration(duration);
        bgAnim.start();
    }

    /**
     * The exit animation is basically a reverse of the enter animation, except that if
     * the orientation has changed we simply scale the picture back into the center of
     * the screen.
     *
     * @param endAction This action gets run after the animation completes (this is
     * when we actually switch activities)
     */
    public void runExitAnimation(final Runnable endAction) {
        final long duration = (long) (ANIM_DURATION * sAnimatorScale) / 8;

        // Fade out background
        ObjectAnimator bgAnim = ObjectAnimator.ofInt(mBackground, "alpha", 0);
        bgAnim.setDuration(duration);
        bgAnim.start();

        // First, slide/fade text out of the way
        // Animate image back to thumbnail size/location
        mCvContainer.animate().setDuration(duration).
                scaleX(1).scaleY(1).
                translationX(0).translationY(0).
                setInterpolator(sAccelerator).
                withEndAction(endAction);
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
            return;
        }

        rgb = -3;
        liked = false;

        jobManager = ConversaApp.getInstance(this).getJobManager();
        mCvContainer = (CardView) findViewById(R.id.cvContainer);
        mVwStatus = findViewById(R.id.vStatus);

        //mCvContainer.setClipToOutline(true);

        mBackground = new ColorDrawable(ResourcesCompat.getColor(getResources(),
                R.color.profile_light_background, null));
        findViewById(R.id.topLevelLayout).setBackground(mBackground);
        findViewById(R.id.topLevelLayout).setOnClickListener(this);

        // Profile views
        mSdvBusinessHeader = (SimpleDraweeView) findViewById(R.id.sdvProfileHeader);
        SimpleDraweeView mSdvBusinessImage = (SimpleDraweeView) findViewById(R.id.sdvProfileAvatar);
        MediumTextView mMtvBusinessName = (MediumTextView) findViewById(R.id.mtvBusinessName);
        RegularTextView mBtvConversaId = (RegularTextView) findViewById(R.id.rtvConversaId);
        mBtvFollowers = (RegularTextView) findViewById(R.id.rtvFollowers);
        // Action buttons
        mBtnFavorite = (Button) findViewById(R.id.btnFavorite);

        mMtvBusinessName.setText(businessObject.getDisplayName());
        mBtvConversaId.setText("@".concat(businessObject.getConversaId()));

        Uri uri = Utils.getUriFromString(businessObject.getAvatarThumbFileId());

        if (uri == null) {
            uri = Utils.getDefaultImage(this, R.drawable.ic_business_default);
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
                            R.drawable.ic_business_default);
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

        mBtnFavorite.setOnClickListener(this);
        findViewById(R.id.btnStartChat).setOnClickListener(this);
        findViewById(R.id.btnShare).setOnClickListener(this);
        findViewById(R.id.btnCloseProfile).setOnClickListener(this);
        mBtnFavorite.setEnabled(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBtnFavorite.setBackground
                    (getResources().getDrawable(R.drawable.ic_fav_not, null));
        } else {
            mBtnFavorite.setBackground
                    (getResources().getDrawable(R.drawable.ic_fav_not));
        }

        // Call Parse for registry
        HashMap<String, String> params = new HashMap<>(1);
        params.put("business", businessObject.getBusinessId());
        ParseCloud.callFunctionInBackground("getBusinessProfile", params, new FunctionCallback<String>() {
            @Override
            public void done(String result, ParseException e) {
                if(e == null) {
                    parseResult(result);
                } else {
                    if (AppActions.validateParseException(e)) {
                        AppActions.appLogout(getApplicationContext(), true);
                    } else {
                        parseResult("");
                    }
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.topLevelLayout: {
                onBackPressed();
                break;
            }
            case R.id.btnFavorite: {
                if (liked) {
                    liked = false;

                    jobManager.addJobInBackground(new FavoriteJob(TAG, businessObject.getBusinessId(), false));
                    followers--;
                    mBtvFollowers.setText(String.valueOf(followers));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mBtnFavorite.setBackground
                                (getResources().getDrawable(R.drawable.ic_fav_not, null));
                    } else {
                        mBtnFavorite.setBackground
                                (getResources().getDrawable(R.drawable.ic_fav_not));
                    }
                } else {
                    liked = true;

                    jobManager.addJobInBackground(new FavoriteJob(TAG, businessObject.getBusinessId(), true));
                    followers++;
                    mBtvFollowers.setText(String.valueOf(followers));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mBtnFavorite.setBackground
                                (getResources().getDrawable(R.drawable.ic_fav, null));
                    } else {
                        mBtnFavorite.setBackground
                                (getResources().getDrawable(R.drawable.ic_fav));
                    }
                }
                break;
            }
            case R.id.btnStartChat: {
                Intent intent = new Intent(this, ActivityChatWall.class);
                intent.putExtra(Const.iExtraBusiness, businessObject);
                intent.putExtra(Const.iExtraAddBusiness, addAsContact);
                startActivity(intent);
                break;
            }
            case R.id.btnShare: {
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
            case R.id.btnCloseProfile: {
                onBackPressed();
                break;
            }
        }
    }

    /**
     * Overriding this method allows us to run our exit animation first, then exiting
     * the activity when it is complete.
     */
    @Override
    public void onBackPressed() {
        runExitAnimation(new Runnable() {
            public void run() {
                // *Now* go ahead and exit the activity
                finish();
            }
        });
    }

    private void parseResult(String result) {
        try {
            if (!result.isEmpty()) {
                JSONObject jsonRootObject = new JSONObject(result);

                followers = jsonRootObject.optInt("followers", 0);
                String headerUrl = jsonRootObject.optString("header", null);
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

                liked = favorite;

                if (favorite) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mBtnFavorite.setBackground
                                (getResources().getDrawable(R.drawable.ic_fav, null));
                    } else {
                        mBtnFavorite.setBackground
                                (getResources().getDrawable(R.drawable.ic_fav));
                    }
                }

                mBtvFollowers.setText(String.valueOf(followers));

                Uri uri = Utils.getUriFromString(headerUrl);

                if (uri != null) {
                    mSdvBusinessHeader.setImageURI(uri);
                }

                GradientDrawable shapeDrawable;

                switch (status) {
                    case 0: {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            shapeDrawable = (GradientDrawable) getDrawable(R.drawable.circular_status_online);
                        } else {
                            shapeDrawable = (GradientDrawable) getResources().getDrawable(R.drawable.circular_status_online);
                        }
                        break;
                    }
                    case 1: {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            shapeDrawable = (GradientDrawable) getDrawable(R.drawable.circular_status_away);
                        } else {
                            shapeDrawable = (GradientDrawable) getResources().getDrawable(R.drawable.circular_status_away);
                        }
                        break;
                    }
                    default: {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            shapeDrawable = (GradientDrawable) getDrawable(R.drawable.circular_status_offline);
                        } else {
                            shapeDrawable = (GradientDrawable) getResources().getDrawable(R.drawable.circular_status_offline);
                        }
                        break;
                    }
                }

                mVwStatus.setBackground(shapeDrawable);
            }
        } catch (JSONException e) {
            Logger.error("parseResult", e.getMessage());
        } finally {
            mBtnFavorite.setEnabled(true);
        }
    }

}