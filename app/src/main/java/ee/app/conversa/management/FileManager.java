package ee.app.conversa.management;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;

/**
 * Created by edgargomez on 8/15/16.
 *
 * Help from:
 * 1. http://stackoverflow.com/questions/7887078/android-saving-file-to-external-storage
 * 2. http://stackoverflow.com/questions/4595334/get-free-space-on-internal-memory
 */
public class FileManager {

    public enum savedOnStorage {
        INTERNAL, EXTERNAL, NONE
    }

    public static String storageSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    private void createDirectoryAndSaveFile(Bitmap imageToSave, String fileName) {
        String path = Environment.getExternalStorageDirectory() + "/DirName";
        String internalpath = Environment.getExternalStorageDirectory().getPath() + "/DirName/";

        File direct = new File(path);
        // when is a directory getFreeSpace() or getTotalSpace()

        if (!direct.exists()) {
            File wallpaperDirectory = new File(internalpath);
            wallpaperDirectory.mkdirs();
        }

        File file = new File(new File(internalpath), fileName);

        if (file.exists()) {
            file.delete();
        }

        try {
            FileOutputStream out = new FileOutputStream(file);
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to check whether external media available and writable. This is
     * adapted from
     * http://developer.android.com/guide/topics/data/data-storage.html
     * #filesExternal
     */
    private static boolean checkExternalMedia() {
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // Can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // Can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Can't read or write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }

        return (mExternalStorageAvailable && mExternalStorageWriteable);
    }

    /**
     * Method to write ascii text characters to file on SD card. Note that you
     * must add a WRITE_EXTERNAL_STORAGE permission to the manifest file or this
     * method will throw a FileNotFound Exception because you won't have write
     * permission.
     */
    private void writeToSDFile() {
        // Find the root of the external storage.
        // See http://developer.android.com/guide/topics/data/data-
        // storage.html#filesExternal
        File root = android.os.Environment.getExternalStorageDirectory();
        // See
        // http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder
        File dir = new File(root.getAbsolutePath() + "/download");
        dir.mkdirs();
        File file = new File(dir, "myData.txt");
        try {
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);
            pw.println("Hi , How are you");
            pw.println("Hello");
            pw.flush();
            pw.close();
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i("fadsfads", "******* File not found. Did you"
                    + " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static savedOnStorage saveToInternalStorage(Context context, byte[] data, String filename) {
        // Create a file in the Internal Storage
        try {
            FileOutputStream outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(data);
            outputStream.flush();
            outputStream.close();
            return savedOnStorage.INTERNAL;
        } catch (Exception e) {
            e.printStackTrace();
            return savedOnStorage.NONE;
        }
    }

    public static savedOnStorage saveToExternalStorage(Context context, byte[] data, String filename) {
        if (!checkExternalMedia()) {
            return savedOnStorage.NONE;
        }

        if(freeExternalMemory() < data.length) {
            return savedOnStorage.NONE;
        }

        // Create a file in the External Storage
        try {
            FileOutputStream outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(data);
            outputStream.flush();
            outputStream.close();
            return savedOnStorage.EXTERNAL;
        } catch (Exception e) {
            e.printStackTrace();
            return savedOnStorage.NONE;
        }
    }

    public static boolean deleteFileWithName(Context context, String name) {
        return context.deleteFile(name);
    }

    /**
     * @return Number of bytes available on internal storage
     */
    public static long freeInternalMemory() {
        StatFs statFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
        long free;

        if (Build.VERSION.SDK_INT >= 18) {
            free = statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong();
        } else {
            free = (statFs.getAvailableBlocks() * (long) statFs.getBlockSize());
        }

        return free;
    }

    /**
     * @return Number of bytes available on external storage
     */
    public static long freeExternalMemory() {
        StatFs statEFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
        long free;

        if (Build.VERSION.SDK_INT >= 18) {
            free = statEFs.getAvailableBlocksLong() * statEFs.getBlockSizeLong();
        } else {
            free = (statEFs.getAvailableBlocks() * (long) statEFs.getBlockSize());
        }

        return free;
    }

}
