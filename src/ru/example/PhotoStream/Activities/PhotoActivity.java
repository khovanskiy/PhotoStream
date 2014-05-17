package ru.example.PhotoStream.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import org.json.JSONObject;
import ru.example.PhotoStream.Console;
import ru.example.PhotoStream.InfoHolder;
import ru.example.PhotoStream.R;
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
    private Context context;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.photoactivity);
        Intent intent = getIntent();
        fid = intent.getStringExtra("fid");
        gid = intent.getStringExtra("gid");
        aid = intent.getStringExtra("aid");
        photoId = intent.getStringExtra("photo_id");
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
        ((Button) findViewById(R.id.photoactivity_albums)).setText(toAlbums);
        ((Button) findViewById(R.id.photoactivity_stream)).setText(toStream);
        ImageView photo = (ImageView) findViewById(R.id.photoactivity_image);
        JSONObject photoObject = InfoHolder.allPhotos.get(photoId);
        try {
            new DownloadImageTask(photo).execute(photoObject.getString("pic1024x768"));
        } catch (Exception e) {
            Console.print(e.getMessage());
        }
        updateLayout();
    }

    private class LikeAdder extends AsyncTask<Map<String, String>, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Map<String, String>... params) {
            try {
                Odnoklassniki mOdnoklassniki = Odnoklassniki.getInstance(context);
                String responseString = mOdnoklassniki.request("photos.addPhotoLike", params[0], "get");
                if (responseString.equals("true")) {
                    return true;
                } else {
                    Console.print(responseString);
                    return false;
                }
            } catch (Exception e) {
                Console.print(e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                InfoHolder.likedDuringSessionPhotos.add(photoId);
                updateLayout();
            } else {
                Context context = getApplicationContext();
                CharSequence text = getString(R.string.cant_add_like);
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        }
    }

    public void onAddPhotoLikeClick(View view) {
        Map<String, String> requestParams = new HashMap<String, String>();
        requestParams.put("photo_id", photoId);
        if (gid != null) {
            requestParams.put("gid", gid);
        }
        new LikeAdder().execute(requestParams);
    }

    public void updateLayout() {
        JSONObject photoObject = InfoHolder.allPhotos.get(photoId);
        try {
            if (photoObject.getBoolean("liked_it") || InfoHolder.likedDuringSessionPhotos.contains(photoId)) {
                findViewById(R.id.photoactivity_like_button).setVisibility(View.GONE);
                findViewById(R.id.photoactivity_you_liked_text).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.photoactivity_like_button).setVisibility(View.VISIBLE);
                findViewById(R.id.photoactivity_you_liked_text).setVisibility(View.GONE);
            }
            if (fid == null && gid == null) {
                Button likeButton = (Button) findViewById(R.id.photoactivity_like_button);
                likeButton.setVisibility(View.GONE);
            }
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