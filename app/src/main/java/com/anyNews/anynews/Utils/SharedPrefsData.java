package com.anyNews.anynews.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPrefsData {

    private static SharedPrefsData instance = null;
    private static final String DEFAULTVALUESTRING = "N/A";
    private static final int DEFAULTVALUINT = 0;
    private static final String POULTRY_DATA = "myapp";
    private SharedPreferences PoultrySharedPrefs = null;

    public static SharedPrefsData getInstance(Context context) {

        if (instance == null) {
            instance = new SharedPrefsData();
            instance.getPitchItSharedPrefs(context);
        }
        return instance;
    }

    private void getPitchItSharedPrefs(Context context) {
        if (this.PoultrySharedPrefs == null) {
            this.PoultrySharedPrefs = context.getSharedPreferences(POULTRY_DATA, Context.MODE_PRIVATE);
        }
    }

    private void loadFreshPitchItSharedPrefs(Context context) {
        this.PoultrySharedPrefs = context.getSharedPreferences(POULTRY_DATA, Context.MODE_PRIVATE);
    }

    public String getStringFromSharedPrefs(String key) {
        return PoultrySharedPrefs.getString(key, DEFAULTVALUESTRING);
    }

    public int getIntFromSharedPrefs(String key) {
        return PoultrySharedPrefs.getInt(key, DEFAULTVALUINT);
    }

    public void updateStringValue(Context context, String key, String value) {
        //getPitchItSharedPrefs(context);
        SharedPreferences.Editor editor = this.PoultrySharedPrefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public void updateIntValue(Context context, String key, int value) {
        //getPitchItSharedPrefs(context);
        SharedPreferences.Editor editor = this.PoultrySharedPrefs.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public void ClearData(Context context) {
        getPitchItSharedPrefs(context);
        SharedPreferences profilePref = context.getSharedPreferences(POULTRY_DATA, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = profilePref.edit();
        editor.clear();
        editor.apply();
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().apply();
    }

    public static void putString(Context context, String key, String value, String pref) {
        if (context != null && key != null) {
            if (pref != null && !pref.isEmpty()) {
                context.getSharedPreferences(pref, 0).edit().putString(key, value).apply();
            } else {
                PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, value).apply();
            }

        }
    }

    /**
     * Same as {@link #putString} but {@code commit()} so the next {@link #getString}
     * in this process never races (e.g. rewards_exclude_reporters after splash/session).
     */
    public static void putStringImmediate(Context context, String key, String value, String pref) {
        if (context == null || key == null) {
            return;
        }
        try {
            SharedPreferences.Editor editor;
            if (pref == null || pref.isEmpty()) {
                editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            } else {
                editor = context.getSharedPreferences(pref, Context.MODE_PRIVATE).edit();
            }
            editor.putString(key, value);
            editor.commit();
        } catch (Exception e) {
            putString(context, key, value, pref);
        }
    }

    public static String getString(Context context, String key, String pref) {
        if (context == null || key == null) {
            return null;
        }
        SharedPreferences sp = prefsOf(context, pref);
        try {
            return sp.getString(key, null);
        } catch (ClassCastException e) {
            // Wrong type under this key (e.g. Long from legacy USER_ID / district name collision).
            Object raw = safeRaw(sp, key);
            return raw instanceof String ? (String) raw : null;
        }
    }

    public static void putLong(Context context, String key, long value, String pref) {
        if (context == null || key == null) {
            return;
        }
        SharedPreferences sp = prefsOf(context, pref);
        SharedPreferences.Editor editor = sp.edit().putLong(key, value);
        // USER_ID once used Selected_Distric_Name — clear numeric garbage so getString works.
        if (Constants.USER_ID.equals(key)) {
            Object legacy = safeRaw(sp, "Selected_Distric_Name");
            if (legacy instanceof Number) {
                editor.remove("Selected_Distric_Name");
            }
        }
        editor.apply();
    }

    public static long getLong(Context context, String key, String pref) {
        if (context == null || key == null) {
            return 0;
        }
        SharedPreferences sp = prefsOf(context, pref);
        long value;
        try {
            value = sp.getLong(key, 0);
        } catch (ClassCastException e) {
            Object raw = safeRaw(sp, key);
            if (raw instanceof Number) {
                value = ((Number) raw).longValue();
            } else if (raw instanceof String) {
                try {
                    value = Long.parseLong(((String) raw).trim());
                } catch (Exception ignored) {
                    value = 0;
                }
            } else {
                value = 0;
            }
        }
        // One-time migrate user id out of the district-name key collision.
        if (Constants.USER_ID.equals(key)) {
            Object legacy = safeRaw(sp, "Selected_Distric_Name");
            if (legacy instanceof Number) {
                long legacyVal = ((Number) legacy).longValue();
                SharedPreferences.Editor ed = sp.edit().remove("Selected_Distric_Name");
                if (value == 0 && legacyVal != 0) {
                    value = legacyVal;
                    ed.putLong(key, value);
                }
                ed.apply();
            }
        }
        return value;
    }

    private static SharedPreferences prefsOf(Context context, String pref) {
        if (pref == null || pref.isEmpty()) {
            return PreferenceManager.getDefaultSharedPreferences(context);
        }
        return context.getSharedPreferences(pref, Context.MODE_PRIVATE);
    }

    private static Object safeRaw(SharedPreferences sp, String key) {
        try {
            return sp.getAll().get(key);
        } catch (Exception ignored) {
            return null;
        }
    }
    public static void putInt(Context context, String key, int value, String pref) {
        if (context == null || key == null) {
            return;
        }
        if (pref == null || pref.isEmpty()) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(key, value).apply();
        } else {
            context.getSharedPreferences(pref, Context.MODE_PRIVATE).edit().putInt(key, value).apply();
        }
    }

    public static int getInt(Context context, String key, String pref) {
        if (context == null || key == null) {
            return 0;
        }
        if (pref == null || pref.isEmpty()) {
            return PreferenceManager.getDefaultSharedPreferences(context).getInt(key, 0);
        } else {
            return context.getSharedPreferences(pref, Context.MODE_PRIVATE).getInt(key, 0);
        }
    }

    public static void putBool(Context context, String key, boolean value, String pref) {
        if (context == null || key == null) {
            return;
        }
        if (pref == null || pref.isEmpty()) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(key, value).apply();
        } else {
            context.getSharedPreferences(pref, Context.MODE_PRIVATE).edit().putBoolean(key, value).apply();
        }
    }

    /**
     * Same as {@link #putBool} but {@code commit()} so the next {@link #getBool} in this
     * process never races (critical for Special/Video feed mode before request build).
     */
    public static void putBoolImmediate(Context context, String key, boolean value, String pref) {
        if (context == null || key == null) {
            return;
        }
        try {
            SharedPreferences.Editor editor;
            if (pref == null || pref.isEmpty()) {
                editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            } else {
                editor = context.getSharedPreferences(pref, Context.MODE_PRIVATE).edit();
            }
            editor.putBoolean(key, value);
            editor.commit();
        } catch (Exception e) {
            // Fall back to async write so callers never lose the intent entirely.
            putBool(context, key, value, pref);
        }
    }

    public static boolean getBool(Context context, String key, String pref) {
        return getBool(context, key, pref, false);
    }

    public static boolean getBool(Context context, String key, String pref, boolean defaultValue) {
        if (context == null || key == null) {
            return defaultValue;
        }
        if (pref == null || pref.isEmpty()) {
            return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, defaultValue);
        } else {
            return context.getSharedPreferences(pref, Context.MODE_PRIVATE).getBoolean(key, defaultValue);
        }
    }

    public static void putFloat(Context context, String key, float value, String pref) {
        if (context == null || key == null) {
            return;
        }
        if (pref == null || pref.isEmpty()) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putFloat((key), value).apply();
        } else {
            context.getSharedPreferences(pref, Context.MODE_PRIVATE).edit().putFloat((key), value).apply();
        }
    }

    public static float getFloat(Context context, String key, String pref) {
        if (context == null || key == null) {
            return 0;
        }
        if (pref == null || pref.isEmpty()) {
            return PreferenceManager.getDefaultSharedPreferences(context).getFloat(key, 0);
        } else {
            return context.getSharedPreferences(pref, Context.MODE_PRIVATE).getFloat(key, 0);
        }


    }
}
