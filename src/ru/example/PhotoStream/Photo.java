package ru.example.PhotoStream;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Photo {
    public String id = "";
    public long created_ms = 0;
    public String album_id = "";
    public String pic50x50 = "";
    public String pic128x128 = "";
    public String pic190x190 = "";
    public String pic180min = "";
    public String pic640x480 = "";
    public String pic1024x768 = "";
    public int comments_count = 0;
    public String like_count = "";
    public String liked_it = "";
    public String user_id = "";
    public int mark_count = 0;
    public int mark_bonus_count = 0;
    public double mark_avg = 0.0;
    public String viewer_mark = "";

    private static Map<String, Photo> cache = new ConcurrentHashMap<>();

    private Photo() {
    }

    public static Photo get(String photoId) {
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
        if (object.has("mark_avg")) {
            current.mark_avg = object.getDouble("mark_avg");
        }
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
