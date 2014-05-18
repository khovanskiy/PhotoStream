package ru.example.PhotoStream.Camera.Filters;

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
        int h = bitmap.height, w = bitmap.width;
        int[][] r = bitmap.r, g = bitmap.g, b = bitmap.b;
        int avg;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                avg = (r[i][j] + g[i][j] + b[i][j]) / 3;
                r[i][j] = avg;
                g[i][j] = avg;
                b[i][j] = avg;
            }
        }
    }
}
