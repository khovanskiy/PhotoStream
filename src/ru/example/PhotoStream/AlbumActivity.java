package ru.example.PhotoStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import org.json.JSONObject;
import ru.example.PhotoStream.ViewAdapters.PhotoListAdapter;

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