package ru.example.PhotoStream;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.ok.android.sdk.Odnoklassniki;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StreamActivity extends Activity {
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
                    e.printStackTrace();
                }
                return mIcon11;
            }

            protected void onPostExecute(Bitmap result) {
                bmImage.setImageBitmap(result);
            }
        }

        private class PhotoInfo {
            String url;
            String source_id;
            View view;

            public PhotoInfo(String url, String source_id) {
                this.url = url;
                this.source_id = source_id;
                this.view = new ImageView(context);
                new DownloadImageTask((ImageView) view).execute(URLEncoder.encode(url));
            }
        }

        private List<PhotoInfo> photoInfos;

        public PhotoListAdapter(Context context) {
            this.context = context;
            this.photoInfos = new ArrayList<PhotoInfo>();
        }

        public void addPhoto(String url, String source_id) {
            this.photoInfos.add(new PhotoInfo(url, source_id));
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

    private class PhotoInfoLoader extends AsyncTask<Void, Void, Void> {

        private List<String> getFriendIDs() {
            List<String> result = new ArrayList<String>();
            try {
                JSONArray friendIDs = new JSONArray(mOdnoklassniki.request("friends.get", null, "get"));
                for (int i = 0; i < friendIDs.length(); i++) {
                    result.add(friendIDs.getString(i));
                }
            } catch (Exception ignored) {
            }
            return result;
        }

        private List<JSONObject> getAllGroups() {
            Map<String, String> requestParams = new HashMap<String, String>();
            requestParams.put("fields", "group.*");
            List<JSONObject> result = new ArrayList<JSONObject>();
            boolean hasMore = true;
            while (hasMore) {
                try {
                    JSONObject groupsObject = new JSONObject(mOdnoklassniki.request("group.getUserGroupsV2", requestParams, "get"));
                    JSONArray groups = groupsObject.getJSONArray("groups");
                    for (int i = 0; i < groups.length(); i++) {
                        result.add(groups.getJSONObject(i));
                    }
                    hasMore = groupsObject.getBoolean("hasMore");
                    requestParams.put("anchor", groupsObject.getString("anchor"));
                } catch (Exception e) {
                    hasMore = false;
                }
            }
            return result;
        }

        private List<JSONObject> getAllAlbums(String fid, String gid) {
            Map<String, String> requestParams = new HashMap<String, String>();
            requestParams.put("fields", "album.*");
            if (fid != null) {
                requestParams.put("fid", fid);
            }
            if (gid != null) {
                requestParams.put("gid", gid);
            }
            List<JSONObject> result = new ArrayList<JSONObject>();
            boolean hasMore = true;
            while (hasMore) {
                try {
                    JSONObject albumsObject = new JSONObject(mOdnoklassniki.request("photos.getAlbums", requestParams, "get"));
                    JSONArray albums = albumsObject.getJSONArray("albums");
                    for (int i = 0; i < albums.length(); i++) {
                        result.add(albums.getJSONObject(i));
                    }
                    hasMore = albumsObject.getBoolean("hasMore");
                    requestParams.put("anchor", albumsObject.getString("pagingAnchor"));
                } catch (Exception e) {
                    hasMore = false;
                }
            }
            return result;
        }

        private List<JSONObject> getAllPhotos(String fid, String aid) {
            Map<String, String> requestParams = new HashMap<String, String>();
            if (fid != null) {
                requestParams.put("fid", fid);
            }
            if (aid != null) {
                requestParams.put("aid", aid);
            }
            requestParams.put("fields", "photo.*");
            List<JSONObject> result = new ArrayList<JSONObject>();
            boolean hasMore = true;
            while (hasMore) {
                try {
                    JSONObject photosObject = new JSONObject(mOdnoklassniki.request("photos.getPhotos", requestParams, "get"));
                    JSONArray photos = photosObject.getJSONArray("photos");
                    for (int i = 0; i < photos.length(); i++) {
                        result.add(photos.getJSONObject(i));
                    }
                    hasMore = photosObject.getBoolean("hasMore");
                    requestParams.put("anchor", photosObject.getString("anchor"));
                } catch (Exception e) {
                    hasMore = false;
                }
            }
            return result;
        }

        @Override
        protected Void doInBackground(Void... params) {
            List<JSONObject> albums = getAllAlbums(null, null); // User own albums.
            List<JSONObject> photos = getAllPhotos(null, null); //User own photos in private album.
            List<String> friendIDs = getFriendIDs();
            for (int i = 0; i < friendIDs.size(); i++) {
                albums.addAll(getAllAlbums(friendIDs.get(i), null));
                photos.addAll(getAllPhotos(friendIDs.get(i), null));
            }
            List<JSONObject> groups = getAllGroups();
            for (int i = 0; i < groups.size(); i++) {
                try {
                    albums.addAll(getAllAlbums(null, groups.get(i).getString("gid")));
                } catch (Exception ignored) {
                }
            }
            for (int i = 0; i < albums.size(); i++) {
                try {
                    photos.addAll(getAllPhotos(null, albums.get(i).getString("aid")));
                } catch (Exception ignored) {
                }
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            photoListAdapter.notifyDataSetChanged();
        }
    }

    private ListView photoList;
    private PhotoListAdapter photoListAdapter;
    private Odnoklassniki mOdnoklassniki;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.streamactivity);
        photoList = (ListView) findViewById(R.id.authactivity_photolist);
        mOdnoklassniki = Odnoklassniki.getInstance(getApplicationContext());
    }

    private void update() {
        photoListAdapter = new PhotoListAdapter(this);
        photoList.setAdapter(photoListAdapter);
        new PhotoInfoLoader().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        update();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
