package ee.app.conversa.settings;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.BSD3ClauseLicense;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;
import ee.app.conversa.ActivityProfile;
import ee.app.conversa.ConversaApp;
import ee.app.conversa.R;
import ee.app.conversa.browser.CustomTabActivityHelper;
import ee.app.conversa.browser.WebviewFallback;
import ee.app.conversa.extendables.ConversaActivity;
import ee.app.conversa.model.database.dbBusiness;
import ee.app.conversa.model.parse.Business;
import ee.app.conversa.utils.AppActions;
import ee.app.conversa.utils.Const;

/**
 * Created by edgargomez on 10/10/16.
 */

public class ActivitySettingsHelp extends ConversaActivity implements View.OnClickListener {

    private CustomTabActivityHelper mCustomTabActivityHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_help);
        initialization();
    }

    @Override
    protected void initialization() {
        super.initialization();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.preferences__help);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.btnLicences).setOnClickListener(this);
        findViewById(R.id.btnSupport).setOnClickListener(this);
        findViewById(R.id.btnTerms).setOnClickListener(this);
        findViewById(R.id.btnFeedback).setOnClickListener(this);
        mCustomTabActivityHelper = new CustomTabActivityHelper();
        mCustomTabActivityHelper.setConnectionCallback(mConnectionCallback);
        mCustomTabActivityHelper.mayLaunchUrl(Uri.parse("http://conversachat.com"), null, null);
    }

    @Override
    public void onStart() {
        super.onStart();
        mCustomTabActivityHelper.bindCustomTabsService(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mCustomTabActivityHelper.unbindCustomTabsService(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLicences: {
                onLicencesClick();
                break;
            }
            case R.id.btnSupport: {
                dbBusiness dbBusiness = ConversaApp.getInstance(this).getDB().isContact("E5ZE2sr0tx");
                final Intent intent = new Intent(this, ActivityProfile.class);

                if (dbBusiness == null) {
                    new MaterialDialog.Builder(this)
                            .title(R.string.sett_help_dialog_title)
                            .content(R.string.sett_help_dialog_message)
                            .progress(true, 0)
                            .progressIndeterminateStyle(true)
                            .showListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(final DialogInterface dialogInterface) {
                                    ParseQuery<Business> query = ParseQuery.getQuery(Business.class);
                                    query.whereEqualTo(Const.kBusinessActiveKey, true);
                                    query.whereEqualTo(Const.kBusinessCountryKey, ParseObject.createWithoutData("Country", "QZ31UNerIj"));
                                    query.whereDoesNotExist(Const.kBusinessBusinessKey);

                                    query.getInBackground("E5ZE2sr0tx", new GetCallback<Business>() {
                                        @Override
                                        public void done(Business object, ParseException e) {
                                            dialogInterface.dismiss();

                                            if (e == null) {
                                                if (isFragmentActive()) {
                                                    dbBusiness dbBusiness = new dbBusiness();
                                                    dbBusiness.setBusinessId("E5ZE2sr0tx");
                                                    dbBusiness.setDisplayName(object.getDisplayName());
                                                    dbBusiness.setConversaId(object.getConversaID());
                                                    dbBusiness.setAbout(object.getAbout());

                                                    if (object.getAvatar() != null)
                                                        dbBusiness.setAvatarThumbFileId(object.getAvatar().getUrl());

                                                    intent.putExtra(Const.iExtraAddBusiness, true);
                                                    intent.putExtra(Const.iExtraBusiness, dbBusiness);
                                                    startActivity(intent);
                                                }
                                            } else {
                                                AppActions.validateParseException(getApplicationContext(), e);
                                            }
                                        }
                                    });
                                }
                            })
                            .show();
                } else {
                    intent.putExtra(Const.iExtraAddBusiness, false);
                    intent.putExtra(Const.iExtraBusiness, dbBusiness);
                    startActivity(intent);
                }
                break;
            }
            case R.id.btnTerms: {
                // create an intent builder
                CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
                // Begin customizing
                intentBuilder.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary));
                intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
                intentBuilder.setShowTitle(true);
                intentBuilder.setCloseButtonIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_arrow_back));
                intentBuilder.setStartAnimations(this, R.anim.slide_in_right, R.anim.slide_out_left);
                intentBuilder.setExitAnimations(this, android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right);

                CustomTabActivityHelper.openCustomTab(
                        this,
                        intentBuilder.build(),
                        Uri.parse("http://conversachat.com"),
                        new WebviewFallback());
                break;
            }
            case R.id.btnFeedback: {
                // create an intent builder
                CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
                // Begin customizing
                intentBuilder.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary));
                intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
                intentBuilder.setShowTitle(true);
                intentBuilder.setCloseButtonIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_arrow_back));
                intentBuilder.setStartAnimations(this, R.anim.slide_in_right, R.anim.slide_out_left);
                intentBuilder.setExitAnimations(this, android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right);

                CustomTabActivityHelper.openCustomTab(
                        this,
                        intentBuilder.build(),
                        Uri.parse("http://conversachat.com"),
                        new WebviewFallback());
                break;
            }
        }
    }

    public void onLicencesClick() {
        try {
            final Notices notices = new Notices();
            notices.addNotice(new Notice(
                    "AVLoadingIndicatorView",
                    "https://github.com/81813780/AVLoadingIndicatorView",
                    "Copyright 2015 jack wang",
                    new ApacheSoftwareLicense20())
            );
            notices.addNotice(new Notice(
                    "EventBus",
                    "http://greenrobot.org/eventbus/",
                    "Copyright (C) 2012-2016 Markus Junginger, greenrobot",
                    new ApacheSoftwareLicense20())
            );
            notices.addNotice(new Notice(
                    "PhotoDraweeView",
                    "https://github.com/ongakuer/PhotoDraweeView",
                    "Copyright 2015-2016 Relex",
                    new ApacheSoftwareLicense20())
            );
            notices.addNotice(new Notice(
                    "OkHttp",
                    "http://square.github.io/okhttp/",
                    "Copyright 2016 Square, Inc.",
                    new ApacheSoftwareLicense20())
            );
            notices.addNotice(new Notice(
                    "BugShaker",
                    "https://github.com/stkent/bugshaker-android",
                    "Copyright 2016 Stuart Kent",
                    new ApacheSoftwareLicense20())
            );
            notices.addNotice(new Notice(
                    "LikeButton",
                    "https://github.com/jd-alexander/LikeButton",
                    "Copyright 2016 Joel Dean",
                    new ApacheSoftwareLicense20())
            );
            notices.addNotice(new Notice(
                    "Android Priority Job Queue",
                    "https://github.com/yigit/android-priority-jobqueue",
                    "yigit",
                    new MITLicense())
            );
            notices.addNotice(new Notice(
                    "Floating Search View",
                    "https://github.com/arimorty/floatingsearchview",
                    "Copyright (C) 2015 Ari C.",
                    new ApacheSoftwareLicense20())
            );
            notices.addNotice(new Notice(
                    "Fresco",
                    "http://frescolib.org",
                    "Copyright (c) 2015-present, Facebook, Inc. All rights reserved.",
                    new BSD3ClauseLicense())
            );

            new LicensesDialog.Builder(this)
                    .setNotices(notices)
                    .setIncludeOwnLicense(true)
                    .build()
                    .showAppCompat();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private CustomTabActivityHelper.ConnectionCallback mConnectionCallback = new CustomTabActivityHelper.ConnectionCallback() {
        @Override
        public void onCustomTabsConnected() {
            //SnackbarFactory.createSnackbar(MainActivity.this, mLayoutMainCoordinator, "Connected to service").show();
        }

        @Override
        public void onCustomTabsDisconnected() {
            //SnackbarFactory.createSnackbar(MainActivity.this, mLayoutMainCoordinator, "Disconnected from service").show();
        }
    };

    public boolean isFragmentActive() {
        return !isFinishing();
    }

}
