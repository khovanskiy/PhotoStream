package ru.example.PhotoStream;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.WindowManager;

/**
 * Created by Genyaz on 07.11.2014.
 */
public abstract class PortraitLandscapeListener extends OrientationEventListener {
    private final int deviceDefaultOrientation;

    private static final int ORIENTATION_PORTRAIT_NORMAL =  1;
    private static final int ORIENTATION_PORTRAIT_INVERTED =  2;
    private static final int ORIENTATION_LANDSCAPE_NORMAL =  3;
    private static final int ORIENTATION_LANDSCAPE_INVERTED =  4;

    private int lastOrientation = -1;

    public PortraitLandscapeListener(Activity activity) {
        super(activity);
        deviceDefaultOrientation = getDeviceDefaultOrientation(activity);
    }

    private static int getDeviceDefaultOrientation(Activity activity) {

        WindowManager windowManager =  (WindowManager) activity.getSystemService(activity.WINDOW_SERVICE);

        Configuration config = activity.getResources().getConfiguration();

        int rotation = windowManager.getDefaultDisplay().getRotation();

        if ( ((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) &&
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
        int currentOrientation;
        if (orientation >= 315 || orientation < 45) {
            currentOrientation = ORIENTATION_PORTRAIT_NORMAL;
        }
        else if (orientation < 315 && orientation >= 225) {
            currentOrientation = ORIENTATION_LANDSCAPE_NORMAL;
        }
        else if (orientation < 225 && orientation >= 135) {
            currentOrientation = ORIENTATION_PORTRAIT_INVERTED;
        }
        else { // orientation <135 || orientation > 45
            currentOrientation = ORIENTATION_LANDSCAPE_INVERTED;
        }
        if (lastOrientation == -1 || (lastOrientation % 2 != currentOrientation % 2)) {
            onOrientationChange((deviceDefaultOrientation == Configuration.ORIENTATION_LANDSCAPE)
                    ^ (currentOrientation == ORIENTATION_LANDSCAPE_INVERTED
                        || currentOrientation == ORIENTATION_LANDSCAPE_NORMAL));
        }
        lastOrientation = currentOrientation;
    }

    public abstract void onOrientationChange(boolean landscape);
}
