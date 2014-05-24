package ru.example.PhotoStream.Camera.Filters;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.widget.ImageView;
import ru.example.PhotoStream.Camera.RawBitmap;

/**
 * Created by Genyaz on 23.05.2014.
 */
public class ColorMatrixPhotoFilter implements PhotoFilter {

    private ColorMatrix colorMatrix;

    public ColorMatrixPhotoFilter(ColorMatrix colorMatrix) {
        this.colorMatrix = colorMatrix;
    }

    @Override
    public void transformOpaqueRaw(RawBitmap bitmap) {
        int c, a0, r0, g0, b0;
        float a1, r1, g1, b1;
        float[] m = colorMatrix.getArray();
        int imageSize = bitmap.height * bitmap.width;
        for (int i = 0; i < imageSize; i++) {
            c = bitmap.colors[i];
            a0 = Color.alpha(c);
            r0 = Color.red(c);
            g0 = Color.green(c);
            b0 = Color.green(c);
            r1 = m[0] * r0 + m[1] * g0 + m[2] * b0 + m[3] * a0 + m[4];
            g1 = m[5] * r0 + m[6] * g0 + m[7] * b0 + m[8] * a0 + m[9];
            b1 = m[10] * r0 + m[11] * g0 + m[12] * b0 + m[13] * a0 + m[14];
            a1 = m[15] * r0 + m[16] * g0 + m[17] * b0 + m[18] * a0 + m[19];
            r0 = (r1 < 0 ? 0 : r1 > 255 ? 255 : (int)r1);
            g0 = (g1 < 0 ? 0 : g1 > 255 ? 255 : (int)g1);
            b0 = (b1 < 0 ? 0 : b1 > 255 ? 255 : (int)b1);
            a0 = (a1 < 0 ? 0 : a1 > 255 ? 255 : (int)a1);
            bitmap.colors[i] = Color.argb(a0, r0, g0, b0);
        }
    }

    @Override
    public void transformOpaque(Bitmap bitmap) {

    }

    @Override
    public boolean hasPreviewModification() {
        return true;
    }

    @Override
    public void modifyPreview(ImageView view) {
        view.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
    }
}
