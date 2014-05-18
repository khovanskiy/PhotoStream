package ru.example.PhotoStream.Activities;

import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ImageView;
import ru.example.PhotoStream.R;

public class UploadActivity extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.uploadactivity);
        getSupportActionBar().setTitle("Загрузка фотографии");
        ImageView photo = (ImageView) findViewById(R.id.uploadactivity_imageview);
        photo.setImageBitmap(CameraActivity.pictureTaken);
    }
}
