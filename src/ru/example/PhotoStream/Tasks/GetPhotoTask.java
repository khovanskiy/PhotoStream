package ru.example.PhotoStream.Tasks;

import org.json.JSONObject;
import ru.example.PhotoStream.AlbumsOwner;
import ru.example.PhotoStream.Group;
import ru.example.PhotoStream.Photo;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class GetPhotoTask implements Callable<Photo> {

    private final Odnoklassniki api;
    private final String photo_id;
    private final AlbumsOwner owner;

    public GetPhotoTask(Odnoklassniki api, String photoId, AlbumsOwner owner) {
        this.api = api;
        this.photo_id = photoId;
        this.owner = owner;
    }

    @Override
    public Photo call() {
        assert (photo_id != null);
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("photo_id", photo_id);
        if (owner instanceof Group) {
            requestParams.put("gid", owner.getId());
            requestParams.put("fields", "group_photo.*");
        } else {
            requestParams.put("fid", owner.getId());
            requestParams.put("fields", "user_photo.*");
        }
        Photo result = null;
        try {
            String response = api.request("photos.getPhotoInfo", requestParams, "get");
            JSONObject photosObject = new JSONObject(response);
            result = Photo.build(photosObject.getJSONObject("photo"));
        } catch (Exception e) {
        }
        return result;
    }
}
