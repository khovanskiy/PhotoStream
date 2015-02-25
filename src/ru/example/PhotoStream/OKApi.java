package ru.example.PhotoStream;

public class OKApi {
    public static OKApiUsers users() {
        return new OKApiUsers();
    }

    public static OKApiFriends friends() {
        return new OKApiFriends();
    }

    public static OKApiGroups groups() {
        return new OKApiGroups();
    }

    public static OKApiAlbums albums() {
        return new OKApiAlbums();
    }

    public static OKApiPhotos photos() {
        return new OKApiPhotos();
    }
}
