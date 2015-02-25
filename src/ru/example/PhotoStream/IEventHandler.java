package ru.example.PhotoStream;

import java.util.Map;

public interface IEventHandler {
    void handleEvent(IEventDispatcher dispatcher, String type, Map<String, Object> data);
}
