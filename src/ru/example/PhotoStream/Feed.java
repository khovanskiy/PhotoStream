package ru.example.PhotoStream;

import android.os.AsyncTask;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Feed extends EventDispatcher {

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

    private class Loader extends AsyncTask<Void, Void, List<Photo>> {
        @Override
        protected List<Photo> doInBackground(Void... params) {
            for (Album album : albums) {
                if (album.hasMore() && album.chunksCount() == 0) {
                    album.loadNextChunk(api, 1);
                    heap.addAll(album.getLastChunk());
                }
            }
            List<Photo> chunk = new ArrayList<>(currentLoadCount);
            for (int i = 0; heap.size() > 0 && i < currentLoadCount; ++i) {
                Photo photo = heap.poll();
                chunk.add(photo);
                Album album = Album.get(photo.album_id);
                if (album.hasMore() && album.getLastLoadedPhoto() == photo) {
                    album.loadNextChunk(api);
                    heap.addAll(album.getLastChunk());
                }
            }
            return chunk;
        }

        @Override
        protected void onPostExecute(List<Photo> chunk) {
            for (Photo photo : chunk) {
                toDisplay.add(photo);
            }
            dispatchEvent(new Event(Feed.this, Event.COMPLETE));
            running.compareAndSet(true, false);
        }
    }

    public final static int DEFAULT_LOAD_COUNT = 100;

    protected List<Album> albums = new ArrayList<>();
    protected PriorityQueue<Photo> heap = new PriorityQueue<>(1, new PhotoByUploadTimeComparator());
    protected List<Photo> toDisplay = new ArrayList<>();
    protected Odnoklassniki api;
    protected int currentLoadCount;
    protected AtomicBoolean running = new AtomicBoolean(false);

    public Feed(Odnoklassniki api) {
        this(api, DEFAULT_LOAD_COUNT);
    }

    public Feed(Odnoklassniki api, int loadCount) {
        this.api = api;
        this.currentLoadCount = loadCount;
    }

    public List<Photo> getAvailablePhotos() {
        return toDisplay;
    }

    public void resetList() {
        toDisplay.clear();
    }

    public void loadMore() {
        if (running.compareAndSet(false, true)) {
            Loader loader = new Loader();
            loader.execute();
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
