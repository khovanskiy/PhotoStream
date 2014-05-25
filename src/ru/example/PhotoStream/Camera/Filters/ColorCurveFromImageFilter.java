package ru.example.PhotoStream.Camera.Filters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import ru.example.PhotoStream.R;

/**
 * Created by Genyaz on 25.05.2014.
 */
public class ColorCurveFromImageFilter extends ColorCurveFilter {
    private Context context;
    private int resID;

    public ColorCurveFromImageFilter(Context context, int resID) {
        this.context = context;
        this.resID = resID;
    }

    @Override
    protected void fillColors(int[] red, int[] green, int[] blue) {
        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), resID);
        if (bm.getHeight() == 1) {
            int[] colors = new int[256];
            bm.getPixels(colors, 0, 256, 0, 0, 256, 1);
            for (int i = 0; i < 256; i++) {
                red[i] = Color.red(colors[i]);
                green[i] = Color.green(colors[i]);
                blue[i] = Color.blue(colors[i]);
            }
        }
    }
}
