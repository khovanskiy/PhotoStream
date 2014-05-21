package ru.example.PhotoStream.Camera;

import android.graphics.*;

import java.io.ByteArrayOutputStream;

/**
 * Created by Genyaz on 17.05.2014.
 */
public class RawBitmap {
    public int[] colors;
    public int width, height;

    private void init(Bitmap bitmap) {
        this.width = bitmap.getWidth();
        this.height = bitmap.getHeight();
        this.colors = new int[width * height];
        bitmap.getPixels(colors, 0, width, 0, 0, width, height);
    }

    public RawBitmap(byte[] yuv, int width, int height) {
        this.width = width;
        this.height = height;
        final int frameSize = width * height;
        this.colors = new int[frameSize];
        int y, u, v, r, g, b;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                y = (0xff & ((int) yuv[i * width + j]));
                u = (0xff & ((int) yuv[frameSize + (i >> 1) * width + (j & ~1) + 1]));
                v = (0xff & ((int) yuv[frameSize + (i >> 1) * width + (j & ~1) + 0]));
                y = y < 16 ? 16 : y;
                r = (int) (1.164f * (y - 16) + 1.596f * (v - 128));
                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = (int) (1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = (int) (1.164f * (y - 16) + 2.018f * (u - 128));
                b = b < 0 ? 0 : (b > 255 ? 255 : b);
                this.colors[i * width + j] = Color.argb(255, r, g, b);

            }
        }
    }

    public RawBitmap(byte[] jpeg) {
        BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
        bitmapFactoryOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap image = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length, bitmapFactoryOptions);
        init(image);
        image.recycle();
    }

    public RawBitmap(Bitmap bitmap) {
        init(bitmap);
    }

    public Bitmap toBitmap() {
        return Bitmap.createBitmap(colors, width, height, Bitmap.Config.ARGB_8888);
    }
}
