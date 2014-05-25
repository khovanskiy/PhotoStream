package ru.example.PhotoStream.Camera.Filters;

/**
 * Created by Genyaz on 25.05.2014.
 */
public class SmoothFilter extends ColorCurveFilter {
    @Override
    protected int redCurve(int redSource) {
        return 7379 * redSource / 4064 - 13 * redSource * redSource / 4064;
    }

    @Override
    protected int greenCurve(int greenSource) {
        return 7379 * greenSource / 4064 - 13 * greenSource * greenSource / 4064;
    }

    @Override
    protected int blueCurve(int blueSource) {
        return 7379 * blueSource / 4064 - 13 * blueSource * blueSource / 4064;
    }
}
