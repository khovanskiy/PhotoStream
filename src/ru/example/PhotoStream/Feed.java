package ru.example.PhotoStream;

import android.os.AsyncTask;
import android.util.Log;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.*;
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

    private class AlbumHolder {

        private Album album;
        public int chunksAdded;

        public AlbumHolder(Album album) {
            this.album = album;
        }

        public Album getAlbum() {
            return album;
        }

    }

    private class Loader extends AsyncTask<Void, Void, List<Photo>> {
        @Override
        protected List<Photo> doInBackground(Void... params) {
            List<Photo> chunk = new ArrayList<>(currentLoadCount);
            try {
                for (Map.Entry<String, AlbumHolder> entry : albums.entrySet()) {
                    AlbumHolder holder = entry.getValue();
                    Album album = holder.getAlbum();
                    synchronized (album) {
                        boolean shouldBeUpdatedOwn = true;
                        while (album.isRunning()) {
                            shouldBeUpdatedOwn = false;
                            album.wait();
                        }
                        if (shouldBeUpdatedOwn && album.hasMore() && album.chunksCount() == 0) {
                            album.loadNextChunk(api);
                        }
                        if (holder.chunksAdded < album.chunksCount()) {
                            heap.addAll(album.getChunk(holder.chunksAdded));
                            ++holder.chunksAdded;
                        }
                        if (shouldBeUpdatedOwn) {
                            album.notifyAll();
                        }
                    }
                }

                for (int i = 0; heap.size() > 0 && i < currentLoadCount; ++i) {
                    Photo photo = heap.poll();
                    chunk.add(photo);
                    AlbumHolder holder = albums.get(photo.album_id);
                    Album album = holder.getAlbum();
                    synchronized (album) {
                        if (album.getLastLoadedPhoto() == photo) {
                            continue;
                        }
                        boolean shouldBeUpdatedOwn = true;
                        while (album.isRunning()) {
                            shouldBeUpdatedOwn = false;
                            album.wait();
                        }
                        if (shouldBeUpdatedOwn && album.hasMore()) {
                            album.loadNextChunk(api);
                        }
                        if (holder.chunksAdded < album.chunksCount()) {
                            heap.addAll(album.getChunk(holder.chunksAdded));
                            ++holder.chunksAdded;
                        }
                        if (shouldBeUpdatedOwn) {
                            album.notifyAll();
                        }
                    }
                }
            }
            catch (Exception e) {
                Log.d("FEED_CONSOLE", e.getMessage(), e);
            }
            return chunk;
        }

        @Override
        protected void onPostExecute(List<Photo> chunk) {
            for (Photo photo : chunk) {
                toDisplay.add(photo);
            }
            dispatchEvent(new Event(Feed.this, Event.COMPLETE));
            isRunning.compareAndSet(true, false);
        }
    }

    public final static int DEFAULT_LOAD_COUNT = 100;

    protected Map<String, AlbumHolder> albums = new HashMap<>();
    protected PriorityQueue<Photo> heap = new PriorityQueue<>(1, new PhotoByUploadTimeComparator());
    protected List<Photo> toDisplay = new ArrayList<>();
    protected Odnoklassniki api;
    protected int currentLoadCount;
    protected AtomicBoolean isRunning = new AtomicBoolean(false);

    public Feed(Odnoklassniki api) {
        this(api, DEFAULT_LOAD_COUNT);
    }

    public Feed(Odnoklassniki api, int loadCount) {
        this.api = api;
        this.currentLoadCount = loadCount;
    }

    public void clear() {
        for (Map.Entry<String, AlbumHolder> entry : albums.entrySet()) {
            AlbumHolder holder = entry.getValue();
            Album album = holder.getAlbum();
            album.clear();
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

    public void addAll(List<Album> albums) {
        for (Album album : albums) {
            add(album);
        }
    }

    public void add(Album album) {
        albums.put(album.getId(), new AlbumHolder(album));
    }
}
