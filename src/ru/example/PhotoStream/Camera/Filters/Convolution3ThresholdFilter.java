package ru.example.PhotoStream.Camera.Filters;

import ru.example.PhotoStream.Camera.RawBitmap;

/**
 * Created by Genyaz on 18.05.2014.
 */
public class Convolution3ThresholdFilter implements PhotoFilter {
    private float[][] tmp = null;
    private float[][] matrix;
    private int offset;
    private int threshold;

    public Convolution3ThresholdFilter(float[][] matrix, int offset, int threshold) {
        this.matrix = matrix;
        this.offset = offset;
        this.threshold = threshold;
    }

    @Override
    public synchronized void transform(RawBitmap bitmap) {

    }

    @Override
    public synchronized void transformOpaque(RawBitmap bitmap) {
        if (tmp == null || tmp.length != bitmap.height || tmp[0].length != bitmap.width) {
            tmp = new float[bitmap.height][bitmap.width];
        }
        int[][] color;
        for (int c = 0; c < 3; c++) {
            switch (c) {
                case 0:
                    color = bitmap.r;
                    break;
                case 1:
                    color = bitmap.g;
                    break;
                default:
                    color = bitmap.b;
                    break;
            }
            float amount;
            for (int i = 1; i < bitmap.height - 1; i++) {
                for (int j = 1; j < bitmap.width - 1; j++) {
                    tmp[i][j] = color[i][j] * matrix[1][1];
                    amount = matrix[1][1];
                    if (Math.abs(color[i - 1][j - 1] - color[i][j]) < threshold) {
                        tmp[i][j] += color[i - 1][j - 1] * matrix[0][0];
                        amount += matrix[0][0];
                    }
                    if (Math.abs(color[i - 1][j] - color[i][j]) < threshold) {
                        tmp[i][j] += color[i - 1][j] * matrix[0][1];
                        amount += matrix[0][1];
                    }
                    if (Math.abs(color[i - 1][j + 1] - color[i][j]) < threshold) {
                        tmp[i][j] += color[i - 1][j + 1] * matrix[0][2];
                        amount += matrix[0][2];
                    }
                    if (Math.abs(color[i][j - 1] - color[i][j]) < threshold) {
                        tmp[i][j] += color[i][j - 1] * matrix[1][0];
                        amount += matrix[1][0];
                    }
                    if (Math.abs(color[i][j + 1] - color[i][j]) < threshold) {
                        tmp[i][j] += color[i][j + 1] * matrix[1][2];
                        amount += matrix[1][2];
                    }
                    if (Math.abs(color[i + 1][j - 1] - color[i][j]) < threshold) {
                        tmp[i][j] += color[i + 1][j - 1] * matrix[2][0];
                        amount += matrix[2][0];
                    }
                    if (Math.abs(color[i + 1][j] - color[i][j]) < threshold) {
                        tmp[i][j] += color[i + 1][j] * matrix[2][1];
                        amount += matrix[2][1];
                    }
                    if (Math.abs(color[i + 1][j + 1] - color[i][j]) < threshold) {
                        tmp[i][j] += color[i + 1][j + 1] * matrix[2][2];
                        amount += matrix[2][2];
                    }
                    tmp[i][j] /= amount;
                }
            }
            for (int i = 1; i < bitmap.height - 1; i++) {
                for (int j = 1; j < bitmap.width - 1; j++) {
                    color[i][j] = (int)(tmp[i][j] < 0 ? 0 : tmp[i][j] > 255 ? 255 : tmp[i][j]);
                }
            }
        }
    }
}
