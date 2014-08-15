package ru.example.PhotoStream.Activities;

import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MotionEvent;
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
import ru.example.PhotoStream.Console;
import ru.example.PhotoStream.R;
import ru.ok.android.sdk.Odnoklassniki;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class VideoUploadActivity extends ActionBarActivity {
    private static Uri videoUri = null;

    public static void setVideoUri(Uri videoUri) {
        VideoUploadActivity.videoUri = videoUri;
    }

    private class Loader extends AsyncTask<Void, Void, Boolean> {

        private Uri videoUri;

        public Loader(Uri videoUri) {
            this.videoUri = videoUri;
        }

        private byte[] fileToBytes(File file) {
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(file);
                int length = (int)file.length(), read = 0;
                byte[] result = new byte[length];
                while (read < length) {
                    read += inputStream.read(result, read, length - read);
                }
                return result;
            } catch (IOException ignored) {
            } finally {
                try {
                    inputStream.close();
                } catch (Exception ignored) {
                }
            }
            return null;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            File videoFile = new File(videoUri.getPath());
            String videoTitleText = videoTitle.getText().toString();
            if (videoTitleText.isEmpty()) {
                videoTitleText = getString(R.string.NoTitle);
            }
            Map<String, String> requestParameters = new HashMap<>();
            requestParameters.put("file_name", videoTitleText);
            requestParameters.put("file_size", videoFile.length() + "");
            try {
                JSONObject response = new JSONObject(api.request("video.getUploadUrl", requestParameters, "get"));
                Console.print("Video response: " + response);
                String videoId = response.getString("video_id");
                URL url = new URL(response.getString("upload_url"));
                HttpClient httpclient = new DefaultHttpClient();
                httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
                HttpPost request = new HttpPost(url.toURI());
                byte[] data = fileToBytes(videoFile);
                ByteArrayBody body = new ByteArrayBody(data, "video");
                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
                entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                entityBuilder.addPart("uploaded", body);
                entityBuilder.addTextBody("videoCaption", "video");
                request.setEntity(entityBuilder.build());
                HttpResponse httpResponse = httpclient.execute(request);
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    requestParameters.clear();
                    requestParameters.put("vid", videoId);
                    String videoDescription = videoComment.getText().toString();
                    if (!videoDescription.isEmpty()) {
                        requestParameters.put("description", videoDescription);
                    }
                    response = new JSONObject(api.request("video.update", requestParameters, "get"));
                    return response.names().length() == 0;
                }
            } catch (Exception e) {
                Log.i("CONSOLE", e.getMessage(), e);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            videoComment.setEnabled(true);
            if (aBoolean) {
                Toast.makeText(VideoUploadActivity.this, getString(R.string.uploadSuccess), Toast.LENGTH_SHORT).show();
                uploadButton.setVisibility(View.GONE);
            } else {
                Toast.makeText(VideoUploadActivity.this, getString(R.string.uploadFailure), Toast.LENGTH_SHORT).show();
                uploadButton.setEnabled(true);
            }
        }
    }

    private Odnoklassniki api;
    private EditText videoComment;
    private EditText videoTitle;
    private Button uploadButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videouploadactivity);
        api = Odnoklassniki.getInstance(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getSupportActionBar().setTitle(getString(R.string.videouploadactivity_title));
        final VideoView videoView = (VideoView) findViewById(R.id.videouploadactivity_video);
        videoView.setVideoURI(videoUri);
        videoView.start();
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoView.start();
            }
        });
        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (videoView.isPlaying()) {
                    videoView.pause();
                } else {
                    videoView.start();
                }
                return false;
            }
        });
        videoComment = (EditText) findViewById(R.id.videouploadactivity_commenttext);
        videoTitle = (EditText) findViewById(R.id.videouploadactivity_title);
        uploadButton = (Button) findViewById(R.id.videouploadactivity_upload);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadButton.setEnabled(false);
                videoComment.setEnabled(false);
                Toast.makeText(VideoUploadActivity.this, getString(R.string.uploadStarting), Toast.LENGTH_LONG).show();
                Loader loader = new Loader(videoUri);
                loader.execute();
            }
        });
    }
}