package ru.example.PhotoStream.Activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ImageView;
import android.widget.Toast;
import org.json.JSONObject;
import ru.example.PhotoStream.R;
import ru.ok.android.sdk.Odnoklassniki;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class UploadActivity extends ActionBarActivity {
    private ImageView photo;
    private Odnoklassniki mOdnoklassniki;

    private void uploadPhoto(Bitmap bitmap) {
        mOdnoklassniki = Odnoklassniki.getInstance(this);
        Map<String, String> requestParameters = new HashMap<>();
        try {
            JSONObject response = new JSONObject(mOdnoklassniki.request("photosV2.getUploadUrl", requestParameters, "get"));
            String photoId = response.getJSONArray("photo_ids").getString(0);
            URL url = new URL(response.getString("upload_url"));
            URLConnection connection = url.openConnection();
            connection.setDoOutput(true);
            String boundary = Long.toHexString(System.currentTimeMillis());
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            OutputStream outputStream = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            writer.write("--" + boundary + "\n");
            writer.write("Content-Disposition: form-data; name=\"picture\"; filename=\"" + boundary + ".jpg\"\n");
            writer.write("Content-Type: image/jpeg\n\n");
            writer.flush();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            writer.write("--" + boundary + "--\n");
            writer.flush();
            BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            response = new JSONObject(rd.readLine());
            String token = response.getJSONArray("photos").getJSONObject(0).getString(photoId);
            requestParameters.clear();
            requestParameters.put("photo_id", photoId);
            requestParameters.put("token", token);
            response = new JSONObject(mOdnoklassniki.request("photosV2.commit", requestParameters, "get"));
            if (response.getJSONArray("photos").getJSONObject(0).getString("status").equals("SUCCESS")) {
                Toast.makeText(this, "Фотография загружена в личный альбом", Toast.LENGTH_SHORT).show();
            }
            rd.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Невозможно получить адрес для загрузки.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.uploadactivity);
        getSupportActionBar().setTitle("Загрузка фотографии");
        photo = (ImageView) findViewById(R.id.uploadactivity_imageview);
        photo.setImageBitmap(CameraActivity.pictureTaken);
        uploadPhoto(CameraActivity.pictureTaken);
    }
}
