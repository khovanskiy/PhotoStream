package ru.example.PhotoStream;

import android.util.Log;

public class Console {
    /**
     * Prints string form of the object to the log.
     * @param string object to write
     */
    public static void print(Object string) {
        Log.i("CONSOLE", string.toString());
    }
}
