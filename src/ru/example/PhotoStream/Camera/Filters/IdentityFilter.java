package ru.example.PhotoStream.Camera.Filters;

import ru.example.PhotoStream.Camera.RawBitmap;

/**
 * Created by Genyaz on 18.05.2014.
 */
public class IdentityFilter implements PhotoFilter {
    @Override
    public synchronized void transform(RawBitmap bitmap) {

    }

    @Override
    public synchronized void transformOpaque(RawBitmap bitmap) {

    }
}
