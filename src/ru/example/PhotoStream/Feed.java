package ru.example.PhotoStream;

import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
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
        private boolean hasMore = true;
        private String lastAnchor = "";
        private int photosCount = 0;
        private Photo lastLoadedPhoto = null;

        public AlbumHolder(Album album) {
            this.album = album;
        }

        public Album getAlbum() {
            return album;
        }

        public boolean hasMore() {
            return hasMore;
        }

        public int getPhotosCount() {
            return photosCount;
        }

        public Photo getLastLoadedPhoto() {
            return lastLoadedPhoto;
        }

        public void clear() {
            photosCount = 0;
            lastLoadedPhoto = null;
            hasMore = true;
            lastAnchor = "";
        }

        public List<Photo> loadNextChunk(Odnoklassniki api, int count) {
            Map<String, String> requestParams = new HashMap<>();
            if (album.albumType == AlbumType.USER) {
                requestParams.put("fid", album.user_id);
                requestParams.put("fields", "photo.*");
            } else if (album.albumType == AlbumType.GROUP) {
                requestParams.put("gid", album.group_id);
                requestParams.put("fields", "group_photo.*");
            }
            if (!album.isPersonal) {
                requestParams.put("aid", album.objectId);
            }
            requestParams.put("count", count + "");
            List<Photo> chunk = new ArrayList<>();
            if (hasMore) {
                if (lastAnchor.length() > 0) {
                    requestParams.put("anchor", lastAnchor);
                }
                try {
                    String response = api.request("photos.getPhotos", requestParams, "get");
                    JSONObject photosObject = new JSONObject(response);
                    JSONArray photos = photosObject.getJSONArray("photos");
                    for (int i = 0; i < photos.length(); ++i) {
                        Photo photo = Photo.build(photos.getJSONObject(i));
                        photo.album_id = album.getId();
                        chunk.add(photo);
                        lastLoadedPhoto = photo;
                    }
                    hasMore = photosObject.getBoolean("hasMore");
                    if (hasMore) {
                        lastAnchor = photosObject.getString("anchor");
                    }
                } catch (Exception e) {
                    Log.i("CONSOLE", e.toString(), e);
                    hasMore = false;
                }
            }
            photosCount += chunk.size();
            return chunk;
        }
    }

    private class Loader extends AsyncTask<Void, Void, List<Photo>> {
        @Override
        protected List<Photo> doInBackground(Void... params) {
            List<Photo> chunk = new ArrayList<>(currentLoadCount);
            for (Map.Entry<String, AlbumHolder> entry : albums.entrySet()) {
                AlbumHolder album = entry.getValue();
                if (album.hasMore() && album.getPhotosCount() == 0) {
                    heap.addAll(album.loadNextChunk(api, 2));
                }
            }
            for (int i = 0; heap.size() > 0 && i < currentLoadCount; ++i) {
                Photo photo = heap.poll();
                chunk.add(photo);
                AlbumHolder album = albums.get(photo.album_id);
                if (album.hasMore() && album.getLastLoadedPhoto() == photo) {
                    heap.addAll(album.loadNextChunk(api, DEFAULT_CHUNK_SIZE));
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
            isRunning.set(false);
        }

    }

    public final static int DEFAULT_LOAD_COUNT = 100;
    private final static int DEFAULT_CHUNK_SIZE = 10;

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

    public void addAll(List<Album> albums) {
        for (Album album : albums) {
            add(album);
        }
    }

    public void add(Album album) {
        albums.put(album.getId(), new AlbumHolder(album));
    }
}
