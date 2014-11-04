package ru.example.PhotoStream.Activities;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import org.w3c.dom.Text;
import ru.example.PhotoStream.Camera.Filters.IncMultiFilter;
import ru.example.PhotoStream.Camera.Filters.TunablePhotoFilterFactory;
import ru.example.PhotoStream.Camera.RawBitmap;
import ru.example.PhotoStream.R;

import java.util.ArrayList;
import java.util.List;

public final class PhotoCorrectionActivity extends ActionBarActivity
        implements IncMultiFilter.OnImageChangingListener, IncMultiFilter.OnImageChangedListener {

    private static Bitmap image = null;
    private static boolean moveBack = false;

    public static void setMoveBack(boolean b) {
        moveBack = b;
    }

    public static void setBitmap(Bitmap bitmap) {
        image = bitmap;
    }

    private ImageView imageView;
    private TabHost tabHost;
    private FrameLayout filtersFrame, settingsFrame;

    private List<View> filterBadges = new ArrayList<>();

    private IncMultiFilter generalFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.photocorrectionactivity);
        imageView = (ImageView) findViewById(R.id.photocorrecting_image);
        generalFilter = new IncMultiFilter(this, image);
        generalFilter.setOnImageChangedListener(this);
        generalFilter.setOnImageChangingListener(this);
        generalFilter.getPhotoFilterHandler().setStrength(0);
        tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();
        TabHost.TabSpec tabSpec = tabHost.newTabSpec("filters");
        tabSpec.setIndicator(getString(R.string.ImageFilters));
        tabSpec.setContent(R.id.photocorrecting_filters_tab);
        tabHost.addTab(tabSpec);
        tabSpec = tabHost.newTabSpec("settings");
        tabSpec.setIndicator(getString(R.string.Settings));
        tabSpec.setContent(R.id.photocorrecting_settings_tab);
        tabHost.addTab(tabSpec);
        filtersFrame = (FrameLayout) findViewById(R.id.photocorrecting_filters_tab);
        switchFilterFrame(findViewById(R.id.photocorrecting_filters_scrollview));
        settingsFrame = (FrameLayout) findViewById(R.id.photocorrecting_settings_tab);
        switchSettingsFrame(findViewById(R.id.photocorrecting_settings_scrollview));
        LinearLayout layout = (LinearLayout) findViewById(R.id.photocorrecting_filters_layout);
        LayoutInflater inflater = getLayoutInflater();
        for (final TunablePhotoFilterFactory.FilterType filterType: TunablePhotoFilterFactory.FilterType.values()) {
            View view = inflater.inflate(R.layout.filterbadgeview, layout, false);
            ImageView badgeImage = (ImageView) view.findViewById(R.id.filterbadgeview_image);
            badgeImage.setImageResource(filterType.getIconResource());
            TextView badgeTitle = (TextView) view.findViewById(R.id.filterbadgeview_label);
            badgeTitle.setText(filterType.toString(this));
            filterBadges.add(view);
            layout.addView(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onFilterClick(v, filterType);
                }
            });
        }
        layout = (LinearLayout) findViewById(R.id.photocorrecting_settings_layout);
        View rotationBadge = inflater.inflate(R.layout.filterbadgeview, layout, false);
        ImageView rotationBadgeView = (ImageView) rotationBadge.findViewById(R.id.filterbadgeview_image);
        rotationBadgeView.setImageResource(R.drawable._0009_f_rotates);
        TextView rotationLabel = (TextView) rotationBadge.findViewById(R.id.filterbadgeview_label);
        rotationLabel.setText(getString(R.string.Rotation));
        layout.addView(rotationBadge);
        rotationBadge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRotationClick(v);
            }
        });
        for (final TunablePhotoFilterFactory.SettingsFilterType filterType: TunablePhotoFilterFactory.SettingsFilterType.values()) {
            View view = inflater.inflate(R.layout.filterbadgeview, layout, false);
            ImageView badgeImage = (ImageView) view.findViewById(R.id.filterbadgeview_image);
            badgeImage.setImageResource(filterType.getIconResource());
            TextView badgeTitle = (TextView) view.findViewById(R.id.filterbadgeview_label);
            badgeTitle.setText(filterType.toString(this));
            layout.addView(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSettingsClick(v, filterType);
                }
            });
        }
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

    private synchronized void onFilterClick(View v, TunablePhotoFilterFactory.FilterType filterType) {
        if (!v.isSelected()) {
            for (View view: filterBadges) {
                view.setSelected(false);
            }
            v.setSelected(true);
            generalFilter.setPhotoFilter(filterType);
            generalFilter.getPhotoFilterHandler().setStrength(0.5);
        } else {
            TextView filterLabel = (TextView) findViewById(R.id.photocorrecting_filters_label);
            filterLabel.setText(filterType.toString(this));
            final IncMultiFilter.FilterHandler filterHandler = generalFilter.getPhotoFilterHandler();
            SeekBar filterBar = (SeekBar) findViewById(R.id.photocorrecting_filters_seekbar);
            filterBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) filterHandler.setStrength(progress * 0.01);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            filterBar.setProgress((int)(filterHandler.getStrength() * 100));
            ImageButton applyButton = (ImageButton) findViewById(R.id.photocorrecting_filters_apply);
            applyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    filterHandler.applyChanges();
                    switchFilterFrame(findViewById(R.id.photocorrecting_filters_scrollview));
                }
            });
            ImageButton discardButton = (ImageButton) findViewById(R.id.photocorrecting_filters_discard);
            discardButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    filterHandler.discardChanges();
                    switchFilterFrame(findViewById(R.id.photocorrecting_filters_scrollview));
                }
            });
            switchFilterFrame(findViewById(R.id.photocorrecting_filters_editing));
        }
    }

    private synchronized void onSettingsClick(View v, TunablePhotoFilterFactory.SettingsFilterType filterType) {
        final IncMultiFilter.FilterHandler filterHandler = generalFilter.getSettingsFilterHandler(filterType);
        TextView filterLabel = (TextView) findViewById(R.id.photocorrecting_settings_label);
        filterLabel.setText(filterType.toString(this));
        SeekBar filterBar = (SeekBar) findViewById(R.id.photocorrecting_settings_seekbar);
        final double minStrength = filterType.getMinStrength();
        final double maxStrength = filterType.getMaxStrength();
        filterBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) filterHandler.setStrength(minStrength + progress * (maxStrength - minStrength) / 100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        filterBar.setProgress((int)((filterHandler.getStrength() - minStrength) * 100 / (maxStrength - minStrength)));
        ImageButton applyButton = (ImageButton) findViewById(R.id.photocorrecting_settings_apply);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterHandler.applyChanges();
                switchSettingsFrame(findViewById(R.id.photocorrecting_settings_scrollview));
            }
        });
        ImageButton discardButton = (ImageButton) findViewById(R.id.photocorrecting_settings_discard);
        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterHandler.discardChanges();
                switchSettingsFrame(findViewById(R.id.photocorrecting_settings_scrollview));
            }
        });
        switchSettingsFrame(findViewById(R.id.photocorrecting_settings_editing));
    }

    private void onRotationClick(View v) {
        final IncMultiFilter.RotationHandler rotationHandler = generalFilter.getRotationHandler();
        ImageButton rotateLeft = (ImageButton) findViewById(R.id.photocorrecting_rotation_left);
        rotateLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotationHandler.changeOrientation(IncMultiFilter.OrientationChange.RotateCounterClockWise);
            }
        });
        ImageButton rotateRight = (ImageButton) findViewById(R.id.photocorrecting_rotation_right);
        rotateRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotationHandler.changeOrientation(IncMultiFilter.OrientationChange.RotateClockwise);
            }
        });
        ImageButton flipVertical = (ImageButton) findViewById(R.id.photocorrecting_flip_vertical);
        flipVertical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotationHandler.changeOrientation(IncMultiFilter.OrientationChange.MirrorVertically);
            }
        });
        ImageButton flipHorizontal = (ImageButton) findViewById(R.id.photocorrecting_flip_horizontal);
        flipHorizontal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotationHandler.changeOrientation(IncMultiFilter.OrientationChange.MirrorHorizontally);
            }
        });
        ImageButton applyChanges = (ImageButton) findViewById(R.id.photocorrecting_rotation_apply);
        applyChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotationHandler.apply();
                switchSettingsFrame(findViewById(R.id.photocorrecting_settings_scrollview));
            }
        });
        ImageButton discardChanges = (ImageButton) findViewById(R.id.photocorrecting_rotation_discard);
        discardChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotationHandler.discardChanges();
                switchSettingsFrame(findViewById(R.id.photocorrecting_settings_scrollview));
            }
        });
        switchSettingsFrame(findViewById(R.id.photocorrecting_rotation_editing));
    }


    @Override
    public void onResume() {
        super.onResume();
        if (moveBack) {
            moveBack = false;
            onBackPressed();
        }
    }

    @Override
    public void onImageChanged(RawBitmap rawBitmap, final Bitmap toFill) {
        rawBitmap.fillBitmap(toFill);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setImageBitmap(toFill);
            }
        });
    }

    @Override
    public void onImageChanging() {

    }
}
