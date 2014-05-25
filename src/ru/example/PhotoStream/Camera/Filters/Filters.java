package ru.example.PhotoStream.Camera.Filters;

import android.graphics.ColorMatrix;
import ru.example.PhotoStream.R;

import java.util.Random;

public class Filters {
    /**
     * Enumerates available {@link ru.example.PhotoStream.Camera.Filters.PhotoFilter}s.
     */
    public static enum FilterType {
        NoFilter {
            @Override
            public String toString() {
                return "Без фильтра";
            }
        },
        Negative {
            @Override
            public String toString() {
                return "Негатив";
            }
        },
        Grayscale {
            @Override
            public String toString() {
                return "Серые тона";
            }
        },
        Contrast {
            @Override
            public String toString() {
                return "Контрастный";
            }
        },
        Sepia {
            @Override
            public String toString() {
                return "Сепия";
            }
        },
        Polaroid {
            @Override
            public String toString() {
                return "Поляроид";
            }
        },
        VintageBlackAndWhite {
            @Override
            public String toString() {
                return "Старинный ч/б";
            }
        },
        Nashville {
            @Override
            public String toString() {
                return "Нэшвилль";
            }
        },
        Sierra {
            @Override
            public String toString() {
                return "Сьерра";
            }
        },
        Valencia {
            @Override
            public String toString() {
                return "Валенсия";
            }
        },
        Walden {
            @Override
            public String toString() {
                return "Вальден";
            }
        },
        Hudson {
            @Override
            public String toString() {
                return "Гудзон";
            }
        },
        Amaro {
            @Override
            public String toString() {
                return "Амаро";
            }
        },
        Rise {
            @Override
            public String toString() {
                return "Рассвет";
            }
        },
        Y1977 {
            @Override
            public String toString() {
                return "1984";
            }
        },
        Random {
            @Override
            public String toString() {
                return "Случайный";
            }
        },
        Special {
            @Override
            public String toString() {
                return "Особый";
            }
        };

        @Override
        public abstract String toString();
    }

    /**
     * Returns {@link ru.example.PhotoStream.Camera.Filters.PhotoFilter} by its name or identity filter if this filter wasn't found.
     *
     * @param name photo filter name
     * @return photo filter
     */
    public static PhotoFilter byName(String name) {
        switch (name) {
            case "Негатив":
                return Negative();
            case "Серые тона":
                return Grayscale();
            case "Контрастный":
                return Contrast();
            case "Сепия":
                return Sepia();
            case "Поляроид":
                return Polaroid();
            case "Старинный ч/б":
                return VintageBlackAndWhite();
            case "Гудзон":
                return Hudson();
            case "Амаро":
                return Amaro();
            case "Нэшвилль":
                return Nashville();
            case "Сьерра":
                return Sierra();
            case "Валенсия":
                return Valencia();
            case "Вальден":
                return Walden();
            case "Рассвет":
                return Rise();
            case "1984":
                return Y1977();
            case "Случайный":
                return Random();
            case "Особый":
                return Special();
            default:
                return NoFilter();
        }
    }

    /**
     * Returns identity filter.
     *
     * @return photo filter
     */
    public static PhotoFilter NoFilter() {
        return new IdentityFilter();
    }

    /**
     * Returns emboss filter.
     *
     * @return photo filter
     */
    public static PhotoFilter Emboss() {
        return new Convolution3Filter(new float[][]{{-2, -1, 0}, {-1, 1, 1}, {0, 1, 2}}, 0);
    }

    /**
     * Returns blur filter.
     *
     * @return photo filter
     */
    public static PhotoFilter Blur() {
        return new Convolution3Filter(new float[][]{{1f / 16, 1f / 8, 1f / 16}, {1f / 8, 1f / 4, 1f / 8}, {1f / 16, 1f / 8, 1f / 16}}, 0);
    }

    /**
     * Returns glow filter.
     *
     * @return photo filter
     */
    public static PhotoFilter Glow() {
        return new Convolution3Filter(new float[][]{{1f / 16, 1f / 8, 1f / 16}, {1f / 8, 5f / 4, 1f / 8}, {1f / 16, 1f / 8, 1f / 16}}, 0);
    }

    /**
     * Returns sharpen filter.
     *
     * @return photo filter
     */
    public static PhotoFilter Sharpen() {
        return new Convolution3Filter(new float[][]{{-1, -1, -1}, {-1, 9, -1}, {-1, -1, -1}}, 0);
    }

    /**
     * Returns edges-negative filter.
     *
     * @return photo filter
     */
    public static PhotoFilter EdgesNegative() {
        return new Convolution3Filter(new float[][]{{-1, -1, -1}, {-1, 8, -1}, {-1, -1, -1}}, 0);
    }

    /**
     * Returns edges-positive filter.
     *
     * @return photo filter
     */
    public static PhotoFilter EdgesPositive() {
        return new Convolution3Filter(new float[][]{{1, 1, 1}, {1, -7, 1}, {1, 1, 1}}, 0);
    }

