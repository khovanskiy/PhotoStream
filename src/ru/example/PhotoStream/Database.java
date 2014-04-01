package ru.example.PhotoStream;

public class Database implements IEventDispatcher, IEventHadler {
    private static final String DATABASE_NAME = "photostream_ok";
    private static final int DATABASE_VERSION = 1;
    private static Database instance = null;

    protected EventDispatcher event_pull;

    public Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    @Override
    public void addEventListener(IEventHadler listener) {
        event_pull.addEventListener(listener);
    }

    @Override
    public void removeEventListener(IEventHadler listener) {
        event_pull.removeEventListener(listener);
    }

    @Override
    public void dispatchEvent(Event e) {
        event_pull.dispatchEvent(e);
    }

    @Override
    public void handleEvent(Event e) {

    }
}
