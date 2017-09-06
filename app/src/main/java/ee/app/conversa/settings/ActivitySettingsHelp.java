package ee.app.conversa.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.parse.ParseCloud;
import com.parse.ParseException;

import org.json.JSONObject;

import java.util.HashMap;

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
        findViewById(R.id.rlSupport).setOnClickListener(this);
        findViewById(R.id.rlTerms).setOnClickListener(this);
        findViewById(R.id.rlAgent).setOnClickListener(this);
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
            case R.id.rlSupport: {
                final Context context = this;
                new MaterialDialog.Builder(this)
                        .title(R.string.sett_help_dialog_support_title)
                        .content(R.string.sett_help_dialog_support_message)
                        .progress(true, 0)
                        .progressIndeterminateStyle(true)
                        .showListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(final DialogInterface dialogInterface) {
                                new SupportInfoTask(context, dialogInterface).execute("1");
                            }
                        })
                        .show();
                break;
            }
            case R.id.rlAgent: {
                final Context context = this;
                new MaterialDialog.Builder(this)
                        .title(R.string.sett_help_dialog_agent_title)
                        .content(R.string.sett_help_dialog_agent_message)
                        .progress(true, 0)
                        .progressIndeterminateStyle(true)
                        .showListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(final DialogInterface dialogInterface) {
                                new SupportInfoTask(context, dialogInterface).execute("2");
                            }
                        })
                        .show();
                break;
            }
            case R.id.rlTerms: {
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
                        Uri.parse("http://conversachat.com/terms"),
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
            //SnackbarFactory.createSnackbar(ActivityTutorial.this, mLayoutMainCoordinator, "Connected to service").show();
        }

        @Override
        public void onCustomTabsDisconnected() {
            //SnackbarFactory.createSnackbar(ActivityTutorial.this, mLayoutMainCoordinator, "Disconnected from service").show();
        }
    };

    private class SupportInfoTask extends AsyncTask<String, Void, dbBusiness> {

        private Context context;
        private DialogInterface dialogInterface;
        private boolean add;

        public SupportInfoTask (Context context, DialogInterface dialogInterface) {
            this.dialogInterface = dialogInterface;
            this.context = context;
        }

        @Override
        protected dbBusiness doInBackground(String... params) {
            try {
                HashMap<String, Object> pparams = new HashMap<>(2);
                pparams.put("customer", 1);
                pparams.put("purpose", Integer.parseInt(params[0]));
                final String supportId = ParseCloud.callFunction("getConversaAccountId", pparams);

                add = false;

                dbBusiness dbBusiness = ConversaApp
                        .getInstance(getApplicationContext())
                        .getDB()
                        .isContact(supportId);

                if (dbBusiness == null) {
                    add = true;

                    HashMap<String, Object> sparams = new HashMap<>(1);
                    sparams.put("accountId", supportId);

                    final String json = ParseCloud.callFunction("getConversaAccount", sparams);

                    JSONObject businessReg = new JSONObject(json);
                    dbBusiness = new dbBusiness();
                    dbBusiness.setBusinessId(businessReg.getString("oj"));
                    dbBusiness.setDisplayName(businessReg.getString("dn"));
                    dbBusiness.setConversaId(businessReg.getString("id"));
                    dbBusiness.setAvatarThumbFileId(businessReg.getString("av"));
                }

                return dbBusiness;
            } catch (Exception e) {
                if (e instanceof ParseException) {
                    if (AppActions.validateParseException((ParseException)e)) {
                        AppActions.appLogout(getApplicationContext(), true);
                        cancel(true);
                    }
                }

                return null;
            }
        }

        @Override
        protected void onPostExecute(dbBusiness result) {
            dialogInterface.dismiss();

            if (isCancelled() || isDestroyed() || isFinishing())
                return;

            if (result == null) {
                new MaterialDialog.Builder(context)
                        .content(R.string.sett_help_dialog_message_error)
                        .positiveText(android.R.string.ok)
                        .positiveColorRes(R.color.black)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            } else {
                Intent intent = new Intent(context, ActivityProfile.class);
                intent.putExtra(Const.iExtraAddBusiness, add);
                intent.putExtra(Const.iExtraBusiness, result);
                startActivity(intent);
            }
        }

    }

}
