package ru.example.PhotoStream;

import ru.ok.android.sdk.Odnoklassniki;

import java.io.Serializable;
import java.util.List;

public abstract class AlbumsKeeper implements Serializable {
    /**
     * Returns loaded albums.
     * @return list of albums
     */
    public abstract List<Album> getAlbums();

    /**
     * Loads necessary albums from the server.
     * @param api
     */
    public abstract void loadAlbums(Odnoklassniki api);
}
