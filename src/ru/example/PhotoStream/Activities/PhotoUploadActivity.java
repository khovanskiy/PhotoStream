package ru.example.PhotoStream.Activities;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
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

public class PhotoUploadActivity extends ActionBarActivity {

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
            photoComment.setEnabled(true);
            if (aBoolean) {
                Toast.makeText(PhotoUploadActivity.this, getString(R.string.uploadSuccess), Toast.LENGTH_SHORT).show();
                uploadButton.setVisibility(View.GONE);
                PhotoCorrectionActivity.setMoveBack(true);
                PhotoTakerActivity.setMoveBack(true);
                pictureTaken.recycle();
                onBackPressed();
            } else {
                Toast.makeText(PhotoUploadActivity.this, getString(R.string.uploadFailure), Toast.LENGTH_SHORT).show();
                uploadButton.setEnabled(true);
            }
        }
    }

    private Odnoklassniki api;
    private static Bitmap pictureTaken;
    protected Button uploadButton;
    private EditText photoComment;

    public static void setPicture(Bitmap bitmap) {
         pictureTaken = bitmap;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.photouploadactivity);
        api = Odnoklassniki.getInstance(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        //getSupportActionBar().setTitle(getString(R.string.uploadActivity_title));
        ImageView photo = (ImageView) findViewById(R.id.photoupload_imageview);
        photo.setImageBitmap(pictureTaken);
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
        photoComment = (EditText) findViewById(R.id.photoupload_commenttext);
    }
}
