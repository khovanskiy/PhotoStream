package ru.example.PhotoStream;

import android.support.v4.util.LruCache;

public class ResourceLruCache<K, V extends IResource> extends LruCache<K, V> {
    private static final int DEFAULT_CACHE_SIZE = (int) (Runtime.getRuntime().maxMemory() / 1024) / 8;

    public ResourceLruCache() {
        this(DEFAULT_CACHE_SIZE);
    }

    public ResourceLruCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected int sizeOf(K key, V value) {
        return value == null ? 0 : value.size();
    }
}