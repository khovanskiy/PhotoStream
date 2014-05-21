package ru.example.PhotoStream.Activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
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
            HttpClient httpclient = new DefaultHttpClient();
            httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
            HttpPost request = new HttpPost(url.toURI());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            byte[] data = outputStream.toByteArray();
            ByteArrayBody body = new ByteArrayBody(data, "filtered.jpg");
            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            entity.addPart("uploaded", body);
            entity.addPart("photoCaption", new StringBody("filtered"));
            request.setEntity(entity);
            HttpResponse httpResponse = httpclient.execute(request);
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
            String s;
            while ((s = reader.readLine()) != null) {
                builder.append(s);
            }
            s = builder.toString();
            response = new JSONObject(s);
            String token = response.getJSONObject("photos").getJSONObject(photoId).getString("token");
            requestParameters.clear();
            requestParameters.put("photo_id", photoId);
            requestParameters.put("token", token);
            response = new JSONObject(mOdnoklassniki.request("photosV2.commit", requestParameters, "get"));
            if (response.getJSONArray("photos").getJSONObject(0).getString("status").equals("SUCCESS")) {
                Toast.makeText(this, "Фотография загружена в личный альбом", Toast.LENGTH_SHORT).show();
            }
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
        Button uploadButton = (Button) findViewById(R.id.uploadactivity_upload);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPhoto(CameraActivity.pictureTaken);
            }
        });
    }
}
