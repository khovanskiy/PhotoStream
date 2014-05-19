package ru.example.PhotoStream.Activities;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ImageView;
import ru.example.PhotoStream.R;

public class UploadActivity extends ActionBarActivity {
    private ImageView photo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.uploadactivity);
        getSupportActionBar().setTitle("Загрузка фотографии");
        photo = (ImageView) findViewById(R.id.uploadactivity_imageview);
        photo.setImageBitmap(CameraActivity.pictureTaken);
        CameraActivity.pictureTaken = null;
    }

    @Override
    public void onDestroy() {
        photo.setImageBitmap(Bitmap.createBitmap(new int[] {0}, 1, 1, Bitmap.Config.ARGB_8888));
    }
}
