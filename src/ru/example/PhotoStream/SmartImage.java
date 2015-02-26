package ru.example.PhotoStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.util.LruCache;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class SmartImage extends ImageView {
    // Get max available VM memory, exceeding this amount will throw an
    // OutOfMemory exception. Stored in kilobytes as LruCache takes an
    // int in its constructor.
    private static final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

    // Use 1/8th of the available memory for this memory cache.
    private static final int cacheSize = maxMemory / 8;

    private static final LruCache<String, Bitmap> mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            // The cache size will be measured in kilobytes rather than
            // number of items.
            return bitmap.getByteCount() / 1024;
        }
    };

    private static class Operation implements Runnable {
        private Looper mLooper;
        private boolean mCancelled = false;
        private CompleteListener completeListener;
        private String url;

        public Operation(String url) {
            mLooper = Looper.myLooper();
            this.url = url;
        }

        @Override
        public void run() {
            try {
                InputStream inputStream = null;
                try {
                    inputStream = new BufferedInputStream(new URL(url).openStream());
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    if (mCancelled) {
                        return;
                    }
                    final Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                    if (!mCancelled) {
                        new Handler(mLooper).post(new Runnable() {
                            @Override
                            public void run() {
                                if (completeListener != null) {
                                    completeListener.onComplete(bitmap);
                                }
                            }
                        });
                    } else {
                        bitmap.recycle();
                    }
                } catch (final IOException e) {
                    if (!mCancelled) {
                        new Handler(mLooper).post(new Runnable() {
                            @Override
                            public void run() {
                                if (completeListener != null) {
                                    completeListener.onError(e);
                                }
                            }
                        });
                    }
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            } catch (final IOException e) {

            }
        }

        public void cancel() {
            mCancelled = true;
        }

        public void setCompleteListener(CompleteListener listener) {
            this.completeListener = listener;
        }

        public static abstract class CompleteListener {
            public void onComplete(Bitmap bitmap) {

            }

            public void onError(Exception error) {

            }
        }
    }

    private static final Executor mBackgroundExecutor = Executors.newFixedThreadPool(5);

    private SmartViewLoadedListener viewLoadedListener;
    private Bitmap mNextBitmap;
    private String mCurrentUrl = "";

    private Operation mCurrentOperation;

    private final Animation.AnimationListener fadeOutListener = new Animation.AnimationListener() {

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            setImageBitmap(mNextBitmap);
            mNextBitmap = null;
            Animation fadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fadein);
            startAnimation(fadeIn);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    public SmartImage(Context context) {
        super(context);
    }

    public SmartImage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SmartImage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setImageURL(final String url) {
        if (mCurrentUrl.equals(url)) {
            return;
        }
        setImageDrawable(getResources().getDrawable(R.drawable.transparent_d));
        mCurrentUrl = url;
        Bitmap cachedBitmap = mMemoryCache.get(url);
        if (cachedBitmap == null) {
            if (mCurrentOperation != null) {
                mCurrentOperation.cancel();
            }
            mCurrentOperation = new Operation(url);
            mCurrentOperation.setCompleteListener(new Operation.CompleteListener() {
                @Override
                public void onComplete(Bitmap bitmap) {
                    mMemoryCache.put(url, bitmap);
                    onBitmapTaken(bitmap);
                }

                @Override
                public void onError(Exception error) {
                    if (viewLoadedListener != null) {
                        viewLoadedListener.onError(error);
                    }
                }
            });
            mBackgroundExecutor.execute(mCurrentOperation);
        } else {
            onBitmapTaken(cachedBitmap);
        }
    }

    private void onBitmapTaken(Bitmap bitmap) {
        mNextBitmap = bitmap;
        Animation fadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.fadeout);
        fadeOut.setAnimationListener(fadeOutListener);
        startAnimation(fadeOut);
        if (viewLoadedListener != null) {
            viewLoadedListener.onUpdated();
        }
    }

    public void setOnSmartViewLoadedListener(SmartViewLoadedListener loadedListener) {
        this.viewLoadedListener = loadedListener;
    }

    public static abstract class SmartViewLoadedListener {
        public void onUpdated() {

        }

        public void onError(Exception error) {

        }
    }
}
