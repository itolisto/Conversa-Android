package ee.app.conversa.settings;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.facebook.drawee.view.SimpleDraweeView;
import com.onesignal.OneSignal;

import java.util.ArrayList;
import java.util.Collection;

import ee.app.conversa.ActivityCameraCrop;
import ee.app.conversa.ActivitySignIn;
import ee.app.conversa.ConversaApp;
import ee.app.conversa.R;
import ee.app.conversa.extendables.ConversaActivity;
import ee.app.conversa.management.ably.Connection;
import ee.app.conversa.model.parse.Account;
import ee.app.conversa.utils.Logger;
import ee.app.conversa.view.RegularTextView;

/**
 * Created by edgargomez on 8/30/16.
 */
public class ActivityAccountSettings extends ConversaActivity implements View.OnClickListener {

    private BottomSheetBehavior mBsbUploadOption;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_account);
        initialization();
    }

    @Override
    protected void initialization() {
        super.initialization();
        SimpleDraweeView mSdvAvatarSettings = (SimpleDraweeView) findViewById(R.id.sdvAvatarSettings);
        RegularTextView mRtvCleanRecents = (RegularTextView) findViewById(R.id.rtvCleanRecents);
        LinearLayout mLlBlockedUsers = (LinearLayout) findViewById(R.id.llBlockedUsers);
        LinearLayout mLlogOut = (LinearLayout) findViewById(R.id.llLogOut);

        mRtvCleanRecents.setOnClickListener(this);
        mLlBlockedUsers.setOnClickListener(this);
        mSdvAvatarSettings.setOnClickListener(this);
        mLlogOut.setOnClickListener(this);

        View bottomSheet = findViewById(R.id.bsbUploadOption);
        mBsbUploadOption = BottomSheetBehavior.from(bottomSheet);

        RegularTextView mRtvCamera = (RegularTextView) findViewById(R.id.rtvCamera);
        RegularTextView mRtvGallery = (RegularTextView) findViewById(R.id.rtvGallery);
        mRtvCamera.setOnClickListener(this);
        mRtvGallery.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rtvCleanRecents:
                break;
            case R.id.llBlockedUsers:
                break;
            case R.id.sdvAvatarSettings:
                mBsbUploadOption.setState(BottomSheetBehavior.STATE_EXPANDED);
                break;
            case R.id.llLogOut:
                logOut();
                break;
            case R.id.rtvCamera: {
                Intent intent = new Intent(this, ActivityCameraCrop.class);
                intent.putExtra("type", "camera");
                startActivityForResult(intent, ActivityCameraCrop.PICK_CAMERA_REQUEST);
                break;
            }
            case R.id.rtvGallery: {
                Intent intent1 = new Intent(this, ActivityCameraCrop.class);
                intent1.putExtra("type", "gallery");
                startActivityForResult(intent1, ActivityCameraCrop.PICK_GALLERY_REQUEST);
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
                case ActivityCameraCrop.PICK_CAMERA_REQUEST: {
                    uploadPhoto(data);
                    break;
                }
                case ActivityCameraCrop.PICK_GALLERY_REQUEST: {
                    uploadPhoto(data);
                    break;
                }
            }
        }
    }

    private void uploadPhoto(Intent data) {
        Log.e(this.getClass().getSimpleName(), "DATA: " + data.toString());
    }

    private void logOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.logout_message)
                .setPositiveButton(R.string.logout_preference, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        appLogout();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    private void appLogout() {
        boolean result = ConversaApp.getDB().deleteDatabase();
        if(result)
            Logger.error("Logout", getString(R.string.settings_logout_succesful));
        else
            Logger.error("Logout", getString(R.string.settings_logout_error));

        OneSignal.setSubscription(false);
        Collection<String> tempList = new ArrayList<>();
        tempList.add("upbc");
        tempList.add("upvt");
        OneSignal.deleteTags(tempList);
        OneSignal.clearOneSignalNotifications();
        Connection.getInstance().disconnectAbly();

        Account.logOut();
        Intent goToSignIn = new Intent(getApplicationContext(), ActivitySignIn.class);
        goToSignIn.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        goToSignIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ConversaApp.getPreferences().cleanSharedPreferences();
        startActivity(goToSignIn);
        finish();
    }
}