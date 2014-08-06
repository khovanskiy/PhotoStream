package ru.example.PhotoStream.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import ru.example.PhotoStream.Camera.Filters.MultiFilter;
import ru.example.PhotoStream.Camera.Filters.TunablePhotoFilter;
import ru.example.PhotoStream.Camera.Filters.TunablePhotoFilterFactory;
import ru.example.PhotoStream.Camera.RawBitmap;
import ru.example.PhotoStream.Console;
import ru.example.PhotoStream.R;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.Inflater;

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
                generalFilter.transformOpaqueRaw(source, result);
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
        private TunablePhotoFilter filter;
        private final double minStrength;
        private final double maxStrength;

        public SeekBarChangeListener(TunablePhotoFilter filter, double minStrength, double maxStrength) {
            this.filter = filter;
            this.minStrength = minStrength;
            this.maxStrength = maxStrength;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            filter.setStrength(calculateStrength(seekBar, minStrength, maxStrength));
            refreshImage();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            filter.setStrength(calculateStrength(seekBar, minStrength, maxStrength));
            refreshImage();
        }
    }

    private class FilterClickListener implements View.OnClickListener {

        private TunablePhotoFilter filter;

        public FilterClickListener(TunablePhotoFilter filter) {
             this.filter = filter;
        }

        @Override
        public void onClick(View v) {
            setCurrentFilter(filter);
            refreshImage();
        }
    }

    private static Bitmap image = null;

    protected AtomicBoolean continueRefreshing = new AtomicBoolean(false);
    protected AtomicBoolean taskIsRunning = new AtomicBoolean(false);

    private ProgressBar progressBar;
    private ImageView imageView;

    private Bitmap currentBitmap;
    private Bitmap currentBitmapRotated;
    private Bitmap nextBitmap;
    private Bitmap nextBitmapRotated;

    private RawBitmap source;
    private RawBitmap result;

    private MultiFilter generalFilter = new MultiFilter();
    private TunablePhotoFilter currentFilter = null;

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
         * Brightness
         */
        TunablePhotoFilter brightnessFilter = TunablePhotoFilterFactory.Brightness();
        generalFilter.attachFilter(0, brightnessFilter);
        SeekBar brightnessBar = (SeekBar) findViewById(R.id.photocorrecting_brightnessbar);
        brightnessBar.setOnSeekBarChangeListener(new SeekBarChangeListener(brightnessFilter, -1, 1));

        /**
         * Contrast
         */
        TunablePhotoFilter contrastFilter = TunablePhotoFilterFactory.Contrast();
        generalFilter.attachFilter(1, contrastFilter);
        SeekBar contrastBar = (SeekBar) findViewById(R.id.photocorrecting_contrastbar);
        contrastBar.setOnSeekBarChangeListener(new SeekBarChangeListener(contrastFilter, -1, 1));

        /**
         * Saturation
         */
        TunablePhotoFilter saturationFilter = TunablePhotoFilterFactory.Saturation();
        generalFilter.attachFilter(2, saturationFilter);
        SeekBar saturationBar = (SeekBar) findViewById(R.id.photocorrecting_saturationbar);
        saturationBar.setOnSeekBarChangeListener(new SeekBarChangeListener(saturationFilter, -1, 1));

        /**
         * Exposure
         */
        TunablePhotoFilter exposureFilter = TunablePhotoFilterFactory.Exposure();
        generalFilter.attachFilter(3, exposureFilter);
        SeekBar exposureBar = (SeekBar) findViewById(R.id.photocorrecting_exposurebar);
        exposureBar.setOnSeekBarChangeListener(new SeekBarChangeListener(saturationFilter, -1, 1));

        /**
         * Shadows
         */
        TunablePhotoFilter darkRegionsFilter = TunablePhotoFilterFactory.DarkRegions();
        generalFilter.attachFilter(4, darkRegionsFilter);
        SeekBar darkRegionsBar = (SeekBar) findViewById(R.id.photocorrecting_darkregionsbar);
        darkRegionsBar.setOnSeekBarChangeListener(new SeekBarChangeListener(darkRegionsFilter, -1, 1));

        /**
         * Light regions
         */
        TunablePhotoFilter lightRegionsFilter = TunablePhotoFilterFactory.LightRegions();
        generalFilter.attachFilter(5, lightRegionsFilter);
        SeekBar lightRegionsBar = (SeekBar) findViewById(R.id.photocorrecting_lightregionsbar);
        lightRegionsBar.setOnSeekBarChangeListener(new SeekBarChangeListener(lightRegionsFilter, -1, 1));

        /**
         * Temperature
         */
        TunablePhotoFilter temperatureFilter = TunablePhotoFilterFactory.ColorTemperature(this);
        generalFilter.attachFilter(6, temperatureFilter);
        SeekBar temperatureBar = (SeekBar) findViewById(R.id.photocorrecting_temperaturebar);
        temperatureBar.setOnSeekBarChangeListener(new SeekBarChangeListener(temperatureFilter, -1, 1));

        /**
         * Sharpness
         */
        TunablePhotoFilter sharpnessFilter = TunablePhotoFilterFactory.Sharpness();
        generalFilter.attachFilter(7, sharpnessFilter);
        SeekBar sharpnessBar = (SeekBar) findViewById(R.id.photocorrecting_sharpnessbar);
        sharpnessBar.setOnSeekBarChangeListener(new SeekBarChangeListener(sharpnessFilter, -1, 1));

        /**
         * Vignette
         */
        TunablePhotoFilter vignetteFilter = TunablePhotoFilterFactory.Vignette();
        generalFilter.attachFilter(101, vignetteFilter);
        SeekBar vignetteBar = (SeekBar) findViewById(R.id.photocorrecting_vignettebar);
        vignetteBar.setOnSeekBarChangeListener(new SeekBarChangeListener(vignetteFilter, 0, 1));

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

        setCurrentFilter(TunablePhotoFilterFactory.getFilterByName(this, filterTypes[0].toString(this)));

        for (int i = 0; i < filterTypes.length; ++i) {
            int imageResource = filterTypes[i].getIconResource();
            View filterBadge = inflater.inflate(R.layout.filterbadgeview, filtersView, false);
            ImageView filterImage = (ImageView) filterBadge.findViewById(R.id.filterbadgeview_image);
            filterImage.setImageResource(imageResource);

            String label = filterTypes[i].toString(this);
            TextView filterLabel = (TextView) filterBadge.findViewById(R.id.filterbadgeview_label);
            filterLabel.setText(label);

            TunablePhotoFilter filter = TunablePhotoFilterFactory.getFilterByName(this, label);
            filterBadge.setOnClickListener(new FilterClickListener(filter));

            filtersView.addView(filterBadge);
        }

        assert (currentFilter != null);
        //LinearLayout linearLayout = (LinearLayout) findViewById(R.id.photocorrecting_filters_layout);
        //ArrayAdapter<String> photoFilterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filterNames);
        //linearLayout.setAdapter(photoFilterAdapter);
        //linearLayout.setSelection(0);
    }

    private int findScale(Bitmap image) {
        return 2;
    }

    private void setCurrentFilter(TunablePhotoFilter filter) {
        currentFilter = filter;
        SeekBar filterPowerBar = (SeekBar) findViewById(R.id.photocorrecting_filterpowerbar);
        filter.setStrength(calculateStrength(filterPowerBar, 0, 1));
        filterPowerBar.setOnSeekBarChangeListener(new SeekBarChangeListener(currentFilter, 0, 1));
        generalFilter.attachFilter(100, currentFilter);
    }

    private double calculateStrength(SeekBar seekBar, double min, double max) {
        return min + seekBar.getProgress() * (max - min) / seekBar.getMax();
    }

    public static void setBitmap(Bitmap bitmap) {
        image = bitmap;
    }

    private void refreshImage() {
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
                refreshImage();
            } break;
            case R.id.photocorrecting_clockwisebutton: {
                generalFilter.changeOrientation(MultiFilter.OrientationChange.RotateClockwise);
                refreshImage();
            } break;
            case R.id.photocorrecting_horizontalflip_button: {
                generalFilter.changeOrientation(MultiFilter.OrientationChange.MirrorHorizontally);
                refreshImage();
            } break;
            case R.id.photocorrecting_verticalflip_button: {
                generalFilter.changeOrientation(MultiFilter.OrientationChange.MirrorVertically);
                refreshImage();
            } break;
            case R.id.photocorrecting_uploadbutton: {
                RawBitmap fullSource = new RawBitmap(image);
                RawBitmap fullResult = new RawBitmap(image.getWidth(), image.getHeight());
                generalFilter.transformOpaqueRaw(fullSource, fullResult);
                fullSource.recycle();
                UploadActivity.setPicture(fullResult.toBitmap());
                fullResult.recycle();
                Intent intent = new Intent(this, UploadActivity.class);
                startActivity(intent);
            } break;
        }
    }
}
