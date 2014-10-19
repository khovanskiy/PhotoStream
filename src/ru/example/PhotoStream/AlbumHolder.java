package ru.example.PhotoStream;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlbumHolder {
    private Album album;
    private boolean hasMore = true;
    private String lastAnchor = "";
    private int photosCount = 0;
    private Photo lastLoadedPhoto = null;

    public AlbumHolder(Album album) {
        this.album = album;
    }

    public Album getAlbum() {
        return album;
    }

    public boolean hasMore() {
        return hasMore;
    }

    public int getPhotosCount() {
        return photosCount;
    }

    public Photo getLastLoadedPhoto() {
        return lastLoadedPhoto;
    }

    public void clear() {
        photosCount = 0;
        lastLoadedPhoto = null;
        hasMore = true;
        lastAnchor = "";
    }

    public List<Photo> loadNextChunk(Odnoklassniki api, int count) {
        Map<String, String> requestParams = new HashMap<>();
        if (album.albumType == AlbumType.USER) {
            requestParams.put("fid", album.user_id);
            requestParams.put("fields", "photo.id, photo.created_ms, photo.user_id, photo.text, photo.pic50x50, photo.pic128x128, photo.pic190x190, photo.pic640x480, photo.pic1024x768, photo.liked_it, photo.like_count, photo.comments_count");
        } else if (album.albumType == AlbumType.GROUP) {
            requestParams.put("gid", album.group_id);
            requestParams.put("fields", "group_photo.id, group_photo.created_ms, group_photo.user_id, group_photo.text, group_photo.pic50x50, group_photo.pic128x128, group_photo.pic190x190, group_photo.pic640x480, group_photo.pic1024x768, group_photo.liked_it, group_photo.like_count, group_photo.comments_count");
        }
        if (!album.isPersonal) {
            requestParams.put("aid", album.objectId);
        }
        requestParams.put("count", count + "");
        List<Photo> chunk = new ArrayList<>();
        if (hasMore) {
            if (lastAnchor.length() > 0) {
                requestParams.put("anchor", lastAnchor);
            }
            try {
                String response = api.request("photos.getPhotos", requestParams, "get");
                Console.print("Photo: " + response);
                JSONObject photosObject = new JSONObject(response);
                JSONArray photos = photosObject.getJSONArray("photos");
                for (int i = 0; i < photos.length(); ++i) {
                    Photo photo = Photo.build(photos.getJSONObject(i));
                    photo.album_id = album.getId();
                    chunk.add(photo);
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
        }
        photosCount += chunk.size();
        return chunk;
    }
}
