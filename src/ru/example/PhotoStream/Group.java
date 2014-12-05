package ru.example.PhotoStream;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Group extends AlbumsOwner {

    /**
     * Group's title.
     */
    public String name = "";

    /**
     * Group's description.
     */
    public String description = "";

    /**
     * Short name.
     */
    public String shortname = "";

    /**
     * Title photo's id.
     */
    public Photo photo_id = null;
    public boolean shop_visible_admin = false;
    public boolean shop_visible_public = false;

    /**
     * Group's members count.
     */
    public int members_count = 0;

    private static Map<String, Group> cache = new ConcurrentHashMap<>();
    private List<Album> albums = new ArrayList<>();
    private static List<Group> groups = new ArrayList<>();
    private static AtomicBoolean actual = new AtomicBoolean(false);
    //public Photo photo = new Photo();

    private Group() {

    }

    /**
     * Returns all groups where this user is registered.
     * @return list of groups
     */
    public static List<Group> getAllGroups() {
        if (actual.compareAndSet(false, true)) {
            groups.clear();
            Iterator<Map.Entry<String, Group>> i = cache.entrySet().iterator();
            while (i.hasNext()) {
                Group group = i.next().getValue();
                groups.add(group);
            }
        }
        return groups;
    }

    /**
     * Returns group by its group id
     * @param groupId group id
     * @return group
     */
    public static synchronized Group get(String groupId) {
        Group current;
        if (!cache.containsKey(groupId)) {
            current = new Group();
            cache.put(groupId, current);
        } else {
            current = cache.get(groupId);
        }
        current.objectId = groupId;
        return current;
    }

    /**
     * Builds group from its JSON representation received from server.
     * @param object JSON form
     * @return group
     * @throws JSONException
     */
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
            current.photo_id = Photo.get(object.getString("photo_id"));
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
        requestParams.put("gid", objectId);
        requestParams.put("fields", "group_album.*");

        albums.clear();
        boolean hasMore = true;
        while (hasMore) {
            try {
                String response = api.request("photos.getAlbums", requestParams, "post");
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

    //@Override
    public String getName() {
        return name;
    }

    @Override
    public Photo getAvatar() {
        return photo_id;
    }
}
