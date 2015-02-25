package ru.example.PhotoStream;

import android.os.AsyncTask;
import ru.example.PhotoStream.Activities.UIActivity;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SortedFeed extends Feed {

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
            List<Photo> chunk = new ArrayList<>(currentLoadCount);
            for (Map.Entry<String, AlbumHolder> entry : albums.entrySet()) {
                AlbumHolder album = entry.getValue();
                if (album.hasMore() && album.getPhotosCount() == 0) {
                    heap.addAll(album.loadNextChunk(UIActivity.getAPI(), 1));
                }
            }
            for (int i = 0; heap.size() > 0 && i < currentLoadCount; ++i) {
                Photo photo = heap.poll();
                chunk.add(photo);
                AlbumHolder album = albums.get(photo.album_id);
                if (album.hasMore() && album.getLastLoadedPhoto() == photo) {
                    heap.addAll(album.loadNextChunk(UIActivity.getAPI(), DEFAULT_CHUNK_SIZE));
                }
            }
            return chunk;
        }

        @Override
        protected void onPostExecute(List<Photo> chunk) {
            for (Photo photo : chunk) {
                toDisplay.add(photo);
            }
            dispatchEvent(Event.CHANGE, null);
            isRunning.set(false);
        }

    }

    public final static int DEFAULT_LOAD_COUNT = 100;
    private final static int DEFAULT_CHUNK_SIZE = 10;

    protected Map<String, AlbumHolder> albums = new HashMap<>();
    protected PriorityQueue<Photo> heap = new PriorityQueue<>(1, new PhotoByUploadTimeComparator());
    protected List<Photo> toDisplay = new ArrayList<>();
    protected int currentLoadCount;
    protected AtomicBoolean isRunning = new AtomicBoolean(false);

    public SortedFeed(String name) {
        this(name, DEFAULT_LOAD_COUNT);
    }

    public SortedFeed(String name, int loadCount) {
        super(name);
        this.currentLoadCount = loadCount;
    }

    public void clear() {
        for (Map.Entry<String, AlbumHolder> entry : albums.entrySet()) {
            entry.getValue().clear();
        }
        toDisplay.clear();
        heap.clear();
    }

    public List<Photo> getAvailablePhotos() {
        return toDisplay;
    }

    public void loadMore() {
        if (isRunning.compareAndSet(false, true)) {
            Loader loader = new Loader();
            loader.execute();
        }
    }

    public void addAll(Collection<? extends Album> albums) {
        for (Album album : albums) {
            add(album);
        }
    }

    public void add(Album album) {
        albums.put(album.getId(), new AlbumHolder(album));
    }
}
