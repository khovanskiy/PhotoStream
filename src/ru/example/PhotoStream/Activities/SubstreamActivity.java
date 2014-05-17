package ru.example.PhotoStream.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import org.json.JSONObject;
import ru.example.PhotoStream.Console;
import ru.example.PhotoStream.InfoHolder;
import ru.example.PhotoStream.R;
import ru.example.PhotoStream.ViewAdapters.PhotoListAdapter;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by Genyaz on 05.04.14.
 */
public class SubstreamActivity extends Activity {

    private final int PHOTOS_TO_LOAD = 3;
    private JSONObject[] photosToDisplay;
    private int nextAdded;
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
        SortedSet<JSONObject> photos = new TreeSet<JSONObject>();
        if (fid != null) {
            photos = InfoHolder.friendPhotos.get(fid);
        } else if (gid != null) {
            photos = InfoHolder.groupPhotos.get(gid);
        } else {
            photos = InfoHolder.userPhotos;
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
                if (lastItem == totalItemCount) {
                    loadMorePhotos();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }

    public void onSubstreamToAlbumsClick(View view) {
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