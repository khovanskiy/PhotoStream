package ru.example.PhotoStream.Camera.Filters;


/**
 * Created by Genyaz on 18.05.2014.
 */
public class Filters {

    public static enum FilterType {
        NoFilter,
        BlackAndWhite,
        Negative,
        ThresholdBlur,
        Emboss,
        Blur,
        Glow,
        Sharpen,
        EdgesNegative,
        EdgesPositive,
    }

    public static PhotoFilter byName(String name) {
        switch (name) {
            case "BlackAndWhite":
                return BlackAndWhite();
            case "Negative":
                return Negative();
            case "ThresholdBlur":
                return ThresholdBlur();
            case "Emboss":
                return Emboss();
            case "Blur":
                return Blur();
            case "Glow":
                return Glow();
            case "Sharpen":
                return Sharpen();
            case "EdgesNegative":
                return EdgesNegative();
            case "EdgesPositive":
                return EdgesPositive();
            default:
                return NoFilter();
        }
    }

    public static PhotoFilter NoFilter() {
        return new IdentityFilter();
    }

    public static PhotoFilter ThresholdBlur() {
        return new Convolution3ThresholdFilter(new float[][]{{1f/16, 1f/8, 1f/16}, {1f/8, 1f/4, 1f/8}, {1f/16, 1f/8, 1f/16}}, 0, 20);
    }

    public static PhotoFilter Emboss() {
        return new Convolution3Filter(new float[][]{{-2, -1, 0}, {-1, 1, 1}, {0, 1, 2}}, 0);
    }

    public static PhotoFilter Blur() {
        return new Convolution3Filter(new float[][]{{1f/16, 1f/8, 1f/16}, {1f/8, 1f/4, 1f/8}, {1f/16, 1f/8, 1f/16}}, 0);
    }

    public static PhotoFilter BlackAndWhite() {
        return new BlackAndWhiteFilter();
    }

    public static PhotoFilter Glow() {
        return new Convolution3Filter(new float[][]{{1f/16, 1f/8, 1f/16}, {1f/8, 5f/4, 1f/8}, {1f/16, 1f/8, 1f/16}}, 0);
    }

    public static PhotoFilter Sharpen() {
        return new Convolution3Filter(new float[][]{{-1, -1, -1}, {-1, 9, -1}, {-1, -1, -1}}, 0);
    }

    public static PhotoFilter EdgesNegative() {
        return new Convolution3Filter(new float[][]{{-1, -1, -1}, {-1, 8, -1}, {-1, -1, -1}}, 0);
    }

    public static PhotoFilter EdgesPositive() {
        return new Convolution3Filter(new float[][]{{1, 1, 1}, {1, -7, 1}, {1, 1, 1}}, 0);
    }

    public static PhotoFilter Negative() {
        return new NegativeFilter();
    }
}
