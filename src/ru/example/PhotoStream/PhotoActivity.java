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
import ru.ok.android.sdk.Odnoklassniki;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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
        String toAlbums, toStream;
        if (fid == null && gid == null) {
            toAlbums = getString(R.string.my_albums);
            toStream = getString(R.string.my_stream);
        } else if (fid != null) {
            toAlbums = getString(R.string.friend_albums);
            toStream = getString(R.string.friend_stream);
        } else {
            toAlbums = getString(R.string.group_albums);
            toStream = getString(R.string.group_stream);
        }
        ((Button) findViewById(R.id.photoactivity_album)).setText(toAlbums);
        ((Button) findViewById(R.id.photoactivity_albums)).setText(toAlbums);
        ((Button) findViewById(R.id.photoactivity_stream)).setText(toStream);
        ImageView photo = (ImageView) findViewById(R.id.photoactivity_image);
        JSONObject photoObject = InfoHolder.allPhotos.get(photoId);
        try {
            new DownloadImageTask(photo).execute(photoObject.getString("pic1024x768"));
        } catch (Exception e) {
            Console.print(e.getMessage());
        }
    }

    public void onAddLikeClick(View view) {
        Odnoklassniki mOdnoklassniki = Odnoklassniki.getInstance(this);
        Map<String, String> requestParams = new HashMap<String, String>();
        requestParams.put("photo_id", photoId);
        if (gid != null) {
            requestParams.put("gid", gid);
        }
        try {
            mOdnoklassniki.request("photos.addPhotoLike", requestParams, "submit");
        } catch (Exception e) {
            Console.print(e.getMessage());
        }
    }

    public void onPhotoToAlbumClick(View view) {
        Intent intent = new Intent(this, AlbumActivity.class);
        if (fid != null) {
            intent.putExtra("fid", fid);
        }
        if (gid != null) {
            intent.putExtra("gid", gid);
        }
        if (aid != null) {
            intent.putExtra("aid", aid);
        }
        startActivity(intent);
    }

    public void onPhotoToAlbumsClick(View view) {
        Intent intent = new Intent(this, AlbumsActivity.class);
        if (fid != null) {
            intent.putExtra("fid", fid);
        }
        if (gid != null) {
            intent.putExtra("gid", gid);
        }
        startActivity(intent);
    }

    public void onPhotoToStreamClick(View view) {
        Intent intent = new Intent(this, SubstreamActivity.class);
        if (fid != null) {
            intent.putExtra("fid", fid);
        }
        if (gid != null) {
            intent.putExtra("gid", gid);
        }
        startActivity(intent);
    }
}