package ru.example.PhotoStream;


import android.os.AsyncTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.*;

public class DataLoader extends AsyncTask<Void, Void, List<?>> implements IEventDispatcher {
    private EventDispatcher eventDispatcher;
    private Odnoklassniki api;

    public DataLoader(Odnoklassniki api)
    {
        this.api = api;
        eventDispatcher = new EventDispatcher();
    }

    @Override
    protected List<?> doInBackground(Void... params) {
        List<Album> albums = getAlbums(null, null);
        List<Photo> result = new ArrayList<Photo>();
        for (Album album : albums)
        {
            List<Photo> photos = getAlbumPhotos(null, null, album.aid);
            result.addAll(photos);
        }
        return result;
    }

    private Album parseAlbum(JSONObject object) throws JSONException {
        Album current = new Album();
        if (object.has("aid"))
        {
            current.aid = object.getString("aid");
        }
        if (object.has("title"))
        {
            current.title = object.getString("title");
        }
        if (object.has("description"))
        {
            current.description = object.getString("description");
        }
        if (object.has("created"))
        {
            current.created = object.getString("created");
        }
        if (object.has("type"))
        {
            current.type = object.getString("type");
        }
        return current;
    }

    private Photo parsePhoto(JSONObject object) throws JSONException {
        Photo current = new Photo();
        if (object.has("id"))
        {
            current.id = object.getString("id");
        }
        if (object.has("album_id"))
        {
            current.album_id = object.getString("album_id");
        }
        if (object.has("pic50x50"))
        {
            current.pic50x50 = object.getString("pic50x50");
        }
        if (object.has("pic128x128"))
        {
            current.pic128x128 = object.getString("pic128x128");
        }
        if (object.has("pic190x190"))
        {
            current.pic190x190 = object.getString("pic190x190");
        }
        if (object.has("pic640x480"))
        {
            current.pic640x480 = object.getString("pic640x480");
        }
        if (object.has("pic1024x768"))
        {
            current.pic1024x768 = object.getString("pic1024x768");
        }
        if (object.has("comments_count"))
        {
            current.comments_count = object.getInt("comments_count");
        }
        if (object.has("user_id"))
        {
            current.user_id = object.getString("user_id");
        }
        if (object.has("mark_count"))
        {
            current.mark_count = object.getInt("mark_count");
        }
        if (object.has("mark_bonus_count"))
        {
            current.mark_bonus_count = object.getInt("mark_bonus_count");
        }
        if (object.has("mark_avg"))
        {
            current.mark_avg = object.getDouble("mark_avg");
        }
        return current;
    }

    /**
     *
     * @param fid friend ID
     * @param gid group ID
     * @return list of albums
     */
    private List<Album> getAlbums(String fid, String gid) {
        Console.print("Start loading");
        Map<String, String> requestParams = new HashMap<String, String>();
        requestParams.put("fields", "album.*");
        if (fid != null) {
            requestParams.put("fid", fid);
        }
        if (gid != null) {
            requestParams.put("gid", gid);
        }
        List<Album> result = new ArrayList<Album>();
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

    private List<Photo> getAlbumPhotos(String fid, String gid, String aid) {
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
    protected void onPostExecute(List<?> data) {
        Event e = new Event(this, Event.COMPLETE);
        e.data.put("photos", data);
        dispatchEvent(e);
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
