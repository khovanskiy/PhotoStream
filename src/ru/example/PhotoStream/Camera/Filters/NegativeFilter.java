package ru.example.PhotoStream.Camera.Filters;

import android.graphics.Color;
import ru.example.PhotoStream.Camera.RawBitmap;

/**
 * Created by Genyaz on 18.05.2014.
 */
public class NegativeFilter implements PhotoFilter {

    @Override
    public synchronized void transform(RawBitmap bitmap) {
        transformOpaque(bitmap);
    }

    @Override
    public synchronized void transformOpaque(RawBitmap bitmap) {
        int frameSize = bitmap.height * bitmap.width;
        for (int i = 0; i < frameSize; i++) {
            bitmap.colors[i] ^= 0x00FFFFFF;
        }
    }
}
