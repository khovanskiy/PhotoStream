package ru.example.PhotoStream.Camera.Filters;

import android.graphics.Bitmap;
import android.widget.ImageView;
import ru.example.PhotoStream.Camera.RawBitmap;

public class IdentityFilter implements PhotoFilter {
    @Override
    public synchronized void transformOpaqueRaw(RawBitmap bitmap) {

    }

    @Override
    public synchronized boolean hasPreviewModification() {
        return true;
    }

    @Override
    public synchronized void modifyPreview(ImageView view) {

    }
}
