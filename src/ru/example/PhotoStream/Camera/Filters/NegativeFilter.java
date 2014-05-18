package ru.example.PhotoStream.Camera.Filters;

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
        int h = bitmap.height, w = bitmap.width;
        int[][] r = bitmap.r, g = bitmap.g, b = bitmap.b;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                r[i][j] = 255 - r[i][j];
                g[i][j] = 255 - g[i][j];
                b[i][j] = 255 - b[i][j];
            }
        }
    }
}
