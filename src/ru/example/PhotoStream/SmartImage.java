package ru.example.PhotoStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Debug;
import android.util.AttributeSet;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class SmartImage extends ImageView {
    private static ResourceLruCache<String, BitmapPointer> cache = new ResourceLruCache<>();

    private class Loader extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Void... params) {
            Bitmap bitmap = null;
            InputStream inputStream = null;
            try {
                try {
                    inputStream = new BufferedInputStream(new URL(path).openStream());
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    options.inTempStorage = new byte[16*1024];
                    bitmap = BitmapFactory.decodeStream(inputStream, null, options);
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
            // Фото загружено
            if (running.compareAndSet(true, false)) {
                // А не успели ли уже загрузить наше фото до нас?
                BitmapPointer pointer = cache.get(path);
                if (pointer != null && pointer.get() != null) {
                    // Успели =>
                    bitmap.recycle(); // Наше уже не нужно
                } else {
                    // Не успели =>
                    // А не произошло ли у нас ошибки при нашей загрузке?
                    if (bitmap == null) {
                        calcAvailableMemory();
                        return;
                    }
                    pointer = new BitmapPointer(bitmap); // Новый указатель
                    cache.put(path, pointer); // Поместим новый указатель в кеш
                }
                setupBitmap(pointer);
            }
            calcAvailableMemory();
        }
    }

    public interface OnSmartViewLoadedListener {
         void onSmartViewLoaded();
    }

    public class BitmapPointer implements IResource {
        private WeakReference<Bitmap> reference;
        private int count = 0;
        private int size;

        public BitmapPointer(Bitmap bitmap) {
            this.reference = new WeakReference<>(bitmap);
            this.size = bitmap.getRowBytes() * bitmap.getHeight() / 1024;
        }

        public void acc() {
            this.count++;
        }

        public void rel() {
            this.count--;
            if (this.count == 0) {
                Console.print("Bitmap recycle");
                Bitmap bitmap = reference.get();
                if (bitmap != null) {
                    bitmap.recycle();
                }
            }
        }

        public Bitmap get() {
            Bitmap bitmap = reference.get();
            if (bitmap == null) {
                return null;
            }
            return bitmap.isRecycled() ? null : bitmap;
        }

        @Override
        public int size() {
            return this.size;
        }
    }

    private static final Executor executor = Executors.newFixedThreadPool(5);
    private Loader loader = null;
    protected AtomicBoolean running = new AtomicBoolean(false);
    protected String path;
    protected BitmapPointer currentPointer = null; // Текущее изображение

    public SmartImage(Context context) {
        super(context);
    }

    public SmartImage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SmartImage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void setupBitmap(BitmapPointer bitmap) {
        assert (bitmap != null & bitmap.get() != null);

        if (currentPointer != null) {
            currentPointer.rel();
        }
        currentPointer = bitmap;
        currentPointer.acc();

        if (loadedListener != null) {
            loadedListener.onSmartViewLoaded();
        }

        this.setVisibility(VISIBLE);
        this.setImageBitmap(bitmap.get());
    }

    private void anim(int millisec) {
        final Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setInterpolator(new LinearInterpolator());
        fadeIn.setDuration(millisec);
        fadeIn.setRepeatCount(0);
        this.startAnimation(fadeIn);
    }

    /**
     * Loads image from requested url or retrieves it from the cache and then displays it.
     *
     * @param url
     */

    public void loadFromURL(String url) {
        assert (url != null && !url.equals(""));

        this.setVisibility(INVISIBLE);
        this.path = url;

        BitmapPointer bitmap = cache.get(url);
        if (bitmap != null && bitmap.get() != null) {
            setupBitmap(bitmap);
        } else {
            if (!running.compareAndSet(false, true)) {
                if (!loader.isCancelled()) {
                    loader.cancel(true);
                }
            }
            loader = new Loader();
            loader.executeOnExecutor(executor);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    private OnSmartViewLoadedListener loadedListener;

    public void setOnSmartViewLoadedListener(OnSmartViewLoadedListener loadedListener) {
        this.loadedListener = loadedListener;
    }

    private long calcAvailableMemory() {
        long value = Runtime.getRuntime().maxMemory();
        String type;
        if (android.os.Build.VERSION.SDK_INT >= 11) {
            value = (value / 1024) - (Runtime.getRuntime().totalMemory() / 1024);
            type = "JAVA";
        } else {
            value = (value / 1024) - (Debug.getNativeHeapAllocatedSize() / 1024);
            type = "NATIVE";
        }
        Console.print("calcAvailableMemory, size = " + value + ", type = " + type);
        return value;
    }
}
