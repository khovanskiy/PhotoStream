package ru.example.PhotoStream.Loaders;

import ru.example.PhotoStream.*;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.*;

public class PhotosLoader extends DataLoader {

    private Feed feed;

    public PhotosLoader(Odnoklassniki api, Feed feed) {
        super(api);
        this.feed = feed;
    }

    @Override
    protected List<?> doInBackground(Void... params) {
        List<Photo> result = feed.getAvailablePhotos();
        return result;
    }

    @Override
    protected void onPostExecute(List<?> data) {
        Event e = new Event(this, Event.COMPLETE);
        e.data.put("photos", data);
        dispatchEvent(e);
    }
}
