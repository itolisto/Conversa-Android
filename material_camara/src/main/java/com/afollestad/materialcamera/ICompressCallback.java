package com.afollestad.materialcamera;

public interface ICompressCallback {
    /**
     * It is called when the background operation completes.
     * If the operation is successful, {@code exception} will be {@code null}.
     */
    void done(Exception exception, long newSize);
}