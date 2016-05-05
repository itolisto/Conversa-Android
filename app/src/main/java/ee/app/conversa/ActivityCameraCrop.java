/*
 * The MIT License (MIT)
 * 
 * Copyright � 2013 Clover Studio Ltd. All rights reserved.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ee.app.conversa;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ee.app.conversa.utils.ImageFilePath;
import ee.app.conversa.utils.Logger;
import ee.app.conversa.view.CroppedImageView;

//import com.facebook.appevents.AppEventsLogger;

/**
 * ActivityCameraCrop
 *
 * Creates cropped image from a gallery photo using a square frame.
 */

public class ActivityCameraCrop extends AppCompatActivity implements OnTouchListener {

	// These matrices will be used to move and zoom image
	private Matrix matrix = new Matrix();
	private Matrix savedMatrix = new Matrix();
	private Matrix translateMatrix = new Matrix();

	// We can be in one of these 3 states
	private static final int NONE = 0;
	private static final int DRAG = 1;
	private static final int ZOOM = 2;
	private int mode = NONE;

	// Remember some things for zooming
	private PointF start = new PointF();
	private PointF mid = new PointF();
	private float oldDist = 1f;

	private CroppedImageView mCropImageView;
	private Bitmap mBitmap;

	public int crop_container_size;
	private float mAspect;
	private float start_x, start_y;

	// Gallery type marker
	private static final int GALLERY_IMAGE_REQUEST_CODE = 200;
	// Camera type marker
	private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
	public static final int MEDIA_TYPE_IMAGE = 1;
	// Uri for captured image so we can get image path
	private static String _path;
	public static boolean return_flag;
	private String mFilePath;
	private String mFileFolder;

	// directory name to store captured images and videos
	private static final String IMAGE_DIRECTORY_NAME = "Images";

	private ActivityCameraCrop sInstance;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_crop);
		sInstance = this;
		getImageIntents();
		mCropImageView = (CroppedImageView) findViewById(R.id.ivCameraCropPhoto);
		mCropImageView.setDrawingCacheEnabled(true);

		return_flag = false;

		Button cancel, ok;

		// Cancel button
		cancel = (Button) findViewById(R.id.btnCameraCancel);
		cancel.setTypeface(ConversaApp.getTfRalewayRegular());
		cancel.setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(ActivityCameraCrop.this, ActivityCameraCrop.class);
						if (getIntent().getStringExtra("type").equals("gallery")) {
							intent.putExtra("type", "gallery");
						} else {
							intent.putExtra("type", "camera");
						}
						startActivity(intent);
						finish();
					}
				});

		// Next button
		ok = (Button) findViewById(R.id.btnCameraOk);
		ok.setTypeface(ConversaApp.getTfRalewayRegular());
		ok.setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						Bitmap resizedBitmap = getBitmapFromView(mCropImageView);
						ByteArrayOutputStream bs = new ByteArrayOutputStream();
						resizedBitmap.compress(Bitmap.CompressFormat.PNG, 99, bs);

						if (saveBitmapToFile(resizedBitmap, mFilePath)) {
							fileUploadAsync(mFilePath, mFileFolder);
						} else {
							Toast.makeText(ActivityCameraCrop.this,
									"Failed to send photo", Toast.LENGTH_LONG)
									.show();
						}
					}
				}
		);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Logs 'install' and 'app activate' App Events.
//		AppEventsLogger.activateApp(this);
		if (return_flag)
			finish();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Logs 'app deactivate' App Event.
