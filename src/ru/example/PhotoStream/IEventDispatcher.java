package ru.example.PhotoStream;

public interface IEventDispatcher {
    void addEventListener(IEventHandler listener);

    void removeEventListener(IEventHandler listener);

    void dispatchEvent(Event e);
}
