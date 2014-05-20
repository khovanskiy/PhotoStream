package ru.example.PhotoStream;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.ok.android.sdk.Odnoklassniki;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Album extends Entry implements Serializable {

    private class Chunk implements Serializable {
        public String anchor = "";
        public List<Photo> photos = new ArrayList<>();

        public int size() {
            return photos.size();
        }

        public void clear() {
            photos.clear();
        }
    }

    public String aid = "";
    public String user_id = "";
    public String group_id = "";
    public String title = "";
    public String description = "";
    public String created = "";
    public String type = "";
    public AlbumType albumType = AlbumType.USER;

    private static Map<String, Album> cache = new ConcurrentHashMap<>();

    public String lastAnchor = "";
    public List<Chunk> chunks = new ArrayList<>();
    private boolean hasMore = true;
    private Photo lastLoadedPhoto = null;

    private Album() {

    }

    public static Album get(String albumId) {
        Album current;
        if (!cache.containsKey(albumId)) {
            current = new Album();
            cache.put(albumId, current);
        } else {
            current = cache.get(albumId);
        }
        return current;
    }

    public static Album build(JSONObject object) throws JSONException {
        String currentId;
        if (object.has("aid")) {
            currentId = object.getString("aid");
        } else {
            throw new JSONException("Album object does not have ID");
        }
        Album current = get(currentId);
        current.aid = currentId;

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

    @Override
    public boolean hasMore() {
        return hasMore;
    }

    @Override
    public boolean loadNextChunk(Odnoklassniki api, int count) {
        Map<String, String> requestParams = new HashMap<>();
        if (albumType == AlbumType.USER) {
            requestParams.put("fid", user_id);
            requestParams.put("fields", "photo.*");
        } else if (albumType == AlbumType.GROUP) {
            requestParams.put("gid", group_id);
            requestParams.put("fields", "group_photo.*");
        }
        requestParams.put("aid", aid);
        requestParams.put("count", count + "");

        if (hasMore) {
            Chunk chunk = new Chunk();
            chunk.anchor = lastAnchor;
            if (lastAnchor.length() > 0) {
                requestParams.put("anchor", lastAnchor);
            }
            try {
                //Console.print("Prepared request: " + requestParams.toString());
                String response = api.request("photos.getPhotos", requestParams, "get");
                JSONObject photosObject = new JSONObject(response);
                JSONArray photos = photosObject.getJSONArray("photos");
                for (int i = 0; i < photos.length(); ++i) {
                    Photo photo = Photo.build(photos.getJSONObject(i));
                    chunk.photos.add(photo);
                    lastLoadedPhoto = photo;
                }
                hasMore = photosObject.getBoolean("hasMore");
                if (hasMore) {
                    lastAnchor = photosObject.getString("anchor");
                }
            } catch (Exception e) {
                Log.i("CONSOLE", e.toString(), e);
                hasMore = false;
            }
            //Console.print("Album " + title + " added chunk with " + chunk.photos.size() + " photos");
            chunks.add(chunk);
            return true;
        }
        return false;
    }

    public Photo getLastLoadedPhoto() {
        return lastLoadedPhoto;
    }

    public List<Photo> getChunk(int location) {
        return chunks.get(location).photos;
    }

    public int chunksCount() {
        return chunks.size();
    }

    @Override
    public List<Photo> getLastChunk() {
        if (chunks.size() > 0) {
            return chunks.get(chunks.size() - 1).photos;
        }
        return Collections.emptyList();
    }
}
