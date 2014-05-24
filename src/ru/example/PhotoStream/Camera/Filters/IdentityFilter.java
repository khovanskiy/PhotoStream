package ru.example.PhotoStream.Camera.Filters;

import android.graphics.Bitmap;
import android.widget.ImageView;
import ru.example.PhotoStream.Camera.RawBitmap;

public class IdentityFilter implements PhotoFilter {
    @Override
    public synchronized void transformOpaqueRaw(RawBitmap bitmap) {

    }

    @Override
    public void transformOpaque(Bitmap bitmap) {

    }

    @Override
    public boolean hasPreviewModification() {
        return false;
    }

    @Override
    public void modifyPreview(ImageView view) {

    }
}
