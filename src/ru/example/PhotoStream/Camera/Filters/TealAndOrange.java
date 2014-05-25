package ru.example.PhotoStream.Camera.Filters;

/**
 * Created by Genyaz on 25.05.2014.
 */
public class TealAndOrange extends ColorCurveFilter {
    @Override
    protected void fillColors(int[] red, int[] green, int[] blue) {
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
}
