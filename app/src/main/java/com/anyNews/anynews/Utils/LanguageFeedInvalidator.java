package com.anyNews.anynews.Utils;

import android.content.Context;

import androidx.annotation.Nullable;

/**
 * After an in-app language switch, UI chrome (translator pack / {@link Constants#LanguageID})
 * can update while a previous language's OpenPath / offline pack / in-flight page-0 still
 * paints. Call {@link #onLanguageChanged} as soon as the new language id is committed so
 * Home always network-fetches for that language.
 *
 * <p>Does not touch Daily Updates gold/petrol/jobs caches (separate invalidation work).
 */
public final class LanguageFeedInvalidator {

    private LanguageFeedInvalidator() {
    }

    /**
     * Persist the new language (commit), drop main/special/video page-0 disk,
     * clear offline main pack, force the next feed open onto the network, and
     * allow splash prefetch to run again for the new language.
     */
    public static void onLanguageChanged(@Nullable Context context, int newLanguageId) {
        onLanguageChanged(context, newLanguageId, true);
    }

    /**
     * Same as {@link #onLanguageChanged(Context, int)} but optionally keeps
     * state/district prefs. Use {@code clearGeography=false} after reporter OTP
     * login when account geo was just applied from Writers — wiping district
     * would leave Hindi chrome with a Telugu Hyderabad label half-applied.
     */
    public static void onLanguageChanged(
            @Nullable Context context, int newLanguageId, boolean clearGeography) {
        if (context == null || newLanguageId <= 0) {
            return;
        }
        final Context app = context.getApplicationContext();
        try {
            SharedPrefsData.putIntImmediate(app, Constants.LanguageID, newLanguageId, Constants.PREF_NAME);
        } catch (Exception e) {
            SharedPrefsData.putInt(app, Constants.LanguageID, newLanguageId, Constants.PREF_NAME);
        }
        try {
            SharedPrefsData.putInt(app, Constants.LAST_FEED_LANGUAGE_ID, 0, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
        try {
            // Force get_static_data / translator pack reload for the new language.
            SharedPrefsData.putLong(app, Constants.OPEN_PATH_STATIC_AT, 0L, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
        try {
            OpenPathCache.clearFeed(app);
            OpenPathCache.forceNextFeedNetwork(app);
        } catch (Exception ignored) {
        }
        try {
            OfflineFeedStore.clearMain(app);
        } catch (Exception ignored) {
        }
        try {
            OpenPathFeedPrefetcher.resetForLanguageChange();
        } catch (Exception ignored) {
        }
        if (clearGeography) {
            try {
                // Stale geo labels from the previous language must not soft-fill Home chrome.
                SharedPrefsData.putInt(app, Constants.StateID, 0, Constants.PREF_NAME);
                SharedPrefsData.putInt(app, Constants.Selected_Distric_Id, 0, Constants.PREF_NAME);
                SharedPrefsData.putString(app, Constants.Selected_Distric_Name, "", Constants.PREF_NAME);
                SharedPrefsData.putString(app, Constants.Selected_Distric_Name1, "", Constants.PREF_NAME);
                SharedPrefsData.putInt(app, Constants.Selected_Distric_Name_LangId, 0, Constants.PREF_NAME);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Home open safety: if the last successful feed language differs from the
     * selected {@link Constants#LanguageID}, drop warm disk and force network.
     */
    public static void ensureFeedMatchesSelectedLanguage(@Nullable Context context) {
        if (context == null) return;
        final Context app = context.getApplicationContext();
        int lang;
        int last;
        try {
            lang = SharedPrefsData.getInt(app, Constants.LanguageID, Constants.PREF_NAME);
            last = SharedPrefsData.getInt(app, Constants.LAST_FEED_LANGUAGE_ID, Constants.PREF_NAME);
        } catch (Exception e) {
            return;
        }
        if (lang <= 0) return;
        // 0 = never recorded (first open / after explicit language change clear).
        // Only act when a prior feed language is known and differs.
        if (last <= 0 || last == lang) return;
        try {
            OpenPathCache.clearFeed(app);
            OpenPathCache.forceNextFeedNetwork(app);
            OfflineFeedStore.clearMain(app);
            OpenPathFeedPrefetcher.resetForLanguageChange();
        } catch (Exception ignored) {
        }
    }

    /** Record that page-0 for {@code languageId} was applied successfully. */
    public static void markFeedLanguageApplied(@Nullable Context context, int languageId) {
        if (context == null || languageId <= 0) return;
        try {
            SharedPrefsData.putInt(
                    context.getApplicationContext(),
                    Constants.LAST_FEED_LANGUAGE_ID,
                    languageId,
                    Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }
}
