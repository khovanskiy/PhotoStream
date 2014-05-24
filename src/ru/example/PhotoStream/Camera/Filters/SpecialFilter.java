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
    private ColorMatrixColorFilter[] matrix;
    private int currentFrame = 0;

    public SpecialFilter() {
        matrix = new ColorMatrixColorFilter[MATRIX_SIZE];
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
            matrix[i] = new ColorMatrixColorFilter(new ColorMatrix(new float[] {
                    Color.red(newRed) * 1f / 255, Color.red(newGreen) * 1f / 255, Color.red(newBlue) * 1f / 255, 0, 0,
                    Color.green(newRed) * 1f / 255, Color.green(newGreen) * 1f / 255, Color.green(newBlue) * 1f / 255, 0, 0,
                    Color.blue(newRed) * 1f / 255, Color.blue(newGreen) * 1f / 255, Color.blue(newBlue) * 1f / 255, 0, 0,
                    0, 0, 0, 1, 0,
            }));
        }
    }

    @Override
    public void transformOpaqueRaw(RawBitmap bitmap) {


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
        view.setColorFilter(matrix[currentFrame]);
        currentFrame = (currentFrame + 1) % MATRIX_SIZE;
    }
}
