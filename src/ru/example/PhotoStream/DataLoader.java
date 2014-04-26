package ru.example.PhotoStream;


import android.os.AsyncTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DataLoader extends AsyncTask<Void, Void, List<?>> implements IEventDispatcher {

    public class Groups extends DataLoader {
        public Groups(Odnoklassniki api) {
            super(api);
        }

        @Override
        protected List<?> doInBackground(Void... params) {
            List<Album> albums = getAlbums(null, null);
            List<Photo> result = new ArrayList<>();
            for (Album album : albums) {
                List<Photo> photos = getAlbumPhotos(null, null, album.aid);
                result.addAll(photos);
            }
            return result;
        }

        @Override
        protected void onPostExecute(List<?> data) {
            Event e = new Event(this, Event.COMPLETE);
            e.data.put("friends", data);
            dispatchEvent(e);
        }
    }

    private EventDispatcher eventDispatcher;
    private Odnoklassniki api;

    public DataLoader(Odnoklassniki api) {
        this.api = api;
        eventDispatcher = new EventDispatcher();
    }

    protected User parseUser(JSONObject object) throws JSONException {
        User current = new User();
        if (object.has("uid")) {
            current.uid = object.getString("uid");
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

    protected Album parseAlbum(JSONObject object) throws JSONException {
        Album current = new Album();
        if (object.has("aid")) {
            current.aid = object.getString("aid");
        }
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
        return current;
    }

    protected Group parseGroup(JSONObject object) throws JSONException {
        Group current = new Group();
        if (object.has("uid")) {
            current.uid = object.getString("uid");
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

    protected Photo parsePhoto(JSONObject object) throws JSONException {
        Photo current = new Photo();
        if (object.has("id")) {
            current.id = object.getString("id");
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

    protected List<String> getGroupIds() {
        Map<String, String> requestParams = new HashMap<String, String>();
        List<String> result = new ArrayList<>();
        boolean hasMore = true;
        while (hasMore) {
            try {
                JSONObject groupsObject = new JSONObject(api.request("group.getUserGroupsV2", requestParams, "get"));
                if (groupsObject.isNull("groups")) {
                    hasMore = false;
                } else {
                    JSONArray groups = groupsObject.getJSONArray("groups");
                    for (int i = 0; i < groups.length(); ++i) {
                        result.add(groups.getJSONObject(i).getString("groupId"));
                    }
                    requestParams.put("anchor", groupsObject.getString("anchor"));
                }
            } catch (Exception e) {
                Console.print(e.getMessage());
                hasMore = false;
            }
        }
        return result;
    }

    protected List<Group> getGroupInfo(List<String> groupIds) {
        final int MAX_REQUEST = 100;
        List<Group> result = new ArrayList<>();
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("fields", "uid, name, description, shortname, pic_avatar, photo_id, " +
                "shop_visible_admin, shop_visible_public, members_count");
        for (int i = 0; i < groupIds.size() / MAX_REQUEST + 1; ++i) {
            StringBuilder builder = new StringBuilder();
            for (int j = i * MAX_REQUEST; j < Math.min((i + 1) * MAX_REQUEST, groupIds.size()); j++) {
                builder.append(",").append(groupIds.get(j));
            }
            requestParams.put("uids", builder.substring(1));
            try {
                JSONArray groupInfoArray = new JSONArray(api.request("group.getInfo", requestParams, "get"));
                for (int j = 0; j < groupInfoArray.length(); ++j) {
                    result.add(parseGroup(groupInfoArray.getJSONObject(j)));
                }
            } catch (Exception e) {
                Console.print(e.getMessage());
            }
        }
        return result;
    }

    protected List<String> getFriendIDs() {
        List<String> result = new ArrayList<String>();
        try {
            JSONArray friendIDs = new JSONArray(api.request("friends.get", null, "get"));
            for (int i = 0; i < friendIDs.length(); i++) {
                result.add(friendIDs.getString(i));
            }
        } catch (Exception e) {
            Console.print(e.getMessage());
        }
        return result;
    }

    protected List<User> getUserInfo(List<String> usersIds) {
        final int MAX_REQUEST = 100;
        List<User> result = new ArrayList<>();
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("fields", "uid, locale, first_name, last_name, name, gender, age, " +
                "birthday, has_email, location, current_location, current_status, current_status_id, " +
                "current_status_date, online, last_online, photo_id, pic_1, pic_2, pic_3, pic_4, pic_5, " +
                "pic50x50, pic128x128, pic128max, pic180min, pic240min, pic320min, pic190x190, pic640x480, " +
                "pic1024x768, url_profile, url_chat, url_profile_mobile, url_chat_mobile, can_vcall, " +
                "can_vmail, allows_anonym_access, allows_messaging_only_for_friends, registered_date, has_service_invisible");
        for (int i = 0; i < usersIds.size() / MAX_REQUEST + 1; ++i) {
            StringBuilder builder = new StringBuilder();
            for (int j = i * MAX_REQUEST; j < Math.min((i + 1) * MAX_REQUEST, usersIds.size()); ++j) {
                builder.append(",").append(usersIds.get(j));
            }
            requestParams.put("uids", builder.substring(1));
            try {
                JSONArray friendInfoArray = new JSONArray(api.request("users.getInfo", requestParams, "get"));
                for (int j = 0; j < friendInfoArray.length(); ++j) {
                    result.add(parseUser(friendInfoArray.getJSONObject(j)));
                }
            } catch (Exception e) {
                Console.print(e.getMessage());
            }
        }
        return result;
    }

    /**
     * @param fid friend ID
     * @param gid group ID
     * @return list of albums
     */
    protected List<Album> getAlbums(String fid, String gid) {
        Console.print("Start loading");
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("fields", "album.*");
        if (fid != null) {
            requestParams.put("fid", fid);
        }
        if (gid != null) {
            requestParams.put("gid", gid);
        }
        List<Album> result = new ArrayList<>();
        boolean hasMore = true;
        while (hasMore) {
            try {
                JSONObject albumsObject = new JSONObject(api.request("photos.getAlbums", requestParams, "get"));
                JSONArray albums = albumsObject.getJSONArray("albums");
                for (int i = 0; i < albums.length(); ++i) {
                    Album album = parseAlbum(albums.getJSONObject(i));
                    result.add(album);
                }
                hasMore = albumsObject.getBoolean("hasMore");
                requestParams.put("anchor", albumsObject.getString("pagingAnchor"));
            } catch (Exception e) {
                Console.print(e.getMessage());
                hasMore = false;
            }
        }
        return result;
    }

    protected Photo getPhoto(String photo_id, String gid) {
        Map<String, String> requestParams = new HashMap<String, String>();
        if (photo_id != null) {
            requestParams.put("photo_id", photo_id);
        }
        if (gid != null) {
            requestParams.put("gid", gid);
        }
        requestParams.put("fields", "group_photo.*");
        Photo result = null;
        try {
            String response = api.request("photos.getPhotoInfo", requestParams, "get");
            JSONObject photosObject = new JSONObject(response);
            result = parsePhoto(photosObject.getJSONObject("photo"));
        } catch (Exception e) {
            Console.print("Error " + e.getMessage());
        }
        return result;
    }

    protected List<Photo> getAlbumPhotos(String fid, String gid, String aid) {
        Map<String, String> requestParams = new HashMap<String, String>();
        if (fid != null) {
            requestParams.put("fid", fid);
        }
        if (gid != null) {
            requestParams.put("gid", gid);
        }
        if (aid != null) {
            requestParams.put("aid", aid);
        }
        requestParams.put("fields", "photo.*");
        List<Photo> result = new ArrayList<Photo>();
        boolean hasMore = true;
        while (hasMore) {
            try {
                JSONObject photosObject = new JSONObject(api.request("photos.getPhotos", requestParams, "get"));
                JSONArray photos = photosObject.getJSONArray("photos");
                for (int i = 0; i < photos.length(); ++i) {
                    Photo photo = parsePhoto(photos.getJSONObject(i));
                    result.add(photo);
                }
                hasMore = photosObject.getBoolean("hasMore");
                if (hasMore) {
                    requestParams.put("anchor", photosObject.getString("anchor"));
                }
            } catch (Exception e) {
                Console.print(e.getMessage());
                hasMore = false;
            }
        }
        //Collections.sort(result, new InfoHolder.PhotoByUploadTimeComparator());
        return result;
    }

    @Override
    public void addEventListener(IEventHadler listener) {
        eventDispatcher.addEventListener(listener);
    }

    @Override
    public void removeEventListener(IEventHadler listener) {
        eventDispatcher.removeEventListener(listener);
    }

    @Override
    public void dispatchEvent(Event e) {
        eventDispatcher.dispatchEvent(e);
    }
}
