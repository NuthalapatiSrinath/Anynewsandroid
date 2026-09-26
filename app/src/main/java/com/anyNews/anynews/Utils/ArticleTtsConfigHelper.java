package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Admin article TTS / read-aloud flags from splash / session / feed poll:
 * {@code tts_enabled}, {@code tts_speaker_on_image}, {@code tts_speaker_on_engagement},
 * {@code tts_locale}, {@code tts_voice}, {@code tts_engine}.
 *
 * <p>Master + two placement slots (Search pattern). Defaults all OFF.
 * Null from server = leave cache (never wipe on partial poll).
 */
public final class ArticleTtsConfigHelper {

    public static final String ENGINE_SYSTEM = "system";
    public static final String ENGINE_GOOGLE = "google";
    public static final String GOOGLE_TTS_PACKAGE = "com.google.android.tts";

    public static final boolean DEFAULT_ENABLED = false;
    public static final boolean DEFAULT_ON_IMAGE = false;
    public static final boolean DEFAULT_ON_ENGAGEMENT = false;

    private ArticleTtsConfigHelper() {}

    /** Persist when present (null = old server; leave cache). */
    public static void saveFromServer(
            @Nullable Context context,
            @Nullable Boolean enabled,
            @Nullable String locale,
            @Nullable String voice,
            @Nullable String engine) {
        saveFromServer(context, enabled, null, null, locale, voice, engine);
    }

    /** Persist master + placement + voice fields (null = leave cache). */
    public static boolean saveFromServer(
            @Nullable Context context,
            @Nullable Boolean enabled,
            @Nullable Boolean onImage,
            @Nullable Boolean onEngagement,
            @Nullable String locale,
            @Nullable String voice,
            @Nullable String engine) {
        if (context == null) return false;
        Context app = context.getApplicationContext();
        boolean changed = false;
        if (enabled != null) {
            boolean next = Boolean.TRUE.equals(enabled);
            if (isEnabled(app) != next) {
                SharedPrefsData.putBool(app, Constants.TTS_ENABLED, next, Constants.PREF_NAME);
                changed = true;
            }
        }
        if (onImage != null) {
            boolean next = Boolean.TRUE.equals(onImage);
            if (isOnImage(app) != next) {
                SharedPrefsData.putBool(
                        app, Constants.TTS_SPEAKER_ON_IMAGE, next, Constants.PREF_NAME);
                changed = true;
            }
        }
        if (onEngagement != null) {
            boolean next = Boolean.TRUE.equals(onEngagement);
            if (isOnEngagement(app) != next) {
                SharedPrefsData.putBool(
                        app, Constants.TTS_SPEAKER_ON_ENGAGEMENT, next, Constants.PREF_NAME);
                changed = true;
            }
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
        return changed;
    }

    /** Master: allow read-aloud when a placement is also ON. Default OFF. */
    public static boolean isEnabled(@Nullable Context context) {
        if (context == null) return DEFAULT_ENABLED;
        return SharedPrefsData.getBool(
                context.getApplicationContext(),
                Constants.TTS_ENABLED,
                Constants.PREF_NAME,
                DEFAULT_ENABLED);
    }

    /** Top-right speaker on article image. Requires {@link #isEnabled}. Default OFF. */
    public static boolean isOnImage(@Nullable Context context) {
        if (context == null) return DEFAULT_ON_IMAGE;
        try {
            return SharedPrefsData.getBool(
                    context.getApplicationContext(),
                    Constants.TTS_SPEAKER_ON_IMAGE,
                    Constants.PREF_NAME,
                    DEFAULT_ON_IMAGE);
        } catch (Exception e) {
            return DEFAULT_ON_IMAGE;
        }
    }

    /** Engagement-row speaker. Requires {@link #isEnabled}. Default OFF. */
    public static boolean isOnEngagement(@Nullable Context context) {
        if (context == null) return DEFAULT_ON_ENGAGEMENT;
        try {
            return SharedPrefsData.getBool(
                    context.getApplicationContext(),
                    Constants.TTS_SPEAKER_ON_ENGAGEMENT,
                    Constants.PREF_NAME,
                    DEFAULT_ON_ENGAGEMENT);
        } catch (Exception e) {
            return DEFAULT_ON_ENGAGEMENT;
        }
    }

    public static boolean shouldShowOnImage(@Nullable Context context) {
        return isEnabled(context) && isOnImage(context);
    }

    public static boolean shouldShowOnEngagement(@Nullable Context context) {
        return isEnabled(context) && isOnEngagement(context);
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
