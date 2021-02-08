package com.launch.starter.utils;

import android.util.Log;

public class LogUtils {

    private static boolean sDebug = true;

    public static void i(String msg) {
        if (!sDebug) {
            return;
        }
        Log.i("DispatcherLog", msg);
    }

    public static void e(String msg) {
        if (!sDebug) {
            return;
        }
        Log.e("DispatcherLog", msg);
    }

    public static boolean isDebug() {
        return sDebug;
    }

    public static void setDebug(boolean debug) {
        sDebug = debug;
    }

}
