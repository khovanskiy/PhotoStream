package ru.example.PhotoStream.Camera.Filters;

/**
 * Created by Genyaz on 25.05.2014.
 */
public class TealAndOrange extends ColorCurveFilter {
    @Override
    protected int redCurve(int redSource) {
        if (redSource < 128) {
            return (3 * redSource * redSource) / 512 + redSource / 4;
        } else {
            return (-redSource * redSource / 168) + 551 * redSource / 168 - 194;
        }
    }

    @Override
    protected int greenCurve(int greenSource) {
        return greenSource;
    }

    @Override
    protected int blueCurve(int blueSource) {
        if (blueSource < 128) {
            return 7 * blueSource / 4 - 3 * blueSource * blueSource / 512;
        } else {
            return blueSource * blueSource / 168 - 215 * blueSource / 168 + 194;
        }
    }
}
