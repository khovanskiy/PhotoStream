package ru.example.PhotoStream.Activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBarActivity;
import net.hockeyapp.android.CrashManager;
import ru.ok.android.sdk.Odnoklassniki;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

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

    private static Map<Class<? extends UIActivity>, Map<String, Object>> caches = new HashMap<>();

    public static Map<String, Object> weakCache(Class<? extends UIActivity> aClass) {
        Map<String, Object> cache = caches.get(aClass);
        if (cache == null) {
            cache = new WeakHashMap<>();
            caches.put(aClass, cache);
        }
        return cache;
    }

    /*private String mActivityName = null;
    public String getActivityName() {
        if (mActivityName == null) {
            mActivityName = this.getLocalClassName();
        }
        return mActivityName;
    }*/

    private static Odnoklassniki api;
    private static Activity sTopActivity;

    public static Activity getTopActivity() {
        return sTopActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (sTopActivity != this) {
            sTopActivity = this;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sTopActivity != this) {
            sTopActivity = this;
        }
        CrashManager.register(this, "5adb6faead01ccaa24e6865215ddcb59");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sTopActivity == this) {
            sTopActivity = null;
        }
    }

    public static Odnoklassniki getAPI() {
        if (Odnoklassniki.hasInstance()) {
            return Odnoklassniki.getInstance(sTopActivity);
        }
        return Odnoklassniki.createInstance(sTopActivity, APP_ID, APP_SECRET_KEY, APP_PUBLIC_KEY);
    }
}
