package ru.example.PhotoStream.Tasks;

import android.util.Log;
import org.json.JSONObject;
import ru.example.PhotoStream.AlbumsKeeper;
import ru.example.PhotoStream.Photo;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.HashMap;
import java.util.Map;

public class GetPhotoTask implements Task<Photo> {

    private final String photo_id;
    private final AlbumsKeeper owner;

    public GetPhotoTask(String photoId, AlbumsKeeper owner) {
        this.photo_id = photoId;
        this.owner = owner;
    }

    @Override
    public Photo execute(Odnoklassniki api) {
        Map<String, String> requestParams = new HashMap<>();
        if (photo_id != null) {
            requestParams.put("photo_id", photo_id);
        }
        requestParams.put("gid", owner.getId());
        requestParams.put("fields", "group_photo.*");
        Photo result = null;
        try {
            String response = api.request("photos.getPhotoInfo", requestParams, "get");
            JSONObject photosObject = new JSONObject(response);
            result = Photo.build(photosObject.getJSONObject("photo"));
        } catch (Exception e) {
            Log.d("TASK_ERROR", e.getMessage(), e);
        }
        return result;
    }
}
