package ru.example.PhotoStream.Camera.Filters;

import ru.example.PhotoStream.Camera.RawBitmap;

public interface PhotoFilter {
    /**
     * Transforms bitmap without alpha channel.
     *
     * @param bitmap bitmap in raw format.
     */
    public void transformOpaque(RawBitmap bitmap);
}
