package ru.example.PhotoStream;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class User extends AlbumsKeeper {
    /**
     * User's id or empty string if this is current user.
     */
    public String uid = "";

    /**
     * User's locale.
     */
    public String locale = "";

    /**
     * User's first name.
     */
    public String first_name = "";

    /**
     * User's last name.
     */
    public String last_name = "";

    /**
     * User's name.
     */
    public String name = "";

    /**
     * User's gender.
     */
    public String gender = "";

    /**
     * User's age.
     */
    public String age = "";

    /**
     * User's birthday.
     */
    public String birthday = "";

    /**
     * Shows of user set email.
     */
    public String has_email = "";

    /**
     * User's location.
     */
    public String location = "";

    /**
     * User's home city.
     */
    public String city = "";

    /**
     * User's home country.
     */
    public String country = "";

    /**
     * User's current location.
     */
    public String current_location = "";

    /**
     * User's current status.
     */
    public String current_status = "";

    /**
     * User's current status id.
     */
    public String current_status_id = "";

    /**
     * User's current status setting date.
     */
    public String current_status_date = "";

    /**
     * Shows if user is online.
     */
    public String online = "";

    /**
     * User's last online time in milliseconds.
     */
    public String last_online = "";

    /**
     * User's title photo's id.
     */
    public String photo_id = "";

    /**
     * User's title photo's 50x50 url.
     */
    public String pic50x50 = "";

    /**
     * User's title photo's 128x128 url.
     */
    public String pic128x128 = "";

    /**
     * User's title photo's 128 max size url.
     */
    public String pic128max = "";

    /**
     * User's title photo's 180 min size url.
     */
    public String pic180min = "";

    /**
     * User's title photo's 240 min size url.
     */
    public String pic240min = "";

    /**
     * User's title photo's 320 min size url.
     */
    public String pic320min = "";

    /**
     * User's title photo's 190x190 url.
     */
    public String pic190x190 = "";

    /**
     * User's title photo's 640x480 url.
     */
    public String pic640x480 = "";

    /**
     * User's title photo's 1024x768 url.
     */
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
    private static List<User> friends = new ArrayList<>();
    private static AtomicBoolean actual = new AtomicBoolean(false);

    private User() {

    }

    /**
     * Returns all user friends.
     * @return users that are friends with current user
     */
    public static List<User> getAllUsers() {
        if (actual.compareAndSet(false, true))
        {
            friends.clear();
            Iterator<Map.Entry<String,User>> i = cache.entrySet().iterator();
            while (i.hasNext()) {
                User user = i.next().getValue();
                friends.add(user);
            }
        }
        return friends;
    }

    /**
     * Returns user by its user id.
     * @param userId user id
     * @return user
     */
    public static User get(String userId) {
        User current = null;
        if (!cache.containsKey(userId)) {
            current = new User();
            cache.put(userId, current);
            actual.compareAndSet(true, false);
        } else {
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

    /**
     * Builds user from its JSON representation received from the server.
     * @param object JSON form
     * @return user
     * @throws JSONException
     */
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
        if (object.has("name")) {
            current.name = object.getString("name");
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
