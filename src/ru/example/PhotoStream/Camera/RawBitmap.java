package ru.example.PhotoStream.Camera;

import android.graphics.*;

import java.io.ByteArrayOutputStream;

/**
 * Created by Genyaz on 17.05.2014.
 */
public class RawBitmap {
    public int[][] a, r, g, b;
    public int width, height;

    private void init(Bitmap bitmap) {
        this.width = bitmap.getWidth();
        this.height = bitmap.getHeight();
        a = new int[height][width];
        r = new int[height][width];
        g = new int[height][width];
        b = new int[height][width];
        int[] colors = new int[width * height];
        int color;
        bitmap.getPixels(colors, 0, width, 0, 0, width, height);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                color = colors[i * width + j];
                a[i][j] = Color.alpha(color);
                r[i][j] = Color.red(color);
                g[i][j] = Color.green(color);
                b[i][j] = Color.blue(color);
            }
        }
    }

    public RawBitmap(byte[] yuv, int width, int height) {
        /*YuvImage yuvimage = new YuvImage(yuv, imageFormat, width, height, null);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0, width, height), 100, bytes);
        byte[] jdata = bytes.toByteArray();
        BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
        bitmapFactoryOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        init(BitmapFactory.decodeByteArray(jdata, 0, jdata.length, bitmapFactoryOptions));/**/
        this.width = width;
        this.height = height;
        final int frameSize = width * height;
        this.a = new int[height][width];
        this.r = new int[height][width];
        this.g = new int[height][width];
        this.b = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int y = (0xff & ((int) yuv[i * width + j]));
                int u = (0xff & ((int) yuv[frameSize + (i >> 1) * width + (j & ~1) + 1]));
                int v = (0xff & ((int) yuv[frameSize + (i >> 1) * width + (j & ~1) + 0]));
                y = y < 16 ? 16 : y;
                int r = (int) (1.164f * (y - 16) + 1.596f * (v - 128));
                int g = (int) (1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
                int b = (int) (1.164f * (y - 16) + 2.018f * (u - 128));
                this.a[i][j] = 255;
                this.r[i][j] = r < 0 ? 0 : (r > 255 ? 255 : r);
                this.g[i][j] = g < 0 ? 0 : (g > 255 ? 255 : g);
                this.b[i][j] = b < 0 ? 0 : (b > 255 ? 255 : b);
            }
        }
    }

    public RawBitmap(byte[] jpeg) {
        BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
        bitmapFactoryOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        init(BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length, bitmapFactoryOptions));
    }

    public RawBitmap(Bitmap bitmap) {
        init(bitmap);
    }

    public Bitmap toBitmap() {
        int[] colors = new int[width * height];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                colors[i * width + j] = Color.argb(a[i][j], r[i][j], g[i][j], b[i][j]);
            }
        }
        return Bitmap.createBitmap(colors, width, height, Bitmap.Config.ARGB_8888);
    }
}
