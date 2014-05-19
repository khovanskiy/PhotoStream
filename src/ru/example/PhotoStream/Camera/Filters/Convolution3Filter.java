package ru.example.PhotoStream.Camera.Filters;

import ru.example.PhotoStream.Camera.RawBitmap;

/**
 * Created by Genyaz on 18.05.2014.
 */
public class Convolution3Filter implements PhotoFilter {
    private float[][] matrix;
    private int offset;

    public Convolution3Filter(float[][] matrix, int offset) {
        this.matrix = matrix;
        this.offset = offset;
    }

    @Override
    public synchronized void transform(RawBitmap bitmap) {

    }

    @Override
    public synchronized void transformOpaque(RawBitmap bitmap) {
        float[][] tmp = new float[bitmap.height][bitmap.width];
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
            for (int i = 1; i < bitmap.height - 1; i++) {
                for (int j = 1; j < bitmap.width - 1; j++) {
                    tmp[i][j] = offset;
                    tmp[i][j] += matrix[0][0] * color[i - 1][j - 1] + matrix[0][1] * color[i - 1][j] + matrix[0][2] * color[i - 1][j + 1];
                    tmp[i][j] += matrix[1][0] * color[i][j - 1] + matrix[1][1] * color[i][j] + matrix[1][2] * color[i][j + 1];
                    tmp[i][j] += matrix[2][0] * color[i + 1][j - 1] + matrix[2][1] * color[i + 1][j] + matrix[2][2] * color[i + 1][j + 1];
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
