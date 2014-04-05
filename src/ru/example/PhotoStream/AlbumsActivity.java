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
 * Created by Genyaz on 03.04.14.
 */
public class AlbumsActivity extends Activity {

    private class AlbumListAdapter extends BaseAdapter {
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

        private class AlbumInfo {
            String fid, gid, aid;
            View view;

            public AlbumInfo(String fid, String gid, String aid) {
                this.fid = fid;
                this.gid = gid;
                this.aid = aid;
                SortedSet<JSONObject> photos;
                TextView albumTitle = new TextView(context);
                albumTitle.setTextColor(Color.BLACK);
                if (aid == null) {
                    albumTitle.setText(R.string.private_album);
                    if (fid == null) {
                        photos = InfoHolder.userPrivatePhotos;
                    } else {
                        photos = InfoHolder.friendPrivatePhotos.get(fid);
                    }
                } else {
                    try {
                        albumTitle.setText(InfoHolder.allAlbums.get(aid).getString("title"));
                    } catch (Exception e) {
                        Console.print(e.getMessage());
                    }
                    photos = InfoHolder.albumPhotos.get(aid);
                }
                LinearLayout total = new LinearLayout(context);
                total.setOrientation(LinearLayout.HORIZONTAL);
                total.setGravity(Gravity.CENTER_HORIZONTAL);
                ImageView lastPhoto = new ImageView(context);
                if (!photos.isEmpty()) {
                    JSONObject lastPhotoObject = photos.first();
                    try {
                        new DownloadImageTask(lastPhoto).execute(lastPhotoObject.getString("pic190x190"));
                    } catch (Exception e) {
                        Console.print(e.getMessage());
                    }
                } else {
                    lastPhoto.setImageResource(R.drawable.nophoto);
                }
                total.addView(lastPhoto);
                Space space = new Space(context);
                space.setMinimumWidth(10);
                total.addView(space);
                total.addView(albumTitle);
                this.view = total;
            }
        }

        private List<AlbumInfo> albumInfos;

        public AlbumListAdapter(Context context) {
            this.context = context;
            this.albumInfos = new ArrayList<AlbumInfo>();
        }

        public void addAlbum(String fid, String gid, String aid) {
            this.albumInfos.add(new AlbumInfo(fid, gid, aid));
            notifyDataSetChanged();
        }

        public String getFid(int position) {
            return albumInfos.get(position).fid;
        }

        public String getGid(int position) {
            return albumInfos.get(position).gid;
        }

        public String getAid(int position) {
            return albumInfos.get(position).aid;
        }

        @Override
        public int getCount() {
            return albumInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return albumInfos.get(position).view;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return albumInfos.get(position).view;
        }
    }

    private String fid, gid;
    private ListView albumList;
    private AlbumListAdapter albumListAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.albumsactivity);
        Intent intent = getIntent();
        fid = intent.getStringExtra("fid");
        gid = intent.getStringExtra("gid");
        albumList = (ListView) findViewById(R.id.albumsactivity_album_list);
        albumList.setDividerHeight(20);
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }

    private void update() {
        albumListAdapter = new AlbumListAdapter(this);
        albumList.setAdapter(albumListAdapter);
        albumList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onAlbumClick(position);
            }
        });
        if (gid == null) {
            albumListAdapter.addAlbum(fid, gid, null);
            if (fid == null) {
                for (JSONObject album: InfoHolder.userAlbums) {
                    try {
                        albumListAdapter.addAlbum(fid, gid, album.getString("aid"));
                    } catch (Exception e) {
                        Console.print(e.getMessage());
                    }
                }
            } else {
                for (JSONObject album: InfoHolder.friendAlbums.get(fid)) {
                    try {
                        albumListAdapter.addAlbum(fid, gid, album.getString("aid"));
                    } catch (Exception e) {
                        Console.print(e.getMessage());
                    }
                }
            }
        } else {
            for (JSONObject album: InfoHolder.groupAlbums.get(gid)) {
                try {
                    albumListAdapter.addAlbum(fid, gid, album.getString("aid"));
                } catch (Exception e) {
                    Console.print(e.getMessage());
                }
            }
        }
    }

    private void onAlbumClick(int position) {
        Intent intent = new Intent(this, AlbumActivity.class);
        if (albumListAdapter.getFid(position) != null) {
            intent.putExtra("fid", albumListAdapter.getFid(position));
        }
        if (albumListAdapter.getGid(position) != null) {
            intent.putExtra("gid", albumListAdapter.getGid(position));
        }
        if (albumListAdapter.getAid(position) != null) {
            intent.putExtra("aid", albumListAdapter.getAid(position));
        }
        startActivity(intent);
    }
}