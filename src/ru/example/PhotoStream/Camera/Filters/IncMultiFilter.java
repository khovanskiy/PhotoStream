package ru.example.PhotoStream.Camera.Filters;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import android.os.Handler;
import android.os.Looper;
import ru.example.PhotoStream.Camera.RawBitmap;

public class IncMultiFilter extends MultiFilter {

    /*public class FilterHandler {
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
    }*/

    /*public class RotationHandler {
        private MultiFilter.ImageOrientation initOrientation;
        private boolean locked = false;

        private RotationHandler() {
            initOrientation = orientation;
        }

        public synchronized void changeOrientation(OrientationChange orientationChange) {
            if (!locked) {
                changeOrientation(orientationChange);
                refreshImage(MAX_UPDATE_PRIORITY);
            }
        }

        public synchronized void apply() {
            locked = true;
        }

        public synchronized void discardChanges() {
            locked = true;
            setImageOrientation(initOrientation);
            refreshImage(MAX_UPDATE_PRIORITY);
        }
    }*/

    /*public interface OnImageChangedListener {
        public void onImageChanged(RawBitmap rawBitmap, Bitmap toFill);

        public void onFullBitmapComputed(Bitmap fullImage);

        public void onImageChanging();
    }*/

    private static final Executor mPreviewExecutor = Executors.newSingleThreadExecutor();

