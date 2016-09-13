package ee.app.conversa.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.lang.ref.WeakReference;

import ee.app.conversa.ActivityCameraCrop;
import ee.app.conversa.ActivityChatWall;
import ee.app.conversa.ActivityLocation;
import ee.app.conversa.R;
import ee.app.conversa.utils.Const;

/**
 * Created by edgargomez on 9/9/16.
 */
@SuppressLint("ValidFragment")
public class MyBottomSheetDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    String businessId;
    final WeakReference<ActivityChatWall>mActivity;

    public static MyBottomSheetDialogFragment newInstance(String businessId, ActivityChatWall mActivity) {
        MyBottomSheetDialogFragment f = new MyBottomSheetDialogFragment(mActivity);
        Bundle args = new Bundle();
        args.putString(Const.LOCATION, businessId);
        f.setArguments(args);
        return f;
    }

    public MyBottomSheetDialogFragment(ActivityChatWall mActivity) {
        this.mActivity = new WeakReference<>(mActivity);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        businessId = getArguments().getString(Const.LOCATION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.menu_actionbar_chat, container, false);
		ImageButton mBtnCamera = (ImageButton) v.findViewById(R.id.btnCamera);
		ImageButton mBtnGallery = (ImageButton) v.findViewById(R.id.btnGallery);
		ImageButton mBtnLocation = (ImageButton) v.findViewById(R.id.btnLocation);
		mBtnCamera.setOnClickListener(this);
		mBtnGallery.setOnClickListener(this);
		mBtnLocation.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View view) {
        if (mActivity.get() == null) {
            return;
        }

        switch (view.getId()) {
            case R.id.btnCamera: {
                Intent intent = new Intent(mActivity.get(), ActivityCameraCrop.class);
                intent.putExtra("type", "camera");
                mActivity.get().startActivityForResult(intent, ActivityCameraCrop.PICK_CAMERA_REQUEST);
                break;
            }
            case R.id.btnGallery: {
                Intent intent = new Intent(mActivity.get(), ActivityCameraCrop.class);
                intent.putExtra("type", "gallery");
                mActivity.get().startActivityForResult(intent, ActivityCameraCrop.PICK_GALLERY_REQUEST);
                break;
            }
            case R.id.btnLocation: {
                Intent intent = new Intent(mActivity.get(), ActivityLocation.class);
                intent.putExtra(Const.LOCATION, businessId);
                mActivity.get().startActivityForResult(intent, ActivityLocation.PICK_LOCATION_REQUEST);
                break;
            }
        }

        dismiss();
    }

}