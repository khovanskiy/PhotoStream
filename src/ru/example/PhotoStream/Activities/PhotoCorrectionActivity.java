package ru.example.PhotoStream.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.*;
import android.widget.*;
import ru.example.PhotoStream.Camera.Filters.*;
import ru.example.PhotoStream.PortraitLandscapeListener;
import ru.example.PhotoStream.R;

import java.util.ArrayList;
import java.util.List;

public final class PhotoCorrectionActivity extends UIActivity {

    private Bitmap image;

    private ImageView imageView;
    private FrameLayout filtersFrame, settingsFrame;

    private List<View> filterBadges = new ArrayList<>();

    private IncMultiFilter generalFilter;
    private StateHolder mCurrentState;
    private FilterHolder mCurrentHolder;

    private static class FilterHolder {
        private int priority;
        private TunablePhotoFilter filter;
        private int icon;
        private int label;

        public FilterHolder(int priority, TunablePhotoFilter filter, int icon, int label) {
            this.priority = priority;
            this.filter = filter;
            this.icon = icon;
            this.label = label;
        }

        public int getIconResource() {
            return this.icon;
        }

        public int getLabelResourse() {
            return this.label;
        }

        public int getPriority() {
            return priority;
        }

        public TunablePhotoFilter getFilter() {
            return this.filter;
        }

        public double getMinStrength() {
            return -1;
        }

        public double getMaxStrength() {
            return 1;
        }
    }

    private static class StateHolder {
        IncMultiFilter generalFilter;
        List<FilterHolder> settingsFilters;
        List<FilterHolder> coloursFilters;
    }

    private final TabHost.OnTabChangeListener tabChangeListener = new TabHost.OnTabChangeListener() {
        @Override
        public void onTabChanged(String tabId) {
            rollChangesBack();
        }
    };

    private final IncMultiFilter.ProcessListener processListener = new IncMultiFilter.ProcessListener() {
        @Override
        public void onPreviewTaken(final Bitmap preview) {
            System.out.println("onPreviewTaken");
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.photocorrecting_image_processing);
            progressBar.setVisibility(View.GONE);
            imageView.setImageBitmap(preview);
        }

