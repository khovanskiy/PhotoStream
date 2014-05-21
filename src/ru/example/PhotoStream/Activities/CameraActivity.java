package ru.example.PhotoStream.Activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.*;
import ru.example.PhotoStream.Camera.CameraPreview;
import ru.example.PhotoStream.Camera.Filters.Filters;
import ru.example.PhotoStream.Camera.Filters.PhotoFilter;
import ru.example.PhotoStream.Camera.PictureBitmapCallback;
import ru.example.PhotoStream.R;

import java.util.ArrayList;
import java.util.List;

public class CameraActivity extends ActionBarActivity implements PictureBitmapCallback {
    private CameraPreview preview;
    private Button takePictureButton;
    /**
     * Bitmap taken by takePicture() method.
     */
    public static Bitmap pictureTaken = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cameraactivity);
        getSupportActionBar().setTitle("Получение фотоснимка");
        preview = (CameraPreview) findViewById(R.id.cameraactivity_preview);
        preview.setPictureBitmapCallback(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        Spinner filterSpinner = (Spinner) findViewById(R.id.cameraactivity_filter_spinner);
        Filters.FilterType[] filterTypes = Filters.FilterType.values();
        String[] filterNames = new String[Filters.FilterType.values().length];
        for (int i = 0; i < filterNames.length; i++) {
            filterNames[i] = filterTypes[i].name();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filterNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(adapter);
        filterSpinner.setSelection(0);
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                List<PhotoFilter> photoFilters = new ArrayList<>();
                photoFilters.add(Filters.byName(((TextView) view).getText().toString()));
                preview.setPhotoFilters(photoFilters);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        takePictureButton = (Button) findViewById(R.id.cameraactivity_takepicture_button);
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preview.takePicture();
                takePictureButton.setClickable(false);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (pictureTaken != null) {
            pictureTaken = null;
        }
        preview.setPictureBitmapCallback(this);
        preview.startPreview();
    }

    @Override
    public void onPause() {
        super.onPause();
        preview.stopPreview();
    }

    @Override
    public void onPictureTaken(Bitmap bitmap) {
        pictureTaken = bitmap;
        Intent intent = new Intent(this, UploadActivity.class);
        takePictureButton.setClickable(true);
        startActivity(intent);
    }
}
