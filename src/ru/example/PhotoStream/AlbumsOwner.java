package ru.example.PhotoStream;

import ru.ok.android.sdk.Odnoklassniki;

import java.io.Serializable;
import java.util.List;

public abstract class AlbumsOwner extends OKApiObject implements Serializable {
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

    /**
     * Gets owner`s avatar URL
     *
     * @return avatar URL
     */
    public abstract Photo getAvatar();
}
