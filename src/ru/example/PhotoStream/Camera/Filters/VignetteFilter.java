package ru.example.PhotoStream.Camera.Filters;

import android.graphics.Color;
import ru.example.PhotoStream.Camera.RawBitmap;

/**
 * Created by Genyaz on 05.08.2014.
 */
public class VignetteFilter extends TunablePhotoFilter {
    @Override
    protected void transformOpaqueRaw(RawBitmap source, RawBitmap destination, double strength) {
        final int centerX = source.width / 2, centerY = source.height / 2;
        double s;
        int color;
        for (int i = 0; i < source.height; i++) {
            for (int j = 0; j < source.width; j++) {
                color = source.colors[i * source.width + j];
                s = 1 - (Math.abs(j - centerX) * 1f / centerX) * (Math.abs(i - centerY) * 1f / centerY) * strength;
                destination.colors[i * destination.width + j] = Color.rgb((int)(s * Color.red(color)),
                        (int)(s * Color.green(color)), (int)(s * Color.blue(color)));
            }
        }
    }

    @Override
    public TunableType getType() {
        return TunableType.Vignette;
    }
}
