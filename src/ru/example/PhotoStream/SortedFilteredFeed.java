package ru.example.PhotoStream;

import ru.ok.android.sdk.Odnoklassniki;

import java.util.List;

/**
 * Created by Genyaz on 11.11.2014.
 */
public class SortedFilteredFeed extends SortedFeed {

    public SortedFilteredFeed(String name) {
        super(name);
    }

    public SortedFilteredFeed(String name, int loadCount) {
        super(name, loadCount);
    }

    @Override
    public void add(Album album) {
        if (!album.group_id.isEmpty() || album.isPersonal || album.title.toUpperCase().equals("РАЗНОЕ")) {
            super.add(album);
        }
    }
}
