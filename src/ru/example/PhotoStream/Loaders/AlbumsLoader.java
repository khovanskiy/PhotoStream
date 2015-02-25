package ru.example.PhotoStream.Loaders;


import ru.example.PhotoStream.Album;
import ru.example.PhotoStream.AlbumsOwner;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.List;
import java.util.concurrent.Callable;

public class AlbumsLoader implements Callable<List<Album>> {

    private Odnoklassniki api;
    private AlbumsOwner entry;

    public AlbumsLoader(Odnoklassniki api, AlbumsOwner entry) {
        this.api = api;
        this.entry = entry;
    }

    @Override
    public List<Album> call() throws Exception {
        //entry.loadAlbums(api);
        return entry.getAlbums();
    }
}
