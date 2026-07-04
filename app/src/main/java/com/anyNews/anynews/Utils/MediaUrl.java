package com.anyNews.anynews.Utils;

import android.net.Uri;
import android.text.TextUtils;

import com.anyNews.anynews.models.FollowersProfileModel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Rewrites only fixed placeholder / static assets from legacy AWS URLs to OCI.
 * Does not touch article images, user-uploaded photos, or other dynamic media.
 */
public final class MediaUrl {

    public static final String OCI_BASE =
            "https://objectstorage.ap-hyderabad-1.oraclecloud.com/n/axknm73bvebv/b/anynewsstorage/o/";

    public static final String DEFAULT_PROFILE = OCI_BASE + "user_profile.webp";

    /** App / API placeholder files — upload these once to OCI; no bulk article migration. */
    private static final Set<String> STATIC_OBJECT_KEYS = new HashSet<>(Arrays.asList(
            "user_profile.webp",
            "user_profile.png",
            "video_thumb.png",
            "full_thumb.webp",
            "half_thumb.webp",
            "profile_icon.webp",
            "logo.png",
            "Test-Animation-1.gif"
    ));

    private MediaUrl() {
    }

    /** Use for profile pics, comment avatars, writer fallbacks — not feed images. */
    public static String resolve(String url) {
        if (TextUtils.isEmpty(url)) {
            return DEFAULT_PROFILE;
        }
        String trimmed = url.trim();
        if (trimmed.contains("objectstorage.") && trimmed.contains("oraclecloud.com")) {
            return trimmed;
        }
        String key = extractObjectKey(trimmed);
        if (key != null && isStaticObjectKey(key) && isLegacyHost(trimmed)) {
            return OCI_BASE + key;
        }
        return trimmed;
    }

    public static void normalizeFollowProfile(FollowersProfileModel.Datum profile) {
        if (profile == null) {
            return;
        }
        profile.setProfilePic(resolve(profile.getProfilePic()));
    }

    private static boolean isLegacyHost(String url) {
        return url.contains("amazonaws.com") || url.contains("cloudfront.net");
    }

    private static boolean isStaticObjectKey(String key) {
        String normalized = key;
        int query = normalized.indexOf('?');
        if (query >= 0) {
            normalized = normalized.substring(0, query);
        }
        normalized = normalized.toLowerCase(Locale.US);
        if (STATIC_OBJECT_KEYS.contains(normalized)) {
            return true;
        }
        return normalized.endsWith("/user_profile.webp")
                || normalized.endsWith("/user_profile.png");
    }

    private static String extractObjectKey(String url) {
        try {
            Uri uri = Uri.parse(url);
            String path = uri.getPath();
            if (path == null) {
                return null;
            }
            while (path.startsWith("/")) {
                path = path.substring(1);
            }
            return path.isEmpty() ? null : path;
        } catch (Exception e) {
            return null;
        }
    }
}
