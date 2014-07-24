package ru.example.PhotoStream.Camera.Filters;

import ru.example.PhotoStream.Camera.RawBitmap;

public class IdentityFilter extends TunablePhotoFilter {
    @Override
    public void transformOpaqueRaw(RawBitmap source, RawBitmap destination, double strength) {
        if (source != destination) {
            System.arraycopy(source.colors, 0, destination.colors, 0, source.width * source.height);
        }
    }

    @Override
    public TunableType getType() {
        return TunableType.Identity;
    }
}
