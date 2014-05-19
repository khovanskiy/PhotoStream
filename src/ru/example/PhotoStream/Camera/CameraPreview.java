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
    private Context context;
    private Camera camera = null;
    private boolean holderReady = false;
    private SurfaceView surface;
    private SurfaceHolder holder;
    private ImageView realView;
    private int width = 0, height = 0;
    private PictureBitmapCallback pictureBitmapCallback = null;
    private List<PhotoFilter> photoFilters = new ArrayList<PhotoFilter>();

    private void init() {
        context = getContext();
        surface = new SurfaceView(context);
        holder = surface.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                //Do nothing
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
                if (!holderReady) {
                    holderReady = true;
                    startPreview();
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                try {
                    if (camera != null) {
                        camera.stopPreview();
                        camera.release();
                        camera = null;
                    }
                } catch (Exception e) {
                    Log.e("Camera", e.getMessage());
                }
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

    public synchronized void startPreview() {
        if (holderReady) {
            if (camera == null) {
                camera = Camera.open();
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
                } catch (IOException e) {
                    Log.v("Camera error:", e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    public synchronized void stopPreview() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    public synchronized void resetPreview() {
        holderReady = false;
    }

    public synchronized void setPictureBitmapCallback(PictureBitmapCallback callback) {
        this.pictureBitmapCallback = callback;
    }

    public synchronized void takePicture() {
        if (camera != null && holderReady) {
            camera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    RawBitmap rawBitmap = new RawBitmap(data);
                    for (PhotoFilter photoFilter: photoFilters) {
                        photoFilter.transformOpaque(rawBitmap);
                    }
                    if (pictureBitmapCallback != null) {
                        pictureBitmapCallback.onPictureTaken(rawBitmap.toBitmap());
                    }
                }
            });
        }
    }
}
