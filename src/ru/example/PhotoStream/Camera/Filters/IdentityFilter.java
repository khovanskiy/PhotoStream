package ru.example.PhotoStream.Camera.Filters;

import ru.example.PhotoStream.Camera.RawBitmap;

public class IdentityFilter implements PhotoFilter {
    @Override
    public synchronized void transformOpaque(RawBitmap bitmap) {

    }
}
