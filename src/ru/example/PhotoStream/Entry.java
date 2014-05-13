package ru.example.PhotoStream;

import ru.ok.android.sdk.Odnoklassniki;

import java.util.List;

public abstract class Entry {
    /**
     * Can entry load more photos
     *
     */
    public abstract boolean hasMore();

    /**
     * Load next chunk of photos data
     *
     * @param api API of Odnoklassniki.ru
     * @return is something has been loaded
     */
    public abstract boolean loadNextChunk(Odnoklassniki api);

    /**
     * Gets the latest loaded photos
     *
     * @return list of photos
     */
    public abstract List<Photo> getLastChunk();
}
