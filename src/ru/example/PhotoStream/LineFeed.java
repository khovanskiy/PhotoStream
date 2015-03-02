package ru.example.PhotoStream;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import ru.example.PhotoStream.Activities.UIActivity;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class LineFeed extends Feed {

    private class Loader extends AsyncTask<Void, Void, List<Photo>> {
        @Override
        protected List<Photo> doInBackground(Void... params) {
            List<Photo> chunk = new ArrayList<>(currentLoadCount);
            int toLoad = currentLoadCount;
            for (Map.Entry<String, AlbumHolder> entry : albums.entrySet()) {
                AlbumHolder album = entry.getValue();
                while (toLoad > 0 && album.hasMore()) {
                    List<Photo> photos = album.loadNextChunk(UIActivity.getAPI(), DEFAULT_CHUNK_SIZE);
                    toLoad -= photos.size();
                    chunk.addAll(photos);
                }
            }
            return chunk;
        }

        @Override
        protected void onPostExecute(List<Photo> chunk) {
            for (Photo photo : chunk) {
                toDisplay.add(photo);
            }
            dispatchEvent(Event.COMPLETE, null);
            isRunning.set(false);
        }

    }

    public final static int DEFAULT_LOAD_COUNT = 100;
    private final static int DEFAULT_CHUNK_SIZE = 10;

    //protected Executor mBackgroundExecutor = Executors.newSingleThreadExecutor();
    protected Map<String, AlbumHolder> albums = new HashMap<>();
    protected List<Photo> toDisplay = new ArrayList<>();
    protected int currentLoadCount;
    protected AtomicBoolean isRunning = new AtomicBoolean(false);

    public LineFeed(String name) {
        this(name, DEFAULT_LOAD_COUNT);
    }

    public LineFeed(String name, int loadCount) {
        super(name);
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

    public void fetch() {
        if (isRunning.compareAndSet(false, true)) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    new Loader().execute();
                }
            });
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