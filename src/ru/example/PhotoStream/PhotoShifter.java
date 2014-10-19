package ru.example.PhotoStream;

import java.util.*;

/**
 * Created by Genyaz on 19.10.2014.
 */
public class PhotoShifter extends EventDispatcher implements IEventHandler {
    private static Random random = new Random(System.currentTimeMillis());
    private static int MAX_INITIAL_DELAY = 5000;
    private static long REFRESH_DELAY = 5000;
    private Feed feed;
    private List<Photo> currentPhotos = new ArrayList<>();
    private int currentPosition = -1;
    private int lastSize = 0;
    private boolean hasMore = true;
    private boolean toChange = false;
    private Timer timer;

    public PhotoShifter(Feed feed) {
        this.feed = feed;
        this.feed.addEventListener(this);
        this.feed.loadMore();
        start();
    }

    @Override
    public synchronized void handleEvent(Event e) {
        if (e.type == Event.COMPLETE) {
            currentPhotos = feed.getAvailablePhotos();
            if (lastSize == currentPhotos.size()) {
                hasMore = false;
            }
            lastSize = currentPhotos.size();
            if (toChange) {
                toChange = false;
                nextPosition();
            }
        }
    }

    private void nextPosition() {
        if (currentPhotos.size() != 0) {
            currentPosition = (currentPosition + 1) % currentPhotos.size();
            Event event = new Event(PhotoShifter.this, Event.PHOTO_CHANGED);
            event.data.put("photo", currentPhotos.get(currentPosition));
            dispatchEvent(event);
        }
    }

    private synchronized void changePhoto() {
        if (hasMore && currentPosition + 1 == currentPhotos.size()) {
            toChange = true;
            feed.loadMore();
        } else {
            nextPosition();
        }
    }

    public synchronized void pause() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public synchronized void start() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                changePhoto();
            }
        }, (long) random.nextInt(MAX_INITIAL_DELAY), REFRESH_DELAY);
    }
}
