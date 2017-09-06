package ee.app.conversa.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.net.URI;

import ee.app.conversa.ActivityChatWall;
import ee.app.conversa.ActivityLocation;
import ee.app.conversa.ConversaApp;
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

                Intent intent = new Intent(mActivity, ImagePickerDemo.class);
                intent.putExtra("picker", "single");
                //mActivity.startActivity(intent);
                mActivity.startActivityForResult(intent, ImagePickerDemo.CAMERA_CODE_ACTIVITY);

                //mActivity.startActivityForResult(intent, ActivityLocation.PICK_LOCATION_REQUEST);


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