package ee.app.conversa;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import ee.app.conversa.settings.ActivityAccountSettings;
import ee.app.conversa.settings.ActivityChatSettings;
import ee.app.conversa.settings.ActivityNotificationSettings;
import ee.app.conversa.settings.ActivitySupportSettings;

public class FragmentSettings extends Fragment implements View.OnClickListener{

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        RelativeLayout mRlOption1 = (RelativeLayout) rootView.findViewById(R.id.rlOption1);
        RelativeLayout mRlOption2 = (RelativeLayout) rootView.findViewById(R.id.rlOption2);
        RelativeLayout mRlOption3 = (RelativeLayout) rootView.findViewById(R.id.rlOption3);
        RelativeLayout mRlOption4 = (RelativeLayout) rootView.findViewById(R.id.rlOption4);
        RelativeLayout mRlOption5 = (RelativeLayout) rootView.findViewById(R.id.rlOption5);
        mRlOption1.setOnClickListener(this);
        mRlOption2.setOnClickListener(this);
        mRlOption3.setOnClickListener(this);
        mRlOption4.setOnClickListener(this);
        mRlOption5.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.rlOption1:
                intent = new Intent(getActivity(), ActivityAccountSettings.class);
                break;
            case R.id.rlOption2:
                intent = new Intent(getActivity(), ActivityChatSettings.class);
                break;
            case R.id.rlOption3:
                intent = new Intent(getActivity(), ActivityNotificationSettings.class);
                break;
            case R.id.rlOption4:
                intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setType("text/plain");
                // Add data to the intent, the receiving app will decide what to do with it.
                String subject = getActivity().getString(R.string.settings_using_conversa) + " " + getActivity().getString(R.string.app_name);
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                String body = getActivity().getString(R.string.settings_body1_conversa) + " " +
                        getActivity().getString(R.string.app_name) + " " + getActivity().getString(R.string.settings_body2_conversa);
                intent.putExtra(Intent.EXTRA_TEXT, body);
                startActivity(Intent.createChooser(intent, getActivity().getString(R.string.settings_share_conversa)));
                return;
            case R.id.rlOption5:
                intent = new Intent(getActivity(), ActivitySupportSettings.class);
                break;
        }

        if (intent != null) {
            startActivity(intent);
        }
    }
}

//    @Override
//    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//        Preference preference = findPreference(key);
//        if (preference instanceof ListPreference) {
//            ListPreference listPreference = (ListPreference) preference;
//            int prefIndex = listPreference.findIndexOfValue(sharedPreferences.getString(key, ""));
//            if (prefIndex >= 0) {
//                preference.setSummary(listPreference.getEntries()[prefIndex]);
//            }
//        } else if (preference instanceof EditTextPreference) {
//            EditTextPreference editTextPreference = (EditTextPreference) preference;
//            if (key.equals(getString(R.string.email_edittext_preference_key))) {
////                try{
////                    String newEmail     = (String) newValue;
////                    newEmail = newEmail.replaceAll("\\t", "");
////                    newEmail = newEmail.replaceAll("\\n", "");
////                    newEmail = newEmail.replaceAll(" ", "");
////                    String emailResult  = Utils.checkEmail(getActivity(), newEmail);
////                    if (!emailResult.equals(getString(R.string.email_ok))) {
////                        Toast.makeText(getActivity(), emailResult, Toast.LENGTH_SHORT).show();
////                    } else {
////                        email = newEmail;
////                        CouchDB.updateUserAsync(newEmail, Const.UPDATE_EMAIL, new UserEmailUpdateListener(), getActivity(), false);
////                    }
////                } catch(ClassCastException e) {
////                    Logger.error("FragmentSettings Email", e.getMessage());
////                }
//            } else if (key.equals(getString(R.string.name_edittext_preference_key))) {
////                try{
////                    String newUsername = (String) newValue;
////                    newUsername = newUsername.replaceAll("\\t", "");
////                    newUsername = newUsername.replaceAll("\\n", "");
////                    newUsername = newUsername.replaceAll(" ", "");
////                    String nameResult  = Utils.checkName(getActivity(), newUsername);
////                    if (!nameResult.equals(getString(R.string.name_ok))) {
////                        Toast.makeText(getActivity(), nameResult, Toast.LENGTH_SHORT).show();
////                    } else {
////                        username = newUsername;
////                        CouchDB.updateUserAsync(newUsername, Const.UPDATE_USERNAME, new UserUsernameUpdateListener(), getActivity(), false);
////                    }
////                } catch(ClassCastException e) {
////                    Logger.error("FragmentSettings Name", e.getMessage());
////                }
//            } else {
////                try {
////                    String newPassword    = (String) newValue;
////                    String passwordResult = Utils.checkPassword(getActivity(), newPassword);
////                    if (!passwordResult.equals(getString(R.string.password_ok))) {
////                        Toast.makeText(getActivity(), passwordResult, Toast.LENGTH_SHORT).show();
////                    } else {
////                        CouchDB.updateUserAsync(FileManagement.md5(newPassword), Const.UPDATE_PASSWORD, new UserPasswordUpdateListener(), getActivity(), false);
////                    }
////                } catch (ClassCastException e) {
////                    Logger.error("FragmentSettings Email", e.getMessage());
////                }
//            }
//        }
//    }
//
//
//}