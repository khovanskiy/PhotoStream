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

public class User extends AlbumsKeeper {
    public String uid = "";
    public String locale = "";
    public String first_name = "";
    public String last_name = "";
    public String name = "";
    public String gender = "";
    public String age = "";
    public String birthday = "";
    public String has_email = "";
    public String location = "";
    public String city = "";
    public String country = "";
    public String current_location = "";
    public String current_status = "";
    public String current_status_id = "";
    public String current_status_date = "";
    public String online = "";
    public String last_online = "";
    public String photo_id = "";
    public String pic50x50 = "";
    public String pic128x128 = "";
    public String pic128max = "";
    public String pic180min = "";
    public String pic240min = "";
    public String pic320min = "";
    public String pic190x190 = "";
    public String pic640x480 = "";
    public String pic1024x768 = "";
    public String url_profile = "";
    public String url_chat = "";
    public String url_profile_mobile = "";
    public String url_chat_mobile = "";
    public String can_vcall = "";
    public String can_vmail = "";
    public String allows_anonym_access = "";
    public String allows_messaging_only_for_friends = "";
    public String registered_date = "";
    public String has_service_invisible = "";

    private static Map<String, User> cache = new ConcurrentHashMap<>();
    private List<Album> albums = new ArrayList<>();

    private User() {

    }

    public static User get(String userId) {
        User current = null;
        if (!cache.containsKey(userId)) {
            current = new User();
            cache.put(userId, current);
        }
        else {
            current = cache.get(userId);
        }
        current.uid = userId;
        return current;
    }

    @Override
    public List<Album> getAlbums() {
        return albums;
    }

    @Override
    public void loadAlbums(Odnoklassniki api) {
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("fid", uid);
        requestParams.put("fields", "user_album.*");

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
                    album.albumType = AlbumType.USER;
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

    public static User build(JSONObject object) throws JSONException {
        User current;
        if (object.has("uid")) {
            current = User.get(object.getString("uid"));
        } else {
            throw new JSONException("User object does not have ID");
        }

        if (object.has("locale")) {
            current.locale = object.getString("locale");
        }
        if (object.has("first_name")) {
            current.first_name = object.getString("first_name");
        }
        if (object.has("last_name")) {
            current.last_name = object.getString("last_name");
        }
        if (object.has("pic50x50")) {
            current.pic50x50 = object.getString("pic50x50");
        }
        if (object.has("pic128x128")) {
            current.pic128x128 = object.getString("pic128x128");
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
        return current;
    }
}