//		AppEventsLogger.deactivateApp(this);
	}

	private void getImageIntents() {
		mFileFolder = getIntent().getStringExtra("folder");

		if (getIntent().getStringExtra("type").equals("gallery")) {
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(intent, GALLERY_IMAGE_REQUEST_CODE);
		} else {
			try {
				startCamera();
			} catch (UnsupportedOperationException ex) {
				Toast.makeText(getBaseContext(), "Can't initiate camera", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

	public void startCamera() {
		//Checking device has camera hardware or not
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			Toast.makeText(this, "No camera on device", Toast.LENGTH_LONG).show();
			finish();
		} else {
			try {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				Uri fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
				startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
			} catch (Exception ex) {
				Toast.makeText(this, "No camera on device", Toast.LENGTH_LONG)
						.show();
				finish();
			}
		}
	}

	/**
	 * Creating file uri to store image/video
	 *
	 * @param type
	 * @return
	 */
	public Uri getOutputMediaFileUri(int type) {
		return Uri.fromFile(getOutputMediaFile(type));
	}

	/**
	 * Return image/video
	 *
	 * @param type
	 * @return
	 */
	private static File getOutputMediaFile(int type) {
		// External sdcard location
		File mediaStorageDir = new File(
				Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				IMAGE_DIRECTORY_NAME);

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Logger.error(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
						+ IMAGE_DIRECTORY_NAME + " directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
				Locale.getDefault()).format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".png");
			_path = mediaFile.getPath();
		} else {
			return null;
		}

		return mediaFile;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			scaleView();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case GALLERY_IMAGE_REQUEST_CODE:
					try {
						Uri selected_image = data.getData();
						String selected_image_path = getImagePath(selected_image);
						if (selected_image_path != null) {
							onPhotoTaken(selected_image_path);
						} else {
							Toast.makeText(this, "Error loading image from Gallery!", Toast.LENGTH_LONG).show();
							finish();
						}
					} catch (Exception e) {
						Toast.makeText(this, "Error loading image from Gallery!", Toast.LENGTH_LONG).show();
						finish();
					}
					break;
				case CAMERA_CAPTURE_IMAGE_REQUEST_CODE:
					File file = new File(_path);
					boolean exists = file.exists();
					if (exists)
						onPhotoTaken(_path);
					else
						Toast.makeText( getBaseContext(),
								"Something goes wrong while taking picture, please try again.",
								Toast.LENGTH_SHORT).show();
					break;
				default:
					finish();
					break;
			}
		} else {
			finish();
		}
	}

	protected void onPhotoTaken(String path) {
		// External sdcard location
		File mediaStorageDir = new File(
				Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				IMAGE_DIRECTORY_NAME);

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Logger.error(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
						+ IMAGE_DIRECTORY_NAME + " directory");
				return;
			}
		}

		// Create a media file name
		String fileName = Uri.parse(path).getLastPathSegment();
		mFilePath = mediaStorageDir.getPath() + File.separator + fileName;

		if (!path.equals(mFilePath)) {
			copy(new File(path), new File(mFilePath));
		}

		new AsyncTask<String, Void, byte[]>() {
			boolean loadingFailed = false;

			@Override
			protected byte[] doInBackground(String... params) {
				try {

					if (params == null)
						return null;

					File f = new File(params[0]);

					BitmapFactory.Options optionsMeta = new BitmapFactory.Options();
					optionsMeta.inJustDecodeBounds = true;
					BitmapFactory.decodeFile(f.getAbsolutePath(), optionsMeta);

					BitmapFactory.Options options = new BitmapFactory.Options();

					//options.inSampleSize = BitmapManagement
					//		.calculateInSampleSize(optionsMeta, 640, 640);
					options.inPurgeable = true;
					options.inInputShareable = true;
					mBitmap = BitmapFactory.decodeStream(
							new FileInputStream(f), null, options);
					mBitmap = Bitmap.createBitmap(mBitmap, 0, 0,
							mBitmap.getWidth(), mBitmap.getHeight());
					_scaleBitmap();
					return null;
				} catch (Exception ex) {
					loadingFailed = true;
					finish();
				}

				return null;
			}

			@Override
			protected void onPostExecute(byte[] result) {
				super.onPostExecute(result);
				if (mBitmap != null) {
					mCropImageView.setImageBitmap(mBitmap);
					mCropImageView.setScaleType(ImageView.ScaleType.MATRIX);
					matrix = translateMatrix;
				}
			}
		}.execute(mFilePath);
	}

	private boolean saveBitmapToFile(Bitmap bitmap, String path) {
		File file = new File(path);
		FileOutputStream fOut;
		try {
			fOut = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 99, fOut);
			fOut.flush();
			fOut.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	private String getImagePath(Uri uri) {
		return ImageFilePath.getPath(getApplicationContext(), uri);
	}

	private void fileUploadAsync (String filePath, String folder) {
//		new ConversaAsyncTask<Void, Void, String>(
//				new FileUpload(filePath, folder) , new FileUploadFinished(), ActivityCameraCrop.this, true
//		).execute();
	}

//	private class FileUpload implements Command<String> {
//
//		String filePath;
//		String folder;
//
//		public FileUpload (String filePath, String folder) {
//			this.filePath = filePath;
//			this.folder   = folder;
//		}
//
//		@Override
//		public String execute() throws JSONException, IOException,ConversaException {//ArrayList<String> execute() throws JSONException, IOException,ConversaException {
//			// External sdcard location
//			File mediaStorageDir = new File(
//					Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
//					IMAGE_DIRECTORY_NAME);
//
//			// Create the storage directory if it does not exist
//			if (!mediaStorageDir.exists()) {
//				if (!mediaStorageDir.mkdirs()) {
//					Logger.error(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
//							+ IMAGE_DIRECTORY_NAME + " directory");
//					return null;
//				}
//			}
//
//			return CouchDB.uploadFile(filePath, folder);
//		}
//	}
//
//	private class FileUploadFinished implements ResultListener<String> {
//
//		@Override
//		public void onResultsSuccess(String result) {
//			if (result != null) {
//				try {
//					String fileId = result;
//
//					new SendMessageAsync(sInstance, SendMessageAsync.TYPE_PHOTO)
//							.execute(fileId).get();
//
//				} catch (InterruptedException|ExecutionException e) {
//					e.printStackTrace();
//				}
//			} else {
//				Logger.error("FileUploadAsync", "Failed");
//			}
//			finish();
//		}
//
//		@Override
//		public void onResultsFail() {
//			Logger.error("FileUploadAsync", "Failed Fail");
//			finish();
//		}
//	}

	public void copy(File src, File dst) {
		InputStream in;
		OutputStream out;
		try {
			in = new FileInputStream(src);
			out = new FileOutputStream(dst);

			// Transfer bytes from in to out
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}

			in.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/***********************************************************************************/
	/************************************IMAGE DRAW*************************************/
	/***********************************************************************************/
    public void scaleView() {
        // instantiate the views
        View top_view = findViewById(R.id.topView);
        View bottom_view = findViewById(R.id.bottomView);
        LinearLayout footer = (LinearLayout) findViewById(R.id.llFooter);
        LinearLayout crop_frame = (LinearLayout) findViewById(R.id.llCropFrame);
        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();

        // 90% of width
        crop_container_size = (int) ((float) width * (1f - (10f / 100f)));

        // 10% margins
        float margin = ((float) width * (1f - (90f / 100f)));

        // Parameters for white crop border
        LinearLayout.LayoutParams par = new LinearLayout.LayoutParams(
                crop_container_size, crop_container_size);
        par.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
        par.setMargins((int) (margin / 2f), 0, (int) (margin / 2f), 0);
        crop_frame.setLayoutParams(par);

        // Margins for other transparent views
        float top_view_height = ((float) (height - crop_container_size - footer
                .getHeight())) / (float) 2;
        top_view.setLayoutParams(new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, (int) top_view_height));
        bottom_view.setLayoutParams(new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, (int) top_view_height));

        // Image container
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                crop_container_size, crop_container_size);
        params.setMargins((int) (margin / 2f), (int) top_view_height,
                (int) (margin / 2f), 0);
        mCropImageView.setLayoutParams(params);
        mCropImageView.setImageBitmap(mBitmap);
        mCropImageView.setMaxZoom(4f);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ImageView view = (ImageView) v;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                mode = DRAG;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    // ...
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x, event.getY()
                            - start.y);
                    start_x = event.getX() - start.x;
                    start_y = event.getY() - start.y;
                } else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = newDist / oldDist;
                        mAspect = scale;
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
                break;
        }

        view.setImageMatrix(matrix);
        view.invalidate();
        return true;
    }
