package ru.example.PhotoStream;

import org.json.JSONException;
import org.json.JSONObject;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Selection extends AlbumsOwner {

    private static Map<String, Selection> cache = new ConcurrentHashMap<>();

    private List<Album> albums = new ArrayList<>();
    private List<AlbumsOwner> owners = new ArrayList<>();
    private String name = "";

    private Selection() {

    }

    public static synchronized Selection get(String selectionId) {
        Selection current;
        if (!cache.containsKey(selectionId)) {
            current = new Selection();
            cache.put(selectionId, current);
        } else {
            current = cache.get(selectionId);
        }
        current.objectId = selectionId;
        return current;
    }

    public static Selection build(String selectionId, String name) {
        Selection current = Selection.get(selectionId);
        current.name = name;
        return current;
    }

    @Override
    public List<Album> getAlbums() {
        return albums;
    }

    @Override
    public void loadAlbums(Odnoklassniki api) {
        albums.clear();
        for (AlbumsOwner owner : owners) {
            owner.loadAlbums(api);
            albums.addAll(owner.getAlbums());
        }
    }

    //@Override
    public String getName() {
        return name;
    }

    @Override
    public Photo getAvatar() {
        return null;
    }

    public void addAll(Collection<? extends AlbumsOwner> collection) {
        owners.addAll(collection);
    }

    public void add(AlbumsOwner albumsOwner) {
        owners.add(albumsOwner);
    }

    public void clear() {
        owners.clear();
    }

    public int size() {
        return owners.size();
    }
}
