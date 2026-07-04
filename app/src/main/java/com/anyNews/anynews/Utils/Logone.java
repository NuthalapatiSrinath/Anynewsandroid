package com.anyNews.anynews.Utils;


import android.util.Log;

import com.anyNews.anynews.BuildConfig;

import java.util.Formatter;

public class Logone {

    public static final boolean ENABLE_DEBUG = false;
    public static final String TAG = "FlipView";
    private static final
    ThreadLocal<ReusableFormatter>
            thread_local_formatter =
            new ThreadLocal<ReusableFormatter>() {
                protected ReusableFormatter initialValue() {
                    return new ReusableFormatter();
                }
            };

    private Logone() {
    }

    public static String format(String msg, Object... args) {
        ReusableFormatter formatter = thread_local_formatter.get();
        return formatter.format(msg, args);
    }

    public static void d(String msg) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, msg);
        }
    }

    public static void v(String msg) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, msg);
        }
    }

    public static void d(String msg, Object... args) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, format(msg, args));
        }
    }

    public static void d(Throwable err, String msg, Object... args) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, format(msg, args), err);
        }
    }

    public static void i(String msg) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, msg);
        }
    }

    public static void i(String msg, Object... args) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, format(msg, args));
        }
    }

    public static void v(String msg, Object... args) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, format(msg, args));
        }
    }

    public static void i(Throwable err, String msg, Object... args) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, format(msg, args), err);
        }
    }

    public static void w(String msg) {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, msg);
        }
    }

    public static void w(String msg, Object... args) {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, format(msg, args));
        }
    }

    public static void w(Throwable err, String msg, Object... args) {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, format(msg, args), err);
        }
    }

    public static void e(String msg) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, msg);
        }
    }

    public static void e(String msg, Object... args) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, format(msg, args));
        }
    }

    public static void e(Throwable err, String msg, Object... args) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, format(msg, args), err);
        }
    }

    /**
     * A little trick to reuse a formatter in the same thread
     */
    private static class ReusableFormatter {

        private Formatter formatter;
        private StringBuilder builder;

        public ReusableFormatter() {
            builder = new StringBuilder();
            formatter = new Formatter(builder);
        }

        public String format(String msg, Object... args) {
            formatter.format(msg, args);
            String s = builder.toString();
            builder.setLength(0);
            return s;
        }
    }

}

