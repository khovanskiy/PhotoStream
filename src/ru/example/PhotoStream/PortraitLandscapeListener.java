package ru.example.PhotoStream;

import android.app.Activity;
import android.content.res.Configuration;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.WindowManager;

public abstract class PortraitLandscapeListener extends OrientationEventListener {
    private final int deviceDefaultOrientation;

    public static final int ORIENTATION_PORTRAIT_NORMAL = 1;
    public static final int ORIENTATION_PORTRAIT_INVERTED = 2;
    public static final int ORIENTATION_LANDSCAPE_NORMAL = 3;
    public static final int ORIENTATION_LANDSCAPE_INVERTED = 4;

    private int mPreviousOrientation = ORIENTATION_UNKNOWN;

    public PortraitLandscapeListener(Activity activity) {
        super(activity);
        deviceDefaultOrientation = getDeviceDefaultOrientation(activity);
    }

    private static int getDeviceDefaultOrientation(Activity activity) {

        WindowManager windowManager = (WindowManager) activity.getSystemService(activity.WINDOW_SERVICE);

        Configuration config = activity.getResources().getConfiguration();

        int rotation = windowManager.getDefaultDisplay().getRotation();

        if (((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) &&
                config.orientation == Configuration.ORIENTATION_LANDSCAPE)
                || ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) &&
                config.orientation == Configuration.ORIENTATION_PORTRAIT)) {
            return Configuration.ORIENTATION_LANDSCAPE;
        } else {
            return Configuration.ORIENTATION_PORTRAIT;
        }
    }

    @Override
    public void onOrientationChanged(int orientation) {
        if (deviceDefaultOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            orientation = (orientation + 270) % 360;
        }
        int mCurrentOrientation;
        if (orientation >= 315 || orientation < 45) {
            mCurrentOrientation = ORIENTATION_PORTRAIT_NORMAL;
        } else if (orientation < 315 && orientation >= 225) {
            mCurrentOrientation = ORIENTATION_LANDSCAPE_NORMAL;
        } else if (orientation < 225 && orientation >= 135) {
            mCurrentOrientation = ORIENTATION_PORTRAIT_INVERTED;
        } else { // orientation <135 || orientation > 45
            mCurrentOrientation = ORIENTATION_LANDSCAPE_INVERTED;
        }
        /*if (mPreviousOrientation == -1 || (mPreviousOrientation % 2 != mCurrentOrientation % 2)) {
            //onOrientationChange((deviceDefaultOrientation == Configuration.ORIENTATION_LANDSCAPE)
                    ^ (mCurrentOrientation == ORIENTATION_LANDSCAPE_INVERTED
                        || mCurrentOrientation == ORIENTATION_LANDSCAPE_NORMAL));
        }*/
        if (mPreviousOrientation == ORIENTATION_UNKNOWN || mPreviousOrientation != mCurrentOrientation) {
            System.out.println("OrientationChanged " + orientation);
            onOrientationTypeChanged(mCurrentOrientation);
        }
        mPreviousOrientation = mCurrentOrientation;
    }

    public abstract void onOrientationTypeChanged(int orientation);
}
