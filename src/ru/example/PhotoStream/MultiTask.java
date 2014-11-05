package ru.example.PhotoStream;

import android.os.AsyncTask;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MultiTask<Key> extends AsyncTask<Void, Void, Map<Key, Future<?>>> {

    private ExecutorService service = Executors.newFixedThreadPool(5);
    private Map<Key, Callable<?>> callables = new HashMap<>();

    @Override
    protected Map<Key, Future<?>> doInBackground(Void... params) {
        Map<Key, Future<?>> futures = new HashMap<>();
        for (Map.Entry<Key, Callable<?>> callable : callables.entrySet()) {
            futures.put(callable.getKey(), service.submit(callable.getValue()));
        }
        for (Map.Entry<Key, Future<?>> future : futures.entrySet()) {
            try {
                future.getValue().get();
            } catch (Exception e) {
            }
        }
        //Console.print("Multi Done");
        return futures;
    }

    public void put(Key key, Callable<?> callable) {
        callables.put(key, callable);
    }
}
