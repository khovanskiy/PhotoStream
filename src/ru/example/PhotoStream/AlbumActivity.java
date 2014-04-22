package ru.example.PhotoStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import org.json.JSONObject;
import ru.example.PhotoStream.ViewAdapters.PhotoListAdapter;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

/**
 * Created by Genyaz on 01.04.14.
 */
public class AlbumActivity extends Activity {

    private final int PHOTOS_TO_LOAD = 3;
    private JSONObject[] photosToDisplay;
    private int nextAdded;
    private String fid = null, gid = null, aid = null;
    private ListView photoList;
    private PhotoListAdapter photoListAdapter;
    private Context context;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.albumactivity);
        Intent intent = getIntent();
        fid = intent.getStringExtra("fid");
        gid = intent.getStringExtra("gid");
        aid = intent.getStringExtra("aid");
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
        ((Button) findViewById(R.id.albumactivity_albums)).setText(toAlbums);
        ((Button) findViewById(R.id.albumactivity_stream)).setText(toStream);
        photoList = (ListView) findViewById(R.id.albumactivity_photolist);
        updateLayout();
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }

    private void loadMorePhotos() {
        for (int i = nextAdded; i < Math.min(nextAdded + PHOTOS_TO_LOAD, photosToDisplay.length); i++) {
            try {
                photoListAdapter.addPhoto(photosToDisplay[i].getString("id"));
            } catch (Exception e) {
                Console.print(e.getMessage());
            }
        }
        nextAdded = Math.min(nextAdded + PHOTOS_TO_LOAD, photosToDisplay.length);
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
        SortedSet<JSONObject> photos;
        if (aid != null) {
            photos = InfoHolder.albumPhotos.get(aid);
        } else if (fid != null) {
            photos = InfoHolder.friendPrivatePhotos.get(fid);
        } else {
            photos = InfoHolder.userPrivatePhotos;
        }
        photosToDisplay = photos.toArray(new JSONObject[0]);
        nextAdded = 0;
        loadMorePhotos();
        photoList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                final int lastItem = firstVisibleItem + visibleItemCount;
                if(lastItem == totalItemCount) {
                    loadMorePhotos();
                }
            }
        });
    }

    private class LikeAdder extends AsyncTask<Map<String, String>, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Map<String, String>... params) {
            try {
                Odnoklassniki mOdnoklassniki = Odnoklassniki.getInstance(context);
                String responseString = mOdnoklassniki.request("photos.addAlbumLike", params[0], "get");
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
                InfoHolder.likedDuringSessionAlbums.add(aid);
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

    private void updateLayout() {
        if (aid != null) {
            JSONObject albumObject = InfoHolder.allAlbums.get(aid);
            try {
                if (albumObject.getBoolean("liked_it") || InfoHolder.likedDuringSessionAlbums.contains(aid)) {
                    findViewById(R.id.albumactivity_likebutton).setVisibility(View.GONE);
                    findViewById(R.id.albumactivity_you_liked_text).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.albumactivity_likebutton).setVisibility(View.VISIBLE);
                    findViewById(R.id.albumactivity_you_liked_text).setVisibility(View.GONE);
                }
            } catch (Exception e) {
                Console.print(e.getMessage());
            }
        }
        if (fid == null && gid == null || aid == null || InfoHolder.likedDuringSessionPhotos.contains(aid)) {
            Button likeButton = (Button) findViewById(R.id.albumactivity_likebutton);
            likeButton.setVisibility(View.GONE);
        }
    }

    public void onAddAlbumLikeClick(View view) {
        Map<String, String> requestParams = new HashMap<String, String>();
        requestParams.put("aid", aid);
        if (gid != null) {
            requestParams.put("gid", gid);
        }
        new LikeAdder().execute(requestParams);
    }

    public void onAlbumToAlbumsClick(View view) {
        Intent intent = new Intent(this, AlbumsActivity.class);
        if (fid != null) {
            intent.putExtra("fid", fid);
        }
        if (gid != null) {
            intent.putExtra("gid", gid);
        }
        startActivity(intent);
    }

    public void onAlbumToStreamClick(View view) {
        Intent intent = new Intent(this, SubstreamActivity.class);
        if (fid != null) {
            intent.putExtra("fid", fid);
        }
        if (gid != null) {
            intent.putExtra("gid", gid);
        }
        startActivity(intent);
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