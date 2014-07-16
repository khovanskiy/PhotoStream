package ru.example.PhotoStream;

import android.annotation.SuppressLint;
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
    private static ResourceLruCache<String, BitmapPointer> cache = new ResourceLruCache<String, BitmapPointer>() {
        @Override
        protected void entryRemoved(boolean evicted, String key, BitmapPointer oldValue, BitmapPointer newValue) {
            super.entryRemoved(evicted, key, oldValue, newValue);
            Console.print("Resource [" + oldValue.getCount() + "] " + key + " has been removed from cache");
        }
    };

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
                    options.inTempStorage = new byte[16 * 1024];
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
            // А не успели ли уже загрузить наше фото до нас?
            BitmapPointer pointer = cache.get(path);
            if (pointer != null && pointer.get() != null) {
                // Успели =>
                //bitmap.recycle(); // Наше уже не нужно
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
            calcAvailableMemory();

            updateImageBitmap(pointer);
        }
    }

    public interface OnSmartViewLoadedListener {
        void onSmartViewUpdated();
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

        public int getCount() {
            return this.count;
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
    protected String path = "";
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

    private void updateImageBitmap(BitmapPointer pointer) {
        Bitmap bitmap = pointer.get();
        if (bitmap == null) {
            return;
        }

        if (currentPointer != null) {
            currentPointer.rel();
        }
        currentPointer = pointer;
        currentPointer.acc();

        setImageBitmap(bitmap);
        setVisibility(VISIBLE);
        if (loadedListener != null) {
            loadedListener.onSmartViewUpdated();
        }
    }

    /*private void setupBitmap(BitmapPointer bitmap) {
        assert (bitmap != null & bitmap.get() != null);

        if (currentPointer != null) {
            currentPointer.rel();
        }
        currentPointer = bitmap;
        currentPointer.acc();

        if (loadedListener != null) {
            loadedListener.onSmartViewUpdated();
        }

        this.setVisibility(VISIBLE);
        this.setImageBitmap(bitmap.get());
    }  */

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

    @SuppressLint("Assert")
    public synchronized void loadFromURL(String url) {
        if (!(url != null)) throw new AssertionError();
        this.setVisibility(INVISIBLE);
        this.path = url;

        BitmapPointer bitmap = cache.get(url);
        if (bitmap != null && bitmap.get() != null) {
            updateImageBitmap(bitmap);
        } else {
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
