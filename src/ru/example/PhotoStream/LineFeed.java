package ru.example.PhotoStream;

import android.os.AsyncTask;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class LineFeed extends EventDispatcher implements Feed {

    private class Loader extends AsyncTask<Void, Void, List<Photo>> {
        @Override
        protected List<Photo> doInBackground(Void... params) {
            List<Photo> chunk = new ArrayList<>(currentLoadCount);
            int toLoad = currentLoadCount;
            for (Map.Entry<String, AlbumHolder> entry : albums.entrySet()) {
                AlbumHolder album = entry.getValue();
                while (toLoad > 0 && album.hasMore()) {
                    List<Photo> photos = album.loadNextChunk(api, DEFAULT_CHUNK_SIZE);
                    toLoad -= photos.size();
                    chunk.addAll(photos);
                }
            }
            return chunk;
        }

        @Override
        protected void onPostExecute(List<Photo> chunk) {
            Console.print("LineFeed.Loader.onPostExecute");
            for (Photo photo : chunk) {
                toDisplay.add(photo);
            }
            dispatchEvent(new Event(LineFeed.this, Event.COMPLETE));
            isRunning.set(false);
        }

    }

    public final static int DEFAULT_LOAD_COUNT = 100;
    private final static int DEFAULT_CHUNK_SIZE = 10;

    protected Map<String, AlbumHolder> albums = new HashMap<>();
    protected List<Photo> toDisplay = new ArrayList<>();
    protected Odnoklassniki api;
    protected int currentLoadCount;
    protected AtomicBoolean isRunning = new AtomicBoolean(false);

    public LineFeed(Odnoklassniki api) {
        this(api, DEFAULT_LOAD_COUNT);
    }

    public LineFeed(Odnoklassniki api, int loadCount) {
        this.api = api;
        this.currentLoadCount = loadCount;
    }

    public void clear() {
        for (Map.Entry<String, AlbumHolder> entry : albums.entrySet()) {
            entry.getValue().clear();
        }
        toDisplay.clear();
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