package ru.example.PhotoStream.Camera.Filters;

import android.graphics.Color;
import android.graphics.ColorMatrix;
import ru.example.PhotoStream.Camera.RawBitmap;

public class ColorMatrixPhotoFilter extends TunablePhotoFilter {

    private float[] m;

    public ColorMatrixPhotoFilter(float[] m) {
        this.m = m;
        setStrength(0);
    }

    @Override
    public void transformOpaqueRaw(RawBitmap source, RawBitmap destination, double strength) {
        int c, a0, r0, g0, b0;
        float a1, r1, g1, b1;
        int imageSize = source.height * source.width;
        for (int i = 0; i < imageSize; i++) {
            c = source.colors[i];
            a0 = Color.alpha(c);
            r0 = Color.red(c);
            g0 = Color.green(c);
            b0 = Color.blue(c);
            r1 = m[0] * r0 + m[1] * g0 + m[2] * b0 + m[3] * a0 + m[4];
            g1 = m[5] * r0 + m[6] * g0 + m[7] * b0 + m[8] * a0 + m[9];
            b1 = m[10] * r0 + m[11] * g0 + m[12] * b0 + m[13] * a0 + m[14];
            a1 = m[15] * r0 + m[16] * g0 + m[17] * b0 + m[18] * a0 + m[19];
            r0 = (int)(r1 * strength + (1 - strength) * r0);
            g0 = (int)(g1 * strength + (1 - strength) * g0);
            b0 = (int)(b1 * strength + (1 - strength) * b0);
            a0 = (int)(a1 * strength + (1 - strength) * a0);
            r0 = (r0 < 0 ? 0 : r0 > 255 ? 255 : r0);
            g0 = (g0 < 0 ? 0 : g0 > 255 ? 255 : g0);
            b0 = (b0 < 0 ? 0 : b0 > 255 ? 255 : b0);
            a0 = (a0 < 0 ? 0 : a0 > 255 ? 255 : a0);
            destination.colors[i] = Color.argb(a0, r0, g0, b0);
        }
    }

    @Override
    public TunableType getType() {
        return TunableType.ColorMatrix;
    }
}
