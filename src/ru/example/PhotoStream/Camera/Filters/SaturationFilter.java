package ru.example.PhotoStream.Camera.Filters;

import android.graphics.Color;
import ru.example.PhotoStream.Camera.RawBitmap;

/**
 * Created by Genyaz on 22.07.2014.
 */
public class SaturationFilter extends TunablePhotoFilter {
    @Override
    protected void transformOpaqueRaw(RawBitmap source, RawBitmap destination, double strength) {
        double multiplier = strength + 1;
        int imageSize = source.width * source.height;
        float[] hsv = new float[3];
        for (int i = 0; i < imageSize; i++) {
            Color.colorToHSV(source.colors[i], hsv);
            hsv[1] *= multiplier;
            if (hsv[1] > 1) {
                hsv[1] = 1;
            }
            destination.colors[i] = Color.HSVToColor(hsv);
        }
    }

    @Override
    public TunableType getType() {
        return TunableType.Saturation;
    }
}
