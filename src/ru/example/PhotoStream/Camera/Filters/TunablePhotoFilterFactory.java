package ru.example.PhotoStream.Camera.Filters;

import android.content.Context;
import ru.example.PhotoStream.R;

import java.util.Random;

public class TunablePhotoFilterFactory {
    /**
     * Enumerates available {@link ru.example.PhotoStream.Camera.Filters.PhotoFilter}s.
     */
    public static enum FilterType implements FilterDescription {
        NoFilter {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.NoFilter);
            }

            @Override
            public int getIconResource() {
                return R.drawable.filter_normal;
            }

            @Override
            public TunablePhotoFilter getFilter(Context context) {
                return NoFilter();
            }
        },
        Polaroid {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Polaroid);
            }

            @Override
            public int getIconResource() {
                return R.drawable.filter_polaroid;
            }

            @Override
            public TunablePhotoFilter getFilter(Context context) {
                return Polaroid();
            }
        },
        VintageBlackAndWhite {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.VintageBlackAndWhite);
            }

            @Override
            public int getIconResource() {
                return R.drawable.filter_vintage;
            }

            @Override
            public TunablePhotoFilter getFilter(Context context) {
                return VintageBlackAndWhite();
            }
        },
        Nashville {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Nashville);
            }

            @Override
            public int getIconResource() {
                return R.drawable.filter_nashville;
            }

            @Override
            public TunablePhotoFilter getFilter(Context context) {
                return Nashville();
            }
        },
        Sierra {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Sierra);
            }

            @Override
            public int getIconResource() {
                return R.drawable.filter_sierra;
            }

            @Override
            public TunablePhotoFilter getFilter(Context context) {
                return Sierra(context);
            }
        },
        Valencia {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Valencia);
            }

            @Override
            public int getIconResource() {
                return R.drawable.filter_valencia;
            }

            @Override
            public TunablePhotoFilter getFilter(Context context) {
                return Valencia();
            }
        },
        Walden {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Walden);
            }

            @Override
            public int getIconResource() {
                return R.drawable.filter_walden;
            }

            @Override
            public TunablePhotoFilter getFilter(Context context) {
                return Walden();
            }
        },
        Hudson {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Hudson);
            }

            @Override
            public int getIconResource() {
                return R.drawable.filter_hudson;
            }

            @Override
            public TunablePhotoFilter getFilter(Context context) {
                return Hudson();
            }
        },
        Amaro {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Amaro);
            }

            @Override
            public int getIconResource() {
                return R.drawable.filter_amaro;
            }

            @Override
            public TunablePhotoFilter getFilter(Context context) {
                return Amaro();
            }
        },
        Rise {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Rise);
            }

            @Override
            public int getIconResource() {
                return R.drawable.filter_rise;
            }

            @Override
            public TunablePhotoFilter getFilter(Context context) {
                return Rise();
            }
        },
        Y1977 {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Y1977);
            }

            @Override
            public int getIconResource() {
                return R.drawable.filter_1977;
            }

            @Override
            public TunablePhotoFilter getFilter(Context context) {
                return Y1977(context);
            }
        },
        Kelvin {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Kelvin);
            }

            @Override
            public int getIconResource() {
                return R.drawable.filter_kelvin;
            }

            @Override
            public TunablePhotoFilter getFilter(Context context) {
                return Kelvin(context);
            }
        },
        XPro {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Xpro);
            }

            @Override
            public int getIconResource() {
                return R.drawable.filter_xproii;
            }

            @Override
            public TunablePhotoFilter getFilter(Context context) {
                return Xpro(context);
            }
        },
        Toaster {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Toaster);
            }

            @Override
            public int getIconResource() {
                return R.drawable.filter_toaster;
            }

            @Override
            public TunablePhotoFilter getFilter(Context context) {
                return Toaster(context);
            }
        },
        TealAndOrange {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.TealAndOrange);
            }

            @Override
            public int getIconResource() {
                return R.drawable.filter_tealandorange;
            }

            @Override
            public TunablePhotoFilter getFilter(Context context) {
                return TealAndOrange();
            }
        },
        Negative {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Negative);
            }

            @Override
            public int getIconResource() {
                return R.drawable.filter_negative;
            }

            @Override
            public TunablePhotoFilter getFilter(Context context) {
                return Negative();
            }
        },
        Grayscale {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Grayscale);
            }

            @Override
            public int getIconResource() {
                return R.drawable.filter_grayscale;
            }

            @Override
            public TunablePhotoFilter getFilter(Context context) {
                return Grayscale();
            }
        },
        Sepia {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Sepia);
            }

            @Override
            public int getIconResource() {
                return R.drawable.filter_sepia;
            }

            @Override
            public TunablePhotoFilter getFilter(Context context) {
                return Sepia();
            }
        },
        Smooth {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Smooth);
            }

            @Override
            public int getIconResource() {
                return R.drawable.filter_smooth;
            }

            @Override
            public TunablePhotoFilter getFilter(Context context) {
                return Smooth();
            }
        },
        ColorReduction {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.ColorReduction);
            }

            @Override
            public int getIconResource() {
                return R.drawable.filter_16;
            }

            @Override
            public TunablePhotoFilter getFilter(Context context) {
                return ColorReduction();
            }
        },
        EightBit {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.EightBit);
            }

            @Override
            public int getIconResource() {
                return R.drawable.filter_8;
            }

            @Override
            public TunablePhotoFilter getFilter(Context context) {
                return EightBit();
            }
        };

        public int getIconResource() {
            return R.drawable.filter_normal;
        }
        public abstract TunablePhotoFilter getFilter(Context context);
        public int getPriority() {
            return 100;
        }
        public int getMaxUpdatePriority() {
            return 100;
        }
    }

    public static enum SettingsFilterType implements FilterDescription {
        ColorTemperature {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Temperature);
            }

            @Override
            public int getPriority() {
                return 1;
            }

            @Override
            public TunablePhotoFilter getFilter(Context context) {
                return ColorTemperature(context);
            }
        },

        Exposure {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Temperature);
            }

            @Override
            public int getPriority() {
                return 2;
            }

            @Override
            public TunablePhotoFilter getFilter(Context context) {
                return Exposure();
            }
        },

        Brightness {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Brightness);
            }

            @Override
            public int getPriority() {
                return 3;
            }

            @Override
            public TunablePhotoFilter getFilter(Context context) {
                return Brightness();
            }
        },

        Contrast {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Contrast);
            }

            @Override
            public int getPriority() {
                return 4;
            }

            @Override
            public TunablePhotoFilter getFilter(Context context) {
                return Contrast();
            }
        },

        LightRegions {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.LightRegions);
            }

            @Override
            public int getPriority() {
                return 5;
            }

            @Override
            public TunablePhotoFilter getFilter(Context context) {
                return LightRegions();
            }
        },

        DarkRegions {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.DarkRegions);
            }

            @Override
            public int getPriority() {
                return 6;
            }

            @Override
            public TunablePhotoFilter getFilter(Context context) {
                return DarkRegions();
            }
        },

        Saturation {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Saturation);
            }

            @Override
            public int getPriority() {
                return 7;
            }

            @Override
            public TunablePhotoFilter getFilter(Context context) {
                return Saturation();
            }
        },

        Sharpness {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Sharpness);
            }

            @Override
            public int getPriority() {
                return 101;
            }

            @Override
            public int getMaxUpdatePriority() {
                return 101;
            }

            @Override
            public TunablePhotoFilter getFilter(Context context) {
                return Sharpness();
            }
        },

        Vignette {
            @Override
            public String toString(Context context) {
                return context.getString(R.string.Vignette);
            }

            @Override
            public int getPriority() {
                return 102;
            }

            @Override
            public int getMaxUpdatePriority() {
                return 102;
            }

            @Override
            public TunablePhotoFilter getFilter(Context context) {
                return Vignette();
            }
        };

        public int getIconResource() {
            return R.drawable.filter_normal;
        }
        public abstract TunablePhotoFilter getFilter(Context context);
        public int getMaxUpdatePriority() {
            return 100;
        }
    }

    private static TunablePhotoFilter EightBit() {
        return new ColorCurveFilter(ColorCurveFactory.createEightBit());
    }

    private static TunablePhotoFilter Contrast() {
        return new ColorCurveFilter(ColorCurveProviderFactory.contrastProvider());
    }

    private static TunablePhotoFilter Brightness() {
        return new ColorCurveFilter(ColorCurveProviderFactory.brightnessProvider());
    }

    private static TunablePhotoFilter Saturation() {
        return new SaturationFilter();
    }

    private static TunablePhotoFilter LightRegions() {
        return new ColorCurveFilter(ColorCurveProviderFactory.lightRegionsProvider());
    }

    private static TunablePhotoFilter DarkRegions() {
        return new ColorCurveFilter(ColorCurveProviderFactory.darkRegionsProvider());
    }

    private static TunablePhotoFilter ColorReduction() {
        return new ColorCurveFilter(ColorCurveFactory.createColorReduction());
    }

    /**
     * Returns identity filter.
     *
     * @return photo filter
     */
    private static TunablePhotoFilter NoFilter() {
        return new IdentityFilter();
    }

    /**
     * Returns emboss filter.
     *
     * @return photo filter
     */
    private static TunablePhotoFilter Emboss() {
        return new Convolution3Filter(new float[][]{{-2, -1, 0}, {-1, 1, 1}, {0, 1, 2}}, 0);
    }

    /**
     * Returns blur filter.
     *
     * @return photo filter
     */
    private static TunablePhotoFilter Blur() {
        return new Convolution3Filter(new float[][]{{1f / 16, 1f / 8, 1f / 16}, {1f / 8, 1f / 4, 1f / 8}, {1f / 16, 1f / 8, 1f / 16}}, 0);
    }

    /**
     * Returns glow filter.
     *
     * @return photo filter
     */
    private static TunablePhotoFilter Glow() {
        return new Convolution3Filter(new float[][]{{1f / 16, 1f / 8, 1f / 16}, {1f / 8, 5f / 4, 1f / 8}, {1f / 16, 1f / 8, 1f / 16}}, 0);
    }

    /**
     * Returns sharpen filter.
     *
     * @return photo filter
     */
    private static TunablePhotoFilter Sharpness() {
        return new Convolution3Filter(new float[][]{{-1f/16, -1f/8, -1f/16}, {-1f/8, 7f/4, -1f/8}, {-1f/16, -1f/8, -1f/16}}, 0);
    }

    /**
     * Returns edges-negative filter.
     *
     * @return photo filter
     */
    private static TunablePhotoFilter EdgesNegative() {
        return new Convolution3Filter(new float[][]{{-1, -1, -1}, {-1, 8, -1}, {-1, -1, -1}}, 0);
    }

    /**
     * Returns edges-positive filter.
     *
     * @return photo filter
     */
    private static TunablePhotoFilter EdgesPositive() {
        return new Convolution3Filter(new float[][]{{1, 1, 1}, {1, -7, 1}, {1, 1, 1}}, 0);
    }

    /**
     * Negative filter,
     *
     * @return photo filter
     */
    private static TunablePhotoFilter Negative() {
        float[] matrix = new float[] {
                -1f, 0f, 0f, 0, 255,
                0f, -1f, 0f, 0, 255,
                0f, 0f, -1f, 0, 255,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(matrix);
    }

    private static TunablePhotoFilter Sepia() {
        float[] matrix = new float[] {
                0.393f, 0.769f, 0.180f, 0, 0,
                0.349f, 0.686f, 0.168f, 0, 0,
                0.272f, 0.534f, 0.131f, 0, 0,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(matrix);
    }

    private static TunablePhotoFilter Polaroid() {
        float[] matrix = new float[] {
                1.438f, -0.062f, -0.062f, 0, 0,
                -0.122f, 1.378f, -0.122f, 0, 0,
                -0.016f, -0.016f, 1.483f, 0, 0,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(matrix);
    }

    private static TunablePhotoFilter VintageBlackAndWhite() {
        float[] matrix = new float[] {
                0.75f, 0.75f, 0.75f, 0, 0,
                0.75f, 0.75f, 0.75f, 0, 0,
                0.75f, 0.75f, 0.75f, 0, 0,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(matrix);
    }

    private static TunablePhotoFilter Grayscale() {
        float[] matrix = new float[] {
                0.33f, 0.59f, 0.11f, 0, 0,
                0.33f, 0.59f, 0.11f, 0, 0,
                0.33f, 0.59f, 0.11f, 0, 0,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(matrix);
    }

    private static TunablePhotoFilter Hudson() {
        float[] matrix = new float[] {
                0.859f, 0f, 0f, 0, 36,
                0f, 1f, 0f, 0, 0,
                0f, 0f, 1f, 0, 0,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(matrix);
    }

    private static TunablePhotoFilter Amaro() {
        float[] matrix = new float[] {
                0.898f, 0f, 0f, 0, 26,
                0f, 1f, 0f, 0, 0,
                0f, 0f, 0.902f, 0, 25,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(matrix);
    }

    private static TunablePhotoFilter Nashville() {
        float[] matrix = new float[] {
                1f, 0f, 0f, 0, 0,
                0f, 0.906f, 0f, 0, 0,
                0f, 0f, 0.565f, 0, 65,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(matrix);
    }

    private static TunablePhotoFilter Rise() {
        float[] matrix = new float[] {
                0.914f, 0f, 0f, 0, 22,
                0f, 0.914f, 0f, 0, 22,
                0f, 0f, 0.875f, 0, 32,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(matrix);
    }

    private static TunablePhotoFilter Valencia() {
        float[] matrix = new float[] {
                0.824f, 0f, 0f, 0, 30,
                0f, 1f, 0f, 0, 0,
                0f, 0f, 0.804f, 0, 28,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(matrix);
    }

    private static TunablePhotoFilter Walden() {
        float[] matrix = new float[] {
                0.949f, 0f, 0f, 0, 11,
                0f, 0.812f, 0f, 0, 39,
                0f, 0f, 0.533f, 0, 90,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(matrix);
    }

    private static TunablePhotoFilter Sierra(Context context) {
        return new ColorCurveFilter(ColorCurveFactory.createFromImage(context, R.drawable.sierra_map));
    }

    private static TunablePhotoFilter Y1977(Context context) {
        return new ColorCurveFilter(ColorCurveFactory.createFromImage(context, R.drawable.y1977map));
    }

    private static TunablePhotoFilter TealAndOrange() {
        return new ColorCurveFilter(ColorCurveFactory.createTealAndOrange());
    }

    private static TunablePhotoFilter Smooth() {
        return new ColorCurveFilter(ColorCurveFactory.createSmooth());
    }

    private static TunablePhotoFilter Kelvin(Context context) {
        return new ColorCurveFilter(ColorCurveFactory.createFromImage(context, R.drawable.kelvin_map));
    }

    private static TunablePhotoFilter Xpro(Context context) {
        return new ColorCurveFilter(ColorCurveFactory.createFromImage(context, R.drawable.xpro_map));
    }

    private static TunablePhotoFilter Toaster(Context context) {
        return new ColorCurveFilter(ColorCurveFactory.createFromImage(context, R.drawable.toaster_map));
    }

    private static TunablePhotoFilter Random() {
        Random rand = new Random();
        float[] matrix = new float[] {
                rand.nextFloat(), rand.nextFloat() / 2, rand.nextFloat() / 2, 0, rand.nextInt(100) - 50,
                rand.nextFloat() / 2, rand.nextFloat(), rand.nextFloat() / 2, 0, rand.nextInt(100) - 50,
                rand.nextFloat() / 2, rand.nextFloat() / 2, rand.nextFloat(), 0, rand.nextInt(100) - 50,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(matrix);
    }

    private static TunablePhotoFilter ColorTemperature(Context context) {
        return new ColorCurveFilter(ColorCurveFactory.createFromImage(context, R.drawable.kelvin_map));
    }

    private static TunablePhotoFilter Exposure() {
        return new ColorCurveFilter(ColorCurveProviderFactory.exposureProvider());
    }

    private static TunablePhotoFilter Vignette() {
        return new VignetteFilter();
    }
}
