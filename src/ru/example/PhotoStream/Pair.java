package ru.example.PhotoStream;


public class Pair<F, S> {
    /**
     * First element of pair.
     */
    public F first;

    /**
     * Second element of pair.
     */
    public S second;

    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }
}
