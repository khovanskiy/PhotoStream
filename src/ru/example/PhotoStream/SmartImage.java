package ru.example.PhotoStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.util.LinkedHashMap;

public class SmartImage extends ImageView implements IEventHadler{
    private static LinkedHashMap<String, Bitmap> cache = new LinkedHashMap<>();

    public SmartImage(Context context) {
        super(context);
    }

    public SmartImage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SmartImage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void loadFromURL(String url) {
        if (cache.containsKey(url)) {
            Bitmap bitmap = cache.get(url);
            this.setImageBitmap(bitmap);
        }
        else {
            ImageLoader loader = new ImageLoader(url);
            loader.addEventListener(this);
            loader.execute();
        }
    }

    @Override
    public void handleEvent(Event e) {
        if (e.type == Event.COMPLETE) {
            e.target.removeEventListener(this);
            Bitmap bitmap = (Bitmap) e.data.get("bitmap");
            String path = (String) e.data.get("path");
            this.setImageBitmap(bitmap);
            if (!cache.containsKey(path)) {
                cache.put(path, bitmap);
            }
        }
    }
}
