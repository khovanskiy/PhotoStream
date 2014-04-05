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
import java.util.*;

/**
 * Created by Genyaz on 05.04.14.
 */
public class SubstreamActivity extends Activity {

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
            String photo_id, fid, gid, aid;
            View view;

            public PhotoInfo(String photo_id) {
                this.photo_id = photo_id;
                fid = null;
                gid = null;
                aid = null;
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
                        fid = userId;
                        JSONObject friend = InfoHolder.friendInfo.get(userId);
                        try {
                            owner.setText(R.string.owner + ": " + friend.getString("name"));
                        } catch (Exception e) {
                            Console.print(e.getMessage());
                        }
                    } else if (InfoHolder.groupInfo.containsKey(userId)) {
                        gid = userId;
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
                        aid = photo.getString("album_id");
                        JSONObject albumObject = InfoHolder.allAlbums.get(aid);
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
                    total.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
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
            notifyDataSetChanged();
        }

        public String getPhotoId(int position) {
            return photoInfos.get(position).photo_id;
        }

        public String getFriendId(int position) {
            return photoInfos.get(position).fid;
        }

        public String getGroupId(int position) {
            return photoInfos.get(position).gid;
        }

        public String getAlbumId(int position) {
            return photoInfos.get(position).aid;
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

    private ListView photoList;
    private PhotoListAdapter photoListAdapter;
    private String fid, gid;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.substreamactivity);
        Intent intent = getIntent();
        fid = intent.getStringExtra("fid");
        gid = intent.getStringExtra("gid");
        photoList = (ListView) findViewById(R.id.substreamactivity_photolist);
    }

    private void onPhotoClick(int position) {
        Intent intent = new Intent(this, PhotoActivity.class);
        intent.putExtra("photo_id", photoListAdapter.getPhotoId(position));
        if (photoListAdapter.getFriendId(position) != null) {
            intent.putExtra("fid", photoListAdapter.getFriendId(position));
        }
        if (photoListAdapter.getGroupId(position) != null) {
            intent.putExtra("gid", photoListAdapter.getGroupId(position));
        }
        if (photoListAdapter.getAlbumId(position) != null) {
            intent.putExtra("aid", photoListAdapter.getAlbumId(position));
        }
        startActivity(intent);
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
        SortedSet<JSONObject> photos = new TreeSet<JSONObject>();
        if (fid != null) {
            photos = InfoHolder.friendPhotos.get(fid);
        } else if (gid != null) {
            photos = InfoHolder.groupPhotos.get(gid);
        }
        for (JSONObject photo: photos) {
            try {
                photoListAdapter.addPhoto(photo.getString("id"));
            } catch (Exception e) {
                Console.print(e.getMessage());
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }

    public void onAlbumsClick(View view) {
        Intent intent = new Intent(this, AlbumsActivity.class);
        if (fid != null) {
            intent.putExtra("fid", fid);
        }
        if (gid != null) {
            intent.putExtra("gid", gid);
        }
        startActivity(intent);
    }
}