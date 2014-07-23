package ru.example.PhotoStream.Camera.Filters;

import android.graphics.Color;
import ru.example.PhotoStream.Camera.RawBitmap;

public class ColorCurveFilter extends TunablePhotoFilter {
    private int[] r0, g0, b0, r, g, b;
    private ColorCurveProvider colorCurveProvider = null;

    public ColorCurveFilter(ColorCurve colorCurve) {
        r0 = new int[256];
        g0 = new int[256];
        b0 = new int[256];
        colorCurve.fillColors(r0, g0, b0);
        r = new int[256];
        g = new int[256];
        b = new int[256];
        setStrength(0);
    }

    public ColorCurveFilter(ColorCurveProvider colorCurveProvider) {
        r = new int[256];
        g = new int[256];
        b = new int[256];
        this.colorCurveProvider = colorCurveProvider;
        setStrength(0);
    }

    public ColorCurveFilter(ColorCurveFilter first, ColorCurveFilter second) {
        r0 = new int[256];
        g0 = new int[256];
        b0 = new int[256];
        r = new int[256];
        g = new int[256];
        b = new int[256];
        int[] r1 = new int[256], g1 = new int[256], b1 = new int[256];
        if (first.colorCurveProvider != null) {
            first.colorCurveProvider.getColorCurve(first.getStrength()).fillColors(r, g, b);
        } else {
            double strength = first.getStrength();
            for (int i = 0; i < 256; i++) {
                r[i] = (int) (strength * first.r0[i] + (1 - strength) * i);
                r[i] = Math.max(0, Math.min(255, r[i]));
                g[i] = (int) (strength * first.g0[i] + (1 - strength) * i);
                g[i] = Math.max(0, Math.min(255, g[i]));
                b[i] = (int) (strength * first.b0[i] + (1 - strength) * i);
                b[i] = Math.max(0, Math.min(255, b[i]));
            }
        }
        if (second.colorCurveProvider != null) {
            second.colorCurveProvider.getColorCurve(second.getStrength()).fillColors(r1, g1, b1);
        } else {
            double strength = second.getStrength();
            for (int i = 0; i < 256; i++) {
                r1[i] = (int) (strength * second.r0[i] + (1 - strength) * i);
                r1[i] = Math.max(0, Math.min(255, r1[i]));
                g1[i] = (int) (strength * second.g0[i] + (1 - strength) * i);
                g1[i] = Math.max(0, Math.min(255, g1[i]));
                b1[i] = (int) (strength * second.b0[i] + (1 - strength) * i);
                b1[i] = Math.max(0, Math.min(255, b1[i]));
            }
        }
        for (int i = 0; i < 256; i++) {
            r0[i] = r1[r[i]];
            g0[i] = g1[g[i]];
            b0[i] = b1[b[i]];
        }
        setStrength(1);
    }

    @Override
    public void transformOpaqueRaw(RawBitmap source, RawBitmap destination, double strength) {
        if (colorCurveProvider == null) {
            for (int i = 0; i < 256; i++) {
                r[i] = (int) (strength * this.r0[i] + (1 - strength) * i);
                r[i] = Math.max(0, Math.min(255, r[i]));
                g[i] = (int) (strength * this.g0[i] + (1 - strength) * i);
                g[i] = Math.max(0, Math.min(255, g[i]));
                b[i] = (int) (strength * this.b0[i] + (1 - strength) * i);
                b[i] = Math.max(0, Math.min(255, b[i]));
            }
        } else {
            colorCurveProvider.getColorCurve(strength).fillColors(r, g, b);
        }
        int c, imageSize = source.width * source.height;
        for (int i = 0; i < imageSize; i++) {
            c = source.colors[i];
            destination.colors[i] = Color.argb(255, r[Color.red(c)], g[Color.green(c)], b[Color.blue(c)]);
        }
    }

    @Override
    public TunableType getType() {
        return TunableType.ColorCurve;
    }
}
