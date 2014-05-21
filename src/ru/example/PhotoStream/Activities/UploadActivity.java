package ru.example.PhotoStream.Activities;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.json.JSONObject;
import ru.example.PhotoStream.Console;
import ru.example.PhotoStream.R;
import ru.ok.android.sdk.Odnoklassniki;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class UploadActivity extends ActionBarActivity {

    private class Loader extends AsyncTask<Void, Void, Boolean> {

        private Bitmap bitmap;

        public Loader(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            Map<String, String> requestParameters = new HashMap<>();
            try {
                JSONObject response = new JSONObject(api.request("photosV2.getUploadUrl", requestParameters, "get"));
                Console.print("Photo response: " + response);
                String photoId = response.getJSONArray("photo_ids").getString(0);
                URL url = new URL(response.getString("upload_url"));
                HttpClient httpclient = new DefaultHttpClient();
                httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
                HttpPost request = new HttpPost(url.toURI());
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                byte[] data = outputStream.toByteArray();
                ByteArrayBody body = new ByteArrayBody(data, "filtered.jpg");
                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
                entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                entityBuilder.addPart("uploaded", body);
                entityBuilder.addTextBody("photoCaption", "filtered");
                request.setEntity(entityBuilder.build());
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
                response = new JSONObject(api.request("photosV2.commit", requestParameters, "get"));
                if (response.getJSONArray("photos").getJSONObject(0).getString("status").equals("SUCCESS")) {
                    return true;
                }
            } catch (Exception e) {
                Log.i("CONSOLE", e.getMessage(), e);

            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                Toast.makeText(UploadActivity.this, "Фотография загружена в личный альбом", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(UploadActivity.this, "Невозможно получить адрес для загрузки.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Odnoklassniki api;
    private static Bitmap pictureTaken;

    public static void setPicture(Bitmap bitmap) {
         pictureTaken = bitmap;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.uploadactivity);
        api = Odnoklassniki.getInstance(this);

        getSupportActionBar().setTitle("Загрузка фотографии");
        ImageView photo = (ImageView) findViewById(R.id.uploadactivity_imageview);
        photo.setImageBitmap(pictureTaken);
        Button uploadButton = (Button) findViewById(R.id.uploadactivity_upload);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Loader loader = new Loader(pictureTaken);
                loader.execute();
            }
        });
    }
}
