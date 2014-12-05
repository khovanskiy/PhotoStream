package ru.example.PhotoStream.Activities;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBarActivity;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.HashMap;

public abstract class UIActivity extends ActionBarActivity {
    private final static String APP_ID = "409574400";
    private final static String APP_SECRET_KEY = "9C9616F58E44F35643492983";
    private final static String APP_PUBLIC_KEY = "CBANJKGJBBABABABA";
    private static HashMap<Class, Holder> holders = new HashMap<>();

    protected static class Holder {
        private int version = 0;
        private HashMap<String, Object> params = new HashMap<>();

        public boolean hasParam(String key) {
            return params.containsKey(key);
        }

        public Object getParam(String key) {
            return params.get(key);
        }

        public void putParam(String key, Object value) {
            params.put(key, value);
        }
    }

    protected static Holder instance(Class currentClass) {
        Holder holder = holders.get(currentClass);
        if (holder == null) {
            holder = new Holder();
            holders.put(currentClass, holder);
        }
        return holder;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected Odnoklassniki getAPI() {
        if (Odnoklassniki.hasInstance()) {
            return Odnoklassniki.getInstance(this);
        }
        return Odnoklassniki.createInstance(this, APP_ID, APP_SECRET_KEY, APP_PUBLIC_KEY);
    }
}
