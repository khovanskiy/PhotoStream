package ru.example.PhotoStream.Camera.Filters;

import android.graphics.Bitmap;
import android.widget.ImageView;
import ru.example.PhotoStream.Camera.RawBitmap;

public interface PhotoFilter {
    /**
     * Transforms bitmap without alpha channel.
     *
     * @param bitmap bitmap in raw format.
     */
    public void transformOpaqueRaw(RawBitmap bitmap);

    /**
     * Returns true if this photo filter can be applied to the resulting image directly.
     * @return preview modification possibility
     */
    public boolean hasPreviewModification();

    /**
     * Modifies preview image.
     * @param view preview image
     */
    public void modifyPreview(ImageView view);
}
