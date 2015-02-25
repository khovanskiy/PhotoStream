package ru.example.PhotoStream;

import java.util.Map;

public abstract class FeedPreview implements IEventDispatcher {

    private IEventDispatcher dispatcher = new EventDispatcher();

    protected final Feed currentFeed;

    protected FeedPreview(Feed currentFeed) {
        this.currentFeed = currentFeed;
    }

    public abstract void start();

    public abstract void pause();

    public abstract Photo getPhoto();

    public abstract int getPosition();

    public final Feed getFeed() {
        return this.currentFeed;
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
