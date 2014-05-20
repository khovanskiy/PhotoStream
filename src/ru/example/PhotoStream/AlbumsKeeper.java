package ru.example.PhotoStream;

import ru.ok.android.sdk.Odnoklassniki;

import java.io.Serializable;
import java.util.List;

public abstract class AlbumsKeeper implements Serializable {
    public abstract List<Album> getAlbums();

    public abstract void loadAlbums(Odnoklassniki api);
}
