package ru.example.PhotoStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import org.json.JSONObject;

import java.io.InputStream;

/**
 * Created by Genyaz on 01.04.14.
 */
public class PhotoActivity extends Activity {

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Console.print(e.getMessage());
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    private String fid, gid, aid, photoId;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photoactivity);
        Intent intent = getIntent();
        fid = intent.getStringExtra("fid");
        gid = intent.getStringExtra("gid");
        aid = intent.getStringExtra("aid");
        photoId = intent.getStringExtra("photo_id");
        if (fid == null && gid == null) {
            Button likeButton = (Button) findViewById(R.id.photoactivity_like_button);
            likeButton.setEnabled(false);
            likeButton.setBackgroundColor(Color.GRAY);
        }
        ImageView photo = (ImageView) findViewById(R.id.photoactivity_image);
        JSONObject photoObject = InfoHolder.allPhotos.get(photoId);
        try {
            new DownloadImageTask(photo).execute(photoObject.getString("pic1024x768"));
        } catch (Exception e) {
            Console.print(e.getMessage());
        }
    }

    public void onAddLikeClick(View view) {
        //todo
    }
}