package ru.example.PhotoStream.Camera.Filters;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.widget.ImageView;
import ru.example.PhotoStream.Camera.RawBitmap;

/**
 * Created by Genyaz on 24.05.2014.
 */
public class SpecialFilter implements PhotoFilter {

    private static final int MATRIX_SIZE = 32;
    private static boolean frozen = false;
    private ColorMatrix[] matrix;
    private int currentFrame = 0;

    public static synchronized void freeze() {
        frozen = true;
    }

    public static synchronized void unfreeze() {
        frozen = false;
    }

    public static synchronized boolean isFrozen() {
        return frozen;
    }

    public SpecialFilter() {
        unfreeze();
        matrix = new ColorMatrix[MATRIX_SIZE];
        float[] hsvRed = new float[3], hsvGreen = new float[3], hsvBlue = new float[3];
        int newRed, newGreen, newBlue;
        for (int i = 0; i < MATRIX_SIZE; i++) {
            Color.colorToHSV(Color.RED, hsvRed);
            hsvRed[0] += (360f * i) / MATRIX_SIZE;
            if (hsvRed[0] > 360) hsvRed[0] -= 360;
            newRed = Color.HSVToColor(hsvRed);
            Color.colorToHSV(Color.GREEN, hsvGreen);
            hsvGreen[0] += (360f * i) / MATRIX_SIZE;
            if (hsvGreen[0] > 360) hsvGreen[0] -= 360;
            newGreen = Color.HSVToColor(hsvGreen);
            Color.colorToHSV(Color.BLUE, hsvBlue);
            hsvBlue[0] += (360f * i) / MATRIX_SIZE;
            if (hsvBlue[0] > 360) hsvBlue[0] -= 360;
            newBlue = Color.HSVToColor(hsvBlue);
            matrix[i] = new ColorMatrix(new float[] {
                    Color.red(newRed) * 1f / 255, Color.red(newGreen) * 1f / 255, Color.red(newBlue) * 1f / 255, 0, 0,
                    Color.green(newRed) * 1f / 255, Color.green(newGreen) * 1f / 255, Color.green(newBlue) * 1f / 255, 0, 0,
                    Color.blue(newRed) * 1f / 255, Color.blue(newGreen) * 1f / 255, Color.blue(newBlue) * 1f / 255, 0, 0,
                    0, 0, 0, 1, 0,
            });
        }
    }

    @Override
    public synchronized void transformOpaqueRaw(RawBitmap bitmap) {
        currentFrame = (currentFrame + MATRIX_SIZE - 2) % MATRIX_SIZE;
        int c, a0, r0, g0, b0;
        float a1, r1, g1, b1;
        float[] m = matrix[currentFrame].getArray();
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
    public synchronized boolean hasPreviewModification() {
        return true;
    }

    @Override
    public synchronized void modifyPreview(ImageView view) {
        view.setColorFilter(new ColorMatrixColorFilter(matrix[currentFrame]));
        if (!isFrozen()) {
            currentFrame = (currentFrame + 1) % MATRIX_SIZE;
        }
    }
}
