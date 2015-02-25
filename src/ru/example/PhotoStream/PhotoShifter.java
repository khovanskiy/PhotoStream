package ru.example.PhotoStream;

import android.os.Handler;
import android.os.Looper;

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
    private Looper mLooper;

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            dispatchEvent(Event.CHANGE, null);
        }
    };

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

    private void nextPosition() {
        if (currentPhotos.size() != 0) {
            currentPosition = (currentPosition + 1) % currentPhotos.size();
            new Handler(mLooper).post(runnable);
        }
    }

    private void changePhoto() {
        if (hasMore && currentPosition + 1 == currentPhotos.size()) {
            toChange = true;
            currentFeed.loadMore();
        } else {
            nextPosition();
        }
    }

    public void pause() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public void start() {
        mLooper = Looper.myLooper();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                changePhoto();
            }
        }, (long) random.nextInt(MAX_INITIAL_DELAY), REFRESH_DELAY);
    }

    /*public synchronized void immediateGet() {
        if (currentPosition != -1) {
            Event event = new Event(PhotoShifter.this, Event.PHOTO_CHANGED);
            event.data.put("photo", currentPhotos.get(currentPosition));
            dispatchEvent(event);
        }
    }*/

    public int getPosition() {
        synchronized (this) {
            return currentPosition;
        }
    }

    @Override
    public void handleEvent(IEventDispatcher dispatcher, String type, Map<String, Object> data) {
        if (type.equals(Event.COMPLETE)) {
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
}
