package ru.example.PhotoStream.Loaders;

import ru.example.PhotoStream.Album;
import ru.example.PhotoStream.DataLoader;
import ru.example.PhotoStream.Event;
import ru.example.PhotoStream.Photo;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.ArrayList;
import java.util.List;

public class PhotosLoader extends DataLoader
{
    public PhotosLoader(Odnoklassniki api) {
        super(api);
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

    @Override
    protected void onPostExecute(List<?> data) {
        Event e = new Event(this, Event.COMPLETE);
        e.data.put("photos", data);
        dispatchEvent(e);
    }
}
