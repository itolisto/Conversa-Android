package com.afollestad.materialcamera;

import android.graphics.Bitmap;

/**
 * Created by edgargomez on 10/19/16.
 */

public interface CompleteCallback {
    /**
     * It is called when the background operation completes.
     * If the operation is unsuccessful, {@code bitmap} will be {@code null}.
     */
    void done(Bitmap bitmap);
}