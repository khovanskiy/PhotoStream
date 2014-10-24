package ru.example.PhotoStream.Camera.Filters;

import android.content.Context;

public interface FilterDescription {
    public abstract String toString(Context context);
    public abstract int getIconResource();
    public abstract int getPriority();
    public abstract int getMaxUpdatePriority();
}
