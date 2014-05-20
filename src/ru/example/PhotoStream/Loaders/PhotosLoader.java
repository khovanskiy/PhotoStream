package ru.example.PhotoStream.Loaders;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import ru.example.PhotoStream.DataLoader;
import ru.example.PhotoStream.Event;
import ru.example.PhotoStream.Feed;
import ru.example.PhotoStream.Photo;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.List;

public class PhotosLoader extends DataLoader {

    private Feed feed;

    public PhotosLoader(Odnoklassniki api, Feed feed) {
        super(api);
        this.feed = feed;
    }

    @Override
    protected List<?> doInBackground(Void... params) {
        feed.loadMore();
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
