package ru.example.PhotoStream;

import java.util.HashMap;

public class Event {
    public static final String COMPLETE = "complete";
    public static final String ALBUMS_LOADED = "albumsLoaded";
    public static final String GROUPS_LOADED = "groupsLoaded";
    public static final String FRIENDS_LOADED = "friendsLoaded";
    public static final String CREATE = "create";
    public static final String CHANGE = "change";
    public static final String ERROR = "error";

    public final IEventDispatcher target;
    public final String type;
    public HashMap<String, Object> data;

    public Event(IEventDispatcher target, String type) {
        this.target = target;
        this.type = type;
        this.data = new HashMap<String, Object>();
    }
}
