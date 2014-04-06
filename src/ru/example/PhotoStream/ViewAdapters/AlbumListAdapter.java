package ru.example.PhotoStream.ViewAdapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.json.JSONObject;
import ru.example.PhotoStream.Console;
import ru.example.PhotoStream.InfoHolder;
import ru.example.PhotoStream.R;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

/**
 * Created by Genyaz on 06.04.14.
 */
public class AlbumListAdapter extends BaseAdapter {
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
                albumTitle.setText(context.getString(R.string.private_album));
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