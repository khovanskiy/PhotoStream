package ru.example.PhotoStream;

import ru.ok.android.sdk.Odnoklassniki;

import java.util.List;

/**
 * Created by Genyaz on 11.11.2014.
 */
public class SortedFilteredFeed extends SortedFeed {

    public SortedFilteredFeed(Odnoklassniki api) {
        super(api);
    }

    public SortedFilteredFeed(Odnoklassniki api, int loadCount) {
        super(api, loadCount);
    }

    @Override
    public void add(Album album) {
        if (!album.group_id.isEmpty() || album.isPersonal || album.title.toUpperCase().equals("РАЗНОЕ")) {
            super.add(album);
        }
    }
}
