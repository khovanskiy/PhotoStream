package ru.example.PhotoStream.Camera.Filters;

import android.content.Context;
import android.graphics.ColorMatrix;
import ru.example.PhotoStream.Console;
import ru.example.PhotoStream.R;

import java.util.Random;

public class TunablePhotoFilterFactory {
    /**
     * Enumerates available {@link ru.example.PhotoStream.Camera.Filters.PhotoFilter}s.
     */
    public static enum FilterType {
        NoFilter {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.NoFilter);
            }
        },
        Negative {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Negative);
            }
        },
        Grayscale {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Grayscale);
            }
        },
        Sepia {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Sepia);
            }
        },
        Polaroid {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Polaroid);
            }
        },
        VintageBlackAndWhite {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.VintageBlackAndWhite);
            }
        },
        Nashville {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Nashville);
            }
        },
        Sierra {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Sierra);
            }
        },
        Valencia {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Valencia);
            }
        },
        Walden {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Walden);
            }
        },
        Hudson {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Hudson);
            }
        },
        Amaro {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Amaro);
            }
        },
        Rise {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Rise);
            }
        },
        Y1977 {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Y1977);
            }
        },
        Kelvin {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Kelvin);
            }
        },
        XPro {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Xpro);
            }
        },
        Toaster {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Toaster);
            }
        },
        TealAndOrange {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.TealAndOrange);
            }
        },
        Smooth {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Smooth);
            }
        },
        ColorReduction {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.ColorReduction);
            }
        },
        Random {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Random);
            }
        };

        public abstract String toString(Context context);
    }

    /**
     * Returns {@link ru.example.PhotoStream.Camera.Filters.PhotoFilter} by its name or identity filter if this filter wasn't found.
     *
     * @param name photo filter name
     * @return photo filter
     */
    public static TunablePhotoFilter getFilterByName(Context context, String name) {
        if (name.equals(context.getString(R.string.Negative))) {
            return Negative();
        } else if (name.equals(context.getString(R.string.Grayscale))) {
            return Grayscale();
        } else if (name.equals(context.getString(R.string.Sepia))) {
            return Sepia();
        } else if (name.equals(context.getString(R.string.Polaroid))) {
            return Polaroid();
        } else if (name.equals(context.getString(R.string.VintageBlackAndWhite))) {
            return VintageBlackAndWhite();
        } else if (name.equals(context.getString(R.string.Hudson))) {
            return Hudson();
        } else if (name.equals(context.getString(R.string.Nashville))) {
            return Nashville();
        } else if (name.equals(context.getString(R.string.Amaro))) {
            return Amaro();
        } else if (name.equals(context.getString(R.string.Sierra))) {
            return Sierra(context);
        } else if (name.equals(context.getString(R.string.Valencia))) {
            return Valencia();
        } else if (name.equals(context.getString(R.string.Walden))) {
            return Walden();
        } else if (name.equals(context.getString(R.string.Rise))) {
            return Rise();
        } else if (name.equals(context.getString(R.string.Y1977))) {
            return Y1977(context);
        } else if (name.equals(context.getString(R.string.Kelvin))) {
            return Kelvin(context);
        } else if (name.equals(context.getString(R.string.Xpro))) {
            return Xpro(context);
        } else if (name.equals(context.getString(R.string.Toaster))) {
            return Toaster(context);
        } else if (name.equals(context.getString(R.string.TealAndOrange))) {
            return TealAndOrange();
        } else if (name.equals(context.getString(R.string.Smooth))) {
            return Smooth();
        } else if (name.equals(context.getString(R.string.ColorReduction))) {
            return ColorReduction();
        } else if (name.equals(context.getString(R.string.Random))) {
            return Random();
        } else {
            return NoFilter();
        }
    }

    public static TunablePhotoFilter Contrast() {
        return new ColorCurveFilter(ColorCurveProviderFactory.contrastProvider());
    }

    public static TunablePhotoFilter Brightness() {
        return new ColorCurveFilter(ColorCurveProviderFactory.brightnessProvider());
    }

    public static TunablePhotoFilter Saturation() {
        return new SaturationFilter();
    }

    public static TunablePhotoFilter LightRegions() {
        return new ColorCurveFilter(ColorCurveProviderFactory.lightRegionsProvider());
    }

    public static TunablePhotoFilter DarkRegions() {
        return new ColorCurveFilter(ColorCurveProviderFactory.darkRegionsProvider());
    }

    public static TunablePhotoFilter ColorReduction() {
        return new ColorCurveFilter(ColorCurveFactory.createColorReduction());
    }

    /**
     * Returns identity filter.
     *
     * @return photo filter
     */
    public static TunablePhotoFilter NoFilter() {
        return new IdentityFilter();
    }

    /**
     * Returns emboss filter.
     *
     * @return photo filter
     */
    public static TunablePhotoFilter Emboss() {
        return new Convolution3Filter(new float[][]{{-2, -1, 0}, {-1, 1, 1}, {0, 1, 2}}, 0);
    }

    /**
     * Returns blur filter.
     *
     * @return photo filter
     */
    public static TunablePhotoFilter Blur() {
        return new Convolution3Filter(new float[][]{{1f / 16, 1f / 8, 1f / 16}, {1f / 8, 1f / 4, 1f / 8}, {1f / 16, 1f / 8, 1f / 16}}, 0);
    }

    /**
     * Returns glow filter.
     *
     * @return photo filter
     */
    public static TunablePhotoFilter Glow() {
        return new Convolution3Filter(new float[][]{{1f / 16, 1f / 8, 1f / 16}, {1f / 8, 5f / 4, 1f / 8}, {1f / 16, 1f / 8, 1f / 16}}, 0);
    }

    /**
     * Returns sharpen filter.
     *
     * @return photo filter
     */
    public static TunablePhotoFilter Sharpness() {
        return new Convolution3Filter(new float[][]{{-1f/16, -1f/8, -1f/16}, {-1f/8, 7f/4, -1f/8}, {-1f/16, -1f/8, -1f/16}}, 0);
    }

    /**
     * Returns edges-negative filter.
     *
     * @return photo filter
     */
    public static TunablePhotoFilter EdgesNegative() {
        return new Convolution3Filter(new float[][]{{-1, -1, -1}, {-1, 8, -1}, {-1, -1, -1}}, 0);
    }

    /**
     * Returns edges-positive filter.
     *
     * @return photo filter
     */
    public static TunablePhotoFilter EdgesPositive() {
        return new Convolution3Filter(new float[][]{{1, 1, 1}, {1, -7, 1}, {1, 1, 1}}, 0);
    }

    /**
     * Negative filter,
     *
     * @return photo filter
     */
    public static TunablePhotoFilter Negative() {
        float[] matrix = new float[] {
                -1f, 0f, 0f, 0, 255,
                0f, -1f, 0f, 0, 255,
                0f, 0f, -1f, 0, 255,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(matrix);
    }

    public static TunablePhotoFilter Sepia() {
        float[] matrix = new float[] {
                0.393f, 0.769f, 0.180f, 0, 0,
                0.349f, 0.686f, 0.168f, 0, 0,
                0.272f, 0.534f, 0.131f, 0, 0,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(matrix);
    }

    public static TunablePhotoFilter Polaroid() {
        float[] matrix = new float[] {
                1.438f, -0.062f, -0.062f, 0, 0,
                -0.122f, 1.378f, -0.122f, 0, 0,
                -0.016f, -0.016f, 1.483f, 0, 0,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(matrix);
    }

    public static TunablePhotoFilter VintageBlackAndWhite() {
        float[] matrix = new float[] {
                0.75f, 0.75f, 0.75f, 0, 0,
                0.75f, 0.75f, 0.75f, 0, 0,
                0.75f, 0.75f, 0.75f, 0, 0,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(matrix);
    }

    public static TunablePhotoFilter Grayscale() {
        float[] matrix = new float[] {
                0.33f, 0.59f, 0.11f, 0, 0,
                0.33f, 0.59f, 0.11f, 0, 0,
                0.33f, 0.59f, 0.11f, 0, 0,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(matrix);
    }

    public static TunablePhotoFilter Hudson() {
        float[] matrix = new float[] {
                0.859f, 0f, 0f, 0, 36,
                0f, 1f, 0f, 0, 0,
                0f, 0f, 1f, 0, 0,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(matrix);
    }

    public static TunablePhotoFilter Amaro() {
        float[] matrix = new float[] {
                0.898f, 0f, 0f, 0, 26,
                0f, 1f, 0f, 0, 0,
                0f, 0f, 0.902f, 0, 25,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(matrix);
    }

    public static TunablePhotoFilter Nashville() {
        float[] matrix = new float[] {
                1f, 0f, 0f, 0, 0,
                0f, 0.906f, 0f, 0, 0,
                0f, 0f, 0.565f, 0, 65,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(matrix);
    }

    public static TunablePhotoFilter Rise() {
        float[] matrix = new float[] {
                0.914f, 0f, 0f, 0, 22,
                0f, 0.914f, 0f, 0, 22,
                0f, 0f, 0.875f, 0, 32,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(matrix);
    }

    public static TunablePhotoFilter Valencia() {
        float[] matrix = new float[] {
                0.824f, 0f, 0f, 0, 30,
                0f, 1f, 0f, 0, 0,
                0f, 0f, 0.804f, 0, 28,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(matrix);
    }

    public static TunablePhotoFilter Walden() {
        float[] matrix = new float[] {
                0.949f, 0f, 0f, 0, 11,
                0f, 0.812f, 0f, 0, 39,
                0f, 0f, 0.533f, 0, 90,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(matrix);
    }

    public static TunablePhotoFilter Sierra(Context context) {
        return new ColorCurveFilter(ColorCurveFactory.createFromImage(context, R.drawable.sierra_map));
    }

    public static TunablePhotoFilter Y1977(Context context) {
        return new ColorCurveFilter(ColorCurveFactory.createFromImage(context, R.drawable.y1977map));
    }

    public static TunablePhotoFilter TealAndOrange() {
        return new ColorCurveFilter(ColorCurveFactory.createTealAndOrange());
    }

    public static TunablePhotoFilter Smooth() {
        return new ColorCurveFilter(ColorCurveFactory.createSmooth());
    }

    public static TunablePhotoFilter Kelvin(Context context) {
        return new ColorCurveFilter(ColorCurveFactory.createFromImage(context, R.drawable.kelvin_map));
    }

    public static TunablePhotoFilter Xpro(Context context) {
        return new ColorCurveFilter(ColorCurveFactory.createFromImage(context, R.drawable.xpro_map));
    }

    public static TunablePhotoFilter Toaster(Context context) {
        return new ColorCurveFilter(ColorCurveFactory.createFromImage(context, R.drawable.toaster_map));
    }

    public static TunablePhotoFilter Random() {
        Random rand = new Random();
        float[] matrix = new float[] {
                rand.nextFloat(), rand.nextFloat() / 2, rand.nextFloat() / 2, 0, rand.nextInt(100) - 50,
                rand.nextFloat() / 2, rand.nextFloat(), rand.nextFloat() / 2, 0, rand.nextInt(100) - 50,
                rand.nextFloat() / 2, rand.nextFloat() / 2, rand.nextFloat(), 0, rand.nextInt(100) - 50,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(matrix);
    }

    public static TunablePhotoFilter ColorTemperature(Context context) {
        return new ColorCurveFilter(ColorCurveFactory.createFromImage(context, R.drawable.kelvin_map));
    }

    public static TunablePhotoFilter Exposure() {
        return new ColorCurveFilter(ColorCurveProviderFactory.exposureProvider());
    }
}
