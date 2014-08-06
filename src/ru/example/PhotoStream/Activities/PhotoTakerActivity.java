package ru.example.PhotoStream.Activities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.ImageButton;
import android.widget.Toast;
import ru.example.PhotoStream.Console;
import ru.example.PhotoStream.R;

import java.io.FileNotFoundException;

public final class PhotoTakerActivity extends Activity implements SurfaceHolder.Callback, View.OnClickListener, Camera.AutoFocusCallback {
    private static final int NO_CAMERA = -1;
    private static final int SELECT_PICTURE = 1;
    private static final int MEMORY_SCALE_DOWN = 4;
    private static final int PIXEL_TOTAL_OVERHEAD_IN_BYTES = 22;
    private static final int MAX_WIDTH = 1024;
    private static final int MAX_HEIGHT = 1024;
    private Camera camera = null;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private int currentCameraId;
    private Context context;
    private boolean isFrontCamera = false;

    private void stopCamera() {
        if (camera != null) {
            Console.print("Release camera");
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private void startCamera(int cameraId) {
        try {
            Console.print("Open camera");
            camera = Camera.open(cameraId);
            camera.setPreviewDisplay(surfaceHolder);
            Camera.Size size = camera.getParameters().getPreviewSize();
            float aspectRatio = (float) size.width / size.height;
            int newWidth;
            int newHeight;
            if (surfaceView.getWidth() / surfaceView.getHeight() < aspectRatio) {
                newWidth = Math.round(surfaceView.getHeight() * aspectRatio);
                newHeight = surfaceView.getHeight();
            } else {
                newWidth = surfaceView.getWidth();
                newHeight = Math.round(surfaceView.getWidth() / aspectRatio);
            }
            ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();
            layoutParams.width = newWidth;
            layoutParams.height = newHeight;
            surfaceView.setLayoutParams(layoutParams);
            camera.startPreview();
            currentCameraId = cameraId;
        } catch (Exception e) {
            Toast.makeText(context, "Failed to open camera", Toast.LENGTH_SHORT).show();
            Log.i("M_CONSOLE", e.getMessage(), e);
        }
    }

    private void setCamera(int cameraFacing) {
        stopCamera();
        int cameraNumber = Camera.getNumberOfCameras();
        currentCameraId = NO_CAMERA;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int cameraId = 0; cameraId < cameraNumber; cameraId++) {
            Camera.getCameraInfo(cameraId, cameraInfo);
            if (cameraInfo.facing == cameraFacing) {
                startCamera(cameraId);
                if (currentCameraId == cameraId) return;
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.phototakeractivity);
        context = this;

        ImageButton galleryButton = (ImageButton) findViewById(R.id.phototakeractivity_gallerybutton);
        galleryButton.setOnClickListener(this);
        ImageButton takePhotoButton = (ImageButton) findViewById(R.id.phototakeractivity_takephotobutton);
        takePhotoButton.setOnClickListener(this);
        ImageButton cameraToggle = (ImageButton) findViewById(R.id.phototakeractivity_cameratogglebutton);
        cameraToggle.setOnClickListener(this);

        currentCameraId = NO_CAMERA;
        surfaceView = (SurfaceView) findViewById(R.id.phototakeractivity_surfaceview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                Toast.makeText(context, selectedImageUri.toString(), Toast.LENGTH_LONG).show();
                try {
                    PhotoCorrectionActivity.setBitmap(decodeFile(getContentResolver(), selectedImageUri, getMaxImageSize(), MAX_WIDTH, MAX_HEIGHT));
                    Intent intent = new Intent(context, PhotoCorrectionActivity.class);
                    context.startActivity(intent);
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentCameraId != NO_CAMERA) {
            startCamera(currentCameraId);
        }
    }

    private static int findScale(int currentSize, int maxSize, int currentWidth, int currentHeight, int minWidth, int minHeight) {
        int currentScale = 1;
        while (currentSize / (currentScale * currentScale) > maxSize || currentWidth / currentScale > 2 * minWidth || currentHeight / currentScale > 2 * minHeight) {
            currentScale <<= 1;
        }
        return currentScale;
    }

    private static int getMaxImageSize() {
        long totalMemory = Runtime.getRuntime().maxMemory();
        return (int) (totalMemory / (MEMORY_SCALE_DOWN * PIXEL_TOTAL_OVERHEAD_IN_BYTES));
    }

    private static Bitmap decodeFile(ContentResolver resolver, Uri uri, int maxImageSize, int maxWidth, int maxHeight) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(resolver.openInputStream(uri), null, options);
            BitmapFactory.Options newOptions = new BitmapFactory.Options();
            newOptions.inSampleSize = findScale(options.outWidth * options.outHeight, maxImageSize, options.outWidth, options.outHeight, maxWidth, maxHeight);
            return BitmapFactory.decodeStream(resolver.openInputStream(uri), null, newOptions);
        } catch (FileNotFoundException ignored) {
        }
        return null;
    }

    private static Bitmap scaleBitmapDown(Bitmap bitmap, int maxImageSize, int maxWidth, int maxHeight) {
        int scale = findScale(bitmap.getWidth() * bitmap.getHeight(), maxImageSize, bitmap.getWidth(), bitmap.getHeight(), maxWidth, maxHeight);
        return Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / scale, bitmap.getHeight() / scale, true);
    }

    private boolean used = false;

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Console.print("Surface created");
        camera = Camera.open();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (!used) {
            used = false;
            if (isFrontCamera) {
                setCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
            } else {
                setCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
          Console.print("Surface destroyed");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.phototakeractivity_takephotobutton: {
                if (camera != null) {
                    camera.autoFocus(this);
                }
            }
            break;
            case R.id.phototakeractivity_cameratogglebutton: {
                isFrontCamera = !isFrontCamera;
                if (isFrontCamera) {
                    setCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
                } else {
                    setCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                }
            }
            break;
            case R.id.phototakeractivity_gallerybutton: {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
            }
            break;
        }
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        camera.autoFocus(null);
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Toast toast = Toast.makeText(context, "The photo has been taken", Toast.LENGTH_LONG);
                toast.show();
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                PhotoCorrectionActivity.setBitmap(scaleBitmapDown(bitmap, getMaxImageSize(), MAX_WIDTH, MAX_HEIGHT));
                Intent intent = new Intent(context, PhotoCorrectionActivity.class);
                context.startActivity(intent);
            }
        });
    }
}
