package com.afollestad.materialcamera.util;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import com.afollestad.materialcamera.CompleteCallback;
import com.afollestad.materialcamera.ICallback;
import com.afollestad.materialcamera.ICompressCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.afollestad.materialcamera.util.Degrees.DEGREES_270;
import static com.afollestad.materialcamera.util.Degrees.DEGREES_90;

/**
 * Created by tomiurankar on 06/03/16.
 */
public class ImageUtil {

    /**
     * Method for return file path of Gallery image
     *
     * @param context
     * @param uri
     * @return path of the selected image file from gallery
     */
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {

        // Check here to KITKAT or new version
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {

            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;

                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * Saves byte[] array to disk
     *
     * @param input    byte array
     * @param output   path to output file
     * @param callback will always return in originating thread
     */
    public static void saveToDiskWithCompressionAsync(final byte[] input, final File output,
                                                      final ICompressCallback callback) {
        final Handler handler = new Handler();
        new Thread() {
            @Override
            public void run() {
                try {
                    FileOutputStream outputStream = new FileOutputStream(output);
                    outputStream.write(input);
                    outputStream.flush();
                    outputStream.close();
                    final String outputPath = compressImage(output.getAbsolutePath(),
                            output.getAbsolutePath());
                    if (outputPath == null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.done(new Exception("Image compress failed"), -1);
                            }
                        });
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.done(null, new File(outputPath).length());
                            }
                        });
                    }
                } catch (final Exception e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.done(e, -1);
                        }
                    });
                }
            }
        }.start();
    }

    /**
     * Saves byte[] array to disk
     *
     * @param input    byte array
     * @param output   path to output file
     * @param callback will always return in originating thread
     */
    public static void saveToDiskAsync(final byte[] input, final File output,
                                       final ICallback callback) {
        final Handler handler = new Handler();
        new Thread() {
            @Override
            public void run() {
                try {
                    FileOutputStream outputStream = new FileOutputStream(output);
                    outputStream.write(input);
                    outputStream.flush();
                    outputStream.close();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.done(null);
                        }
                    });
                } catch (final Exception e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.done(e);
                        }
                    });
                }
            }
        }.start();
    }

    /**
     * Rotates the bitmap per their EXIF flag. This is a recursive function that will
     * be called again if the image needs to be downsized more.
     *
     * @param inputFile Expects an JPEG file if corrected orientation wants to be set.
     * @return rotated bitmap or null
     */
    @Nullable
    public static void getRotatedBitmap(String inputFile, int reqWidth, int reqHeight,
                                        CompleteCallback callback) {
        new AsyncTask<Object, Void, Bitmap>() {

            CompleteCallback callback;

            @Override
            protected Bitmap doInBackground(Object... params) {
                try {
                    callback = (CompleteCallback) params[3];

                    File f = new File(params[0].toString());

                    if (!f.exists()) {
                        return null;
                    }

                    final int rotationInDegrees = getExifDegreesFromJpeg(f.getAbsolutePath());

                    BitmapFactory.Options optionsMeta = new BitmapFactory.Options();
                    optionsMeta.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(f.getAbsolutePath(), optionsMeta);
                    optionsMeta.inSampleSize = calculateInSampleSize(optionsMeta,
                            (Integer)params[1],
                            (Integer)params[2],
                            rotationInDegrees);
                    optionsMeta.inJustDecodeBounds = false;

                    Bitmap mBitmap;
                    FileInputStream fis = new FileInputStream(f);

                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPurgeable = true;
                        options.inInputShareable = true;
                        mBitmap = BitmapFactory.decodeStream(fis, null, options);
                    } else {
                        mBitmap = BitmapFactory.decodeStream(fis);
                    }

                    fis.close();

                    if (mBitmap == null) {
                        return null;
                    }

                    Matrix matrix = new Matrix();
                    matrix.preRotate(rotationInDegrees);
                    // we need not check if the rotation is not needed, since the below function
                    // will then return the same bitmap. Thus no memory loss occurs.
                    return Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(),
                            mBitmap.getHeight(), matrix, true);
                } catch (Exception ex) {
                    Log.e("getRotatedBitmap", ex.getMessage());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                if (callback != null) {
                    callback.done(result);
                }
            }
        }.execute(inputFile, reqWidth, reqHeight, callback);
    }

    private static int getExifDegreesFromJpeg(String inputFile) {
        try {
            final ExifInterface exif = new ExifInterface(inputFile);
            final int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
                return 90;
            } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
                return 180;
            } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
                return 270;
            }
        } catch (IOException e) {
            Log.e("exif", "Error when trying to get exif data from : " + inputFile, e);
        }
        return 0;
    }

    /**
     * - Image Compression without affecting Image quality in Android -
     * http://grishma102.blogspot.com/2016/03/image-compression-in-android.html
     * - Whatsapp like Image Compression in Android with demo -
     * http://voidcanvas.com/whatsapp-like-image-compression-in-android/
     */
    @SuppressWarnings("SuspiciousNameCombination")
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight, int rotationInDegrees) {
        // Raw height and width of image
        final int height;
        final int width;
        // We begin inSampleSize to 2 so we guarantee the new image
        // will be at least half of the size if one of his dimensions
        // exceeds the requested width or height
        int inSampleSize = 2; // <-- This line was edited -->

        // Check for rotation
        if(rotationInDegrees == DEGREES_90 || rotationInDegrees == DEGREES_270) {
            width = options.outHeight;
            height = options.outWidth;
        } else {
            height = options.outHeight;
            width = options.outWidth;
        }

        // <-- This block was added -->
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width shorter or equal than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        } else {
            // If the image is not larger in width and height to the requested height and
            // width we don't need to resize the image
            inSampleSize = 1;
        }
        // <-- This block was added -->
        return inSampleSize;
    }

    private static final float maxHeight = 600.0f;
    private static final float maxWidth = 800.0f;

    @SuppressWarnings({"UnusedAssignment", "ResultOfMethodCallIgnored"})
    private static String compressImage(String imagePath, String outputPath) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            // By setting this field as true, the actual bitmap pixels are not loaded in the memory.
            // Just the bounds are loaded. If you try the use the bitmap here, you will get null.
            options.inJustDecodeBounds = true;
            Bitmap bmp = BitmapFactory.decodeFile(imagePath, options);

            int actualHeight = options.outHeight;
            int actualWidth = options.outWidth;

            float imgRatio = (float) actualWidth / (float) actualHeight;
            float maxRatio = maxWidth / maxHeight;

            // width and height values are set maintaining the aspect ratio of the image
            if (actualHeight > maxHeight || actualWidth > maxWidth) {
                if (imgRatio < maxRatio) {
                    // If image is larger in height to the max image height
                    imgRatio = maxHeight / actualHeight;
                    actualWidth = (int) (imgRatio * actualWidth);
                    actualHeight = (int) maxHeight;
                } else if (imgRatio > maxRatio) {
                    // If image is larger in width to the max image width
                    imgRatio = maxWidth / actualWidth;
                    actualHeight = (int) (imgRatio * actualHeight);
                    actualWidth = (int) maxWidth;
                }  else {
                    if (actualHeight > actualWidth) {
                        actualHeight = (int) maxWidth;
                        actualWidth = (int) maxHeight;
                    } else if (actualHeight < actualWidth) {
                        actualHeight = (int) maxHeight;
                        actualWidth = (int) maxWidth;
                    } else {
                        actualHeight = (int) maxHeight;
                        actualWidth = (int) maxHeight;
                    }
                }
            }

            // Check the rotation of the image
            final int rotationInDegrees = getExifDegreesFromJpeg(imagePath);
            // Setting inSampleSize value allows to load a scaled down version of the original image
            options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight, rotationInDegrees);
            // inJustDecodeBounds set to false to load the actual bitmap
            options.inJustDecodeBounds = false;
            // This options allow android to claim the bitmap memory if it runs low on memory
            options.inDither = true;
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inTempStorage = new byte[16 * 1024];

            bmp = BitmapFactory.decodeFile(imagePath, options);
            Bitmap scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.RGB_565);

            float ratioX = actualWidth / (float) options.outWidth;
            float ratioY = actualHeight / (float) options.outHeight;
            float middleX = actualWidth / 2.0f;
            float middleY = actualHeight / 2.0f;

            Matrix scaleMatrix = new Matrix();
            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

            Canvas canvas = new Canvas(scaledBitmap);
            canvas.setMatrix(scaleMatrix);
            canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

            // Free bitmap as we will no longer reference to it
            bmp.recycle();

            Matrix matrix = new Matrix();
            matrix.preRotate(rotationInDegrees); // <-- This line was added -->
            //matrix.postRotate(rotationInDegrees);
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

            // Delete the old uncompress image
            new File(imagePath).delete();
            FileOutputStream out = new FileOutputStream(outputPath);
            // Write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.close();

            return outputPath;
        } catch (Exception e) {
            Log.e("compressImage", e.getMessage());
            return null;
        }
    }

}