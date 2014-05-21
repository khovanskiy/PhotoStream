package ru.example.PhotoStream.Camera.Filters;

import android.graphics.Bitmap;
import ru.example.PhotoStream.Camera.RawBitmap;

public class IdentityFilter implements PhotoFilter {
    @Override
    public synchronized void transformOpaqueRaw(RawBitmap bitmap) {

    }

    @Override
    public void transformOpaque(Bitmap bitmap) {

    }
}