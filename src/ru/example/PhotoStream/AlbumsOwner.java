package ru.example.PhotoStream;

import android.os.Parcel;
import android.os.Parcelable;
import ru.ok.android.sdk.Odnoklassniki;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class AlbumsOwner extends OKApiObject implements Serializable {
    protected List<Album> albums = new ArrayList<>();

    /**
     * Returns loaded albums.
     * @return list of albums
     */
    public List<Album> getAlbums() {
        return albums;
    }

    public void setAlbums(List<Album> albums) {
        this.albums = albums;
    }

    /**
     * Loads necessary albums from the server.
     * @param api
     */
    //public abstract void loadAlbums(Odnoklassniki api);

    /**
     * Gets owner`s avatar URL
     *
     * @return avatar URL
     */
    public abstract Photo getAvatar();

    public abstract String getName();
}
