package ru.example.PhotoStream;

import ru.ok.android.sdk.Odnoklassniki;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Feed {

    private class PhotoByUploadTimeComparator implements Comparator<Photo> {

        @Override
        public int compare(Photo lhs, Photo rhs) {
            long l = lhs.created_ms, r = rhs.created_ms;
            if (l > r) {
                return -1;
            } else if (l < r) {
                return 1;
            }

            return lhs.id.compareTo(rhs.id);
        }
    }

    public final static int LOAD_COUNT = 30;

    private List<Album> albums = new ArrayList<>();
    private PriorityQueue<Photo> heap = new PriorityQueue<>(1, new PhotoByUploadTimeComparator());
    private List<Photo> toDisplay = new ArrayList<>();
    private Odnoklassniki api;

    public Feed(Odnoklassniki api) {
        this.api = api;
    }

    public List<Photo> getAvailablePhotos() {
        update();
        return toDisplay;
    }

    private void update() {
        for (Album album : albums) {
            if (album.hasMore() && album.chunksCount() == 0) {
                album.loadNextChunk(api, 1);
                heap.addAll(album.getLastChunk());
            }
        }
        for (int i = 0; heap.size() > 0 && i < LOAD_COUNT; ++i) {
            Photo photo = heap.poll();
            toDisplay.add(photo);
            Album album = Album.get(photo.album_id);
            if (album.hasMore() && album.getLastLoadedPhoto() == photo) {
                album.loadNextChunk(api);
                heap.addAll(album.getLastChunk());
            }
        }
    }

    public boolean hasMore() {
        return heap.size() > 0;
    }

    public void add(Album album) {
        albums.add(album);
        for (int i = 0; i < album.chunksCount(); ++i) {
            heap.addAll(album.getChunk(i));
        }
    }
}
