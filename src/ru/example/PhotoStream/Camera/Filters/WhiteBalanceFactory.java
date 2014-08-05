package ru.example.PhotoStream.Camera.Filters;

import android.content.Context;
import android.graphics.Color;
import ru.example.PhotoStream.Camera.Algorithms;
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
        WhitePatch {
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
        },
        IncandescentLamp {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.IncandescentLamp);
            }
        },
        Sunrise {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Sunrise);
            }
        },
        FluorescentLampWhite {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.FluorescentLampWhite);
            }
        },
        Midday {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Midday);
            }
        },
        FluorescentLampDay {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.FluorescentLampDay);
            }
        },
        Cloudy {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Cloudy);
            }
        },
        ClearDay {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.ClearDay);
            }
        },
        Twilight {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Twilight);
            }
        },
        PolarSky {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.PolarSky);
            }
        };

        public abstract String toString(Context context);
    }

    private static final double WHITE_PATCH_PERCENTAGE = 0.05;
    private static final double GIMP_PERCENTAGE = 0.0005;

    public static TunablePhotoFilter byName(Context context, String name, RawBitmap rawBitmap) {
        if (name.equals(context.getString(R.string.GreyWorld))) {
            return greyWorld(rawBitmap);
        } else if (name.equals(context.getString(R.string.WhitePatch))) {
            return whitePatch(rawBitmap);
        } else if (name.equals(context.getString(R.string.GIMP))) {
            return gimp(rawBitmap);
        } else if (name.equals(context.getString(R.string.IncandescentLamp))) {
            return kelvin(2800);
        } else if (name.equals(context.getString(R.string.Sunrise))) {
            return kelvin(3400);
        } else if (name.equals(context.getString(R.string.FluorescentLampWhite))) {
            return kelvin(4000);
        } else if (name.equals(context.getString(R.string.Midday))) {
            return kelvin(5000);
        } else if (name.equals(context.getString(R.string.FluorescentLampDay))) {
            return kelvin(6000);
        } else if (name.equals(context.getString(R.string.Cloudy))) {
            return kelvin(7000);
        } else if (name.equals(context.getString(R.string.ClearDay))) {
            return kelvin(7500);
        } else if (name.equals(context.getString(R.string.Twilight))) {
            return kelvin(8000);
        } else if (name.equals(context.getString(R.string.PolarSky))) {
            return kelvin(15000);
        }
        else {
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
        return scaleByRGB(rAvg, gAvg, bAvg);
    }

    public static TunablePhotoFilter whitePatch(RawBitmap rawBitmap) {
        int imageSize = rawBitmap.width * rawBitmap.height;
        float[] hsv = new float[3];
        float[] values = new float[imageSize];
        for (int i = 0; i < imageSize; i++) {
            Color.colorToHSV(rawBitmap.colors[i], hsv);
            values[i] = hsv[2];
        }
        int pivotIndex = (int)(imageSize * WHITE_PATCH_PERCENTAGE);
        Algorithms.orderStatistics(values, pivotIndex);
        float threshold = values[pivotIndex - 1];
        int color;
        long red = 0, green = 0, blue = 0;
        for (int i = 0; i < imageSize; i++) {
            color = rawBitmap.colors[i];
            Color.colorToHSV(color, hsv);
            if (hsv[2] >= threshold) {
                red += Color.red(color);
                green += Color.green(color);
                blue += Color.blue(color);
            }
        }
        double rAvg = red * 1.0 / (imageSize * WHITE_PATCH_PERCENTAGE);
        double gAvg = green * 1.0 / (imageSize * WHITE_PATCH_PERCENTAGE);
        double bAvg = blue * 1.0 / (imageSize * WHITE_PATCH_PERCENTAGE);
        return scaleByRGB(rAvg, gAvg, bAvg);
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

    private static TunablePhotoFilter scaleByRGB(final double r, final double g, final double b) {
        return new ColorCurveFilter(new ColorCurve() {
            @Override
            public void fillColors(int[] red, int[] green, int[] blue) {
                double max = Math.max(r, Math.max(g, b));
                double rScale = max / r, gScale = max / g, bScale = max / b;
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

    public static TunablePhotoFilter kelvin(int temperature) {
        temperature /= 100;
        double red, green, blue;
        if (temperature <= 66) {
            red = 255;
            green = 99.4708025861 * Math.log(temperature) - 161.1195681661;
            green = Math.max(0, Math.min(255, green));
            if (temperature <= 19) {
                blue = 0;
            } else {
                blue = 138.5177312231 * Math.log(temperature - 10) - 305.0447927307;
                blue = Math.max(0, Math.min(255, blue));
            }
        } else {
            red = 329.698727446 * (Math.pow((temperature - 60), -0.1332047592));
            red = Math.max(0, Math.min(255, red));
            green = 288.1221695283 * (Math.pow(temperature - 60, -0.0755148492));
            green = Math.max(0, Math.min(255, green));
            blue = 255;
        }
        return scaleByRGB(red, green, blue);
    }
}
