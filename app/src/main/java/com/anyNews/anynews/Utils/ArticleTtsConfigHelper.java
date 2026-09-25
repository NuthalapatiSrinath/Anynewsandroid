package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Admin article TTS / read-aloud flags from splash ({@code tts_enabled},
 * {@code tts_locale}, {@code tts_voice}, {@code tts_engine}).
 * Default OFF — speaker hidden until admin enables.
 */
public final class ArticleTtsConfigHelper {

    public static final String ENGINE_SYSTEM = "system";
    public static final String ENGINE_GOOGLE = "google";
    public static final String GOOGLE_TTS_PACKAGE = "com.google.android.tts";

    private ArticleTtsConfigHelper() {}

    /** Persist when present (null = old server; leave cache). */
    public static void saveFromServer(
            @Nullable Context context,
            @Nullable Boolean enabled,
            @Nullable String locale,
            @Nullable String voice,
            @Nullable String engine) {
        if (context == null) return;
        Context app = context.getApplicationContext();
        if (enabled != null) {
            SharedPrefsData.putBool(
                    app, Constants.TTS_ENABLED, Boolean.TRUE.equals(enabled), Constants.PREF_NAME);
        }
        if (locale != null) {
            SharedPrefsData.putString(
                    app, Constants.TTS_LOCALE, locale.trim(), Constants.PREF_NAME);
        }
        if (voice != null) {
            SharedPrefsData.putString(
                    app, Constants.TTS_VOICE, voice.trim(), Constants.PREF_NAME);
        }
        if (engine != null) {
            SharedPrefsData.putString(
                    app, Constants.TTS_ENGINE, normalizeEngine(engine), Constants.PREF_NAME);
        }
    }

    /** Master: show speaker + allow read-aloud. Default OFF. */
    public static boolean isEnabled(@Nullable Context context) {
        if (context == null) return false;
        return SharedPrefsData.getBool(
                context.getApplicationContext(),
                Constants.TTS_ENABLED,
                Constants.PREF_NAME,
                false);
    }

    @NonNull
    public static String getLocale(@Nullable Context context) {
        if (context == null) return "";
        String v =
                SharedPrefsData.getString(
                        context.getApplicationContext(),
                        Constants.TTS_LOCALE,
                        Constants.PREF_NAME);
        return v == null ? "" : v.trim();
    }

    @NonNull
    public static String getVoice(@Nullable Context context) {
        if (context == null) return "";
        String v =
                SharedPrefsData.getString(
                        context.getApplicationContext(),
                        Constants.TTS_VOICE,
                        Constants.PREF_NAME);
        return v == null ? "" : v.trim();
    }

    @NonNull
    public static String getEngine(@Nullable Context context) {
        if (context == null) return ENGINE_SYSTEM;
        String v =
                SharedPrefsData.getString(
                        context.getApplicationContext(),
                        Constants.TTS_ENGINE,
                        Constants.PREF_NAME);
        return normalizeEngine(v);
    }

    @NonNull
    private static String normalizeEngine(@Nullable String raw) {
        if (TextUtils.isEmpty(raw)) return ENGINE_SYSTEM;
        String v = raw.trim().toLowerCase();
        return ENGINE_GOOGLE.equals(v) ? ENGINE_GOOGLE : ENGINE_SYSTEM;
    }
}
