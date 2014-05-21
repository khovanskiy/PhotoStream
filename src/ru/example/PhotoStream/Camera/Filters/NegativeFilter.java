package ru.example.PhotoStream.Camera.Filters;

import ru.example.PhotoStream.Camera.RawBitmap;

public class NegativeFilter implements PhotoFilter {

    @Override
    public synchronized void transformOpaque(RawBitmap bitmap) {
        int frameSize = bitmap.height * bitmap.width;
        for (int i = 0; i < frameSize; i++) {
            bitmap.colors[i] ^= 0x00FFFFFF;
        }
    }
}
