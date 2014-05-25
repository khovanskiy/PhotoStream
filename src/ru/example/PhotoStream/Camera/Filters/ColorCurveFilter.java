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

    @Override
    public void transformOpaqueRaw(RawBitmap bitmap) {
        int c, imageSize = bitmap.width * bitmap.height;
        for (int i = 0; i < 256; i++) {
            int r = redCurve(i);
            int g = greenCurve(i);
            int b = blueCurve(i);
        }
        for (int i = 0; i < imageSize; i++) {
            c = bitmap.colors[i];
            bitmap.colors[i] = Color.argb(255, redCurve(Color.red(c)), greenCurve(Color.green(c)), blueCurve(Color.blue(c)));
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
