package ru.example.PhotoStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.InputStream;
import java.net.URL;

public class ImageLoader extends AsyncTask<Void, Void, Bitmap> implements IEventDispatcher{
    private String path;
    private EventDispatcher eventDispatcher;

    public ImageLoader(String path) {
        this.path = path;
        eventDispatcher = new EventDispatcher();
    }

    protected Bitmap doInBackground(Void... urls) {
        String urldisplay = path;
        Bitmap mIcon11 = null;
        InputStream in = null;
        try
        {
            try {
                in = new URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);

            } catch (Exception e) {
                Console.print(e.getMessage());
            }
            finally {
                if (in != null)
                {
                    in.close();
                }
            }
        }
        catch (Exception e) {

        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        Event e = new Event(this, Event.COMPLETE);
        e.data.put("bitmap", result);
        e.data.put("path", path);
        dispatchEvent(e);
    }

    @Override
    public void addEventListener(IEventHadler listener) {
        eventDispatcher.addEventListener(listener);
    }

    @Override
    public void removeEventListener(IEventHadler listener) {
        eventDispatcher.removeEventListener(listener);
    }

    @Override
    public void dispatchEvent(Event e) {
        eventDispatcher.dispatchEvent(e);
    }
}
