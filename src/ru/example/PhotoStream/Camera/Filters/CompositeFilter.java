package ru.example.PhotoStream.Camera.Filters;

import ru.example.PhotoStream.Camera.RawBitmap;

/**
 * Created by Genyaz on 18.05.2014.
 */
public class CompositeFilter implements PhotoFilter {
    private PhotoFilter[] filters;

    public CompositeFilter(PhotoFilter... filters) {
        this.filters = filters;
    }

    @Override
    public synchronized void transform(RawBitmap bitmap) {
        for (PhotoFilter filter: filters) {
            filter.transform(bitmap);
        }
    }

    @Override
    public synchronized void transformOpaque(RawBitmap bitmap) {
        for (PhotoFilter filter: filters) {
            filter.transformOpaque(bitmap);
        }
    }
}
