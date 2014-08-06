package ru.example.PhotoStream.Camera.Filters;

import ru.example.PhotoStream.Camera.RawBitmap;

public abstract class TunablePhotoFilter implements PhotoFilter {
    public enum TunableType {
        ColorCurve,
        ColorMatrix,
        Convolution,
        Identity,
        Saturation,
        Vignette,
    }
    /**
     * Strength of applying filter. Should be from -1.0 to 1.0 inclusive.
     */
    private double strength = 0;

    protected abstract void transformOpaqueRaw(RawBitmap source, RawBitmap destination, double strength);

    public abstract TunableType getType();

    public synchronized void setStrength(double strength) {
        this.strength = strength;
    }

    public synchronized double getStrength() {
        return strength;
    }

    @Override
    public void transformOpaqueRaw(RawBitmap source, RawBitmap destination) {
        double strength = getStrength();
        if (strength != 0) {
            transformOpaqueRaw(source, destination, strength);
        } else if (source != destination) {
            System.arraycopy(source.colors, 0, destination.colors, 0, source.width * source.height);
        }
    }
}
