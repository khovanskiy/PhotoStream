package ru.example.PhotoStream;

import ru.ok.android.sdk.Odnoklassniki;

import java.util.List;

public abstract class Entry extends OKApiObject {

    private final static int DEFAULT_CHUNK_SIZE = 10;

    /**
     * Can entry fetch more photos
     */
    public abstract boolean hasMore();

    /**
     * Load next chunk of photos data with {@code DEFAULT_CHUNK_SIZE}
     *
     * @param api API of Odnoklassniki.ru
     * @return is something has been loaded
     */
    public boolean loadNextChunk(Odnoklassniki api) {
        return loadNextChunk(api, DEFAULT_CHUNK_SIZE);
    }

    /**
     * Load next chunk with at most {@code count} photos
     *
     * @param api   API of Odnoklassniki.ru
     * @param count maximum count of photos
     * @return is something has been loaded
     */
    public abstract boolean loadNextChunk(Odnoklassniki api, int count);

    /**
     * Gets the latest loaded photos
     *
     * @return list of photos
     */
    public abstract List<Photo> getLastChunk();
}
