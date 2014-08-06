package ru.example.PhotoStream.Camera.Filters;

import android.graphics.Color;
import ru.example.PhotoStream.Camera.RawBitmap;

public class Convolution3Filter extends TunablePhotoFilter {
    private float[][] matrix;
    private int offset;

    /**
     * Creates new convolution filter with 3x3 convolution matrix and offset.
     *
     * @param matrix convolution matrix
     * @param offset offset
     */
    public Convolution3Filter(float[][] matrix, int offset) {
        this.matrix = matrix;
        this.offset = offset;
        setStrength(0);
    }

    @Override
    public void transformOpaqueRaw(RawBitmap source, RawBitmap destination, double strength) {
        float[][] matrix = new float[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                matrix[i][j] = (float) (this.matrix[i][j] * strength);
            }
        }
        matrix[1][1] = (float)((this.matrix[1][1] - 1) * strength + 1);
        int h = source.height, w = source.width;
        int[] last = new int[w], cur = new int[w];
        int c1, c2, c3;
        float r, g, b;
        for (int j = 0; j < w; j++) {
            r = offset;
            g = offset;
            b = offset;
            c1 = source.colors[Math.max(0, j - 1)];
            c2 = source.colors[j];
            c3 = source.colors[Math.min(j + 1, w - 1)];
            r += matrix[0][0] * Color.red(c1) + matrix[0][1] * Color.red(c2) + matrix[0][2] * Color.red(c3);
            g += matrix[0][0] * Color.green(c1) + matrix[0][1] * Color.green(c2) + matrix[0][2] * Color.green(c3);
            b += matrix[0][0] * Color.blue(c1) + matrix[0][1] * Color.blue(c2) + matrix[0][2] * Color.blue(c3);
            r += matrix[1][0] * Color.red(c1) + matrix[1][1] * Color.red(c2) + matrix[1][2] * Color.red(c3);
            g += matrix[1][0] * Color.green(c1) + matrix[1][1] * Color.green(c2) + matrix[1][2] * Color.green(c3);
            b += matrix[1][0] * Color.blue(c1) + matrix[1][1] * Color.blue(c2) + matrix[1][2] * Color.blue(c3);
            c1 = source.colors[w + Math.max(0, j - 1)];
            c2 = source.colors[w + j];
            c3 = source.colors[w + Math.min(j + 1, w - 1)];
            r += matrix[2][0] * Color.red(c1) + matrix[2][1] * Color.red(c2) + matrix[2][2] * Color.red(c3);
            g += matrix[2][0] * Color.green(c1) + matrix[2][1] * Color.green(c2) + matrix[2][2] * Color.green(c3);
            b += matrix[2][0] * Color.blue(c1) + matrix[2][1] * Color.blue(c2) + matrix[2][2] * Color.blue(c3);
            last[j] = Color.argb(255, Math.max(0, Math.min((int) r, 255)), Math.max(0, Math.min((int) g, 255)), Math.max(0, Math.min((int) b, 255)));
        }
        for (int i = 1; i < h - 1; i++) {
            for (int j = 0; j < w; j++) {
                r = offset;
                g = offset;
                b = offset;
                c1 = source.colors[(i - 1) * w + Math.max(0, j - 1)];
                c2 = source.colors[(i - 1) * w + j];
                c3 = source.colors[(i - 1) * w + Math.min(j + 1, w - 1)];
                r += matrix[0][0] * Color.red(c1) + matrix[0][1] * Color.red(c2) + matrix[0][2] * Color.red(c3);
                g += matrix[0][0] * Color.green(c1) + matrix[0][1] * Color.green(c2) + matrix[0][2] * Color.green(c3);
                b += matrix[0][0] * Color.blue(c1) + matrix[0][1] * Color.blue(c2) + matrix[0][2] * Color.blue(c3);
                c1 = source.colors[i * w + Math.max(0, j - 1)];
                c2 = source.colors[i * w + j];
                c3 = source.colors[i * w + Math.min(j + 1, w - 1)];
                r += matrix[1][0] * Color.red(c1) + matrix[1][1] * Color.red(c2) + matrix[1][2] * Color.red(c3);
                g += matrix[1][0] * Color.green(c1) + matrix[1][1] * Color.green(c2) + matrix[1][2] * Color.green(c3);
                b += matrix[1][0] * Color.blue(c1) + matrix[1][1] * Color.blue(c2) + matrix[1][2] * Color.blue(c3);
                c1 = source.colors[(i + 1) * w + Math.max(0, j - 1)];
                c2 = source.colors[(i + 1) * w + j];
                c3 = source.colors[(i + 1) * w + Math.min(j + 1, w - 1)];
                r += matrix[2][0] * Color.red(c1) + matrix[2][1] * Color.red(c2) + matrix[2][2] * Color.red(c3);
                g += matrix[2][0] * Color.green(c1) + matrix[2][1] * Color.green(c2) + matrix[2][2] * Color.green(c3);
                b += matrix[2][0] * Color.blue(c1) + matrix[2][1] * Color.blue(c2) + matrix[2][2] * Color.blue(c3);
                cur[j] = Color.argb(255, Math.max(0, Math.min((int) r, 255)), Math.max(0, Math.min((int) g, 255)), Math.max(0, Math.min((int) b, 255)));
            }
            System.arraycopy(last, 0, destination.colors, (i - 1) * w, w);
            int[] tmp = cur;
            cur = last;
            last = tmp;
        }
        for (int j = 0; j < w; j++) {
            r = offset;
            g = offset;
            b = offset;
            c1 = source.colors[(h - 2) * w + Math.max(0, j - 1)];
            c2 = source.colors[(h - 2) * w + j];
            c3 = source.colors[(h - 2) * w + Math.min(j + 1, w - 1)];
            r += matrix[0][0] * Color.red(c1) + matrix[0][1] * Color.red(c2) + matrix[0][2] * Color.red(c3);
            g += matrix[0][0] * Color.green(c1) + matrix[0][1] * Color.green(c2) + matrix[0][2] * Color.green(c3);
            b += matrix[0][0] * Color.blue(c1) + matrix[0][1] * Color.blue(c2) + matrix[0][2] * Color.blue(c3);
            c1 = source.colors[(h - 1) * w + Math.max(0, j - 1)];
            c2 = source.colors[(h - 1) * w + j];
            c3 = source.colors[(h - 1) * w + Math.min(j + 1, w - 1)];
            r += matrix[1][0] * Color.red(c1) + matrix[1][1] * Color.red(c2) + matrix[1][2] * Color.red(c3);
            g += matrix[1][0] * Color.green(c1) + matrix[1][1] * Color.green(c2) + matrix[1][2] * Color.green(c3);
            b += matrix[1][0] * Color.blue(c1) + matrix[1][1] * Color.blue(c2) + matrix[1][2] * Color.blue(c3);
            r += matrix[2][0] * Color.red(c1) + matrix[2][1] * Color.red(c2) + matrix[2][2] * Color.red(c3);
            g += matrix[2][0] * Color.green(c1) + matrix[2][1] * Color.green(c2) + matrix[2][2] * Color.green(c3);
            b += matrix[2][0] * Color.blue(c1) + matrix[2][1] * Color.blue(c2) + matrix[2][2] * Color.blue(c3);
            cur[j] = Color.argb(255, Math.max(0, Math.min((int) r, 255)), Math.max(0, Math.min((int) g, 255)), Math.max(0, Math.min((int) b, 255)));
        }
        for (int j = 0; j < w; j++) {
            destination.colors[(h - 2) * w + j] = last[j];
            destination.colors[(h - 1) * w + j] = cur[j];
        }
    }

    @Override
    public TunableType getType() {
        return TunableType.Convolution;
    }
}