    /**
     * Negative filter,
     *
     * @return photo filter
     */
    public static PhotoFilter Negative() {
        float[] matrix = new float[] {
                -1f, 0f, 0f, 0, 255,
                0f, -1f, 0f, 0, 255,
                0f, 0f, -1f, 0, 255,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(new ColorMatrix(matrix));
    }

    public static PhotoFilter Sepia() {
        float[] matrix = new float[] {
                0.393f, 0.769f, 0.180f, 0, 0,
                0.349f, 0.686f, 0.168f, 0, 0,
                0.272f, 0.534f, 0.131f, 0, 0,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(new ColorMatrix(matrix));
    }

    public static PhotoFilter Polaroid() {
        float[] matrix = new float[] {
                1.438f, -0.062f, -0.062f, 0, 0,
                -0.122f, 1.378f, -0.122f, 0, 0,
                -0.016f, -0.016f, 1.483f, 0, 0,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(new ColorMatrix(matrix));
    }

    public static PhotoFilter Contrast() {
        float[] matrix = new float[] {
                1.5f, 0, 0, 0, -40,
                0f, 1.5f, 0f, 0, -40,
                0f, 0f, 1.5f, 0, -40,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(new ColorMatrix(matrix));
    }

    public static PhotoFilter VintageBlackAndWhite() {
        float[] matrix = new float[] {
                0.75f, 0.75f, 0.75f, 0, 0,
                0.75f, 0.75f, 0.75f, 0, 0,
                0.75f, 0.75f, 0.75f, 0, 0,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(new ColorMatrix(matrix));
    }

    public static PhotoFilter Grayscale() {
        float[] matrix = new float[] {
                0.33f, 0.59f, 0.11f, 0, 0,
                0.33f, 0.59f, 0.11f, 0, 0,
                0.33f, 0.59f, 0.11f, 0, 0,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(new ColorMatrix(matrix));
    }

    public static PhotoFilter Hudson() {
        float[] matrix = new float[] {
                0.859f, 0f, 0f, 0, 36,
                0f, 1f, 0f, 0, 0,
                0f, 0f, 1f, 0, 0,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(new ColorMatrix(matrix));
    }

    public static PhotoFilter Amaro() {
        float[] matrix = new float[] {
                0.898f, 0f, 0f, 0, 26,
                0f, 1f, 0f, 0, 0,
                0f, 0f, 0.902f, 0, 25,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(new ColorMatrix(matrix));
    }

    public static PhotoFilter Nashville() {
        float[] matrix = new float[] {
                1f, 0f, 0f, 0, 0,
                0f, 0.906f, 0f, 0, 0,
                0f, 0f, 0.565f, 0, 65,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(new ColorMatrix(matrix));
    }

    public static PhotoFilter Rise() {
        float[] matrix = new float[] {
                0.914f, 0f, 0f, 0, 22,
                0f, 0.914f, 0f, 0, 22,
                0f, 0f, 0.875f, 0, 32,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(new ColorMatrix(matrix));
    }

    public static PhotoFilter Valencia() {
        float[] matrix = new float[] {
                0.824f, 0f, 0f, 0, 30,
                0f, 1f, 0f, 0, 0,
                0f, 0f, 0.804f, 0, 28,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(new ColorMatrix(matrix));
    }

    public static PhotoFilter Walden() {
        float[] matrix = new float[] {
                0.949f, 0f, 0f, 0, 11,
                0f, 0.812f, 0f, 0, 39,
                0f, 0f, 0.533f, 0, 90,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(new ColorMatrix(matrix));
    }

    public static PhotoFilter Sierra() {
        float[] matrix = new float[] {
                0.933f, 0f, 0f, 0, 10,
                0f, 0.871f, 0f, 0, 12,
                0f, 0f, 0.757f, 0, 27,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(new ColorMatrix(matrix));
    }

    public static PhotoFilter Y1977() {
        float[] matrix = new float[] {
                0.567f, 0f, 0f, 0, 81,
                0f, 0.773f, 0f, 0, 57,
                0f, 0f, 0.580f, 0, 64,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(new ColorMatrix(matrix));
    }

    public static PhotoFilter Random() {
        Random rand = new Random();
        float[] matrix = new float[] {
                rand.nextFloat(), rand.nextFloat() / 2, rand.nextFloat() / 2, 0, rand.nextInt(100) - 50,
                rand.nextFloat() / 2, rand.nextFloat(), rand.nextFloat() / 2, 0, rand.nextInt(100) - 50,
                rand.nextFloat() / 2, rand.nextFloat() / 2, rand.nextFloat(), 0, rand.nextInt(100) - 50,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(new ColorMatrix(matrix));
    }

    public static PhotoFilter Special() {
        return new SpecialFilter();
    }
}
