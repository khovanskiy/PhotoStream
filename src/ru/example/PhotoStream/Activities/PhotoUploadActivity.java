package ru.example.PhotoStream.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
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
import ru.example.PhotoStream.R;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class PhotoUploadActivity extends UIActivity {

    private class Loader extends AsyncTask<Void, Void, Boolean> {

        private Bitmap bitmap;

        public Loader(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            Map<String, String> requestParameters = new HashMap<>();
            try {
                JSONObject response = new JSONObject(getAPI().request("photosV2.getUploadUrl", requestParameters, "get"));
                //Console.print("Photo response: " + response);
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
                String comment = photoComment.getText().toString();
                if (!comment.isEmpty()) {
                    requestParameters.put("comment", comment);
                }
                response = new JSONObject(getAPI().request("photosV2.commit", requestParameters, "get"));
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
            photoComment.setEnabled(true);
            if (aBoolean) {
                Toast.makeText(PhotoUploadActivity.this, getString(R.string.uploadSuccess), Toast.LENGTH_SHORT).show();
                uploadButton.setVisibility(View.GONE);
                Intent intent = new Intent(getApplicationContext(), FeedsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } else {
                Toast.makeText(PhotoUploadActivity.this, getString(R.string.uploadFailure), Toast.LENGTH_SHORT).show();
                uploadButton.setEnabled(true);
            }
        }
    }

    private Bitmap pictureTaken;
    protected Button uploadButton;
    private EditText photoComment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pictureTaken = (Bitmap) weakCache(PhotoUploadActivity.class).get("pictureTaken");
        if (pictureTaken == null) {
            finish();
        }
        //weakCache(PhotoUploadActivity.class).remove("pictureTaken");

        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //Line is commented because it prevents view from resizing when keyboard appears
        //This is Android bug, we can do nothing
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.photouploadactivity);

        ImageView photo = (ImageView) findViewById(R.id.photoupload_imageview);
        photo.setImageBitmap(pictureTaken);

        photoComment = (EditText) findViewById(R.id.photoupload_commenttext);

        uploadButton = (Button) findViewById(R.id.photoupload_upload);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadButton.setEnabled(false);
                photoComment.setEnabled(false);
                Toast.makeText(PhotoUploadActivity.this, getString(R.string.uploadStarting), Toast.LENGTH_LONG).show();
                Loader loader = new Loader(pictureTaken);
                loader.execute();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                //NavUtils.navigateUpFromSameTask(this);
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
