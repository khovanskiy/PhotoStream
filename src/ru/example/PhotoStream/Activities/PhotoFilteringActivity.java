package ru.example.PhotoStream.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import ru.example.PhotoStream.Camera.Filters.WhiteBalanceFactory;
import ru.example.PhotoStream.Console;
import ru.example.PhotoStream.R;
import ru.example.PhotoStream.Camera.Filters.MultiFilter;
import ru.example.PhotoStream.Camera.Filters.TunablePhotoFilter;
import ru.example.PhotoStream.Camera.Filters.TunablePhotoFilterFactory;
import ru.example.PhotoStream.Camera.RawBitmap;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class PhotoFilteringActivity extends Activity {
    private static final int WHITE_BALANCE_PRIORITY = 0;
    private static final int COLOR_TEMPERATURE_PRIORITY = 1;
    private static final int EXPOSURE_PRIORITY = 2;
    private static final int BRIGHTNESS_PRIORITY = 3;
    private static final int CONTRAST_PRIORITY = 4;
    private static final int LIGHT_REGIONS_PRIORITY = 5;
    private static final int DARK_REGIONS_PRIORITY = 6;
    private static final int SATURATION_PRIORITY = 7;
    private static final int SHARPNESS_PRIORITY = 8;
    private static final int PHOTO_FILTER_PRIORITY = 9;
    private static final int VIGNETTE_PRIORITY = 10;

    private static final int SCALE_DOWN = 2;

    private static Bitmap image = null;

    public static void setBitmap(Bitmap bitmap) {
        image = bitmap;
    }

    private Context context;
    private Bitmap currentBitmap, currentBitmapRotated, nextBitmap, nextBitmapRotated;
    private RawBitmap source, result;
    private ImageView imageView;
    private Button toLoadButton;
    private SeekBar photoFilterBar;
    private SeekBar whiteBalanceBar;
    private HashMap<String, TunablePhotoFilter> whiteBalanceFilters = new HashMap<>();

    private MultiFilter generalFilter;

    private AtomicBoolean continueRefreshing = new AtomicBoolean(false);
    private AtomicBoolean taskIsRunning = new AtomicBoolean(false);

    private class ImageRefreshTask extends AsyncTask<Void, Void, Void> {
        private boolean rotated;

        @Override
        protected Void doInBackground(Void... params) {
            while (continueRefreshing.get()) {
                continueRefreshing.set(false);
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

    private void refreshImage() {
        continueRefreshing.set(true);
        if (taskIsRunning.compareAndSet(false, true)) {
            new ImageRefreshTask().execute();
        }
    }

    private class MySeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        private TunablePhotoFilter filter;
        private final double minStrength, maxStrength;

        public MySeekBarChangeListener(TunablePhotoFilter filter, double minStrength, double maxStrength) {
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photofilteringactivity);
        context = this;
        Console.print("Filtering photo size = " + image.getWidth() + " " + image.getHeight());
        imageView = (ImageView) findViewById(R.id.photofilteringactivity_imageView);

        currentBitmap = Bitmap.createScaledBitmap(image, image.getWidth() / SCALE_DOWN, image.getHeight() / SCALE_DOWN, false);
        nextBitmap = Bitmap.createBitmap(image.getWidth() / SCALE_DOWN, image.getHeight() / SCALE_DOWN, Bitmap.Config.ARGB_8888);
        currentBitmapRotated = Bitmap.createBitmap(image.getHeight() / SCALE_DOWN, image.getWidth() / SCALE_DOWN, Bitmap.Config.ARGB_8888);
        nextBitmapRotated = Bitmap.createBitmap(image.getHeight() / SCALE_DOWN, image.getWidth() / SCALE_DOWN, Bitmap.Config.ARGB_8888);

        source = new RawBitmap(currentBitmap);
        result = new RawBitmap(nextBitmap.getWidth(), nextBitmap.getHeight());

        imageView.setImageBitmap(currentBitmap);
        TunablePhotoFilter brightness = TunablePhotoFilterFactory.Brightness();
        TunablePhotoFilter contrast = TunablePhotoFilterFactory.Contrast();
        TunablePhotoFilter saturation = TunablePhotoFilterFactory.Saturation();
        TunablePhotoFilter lightRegions = TunablePhotoFilterFactory.LightRegions();
        TunablePhotoFilter darkRegions = TunablePhotoFilterFactory.DarkRegions();
        TunablePhotoFilter colorTemperature = TunablePhotoFilterFactory.ColorTemperature(context);
        TunablePhotoFilter exposure = TunablePhotoFilterFactory.Exposure();
        TunablePhotoFilter sharpness = TunablePhotoFilterFactory.Sharpness();
        TunablePhotoFilter vignette = TunablePhotoFilterFactory.Vignette();

        photoFilterBar = (SeekBar) findViewById(R.id.photofilteringactivity_filterbar);
        Spinner photoFilterSpinner = (Spinner) findViewById(R.id.photofilteringactivity_filterspinner);
        TunablePhotoFilterFactory.FilterType[] filterTypes = TunablePhotoFilterFactory.FilterType.values();
        final String[] filterNames = new String[filterTypes.length];
        for (int i = 0; i < filterTypes.length; i++) {
            filterNames[i] = filterTypes[i].toString(context);
        }
        ArrayAdapter<String> photoFilterAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, filterNames);
        photoFilterSpinner.setAdapter(photoFilterAdapter);
        photoFilterSpinner.setSelection(0);
        photoFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TunablePhotoFilter filter = TunablePhotoFilterFactory.getFilterByName(context, filterNames[position]);
                double strength = photoFilterBar.getProgress() * 1.0 / photoFilterBar.getMax();
                filter.setStrength(strength);
                photoFilterBar.setOnSeekBarChangeListener(new MySeekBarChangeListener(filter, 0, 1));
                generalFilter.attachFilter(PHOTO_FILTER_PRIORITY, filter);
                refreshImage();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Spinner whiteBalanceSpinner = (Spinner) findViewById(R.id.photofilteringactivity_whitebalancespinner);
        whiteBalanceBar = (SeekBar) findViewById(R.id.photofilteringactivity_whitebalancebar);
        WhiteBalanceFactory.WhiteBalanceType[] whiteBalanceTypes = WhiteBalanceFactory.WhiteBalanceType.values();
        final String[] whiteBalanceNames = new String[whiteBalanceTypes.length];
        for (int i = 0; i < whiteBalanceTypes.length; i++) {
            whiteBalanceNames[i] = whiteBalanceTypes[i].toString(context);
            whiteBalanceFilters.put(whiteBalanceNames[i], WhiteBalanceFactory.byName(context, whiteBalanceNames[i], source));
        }
        ArrayAdapter<String> whiteBalanceAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, whiteBalanceNames);
        whiteBalanceSpinner.setAdapter(whiteBalanceAdapter);
        whiteBalanceSpinner.setSelection(0);
        whiteBalanceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TunablePhotoFilter whiteBalanceFilter = whiteBalanceFilters.get(whiteBalanceNames[position]);
                double strength = whiteBalanceBar.getProgress() * 1.0 / whiteBalanceBar.getMax();
                whiteBalanceFilter.setStrength(strength);
                whiteBalanceBar.setOnSeekBarChangeListener(new MySeekBarChangeListener(whiteBalanceFilter, 0, 1));
                generalFilter.attachFilter(WHITE_BALANCE_PRIORITY, whiteBalanceFilter);
                refreshImage();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        SeekBar brightnessBar = (SeekBar) findViewById(R.id.photofilteringactivity_brightnessbar);
        SeekBar contrastBar = (SeekBar) findViewById(R.id.photofilteringactivity_contrastbar);
        SeekBar saturationBar = (SeekBar) findViewById(R.id.photofilteringactivity_saturationbar);
        SeekBar lightRegionsBar = (SeekBar) findViewById(R.id.photofilteringactivity_lightregionsbar);
        SeekBar darkRegionsBar = (SeekBar) findViewById(R.id.photofilteringactivity_darkregionsbar);
        SeekBar colorTemperatureBar = (SeekBar) findViewById(R.id.photofilteringactivity_temperaturebar);
        SeekBar exposureBar = (SeekBar) findViewById(R.id.photofilteringactivity_exposurebar);
        SeekBar sharpnessBar = (SeekBar) findViewById(R.id.photofilteringactivity_sharpnessbar);
        SeekBar vignetteBar = (SeekBar) findViewById(R.id.photofilteringactivity_vignettebar);
        photoFilterBar = (SeekBar) findViewById(R.id.photofilteringactivity_filterbar);

        generalFilter = new MultiFilter();
        generalFilter.attachFilter(SATURATION_PRIORITY, saturation);
        generalFilter.attachFilter(CONTRAST_PRIORITY, contrast);
        generalFilter.attachFilter(BRIGHTNESS_PRIORITY, brightness);
        generalFilter.attachFilter(LIGHT_REGIONS_PRIORITY, lightRegions);
        generalFilter.attachFilter(DARK_REGIONS_PRIORITY, darkRegions);
        generalFilter.attachFilter(PHOTO_FILTER_PRIORITY, TunablePhotoFilterFactory.NoFilter());
        generalFilter.attachFilter(WHITE_BALANCE_PRIORITY, whiteBalanceFilters.get(getString(R.string.NoWhiteBalance)));
        generalFilter.attachFilter(COLOR_TEMPERATURE_PRIORITY, colorTemperature);
        generalFilter.attachFilter(EXPOSURE_PRIORITY, exposure);
        generalFilter.attachFilter(SHARPNESS_PRIORITY, sharpness);
        generalFilter.attachFilter(VIGNETTE_PRIORITY, vignette);

        brightnessBar.setOnSeekBarChangeListener(new MySeekBarChangeListener(brightness, -1, 1));
        contrastBar.setOnSeekBarChangeListener(new MySeekBarChangeListener(contrast, -1, 1));
        saturationBar.setOnSeekBarChangeListener(new MySeekBarChangeListener(saturation, -1, 1));
        lightRegionsBar.setOnSeekBarChangeListener(new MySeekBarChangeListener(lightRegions, -1, 1));
        darkRegionsBar.setOnSeekBarChangeListener(new MySeekBarChangeListener(darkRegions, -1, 1));
        colorTemperatureBar.setOnSeekBarChangeListener(new MySeekBarChangeListener(colorTemperature, -1, 1));
        exposureBar.setOnSeekBarChangeListener(new MySeekBarChangeListener(exposure, -1, 1));
        sharpnessBar.setOnSeekBarChangeListener(new MySeekBarChangeListener(sharpness, -1, 1));
        vignetteBar.setOnSeekBarChangeListener(new MySeekBarChangeListener(vignette, 0, 1));
        toLoadButton = (Button) findViewById(R.id.photofilteringactivity_toloadbutton);
        toLoadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toLoadButton.setEnabled(false);
                RawBitmap fullSource = new RawBitmap(image);
                RawBitmap fullDestination = new RawBitmap(image.getWidth(), image.getHeight());
                generalFilter.transformOpaqueRaw(fullSource, fullDestination);
                //fullSource.recycle();
                UploadActivity.setPicture(fullDestination.toBitmap());
                //fullDestination.recycle();
                Intent intent = new Intent(context, UploadActivity.class);
                startActivity(intent);
            }
        });
        ImageButton mirrorVerticallyButton = (ImageButton) findViewById(R.id.photofilteringactivity_mirrorverticallybutton);
        mirrorVerticallyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generalFilter.changeOrientation(MultiFilter.OrientationChange.MirrorVertically);
                refreshImage();
            }
        });
        ImageButton mirrorHorizontallyButton = (ImageButton) findViewById(R.id.photofilteringactivity_mirrorhorizontallybutton);
        mirrorHorizontallyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generalFilter.changeOrientation(MultiFilter.OrientationChange.MirrorHorizontally);
                refreshImage();
            }
        });
        ImageButton rotateClockwiseButton = (ImageButton) findViewById(R.id.photofilteringactivity_rotateclockwisebutton);
        rotateClockwiseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generalFilter.changeOrientation(MultiFilter.OrientationChange.RotateClockwise);
                refreshImage();
            }
        });
        ImageButton rotateCounterclockwiseButton = (ImageButton) findViewById(R.id.photofilteringactivity_rotatecounterclockwisebutton);
        rotateCounterclockwiseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generalFilter.changeOrientation(MultiFilter.OrientationChange.RotateCounterClockWise);
                refreshImage();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        toLoadButton.setEnabled(true);
    }
}