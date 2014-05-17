package ru.example.PhotoStream;

import ru.ok.android.sdk.Odnoklassniki;

import java.util.List;

public abstract class AlbumsKeeper {
    public abstract List<Album> getAlbums();

    public abstract void loadAlbums(Odnoklassniki api);
}