    private class ImageRefreshTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*if (processListener != null) {
                processListener.onImageChanging();
            }*/
        }

        @Override
        protected Void doInBackground(Void... params) {
            while (continueRefreshing.compareAndSet(true, false)) {
                /*if (processListener != null) {
                    processListener.onImageChanging();
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
                if (processListener != null) {
                    processListener.onImageChanged(rawResult, toFill);
                }
                Bitmap tmp = currentBitmap;
                currentBitmap = nextBitmap;
                nextBitmap = tmp;
                tmp = currentBitmapRotated;
                currentBitmapRotated = nextBitmapRotated;
                nextBitmapRotated = tmp;*/
            }
            taskIsRunning.set(false);
            return null;
        }
    }

    private static final int MAX_UPDATE_PRIORITY = 1000;

    private Looper mLooper;

    //private MultiFilter multiFilter = new MultiFilter();
    //private HashMap<TunablePhotoFilterFactory.SettingsFilterType, TunablePhotoFilter> filters = new HashMap<>();
    //private TunablePhotoFilter photoFilter;
    //private TunablePhotoFilter whiteBalanceFilter;
    //private TunablePhotoFilterFactory.FilterType photoFilterType;
    //private WhiteBalanceFactory.WhiteBalanceType whiteBalanceType;

    private Bitmap source;

    private Bitmap currentBitmap;
    private Bitmap currentBitmapRotated;
    private Bitmap nextBitmap;
    private Bitmap nextBitmapRotated;

    private RawBitmap rawSource;
    private RawBitmap rawResult;

    private ProcessListener processListener = null;

    protected AtomicBoolean continueRefreshing = new AtomicBoolean(false);
    protected AtomicBoolean taskIsRunning = new AtomicBoolean(false);
    protected AtomicInteger filterPriority = new AtomicInteger(0);

    public IncMultiFilter(Bitmap source) {
        this.source = source;

        int scale = findScale(source);
        currentBitmap = Bitmap.createScaledBitmap(source, source.getWidth() / scale, source.getHeight() / scale, false);
        nextBitmap = Bitmap.createBitmap(source.getWidth() / scale, source.getHeight() / scale, Bitmap.Config.ARGB_8888);
        currentBitmapRotated = Bitmap.createBitmap(source.getHeight() / scale, source.getWidth() / scale, Bitmap.Config.ARGB_8888);
        nextBitmapRotated = Bitmap.createBitmap(source.getHeight() / scale, source.getWidth() / scale, Bitmap.Config.ARGB_8888);
        rawSource = new RawBitmap(currentBitmap);
        rawResult = new RawBitmap(currentBitmap.getWidth(), currentBitmap.getHeight());

        /*TunablePhotoFilterFactory.SettingsFilterType[] settingsFilterTypes = TunablePhotoFilterFactory.SettingsFilterType.values();
        for (TunablePhotoFilterFactory.SettingsFilterType filterType : settingsFilterTypes) {
            filters.put(filterType, photoFilter);
        }*/

        //setPhotoFilter(TunablePhotoFilterFactory.FilterType.NoFilter);
        //setWhiteBalance(WhiteBalanceFactory.WhiteBalanceType.NoWhiteBalance);
    }

    /*public synchronized FilterHandler getSettingsFilterHandler(TunablePhotoFilterFactory.SettingsFilterType settingsFilterType) {
        return new FilterHandler(filters.get(settingsFilterType), settingsFilterType.getMaxUpdatePriority());
    }*/

    /*public synchronized void setPhotoFilter(TunablePhotoFilterFactory.FilterType photoFilterType) {
        this.photoFilterType = photoFilterType;
        //photoFilter = this.photoFilterType.getFilter(context);
        photoFilter.setStrength(0.5);
        //multiFilter.attachFilter(this.photoFilterType.getPriority(), photoFilter);
    }*/

    /*public synchronized TunablePhotoFilterFactory.FilterType getPhotoFilterType() {
        return photoFilterType;
    }*/

    /*public synchronized FilterHandler getPhotoFilterHandler() {
        return new FilterHandler(photoFilter, photoFilterType.getMaxUpdatePriority());
    }*/

    /*public synchronized void setWhiteBalance(WhiteBalanceFactory.WhiteBalanceType whiteBalanceType) {
        this.whiteBalanceType = whiteBalanceType;
        whiteBalanceFilter = this.whiteBalanceType.getFilter(rawSource);
        //multiFilter.attachFilter(whiteBalanceType.getPriority(), whiteBalanceFilter);
    }

    public synchronized WhiteBalanceFactory.WhiteBalanceType getWhiteBalanceType() {
        return whiteBalanceType;
    }

    public synchronized FilterHandler getWhiteBalanceHandler() {
        return new FilterHandler(whiteBalanceFilter, whiteBalanceType.getMaxUpdatePriority());
    }*/

    public void takePicture() {
        mLooper = Looper.myLooper();
        mPreviewExecutor.execute(new Runnable() {
            @Override
            public void run() {
                RawBitmap s = new RawBitmap(source);
                RawBitmap d = new RawBitmap(source.getWidth(), source.getHeight());
                transformOpaqueRaw(s, d, MAX_UPDATE_PRIORITY);
                s.recycle();

                final Bitmap picture = d.toBitmap();
                new Handler(mLooper).post(new Runnable() {
                    @Override
                    public void run() {
                        processListener.onPictureTaken(picture);
                    }
                });
            }
        });
    }

    public void takePreview() {
        mLooper = Looper.myLooper();
        mPreviewExecutor.execute(new Runnable() {
            @Override
            public void run() {
                transformOpaqueRaw(rawSource, rawResult, MAX_UPDATE_PRIORITY);
                final Bitmap preview;
                if (rawResult.width == rawSource.width) {
                    rawResult.fillBitmap(nextBitmap);
                    preview = nextBitmap;
                } else {
                    rawResult.fillBitmap(nextBitmapRotated);
                    preview = nextBitmapRotated;
                }
                new Handler(mLooper).post(new Runnable() {
                    @Override
                    public void run() {
                        if (processListener != null) {
                            rawResult.fillBitmap(preview);
                            processListener.onPreviewTaken(preview);
                        }
                    }
                });
                Bitmap tmp = currentBitmap;
                currentBitmap = nextBitmap;
                nextBitmap = tmp;
                tmp = currentBitmapRotated;
                currentBitmapRotated = nextBitmapRotated;
                nextBitmapRotated = tmp;
            }
        });
    }

    private int findScale(Bitmap image) {
        return 2;
    }

    /*private void refreshImage(int priority) {
        filterPriority.set(priority);
        continueRefreshing.set(true);
        if (taskIsRunning.compareAndSet(false, true)) {
            new ImageRefreshTask().execute();
        }
    }*/

    /*public void recycle() {
        while (taskIsRunning.get()) ;
        currentBitmap.recycle();
        currentBitmapRotated.recycle();
        nextBitmap.recycle();
        nextBitmapRotated.recycle();
        rawResult.recycle();
        rawSource.recycle();
    }*/

    public void setProcessListener(ProcessListener listener) {
        this.processListener = listener;
    }

    public static abstract class ProcessListener {

        public void onPreviewTaken(Bitmap preview) {

        }

        public void onPictureTaken(Bitmap picture) {

        }
    }

    @Override
    protected void finalize() throws Throwable {
        System.out.println("IncMultiFilter deleted");
        super.finalize();
    }
}
