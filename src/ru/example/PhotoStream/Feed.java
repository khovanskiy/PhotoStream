package ru.example.PhotoStream;

import java.util.List;

public interface Feed extends IEventDispatcher {
    public void clear();

    public List<Photo> getAvailablePhotos();

    public void loadMore();

    public void addAll(List<Album> albums);

    public void add(Album album);
}
