package ru.example.PhotoStream;

import ru.ok.android.sdk.Odnoklassniki;

import java.util.*;

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

    public final static int INITIAL_PHOTOS_COUNT = 10;

    private List<Album> albums = new ArrayList<>();
    private TreeSet<Photo> heap = new TreeSet<>(new PhotoByUploadTimeComparator());
    private Odnoklassniki api;

    public Feed(Odnoklassniki api) {
        this.api = api;
    }

    public List<Photo> getAvailablePhotos() {
        for (Album album : albums) {
            if (album.hasMore() && album.chunksCount() == 0) {
                album.loadNextChunk(api);
                heap.addAll(album.getLastChunk());
            }
        }
        List<Photo> photos = new ArrayList<>(heap.size());
        update();
        Console.print("Heap size: " + heap.size());
        for (Iterator<Photo> i = heap.iterator(); i.hasNext();) {
            Photo photo = i.next();
            photos.add(photo);
        }
        return photos;
    }

    private void update() {
        List<List<Photo>> list = new ArrayList<>();
        for (Iterator<Photo> i = heap.iterator(); i.hasNext();) {
            Photo photo = i.next();
            Album album = Album.get(photo.album_id);
            if (album.hasMore() && album.getLastLoadedPhoto() == photo) {
                album.loadNextChunk(api);
                list.add(album.getLastChunk());
            }
        }
        for (List<Photo> chunk : list) {
            heap.addAll(chunk);
        }
    }

    public boolean hasMore() {
        return true;
    }

    public void add(Album album) {
        albums.add(album);
        for (int i = 0; i < album.chunksCount(); ++i) {
            heap.addAll(album.getChunk(i));
        }
    }
}
