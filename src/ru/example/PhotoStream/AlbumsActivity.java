package ru.example.PhotoStream;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import org.json.JSONObject;
import ru.example.PhotoStream.ViewAdapters.AlbumListAdapter;

/**
 * Created by Genyaz on 03.04.14.
 */
public class AlbumsActivity extends Activity {

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