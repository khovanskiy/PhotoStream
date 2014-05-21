package ru.example.PhotoStream.Camera;

import android.graphics.Bitmap;

public interface PictureBitmapCallback {
    /**
     * Action on taking {@link ru.example.PhotoStream.Camera.CameraPreview} picture.
     *
     * @param bitmap picture in {@link android.graphics.Bitmap} format.
     */
    public void onPictureTaken(Bitmap bitmap);
}
