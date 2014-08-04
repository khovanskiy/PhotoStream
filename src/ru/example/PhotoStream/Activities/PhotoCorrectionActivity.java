package ru.example.PhotoStream.Activities;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ImageView;
import android.widget.SeekBar;
import ru.example.PhotoStream.Camera.Filters.MultiFilter;
import ru.example.PhotoStream.Camera.Filters.TunablePhotoFilter;
import ru.example.PhotoStream.Camera.Filters.TunablePhotoFilterFactory;
import ru.example.PhotoStream.Camera.RawBitmap;
import ru.example.PhotoStream.Console;
import ru.example.PhotoStream.R;

import java.util.concurrent.atomic.AtomicBoolean;

public final class PhotoCorrectionActivity extends ActionBarActivity {

    private class ImageRefreshTask extends AsyncTask<Void, Void, Void> {
        private boolean rotated;

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
            double strength = minStrength + progress * (maxStrength - minStrength) / seekBar.getMax();
            filter.setStrength(strength);
            refreshImage();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            double strength = minStrength + seekBar.getProgress() * (maxStrength - minStrength) / seekBar.getMax();
            filter.setStrength(strength);
            refreshImage();
        }
    }

    private static Bitmap image = null;

    protected AtomicBoolean continueRefreshing = new AtomicBoolean(false);
    protected AtomicBoolean taskIsRunning = new AtomicBoolean(false);

    private ImageView imageView;

    private Bitmap currentBitmap;
    private Bitmap currentBitmapRotated;
    private Bitmap nextBitmap;
    private Bitmap nextBitmapRotated;

    private RawBitmap source;
    private RawBitmap result;

    private MultiFilter generalFilter = new MultiFilter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photocorrectionactivity);

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
         * Shadows
         */
        TunablePhotoFilter darkRegionsFilter = TunablePhotoFilterFactory.DarkRegions();
        generalFilter.attachFilter(3, darkRegionsFilter);
        SeekBar darkRegionsBar = (SeekBar) findViewById(R.id.photocorrecting_darkregionsbar);
        darkRegionsBar.setOnSeekBarChangeListener(new SeekBarChangeListener(darkRegionsFilter, -1, 1));

        /**
         * Light regions
         */
        TunablePhotoFilter lightRegionsFilter = TunablePhotoFilterFactory.LightRegions();
        generalFilter.attachFilter(4, lightRegionsFilter);
        SeekBar lightRegionsBar = (SeekBar) findViewById(R.id.photocorrecting_lightregionsbar);
        lightRegionsBar.setOnSeekBarChangeListener(new SeekBarChangeListener(lightRegionsFilter, -1, 1));

        /**
         * Temperature
         */
        TunablePhotoFilter temperatureFilter = TunablePhotoFilterFactory.ColorTemperature(this);
        generalFilter.attachFilter(5, temperatureFilter);
        SeekBar temperatureBar = (SeekBar) findViewById(R.id.photocorrecting_temperaturebar);
        temperatureBar.setOnSeekBarChangeListener(new SeekBarChangeListener(temperatureFilter, -1, 1));
    }

    private int findScale(Bitmap image) {
        return 2;
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
}
