package ru.example.PhotoStream.Camera.Filters;

import android.graphics.Color;
import ru.example.PhotoStream.Camera.RawBitmap;

/**
 * Created by Genyaz on 18.05.2014.
 */
public class BlackAndWhiteFilter implements PhotoFilter {

    @Override
    public synchronized void transform(RawBitmap bitmap) {
        transformOpaque(bitmap);
    }

    @Override
    public synchronized void transformOpaque(RawBitmap bitmap) {
        int frameSize = bitmap.height * bitmap.width;
        int c, avg;
        for (int i = 0; i < frameSize; i++) {
            c = bitmap.colors[i];
            avg = (Color.red(c) + Color.green(c) + Color.blue(c)) / 3;
            bitmap.colors[i] = Color.argb(Color.alpha(c), avg, avg, avg);
        }
    }
}
