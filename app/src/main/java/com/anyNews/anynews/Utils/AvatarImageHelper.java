package com.anyNews.anynews.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import com.anyNews.anynews.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.picasso.Picasso;

/**
 * Default avatar for feed author chips / followers when URL is missing or load fails
 * (light-blue person silhouette).
 *
 * <p>Profile self-avatar empty state uses {@link #PROFILE_EMPTY} + camera badge so
 * signed-in users see a clear “tap to upload” affordance instead of a person icon.
 */
public final class AvatarImageHelper {

    /** Feed / other-user empty: light-blue circle + white person. */
    @DrawableRes
    public static final int DEFAULT = R.drawable.ic_user_profile;

    /** Own profile empty photo: camera illustration (tap to add). */
    @DrawableRes
    public static final int PROFILE_EMPTY = R.drawable.ic_profile_add_photo;

    private static final DisplayImageOptions UIL_AVATAR_OPTIONS =
            new DisplayImageOptions.Builder()
                    .showImageOnLoading(DEFAULT)
                    .showImageForEmptyUri(DEFAULT)
                    .showImageOnFail(DEFAULT)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();

    private AvatarImageHelper() {
    }

    /** True when the value cannot be used as a network image URL. */
    public static boolean isMissing(@Nullable String url) {
        if (TextUtils.isEmpty(url)) {
            return true;
        }
        String t = url.trim();
        return t.isEmpty()
                || "null".equalsIgnoreCase(t)
                || "undefined".equalsIgnoreCase(t)
                || "none".equalsIgnoreCase(t)
                || "n/a".equalsIgnoreCase(t)
                || "/".equals(t);
    }

    /**
     * True when URL is missing or the shared default silhouette asset
     * ({@code user_profile.webp} / similar) — not a real uploaded photo.
     */
    public static boolean isUnusablePhoto(@Nullable String url) {
        if (isMissing(url)) {
            return true;
        }
        String lower = url.trim().toLowerCase();
        return lower.contains("user_profile.webp")
                || lower.contains("user_profile.png")
                || lower.contains("profile_icon.webp")
                || lower.contains("/avatar.png");
    }

    public static DisplayImageOptions uilOptions() {
        return UIL_AVATAR_OPTIONS;
    }

    public static void setDefault(ImageView imageView) {
        if (imageView != null) {
            imageView.setImageResource(DEFAULT);
            imageView.setTag(null);
        }
    }

    /** Own profile: empty photo shows camera / add-photo, not person silhouette. */
    public static void setProfileEmpty(ImageView imageView) {
        if (imageView != null) {
            Picasso.get().cancelRequest(imageView);
            imageView.setImageResource(PROFILE_EMPTY);
            imageView.setTag(null);
        }
    }

    /**
     * Load into own-profile avatar. Missing URL → add-photo illustration
     * (not the feed person silhouette).
     */
    public static void loadProfilePicasso(ImageView imageView, @Nullable String url) {
        if (imageView == null) {
            return;
        }
        if (isMissing(url)) {
            setProfileEmpty(imageView);
            return;
        }
        Picasso.get()
                .load(MediaUrl.resolve(url))
                .placeholder(PROFILE_EMPTY)
                .error(PROFILE_EMPTY)
                .into(imageView);
    }

    /** Picasso load with guest blue avatar for missing / failed URLs. */
    public static void loadPicasso(ImageView imageView, @Nullable String url) {
        if (imageView == null) {
            return;
        }
        if (isMissing(url)) {
            Picasso.get().cancelRequest(imageView);
            setDefault(imageView);
            return;
        }
        // MediaUrl.resolve already beacons OCI hosts via MediaAccessReporter.
        Picasso.get()
                .load(MediaUrl.resolve(url))
                .placeholder(DEFAULT)
                .error(DEFAULT)
                .into(imageView);
    }

    /** Universal Image Loader path used by home feed author chips. */
    public static void loadUil(ImageLoader imageLoader, ImageView imageView, @Nullable String url) {
        if (imageView == null) {
            return;
        }
        try {
            Context ctx = imageView.getContext();
            if (ctx != null && !ReporterPostGateHelper.isShowWriterPhotoOnCards(ctx)) {
                imageView.setVisibility(android.view.View.GONE);
                return;
            }
            if (imageView.getVisibility() == android.view.View.GONE) {
                imageView.setVisibility(android.view.View.VISIBLE);
            }
        } catch (Exception ignored) {
        }
        if (imageLoader == null || isMissing(url)) {
            setDefault(imageView);
            return;
        }
        String resolved = MediaUrl.resolve(url);
        Object tag = imageView.getTag();
        if (tag != null && resolved.equals(tag)) {
            return;
        }
        imageLoader.displayImage(resolved, imageView, UIL_AVATAR_OPTIONS, null);
        imageView.setTag(resolved);
    }

    /** Glide + circleCrop for circular profile ImageViews. */
    public static void loadGlideCircle(Context context, ImageView imageView, @Nullable String url) {
        if (imageView == null || context == null) {
            return;
        }
        if (isMissing(url)) {
            setDefault(imageView);
            return;
        }
        Glide.with(context)
                .load(MediaUrl.resolve(url))
                .placeholder(DEFAULT)
                .error(DEFAULT)
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }
}
