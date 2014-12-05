package ru.example.PhotoStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class User extends AlbumsOwner {

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
     * User's title photo's.
     */
    public Photo photo_id = null;

    public static String currentUID = "";

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
    public static synchronized User get(String userId) {
        User current;
        if (!cache.containsKey(userId)) {
            current = new User();
            cache.put(userId, current);
            actual.compareAndSet(true, false);
        } else {
            current = cache.get(userId);
        }
        current.objectId = userId;
        return current;
    }

    @Override
    public List<Album> getAlbums() {
        return albums;
    }

    @Override
    public void loadAlbums(Odnoklassniki api) {
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("fid", objectId);
        requestParams.put("fields", "user_album.*");

        albums.clear();
        Album personalAlbum = Album.build("personal" + objectId, objectId, AlbumType.USER);
        personalAlbum.title = "Личный альбом";
        personalAlbum.isPersonal = true;
        albums.add(personalAlbum);
        boolean hasMore = true;
        while (hasMore) {
            try {
                String response = api.request("photos.getAlbums", requestParams, "post");
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
        if (object.has("first_name")) {
            current.first_name = object.getString("first_name");
        }
        if (object.has("last_name")) {
            current.last_name = object.getString("last_name");
        }
        if (object.has("name")) {
            current.name = object.getString("name");
        }
        if (object.has("photo_id")) {
            current.photo_id = Photo.get(object.getString("photo_id"));
        }
        //Console.print("User " + current.name + " " + current.photo_id);
        return current;
    }
}
