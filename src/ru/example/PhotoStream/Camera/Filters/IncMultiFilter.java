package ru.example.PhotoStream.Camera.Filters;

import android.content.Context;
import ru.example.PhotoStream.Camera.RawBitmap;

import java.util.HashMap;

public class IncMultiFilter implements PhotoFilter {
    public static class FilterHandler {
        private final TunablePhotoFilter photoFilter;
        private final double initialStrength;
        private final int maxUpdatePriority;
        private boolean locked = false;

        public FilterHandler(TunablePhotoFilter photoFilter, int maxUpdatePriority) {
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
            }
        }

        public synchronized void discardChanges() {
            photoFilter.setStrength(initialStrength);
            locked = true;
        }

        public synchronized void applyChanges() {
            locked = true;
        }

        public int getMaxUpdatePriority() {
            return maxUpdatePriority;
        }
    }

    private static final int MAX_UPDATE_PRIORITY = 1000;

    private MultiFilter multiFilter = new MultiFilter();
    private HashMap<TunablePhotoFilterFactory.SettingsFilterType, TunablePhotoFilter> filters = new HashMap<>();
    private TunablePhotoFilter photoFilter, whiteBalanceFilter;
    private TunablePhotoFilterFactory.FilterType photoFilterType;
    private WhiteBalanceFactory.WhiteBalanceType whiteBalanceType;
    private Context context;
    private RawBitmap source;

    public IncMultiFilter(Context context, RawBitmap sourceForAnalysis) {
        this.context = context;
        this.source = sourceForAnalysis;
        TunablePhotoFilterFactory.SettingsFilterType[] settingsFilterTypes = TunablePhotoFilterFactory.SettingsFilterType.values();
        for (TunablePhotoFilterFactory.SettingsFilterType filterType: settingsFilterTypes) {
            TunablePhotoFilter photoFilter = filterType.getFilter(context);
            multiFilter.attachFilter(filterType.getPriority(), photoFilter);
            filters.put(filterType, photoFilter);
        }
        setPhotoFilter(TunablePhotoFilterFactory.FilterType.NoFilter);
        setWhiteBalance(WhiteBalanceFactory.WhiteBalanceType.NoWhiteBalance);
    }

    public int getMaxUpdatePriority() {
        return MAX_UPDATE_PRIORITY;
    }

    public synchronized FilterHandler getSettingsFilterHandler(TunablePhotoFilterFactory.SettingsFilterType settingsFilterType) {
        return new FilterHandler(filters.get(settingsFilterType), settingsFilterType.getMaxUpdatePriority());
    }

    public synchronized void setPhotoFilter(TunablePhotoFilterFactory.FilterType photoFilterType) {
        this.photoFilterType = photoFilterType;
        photoFilter = this.photoFilterType.getFilter(context);
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
        whiteBalanceFilter = this.whiteBalanceType.getFilter(source);
        multiFilter.attachFilter(whiteBalanceType.getPriority(), whiteBalanceFilter);
    }

    public synchronized WhiteBalanceFactory.WhiteBalanceType getWhiteBalanceType() {
        return whiteBalanceType;
    }

    public synchronized FilterHandler getWhiteBalanceHandler() {
        return new FilterHandler(whiteBalanceFilter, whiteBalanceType.getMaxUpdatePriority());
    }

    @Override
    public void transformOpaqueRaw(RawBitmap source, RawBitmap destination) {
        multiFilter.transformOpaqueRaw(source, destination);
    }

    public void transformOpaqueRaw(RawBitmap source, RawBitmap destination, int maxUpdatePriority) {
        multiFilter.transformOpaqueRaw(source, destination, maxUpdatePriority);
    }

    public synchronized void changeOrientation(MultiFilter.OrientationChange change) {
        multiFilter.changeOrientation(change);
    }
}
