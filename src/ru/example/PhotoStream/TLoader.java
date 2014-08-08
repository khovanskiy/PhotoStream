package ru.example.PhotoStream;


import android.os.AsyncTask;

public abstract class TLoader<T> extends AsyncTask<Void, Void, T> {

    public interface Callback<T> {
        void onDataLoaded(T data);
    }

    private Callback<T> callback = null;

    public void attachLoadedCallback(Callback<T> callback) {
        this.callback = callback;
    }

    @Override
    protected void onPostExecute(T data) {
        if (callback != null) {
            callback.onDataLoaded(data);
        }
    }
}
