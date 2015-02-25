package ru.example.PhotoStream;

import java.util.Map;

public interface IEventDispatcher {
    void addEventListener(IEventHandler listener);

    void removeEventListener(IEventHandler listener);

    void dispatchEvent(String type, Map<String, Object> data);
}
