package ru.example.PhotoStream.Camera.Filters;

import ru.example.PhotoStream.Camera.RawBitmap;

import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

public class MultiFilter implements PhotoFilter {

    private SortedMap<Integer, TunablePhotoFilter> filters = new TreeMap<Integer, TunablePhotoFilter>();

    public synchronized void changeFilter(int filterPriority, TunablePhotoFilter photoFilter) {
        filters.put(filterPriority, photoFilter);
    }

    public synchronized Collection<TunablePhotoFilter> getAllFilters() {
        return filters.values();
    }

    @Override
    public void transformOpaqueRaw(RawBitmap source, RawBitmap destination) {
        if (source != destination) {
            System.arraycopy(source.colors, 0, destination.colors, 0, source.width * source.height);
        }
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
