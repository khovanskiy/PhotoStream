package ru.example.PhotoStream.Camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import ru.example.PhotoStream.Camera.Filters.PhotoFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Genyaz on 14.05.2014.
 */
public class CameraPreview extends FrameLayout {
    private Camera camera = null;
    private boolean holderReady = false, toPreview = false, toTakePicture = false, previewing = false;
    private SurfaceHolder holder;
    private ImageView realView;
    private int width = 0, height = 0;
    private PictureBitmapCallback pictureBitmapCallback = null;
    private List<PhotoFilter> photoFilters = new ArrayList<PhotoFilter>();

    private synchronized void init() {
        Context context = getContext();
        SurfaceView surface = new SurfaceView(context);
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

    public synchronized void setPhotoFilters(List<PhotoFilter> photoFilters) {
        this.photoFilters = photoFilters;
    }

    private synchronized void realStart() {
        camera = Camera.open();
        if (camera != null) {
            Camera.Size size = camera.getParameters().getPreviewSize();
            if (size != null) {
                this.width = size.width;
                this.height = size.height;
            }
            camera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    RawBitmap rawBitmap = new RawBitmap(data, width, height);
                    for (PhotoFilter photoFilter : photoFilters) {
                        photoFilter.transformOpaque(rawBitmap);
                    }
                    realView.setImageBitmap(rawBitmap.toBitmap());
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
    }

    public synchronized void setPictureBitmapCallback(PictureBitmapCallback callback) {
        this.pictureBitmapCallback = callback;
    }

    private synchronized void realTakePicture() {
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                RawBitmap rawBitmap = new RawBitmap(data);
                for (PhotoFilter photoFilter: photoFilters) {
                    photoFilter.transformOpaque(rawBitmap);
                }
                if (pictureBitmapCallback != null) {
                    pictureBitmapCallback.onPictureTaken(rawBitmap.toBitmap());
                    pictureBitmapCallback = null;
                }
            }
        });
        toTakePicture = false;
    }

    public synchronized void takePicture() {
        if (previewing) {
            realTakePicture();
        } else {
            toTakePicture = true;
        }
    }
}
