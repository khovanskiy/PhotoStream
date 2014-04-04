package ru.example.PhotoStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;

/**
 * Created by Genyaz on 01.04.14.
 */
public class AlbumActivity extends Activity {

    private class PhotoListAdapter extends BaseAdapter {
        private Context context;

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

        private class PhotoInfo {
            String photo_id;
            View view;

            public PhotoInfo(String photo_id) {
                this.photo_id = photo_id;
                ImageView photoView = new ImageView(context);
                try {
                    JSONObject photo = InfoHolder.allPhotos.get(photo_id);
                    new DownloadImageTask(photoView).execute(photo.getString("pic190x190"));
                    LinearLayout infoLayout = new LinearLayout(context);
                    infoLayout.setOrientation(LinearLayout.VERTICAL);
                    TextView owner = new TextView(context);
                    owner.setTextColor(Color.BLACK);
                    String userId = photo.getString("user_id");
                    if (InfoHolder.friendInfo.containsKey(userId)) {
                        JSONObject friend = InfoHolder.friendInfo.get(userId);
                        try {
                            owner.setText(R.string.owner + ": " + friend.getString("name"));
                        } catch (Exception e) {
                            Console.print(e.getMessage());
                        }
                    } else if (InfoHolder.groupInfo.containsKey(userId)) {
                        JSONObject group = InfoHolder.groupInfo.get(userId);
                        try {
                            owner.setText(R.string.owner + ": " + group.getString("title"));
                        } catch (Exception e) {
                            Console.print(e.getMessage());
                        }
                    } else {
                        owner.setText(getString(R.string.my_photo));
                    }
                    infoLayout.addView(owner);
                    TextView album = new TextView(context);
                    album.setTextColor(Color.BLACK);
                    if (photo.has("album_id")) {
                        String albumId = photo.getString("album_id");
                        JSONObject albumObject = InfoHolder.allAlbums.get(albumId);
                        try {
                            album.setText(R.string.album + ": " + albumObject.getString("title"));
                        } catch (Exception e) {
                            Console.print(e.getMessage());
                        }
                    } else {
                        album.setText(getString(R.string.private_album));
                    }
                    infoLayout.addView(album);
                    TextView created = new TextView(context);
                    created.setTextColor(Color.BLACK);
                    long ms = Long.parseLong(photo.getString("created_ms"));
                    Date date = new Date(ms);
                    created.setText(getString(R.string.uploaded) + ": " + date.toLocaleString());
                    infoLayout.addView(created);
                    LinearLayout total = new LinearLayout(context);
                    total.setOrientation(LinearLayout.HORIZONTAL);
                    total.setGravity(Gravity.CENTER_HORIZONTAL);
                    total.addView(photoView);
                    Space space = new Space(context);
                    space.setMinimumWidth(40);
                    total.addView(space);
                    total.addView(infoLayout);
                    this.view = total;
                } catch (Exception e) {
                    Console.print(e.getMessage());
                }
            }
        }

        private List<PhotoInfo> photoInfos;

        public PhotoListAdapter(Context context) {
            this.context = context;
            this.photoInfos = new ArrayList<PhotoInfo>();
        }

        public void addPhoto(String photo_id) {
            this.photoInfos.add(new PhotoInfo(photo_id));
        }

        public String getPhotoId(int position) {
            return this.photoInfos.get(position).photo_id;
        }

        @Override
        public int getCount() {
            return photoInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return photoInfos.get(position).view;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return photoInfos.get(position).view;
        }
    }

    private String fid = null, gid = null, aid = null;
    private ListView photoList;
    private PhotoListAdapter photoListAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.albumactivity);
        Intent intent = getIntent();
        fid = intent.getStringExtra("fid");
        gid = intent.getStringExtra("gid");
        aid = intent.getStringExtra("aid");
        if (fid == null && gid == null) {
            Button likeButton = (Button) findViewById(R.id.albumactivity_likebutton);
            likeButton.setEnabled(false);
            likeButton.setBackgroundColor(Color.GRAY);
        }
        photoList = (ListView) findViewById(R.id.albumactivity_photolist);
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }

    private void update() {
        photoListAdapter = new PhotoListAdapter(this);
        photoList.setAdapter(photoListAdapter);
        photoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onPhotoClick(position);
            }
        });
        SortedSet<JSONObject> photos = null;
        if (aid != null) {
            photos = InfoHolder.albumPhotos.get(aid);
        } else if (fid != null) {
            photos = InfoHolder.friendPrivatePhotos.get(fid);
        } else {
            photos = InfoHolder.userPrivatePhotos;
        }
        for (JSONObject photo: photos) {
            try {
                photoListAdapter.addPhoto(photo.getString("id"));
            } catch (Exception e) {
                Console.print(e.getMessage());
            }
        }
    }

    public void onAddLikeButton(View view) {
        //todo
    }

    private void onPhotoClick(int position) {
        Intent intent = new Intent(this, PhotoActivity.class);
        if (fid != null) {
            intent.putExtra("fid", fid);
        }
        if (gid != null) {
            intent.putExtra("gid", gid);
        }
        if (aid != null) {
            intent.putExtra("aid", aid);
        }
        intent.putExtra("photo_id", photoListAdapter.getPhotoId(position));
        startActivity(intent);
    }
}