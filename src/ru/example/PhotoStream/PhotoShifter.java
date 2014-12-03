package ru.example.PhotoStream;

import java.util.*;

public class PhotoShifter extends FeedPreview implements IEventHandler {
    private static Random random = new Random(System.currentTimeMillis());
    private static int MAX_INITIAL_DELAY = 10000;
    private static long REFRESH_DELAY = 10000;
    private List<Photo> currentPhotos = new ArrayList<>();
    private int currentPosition = -1;
    private int lastSize = 0;
    private boolean hasMore = true;
    private boolean toChange = false;
    private Timer timer;
    private Photo defaultPhoto;

    public PhotoShifter(Feed feed, Photo defaultPhoto) {
        super(feed);
        currentFeed.addEventListener(this);
    }

    public Photo getPhoto() {
        if (currentPosition == -1) {
            return defaultPhoto;
        }
        return currentPhotos.get(currentPosition);
    }

    @Override
    public synchronized void handleEvent(Event e) {
        if (e.type == Event.COMPLETE) {
            currentPhotos = currentFeed.getAvailablePhotos();
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

    private synchronized void nextPosition() {
        if (currentPhotos.size() != 0) {
            currentPosition = (currentPosition + 1) % currentPhotos.size();
            dispatchEvent(new Event(this, EVENT_UPDATED));
        }
    }

    private synchronized void changePhoto() {
        if (hasMore && currentPosition + 1 == currentPhotos.size()) {
            toChange = true;
            currentFeed.loadMore();
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

    public synchronized void immediateGet() {
        if (currentPosition != -1) {
            Event event = new Event(PhotoShifter.this, Event.PHOTO_CHANGED);
            event.data.put("photo", currentPhotos.get(currentPosition));
            dispatchEvent(event);
        }
    }

    public synchronized int getPosition() {
        return currentPosition;
    }
}
