package ru.example.PhotoStream.Camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import ru.example.PhotoStream.Camera.Filters.PhotoFilter;
import ru.example.PhotoStream.Camera.Filters.SpecialFilter;
import ru.example.PhotoStream.Console;
import ru.example.PhotoStream.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CameraPreview extends FrameLayout {

    private class HiddenSurface extends SurfaceView {
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
    private boolean holderReady = false;
    private boolean toPreview = false, toTakePicture = false, previewing = false;
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
                if (!holderReady) {
                    holderReady = true;
                    if (toPreview) {
                        realStart();
                        if (toTakePicture) {
                            realTakePicture();
                        }
                    }
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                holderReady = false;
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

    private Camera.Size getBestPreviewSize(Camera.Parameters parameters){
        Camera.Size bestSize = null;
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();

        bestSize = sizeList.get(0);

        for(int i = 1; i < sizeList.size(); i++){
            if((sizeList.get(i).width * sizeList.get(i).height) >
                    (bestSize.width * bestSize.height)){
                bestSize = sizeList.get(i);
            }
        }

        return bestSize;
    }

    private synchronized void realStart() {
        try {
            camera = Camera.open();
        } catch (Exception e) {
            Toast.makeText(getContext(), getContext().getString(R.string.cameraIsNotAvailable), Toast.LENGTH_SHORT).show();
        }
        if (camera != null) {
            List<Integer> formats = camera.getParameters().getSupportedPreviewFormats();
            for (int i = 0; i < formats.size(); ++i) {
                Console.print(formats.get(i));
            }
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = getBestPreviewSize(parameters);
            parameters.setPreviewSize(size.width / 3, size.height / 3);
            parameters.setPictureSize(size.width, size.height);
            camera.setParameters(parameters);

            if (size != null) {
                Console.print("Current prevew size: " + size.width + " " + size.height);
                this.width = size.width / 3;
                this.height = size.height / 3;
            }
            camera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    if (rawBitmap == null) {
                        rawBitmap = new RawBitmap(data, width, height);
                    } else {
                        rawBitmap.fillFrom(data, width, height);
                    }
                    realView.setColorFilter(null);
                    for (PhotoFilter photoFilter : photoFilters) {
                        if (photoFilter.hasPreviewModification()) {
                            photoFilter.modifyPreview(realView);
                        } else {
                            photoFilter.transformOpaqueRaw(rawBitmap);
                        }
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
        } else {
            Toast.makeText(getContext(), getContext().getString(R.string.cameraIsNotAvailable), Toast.LENGTH_SHORT).show();
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

    public synchronized void resetPreview() {
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
                Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length, bitmapFactoryOptions);
                RawBitmap rb = new RawBitmap(image);
                for (PhotoFilter photoFilter : photoFilters) {
                    photoFilter.transformOpaqueRaw(rb);
                }
                rb.fillBitmap(image);
                SpecialFilter.unfreeze();
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
        SpecialFilter.freeze();
        if (previewing) {
            realTakePicture();
        } else {
            toTakePicture = true;
        }
    }
}
