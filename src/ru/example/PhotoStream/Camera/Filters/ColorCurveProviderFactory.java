package ru.example.PhotoStream.Camera.Filters;

public class ColorCurveProviderFactory {
    private static final int BRIGHTNESS_MAX_SHIFT = 255;
    private static final int CONTRAST_MIDDLE_POINT_X = 96;
    private static final int CONTRAST_MIDDLE_POINT_Y = 64;
    private static final int DARK_REGIONS_MAX_Y = 64;
    private static final int DARK_REGIONS_MIN_X = 64;
    private static final int LIGHT_REGIONS_MAX_X = 64;
    private static final int LIGHT_REGIONS_MIN_Y = 64;

    public static ColorCurveProvider brightnessProvider() {
        return new ColorCurveProvider() {
            @Override
            public ColorCurve getColorCurve(double strength) {
                final int shift = (int)(strength * BRIGHTNESS_MAX_SHIFT);
                return new ColorCurve() {
                    @Override
                    public void fillColors(int[] red, int[] green, int[] blue) {
                        for (int i = 0; i < 256; i++) {
                            red[i] = Math.max(0, Math.min(255, i + shift));
                            blue[i] = red[i];
                            green[i] = red[i];
                        }
                    }
                };
            }
        };
    }

    public static ColorCurveProvider contrastProvider() {
        return new ColorCurveProvider() {
            @Override
            public ColorCurve getColorCurve(double strength) {
                int[] x = new int[4], y = new int[4];
                x[0] = 0;
                y[0] = 0;
                x[3] = 256;
                y[3] = 256;
                if (strength > 0) {
                    x[1] = (int)(strength * CONTRAST_MIDDLE_POINT_X);
                    y[1] = (int)(strength * CONTRAST_MIDDLE_POINT_Y);
                } else {
                    x[1] = (int)(-strength * CONTRAST_MIDDLE_POINT_Y);
                    y[1] = (int)(-strength * CONTRAST_MIDDLE_POINT_X);
                }
                x[2] = 256 - x[1];
                y[2] = 256 - y[1];
                return ColorCurveFactory.createFromPoints(x, y);
            }
        };
    }

    public static ColorCurveProvider darkRegionsProvider() {
        return new ColorCurveProvider() {
            @Override
            public ColorCurve getColorCurve(double strength) {
                int[] x = new int[4], y = new int[4];
                x[0] = 0;
                y[0] = 0;
                x[2] = 128;
                y[2] = 128;
                x[3] = 256;
                y[3] = 256;
                if (strength > 0) {
                    x[1] = 0;
                    y[1] = (int) (strength * DARK_REGIONS_MAX_Y);
                } else {
                    x[1] = (int) (-strength * DARK_REGIONS_MIN_X);
                    y[1] = 0;
                }
                return ColorCurveFactory.createFromPoints(x, y);
            }
        };
    }

    public static ColorCurveProvider lightRegionsProvider() {
        return new ColorCurveProvider() {
            @Override
            public ColorCurve getColorCurve(double strength) {
                int[] x = new int[4], y = new int[4];
                x[0] = 0;
                y[0] = 0;
                x[1] = 128;
                y[1] = 128;
                x[3] = 256;
                y[3] = 256;
                if (strength > 0) {
                    x[2] = 256 - (int)(strength * LIGHT_REGIONS_MAX_X);
                    y[2] = 256;
                } else {
                    x[2] = 256;
                    y[2] = 256 - (int)(-strength * LIGHT_REGIONS_MIN_Y);
                }
                return ColorCurveFactory.createFromPoints(x, y);
            }
        };
    }

    public static ColorCurveProvider exposureProvider() {
        return new ColorCurveProvider() {
            @Override
            public ColorCurve getColorCurve(double strength) {
                final double k = strength + 1;
                return new ColorCurve() {
                    @Override
                    public void fillColors(int[] red, int[] green, int[] blue) {
                        for (int i = 0; i < 256; i++) {
                            red[i] = (int)(k * i);
                            red[i] = Math.max(0, Math.min(255, red[i]));
                            green[i] = red[i];
                            blue[i] = red[i];
                        }
                    }
                };
            }
        };
    }
}
