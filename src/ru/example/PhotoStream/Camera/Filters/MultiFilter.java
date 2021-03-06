package ru.example.PhotoStream.Camera.Filters;

import ru.example.PhotoStream.Camera.RawBitmap;

import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

public class MultiFilter implements PhotoFilter {
    private static enum ImageOrientation {
        Top,
        Left,
        Bottom,
        Right,
    }

    public static enum OrientationChange {
        RotateClockwise,
        RotateCounterClockWise,
        MirrorVertically,
        MirrorHorizontally,
    }

    private SortedMap<Integer, TunablePhotoFilter> filters = new TreeMap<Integer, TunablePhotoFilter>();
    private ImageOrientation orientation = ImageOrientation.Top;
    private boolean mirrored = false;

    public synchronized void attachFilter(int filterPriority, TunablePhotoFilter photoFilter) {
        filters.put(filterPriority, photoFilter);
    }

    public synchronized Collection<TunablePhotoFilter> getAllFilters() {
        return filters.values();
    }

    public synchronized void changeOrientation(OrientationChange change) {
        switch (change) {
            case RotateClockwise:
                switch (orientation) {
                    case Top:
                        orientation = ImageOrientation.Right;
                        break;
                    case Left:
                        orientation = ImageOrientation.Top;
                        break;
                    case Bottom:
                        orientation = ImageOrientation.Left;
                        break;
                    case Right:
                        orientation = ImageOrientation.Bottom;
                        break;
                }
                break;
            case RotateCounterClockWise:
                switch (orientation) {
                    case Top:
                        orientation = ImageOrientation.Left;
                        break;
                    case Left:
                        orientation = ImageOrientation.Bottom;
                        break;
                    case Bottom:
                        orientation = ImageOrientation.Right;
                        break;
                    case Right:
                        orientation = ImageOrientation.Top;
                        break;
                }
                break;
            case MirrorVertically:
                mirrored = !mirrored;
                switch (orientation) {
                    case Top:
                        orientation = ImageOrientation.Bottom;
                        break;
                    case Bottom:
                        orientation = ImageOrientation.Top;
                        break;
                }
                break;
            case MirrorHorizontally:
                mirrored = !mirrored;
                switch (orientation) {
                    case Left:
                        orientation = ImageOrientation.Right;
                        break;
                    case Right:
                        orientation = ImageOrientation.Left;
                        break;
                }
                break;
        }
    }

    private synchronized void copyWithOrientation(RawBitmap source, RawBitmap destination) {
        int[] s = source.colors, d = destination.colors;
        switch (orientation) {
            case Top:
                destination.width = source.width;
                destination.height = source.height;
                if (mirrored) {
                    for (int i = 0; i < source.height; i++) {
                        for (int j = 0; j < source.width; j++) {
                            d[destination.width * i + destination.width - 1 - j] = s[source.width * i + j];
                        }
                    }
                } else {
                    for (int i = 0; i < source.height; i++) {
                        for (int j = 0; j < source.width; j++) {
                            d[destination.width * i + j] = s[destination.width * i + j];
                        }
                    }
                }
                break;
            case Right:
                destination.width = source.height;
                destination.height = source.width;
                if (mirrored) {
                    for (int i = 0; i < source.height; i++) {
                        for (int j = 0; j < source.width; j++) {
                            d[destination.width * (destination.height - 1 - j) + destination.width - 1 - i] = s[i * source.width + j];
                        }
                    }
                } else {
                    for (int i = 0; i < source.height; i++) {
                        for (int j = 0; j < source.width; j++) {
                            d[destination.width * j + destination.width - 1 - i] = s[i * source.width + j];
                        }
                    }
                }
                break;
            case Bottom:
                destination.width = source.width;
                destination.height = source.height;
                if (mirrored) {
                    for (int i = 0; i < source.height; i++) {
                        for (int j = 0; j < source.width; j++) {
                            d[destination.width * (destination.height - 1 - i) + j] = s[i * source.width + j];
                        }
                    }
                } else {
                    for (int i = 0; i < source.height; i++) {
                        for (int j = 0; j < source.width; j++) {
                            d[destination.width * (destination.height - 1 - i) + destination.width - 1 - j] = s[i * source.width + j];
                        }
                    }
                }
                break;
            case Left:
                destination.width = source.height;
                destination.height = source.width;
                if (mirrored) {
                    for (int i = 0; i < source.height; i++) {
                        for (int j = 0; j < source.width; j++) {
                           d[destination.width * j + i] = s[i * source.width + j];
                        }
                    }
                } else {
                    for (int i = 0; i < source.height; i++) {
                        for (int j = 0; j < source.width; j++) {
                            d[destination.width * (destination.height - 1 - j) + i] = s[i * source.width + j];
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void transformOpaqueRaw(RawBitmap source, RawBitmap destination) {
        copyWithOrientation(source, destination);
        ColorCurveFilter colorCurveFilter = null;
        for (TunablePhotoFilter filter: getAllFilters()) {
            if (filter.getType() == TunablePhotoFilter.TunableType.ColorCurve) {
                if (colorCurveFilter == null) {
                    colorCurveFilter = (ColorCurveFilter) filter;
                } else {
                    colorCurveFilter = new ColorCurveFilter(colorCurveFilter, (ColorCurveFilter) filter);
                }
            } else {
                if (colorCurveFilter != null) {
                    colorCurveFilter.transformOpaqueRaw(destination, destination);
                    colorCurveFilter = null;
                }
                filter.transformOpaqueRaw(destination, destination);
            }
        }
        if (colorCurveFilter != null) {
            colorCurveFilter.transformOpaqueRaw(destination, destination);
        }
    }
}
