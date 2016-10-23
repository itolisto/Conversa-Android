package ee.app.conversa.settings;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.BSD3ClauseLicense;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;
import ee.app.conversa.R;

/**
 * Created by edgargomez on 10/20/16.
 */

public class FragmentSettingsHelp extends Fragment implements View.OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_settings_help, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btnLicences).setOnClickListener(this);
        view.findViewById(R.id.help).setOnClickListener(this);
        view.findViewById(R.id.terms).setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ActivityPreferences)getActivity()).getSupportActionBar().setTitle(R.string.preferences__help);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLicences: {
                onMultipleFragmentClick();
                break;
            }
            case R.id.help: {

                break;
            }
            case R.id.terms: {

                break;
            }
        }
    }

    public void onMultipleFragmentClick() {
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

//            compile 'com.afollestad.material-dialogs:core:0.9.0.2'
//            compile 'com.github.dmytrodanylyk:android-morphing-button:98a4986e56'

            new LicensesDialog.Builder(getActivity())
                    .setNotices(notices)
                    .setIncludeOwnLicense(true)
                    .build()
                    .showAppCompat();
        } catch (Exception e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}