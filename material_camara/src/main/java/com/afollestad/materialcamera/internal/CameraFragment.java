package com.afollestad.materialcamera.internal;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.afollestad.materialcamera.ICallback;
import com.afollestad.materialcamera.ICompressCallback;
import com.afollestad.materialcamera.R;
import com.afollestad.materialcamera.util.CameraUtil;
import com.afollestad.materialcamera.util.Degrees;
import com.afollestad.materialcamera.util.ImageUtil;
import com.afollestad.materialcamera.util.ManufacturerUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.afollestad.materialcamera.internal.BaseCaptureActivity.CAMERA_POSITION_BACK;
import static com.afollestad.materialcamera.internal.BaseCaptureActivity.CAMERA_POSITION_FRONT;
import static com.afollestad.materialcamera.internal.BaseCaptureActivity.CAMERA_POSITION_UNKNOWN;
import static com.afollestad.materialcamera.internal.BaseCaptureActivity.FLASH_MODE_ALWAYS_ON;
import static com.afollestad.materialcamera.internal.BaseCaptureActivity.FLASH_MODE_AUTO;
import static com.afollestad.materialcamera.internal.BaseCaptureActivity.FLASH_MODE_OFF;

/**
 * @author Aidan Follestad (afollestad)
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class CameraFragment extends BaseCameraFragment implements View.OnClickListener {

    CameraPreview mPreviewView;
    RelativeLayout mPreviewFrame;
    View container;

    /**
     * Holds reference to the default image size (width and height dimensions)
     * picked by the user or if no reasonable size could be found set the last
     * of the available camera's supported sizes
     */
    private Camera.Size mVideoSize;
    private Camera mCamera;
    /**
     * Holds width and height of the display in pixels
     */
    private Point mWindowSize;
    private int mDisplayOrientation;
    private boolean mIsAutoFocusing;
    List<Integer> mFlashModes;

    public static CameraFragment newInstance() {
        CameraFragment fragment = new CameraFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        container = view;
        mPreviewFrame = (RelativeLayout) view.findViewById(R.id.rootFrame);
        mPreviewFrame.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        openCamera();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            mPreviewView.getHolder().getSurface().release();
        } catch (Throwable ignored) {
        }
        mPreviewFrame = null;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.rootFrame) {
            if (mCamera == null || mIsAutoFocusing) return;
            try {
                mIsAutoFocusing = true;
                mCamera.cancelAutoFocus();
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        mIsAutoFocusing = false;
                        if (!success)
                            Toast.makeText(getActivity(), "Unable to auto-focus!", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Throwable t) {
                t.printStackTrace();
                Log.e("CameraFragment", t.getMessage());
            }
        } else {
            super.onClick(view);
        }
    }

    @Override
    public void openCamera() {
        final Activity activity = getActivity();
        if (null == activity || activity.isFinishing()) return;
        try {
            // The first time both will be null so value will be -1
            final int mBackCameraId = mInterface.getBackCamera() != null ? (Integer) mInterface.getBackCamera() : -1;
            final int mFrontCameraId = mInterface.getFrontCamera() != null ? (Integer) mInterface.getFrontCamera() : -1;

            if (mBackCameraId == -1 || mFrontCameraId == -1) {
                int numberOfCameras = Camera.getNumberOfCameras();
                if (numberOfCameras == 0) {
                    throwError(new Exception("No cameras are available on this device."));
                    return;
                }

                for (int i = 0; i < numberOfCameras; i++) {
                    //noinspection ConstantConditions
                    if (mFrontCameraId != -1 && mBackCameraId != -1) break;
                    Camera.CameraInfo info = new Camera.CameraInfo();
                    Camera.getCameraInfo(i, info);
                    if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT && mFrontCameraId == -1) {
                        mInterface.setFrontCamera(i);
                    } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK && mBackCameraId == -1) {
                        mInterface.setBackCamera(i);
                    }
                }
            }
            // The first time position is UNKNOWN. Later it's impossible as at least one camera
            // should have been set inside the above if block
            switch (getCurrentCameraPosition()) {
                case CAMERA_POSITION_FRONT:
                    setImageRes(mButtonFacing, mInterface.iconRearCamera());
                    break;
                case CAMERA_POSITION_BACK:
                    setImageRes(mButtonFacing, mInterface.iconFrontCamera());
                    break;
                case CAMERA_POSITION_UNKNOWN:
                default:
                    if (getArguments().getBoolean(CameraIntentKey.DEFAULT_TO_FRONT_FACING, false)) {
                        // Check front facing first
                        if (mInterface.getFrontCamera() != null && (Integer) mInterface.getFrontCamera() != -1) {
                            setImageRes(mButtonFacing, mInterface.iconRearCamera());
                            mInterface.setCameraPosition(CAMERA_POSITION_FRONT);
                        } else {
                            setImageRes(mButtonFacing, mInterface.iconFrontCamera());
                            if (mInterface.getBackCamera() != null && (Integer) mInterface.getBackCamera() != -1)
                                mInterface.setCameraPosition(CAMERA_POSITION_BACK);
                            else mInterface.setCameraPosition(CAMERA_POSITION_UNKNOWN);
                        }
                    } else {
                        // Check back facing first
                        if (mInterface.getBackCamera() != null && (Integer) mInterface.getBackCamera() != -1) {
                            setImageRes(mButtonFacing, mInterface.iconFrontCamera());
                            mInterface.setCameraPosition(CAMERA_POSITION_BACK);
                        } else {
                            setImageRes(mButtonFacing, mInterface.iconRearCamera());
                            if (mInterface.getFrontCamera() != null && (Integer) mInterface.getFrontCamera() != -1)
                                mInterface.setCameraPosition(CAMERA_POSITION_FRONT);
                            else mInterface.setCameraPosition(CAMERA_POSITION_UNKNOWN);
                        }
                    }
                    break;
            }

            if (mWindowSize == null) {
                mWindowSize = new Point();
            }
            // Sets display size both width and height
            activity.getWindowManager().getDefaultDisplay().getSize(mWindowSize);
            // Gets the position of the camera the app should open
            final int toOpen = getCurrentCameraId();
            // Creates a reference to the Camera
            try {
                mCamera = Camera.open(toOpen == -1 ? 0 : toOpen);
            } catch (Exception e) {
                throwError(e);
            }

            Camera.Parameters parameters = mCamera.getParameters();
            /**
             * Video
             */
            // If getSupportedVideoSizes() returns null then the camera's supported preview
            // sizes and video sizes are the same
            List<Camera.Size> videoSizes = parameters.getSupportedVideoSizes();
            if (videoSizes == null || videoSizes.size() == 0)
                videoSizes = parameters.getSupportedPreviewSizes();

            mVideoSize = chooseVideoSize((BaseCaptureActivity) activity, videoSizes);
            // Now find an optimal size or stay with mVideoSize
            Camera.Size previewSize = chooseOptimalSize(parameters.getSupportedPreviewSizes(),
                    mWindowSize.x, mWindowSize.y, mVideoSize);

            if (ManufacturerUtil.isSamsungGalaxyS3()) {
                parameters.setPreviewSize(ManufacturerUtil.SAMSUNG_S3_PREVIEW_WIDTH,
                        ManufacturerUtil.SAMSUNG_S3_PREVIEW_HEIGHT);
            } else {
                parameters.setPreviewSize(previewSize.width, previewSize.height);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                    parameters.setRecordingHint(true);
            }
            /**
             * Camera
             */
            // Get the highset supported size for taking pictures
            Camera.Size mStillShotSize = getHighestSupportedStillShotSize(parameters.getSupportedPictureSizes());
            parameters.setPictureSize(mStillShotSize.width, mStillShotSize.height);
            // Ensure correct orientation of preview.
            setCameraDisplayOrientation(parameters);
            mCamera.setParameters(parameters);

            // NOTE: onFlashModesLoaded should not be called while modifying camera parameters as
            //       the flash parameters set in setupFlashMode will then be overwritten
            mFlashModes = CameraUtil.getSupportedFlashModes(this.getActivity(), parameters);
            mInterface.setFlashModes(mFlashModes);
            onFlashModesLoaded();

            createPreview();
            mMediaRecorder = new MediaRecorder();
            onCameraOpened();
        } catch (IllegalStateException e) {
            Log.e("openCamera", e.getMessage());
            throwError(new Exception("Cannot access the camera.", e));
        }  catch (RuntimeException e2) {
            Log.e("openCamera", e2.getMessage());
            throwError(new Exception("Cannot access the camera, you may need to restart your device.", e2));
        }
    }

    @Override
    public void closeCamera() {
        try {
            if (mCamera != null) {
                try {
                    mCamera.lock();
                } catch (Throwable ignored) {
                }
                mCamera.release();
                mCamera = null;
            }
        } catch (IllegalStateException e) {
            throwError(new Exception("Illegal state while trying to close camera.", e));
        }
    }

    private static Camera.Size chooseVideoSize(BaseCaptureInterface ci, List<Camera.Size> choices) {
        Camera.Size backupSize = null;
        for (Camera.Size size : choices) {
            if (size.height <= ci.videoPreferredHeight()) {
                if (size.width == size.height * ci.videoPreferredAspect())
                    return size;
                if (ci.videoPreferredHeight() >= size.height)
                    backupSize = size;
            }
        }
        if (backupSize != null) return backupSize;
        LOG(CameraFragment.class, "Couldn't find any suitable video size");
        return choices.get(0);
    }

    private static Camera.Size chooseOptimalSize(List<Camera.Size> choices, int width, int height, Camera.Size aspectRatio) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Camera.Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.width;
        int h = aspectRatio.height;
        for (Camera.Size option : choices) {
            if (option.height == width * h / w &&
                    option.width >= width && option.height >= height) {
                bigEnough.add(option);
            }
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            LOG(CameraFragment.class, "Couldn't find any suitable preview size");
            return aspectRatio;
        }
    }

    private Camera.Size getHighestSupportedStillShotSize(List<Camera.Size> supportedPictureSizes) {
        Collections.sort(supportedPictureSizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                if (lhs.height * lhs.width > rhs.height * rhs.width)
                    return -1;
                return 1;

            }
        });
        Camera.Size maxSize = supportedPictureSizes.get(0);
        Log.e("CameraFragment", "Using resolution: " + maxSize.width + "x" + maxSize.height);
        return maxSize;
    }

    @SuppressWarnings("WrongConstant")
    private void setCameraDisplayOrientation(Camera.Parameters parameters) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(getCurrentCameraId(), info);
        final int deviceOrientation = Degrees.getDisplayRotation(getActivity());
        // Sets display orientantion. Required because front facing camera has a mirror effect
        // that must be fixed
        mDisplayOrientation = Degrees.getDisplayOrientation(
                info.orientation, deviceOrientation, info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);
        Log.e("CameraFragment", String.format("Orientations: Sensor = %d˚, Device = %d˚, Display = %d˚",
                info.orientation, deviceOrientation, mDisplayOrientation));

        int previewOrientation;
        int jpegOrientation;
        if (CameraUtil.isChromium()) {
            previewOrientation = 0;
            jpegOrientation = 0;
        } else {
            jpegOrientation = previewOrientation = mDisplayOrientation;

            if (Degrees.isPortrait(deviceOrientation) && getCurrentCameraPosition() == CAMERA_POSITION_FRONT)
                previewOrientation = Degrees.mirror(mDisplayOrientation);
        }

        parameters.setRotation(jpegOrientation);
        mCamera.setDisplayOrientation(previewOrientation);
    }

    private void createPreview() {
        Activity activity = getActivity();
        if (activity == null) return;
        // NOTE: mWindowSize is already defined in the caller method
        //if (mWindowSize == null)
        //    mWindowSize = new Point();
        //activity.getWindowManager().getDefaultDisplay().getSize(mWindowSize);
        // Gets a new reference to CameraPreview
        mPreviewView = new CameraPreview(getActivity(), mCamera);
        // If a CameraPreview reference is already on top we should remove it and add the new
        // one instead (maybe has a new orientantion, rotation, optimal size, etc)
        if (mPreviewFrame.getChildCount() > 0 && mPreviewFrame.getChildAt(0) instanceof CameraPreview)
            mPreviewFrame.removeViewAt(0);

        mPreviewView.setAspectRatio(mWindowSize.x, mWindowSize.y);
        mPreviewFrame.addView(mPreviewView, 0);
    }

    private boolean prepareMediaRecorder() {
        try {
            final Activity activity = getActivity();
            if (null == activity) return false;
            final BaseCaptureInterface captureInterface = (BaseCaptureInterface) activity;

            setCameraDisplayOrientation(mCamera.getParameters());
            mMediaRecorder = new MediaRecorder();
            mCamera.stopPreview();
            mCamera.unlock();
            mMediaRecorder.setCamera(mCamera);

            boolean canUseAudio = true;
            boolean audioEnabled = !mInterface.audioDisabled();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                canUseAudio = ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

            if (canUseAudio && audioEnabled) {
                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            } else if (audioEnabled) {
                Toast.makeText(getActivity(), R.string.mcam_no_audio_access, Toast.LENGTH_LONG).show();
            }
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

            final CamcorderProfile profile = CamcorderProfile.get(getCurrentCameraId(), mInterface.qualityProfile());
            mMediaRecorder.setOutputFormat(profile.fileFormat);
            mMediaRecorder.setVideoFrameRate(mInterface.videoFrameRate(profile.videoFrameRate));
            mMediaRecorder.setVideoSize(mVideoSize.width, mVideoSize.height);
            mMediaRecorder.setVideoEncodingBitRate(mInterface.videoEncodingBitRate(profile.videoBitRate));
            mMediaRecorder.setVideoEncoder(profile.videoCodec);

            if (canUseAudio && audioEnabled) {
                mMediaRecorder.setAudioEncodingBitRate(mInterface.audioEncodingBitRate(profile.audioBitRate));
                mMediaRecorder.setAudioChannels(profile.audioChannels);
                mMediaRecorder.setAudioSamplingRate(profile.audioSampleRate);
                mMediaRecorder.setAudioEncoder(profile.audioCodec);
            }

            Uri uri = Uri.fromFile(getOutputMediaFile());
            mOutputUri = uri.toString();
            mMediaRecorder.setOutputFile(uri.getPath());

            if (captureInterface.maxAllowedFileSize() > 0) {
                mMediaRecorder.setMaxFileSize(captureInterface.maxAllowedFileSize());
                mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                    @Override
                    public void onInfo(MediaRecorder mediaRecorder, int what, int extra) {
                        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
                            Toast.makeText(getActivity(), R.string.mcam_file_size_limit_reached, Toast.LENGTH_SHORT).show();
                            stopRecordingVideo(false);
                        }
                    }
                });
            }

            mMediaRecorder.setOrientationHint(mDisplayOrientation);
            mMediaRecorder.setPreviewDisplay(mPreviewView.getHolder().getSurface());

            try {
                mMediaRecorder.prepare();
                return true;
            } catch (Throwable e) {
                throwError(new Exception("Failed to prepare the media recorder: " + e.getMessage(), e));
                return false;
            }
        } catch (Throwable t) {
            try {
                mCamera.lock();
            } catch (IllegalStateException e) {
                throwError(new Exception("Failed to re-lock camera: " + e.getMessage(), e));
                return false;
            }
            t.printStackTrace();
            throwError(new Exception("Failed to begin recording: " + t.getMessage(), t));
            return false;
        }
    }

    @Override
    public boolean startRecordingVideo() {
        super.startRecordingVideo();
        if (prepareMediaRecorder()) {
            try {
                // UI
                setImageRes(mButtonVideo, mInterface.iconStop());
                if (!CameraUtil.isChromium())
                    mButtonFacing.setVisibility(View.GONE);

                // Only start counter if count down wasn't already started
                if (!mInterface.hasLengthLimit()) {
                    mInterface.setRecordingStart(System.currentTimeMillis());
                    startCounter();
                }

                // Start recording
                mMediaRecorder.start();

                mButtonVideo.setEnabled(false);
                mButtonVideo.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mButtonVideo.setEnabled(true);
                    }
                }, 200);

                return true;
            } catch (Throwable t) {
                t.printStackTrace();
                mInterface.setRecordingStart(-1);
                stopRecordingVideo(false);
                throwError(new Exception("Failed to start recording: " + t.getMessage(), t));
            }
        }
        return false;
    }

    @Override
    public void stopRecordingVideo(final boolean reachedZero) {
        super.stopRecordingVideo(reachedZero);

        if (mInterface.hasLengthLimit() && mInterface.shouldAutoSubmit() &&
                (mInterface.getRecordingStart() < 0 || mMediaRecorder == null)) {
            stopCounter();
            if (mCamera != null) {
                try {
                    mCamera.lock();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            releaseRecorder();
            closeCamera();
            mButtonFacing.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mInterface.onShowPreview(mOutputUri, reachedZero);
                }
            }, 100);
            return;
        }

        if (mCamera != null)
            mCamera.lock();
        releaseRecorder();
        closeCamera();

        if (!mInterface.didRecord())
            mOutputUri = null;

        setImageRes(mButtonVideo, mInterface.iconRecord());

        if (!CameraUtil.isChromium())
            mButtonFacing.setVisibility(View.VISIBLE);
        if (mInterface.getRecordingStart() > -1 && getActivity() != null)
            mInterface.onShowPreview(mOutputUri, reachedZero);

        stopCounter();
    }

    @Override
    public void takeStillshot() {
        // The callback for image capture moment
        Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
            public void onShutter() {
                //Log.e(TAG, "onShutter'd");
            }
        };
        // The callback for raw (uncompressed) image data
        Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {
                //Log.e(TAG, "onPictureTaken - raw. Raw is null: " + (data == null));
            }
        };
        // The callback for JPEG image data
        Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
            public void onPictureTaken(final byte[] data, Camera camera) {
                final File outputPic = getOutputPictureFile();
                // Save the image to disk
                if (mInterface.compressData()) {
                    ImageUtil.saveToDiskWithCompressionAsync(data, outputPic, new ICompressCallback() {
                        @Override
                        public void done(Exception e, long newSize) {
                            if (e == null) {
                                Log.e("stillshot", "picture compress saved to disk " +
                                        "- jpeg, original size: " + data.length
                                        + ", new size: " + newSize);
                                mOutputUri = outputPic.getPath();
                                mInterface.onShowStillshot(mOutputUri, newSize);
                                //mCamera.startPreview();
                                mButtonStillshot.setEnabled(true);
                            } else {
                                Log.e("stillshot", e.getMessage());
                                throwError(e);
                            }
                        }
                    });
                } else {
                    ImageUtil.saveToDiskAsync(data, outputPic, new ICallback() {
                        @Override
                        public void done(Exception e) {
                            if (e == null) {
                                Log.e("stillshot", "picture saved to disk - jpeg, size: " + data.length);
                                mOutputUri = outputPic.getPath();
                                mInterface.onShowStillshot(mOutputUri, data.length);
                                //mCamera.startPreview();
                                mButtonStillshot.setEnabled(true);
                            } else {
                                Log.e("stillshot", e.getMessage());
                                throwError(e);
                            }
                        }
                    });
                }
            }
        };

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            // We could have configurable shutter sound here
//            mCamera.enableShutterSound(false);
//        }

        mButtonStillshot.setEnabled(false);
        mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
    }

    private void setupFlashMode() {
        String flashMode = null;
        switch (mInterface.getFlashMode()) {
            case FLASH_MODE_AUTO:
                flashMode = Camera.Parameters.FLASH_MODE_AUTO;
                break;
            case FLASH_MODE_ALWAYS_ON:
                flashMode = Camera.Parameters.FLASH_MODE_ON;
                break;
            case FLASH_MODE_OFF:
                flashMode = Camera.Parameters.FLASH_MODE_OFF;
            default:
                break;
        }
        if (flashMode != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFlashMode(flashMode);
            mCamera.setParameters(parameters);
        }
    }

    @Override
    public void onPreferencesUpdated() {
        setupFlashMode();
    }

    static class CompareSizesByArea implements Comparator<Camera.Size> {
        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.width * lhs.height -
                    (long) rhs.width * rhs.height);
        }
    }
}