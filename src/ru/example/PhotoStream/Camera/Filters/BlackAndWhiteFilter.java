package ru.example.PhotoStream.Camera.Filters;

import android.graphics.Bitmap;
import android.graphics.Color;
import ru.example.PhotoStream.Camera.RawBitmap;

public class BlackAndWhiteFilter implements PhotoFilter {

    @Override
    public synchronized void transformOpaqueRaw(RawBitmap bitmap) {
        int frameSize = bitmap.height * bitmap.width;
        int c, avg;
        for (int i = 0; i < frameSize; i++) {
            c = bitmap.colors[i];
            avg = (Color.red(c) + Color.green(c) + Color.blue(c)) / 3;
            bitmap.colors[i] = Color.argb(Color.alpha(c), avg, avg, avg);
        }
    }

    @Override
    public void transformOpaque(Bitmap bitmap) {
        int w = bitmap.getWidth(), h = bitmap.getHeight(), c, avg;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                c = bitmap.getPixel(j, i);
                avg = (Color.red(c) + Color.green(c) + Color.blue(c)) / 3;
                bitmap.setPixel(j, i, Color.argb(Color.alpha(c), avg, avg, avg));
            }
        }
    }
}