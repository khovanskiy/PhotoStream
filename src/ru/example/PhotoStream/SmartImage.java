package ru.example.PhotoStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Debug;
import android.support.v4.util.LruCache;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class SmartImage extends ImageView {
    private static LruCache<String, Bitmap> cache;

    static {
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

    private class Loader extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Void... params) {
            Bitmap bitmap = null;
            InputStream inputStream = null;
            try {
                try {
                    inputStream = new URL(path).openStream();
                    bitmap = BitmapFactory.decodeStream(inputStream);

                } catch (Exception e) {
                    Console.print(e.getMessage());
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            } catch (Exception e) {

            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            setupBitmap(bitmap);
            anim(500);
            if (cache.get(path) == null) {
                cache.put(path, bitmap);
            }
            running.compareAndSet(true, false);
        }
    }

    private static final Executor executor = Executors.newFixedThreadPool(5);
    private Loader loader = null;
    protected AtomicBoolean running = new AtomicBoolean(false);
    protected String path;

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
        if (bitmap == null) {
            return;
        }
        this.setVisibility(VISIBLE);
        this.setImageBitmap(bitmap);
    }

    private void anim(int millisec) {
        final Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setInterpolator(new LinearInterpolator());
        fadeIn.setDuration(millisec);
        fadeIn.setRepeatCount(0);
        this.startAnimation(fadeIn);
    }

    public void loadFromURL(String url) {
        this.setVisibility(INVISIBLE);
        this.path = url;
        Bitmap bitmap = cache.get(url);
        if (bitmap != null) {
            setupBitmap(bitmap);
        } else {
            if (running.compareAndSet(false, true)) {
                loader = new Loader();
                loader.execute();
            } else {
                if (!loader.isCancelled()) {
                    loader.cancel(true);
                }
                loader = new Loader();
                loader.executeOnExecutor(executor);
            }
        }
    }

    private long calcAvailableMemory() {
        long value = Runtime.getRuntime().maxMemory();
        String type = "";
        if (android.os.Build.VERSION.SDK_INT >= 11) {
            value = (value / 1024) - (Runtime.getRuntime().totalMemory() / 1024);
            type = "JAVA";
        } else {
            value = (value / 1024) - (Debug.getNativeHeapAllocatedSize() / 1024);
            type = "NATIVE";
        }
        Log.i("CONSOLE", "calcAvailableMemory, size = " + value + ", type = " + type);
        return value;
    }
}
