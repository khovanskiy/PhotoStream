package ru.example.PhotoStream.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;
import ru.example.PhotoStream.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class PhotoTakerActivity extends Activity {
    private static final int NO_CAMERA = -1;
    private static final int SELECT_PICTURE = 1;
    private static final int MEMORY_SCALE_DOWN = 4;
    private static final int PIXEL_TOTAL_OVERHEAD_IN_BYTES = 22;
    private static final int MAX_WIDTH = 1024;
    private static final int MAX_HEIGHT = 1024;

    private Switch cameraSwitch;
    private Camera camera = null;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private int currentCameraId;
    private Context context;

    private void stopCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private void startCamera(int cameraId) {
        try {
            camera = Camera.open(cameraId);
            camera.setPreviewDisplay(surfaceHolder);
            Camera.Size size = camera.getParameters().getPreviewSize();
            float aspectRatio = (float) size.width / size.height;
            int newWidth, newHeight;
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
            Toast toast = Toast.makeText(context, "Failed to open camera", Toast.LENGTH_SHORT);
            toast.show();
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
        setContentView(R.layout.phototakeractivity);
        context = this;
        ImageButton galleryButton = (ImageButton) findViewById(R.id.phototakeractivity_gallerybutton);
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"), SELECT_PICTURE);
            }
        });
        ImageButton takePhotoButton = (ImageButton) findViewById(R.id.phototakeractivity_takephotobutton);
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (camera != null) {
                    camera.takePicture(null, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {
                            Toast toast = Toast.makeText(context, "The photo has been taken", Toast.LENGTH_LONG);
                            toast.show();
                            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                            PhotoFilteringActivity.setBitmap(scaleBitmapDown(bitmap, getMaxImageSize(), MAX_WIDTH, MAX_HEIGHT));
                            bitmap.recycle();
                            Intent intent = new Intent(context, PhotoFilteringActivity.class);
                            context.startActivity(intent);
                        }
                    });
                }
            }
        });
        cameraSwitch = (Switch) findViewById(R.id.phototakeractivity_cameraswitch);
        cameraSwitch.setTextColor(Color.WHITE);
        cameraSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
                } else {
                    setCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                }
            }
        });
        cameraSwitch.setEnabled(false);
        currentCameraId = NO_CAMERA;
        surfaceView = (SurfaceView) findViewById(R.id.phototakeractivity_surfaceview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            private boolean used = false;

            @Override
            public void surfaceCreated(SurfaceHolder holder) {

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (!used) {
                    used = false;
                    cameraSwitch.setEnabled(true);
                    if (cameraSwitch.isChecked()) {
                        setCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
                    } else {
                        setCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                    }
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                String selectedImagePath = getPath(selectedImageUri);
                Toast toast = Toast.makeText(context, selectedImagePath, Toast.LENGTH_LONG);
                toast.show();
                try {
                    File image = new File(selectedImagePath);
                    PhotoFilteringActivity.setBitmap(decodeFile(image, getMaxImageSize(), MAX_WIDTH, MAX_HEIGHT));
                    Intent intent = new Intent(context, PhotoFilteringActivity.class);
                    context.startActivity(intent);
                } catch (Exception ignored) {

                }
            }
        }
    }

    public String getPath(Uri uri) {
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        return picturePath;
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

    private static int getMaxImageSize() {
        long totalMemory = Runtime.getRuntime().maxMemory();
        return (int)(totalMemory / (MEMORY_SCALE_DOWN * PIXEL_TOTAL_OVERHEAD_IN_BYTES));
    }

    private static Bitmap decodeFile(File file, int maxImageSize, int maxWidth, int maxHeight) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(file), null, options);
            int currentSize = options.outWidth * options.outHeight;
            int scale = 1;
            while ((currentSize / (scale * scale) > maxImageSize)
                    ||  (Math.abs(options.outWidth / scale - maxWidth) + Math.abs(options.outHeight / scale - maxHeight)
                        >= Math.abs(options.outWidth / (scale * 2) - maxWidth) + Math.abs(options.outHeight / (scale * 2) - maxHeight))) {
                scale *= 2;
            }
            BitmapFactory.Options newOptions = new BitmapFactory.Options();
            newOptions.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(file), null, newOptions);
        } catch (FileNotFoundException ignored) {}
        return null;
    }

    private static Bitmap scaleBitmapDown(Bitmap bitmap, int maxImageSize, int maxWidth, int maxHeight) {
        int currentSize = bitmap.getWidth() * bitmap.getHeight();
        int scale = 1;
        while ((currentSize / (scale * scale) > maxImageSize)
                ||  (Math.abs(bitmap.getWidth() / scale - maxWidth) + Math.abs(bitmap.getHeight() / scale - maxHeight)
                >= Math.abs(bitmap.getWidth() / (scale * 2) - maxWidth) + Math.abs(bitmap.getHeight() / (scale * 2) - maxHeight))) {
            scale *= 2;
        }
        return Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / scale, bitmap.getHeight() / scale, true);
    }
}