//
	/**
	 * Get the image from container - it is already cropped and zoomed If the
	 * image is smaller than container it will be black color set aside
	 * */
	private Bitmap getBitmapFromView(View view) {
		Bitmap returnedBitmap = Bitmap.createBitmap(crop_container_size,
				crop_container_size, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(returnedBitmap);
		Drawable bgDrawable = view.getBackground();
		if (bgDrawable != null)
			bgDrawable.draw(canvas);
		else
			canvas.drawColor(Color.BLACK);
		view.draw(canvas);
		return returnedBitmap;
	}

    /** Determine the space between the first two fingers */
    @SuppressLint("FloatMath")
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /** Calculate the mid point of the first two fingers */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

	private void _scaleBitmap() {
		int image_width = this.mBitmap.getWidth();
		int image_height = this.mBitmap.getHeight();
		int new_image_width;
		int new_image_height;
		int _screen_width = 640;

		if (image_width >= image_height) {
			if (image_height < _screen_width) {
				new_image_width = (int) ((float) image_width * ((float) _screen_width / (float) image_height));
			} else {
				new_image_width = (int) ((float) image_width / ((float) image_height / (float) _screen_width)); // ok
			}
			this.mBitmap = Bitmap.createScaledBitmap(this.mBitmap,
					new_image_width, _screen_width, true);

		} else if (image_width < image_height) {
			if (image_width < _screen_width) {
				new_image_height = (int) ((float) image_height * ((float) _screen_width / (float) image_width));
			} else {
				new_image_height = (int) ((float) image_height / ((float) image_width / (float) _screen_width));
			}

			this.mBitmap = Bitmap.createScaledBitmap(mBitmap, _screen_width,
					new_image_height, true);
		}
	}
}
