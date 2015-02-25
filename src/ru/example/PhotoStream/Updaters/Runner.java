package ru.example.PhotoStream.Updaters;

import android.os.AsyncTask;
import ru.example.PhotoStream.EventDispatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public final class Runner extends EventDispatcher {
    private class RunnerHelper extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            try {
                return task.call();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            if (o != null) {
                dataReady = true;
            }
            Map<String, Object> data = new HashMap<>();
            data.put(type, o);
            dispatchEvent(type, data);
        }
    }

    private Callable<?> task;
    private String type;
    private boolean dataReady = false;

    public Runner(Callable<?> task, String type) {
        this.task = task;
        this.type = type;
    }

    public void execute() {
        new RunnerHelper().execute();
    }

    public boolean isDataReady() {
        return dataReady;
    }
}
