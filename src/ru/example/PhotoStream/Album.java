package ru.example.PhotoStream;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Album extends ApiObject {

    /**
     * Album's id or empty string if album is private.
     */
    //public String aid = "";

    /**
     * User's id or empty string if this is current user's or group's album.
     */
    public String user_id = "";

    /**
     * Group's id or empty string if this is friend's album.
     */
    public String group_id = "";

    /**
     * Album's title.
     */
    public String title = "";

    /**
     * Album's description.
     */
    public String description = "";

    /**
     * Creation time in milliseconds.
     */
    public String created = "";

    /**
     * Album's type in string form.
     */
    public String type = "";

    /**
     * Album's type in enum form.
     */
    public AlbumType albumType = AlbumType.USER;

    public boolean isPersonal = false;

    private static Map<String, Album> cache = new ConcurrentHashMap<>();

    private boolean running = false;

    private Album() {

    }

    /**
     * Returns album by its album id.
     *
     * @param albumId album id
     * @return album
     */

    public static synchronized Album get(String albumId) {
        Album current;
        if (!cache.containsKey(albumId)) {
            current = new Album();
            cache.put(albumId, current);
        } else {
            current = cache.get(albumId);
        }
        current.objectId = albumId;
        return current;
    }

    public static Album build(String aid, String ownerId, AlbumType type) {
        Album album = Album.get(aid);
        if (type == AlbumType.USER) {
            album.user_id = ownerId;
        } else {
            album.group_id = ownerId;
        }
        album.albumType = type;
        return album;
    }

    /**
     * Creates album from its JSON representation received from server.
     *
     * @param object JSON form of album
     * @return album
     * @throws JSONException
     */

    public static Album build(JSONObject object) throws JSONException {
        String currentId;
        if (object.has("aid")) {
            currentId = object.getString("aid");
        } else {
            throw new JSONException("Album object does not have ID");
        }
        Album current = Album.get(currentId);
        if (object.has("title")) {
            current.title = object.getString("title");
        }
        if (object.has("description")) {
            current.description = object.getString("description");
        }
        if (object.has("created")) {
            current.created = object.getString("created");
        }
        if (object.has("type")) {
            current.type = object.getString("type");
        }
        if (object.has("user_id")) {
            current.user_id = object.getString("user_id");
        }
        if (object.has("group_id")) {
            current.group_id = object.getString("group_id");
        }
        return current;
    }
}
