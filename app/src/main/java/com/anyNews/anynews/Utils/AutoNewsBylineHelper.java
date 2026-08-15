package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import com.anyNews.anynews.Pojo.LanguageModelResponse;
import com.anyNews.anynews.R;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Client-only display rules for desk Auto News / admin-posted articles.
 * Server consumer APIs now strip "Name (Auto News)" + role "Auto News" before
 * the app sees them (old APKs benefit from server sanitization). Keep this
 * helper so any remaining suffix/role variants still render as clean name + Reporter.
 *
 * Detection mirrors {@code anynewsserver/helpers/writerByline.js}
 * {@code isAutoNewsByline} plus role / suffix variants seen in the wild.
 */
public final class AutoNewsBylineHelper {

    private static final Pattern PAREN_AUTO_NEWS =
            Pattern.compile("\\(\\s*Auto\\s*News\\s*\\)", Pattern.CASE_INSENSITIVE);
    private static final Pattern DASH_AUTO_NEWS =
            Pattern.compile("\\s*[-–—]\\s*Auto\\s*News\\s*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern TRAILING_AUTO_NEWS =
            Pattern.compile("\\s+Auto\\s*News\\s*$", Pattern.CASE_INSENSITIVE);

    private AutoNewsBylineHelper() {
    }

    /** True when the feed item should hide Auto News branding on the writer strip. */
    public static boolean isAutoNewsPost(ModelClass.Datum item) {
        if (item == null) {
            return false;
        }
        if (isAutoNewsRole(item.getRole())) {
            return true;
        }
        return isAutoNewsByline(item.getWriterName());
    }

    /** Same idea as server isAutoNewsByline: name contains "(Auto News)". */
    public static boolean isAutoNewsByline(String writerName) {
        if (TextUtils.isEmpty(writerName)) {
            return false;
        }
        return PAREN_AUTO_NEWS.matcher(writerName).find()
                || DASH_AUTO_NEWS.matcher(writerName).find()
                || TRAILING_AUTO_NEWS.matcher(writerName.trim()).find();
    }

    public static boolean isAutoNewsRole(String role) {
        if (TextUtils.isEmpty(role)) {
            return false;
        }
        String r = role.trim().toLowerCase(Locale.US);
        return "auto news".equals(r) || "autonews".equals(r);
    }

    /**
     * Display name for the writer strip: admin name without Auto News suffix.
     * Safe to call for all posts (no-op when no branding suffix present).
     */
    public static String displayWriterName(String raw) {
        if (raw == null) {
            return "";
        }
        String name = raw.trim();
        if (name.isEmpty() || "null".equalsIgnoreCase(name)) {
            return "";
        }
        name = PAREN_AUTO_NEWS.matcher(name).replaceAll("").trim();
        name = DASH_AUTO_NEWS.matcher(name).replaceAll("").trim();
        name = TRAILING_AUTO_NEWS.matcher(name).replaceAll("").trim();
        name = name.replaceAll("\\s{2,}", " ").trim();
        // Drop trailing commas left by replacements like "Name, (Auto News)" edge cases.
        while (name.endsWith(",") || name.endsWith("-") || name.endsWith("–")) {
            name = name.substring(0, name.length() - 1).trim();
        }
        return name;
    }

    /**
     * Designation line under the name. Auto News posts always show Reporter
     * (translated via language pack / string resource when available).
     */
    public static String displayDesignation(
            Context context,
            LanguageModelResponse languageModelResponse,
            ModelClass.Datum item) {
        if (item == null) {
            return "";
        }
        if (isAutoNewsPost(item)) {
            return resolveReporterLabel(context, languageModelResponse);
        }
        String role = item.getRole();
        if (TextUtils.isEmpty(role) || "null".equalsIgnoreCase(role)) {
            return "";
        }
        return role;
    }

    public static String resolveReporterLabel(
            Context context, LanguageModelResponse languageModelResponse) {
        try {
            if (languageModelResponse != null
                    && languageModelResponse.getData() != null) {
                String translated = languageModelResponse.getData().getReporter();
                if (!TextUtils.isEmpty(translated) && !"null".equalsIgnoreCase(translated)) {
                    return translated.trim();
                }
            }
        } catch (Exception ignored) {
        }
        if (context != null) {
            try {
                return context.getString(R.string.profile_role_reporter);
            } catch (Exception ignored) {
            }
        }
        return "Reporter";
    }
}
