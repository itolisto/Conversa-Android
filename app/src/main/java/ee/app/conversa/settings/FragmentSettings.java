package ee.app.conversa.settings;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.R;

public class FragmentSettings extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_settings);
        this.findPreference(PreferencesKeys.PREFERENCE_MAIN_ACCOUNT)
                .setOnPreferenceClickListener(this);
        this.findPreference(PreferencesKeys.PREFERENCE_MAIN_CHATS)
                .setOnPreferenceClickListener(this);
        this.findPreference(PreferencesKeys.PREFERENCE_MAIN_NOTIFICATIONS)
                .setOnPreferenceClickListener(this);
        this.findPreference(PreferencesKeys.PREFERENCE_MAIN_SHARE)
                .setOnPreferenceClickListener(this);
        this.findPreference(PreferencesKeys.PREFERENCE_MAIN_LANGUAGE_KEY)
                .setOnPreferenceClickListener(this);
        this.findPreference(PreferencesKeys.PREFERENCE_MAIN_HELP)
                .setOnPreferenceClickListener(this);

        this.findPreference(PreferencesKeys.PREFERENCE_MAIN_LANGUAGE_KEY)
                .setSummary(ConversaApp.getInstance(getActivity()).getPreferences().getLanguageName());
    }

    @Override
    public void onStart() {
        super.onStart();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener((ActivityPreferences)getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ActivityPreferences)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((ActivityPreferences)getActivity()).getSupportActionBar().setTitle(R.string.settings);
    }

    @Override
    public void onStop() {
        super.onStop();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener((ActivityPreferences) getActivity());
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        Fragment fragment = null;

        switch (preference.getKey()) {
            case PreferencesKeys.PREFERENCE_MAIN_ACCOUNT:
                fragment = new FragmentSettingsAccount();
                break;
            case PreferencesKeys.PREFERENCE_MAIN_CHATS:
                fragment = new FragmentSettingsChat();
                break;
            case PreferencesKeys.PREFERENCE_MAIN_NOTIFICATIONS:
                fragment = new FragmentSettingsNotifications();
                break;
            case PreferencesKeys.PREFERENCE_MAIN_LANGUAGE_KEY:
                final int index;

                switch(ConversaApp.getInstance(getActivity()).getPreferences().getLanguage()) {
                    case "en":
                        index = 1;
                        break;
                    case "es":
                        index = 2;
                        break;
                    default:
                        index = 0;
                        break;
                }

                AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                b.setTitle(R.string.language_spinner_title);
                b.setSingleChoiceItems(R.array.language_entries, index, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (which != index)
                            ConversaApp.getInstance(getActivity()).getPreferences()
                                .setLanguage(getResources().getStringArray(R.array.language_values)[which]);
                    }
                });
                b.show();
                return true;
            case PreferencesKeys.PREFERENCE_MAIN_SHARE:
                final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setType("text/plain");
                // Add data to the intent, the receiving app will decide what to do with it.
                intent.putExtra(Intent.EXTRA_SUBJECT,
                        getActivity().getString(R.string.settings_using_conversa));
                intent.putExtra(Intent.EXTRA_TEXT,
                        getActivity().getString(R.string.settings_body_conversa));

                //startActivity(Intent.createChooser(intent, getActivity().getString(R.string.settings_share_conversa)));

                final List<ResolveInfo> activities = getActivity()
                        .getPackageManager().queryIntentActivities(intent, 0);

                List<String> appNames = new ArrayList<>(2);
                List<Drawable> appIcons = new ArrayList<>(2);
                for (ResolveInfo info : activities) {
                    appNames.add(info.loadLabel(getActivity().getPackageManager()).toString());
                    String packageName = info.activityInfo.packageName;

                    try {
                        Drawable icon = getActivity().getPackageManager().getApplicationIcon(packageName);
                        appIcons.add(icon);
                    } catch (PackageManager.NameNotFoundException e) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            appIcons.add(getResources().getDrawable(R.drawable.business_default, null));
                        } else {
                            appIcons.add(getResources().getDrawable(R.drawable.business_default));
                        }
                    }
                }

                ListAdapter adapter = new ArrayAdapterWithIcon(getActivity(), appNames, appIcons);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getActivity().getString(R.string.settings_share_conversa));
                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ResolveInfo info = activities.get(which);
                        //if (info.activityInfo.packageName.equals("com.facebook.katana")) {
                        // Facebook was chosen
                        //}
                        // Start the selected activity
                        intent.setPackage(info.activityInfo.packageName);
                        startActivity(intent);
                    }
                });

                AlertDialog share = builder.create();
                share.show();
                return true;
            case PreferencesKeys.PREFERENCE_MAIN_HELP:
                fragment = new FragmentSettingsHelp();
                break;
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .addToBackStack(null)
                .commit();

        return true;
    }

    public class ArrayAdapterWithIcon extends ArrayAdapter<String> {

        private List<Drawable> images;
        private Context context;

        public ArrayAdapterWithIcon(Context context, List<String> items, List<Drawable> images) {
            super(context, android.R.layout.select_dialog_item, items);
            this.images = images;
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView textView = (TextView) view.findViewById(android.R.id.text1);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                textView.setCompoundDrawablesRelativeWithIntrinsicBounds(images.get(position), null, null, null);
            } else {
                textView.setCompoundDrawablesWithIntrinsicBounds(images.get(position), null, null, null);
            }
            textView.setCompoundDrawablePadding(
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getContext().getResources().getDisplayMetrics()));
            return view;
        }

    }
}