package com.anyNews.anynews.Utils;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;

import com.anyNews.anynews.Pojo.LanguageModelResponse;
import com.anyNews.anynews.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Shared News | Daily Updates | Special | Profile footer titles
 * (Panchangam menu id kept but product-hidden; open from Daily Updates).
 * Flash News remains reachable from the Daily Updates page (Material BNV max 5 tabs).
 *
 * <p>In-app language is {@link Constants#LanguageID} + SessionPreferences translator
 * JSON — not device Locale / {@code values-te}. {@link com.anyNews.anynews.Activities.HomePage}
 * and Profile must both call {@link #apply} so a footer tab switch does not flash English.
 */
public final class FooterTabLabels {

    private FooterTabLabels() {
    }

    public static void apply(
            @Nullable Context context,
            @Nullable BottomNavigationView nav,
            @Nullable LanguageModelResponse.Data d) {
        if (context == null || nav == null) {
            return;
        }
        Menu menu = nav.getMenu();
        if (menu == null) {
            return;
        }
        try {
            MenuItem home = menu.findItem(R.id.navigation_home);
            if (home != null) {
                String reload = d != null ? d.getReload() : null;
                String homePack = d != null ? d.getHome() : null;
                home.setTitle(NewsFooterLabel.resolveDisplay(context, reload, homePack));
            }
            // Slot 2 = Daily Updates. Id stays navigation_status for menu stability.
            MenuItem daily = menu.findItem(R.id.navigation_status);
            if (daily != null) {
                daily.setTitle(dailyUpdates(context));
            }
            MenuItem special = menu.findItem(R.id.navigation_catagiries);
            if (special != null) {
                String events = d != null ? d.getEvents() : null;
                special.setTitle(orPack(events, specialFallback(context)));
            }
            // Slot 4 = Panchangam (was Calendar). Id stays navigation_stricker.
            MenuItem panchangamTab = menu.findItem(R.id.navigation_stricker);
            if (panchangamTab != null) {
                panchangamTab.setTitle(panchangam(context));
            }
            MenuItem profile = menu.findItem(R.id.navigation_profile);
            if (profile != null) {
                String p = d != null ? d.getProfile() : null;
                profile.setTitle(orPack(p, profileFallback(context)));
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Footer Panchangam tab by in-app {@link Constants#LanguageID}
     * (same strings as calendar top chrome).
     */
    public static String panchangam(@Nullable Context context) {
        return CalendarRaasiLabels.segPanchangam(context);
    }

    /**
     * Footer Daily Updates tab by in-app {@link Constants#LanguageID}
     * (1 ML · 2 HI · 3 TE · 4 TA · 5 KN · 6 OR).
     */
    public static String dailyUpdates(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: // Malayalam
                return "ഡെയിലി";
            case 2: // Hindi
                return "डेली अपडेट";
            case 3: // Telugu — mock spirit (రోజువారీ / డైలీ అప్‌డేట్స్)
                return "డైలీ అప్‌డేట్స్";
            case 4: // Tamil
                return "டெய்லி";
            case 5: // Kannada
                return "ಡೈಲಿ ಅಪ್‌ಡೇಟ್";
            case 6: // Odia
                return "ଡେଇଲି ଅପଡେଟ୍";
            default:
                return "Daily Updates";
        }
    }

    /**
     * Flash News label (kept for Daily Updates page chip / legacy callers).
     * {@link Constants#LanguageID} (1 ML · 2 HI · 3 TE · 4 TA · 5 KN · 6 OR).
     */
    public static String breaking(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: // Malayalam
                return "ഫ്ലാഷ് ന്യൂസ്";
            case 2: // Hindi
                return "फ्लैश न्यूज़";
            case 3: // Telugu
                return "ఫ్లాష్ న్యూస్";
            case 4: // Tamil
                return "பிளாஷ் நியூஸ்";
            case 5: // Kannada
                return "ಫ್ಲಾಶ್ ನ್ಯೂಸ್";
            case 6: // Odia
                return "ଫ୍ଲାଶ୍ ନ୍ୟୁଜ୍";
            default:
                if (context != null) {
                    try {
                        return context.getString(R.string.breaking_news);
                    } catch (Exception ignored) {
                    }
                }
                return "Flash News";
        }
    }

    /** @deprecated Use {@link #breaking(Context)} — tab is Flash News (was Video / Breaking). */
    @Deprecated
    public static String videoNews(@Nullable Context context) {
        return breaking(context);
    }

    private static String specialFallback(@Nullable Context context) {
        if (context != null) {
            try {
                return context.getString(R.string.special_badge);
            } catch (Exception ignored) {
            }
        }
        return "Special";
    }

    private static String profileFallback(@Nullable Context context) {
        int langId = languageId(context);
        switch (langId) {
            case 1: return "പ്രൊഫൈൽ";
            case 2: return "प्रोफ़ाइल";
            case 3: return "ప్రొఫైల్";
            case 4: return "சுயவிவரம்";
            case 5: return "ಪ್ರೊಫೈಲ್";
            case 6: return "ପ୍ରୋଫାଇଲ୍";
            default:
                if (context != null) {
                    try {
                        return context.getString(R.string.Profile);
                    } catch (Exception ignored) {
                    }
                }
                return "Profile";
        }
    }

    private static String orPack(@Nullable String translator, String fallback) {
        if (translator != null) {
            String t = translator.trim();
            if (!t.isEmpty() && !"null".equalsIgnoreCase(t)) {
                return t;
            }
        }
        return fallback;
    }

    private static int languageId(@Nullable Context context) {
        if (context == null) return 0;
        try {
            return SharedPrefsData.getInt(
                    context.getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
        } catch (Exception ignored) {
            return 0;
        }
    }
}
