package ru.example.PhotoStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import java.util.LinkedHashMap;

public class SmartImage extends ImageView implements IEventHadler{
    private static LinkedHashMap<String, Bitmap> cache = new LinkedHashMap<>();

    public SmartImage(Context context) {
        super(context);
    }

    public SmartImage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SmartImage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void setupBitmap(Bitmap bitmap) {
        if (bitmap == null)
        {
            return;
        }
        this.setImageBitmap(bitmap);
        final Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setInterpolator(new LinearInterpolator());
        fadeIn.setDuration(500);
        fadeIn.setRepeatCount(0);
        this.startAnimation(fadeIn);
    }

    public void loadFromURL(String url) {
        //this.setVisibility(INVISIBLE);
        if (cache.containsKey(url)) {
            setupBitmap(cache.get(url));
        }
        else {
            ImageLoader loader = new ImageLoader(url);
            loader.addEventListener(this);
            loader.execute();
        }
    }

    @Override
    public void handleEvent(Event e) {
        if (e.type == Event.COMPLETE) {
            e.target.removeEventListener(this);
            Bitmap bitmap = (Bitmap) e.data.get("bitmap");
            String path = (String) e.data.get("path");
            setupBitmap(bitmap);
            if (!cache.containsKey(path)) {
                cache.put(path, bitmap);
            }
        }
    }
}
