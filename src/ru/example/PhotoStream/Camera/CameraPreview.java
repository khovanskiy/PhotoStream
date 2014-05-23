package ru.example.PhotoStream.Camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import ru.example.PhotoStream.Camera.Filters.PhotoFilter;
import ru.example.PhotoStream.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CameraPreview extends FrameLayout {

    private class HiddenSurface extends SurfaceView
    {
        public HiddenSurface(Context context) {
            super(context);
        }

        public HiddenSurface(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public HiddenSurface(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            this.setMeasuredDimension(2, 2);
        }
    }

    private Camera camera = null;
    private boolean holderReady = false, toPreview = false, toTakePicture = false, previewing = false;
    private SurfaceHolder holder;
    private ImageView realView;
    private RawBitmap rawBitmap;
    private Bitmap bitmap;
    private int width = 0, height = 0;
    private PictureBitmapCallback pictureBitmapCallback = null;
    private List<PhotoFilter> photoFilters = new ArrayList<>();

    private synchronized void init() {
        Context context = getContext();
        SurfaceView surface = new HiddenSurface(context);
        holder = surface.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
                holderReady = true;
                if (toPreview) {
                    realStart();
                    if (toTakePicture) {
                        realTakePicture();
                    }
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                stopPreview();
                resetPreview();
            }
        });
        realView = new ImageView(context);
        addView(surface);
        addView(realView);
    }

    public CameraPreview(Context context) {
        super(context);
        init();
    }

    public CameraPreview(Context context, AttributeSet attr) {
        super(context, attr);
        init();
    }

    public CameraPreview(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        init();
    }

    /**
     * Set {@link ru.example.PhotoStream.Camera.Filters.PhotoFilter}s for camera preview and picture taking.
     *
     * @param photoFilters photo filters to apply
     */
    public synchronized void setPhotoFilters(List<PhotoFilter> photoFilters) {
        this.photoFilters = photoFilters;
    }

    private synchronized void realStart() {
        try {
            camera = Camera.open();
        }
        catch (Exception e) {
            Toast.makeText(getContext(), getContext().getString(R.string.CameraIsNotAvailable), Toast.LENGTH_SHORT).show();
        }
        if (camera != null) {
            Camera.Size size = camera.getParameters().getPreviewSize();
            if (size != null) {
                this.width = size.width;
                this.height = size.height;
            }
            camera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    if (rawBitmap == null) {
                        rawBitmap = new RawBitmap(data, width, height);
                    } else {
                        rawBitmap.fillFrom(data, width, height);
                    }
                    for (PhotoFilter photoFilter : photoFilters) {
                        photoFilter.transformOpaqueRaw(rawBitmap);
                    }
                    if (bitmap == null) {
                        bitmap = rawBitmap.toBitmap();
                        realView.setImageBitmap(bitmap);
                    } else {
                        rawBitmap.fillBitmap(bitmap);
                        realView.setImageBitmap(bitmap);
                    }
                }
            });
            try {
                camera.setPreviewDisplay(holder);
                holder.setFixedSize(width, height);
                camera.startPreview();
                previewing = true;
                toPreview = false;
            } catch (IOException e) {
                Log.v("Camera error:", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Sends a signal to start preview.
     * This isn't an instant action according to {@link android.hardware.Camera} contracts.
     */
    public synchronized void startPreview() {
        if (holderReady) {
            realStart();
        } else {
            toPreview = true;
        }
    }

    private synchronized void realStop() {
        previewing = false;
        toPreview = false;
        camera.stopPreview();
        camera.setPreviewCallback(null);
        camera.release();
        camera = null;
    }

    /**
     * Sends a signal to stop preview.
     */
    public synchronized void stopPreview() {
        if (previewing) {
            realStop();
        } else {
            toPreview = false;
        }
    }

    private synchronized void resetPreview() {
        holderReady = false;
        previewing = false;
        toPreview = false;
        toTakePicture = false;
        bitmap = null;
        rawBitmap = null;
    }

    /**
     * Determines the action invoked after receiving and transforming photo.
     *
     * @param callback callback to invoke.
     */
    public synchronized void setPictureBitmapCallback(PictureBitmapCallback callback) {
        this.pictureBitmapCallback = callback;
    }

    private synchronized void realTakePicture() {
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
                bitmapFactoryOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
                bitmapFactoryOptions.inMutable = true;

                bitmapFactoryOptions.inTempStorage = new byte[16 * 1024];
                Camera.Parameters parameters = camera.getParameters();
                Camera.Size size = parameters.getPictureSize();

                int height = size.height;
                int width = size.width;
                float mb = (width * height) / 1024000f;

                if (mb > 4f) {
                    bitmapFactoryOptions.inSampleSize = 4;
                } else if (mb > 3f) {
                    bitmapFactoryOptions.inSampleSize = 2;
                }

                bitmapFactoryOptions.inMutable = true;
                Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length, bitmapFactoryOptions);
                RawBitmap rb = new RawBitmap(image);
                for (PhotoFilter photoFilter : photoFilters) {
                    photoFilter.transformOpaqueRaw(rb);
                }
                rb.fillBitmap(image);
                if (pictureBitmapCallback != null) {
                    pictureBitmapCallback.onPictureTaken(image);
                    pictureBitmapCallback = null;
                }
            }
        });
        toTakePicture = false;
    }

    /**
     * Sends a signal to take picture.
     * Stops previewing after receiving photo according to {@link android.hardware.Camera} contracts.
     */
    public synchronized void takePicture() {
        if (previewing) {
            realTakePicture();
        } else {
            toTakePicture = true;
        }
    }
}
