package ru.example.PhotoStream.Camera.Filters;

import android.graphics.Bitmap;
import android.widget.ImageView;
import ru.example.PhotoStream.Camera.RawBitmap;

public class NegativeFilter implements PhotoFilter {

    @Override
    public synchronized void transformOpaqueRaw(RawBitmap bitmap) {
        int frameSize = bitmap.height * bitmap.width;
        for (int i = 0; i < frameSize; i++) {
            bitmap.colors[i] ^= 0x00FFFFFF;
        }
    }

    @Override
    public void transformOpaque(Bitmap bitmap) {
        int w = bitmap.getWidth(), h = bitmap.getHeight();
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                bitmap.setPixel(j, i, bitmap.getPixel(j, i) ^ 0x00FFFFFF);
            }
        }
    }

    @Override
    public boolean hasPreviewModification() {
        return false;
    }

    @Override
    public void modifyPreview(ImageView view) {

    }
}
