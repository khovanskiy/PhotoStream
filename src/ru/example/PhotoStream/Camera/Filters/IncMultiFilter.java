package ru.example.PhotoStream.Camera.Filters;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import ru.example.PhotoStream.Camera.RawBitmap;

import java.util.Collection;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class IncMultiFilter {

    public static enum OrientationChange {
        RotateClockwise,
        RotateCounterClockWise,
        MirrorVertically,
        MirrorHorizontally,
    }

    private static class MultiFilter implements PhotoFilter {

        @Override
        public void transformOpaqueRaw(RawBitmap source, RawBitmap destination) {
            transformOpaqueRaw(source, destination, Integer.MAX_VALUE - 1);
        }

        public static enum ImageOrientation {
            Top,
            Left,
            Bottom,
            Right,
        }

        private SortedMap<Integer, TunablePhotoFilter> filters = new TreeMap<Integer, TunablePhotoFilter>();
        private ImageOrientation orientation = ImageOrientation.Top;
        private boolean mirrored = false;

        public synchronized void attachFilter(int filterPriority, TunablePhotoFilter photoFilter) {
            filters.put(filterPriority, photoFilter);
        }

        public synchronized Collection<TunablePhotoFilter> getAllFilters(int maxPriority) {
            return filters.headMap(maxPriority + 1).values();
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

        public synchronized void setOrientation(ImageOrientation orientation) {
            this.orientation = orientation;
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

        public void transformOpaqueRaw(RawBitmap source, RawBitmap destination, int maxPriority) {
            copyWithOrientation(source, destination);
            ColorCurveFilter colorCurveFilter = null;
            for (TunablePhotoFilter filter: getAllFilters(maxPriority)) {
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

    public class FilterHandler {
        private final TunablePhotoFilter photoFilter;
        private final double initialStrength;
        private final int maxUpdatePriority;
        private boolean locked = false;

        private FilterHandler(TunablePhotoFilter photoFilter, int maxUpdatePriority) {
            this.photoFilter = photoFilter;
            this.initialStrength = photoFilter.getStrength();
            this.maxUpdatePriority = maxUpdatePriority;
        }

        public synchronized double getStrength() {
            return photoFilter.getStrength();
        }

        public synchronized void setStrength(double strength) {
            if (!locked) {
                photoFilter.setStrength(strength);
                refreshImage(maxUpdatePriority);
            }
        }

        public synchronized void discardChanges() {
            photoFilter.setStrength(initialStrength);
            refreshImage(MAX_UPDATE_PRIORITY);
            locked = true;
        }

        public synchronized void applyChanges() {
            refreshImage(MAX_UPDATE_PRIORITY);
            locked = true;
        }
    }

    public class RotationHandler {
        private MultiFilter.ImageOrientation initOrientation;
        private boolean locked = false;

        private RotationHandler() {
            initOrientation = multiFilter.orientation;
        }

        public synchronized void changeOrientation(OrientationChange orientationChange) {
            if (!locked) {
                multiFilter.changeOrientation(orientationChange);
                refreshImage(MAX_UPDATE_PRIORITY);
            }
        }

        public synchronized void apply() {
            locked = true;
        }

        public synchronized void discardChanges() {
            locked = true;
            multiFilter.setOrientation(initOrientation);
            refreshImage(MAX_UPDATE_PRIORITY);
        }
    }

    public interface OnImageChangedListener {
        public void onImageChanged(RawBitmap rawBitmap, Bitmap toFill);
        public void onFullImageReceived(Bitmap fullImage);
        public void onImageChanging();
    }

    private class ImageRefreshTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (onImageChangedListener != null) {
                onImageChangedListener.onImageChanging();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            while (continueRefreshing.compareAndSet(true, false)) {
                if (onImageChangedListener != null) {
                    onImageChangedListener.onImageChanging();
                }
                multiFilter.transformOpaqueRaw(rawSource, rawResult, filterPriority.get());
                Bitmap toFill;
                if (rawResult.width == rawSource.width) {
                    rawResult.fillBitmap(nextBitmap);
                    toFill = nextBitmap;
                } else {
                    rawResult.fillBitmap(nextBitmapRotated);
                    toFill = nextBitmapRotated;
                }
                if (onImageChangedListener != null) {
                    onImageChangedListener.onImageChanged(rawResult, toFill);
                }
                Bitmap tmp = currentBitmap;
                currentBitmap = nextBitmap;
                nextBitmap = tmp;
                tmp = currentBitmapRotated;
                currentBitmapRotated = nextBitmapRotated;
                nextBitmapRotated = tmp;
            }
            taskIsRunning.set(false);
            return null;
        }
    }

    private static final int MAX_UPDATE_PRIORITY = 1000;

    private MultiFilter multiFilter = new MultiFilter();
    private HashMap<TunablePhotoFilterFactory.SettingsFilterType, TunablePhotoFilter> filters = new HashMap<>();
    private TunablePhotoFilter photoFilter, whiteBalanceFilter;
    private TunablePhotoFilterFactory.FilterType photoFilterType;
    private WhiteBalanceFactory.WhiteBalanceType whiteBalanceType;

    private Context context;
    private Bitmap source;

    private Bitmap currentBitmap;
    private Bitmap currentBitmapRotated;
    private Bitmap nextBitmap;
    private Bitmap nextBitmapRotated;

    private RawBitmap rawSource;
    private RawBitmap rawResult;

    private OnImageChangedListener onImageChangedListener = null;

    protected AtomicBoolean continueRefreshing = new AtomicBoolean(false);
    protected AtomicBoolean taskIsRunning = new AtomicBoolean(false);
    protected AtomicInteger filterPriority = new AtomicInteger(0);

    public IncMultiFilter(Context context, Bitmap source) {
        this.context = context;
        this.source = source;

        int scale = findScale(source);
        currentBitmap = Bitmap.createScaledBitmap(source, source.getWidth() / scale, source.getHeight() / scale, false);
        nextBitmap = Bitmap.createBitmap(source.getWidth() / scale, source.getHeight() / scale, Bitmap.Config.ARGB_8888);
        currentBitmapRotated = Bitmap.createBitmap(source.getHeight() / scale, source.getWidth() / scale, Bitmap.Config.ARGB_8888);
        nextBitmapRotated = Bitmap.createBitmap(source.getHeight() / scale, source.getWidth() / scale, Bitmap.Config.ARGB_8888);
        rawSource = new RawBitmap(currentBitmap);
        rawResult = new RawBitmap(currentBitmap.getWidth(), currentBitmap.getHeight());
        TunablePhotoFilterFactory.SettingsFilterType[] settingsFilterTypes = TunablePhotoFilterFactory.SettingsFilterType.values();
        for (TunablePhotoFilterFactory.SettingsFilterType filterType: settingsFilterTypes) {
            TunablePhotoFilter photoFilter = filterType.getFilter(context);
            multiFilter.attachFilter(filterType.getPriority(), photoFilter);
            filters.put(filterType, photoFilter);
        }
        setPhotoFilter(TunablePhotoFilterFactory.FilterType.NoFilter);
        setWhiteBalance(WhiteBalanceFactory.WhiteBalanceType.NoWhiteBalance);
    }

    public synchronized FilterHandler getSettingsFilterHandler(TunablePhotoFilterFactory.SettingsFilterType settingsFilterType) {
        return new FilterHandler(filters.get(settingsFilterType), settingsFilterType.getMaxUpdatePriority());
    }

    public synchronized void setPhotoFilter(TunablePhotoFilterFactory.FilterType photoFilterType) {
        this.photoFilterType = photoFilterType;
        photoFilter = this.photoFilterType.getFilter(context);
        photoFilter.setStrength(0.5);
        multiFilter.attachFilter(this.photoFilterType.getPriority(), photoFilter);
    }

    public synchronized TunablePhotoFilterFactory.FilterType getPhotoFilterType() {
        return photoFilterType;
    }

    public synchronized FilterHandler getPhotoFilterHandler() {
        return new FilterHandler(photoFilter, photoFilterType.getMaxUpdatePriority());
    }

    public synchronized void setWhiteBalance(WhiteBalanceFactory.WhiteBalanceType whiteBalanceType) {
        this.whiteBalanceType = whiteBalanceType;
        whiteBalanceFilter = this.whiteBalanceType.getFilter(rawSource);
        multiFilter.attachFilter(whiteBalanceType.getPriority(), whiteBalanceFilter);
    }

    public synchronized WhiteBalanceFactory.WhiteBalanceType getWhiteBalanceType() {
        return whiteBalanceType;
    }

    public synchronized FilterHandler getWhiteBalanceHandler() {
        return new FilterHandler(whiteBalanceFilter, whiteBalanceType.getMaxUpdatePriority());
    }

    public synchronized RotationHandler getRotationHandler() {
        return new RotationHandler();
    }

    public synchronized void getFilteredImage() {
        AsyncTask<Void, Void, Bitmap> filterFullImage = new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                RawBitmap s = new RawBitmap(source), d = new RawBitmap(source.getWidth(), source.getHeight());
                multiFilter.transformOpaqueRaw(s, d, MAX_UPDATE_PRIORITY);
                s.recycle();
                return d.toBitmap();
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                onImageChangedListener.onFullImageReceived(result);
            }
        };
        filterFullImage.execute();
    }

    public void setOnImageChangedListener(OnImageChangedListener listener) {
        this.onImageChangedListener = listener;
    }

    private int findScale(Bitmap image) {
        return 2;
    }

    private void refreshImage(int priority) {
        filterPriority.set(priority);
        continueRefreshing.set(true);
        if (taskIsRunning.compareAndSet(false, true)) {
            new ImageRefreshTask().execute();
        }
    }

    public boolean sameBitmap(Bitmap bitmap) {
        return bitmap == source;
    }

    public void recycle() {
        while (taskIsRunning.get());
        currentBitmap.recycle();
        currentBitmapRotated.recycle();
        nextBitmap.recycle();
        nextBitmapRotated.recycle();
        rawResult.recycle();
        rawSource.recycle();
    }
}
