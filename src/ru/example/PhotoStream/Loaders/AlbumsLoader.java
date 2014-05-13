package ru.example.PhotoStream.Loaders;


import ru.example.PhotoStream.AlbumsKeeper;
import ru.example.PhotoStream.DataLoader;
import ru.example.PhotoStream.Event;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.List;

public class AlbumsLoader extends DataLoader {

    private AlbumsKeeper entry;

    public AlbumsLoader(Odnoklassniki api, AlbumsKeeper entry) {
        super(api);
        this.entry = entry;
    }

    @Override
    protected List<?> doInBackground(Void... params) {
        entry.loadAlbums(api);
        return entry.getAlbums();
    }

    @Override
    protected void onPostExecute(List<?> data) {
        Event e = new Event(this, Event.ALBUMS_LOADED);
        e.data.put("albums", data);
        dispatchEvent(e);
    }
}
