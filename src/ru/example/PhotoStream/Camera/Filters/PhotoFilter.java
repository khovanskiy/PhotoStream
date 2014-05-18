package ru.example.PhotoStream.Camera.Filters;

import ru.example.PhotoStream.Camera.RawBitmap;

/**
 * Created by Genyaz on 17.05.2014.
 */
public interface PhotoFilter {
    /**
     * Transforms any bitmap.
     *
     * @param bitmap bitmap in raw format.
     */
    public void transform(RawBitmap bitmap);

    /**
     * Transforms bitmap without alpha channel.
     *
     * @param bitmap bitmap in raw format.
     */
    public void transformOpaque(RawBitmap bitmap);
}
