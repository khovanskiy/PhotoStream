package ru.example.PhotoStream.Activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import ru.example.PhotoStream.Camera.Filters.IncMultiFilter;
import ru.example.PhotoStream.Camera.Filters.MultiFilter;
import ru.example.PhotoStream.Camera.Filters.TunablePhotoFilterFactory;
import ru.example.PhotoStream.Camera.RawBitmap;
import ru.example.PhotoStream.R;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class PhotoCorrectionActivity extends ActionBarActivity implements View.OnClickListener {

    private class ImageRefreshTask extends AsyncTask<Void, Void, Void> {
        private boolean rotated;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            while (continueRefreshing.compareAndSet(true, false)) {
                generalFilter.transformOpaqueRaw(source, result, filterPriority.get());
                if (result.width == source.width) {
                    result.fillBitmap(nextBitmap);
                    rotated = false;
                } else {
                    result.fillBitmap(nextBitmapRotated);
                    rotated = true;
                }
                publishProgress();
            }
            taskIsRunning.set(false);
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... v) {
            if (rotated) {
                imageView.setImageBitmap(nextBitmapRotated);
            } else {
                imageView.setImageBitmap(nextBitmap);
            }
            Bitmap tmp = currentBitmap;
            currentBitmap = nextBitmap;
            nextBitmap = tmp;
            tmp = currentBitmapRotated;
            currentBitmapRotated = nextBitmapRotated;
            nextBitmapRotated = tmp;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBar.setVisibility(View.GONE);
        }
    }

    private class SeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        private IncMultiFilter.FilterHandler filterHandler;
        private final double minStrength;
        private final double maxStrength;

        public SeekBarChangeListener(IncMultiFilter.FilterHandler filterHandler, double minStrength, double maxStrength) {
            this.filterHandler = filterHandler;
            this.minStrength = minStrength;
            this.maxStrength = maxStrength;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            filterHandler.setStrength(calculateStrength(seekBar, minStrength, maxStrength));
            refreshImage(filterHandler.getMaxUpdatePriority());
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            filterHandler.setStrength(calculateStrength(seekBar, minStrength, maxStrength));
            refreshImage(generalFilter.getMaxUpdatePriority());
        }
    }

    private class FilterClickListener implements View.OnClickListener {

        private TunablePhotoFilterFactory.FilterType filterType;

        public FilterClickListener(TunablePhotoFilterFactory.FilterType filterType) {
             this.filterType = filterType;
        }

        @Override
        public void onClick(View v) {
            setCurrentFilter(filterType);
            refreshImage(generalFilter.getMaxUpdatePriority());
        }
    }

    private static Bitmap image = null;
    private static boolean moveBack = false;

    public static void setMoveBack(boolean b) {
        moveBack = b;
    }

    protected AtomicBoolean continueRefreshing = new AtomicBoolean(false);
    protected AtomicBoolean taskIsRunning = new AtomicBoolean(false);
    protected AtomicInteger filterPriority = new AtomicInteger(0);

    private ProgressBar progressBar;
    private ImageView imageView;

    private Bitmap currentBitmap;
    private Bitmap currentBitmapRotated;
    private Bitmap nextBitmap;
    private Bitmap nextBitmapRotated;

    private RawBitmap source;
    private RawBitmap result;

    private IncMultiFilter generalFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.photocorrectionactivity);

        progressBar = (ProgressBar) findViewById(R.id.photocorrecting_progressBar);
        progressBar.setVisibility(View.GONE);
        imageView = (ImageView) findViewById(R.id.photocorrecting_image);


        /**
         * Drawable bitmaps
         */
        int scale = findScale(image);
        currentBitmap = Bitmap.createScaledBitmap(image, image.getWidth() / scale, image.getHeight() / scale, false);
        nextBitmap = Bitmap.createBitmap(image.getWidth() / scale, image.getHeight() / scale, Bitmap.Config.ARGB_8888);
        currentBitmapRotated = Bitmap.createBitmap(image.getHeight() / scale, image.getWidth() / scale, Bitmap.Config.ARGB_8888);
        nextBitmapRotated = Bitmap.createBitmap(image.getHeight() / scale, image.getWidth() / scale, Bitmap.Config.ARGB_8888);
        imageView.setImageBitmap(currentBitmap);

        /**
         * Raw bitmaps
         */
        source = new RawBitmap(currentBitmap);
        result = new RawBitmap(currentBitmap.getWidth(), currentBitmap.getHeight());

        /**
         * General filter
         */
        generalFilter = new IncMultiFilter(this, source);

        /**
         * Brightness
         */
        SeekBar brightnessBar = (SeekBar) findViewById(R.id.photocorrecting_brightnessbar);
        brightnessBar.setOnSeekBarChangeListener(new SeekBarChangeListener(
                generalFilter.getSettingsFilterHandler(TunablePhotoFilterFactory.SettingsFilterType.Brightness), -1, 1));

        /**
         * Contrast
         */
        SeekBar contrastBar = (SeekBar) findViewById(R.id.photocorrecting_contrastbar);
        contrastBar.setOnSeekBarChangeListener(new SeekBarChangeListener(
                generalFilter.getSettingsFilterHandler(TunablePhotoFilterFactory.SettingsFilterType.Contrast), -1, 1));

        /**
         * Saturation
         */
        SeekBar saturationBar = (SeekBar) findViewById(R.id.photocorrecting_saturationbar);
        saturationBar.setOnSeekBarChangeListener(new SeekBarChangeListener(
                generalFilter.getSettingsFilterHandler(TunablePhotoFilterFactory.SettingsFilterType.Saturation), -1, 1));


        /**
         *  Exposure
         */
        SeekBar exposureBar = (SeekBar) findViewById(R.id.photocorrecting_exposurebar);
        exposureBar.setOnSeekBarChangeListener(new SeekBarChangeListener(
                generalFilter.getSettingsFilterHandler(TunablePhotoFilterFactory.SettingsFilterType.Exposure), -1, 1));

        /**
         * Shadows
         */
        SeekBar darkRegionsBar = (SeekBar) findViewById(R.id.photocorrecting_darkregionsbar);
        darkRegionsBar.setOnSeekBarChangeListener(new SeekBarChangeListener(
                generalFilter.getSettingsFilterHandler(TunablePhotoFilterFactory.SettingsFilterType.DarkRegions), -1, 1));

        /**
         * Light regions
         */
        SeekBar lightRegionsBar = (SeekBar) findViewById(R.id.photocorrecting_lightregionsbar);
        lightRegionsBar.setOnSeekBarChangeListener(new SeekBarChangeListener(
                generalFilter.getSettingsFilterHandler(TunablePhotoFilterFactory.SettingsFilterType.LightRegions), -1, 1));

        /**
         * Temperature
         */
        SeekBar temperatureBar = (SeekBar) findViewById(R.id.photocorrecting_temperaturebar);
        temperatureBar.setOnSeekBarChangeListener(new SeekBarChangeListener(
                generalFilter.getSettingsFilterHandler(TunablePhotoFilterFactory.SettingsFilterType.ColorTemperature), -1, 1));

        /**
         * Sharpness
         */
        SeekBar sharpnessBar = (SeekBar) findViewById(R.id.photocorrecting_sharpnessbar);
        sharpnessBar.setOnSeekBarChangeListener(new SeekBarChangeListener(
                generalFilter.getSettingsFilterHandler(TunablePhotoFilterFactory.SettingsFilterType.Sharpness), -1, 1));

        /**
         * Vignette
         */
        SeekBar vignetteBar = (SeekBar) findViewById(R.id.photocorrecting_vignettebar);
        vignetteBar.setOnSeekBarChangeListener(new SeekBarChangeListener(
                generalFilter.getSettingsFilterHandler(TunablePhotoFilterFactory.SettingsFilterType.Vignette), 0, 1));

        Button uploadButton = (Button) findViewById(R.id.photocorrecting_uploadbutton);
        uploadButton.setOnClickListener(this);
        ImageButton clockwiseButton = (ImageButton) findViewById(R.id.photocorrecting_clockwisebutton);
        clockwiseButton.setOnClickListener(this);
        ImageButton counterclockwiseButton = (ImageButton) findViewById(R.id.photocorrecting_counterclockwisebutton);
        counterclockwiseButton.setOnClickListener(this);
        ImageButton horizontalFlipButton = (ImageButton) findViewById(R.id.photocorrecting_horizontalflip_button);
        horizontalFlipButton.setOnClickListener(this);
        ImageButton verticalFlipButton = (ImageButton) findViewById(R.id.photocorrecting_verticalflip_button);
        verticalFlipButton.setOnClickListener(this);

        LinearLayout filtersView = (LinearLayout) findViewById(R.id.photocorrecting_filters_view);
        LayoutInflater inflater = this.getLayoutInflater();
        TunablePhotoFilterFactory.FilterType[] filterTypes = TunablePhotoFilterFactory.FilterType.values();

        setCurrentFilter(TunablePhotoFilterFactory.FilterType.NoFilter);

        for (int i = 0; i < filterTypes.length; ++i) {
            int imageResource = filterTypes[i].getIconResource();
            View filterBadge = inflater.inflate(R.layout.filterbadgeview, filtersView, false);
            ImageView filterImage = (ImageView) filterBadge.findViewById(R.id.filterbadgeview_image);
            filterImage.setImageResource(imageResource);

            String label = filterTypes[i].toString(this);
            TextView filterLabel = (TextView) filterBadge.findViewById(R.id.filterbadgeview_label);
            filterLabel.setText(label);

            filterBadge.setOnClickListener(new FilterClickListener(filterTypes[i]));

            filtersView.addView(filterBadge);
        }
    }

    private int findScale(Bitmap image) {
        return 2;
    }

    private void setCurrentFilter(TunablePhotoFilterFactory.FilterType filterType) {
        SeekBar filterPowerBar = (SeekBar) findViewById(R.id.photocorrecting_filterpowerbar);
        generalFilter.setPhotoFilter(filterType);
        IncMultiFilter.FilterHandler filterHandler = generalFilter.getPhotoFilterHandler();
        filterHandler.setStrength(calculateStrength(filterPowerBar, 0, 1));
        filterPowerBar.setOnSeekBarChangeListener(new SeekBarChangeListener(filterHandler, 0, 1));
    }

    private double calculateStrength(SeekBar seekBar, double min, double max) {
        return min + seekBar.getProgress() * (max - min) / seekBar.getMax();
    }

    public static void setBitmap(Bitmap bitmap) {
        image = bitmap;
    }

    private void refreshImage(int priority) {
        filterPriority.set(priority);
        continueRefreshing.set(true);
        if (taskIsRunning.compareAndSet(false, true)) {
            new ImageRefreshTask().execute();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.photocorrecting_counterclockwisebutton: {
                generalFilter.changeOrientation(MultiFilter.OrientationChange.RotateCounterClockWise);
                refreshImage(generalFilter.getMaxUpdatePriority());
            } break;
            case R.id.photocorrecting_clockwisebutton: {
                generalFilter.changeOrientation(MultiFilter.OrientationChange.RotateClockwise);
                refreshImage(generalFilter.getMaxUpdatePriority());
            } break;
            case R.id.photocorrecting_horizontalflip_button: {
                generalFilter.changeOrientation(MultiFilter.OrientationChange.MirrorHorizontally);
                refreshImage(generalFilter.getMaxUpdatePriority());
            } break;
            case R.id.photocorrecting_verticalflip_button: {
                generalFilter.changeOrientation(MultiFilter.OrientationChange.MirrorVertically);
                refreshImage(generalFilter.getMaxUpdatePriority());
            } break;
            case R.id.photocorrecting_uploadbutton: {
                RawBitmap fullSource = new RawBitmap(image);
                RawBitmap fullResult = new RawBitmap(image.getWidth(), image.getHeight());
                generalFilter.transformOpaqueRaw(fullSource, fullResult);
                fullSource.recycle();
                PhotoUploadActivity.setPicture(fullResult.toBitmap());
                fullResult.recycle();
                image.recycle();
                Intent intent = new Intent(this, PhotoUploadActivity.class);
                startActivity(intent);
            } break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (moveBack) {
            moveBack = false;
            onBackPressed();
        }
    }
}
