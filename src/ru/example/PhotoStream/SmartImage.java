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
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SmartImage extends ImageView {
    private static ResourceLruCache<String, BitmapPointer> cache = new ResourceLruCache<String, BitmapPointer>() {
        @Override
        protected void entryRemoved(boolean evicted, String key, BitmapPointer oldValue, BitmapPointer newValue) {
            super.entryRemoved(evicted, key, oldValue, newValue);
            Console.print("Resource [" + oldValue.getCount() + "] " + key + " has been removed from cache");
        }
    };

    private class Loader extends AsyncTask<Void, Void, Bitmap> {

        private String path;

        public Loader(String path) {
            this.path = path;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            Bitmap bitmap = null;
            InputStream inputStream = null;
            try {
                try {
                    inputStream = new BufferedInputStream(new URL(this.path).openStream());
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
            BitmapPointer pointer = cache.get(this.path);
            if (pointer != null) {
                Console.print("Equals bitmaps: " + (pointer.get() == bitmap));
            }
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
                pointer = new BitmapPointer(this.path, bitmap); // Новый указатель
                cache.put(path, pointer); // Поместим новый указатель в кеш
            }
            calcAvailableMemory();
            // Актуальны ли это загрузчик?
            if (loader != this) {
                // Не актуальный => можно ничего не обновлять
                return;
            }
            updateImageBitmap(pointer);
            anim(500);
        }
    }

    public interface OnSmartViewLoadedListener {
        void onSmartViewUpdated();
    }

    public class BitmapPointer implements IResource {
        private Bitmap reference;
        private String path;
        private int count = 0;
        private int size;

        public BitmapPointer(String path, Bitmap bitmap) {
            this.reference = bitmap;
            this.path = path;
            this.size = bitmap.getRowBytes() * bitmap.getHeight() / 1024;
        }

        public void acc() {
            this.count++;
        }

        public void rel() {
            this.count--;
        }

        public void recycle() {
            reference.recycle();
        }

        public int getCount() {
            return this.count;
        }

        public String getPath() {
            return this.path;
        }

        public Bitmap get() {
            Bitmap bitmap = reference;
            if (bitmap == null) {
                return null;
            }
            return bitmap.isRecycled() ? null : bitmap;
        }

        @Override
        public int size() {
            return this.size;
        }

        @Override
        public String toString() {
            String temp = "Bitmap[";
            Bitmap bitmap = reference;
            if (bitmap == null) {
                temp += "null";
            } else if (bitmap.isRecycled()) {
                temp += "recycled";
            } else {
                temp += bitmap;
            }
            temp += " " + count + " " + path + "]";
            return temp;
        }
    }

    private static final Executor executor = Executors.newFixedThreadPool(5);
    private Loader loader = null;
    protected String currentPath = "";
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
            if (currentPointer.getCount() == 0 && cache.get(currentPointer.getPath()) == null) {
                currentPointer.recycle();
            }
        }
        currentPointer = pointer;
        currentPointer.acc();

        setImageBitmap(bitmap);
        setVisibility(VISIBLE);
        if (loadedListener != null) {
            loadedListener.onSmartViewUpdated();
        }
    }

    public void debug() {
        Console.print("Image info");
        Console.print(loader);
        Console.print(currentPath);
        Console.print("Current: " + currentPointer);
        BitmapPointer pointer = cache.get(currentPath);
        if (pointer == null) {
            Console.print("Pointer is not in the cache");
        } else {
            Console.print("Pointer: " + pointer);
        }
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
    public synchronized void loadFromURL(String url) {
        assert (url != null);
        if (currentPath.equals(url)) {
            return;
        }
        this.setVisibility(INVISIBLE);
        this.currentPath = url;

        BitmapPointer bitmap = cache.get(url);
        if (bitmap != null && bitmap.get() != null) {
            updateImageBitmap(bitmap);
        } else {
            loader = new Loader(this.currentPath);
            loader.executeOnExecutor(executor);
        }
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
