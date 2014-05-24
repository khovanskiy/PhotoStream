package ru.example.PhotoStream.Camera.Filters;

import android.graphics.ColorMatrix;

public class Filters {
    /**
     * Enumerates available {@link ru.example.PhotoStream.Camera.Filters.PhotoFilter}s.
     */
    public static enum FilterType {
        NoFilter,
        BlackAndWhite,
        Negative,
        Grayscale,
        Hudson,
        Amaro,
        Contrast,
        Sepia,
        Polaroid,
        VintageBlackAndWhite,
        Nashville,
        Sierra,
        Valencia,
    }

    /**
     * Returns {@link ru.example.PhotoStream.Camera.Filters.PhotoFilter} by its name or identity filter if this filter wasn't found.
     *
     * @param name photo filter name
     * @return photo filter
     */
    public static PhotoFilter byName(String name) {
        switch (name) {
            case "BlackAndWhite":
                return BlackAndWhite();
            case "Negative":
                return Negative();
            case "Grayscale":
                return Grayscale();
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
            case "Contrast":
                return Contrast();
            case "Sepia":
                return Sepia();
            case "Polaroid":
                return Polaroid();
            case "VintageBlackAndWhite":
                return VintageBlackAndWhite();
            case "Hudson":
                return Hudson();
            case "Amaro":
                return Amaro();
            case "Nashville":
                return Nashville();
            case "Sierra":
                return Sierra();
            case "Valencia":
                return Valencia();
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
     * Returns black-and-white filter.
     *
     * @return photo filter
     */
    public static PhotoFilter BlackAndWhite() {
        float[] matrix = new float[] {
                0.33f, 0.33f, 0.33f, 0, 0,
                0.33f, 0.33f, 0.33f, 0, 0,
                0.33f, 0.33f, 0.33f, 0, 0,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(new ColorMatrix(matrix));
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
                0.393f, 0.349f, 0.272f, 0, 0,
                0.769f, 0.686f, 0.534f, 0, 0,
                0.180f, 0.168f, 0.131f, 0, 0,
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
                1.5f, 1.5f, 1.5f, 0, 0,
                1.5f, 1.5f, 1.5f, 0, 0,
                1.5f, 1.5f, 1.5f, 0, 0,
                0, 0, 0, 1, 0
        };
        return new ColorMatrixPhotoFilter(new ColorMatrix(matrix));
    }

    public static PhotoFilter Grayscale() {
        float[] matrix = new float[] {
                0.33f, 0.33f, 0.33f, 0, 0,
                0.59f, 0.59f, 0.59f, 0, 0,
                0.11f, 0.11f, 0.11f, 0, 0,
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

    public static PhotoFilter Sierra() {
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
}