        @Override
        public void onPictureTaken(Bitmap picture) {
            System.out.println("onPictureTaken");
            FrameLayout progressFrame = (FrameLayout) findViewById(R.id.photocorrecting_processing);
            progressFrame.setVisibility(View.GONE);
            Intent intent = new Intent(PhotoCorrectionActivity.this, PhotoUploadActivity.class);
            weakCache(PhotoUploadActivity.class).put("pictureTaken", picture);
            startActivity(intent);
        }
    };

    private final SeekBar.OnSeekBarChangeListener settingsSeekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                mCurrentHolder.getFilter().setStrength(mCurrentHolder.getMinStrength() + progress * (mCurrentHolder.getMaxStrength() - mCurrentHolder.getMinStrength()) / 100);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private final SeekBar.OnSeekBarChangeListener filtersSeekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                mCurrentHolder.getFilter().setStrength(progress * 0.01);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.photocorrectionactivity);

        System.out.println("onCreate");

        image = (Bitmap) weakCache(PhotoCorrectionActivity.class).get("pictureTaken");
        if (image == null) {
            finish();
            return;
        }

        imageView = (ImageView) findViewById(R.id.photocorrecting_image);

        mCurrentState = (StateHolder) weakCache(PhotoCorrectionActivity.class).get("currentState");
        if (mCurrentState == null) {
            mCurrentState = new StateHolder();

            generalFilter = mCurrentState.generalFilter = new IncMultiFilter(image);

            List<FilterHolder> settingsFilters = mCurrentState.settingsFilters = new ArrayList<>();
            settingsFilters.add(new FilterHolder(0, TunablePhotoFilterFactory.ColorTemperature(this), R.drawable._0004_f_temperature, R.string.Temperature));
            settingsFilters.add(new FilterHolder(1, TunablePhotoFilterFactory.Exposure(), R.drawable._0003_f_exposure, R.string.Exposure));
            settingsFilters.add(new FilterHolder(2, TunablePhotoFilterFactory.Brightness(), R.drawable._0007_f_brightness, R.string.Brightness));
            settingsFilters.add(new FilterHolder(3, TunablePhotoFilterFactory.Contrast(), R.drawable._0005_f_contrast, R.string.Contrast));

            List<FilterHolder> colourFilters = mCurrentState.coloursFilters = new ArrayList<>();
            colourFilters.add(new FilterHolder(settingsFilters.size(), TunablePhotoFilterFactory.NoFilter(), R.drawable.filter_normal, R.string.NoFilter));
            colourFilters.add(new FilterHolder(settingsFilters.size(), TunablePhotoFilterFactory.Polaroid(), R.drawable.filter_polaroid, R.string.Polaroid));
            colourFilters.add(new FilterHolder(settingsFilters.size(), TunablePhotoFilterFactory.VintageBlackAndWhite(), R.drawable.filter_vintage, R.string.VintageBlackAndWhite));
            colourFilters.add(new FilterHolder(settingsFilters.size(), TunablePhotoFilterFactory.Nashville(), R.drawable.filter_nashville, R.string.Nashville));
            colourFilters.add(new FilterHolder(settingsFilters.size(), TunablePhotoFilterFactory.Sierra(this), R.drawable.filter_sierra, R.string.Sierra));
            colourFilters.add(new FilterHolder(settingsFilters.size(), TunablePhotoFilterFactory.Valencia(), R.drawable.filter_valencia, R.string.Valencia));
            colourFilters.add(new FilterHolder(settingsFilters.size(), TunablePhotoFilterFactory.Walden(), R.drawable.filter_walden, R.string.Walden));
            colourFilters.add(new FilterHolder(settingsFilters.size(), TunablePhotoFilterFactory.Hudson(), R.drawable.filter_hudson, R.string.Hudson));
            colourFilters.add(new FilterHolder(settingsFilters.size(), TunablePhotoFilterFactory.Amaro(), R.drawable.filter_amaro, R.string.Amaro));
            colourFilters.add(new FilterHolder(settingsFilters.size(), TunablePhotoFilterFactory.Rise(), R.drawable.filter_rise, R.string.Rise));
            colourFilters.add(new FilterHolder(settingsFilters.size(), TunablePhotoFilterFactory.Y1977(this), R.drawable.filter_y1977, R.string.Y1977));
            colourFilters.add(new FilterHolder(settingsFilters.size(), TunablePhotoFilterFactory.Kelvin(this), R.drawable.filter_kelvin, R.string.Kelvin));
            colourFilters.add(new FilterHolder(settingsFilters.size(), TunablePhotoFilterFactory.Xpro(this), R.drawable.filter_xproii, R.string.Xpro));
            colourFilters.add(new FilterHolder(settingsFilters.size(), TunablePhotoFilterFactory.Toaster(this), R.drawable.filter_toaster, R.string.Toaster));

            for (FilterHolder holder : settingsFilters) {
                generalFilter.attachFilter(holder.getPriority(), holder.getFilter());
            }
            for (FilterHolder holder : colourFilters) {
                generalFilter.attachFilter(holder.getPriority(), holder.getFilter());
            }

            weakCache(PhotoCorrectionActivity.class).put("currentState", mCurrentState);
        }
        generalFilter = mCurrentState.generalFilter;
        generalFilter.setProcessListener(processListener);

        int pictureOrientation = getIntent().getIntExtra("pictureOrientation", PortraitLandscapeListener.ORIENTATION_PORTRAIT_NORMAL);
        switch (pictureOrientation) {
            case PortraitLandscapeListener.ORIENTATION_PORTRAIT_NORMAL: {
                generalFilter.changeOrientation(MultiFilter.OrientationChange.RotateClockwise);
            } break;
            case PortraitLandscapeListener.ORIENTATION_PORTRAIT_INVERTED: {
                generalFilter.changeOrientation(MultiFilter.OrientationChange.RotateCounterClockWise);
            } break;
            case PortraitLandscapeListener.ORIENTATION_LANDSCAPE_NORMAL: {
                //nothing to do
            } break;
            case PortraitLandscapeListener.ORIENTATION_LANDSCAPE_INVERTED: {
                generalFilter.changeOrientation(MultiFilter.OrientationChange.RotateClockwise);
                generalFilter.changeOrientation(MultiFilter.OrientationChange.RotateClockwise);
            } break;
        }

        final TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();
        tabHost.getTabWidget().setShowDividers(TabWidget.SHOW_DIVIDER_MIDDLE);
        tabHost.getTabWidget().setDividerDrawable(R.drawable.photocorrection_tab_divider);
        TabHost.TabSpec tabSpec = tabHost.newTabSpec("filters");
        tabSpec.setIndicator(createTabView(this, getString(R.string.ImageFilters)));
        tabSpec.setContent(R.id.photocorrecting_filters_tab);
        tabHost.addTab(tabSpec);
        tabSpec = tabHost.newTabSpec("settings");
        tabSpec.setIndicator(createTabView(this, getString(R.string.Settings)));
        tabSpec.setContent(R.id.photocorrecting_settings_tab);
        tabHost.addTab(tabSpec);

        filtersFrame = (FrameLayout) findViewById(R.id.photocorrecting_filters_tab);
        switchFilterFrame(findViewById(R.id.photocorrecting_filters_scrollview));
        settingsFrame = (FrameLayout) findViewById(R.id.photocorrecting_settings_tab);
        switchSettingsFrame(findViewById(R.id.photocorrecting_settings_scrollview));

        createSettingsFilterViews(mCurrentState.settingsFilters);
        createColourFilterViews(mCurrentState.coloursFilters);

        setupRotationUI();
        tabHost.setOnTabChangedListener(tabChangeListener);

        generalFilter.takePreview();
    }

    protected void createColourFilterViews(final List<FilterHolder> holders) {
        LinearLayout layout = (LinearLayout) findViewById(R.id.photocorrecting_filters_layout);
        for (int i = 0; i < holders.size(); ++i) {
            final FilterHolder holder = holders.get(i);
            View view = getLayoutInflater().inflate(R.layout.filterbadgeview, null);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onFilterClick(v, holder);
                }
            });
            view.setSelected(i == 0);

            ImageView badgeImage = (ImageView) view.findViewById(R.id.filterbadgeview_image);
            badgeImage.setImageResource(holder.getIconResource());
            TextView badgeTitle = (TextView) view.findViewById(R.id.filterbadgeview_label);
            badgeTitle.setText(getResources().getString(holder.getLabelResourse()));

            filterBadges.add(view);
            layout.addView(view);
        }
    }

    private void createSettingsFilterViews(final List<FilterHolder> holders) {
        LinearLayout layout = (LinearLayout) findViewById(R.id.photocorrecting_settings_layout);

        View rotationBadge = createBadgeView(R.drawable._0009_f_rotates, getString(R.string.Rotation));
        rotationBadge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MultiFilter.ImageOrientation previousImageOrientation = generalFilter.getImageOrientation();
                ImageButton applyChanges = (ImageButton) findViewById(R.id.photocorrecting_rotation_apply);
                applyChanges.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        generalFilter.takePreview();
                        switchSettingsFrame(findViewById(R.id.photocorrecting_settings_scrollview));
                    }
                });
                ImageButton discardChanges = (ImageButton) findViewById(R.id.photocorrecting_rotation_discard);
                discardChanges.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        generalFilter.setImageOrientation(previousImageOrientation);
                        generalFilter.takePreview();
                        switchSettingsFrame(findViewById(R.id.photocorrecting_settings_scrollview));
                    }
                });
                switchSettingsFrame(findViewById(R.id.photocorrecting_rotation_editing));
            }
        });
        layout.addView(rotationBadge);

        for (int i = 0; i < holders.size(); ++i) {
            final FilterHolder holder = holders.get(i);
            View view = createBadgeView(holder.getIconResource(), getResources().getString(holder.getLabelResourse()));
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSettingsClick(holder);
                }
            });
            layout.addView(view);
        }
    }

    private View createBadgeView(int iconResourse, String label) {
        View view = getLayoutInflater().inflate(R.layout.settingbagdeview, null);
        ImageView iconView = (ImageView) view.findViewById(R.id.settingbadgeview_image);
        iconView.setImageResource(iconResourse);
        TextView titleView = (TextView) view.findViewById(R.id.settingbadgeview_label);
        titleView.setText(label);
        return view;
    }

    private void setupRotationUI() {
        ImageButton rotateLeft = (ImageButton) findViewById(R.id.photocorrecting_rotation_left);
        rotateLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generalFilter.changeOrientation(MultiFilter.OrientationChange.RotateCounterClockWise);
                generalFilter.takePreview();
            }
        });
        ImageButton rotateRight = (ImageButton) findViewById(R.id.photocorrecting_rotation_right);
        rotateRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generalFilter.changeOrientation(IncMultiFilter.OrientationChange.RotateClockwise);
                generalFilter.takePreview();
            }
        });
        ImageButton flipVertical = (ImageButton) findViewById(R.id.photocorrecting_flip_vertical);
        flipVertical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generalFilter.changeOrientation(IncMultiFilter.OrientationChange.MirrorVertically);
                generalFilter.takePreview();
            }
        });
        ImageButton flipHorizontal = (ImageButton) findViewById(R.id.photocorrecting_flip_horizontal);
        flipHorizontal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generalFilter.changeOrientation(IncMultiFilter.OrientationChange.MirrorHorizontally);
                generalFilter.takePreview();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.photo_correction_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_next: {
                FrameLayout progressFrame = (FrameLayout) findViewById(R.id.photocorrecting_processing);
                progressFrame.setVisibility(View.VISIBLE);
                generalFilter.takePicture();
            }
            break;
            case android.R.id.home: {
                onRealBackClick();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void switchFilterFrame(View v) {
        findViewById(R.id.photocorrecting_filters_scrollview).setVisibility(View.INVISIBLE);
        findViewById(R.id.photocorrecting_filters_editing).setVisibility(View.INVISIBLE);
        filtersFrame.bringChildToFront(v);
        v.setVisibility(View.VISIBLE);
    }

    private void switchSettingsFrame(View v) {
        findViewById(R.id.photocorrecting_settings_scrollview).setVisibility(View.INVISIBLE);
        findViewById(R.id.photocorrecting_settings_editing).setVisibility(View.INVISIBLE);
        findViewById(R.id.photocorrecting_rotation_editing).setVisibility(View.INVISIBLE);
        settingsFrame.bringChildToFront(v);
        v.setVisibility(View.VISIBLE);
    }

    private void onFilterClick(View v, FilterHolder holder) {
        if (!v.isSelected()) {
            for (View view : filterBadges) {
                view.setSelected(false);
            }
            v.setSelected(true);
            holder.getFilter().setStrength(0.5f);
            generalFilter.attachFilter(holder.getPriority(), holder.getFilter());
            generalFilter.takePreview();
        } else {
            mCurrentHolder = holder;

            TextView filterLabel = (TextView) findViewById(R.id.photocorrecting_filters_label);
            filterLabel.setText(getResources().getString(holder.getLabelResourse()));

            SeekBar filterBar = (SeekBar) findViewById(R.id.photocorrecting_filters_seekbar);
            filterBar.setOnSeekBarChangeListener(filtersSeekBarListener);

            final double previousStrength = mCurrentHolder.getFilter().getStrength();
            filterBar.setProgress((int) (mCurrentHolder.getFilter().getStrength() * 100));

            ImageButton applyButton = (ImageButton) findViewById(R.id.photocorrecting_filters_apply);
            applyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    generalFilter.takePreview();
                    switchFilterFrame(findViewById(R.id.photocorrecting_filters_scrollview));
                }
            });
            ImageButton discardButton = (ImageButton) findViewById(R.id.photocorrecting_filters_discard);
            discardButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCurrentHolder.getFilter().setStrength(previousStrength);
                    generalFilter.takePreview();
                    switchFilterFrame(findViewById(R.id.photocorrecting_filters_scrollview));
                }
            });

            switchFilterFrame(findViewById(R.id.photocorrecting_filters_editing));
        }
    }

    private void onSettingsClick(FilterHolder holder) {
        mCurrentHolder = holder;

        TextView filterLabel = (TextView) findViewById(R.id.photocorrecting_settings_label);
        filterLabel.setText(getResources().getString(holder.getLabelResourse()));

        SeekBar filterBar = (SeekBar) findViewById(R.id.photocorrecting_settings_seekbar);
        filterBar.setOnSeekBarChangeListener(settingsSeekBarListener);

        final double previousStrength = mCurrentHolder.getFilter().getStrength();
        filterBar.setProgress((int) ((mCurrentHolder.getFilter().getStrength() - mCurrentHolder.getMinStrength()) * 100 / (mCurrentHolder.getMaxStrength() - mCurrentHolder.getMinStrength())));

        ImageButton applyButton = (ImageButton) findViewById(R.id.photocorrecting_settings_apply);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generalFilter.takePreview();
                switchSettingsFrame(findViewById(R.id.photocorrecting_settings_scrollview));
            }
        });
        ImageButton discardButton = (ImageButton) findViewById(R.id.photocorrecting_settings_discard);
        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentHolder.getFilter().setStrength(previousStrength);
                generalFilter.takePreview();
                switchSettingsFrame(findViewById(R.id.photocorrecting_settings_scrollview));
            }
        });

        switchSettingsFrame(findViewById(R.id.photocorrecting_settings_editing));
    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("onPause");
    }

    @Override
    public void onBackPressed() {
        if (findViewById(R.id.photocorrecting_processing).getVisibility() != View.VISIBLE && !rollChangesBack()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Отменить все изменения?")
                    .setCancelable(false)
                    .setNegativeButton("Нет",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            })
                    .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onRealBackClick();
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private boolean rollChangesBack() {
        ImageButton filterDiscard = (ImageButton) findViewById(R.id.photocorrecting_filters_discard);
        if (findViewById(R.id.photocorrecting_filters_editing).getVisibility() == View.VISIBLE) {
            filterDiscard.performClick();
            switchFilterFrame(findViewById(R.id.photocorrecting_filters_scrollview));
            return true;
        }
        ImageButton settingDiscard = (ImageButton) findViewById(R.id.photocorrecting_settings_discard);
        if (findViewById(R.id.photocorrecting_settings_editing).getVisibility() == View.VISIBLE) {
            settingDiscard.performClick();
            switchSettingsFrame(findViewById(R.id.photocorrecting_settings_scrollview));
            return true;
        }
        ImageButton rotationDiscard = (ImageButton) findViewById(R.id.photocorrecting_rotation_discard);
        if (findViewById(R.id.photocorrecting_rotation_editing).getVisibility() == View.VISIBLE) {
            rotationDiscard.performClick();
            switchSettingsFrame(findViewById(R.id.photocorrecting_settings_scrollview));
            return true;
        }
        return false;
    }

    private static View createTabView(final Context context, final String text) {
        View view = LayoutInflater.from(context).inflate(R.layout.photocorrecting_tab, null);
        TextView tv = (TextView) view.findViewById(R.id.photocorrecting_tab_text);
        tv.setText(text);
        return view;
    }

    private void onRealBackClick() {
        weakCache(PhotoCorrectionActivity.class).remove("currentState");
        //generalFilter.recycle();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("onDestroy");
    }
}
