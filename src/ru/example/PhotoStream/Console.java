package ru.example.PhotoStream;

import android.os.Debug;
import android.util.Log;

public class Console {
    /**
     * Prints string form of the object to the log.
     * @param string object to write
     */
    public static void print(Object string) {
        Log.d("M_CONSOLE", string.toString());
    }

    public static void printAvailableMemory() {
        long maxMemory = Runtime.getRuntime().maxMemory();
        long size;
        String type;
        if (android.os.Build.VERSION.SDK_INT >= 11) {
            size = (maxMemory / 1024) - (Runtime.getRuntime().totalMemory() / 1024);
            type = "JAVA";
        } else {
            size = (maxMemory / 1024) - (Debug.getNativeHeapAllocatedSize() / 1024);
            type = "NATIVE";
        }
        double percent = Math.ceil(((double)size / (double)maxMemory) * 1000) / 1000.0;
        Console.print("Available [" + type + "] memory size = " + size + "/" + maxMemory + " = " + percent + "%");
    }

    public static void printCurrentThreadId() {
        Console.print("Current Thread " + Thread.currentThread().getId());
    }
}
