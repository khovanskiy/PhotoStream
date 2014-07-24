package ru.example.PhotoStream.Camera.Filters;

import ru.example.PhotoStream.Camera.RawBitmap;

public interface PhotoFilter {
    /**
     * Transforms bitmap without alpha channel.
     * @param source source of pixels
     * @param destination new bitmap
     */
    public void transformOpaqueRaw(RawBitmap source, RawBitmap destination);
}
