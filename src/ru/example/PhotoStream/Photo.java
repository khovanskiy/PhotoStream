package ru.example.PhotoStream;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Photo {
    /**
     * Photo's id.
     */
    public String id = "";

    /**
     * Creation time in milliseconds.
     */
    public long created_ms = 0;

    /**
     * Id of the album where the photo is located or null if the album is private.
     */
    public String album_id = "";

    /**
     * Url of 50x50 version.
     */
    public String pic50x50 = "";

    /**
     * Url of 128x128 version.
     */
    public String pic128x128 = "";

    /**
     * Url of 190x190 version.
     */
    public String pic190x190 = "";

    /**
     * Url of version with min size 180.
     */
    public String pic180min = "";

    /**
     * Url of 640x480 version.
     */
    public String pic640x480 = "";

    /**
     * Url of 1024x768 version.
     */
    public String pic1024x768 = "";

    /**
     * Comments' count.
     */
    public int comments_count = 0;

    /**
     * Likes' count.
     */
    public String like_count = "";

    /**
     * Shows if the user has already liked this photo.
     */
    public Boolean liked_it = false;

    /**
     * User id of the photo's owner.
     */
    public String user_id = "";

    /**
     * Marks' count.
     */
    public int mark_count = 0;

    /**
     * Marks' bonus count.
     */
    public int mark_bonus_count = 0;

    /**
     * Average mark.
     */
    public double mark_avg = 0.0;

    /**
     * User's mark of this photo.
     */
    public String viewer_mark = "";

    private static Map<String, Photo> cache = new ConcurrentHashMap<>();

    private Photo() {
    }

    /**
     * Returns photo by its id.
     * @param photoId photo's id
     * @return photo
     */
    public static synchronized Photo get(String photoId) {
        Photo current = null;
        if (!cache.containsKey(photoId)) {
            current = new Photo();
            cache.put(photoId, current);
        } else {
            current = cache.get(photoId);
        }
        current.id = photoId;
        return current;
    }

    /**
     * Builds photo from its JSON representation received from server.
     * @param object JSON form
     * @return photo
     * @throws JSONException
     */

    public static Photo build(JSONObject object) throws JSONException {
        Photo current;
        if (object.has("id")) {
            current = Photo.get(object.getString("id"));
        } else {
            throw new JSONException("User object does not have ID");
        }
        if (object.has("created_ms")) {
            current.created_ms = object.getLong("created_ms");
        }
        if (object.has("album_id")) {
            current.album_id = object.getString("album_id");
        }
        if (object.has("pic50x50")) {
            current.pic50x50 = object.getString("pic50x50");
        }
        if (object.has("pic128x128")) {
            current.pic128x128 = object.getString("pic128x128");
        }
        if (object.has("pic180min")) {
            current.pic180min = object.getString("pic180min");
        }
        if (object.has("pic190x190")) {
            current.pic190x190 = object.getString("pic190x190");
        }
        if (object.has("pic640x480")) {
            current.pic640x480 = object.getString("pic640x480");
        }
        if (object.has("pic1024x768")) {
            current.pic1024x768 = object.getString("pic1024x768");
        }
        if (object.has("liked_it")) {
            current.liked_it = object.getBoolean("liked_it");
        }
        if (object.has("comments_count")) {
            current.comments_count = object.getInt("comments_count");
        }
        if (object.has("user_id")) {
            current.user_id = object.getString("user_id");
        }
        if (object.has("mark_count")) {
            current.mark_count = object.getInt("mark_count");
        }
        if (object.has("mark_bonus_count")) {
            current.mark_bonus_count = object.getInt("mark_bonus_count");
        }
        /*if (object.has("mark_avg")) {
            current.mark_avg = object.getString("mark_avg");    // output: 5+
        }*/
        return current;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Photo) {
            Photo obj = (Photo) o;
            return this.id.equals(obj.id);
        }
        return false;
    }
}
