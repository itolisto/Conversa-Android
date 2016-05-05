//package ee.app.conversa;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.preference.EditTextPreference;
//import android.preference.Preference;
//import android.preference.PreferenceFragment;
//import android.support.v7.app.AppCompatActivity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Toast;
//
//import com.google.android.gms.gcm.GoogleCloudMessaging;
//
//import java.io.IOException;
//
////import ee.app.conversa.couchdb.CouchDB;
////import ee.app.conversa.couchdb.ResultListener;
//import ee.app.conversa.dialog.HookUpDialog;
//import ee.app.conversa.management.FileManagement;
//import ee.app.conversa.utils.Const;
//import ee.app.conversa.utils.Logger;
//import ee.app.conversa.utils.Utils;
//
////import android.support.v4.preference.PreferenceFragment;
//
//public class FragmentSettings extends PreferenceFragment {
//
//    private EditTextPreference mEditTextPreferenceEmail;
//    private EditTextPreference mEditTextPreferenceName;
//    private EditTextPreference mEditTextPreferencePassword;
//    private Preference mPreferenceShare;
//    private Preference mPreferenceLogout;
//    private HookUpDialog mLogoutDialog;
//
//    private String email;
//    private String username;
//
//    private int mLastFirstVisibleItem = 0;
//
//	@Override
//	public void onCreate(Bundle paramBundle) {
//		super.onCreate(paramBundle);
//		// Load the preferences from an XML resource
//        addPreferencesFromResource(R.layout.fragment_settings);
//	}
//
//    @Override
//    public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle) {
//        mEditTextPreferenceEmail    = (EditTextPreference) getPreferenceManager().findPreference("modify_email_preference");
//        mEditTextPreferenceName     = (EditTextPreference) getPreferenceManager().findPreference("modify_name_preference");
//        mEditTextPreferencePassword = (EditTextPreference) getPreferenceManager().findPreference("modify_password_preference");
//        mPreferenceShare            = (Preference) getPreferenceManager().findPreference("share_preference");
//        mPreferenceLogout           = (Preference) getPreferenceManager().findPreference("logout_preference");
//
//        mEditTextPreferenceEmail.setSummary(ConversaApp.getPreferences().getUserEmail());
//        mEditTextPreferenceName.setSummary(ConversaApp.getPreferences().getUserName());
//
//        mEditTextPreferenceEmail.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                try{
//                    String newEmail     = (String) newValue;
//                    newEmail = newEmail.replaceAll("\\t", "");
//                    newEmail = newEmail.replaceAll("\\n", "");
//                    newEmail = newEmail.replaceAll(" ", "");
//                    String emailResult  = Utils.checkEmail(getActivity(), newEmail);
//                    if (!emailResult.equals(getString(R.string.email_ok))) {
//                        Toast.makeText(getActivity(), emailResult, Toast.LENGTH_SHORT).show();
//                    } else {
//                        email = newEmail;
//                        CouchDB.updateUserAsync(newEmail, Const.UPDATE_EMAIL, new UserEmailUpdateListener(), getActivity(), false);
//                    }
//                } catch(ClassCastException e) {
//                    Logger.error("FragmentSettings Email", e.getMessage());
//                }
//                return false;
//            }
//        });
//
//        mEditTextPreferenceName.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                try{
//                    String newUsername = (String) newValue;
//                    newUsername = newUsername.replaceAll("\\t", "");
//                    newUsername = newUsername.replaceAll("\\n", "");
//                    newUsername = newUsername.replaceAll(" ", "");
//                    String nameResult  = Utils.checkName(getActivity(), newUsername);
//                    if (!nameResult.equals(getString(R.string.name_ok))) {
//                        Toast.makeText(getActivity(), nameResult, Toast.LENGTH_SHORT).show();
//                    } else {
//                        username = newUsername;
//                        CouchDB.updateUserAsync(newUsername, Const.UPDATE_USERNAME, new UserUsernameUpdateListener(), getActivity(), false);
//                    }
//                } catch(ClassCastException e) {
//                    Logger.error("FragmentSettings Name", e.getMessage());
//                }
//                return false;
//            }
//        });
//
//        mEditTextPreferencePassword.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                try {
//                    String newPassword    = (String) newValue;
//                    String passwordResult = Utils.checkPassword(getActivity(), newPassword);
//                    if (!passwordResult.equals(getString(R.string.password_ok))) {
//                        Toast.makeText(getActivity(), passwordResult, Toast.LENGTH_SHORT).show();
//                    } else {
//                        CouchDB.updateUserAsync(FileManagement.md5(newPassword), Const.UPDATE_PASSWORD, new UserPasswordUpdateListener(), getActivity(), false);
//                    }
//                } catch (ClassCastException e) {
//                    Logger.error("FragmentSettings Email", e.getMessage());
//                }
//                return false;
//            }
//        });
//
//        mPreferenceLogout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                    mLogoutDialog = new HookUpDialog(getActivity());
//                mLogoutDialog.setMessage(getString(R.string.logout_message));
//                mLogoutDialog.setOnButtonClickListener(HookUpDialog.BUTTON_OK,
//                        new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                mLogoutDialog.dismiss();
//                                appLogout();
//                            }
//                        });
//                mLogoutDialog.setOnButtonClickListener(HookUpDialog.BUTTON_CANCEL,
//                        new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                mLogoutDialog.dismiss();
//                            }
//                        });
//
//                mLogoutDialog.show();
//                return false;
//            }
//        });
//
//        mPreferenceShare.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                Intent intent=new Intent(android.content.Intent.ACTION_SEND);
//                intent.setType("text/plain");
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
//                // Add data to the intent, the receiving app will decide what to do with it.
//                String subject = getActivity().getString(R.string.settings_using_conversa) + " " + getActivity().getString(R.string.app_name);
//                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
//                String body = getActivity().getString(R.string.settings_body1_conversa) + " " +
//                        getActivity().getString(R.string.app_name) + " " + getActivity().getString(R.string.settings_body2_conversa);
//                intent.putExtra(Intent.EXTRA_TEXT, body);
//                ((AppCompatActivity)getActivity()).startActivity(Intent.createChooser(intent,
//                        getActivity().getString(R.string.settings_share_conversa)));
//                return false;
//            }
//        });
//
//        return super.onCreateView(paramLayoutInflater, paramViewGroup, paramBundle);
//    }
//
//    public void appLogout() {
//        boolean result = ConversaApp.getDB().deleteDatabase();
//        if(result)
//            Logger.error("Logout", getActivity().getString(R.string.settings_logout_succesful));
//        else
//            Logger.error("Logout", getActivity().getString(R.string.settings_logout_error));
//
//        try {
//            GoogleCloudMessaging.getInstance((AppCompatActivity) getActivity()).unregister();
//        } catch (IOException e) {
//
//        }
//
//        AppCompatActivity fromActivity = (AppCompatActivity) ActivityMain.sInstance;
//        Intent goToSignIn              = new Intent(fromActivity, ActivitySignIn.class);
//        ConversaApp.getPreferences().cleanSharedPreferences();
//        goToSignIn.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        fromActivity.startActivity(goToSignIn);
//        fromActivity.finish();
//    }
//
//    private class UserEmailUpdateListener implements ResultListener<Boolean> {
//        public UserEmailUpdateListener() {}
//        @Override
//        public void onResultsSuccess(Boolean result) {
//            if(result) {
//                if( email != null && !email.isEmpty() ) {
//                    ConversaApp.getDB().setUserLoggedInEmail(email);
//                    ConversaApp.getPreferences().setUserEmail(email);
//                    mEditTextPreferenceEmail.setSummary(email);
//                    email = null;
//                }
//                Toast.makeText(getActivity(), getActivity().getString(R.string.settings_email_succesful), Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(getActivity(), getActivity().getString(R.string.settings_email_error), Toast.LENGTH_SHORT).show();
//            }
//        }
//        @Override
//        public void onResultsFail() {
//            Toast.makeText(getActivity(), getActivity().getString(R.string.settings_email_error), Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private class UserUsernameUpdateListener implements ResultListener<Boolean> {
//        public UserUsernameUpdateListener() {}
//        @Override
//        public void onResultsSuccess(Boolean result) {
//            if(result) {
//                if( username != null && !username.isEmpty() ) {
//                    ConversaApp.getDB().setUserLoggedInUsername(username);
//                    ConversaApp.getPreferences().setUserName(username);
//                    mEditTextPreferenceName.setSummary(username);
//                    username = null;
//                }
//
//                Toast.makeText(getActivity(), getActivity().getString(R.string.settings_name_succesful), Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(getActivity(), getActivity().getString(R.string.settings_name_error), Toast.LENGTH_SHORT).show();
//            }
//        }
//        @Override
//        public void onResultsFail() {
//            Toast.makeText(getActivity(), getActivity().getString(R.string.settings_name_error), Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private class UserPasswordUpdateListener implements ResultListener<Boolean> {
//        public UserPasswordUpdateListener() {}
//        @Override
//        public void onResultsSuccess(Boolean result) {
//            if(result) {
//                Toast.makeText(getActivity(), getActivity().getString(R.string.settings_password_succesful), Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(getActivity(), getActivity().getString(R.string.settings_password_error), Toast.LENGTH_SHORT).show();
//            }
//        }
//        @Override
//        public void onResultsFail() {
//            Toast.makeText(getActivity(), getActivity().getString(R.string.settings_password_error), Toast.LENGTH_SHORT).show();
//        }
//    }
//}
