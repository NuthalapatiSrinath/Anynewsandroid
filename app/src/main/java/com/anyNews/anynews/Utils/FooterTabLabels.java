package com.anyNews.anynews.Utils;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;

import com.anyNews.anynews.Pojo.LanguageModelResponse;
import com.anyNews.anynews.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Shared News | Events/Special | Calendar | Video News | Profile footer titles.
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
            MenuItem special = menu.findItem(R.id.navigation_catagiries);
            if (special != null) {
                String events = d != null ? d.getEvents() : null;
                special.setTitle(orPack(events, specialFallback(context)));
            }
            MenuItem calendar = menu.findItem(R.id.navigation_stricker);
            if (calendar != null) {
                calendar.setTitle(HomeChromeActionLabels.calendar(context));
            }
            MenuItem videoNews = menu.findItem(R.id.navigation_status);
            if (videoNews != null) {
                videoNews.setTitle(videoNews(context));
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
     * Short footer Video tab label by in-app {@link Constants#LanguageID}
     * (1 ML · 2 HI · 3 TE · 4 TA · 5 KN · 6 OR). English keeps "Video News".
     * Same LanguageID switch pattern as {@link #profileFallback} / Calendar.
     */
    public static String videoNews(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: // Malayalam
                return "വീഡിയോ";
            case 2: // Hindi
                return "वीडियो";
            case 3: // Telugu — short (matches R.string.video_news_te)
                return "వీడియో";
            case 4: // Tamil
                return "வீடியோ";
            case 5: // Kannada
                return "ವೀಡಿಯೊ";
            case 6: // Odia
                return "ଭିଡିଓ";
            default:
                if (context != null) {
                    try {
                        return context.getString(R.string.video_news);
                    } catch (Exception ignored) {
                    }
                }
                return "Video News";
        }
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
