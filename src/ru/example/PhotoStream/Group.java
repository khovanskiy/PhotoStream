package ru.example.PhotoStream;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Group extends AlbumsKeeper {
    public String uid = "";
    public String name = "";
    public String description = "";
    public String shortname = "";
    public String photo_id = "";
    public boolean shop_visible_admin = false;
    public boolean shop_visible_public = false;
    public int members_count = 0;

    private static Map<String, Group> cache = new ConcurrentHashMap<>();
    private List<Album> albums = new ArrayList<>();
    //public Photo photo = new Photo();

    private Group() {

    }

    public static Group get(String groupId) {
        Group current;
        if (!cache.containsKey(groupId)) {
            current = new Group();
            cache.put(groupId, current);
        } else {
            current = cache.get(groupId);
        }
        current.uid = groupId;
        return current;
    }

    public static Group build(JSONObject object) throws JSONException {
        Group current;
        if (object.has("uid")) {
            current = Group.get(object.getString("uid"));
        } else {
            throw new JSONException("Group object does not have ID");
        }

        if (object.has("name")) {
            current.name = object.getString("name");
        }
        if (object.has("description")) {
            current.description = object.getString("description");
        }
        if (object.has("shortname")) {
            current.shortname = object.getString("shortname");
        }
        if (object.has("photo_id")) {
            current.photo_id = object.getString("photo_id");
        }
        return current;
    }

    @Override
    public List<Album> getAlbums() {
        return albums;
    }

    @Override
    public void loadAlbums(Odnoklassniki api) {
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("gid", uid);
        requestParams.put("fields", "group_album.*");

        albums.clear();
        boolean hasMore = true;
        while (hasMore) {
            try {
                String response = api.request("photos.getAlbums", requestParams, "post");
                Console.print("Response " + response);
                JSONObject albumsObject = new JSONObject(response);
                JSONArray array = albumsObject.getJSONArray("albums");
                for (int i = 0; i < array.length(); ++i) {
                    Album album = Album.build(array.getJSONObject(i));
                    album.albumType = AlbumType.GROUP;
                    albums.add(album);
                }
                hasMore = albumsObject.getBoolean("hasMore");
                requestParams.put("pagingAnchor", albumsObject.getString("pagingAnchor"));
            } catch (Exception e) {
                Log.i("CONSOLE", e.toString(), e);
                hasMore = false;
            }
        }
    }
}
