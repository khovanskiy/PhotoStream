package ru.example.PhotoStream;

public class SharedPointer <T> {
    private T object;
    private int count = 0;

    public SharedPointer(T object) {
        this.object = object;
        this.count++;
    }

    public T get() {
        return this.object;
    }
}
