package ru.example.PhotoStream;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public abstract class Feed implements IEventDispatcher {

    private static final Executor mBackgroundExecutor = Executors.newCachedThreadPool();

    protected IEventDispatcher dispatcher = new EventDispatcher();
    private String name;

    public abstract void clear();

    public abstract List<Photo> getAvailablePhotos();

    public abstract void loadMore();

    public abstract void addAll(Collection<? extends Album> albums);

    public abstract void add(Album album);

    public Feed(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public void addEventListener(IEventHandler listener) {
        dispatcher.addEventListener(listener);
    }

    @Override
    public void removeEventListener(IEventHandler listener) {
        dispatcher.removeEventListener(listener);
    }

    @Override
    public void dispatchEvent(String type, Map<String, Object> data) {
        dispatcher.dispatchEvent(type, data);
    }
}
