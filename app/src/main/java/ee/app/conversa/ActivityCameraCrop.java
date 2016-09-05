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
import android.app.Activity;
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
import android.os.Build;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ee.app.conversa.utils.ImageFilePath;
import ee.app.conversa.utils.Logger;
import ee.app.conversa.view.CroppedImageView;

/**
 * ActivityCameraCrop
 *
 * Creates cropped image from a gallery photo using a square frame.
 */
public class ActivityCameraCrop extends AppCompatActivity implements OnTouchListener, OnClickListener {

	// These matrices will be used to move and zoom image
	private Matrix matrix = new Matrix();
	private Matrix savedMatrix = new Matrix();
	private States state;

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btnCameraOk) {
			Bitmap resizedBitmap = getBitmapFromView(mCropImageView);
			ByteArrayOutputStream bs = new ByteArrayOutputStream();
			resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, bs);
			Intent returnIntent = new Intent();

			if (saveBitmapToFile(resizedBitmap, _path)) {
				returnIntent.putExtra("result", _path);
				setResult(Activity.RESULT_OK, returnIntent);
			} else {
				Toast.makeText(ActivityCameraCrop.this,
						"Failed to send photo", Toast.LENGTH_LONG)
						.show();
				setResult(Activity.RESULT_CANCELED, returnIntent);
			}

			finish();
		}
	}

	// We can be in one of these 3 states
	private enum States {
		NONE, DRAG, ZOOM
	}

