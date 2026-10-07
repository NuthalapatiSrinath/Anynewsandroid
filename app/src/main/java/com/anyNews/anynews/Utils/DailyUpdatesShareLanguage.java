package com.anyNews.anynews.Utils;

import android.content.Context;

import androidx.annotation.Nullable;

/**
 * Screen-only language for a Daily Updates share open.
 *
 * <p>The link carries the sharer's {@link Constants#LanguageID}. While the
 * opened screen is up, label helpers read that id. {@link Constants#LanguageID}
 * itself is never written, so the person's saved language (and Telugu
 * panchangam source strings) stay as they were.
 *
 * <p>A Telugu user ({@code LanguageID == 3}) always keeps 3. Share language
 * must not replace the Telugu panchangam path.
 */
public final class DailyUpdatesShareLanguage {

    private static final int UNSET = Integer.MIN_VALUE;
    private static int override = UNSET;
    /** Balances begin/end across a recreate (new onCreate runs before old onDestroy). */
    private static int depth = 0;

    private DailyUpdatesShareLanguage() {
    }

    public static void begin(@Nullable Context context, int shareLang) {
        depth++;
        if (shareLang >= 0 && shareLang <= 10) {
            override = shareLang;
        } else if (depth == 1) {
            override = UNSET;
        }
    }

    public static void end() {
        if (depth > 0) {
            depth--;
        }
        if (depth == 0) {
            override = UNSET;
        }
    }

    /**
     * Language for Daily Updates chrome on the share-opened screen.
     * Telugu users stay on 3 even when the share used another language.
     */
    public static int effective(@Nullable Context context) {
        int stored = stored(context);
        if (stored == 3) {
            return 3;
        }
        if (override != UNSET) {
            return override;
        }
        return stored;
    }

    private static int stored(@Nullable Context context) {
        if (context == null) {
            return 0;
        }
        try {
            return SharedPrefsData.getInt(
                    context.getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
        } catch (Exception e) {
            return 0;
        }
    }
}
