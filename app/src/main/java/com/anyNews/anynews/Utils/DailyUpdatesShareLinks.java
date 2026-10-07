package com.anyNews.anynews.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anyNews.anynews.Activities.FuelDetailActivity;
import com.anyNews.anynews.Activities.GoldDetailActivity;
import com.anyNews.anynews.Activities.TeluguCalendarActivity;
import com.anyNews.anynews.Activities.TtdDetailActivity;
import com.anyNews.anynews.Activities.WeatherDetailActivity;

import java.util.Locale;

/**
 * Outbound Daily Updates share links.
 *
 * <p>Built on the phone from the Admin share domain already saved at splash
 * ({@link SharePublicBaseHelper}). No extra network call. Article short codes
 * name a post id, so they cannot point at these screens — the path is
 * {@code {shareBase}/du/{page}?lang={LanguageID}}.
 */
public final class DailyUpdatesShareLinks {

    public static final String EXTRA_PAGE = "du_share_page";
    public static final String EXTRA_LANG = "du_share_lang";

    private DailyUpdatesShareLinks() {
    }

    @Nullable
    public static String canonicalKey(@Nullable String raw) {
        if (raw == null) {
            return null;
        }
        String key = raw.trim().toLowerCase(Locale.US);
        int cut = key.indexOf('?');
        if (cut >= 0) {
            key = key.substring(0, cut);
        }
        int slash = key.indexOf('/');
        if (slash >= 0) {
            key = key.substring(0, slash);
        }
        switch (key) {
            case "rashi":
            case "raasi":
            case "rasi":
            case "rashiphalalu":
                return "rashi";
            case "panchangam":
            case "panchang":
            case "calendar":
                return "panchangam";
            case "fuel":
            case "petrol":
                return "fuel";
            case "gold":
            case "silver":
                return "gold";
            case "weather":
                return "weather";
            case "ttd":
                return "ttd";
            default:
                return null;
        }
    }

    /**
     * Page key from {@code /du/{page}} or {@code anynews://du/{page}}.
     * Null when this URI is an article or job share.
     */
    @Nullable
    public static String pageFromUri(@Nullable Uri uri) {
        if (uri == null) {
            return null;
        }
        String scheme = uri.getScheme();
        String host = uri.getHost();
        if (scheme != null && "anynews".equalsIgnoreCase(scheme)
                && host != null && "du".equalsIgnoreCase(host)) {
            return canonicalKey(firstPathSegment(uri.getPath()));
        }
        String path = uri.getPath();
        if (path == null || path.isEmpty()) {
            return null;
        }
        String[] segments = path.split("/");
        for (int i = 0; i < segments.length; i++) {
            String seg = segments[i];
            if (seg != null && "du".equalsIgnoreCase(seg) && i + 1 < segments.length) {
                return canonicalKey(segments[i + 1]);
            }
        }
        return null;
    }

    public static int langFromUri(@Nullable Uri uri) {
        if (uri == null) {
            return -1;
        }
        try {
            String raw = uri.getQueryParameter("lang");
            if (raw == null || raw.isEmpty()) {
                return -1;
            }
            int n = Integer.parseInt(raw.trim());
            if (n < 0 || n > 10) {
                return -1;
            }
            return n;
        } catch (Exception e) {
            return -1;
        }
    }

    @NonNull
    public static String url(@Nullable Context context, @NonNull String pageKey) {
        String key = canonicalKey(pageKey);
        if (key == null) {
            key = "rashi";
        }
        String base = SharePublicBaseHelper.getBaseUrl(context);
        int lang = 0;
        if (context != null) {
            try {
                lang = SharedPrefsData.getInt(
                        context.getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
            } catch (Exception ignored) {
            }
        }
        if (lang < 0) {
            lang = 0;
        }
        return base + "/du/" + key + "?lang=" + lang;
    }

    @NonNull
    public static String withLink(
            @Nullable Context context, @Nullable String body, @NonNull String pageKey) {
        String link = url(context, pageKey);
        String text = body == null ? "" : body.trim();
        if (text.contains(link)) {
            return text;
        }
        if (text.isEmpty()) {
            return link;
        }
        return text + "\n" + link;
    }

    @NonNull
    public static Intent openIntent(
            @NonNull Context context, @NonNull String pageKey, int shareLang) {
        String key = canonicalKey(pageKey);
        if (key == null) {
            key = "rashi";
        }
        Intent intent;
        switch (key) {
            case "panchangam":
                intent = new Intent(context, TeluguCalendarActivity.class);
                intent.putExtra(EXTRA_PAGE, "panchangam");
                break;
            case "rashi":
                intent = new Intent(context, TeluguCalendarActivity.class);
                intent.putExtra(EXTRA_PAGE, "rashi");
                break;
            case "fuel":
                intent = new Intent(context, FuelDetailActivity.class);
                break;
            case "gold":
                intent = new Intent(context, GoldDetailActivity.class);
                break;
            case "weather":
                intent = new Intent(context, WeatherDetailActivity.class);
                break;
            case "ttd":
                intent = new Intent(context, TtdDetailActivity.class);
                break;
            default:
                intent = new Intent(context, TeluguCalendarActivity.class);
                intent.putExtra(EXTRA_PAGE, "rashi");
                break;
        }
        intent.putExtra(EXTRA_LANG, shareLang);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return intent;
    }

    public static void beginLanguage(@NonNull Activity activity) {
        int lang = -1;
        try {
            if (activity.getIntent() != null && activity.getIntent().hasExtra(EXTRA_LANG)) {
                lang = activity.getIntent().getIntExtra(EXTRA_LANG, -1);
            }
        } catch (Exception ignored) {
        }
        DailyUpdatesShareLanguage.begin(activity, lang);
    }

    @Nullable
    private static String firstPathSegment(@Nullable String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        String trimmed = path.startsWith("/") ? path.substring(1) : path;
        int slash = trimmed.indexOf('/');
        if (slash >= 0) {
            trimmed = trimmed.substring(0, slash);
        }
        return trimmed;
    }
}
