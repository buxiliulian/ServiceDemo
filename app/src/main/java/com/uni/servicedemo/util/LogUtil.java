package com.uni.servicedemo.util;

import android.util.Log;

public class LogUtil {
    private static final String TAG = "ServiceDemo";

    public static void d(Object msg) {
        if (msg != null) {
            Log.d(TAG, msg.toString());
        }
    }
}
