package ru.example.PhotoStream.Activities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.*;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.Toast;
import net.hockeyapp.android.CrashManager;
import ru.example.PhotoStream.Camera.SurfaceGridView;
import ru.example.PhotoStream.Console;
import ru.example.PhotoStream.PortraitLandscapeListener;
import ru.example.PhotoStream.R;

import java.io.FileNotFoundException;
import java.util.List;

public final class PhotoTakerActivity extends Activity implements SurfaceHolder.Callback, View.OnClickListener, Camera.AutoFocusCallback {
    private static final int NO_CAMERA = -1;
    private static final int SELECT_PICTURE = 1;
    private static final int TAKE_VIDEO = 2;
    private static final int MEMORY_SCALE_DOWN = 4;
    private static final int PIXEL_TOTAL_OVERHEAD_IN_BYTES = 22;
    private static final int MAX_WIDTH = 1024;
    private static final int MAX_HEIGHT = 1024;
    private static boolean moveBack = false;

    public static void setMoveBack(boolean b) {
        moveBack = b;
    }

    private Camera camera = null;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private int currentCameraId;
    private Context context;
    private OrientationEventListener orientationEventListener;
    private SurfaceGridView gridView;
    private ImageButton flashButton;

    private boolean isFrontCamera = false;
    private boolean isPreviewRunning = false;
    private boolean gridOn = false;