//	private static final int NONE = 0;
//	private static final int DRAG = 1;
//	private static final int ZOOM = 2;
//	private int mode = NONE;

	// Remember some things for zooming
	private PointF start = new PointF();
	private PointF mid = new PointF();
	private float oldDist = 1f;

	private CroppedImageView mCropImageView;

	public int crop_container_size;

	// Private request codes used in this Activity
	private static final int GALLERY_IMAGE_REQUEST_CODE = 200;
	private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
	// Public constants when calling this Activity for request codes
	public static final int PICK_CAMERA_REQUEST = 1001;
	public static final int PICK_GALLERY_REQUEST = 1002;
	public static final int MEDIA_TYPE_IMAGE = 1;
	// Uri for captured image so we can get image path
	private String _path;

	// directory name to store captured images and videos
	private static final String IMAGE_DIRECTORY_NAME = "images";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_crop);
		mCropImageView = (CroppedImageView) findViewById(R.id.ivCameraCropPhoto);
		mCropImageView.setDrawingCacheEnabled(true);
		Button ok = (Button) findViewById(R.id.btnCameraOk);
		ok.setTypeface(ConversaApp.getTfRalewayRegular());
		ok.setOnClickListener(this);

		getImageIntents();
	}

	private void getImageIntents() {
		if (getIntent().getStringExtra("type").equals("gallery")) {
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(intent, GALLERY_IMAGE_REQUEST_CODE);
		} else {
			startCamera();
		}
	}

	public void startCamera() {
		// Checking device has camera hardware or not
		if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			try {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				Uri fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
				// If you specified MediaStore.EXTRA_OUTPUT, the image taken will be written
				// to that path, and no data will given to onActivityResult
				intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
				startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
			} catch (Exception ex) {
				Toast.makeText(this, "No camera on device", Toast.LENGTH_LONG)
						.show();
				finish();
			}
		} else {
			Toast.makeText(this, "No camera on device", Toast.LENGTH_LONG).show();
			finish();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case GALLERY_IMAGE_REQUEST_CODE: {
					if (data == null) {
						finish();
					} else {
						onPhotoTaken(data, false);
					}
					break;
				}
				case CAMERA_CAPTURE_IMAGE_REQUEST_CODE: {
					onPhotoTaken(data, true);
					break;
				}
				default:
					finish();
					break;
			}
		} else {
			finish();
		}
	}

	public Uri getOutputMediaFileUri(int type) throws IOException, NullPointerException {
		return Uri.fromFile(getOutputMediaFile(type));
	}

	private File getOutputMediaFile(int type) throws IOException {
		// Create the File where the photo should go //
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
					+ "IMG_" + timeStamp + ".jpg");
		} else {
			return null;
		}

		_path = mediaFile.getPath();

		return mediaFile;
	}

	protected void onPhotoTaken(Intent data, boolean fromCamera) {
		String path;

		if (fromCamera) {
			path = _path;
		} else {
			Uri selected_image = data.getData();
			path = getImagePath(selected_image);
		}

		new AsyncTask<String, Void, Bitmap>() {

			@Override
			protected Bitmap doInBackground(String... params) {
				try {
					if (params == null || params.length == 0) {
						return null;
					}

					File f = new File(params[0]);

					BitmapFactory.Options optionsMeta = new BitmapFactory.Options();
					optionsMeta.inJustDecodeBounds = true;
					BitmapFactory.decodeFile(f.getAbsolutePath(), optionsMeta);
					Bitmap mBitmap;

					if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
						BitmapFactory.Options options = new BitmapFactory.Options();
						options.inPurgeable = true;
						options.inInputShareable = true;
						mBitmap = BitmapFactory.decodeStream(
								new FileInputStream(f), null, options);
					} else {
						mBitmap = BitmapFactory.decodeStream(
								new FileInputStream(f));
					}

					mBitmap = Bitmap.createBitmap(mBitmap, 0, 0,
							mBitmap.getWidth(), mBitmap.getHeight());

					return _scaleBitmap(mBitmap);
				} catch (Exception ex) {
					Logger.error("doInBackground", ex.getMessage());
					return null;
				}
			}

			@Override
			protected void onPostExecute(Bitmap result) {
				if (result != null) {
					scaleView(result);
					//mCropImageView.setImageBitmap(result);
					mCropImageView.setScaleType(ImageView.ScaleType.MATRIX);
					matrix = new Matrix();//translateMatrix;
				}
			}
		}.execute(path);
	}

	private boolean saveBitmapToFile(Bitmap bitmap, String path) {
		File file = new File(path);
		FileOutputStream fOut;
		try {
			fOut = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
			fOut.flush();
			fOut.close();
			return true;
		} catch (IOException e) {
			Logger.error("saveBitmapToFile", e.getMessage());
		}

		return false;
	}

	private String getImagePath(Uri uri) {
		return ImageFilePath.getPath(getApplicationContext(), uri);
	}

	/***********************************************************************************/
	/************************************IMAGE DRAW*************************************/
	/***********************************************************************************/
    public void scaleView(Bitmap mBitmap) {
        View top_view = findViewById(R.id.topView);
        View bottom_view = findViewById(R.id.bottomView);
        RelativeLayout footer = (RelativeLayout) findViewById(R.id.llFooter);
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
        float top_view_height = ((float) (height - crop_container_size - footer.getHeight())) / (float) 2;
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
                state = States.DRAG;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    state = States.ZOOM;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                state = States.NONE;
                break;
            case MotionEvent.ACTION_MOVE:
                if (state == States.DRAG) {
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x, event.getY()
                            - start.y);
                } else if (state == States.ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = newDist / oldDist;
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
		if (bgDrawable != null) {
			bgDrawable.draw(canvas);
		} else {
			canvas.drawColor(Color.BLACK);
		}
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

	private Bitmap _scaleBitmap(Bitmap mBitmap) {
		int image_width = mBitmap.getWidth();
		int image_height = mBitmap.getHeight();
		int new_image_width;
		int new_image_height;
		int _screen_width = 800;

		if (image_width >= image_height) {
			if (image_height < _screen_width) {
				new_image_width = (int) ((float) image_width * ((float) _screen_width / (float) image_height));
			} else {
				new_image_width = (int) ((float) image_width / ((float) image_height / (float) _screen_width)); // ok
			}

			mBitmap = Bitmap.createScaledBitmap(mBitmap, new_image_width, _screen_width, true);
		} else if (image_width < image_height) {
			if (image_width < _screen_width) {
				new_image_height = (int) ((float) image_height * ((float) _screen_width / (float) image_width));
			} else {
				new_image_height = (int) ((float) image_height / ((float) image_width / (float) _screen_width));
			}

			mBitmap = Bitmap.createScaledBitmap(mBitmap, _screen_width, new_image_height, true);
		}

		return mBitmap;
	}
}
