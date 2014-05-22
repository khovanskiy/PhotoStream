package ru.example.PhotoStream.Camera.Filters;

import android.graphics.Bitmap;
import ru.example.PhotoStream.Camera.RawBitmap;

public interface PhotoFilter {
    /**
     * Transforms bitmap without alpha channel.
     *
     * @param bitmap bitmap in raw format.
     */
    public void transformOpaqueRaw(RawBitmap bitmap);

    /**
     * Transforms bitmap without alpha channel.
     *
     * @param bitmap bitmap in standard format.
     */
    public void transformOpaque(Bitmap bitmap);
}