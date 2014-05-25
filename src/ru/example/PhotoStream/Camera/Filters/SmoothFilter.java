package ru.example.PhotoStream.Camera.Filters;

/**
 * Created by Genyaz on 25.05.2014.
 */
public class SmoothFilter extends ColorCurveFilter {
    @Override
    protected void fillColors(int[] red, int[] green, int[] blue) {
        int c;
        for (int i = 0; i < 256; i++) {
            c = 7379 * i / 4064 - 13 * i * i / 4064;
            red[i] = c;
            green[i] = c;
            blue[i] = c;
        }
    }
}
