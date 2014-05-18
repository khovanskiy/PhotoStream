package ru.example.PhotoStream.Camera.Filters;

import ru.example.PhotoStream.Camera.RawBitmap;

/**
 * Created by Genyaz on 17.05.2014.
 */
public class ConvolutionFilter implements PhotoFilter {
    private float[][] tmp = null;
    private float[][] convolutionMatrix;
    private int matrixSemiWidth, matrixSemiHeight, offset;

    public ConvolutionFilter(float[][] convolutionMatrix, int offset) {
        this.convolutionMatrix = convolutionMatrix;
        this.matrixSemiHeight = convolutionMatrix.length / 2;
        this.matrixSemiWidth = convolutionMatrix[0].length / 2;
        this.offset = offset;
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
            for (int i = matrixSemiHeight; i < bitmap.height - matrixSemiHeight; i++) {
                for (int j = matrixSemiWidth; j < bitmap.width - matrixSemiWidth; j++) {
                    tmp[i][j] = offset;
                    for (int ii = -matrixSemiHeight; ii <= matrixSemiHeight; ii++) {
                        for (int jj = -matrixSemiWidth; jj <= matrixSemiWidth; jj++) {
                            tmp[i][j] += convolutionMatrix[ii + matrixSemiHeight][jj + matrixSemiWidth] * color[i][j];
                        }
                    }
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
