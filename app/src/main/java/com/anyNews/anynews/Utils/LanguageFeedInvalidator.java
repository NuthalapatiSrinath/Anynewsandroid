package com.anyNews.anynews.Utils;

import android.content.Context;

import androidx.annotation.Nullable;

/**
 * After an in-app language switch, UI chrome (translator pack / {@link Constants#LanguageID})
 * can update while a previous language's OpenPath / offline pack / in-flight page-0 still
 * paints. Call {@link #onLanguageChanged} as soon as the new language id is committed so
 * Home always network-fetches for that language.
 *
 * <p>Also clears Rewards chip prefs and Daily Updates flash/local headline caches so
 * Telugu splash strings cannot stick on Hindi/Malayalam.
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
        // Drop the other language's flip seat before Home is recreated. commit()
        // so the next onCreate cannot read the previous index. Home also honors
        // LANGUAGE_SWITCH_RESET_FLIP if a dying activity rewrites the checkpoint.
        try {
            app.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putInt(Constants.FLIP_RESTORE_INDEX, 0)
                    .putString(Constants.FLIP_RESTORE_POST_ID, "-1")
                    .putBoolean(Constants.FLIP_RESTORE_PENDING, false)
                    .putString(Constants.FLIP_RESTORE_AT, "0")
                    .putInt(Constants.FLIP_RESTORE_LANGUAGE_ID, 0)
                    .putBoolean(Constants.LANGUAGE_SWITCH_RESET_FLIP, true)
                    .commit();
        } catch (Exception ignored) {
        }
        try {
            // Force get_static_data / translator pack reload for the new language.
            SharedPrefsData.putLong(app, Constants.OPEN_PATH_STATIC_AT, 0L, Constants.PREF_NAME);
            // Drop pack-language stamp so Profile/Home refuse stale Hindi (etc.) chrome
            // until get_static_data / Update_User_Language writes the new pack.
            SharedPrefsData.putInt(app, Constants.TRANSLATOR_LANGUAGE_ID, 0, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
        try {
            // Share-return seat is not a language change. Drop the pager index
            // so coming back to this language cannot resume the old card.
            FeedPositionKeepConfig.clear(app);
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
            // Singleton still holds the previous language's skip / page cursor.
            OfflinePackPrefetcher.get().resetSession();
        } catch (Exception ignored) {
        }
        try {
            CategoriesDiskCache.invalidate(app);
        } catch (Exception ignored) {
        }
        try {
            // Stale Get_User_Profile disk would re-apply old Writers language_id
            // into SharedPreferences via applyProfileDataToUi.
            ReporterProfileCache.clear(app);
        } catch (Exception ignored) {
        }
        try {
            OpenPathFeedPrefetcher.resetForLanguageChange();
        } catch (Exception ignored) {
        }
        try {
            // Stale Telugu Rewards chip prefs must not survive LanguageID flip.
            com.anyNews.anynews.rewards.RewardsConfigHelper.clearChipCopyForLanguageChange(app);
        } catch (Exception ignored) {
        }
        try {
            DailyUpdatesConfigHelper.clearLanguageSensitiveCaches(app);
        } catch (Exception ignored) {
        }
        if (clearGeography) {
            try {
                // Keep prior language's district so switching back can restore it.
                DistrictLabelHelper.snapshotActiveToLanguageSlot(app);
            } catch (Exception ignored) {
            }
            try {
                DistrictLabelHelper.clearActiveSelection(app);
            } catch (Exception ignored) {
            }
            try {
                // Restore last valid district for the new language when we have one.
                DistrictLabelHelper.ensureSelectedMatchesLanguage(app);
            } catch (Exception ignored) {
            }
        } else {
            // Login path kept geography — still drop cross-language labels.
            try {
                DistrictLabelHelper.ensureSelectedMatchesLanguage(app);
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

    /**
     * After a district change: drop warm OpenPath / offline page-0 so the next
     * Home open cannot paint the previous district's feed, force network, and
     * clear reporter profile disk so Profile does not re-apply a stale location.
     */
    public static void onDistrictChanged(@Nullable Context context) {
        if (context == null) {
            return;
        }
        final Context app = context.getApplicationContext();
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
            ReporterProfileCache.clear(app);
        } catch (Exception ignored) {
        }
    }
}
