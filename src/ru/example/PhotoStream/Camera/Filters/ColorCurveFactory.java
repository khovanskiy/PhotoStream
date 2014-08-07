package ru.example.PhotoStream.Camera.Filters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

public class ColorCurveFactory {
    public static ColorCurve createFromImage(Context context, int resID) {
        final int[] r = new int[256], g = new int[256], b = new int[256];
        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), resID);
        int[] colors = new int[256];
        bm.getPixels(colors, 0, 256, 0, 0, 256, 1);
        for (int i = 0; i < 256; i++) {
            r[i] = Color.red(colors[i]);
            g[i] = Color.green(colors[i]);
            b[i] = Color.blue(colors[i]);
        }
        return new ColorCurve() {
            @Override
            public void fillColors(int[] red, int[] green, int[] blue) {
                for (int i = 0; i < 256; i++) {
                    red[i] = r[i];
                    green[i] = g[i];
                    blue[i] = b[i];
                }
            }
        };
    }

    public static ColorCurve createColorReduction() {
        return new ColorCurve() {
            @Override
            public void fillColors(int[] red, int[] green, int[] blue) {
                for (int i = 0; i < 256; i++) {
                    red[i] = (i / 16) * 16;
                    green[i] = (i / 16) * 16;
                    blue[i] = (i / 16) * 16;
                }
            }
        };
    }

    public static ColorCurve createEightBit() {
        return new ColorCurve() {
            @Override
            public void fillColors(int[] red, int[] green, int[] blue) {
                for (int i = 0; i < 256; i++) {
                    red[i] = ((i + 25) / 51) * 51;
                    green[i] = red[i];
                    blue[i] = red[i];
                }
            }
        };
    }

    public static ColorCurve createSmooth() {
        return new ColorCurve() {
            @Override
            public void fillColors(int[] red, int[] green, int[] blue) {
                int c;
                for (int i = 0; i < 256; i++) {
                    c = 7379 * i / 4064 - 13 * i * i / 4064;
                    red[i] = c;
                    green[i] = c;
                    blue[i] = c;
                }
            }
        };
    }

    public static ColorCurve createTealAndOrange() {
        return new ColorCurve() {
            @Override
            public void fillColors(int[] red, int[] green, int[] blue) {
                for (int i = 0; i < 256; i++) {
                    if (i < 128) {
                        red[i] = (3 * i * i) / 512 + i / 4;
                    } else {
                        red[i] = (-i * i / 168) + 551 * i / 168 - 194;
                    }
                    green[i] = i;
                    if (i < 128) {
                        blue[i] = 7 * i / 4 - 3 * i * i / 512;
                    } else {
                        blue[i] = i * i / 168 - 215 * i / 168 + 194;
                    }
                }
            }
        };
    }

    public static ColorCurve createFromPoints(int[] x, int[] y) {
        final int[] c = new int[256];
        for (int p = 0; p < x.length - 1; p++) {
            for (int i = x[p]; i < x[p + 1]; i++) {
                c[i] = y[p] + (i - x[p]) * (y[p + 1] - y[p]) / (x[p + 1] - x[p]);
                c[i] = Math.max(0, Math.min(255, c[i]));
            }
        }
        return new ColorCurve() {
            @Override
            public void fillColors(int[] red, int[] green, int[] blue) {
                for (int i = 0; i < 256; i++) {
                    red[i] = c[i];
                    green[i] = c[i];
                    blue[i] = c[i];
                }
            }
        };
    }

}
