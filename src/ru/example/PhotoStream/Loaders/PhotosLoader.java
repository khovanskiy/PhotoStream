package ru.example.PhotoStream.Loaders;

import ru.example.PhotoStream.*;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.ArrayList;
import java.util.List;

public class PhotosLoader extends DataLoader
{
    private String uid = "";
    private int type = 0;

    public PhotosLoader(Odnoklassniki api, String uid, int type) {
        super(api);
        this.uid = uid;
        this.type = type;
    }

    @Override
    protected List<?> doInBackground(Void... params) {
        if (type == 0) {
            List<Album> albums = getAlbums(uid, null);
            List<Photo> result = new ArrayList<>();
            for (Album album : albums) {
                List<Photo> photos = getAlbumPhotos(uid, null, album.aid);
                result.addAll(photos);
            }
            return result;
        }
        else {
            Console.print("Group`s photos");
            List<Album> albums = getAlbums(null, uid);
            Console.print("Albums: " + albums.size());
            List<Photo> result = new ArrayList<>();
            int sum = 0;
            for (Album album : albums) {
                List<Photo> photos = getAlbumPhotos(null, uid, album.aid);
                result.addAll(photos);
                sum += photos.size();
            }
            Console.print("Photos: " + sum);
            return result;
        }
    }

    @Override
    protected void onPostExecute(List<?> data) {
        Event e = new Event(this, Event.COMPLETE);
        e.data.put("photos", data);
        dispatchEvent(e);
    }
}
