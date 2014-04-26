package ru.example.PhotoStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Debug;
import android.support.v4.util.LruCache;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import java.util.LinkedHashMap;

public class SmartImage extends ImageView implements IEventHadler{
    private static LruCache<String, Bitmap> cache;
    static
    {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        cache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    private ImageLoader loader = null;

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
        this.setVisibility(VISIBLE);
        this.setImageBitmap(bitmap);
        final Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setInterpolator(new LinearInterpolator());
        fadeIn.setDuration(500);
        fadeIn.setRepeatCount(0);
        this.startAnimation(fadeIn);
    }

    public void loadFromURL(String url) {
        this.setVisibility(INVISIBLE);
        if (loader != null)
        {
            if (!loader.isCancelled())
            {
                loader.cancel(true);
            }
        }
        Bitmap bitmap = cache.get(url);
        if (bitmap != null) {
            setupBitmap(bitmap);
        }
        else {
            loader = new ImageLoader(url);
            loader.addEventListener(this);
            loader.execute();
        }
    }

    private long calcAvailableMemory()
    {
        long value = Runtime.getRuntime().maxMemory();
        String type = "";
        if (android.os.Build.VERSION.SDK_INT >= 11)
        {
            value = (value / 1024) - (Runtime.getRuntime().totalMemory() / 1024);
            type = "JAVA";
        }
        else
        {
            value = (value / 1024) - (Debug.getNativeHeapAllocatedSize() / 1024);
            type = "NATIVE";
        }
        Log.i("CONSOLE", "calcAvailableMemory, size = " + value + ", type = " + type);
        return value;
    }

    @Override
    public void handleEvent(Event e) {
        if (e.type == Event.COMPLETE) {
            e.target.removeEventListener(this);
            Bitmap bitmap = (Bitmap) e.data.get("bitmap");
            String path = (String) e.data.get("path");
            if (bitmap != null)
            {
                setupBitmap(bitmap);
                if (cache.get(path) == null) {
                    cache.put(path, bitmap);
                    //Console.print("Total: " + totalBytes + "B / " + (totalBytes / 1024) + "KB");
                    //calcAvailableMemory();
                }
            }
        }
    }
}
