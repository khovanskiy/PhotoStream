package ru.example.PhotoStream.Camera.Filters;

/**
 * Created by Genyaz on 26.05.2014.
 */
public class ColorReductionFilter extends ColorCurveFilter {
    @Override
    protected void fillColors(int[] red, int[] green, int[] blue) {
        for (int i = 0; i < 256; i++) {
            red[i] = (i / 16) * 16;
            green[i] = (i / 16) * 16;
            blue[i] = (i / 16) * 16;
        }
    }
}
