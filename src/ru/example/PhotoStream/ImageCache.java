package ru.example.PhotoStream;

import android.graphics.Bitmap;

public interface ImageCache {
    Bitmap getBitmap(String url);
    void putBitmap(String url, Bitmap bitmap);
}
