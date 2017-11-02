package ee.app.conversa.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

import ee.app.conversa.ActivityChatWall;
import ee.app.conversa.ActivityLocation;
import ee.app.conversa.R;
import ee.app.conversa.camara.ImagePickerDemo;
import ee.app.conversa.utils.Const;

/**
 * Created by edgargomez on 9/9/16.
 */
@SuppressLint("ValidFragment")
public class MyBottomSheetDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    String businessId;
    final ActivityChatWall mActivity;

    public static MyBottomSheetDialogFragment newInstance(String businessId, ActivityChatWall mActivity) {
        MyBottomSheetDialogFragment f = new MyBottomSheetDialogFragment(mActivity);
        Bundle args = new Bundle();
        args.putString(Const.LOCATION, businessId);
        f.setArguments(args);
        return f;
    }

    public MyBottomSheetDialogFragment(ActivityChatWall mActivity) {
        this.mActivity = mActivity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        businessId = getArguments().getString(Const.LOCATION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottomsheet_chat, container, false);
        v.findViewById(R.id.btnCamera).setOnClickListener(this);
        v.findViewById(R.id.btnLocation).setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnCamera: {
                PermissionListener permissionlistener = new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        Intent intent = new Intent(mActivity, ImagePickerDemo.class);
                        intent.putExtra("picker", "single");
                        mActivity.startActivityForResult(intent, Const.CAPTURE_MEDIA);
                    }

                    @Override
                    public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                        Toast.makeText(mActivity, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
                    }
                };

                new TedPermission(mActivity)
                        .setPermissionListener(permissionlistener)
                        .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                        .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .check();
                break;
            }
            case R.id.btnLocation: {
                Intent intent = new Intent(mActivity, ActivityLocation.class);
                intent.putExtra(Const.LOCATION, businessId);
                mActivity.startActivityForResult(intent, ActivityLocation.PICK_LOCATION_REQUEST);
                break;
            }
        }

        dismiss();
    }

}