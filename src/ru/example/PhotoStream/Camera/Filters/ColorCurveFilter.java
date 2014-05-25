package ru.example.PhotoStream.Camera.Filters;

import android.graphics.Color;
import android.widget.ImageView;
import ru.example.PhotoStream.Camera.RawBitmap;

/**
 * Created by Genyaz on 25.05.2014.
 */
public abstract class ColorCurveFilter implements PhotoFilter {
    protected abstract int redCurve(int redSource);
    protected abstract int greenCurve(int greenSource);
    protected abstract int blueCurve(int blueSource);
    private int[] r = null, g = null, b = null;

    @Override
    public void transformOpaqueRaw(RawBitmap bitmap) {
        if (r == null) {
            r = new int[256];
            g = new int[256];
            b = new int[256];
            for (int i = 0; i < 256; i++) {
                r[i] = redCurve(i);
                g[i] = greenCurve(i);
                b[i] = blueCurve(i);
            }
        }
        int c, imageSize = bitmap.width * bitmap.height;
        for (int i = 0; i < imageSize; i++) {
            c = bitmap.colors[i];
            bitmap.colors[i] = Color.argb(255, r[Color.red(c)], g[Color.green(c)], b[Color.blue(c)]);
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