    private void stopCamera() {
        if (camera != null) {
            //Console.print("Release camera");
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private void startCamera(int cameraId) {
        try {
            //Console.print("Open camera");
            camera = Camera.open(cameraId);
            camera.setPreviewDisplay(surfaceHolder);
            Camera.Size size = camera.getParameters().getPreviewSize();
            float aspectRatio = (float) size.width / size.height;
            int newWidth;
            int newHeight;
            if (surfaceView.getWidth() / surfaceView.getHeight() > aspectRatio) {
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
            updateCameraButton();
        } catch (Exception e) {
            Toast.makeText(context, "Failed to open camera", Toast.LENGTH_SHORT).show();
            Log.i("M_CONSOLE", e.getMessage(), e);
        }
    }

    private void setCamera(int cameraFacing) {
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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.phototakeractivity);
        context = this;

        ImageButton galleryButton = (ImageButton) findViewById(R.id.phototaker_gallery);
        galleryButton.setOnClickListener(this);
        ImageButton takePhotoButton = (ImageButton) findViewById(R.id.phototaker_take_photo);
        takePhotoButton.setOnClickListener(this);
        ImageButton cameraToggle = (ImageButton) findViewById(R.id.phototaker_camera_change);
        cameraToggle.setOnClickListener(this);
        ImageButton backButton = (ImageButton) findViewById(R.id.phototaker_back);
        backButton.setOnClickListener(this);
        ImageButton gridButton = (ImageButton) findViewById(R.id.phototaker_grid_button);
        gridButton.setOnClickListener(this);
        flashButton = (ImageButton) findViewById(R.id.phototaker_flash_type_change);
        flashButton.setOnClickListener(this);

        if (canToggleCamera()) {
            cameraToggle.setImageResource(R.drawable._0008_camera_rotate_camera);
            cameraToggle.setClickable(true);
        } else {
            cameraToggle.setImageResource(R.drawable._0007_camera_rotate_camera_disable);
            cameraToggle.setClickable(false);
        }

        currentCameraId = NO_CAMERA;
        surfaceView = (SurfaceView) findViewById(R.id.phototakeractivity_surfaceview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        orientationEventListener = new PortraitLandscapeListener(this) {
            @Override
            public void onOrientationChange(boolean landscape) {
                rotateAll(landscape);
            }
        };
        gridView = (SurfaceGridView) findViewById(R.id.phototaker_grid);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                PhotoCorrectionActivity.setBitmap(decodeFile(getContentResolver(), selectedImageUri, getMaxImageSize(), MAX_WIDTH, MAX_HEIGHT));
                Intent intent = new Intent(context, PhotoCorrectionActivity.class);
                //Console.printAvailableMemory();
                context.startActivity(intent);
            } else if (requestCode == TAKE_VIDEO) {
                Uri videoUri = data.getData();
                VideoUploadActivity.setVideoUri(videoUri);
                Intent intent = new Intent(this, VideoUploadActivity.class);
                startActivity(intent);
            }
        }
    }

    private static int findScale(int currentSize, int maxSize, int currentWidth, int currentHeight, int minWidth, int minHeight) {
        int currentScale = 1;
        while (/*currentSize / (currentScale * currentScale) > maxSize ||*/ currentWidth / currentScale > 2 * minWidth || currentHeight / currentScale > 2 * minHeight) {
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

            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inJustDecodeBounds = false;
            options.inSampleSize = findScale(options.outWidth * options.outHeight, maxImageSize, options.outWidth, options.outHeight, maxWidth, maxHeight);
            return BitmapFactory.decodeStream(resolver.openInputStream(uri), null, options);
        } catch (FileNotFoundException ignored) {
        }
        return null;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (isFrontCamera) {
            setCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
        } else {
            setCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (isPreviewRunning) {
            camera.stopPreview();
        }
        try {
            camera.startPreview();
        } catch (Exception e) {
        }
        isPreviewRunning = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (isPreviewRunning) {
            stopCamera();
            isPreviewRunning = false;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.phototaker_take_photo: {
                if (camera != null) {
                    camera.autoFocus(this);
                }
            }
            break;
            case R.id.phototaker_camera_change: {
                isFrontCamera = !isFrontCamera;
                stopCamera();
                if (isFrontCamera) {
                    setCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
                } else {
                    setCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                }
            }
            break;
            case R.id.phototaker_gallery: {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
            }
            break;
            case R.id.phototaker_back: {
                onBackPressed();
            }
            break;
            case R.id.phototaker_grid_button: {
                gridOn = !gridOn;
                if (!gridOn) {
                    gridView.setInvisible();
                    ((ImageButton)v).setImageResource(R.drawable._0005_camera_grid_off);
                } else {
                    gridView.setVisible();
                    ((ImageButton)v).setImageResource(R.drawable._0006_camera_grid_on);
                }
            } break;
            case R.id.phototaker_flash_type_change: {
                nextFlashMode();
            } break;
        }
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        camera.autoFocus(null);
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(data, 0, data.length, options);

                options.inSampleSize = findScale(options.outWidth * options.outHeight, getMaxImageSize(), options.outWidth, options.outHeight, MAX_WIDTH, MAX_HEIGHT);
                //Console.print("Scale picture = " + options.outWidth + " " + options.outHeight + " " + options.inSampleSize);
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                options.inJustDecodeBounds = false;
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);

                PhotoCorrectionActivity.setBitmap(bitmap);
                Intent intent = new Intent(context, PhotoCorrectionActivity.class);
                //Console.printAvailableMemory();
                context.startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        CrashManager.register(this, "5adb6faead01ccaa24e6865215ddcb59");
        if (moveBack) {
            moveBack = false;
            onBackPressed();
        }
        orientationEventListener.enable();
    }

    @Override
    public void onPause() {
        super.onPause();
        orientationEventListener.disable();
    }

    private synchronized void rotateAll(boolean landscape) {
        rotate(R.id.phototaker_camera_change, landscape);
        rotate(R.id.phototaker_take_photo, landscape);
        rotate(R.id.phototaker_gallery, landscape);
        rotate(R.id.phototaker_back, landscape);
        rotate(R.id.phototaker_flash_type_change, landscape);
        rotate(R.id.phototaker_grid_button, landscape);
    }

    private void rotate(int id, boolean landscape) {
        Animation animation;
        if (landscape) {
            animation = AnimationUtils.loadAnimation(context, R.anim.rotateright);
        } else {
            animation = AnimationUtils.loadAnimation(context, R.anim.rotateleft);
        }
        findViewById(id).startAnimation(animation);
    }

    private boolean canToggleCamera() {
        return Camera.getNumberOfCameras() > 1;
    }

    private void nextFlashMode() {
        Camera.Parameters parameters = camera.getParameters();
        List<String> supportedFlashModes = parameters.getSupportedFlashModes();
        String currentMode = parameters.getFlashMode();
        switch (currentMode) {
            case Camera.Parameters.FLASH_MODE_AUTO: {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            } break;
            case Camera.Parameters.FLASH_MODE_ON: {
                if (supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                } else {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                }
            } break;
            case Camera.Parameters.FLASH_MODE_OFF:
            default: {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            } break;
        }
        camera.setParameters(parameters);
        updateCameraButton();
    }

    private void updateCameraButton() {
        String flashMode = camera.getParameters().getFlashMode();
        if (flashMode == null) {
            flashButton.setClickable(false);
            flashButton.setImageResource(R.drawable._0009_camera_flash_disable);
        } else if (flashMode.equals(Camera.Parameters.FLASH_MODE_AUTO)) {
            flashButton.setClickable(true);
            flashButton.setImageResource(R.drawable._0010_camera_flash_auto);
        } else if (flashMode.equals(Camera.Parameters.FLASH_MODE_OFF)) {
            flashButton.setClickable(true);
            flashButton.setImageResource(R.drawable._0012_camera_flash_off);
        } else if (flashMode.equals(Camera.Parameters.FLASH_MODE_ON)) {
            flashButton.setClickable(true);
            flashButton.setImageResource(R.drawable._0011_camera_flash_on);
        }
    }

}
