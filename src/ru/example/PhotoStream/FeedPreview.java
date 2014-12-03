package ru.example.PhotoStream;

public abstract class FeedPreview implements IEventDispatcher {

    public final static String EVENT_UPDATED = "preview_updated";

    protected final Feed currentFeed;

    private final EventDispatcher dispatcher = new EventDispatcher();

    protected FeedPreview(Feed currentFeed) {
        this.currentFeed = currentFeed;
    }

    public void start() {

    }

    public void pause() {

    }

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
    public void dispatchEvent(Event e) {
        dispatcher.dispatchEvent(e);
    }
}
