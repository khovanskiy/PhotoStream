package ru.example.PhotoStream.Camera.Filters;

import android.content.Context;
import android.graphics.Color;
import ru.example.PhotoStream.Camera.RawBitmap;
import ru.example.PhotoStream.R;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by Genyaz on 25.07.2014.
 */
public class WhiteBalanceFactory {

    public static enum WhiteBalanceType {
        NoWhiteBalance {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.NoWhiteBalance);
            }
        },
        GreyWorld {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.GreyWorld);
            }
        },
        /*WhitePatch {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.WhitePatch);
            }
        },/**/
        GIMP {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.GIMP);
            }
        };

        public abstract String toString(Context context);
    }

    private static final double WHITE_PATCH_PERCENTAGE = 0.1;
    private static final double GIMP_PERCENTAGE = 0.0005;

    public static TunablePhotoFilter byName(Context context, String name, RawBitmap rawBitmap) {
        if (name.equals(context.getString(R.string.GreyWorld))) {
            return greyWorld(rawBitmap);
        } else if (name.equals(context.getString(R.string.WhitePatch))) {
            return whitePatch(rawBitmap);
        } else if (name.equals(context.getString(R.string.GIMP))) {
            return gimp(rawBitmap);
        } else {
            return new IdentityFilter();
        }
    }

    public static TunablePhotoFilter greyWorld(RawBitmap rawBitmap) {
        int imageSize = rawBitmap.width * rawBitmap.height;
        int color;
        long red = 0, green = 0, blue = 0;
        for (int i = 0; i < imageSize; i++) {
            color = rawBitmap.colors[i];
            red += Color.red(color);
            green += Color.green(color);
            blue += Color.blue(color);
        }
        double rAvg = red * 1.0 / imageSize;
        double gAvg = green * 1.0 / imageSize;
        double bAvg = blue * 1.0 / imageSize;
        double max = Math.max(rAvg, Math.max(gAvg, bAvg));
        final double rScale = max / rAvg, gScale = max / gAvg, bScale = max / bAvg;
        return new ColorCurveFilter(new ColorCurve() {
            @Override
            public void fillColors(int[] red, int[] green, int[] blue) {
                for (int i = 0; i < 256; i++) {
                    red[i] = (int)(rScale * i);
                    red[i] = Math.max(0, Math.min(255, red[i]));
                    green[i] = (int)(gScale * i);
                    green[i] = Math.max(0, Math.min(255, green[i]));
                    blue[i] = (int)(bScale * i);
                    blue[i] = Math.max(0, Math.min(255, blue[i]));
                }
            }
        });
    }

    public static TunablePhotoFilter whitePatch(RawBitmap rawBitmap) {
        int imageSize = rawBitmap.width * rawBitmap.height;
        Integer[] colors = new Integer[imageSize];
        for (int i = 0; i < imageSize; i++) {
            colors[i] = rawBitmap.colors[i];
        }
        Arrays.sort(colors, new Comparator<Integer>() {
            private float[] hsv = new float[3];
            private float v1, v2;
            @Override
            public int compare(Integer lhs, Integer rhs) {
                Color.colorToHSV(lhs, hsv);
                v1 = hsv[2];
                Color.colorToHSV(rhs, hsv);
                v2 = hsv[2];
                if (v1 < v2) {
                    return 1;
                } else if (v1 == v2) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });
        int color;
        long red = 0, green = 0, blue = 0;
        for (int i = 0; i < imageSize * WHITE_PATCH_PERCENTAGE; i++) {
            color = rawBitmap.colors[i];
            red += Color.red(color);
            green += Color.green(color);
            blue += Color.blue(color);
        }
        double rAvg = red * 1.0 / (imageSize * WHITE_PATCH_PERCENTAGE);
        double gAvg = green * 1.0 / (imageSize * WHITE_PATCH_PERCENTAGE);
        double bAvg = blue * 1.0 / (imageSize * WHITE_PATCH_PERCENTAGE);
        double max = Math.max(rAvg, Math.max(gAvg, bAvg));
        final double rScale = max / rAvg, gScale = max / gAvg, bScale = max / bAvg;
        return new ColorCurveFilter(new ColorCurve() {
            @Override
            public void fillColors(int[] red, int[] green, int[] blue) {
                for (int i = 0; i < 256; i++) {
                    red[i] = (int)(rScale * i);
                    red[i] = Math.max(0, Math.min(255, red[i]));
                    green[i] = (int)(gScale * i);
                    green[i] = Math.max(0, Math.min(255, green[i]));
                    blue[i] = (int)(bScale * i);
                    blue[i] = Math.max(0, Math.min(255, blue[i]));
                }
            }
        });
    }

    public static TunablePhotoFilter gimp(RawBitmap rawBitmap) {
        int imageSize = rawBitmap.width * rawBitmap.height;
        int c;
        int[] rCount = new int[256], gCount = new int[256], bCount = new int[256];
        for (int i = 0; i < 256; i++) {
            rCount[i] = 0;
            gCount[i] = 0;
            bCount[i] = 0;
        }
        for (int i = 0; i < imageSize; i++) {
            c = rawBitmap.colors[i];
            rCount[Color.red(c)]++;
            gCount[Color.green(c)]++;
            bCount[Color.blue(c)]++;
        }
        int rMin = 0, rMax = 255, gMin = 0, gMax = 255, bMin = 0, bMax = 255;
        while (rCount[rMin] < GIMP_PERCENTAGE * imageSize) rMin++;
        while (rCount[rMax] < GIMP_PERCENTAGE * imageSize) rMax--;
        while (gCount[gMin] < GIMP_PERCENTAGE * imageSize) gMin++;
        while (gCount[gMax] < GIMP_PERCENTAGE * imageSize) gMax--;
        while (bCount[bMin] < GIMP_PERCENTAGE * imageSize) bMin++;
        while (bCount[bMax] < GIMP_PERCENTAGE * imageSize) bMax--;
        final int finalRMin = rMin;
        final int finalRMax = rMax;
        final int finalGMin = gMin;
        final int finalGMax = gMax;
        final int finalBMin = bMin;
        final int finalBMax = bMax;
        return new ColorCurveFilter(new ColorCurve() {
            @Override
            public void fillColors(int[] red, int[] green, int[] blue) {
                for (int i = 0; i < 256; i++) {
                    red[i] = 256 * (i - finalRMin) / (finalRMax - finalRMin);
                    red[i] = Math.max(0, Math.min(255, red[i]));
                    green[i] = 256 * (i - finalGMin) / (finalGMax - finalGMin);
                    green[i] = Math.max(0, Math.min(255, green[i]));
                    blue[i] = 256 * (i - finalBMin) / (finalBMax - finalBMin);
                    blue[i] = Math.max(0, Math.min(255, blue[i]));
                }
            }
        });
    }
}
