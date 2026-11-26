package com.anyNews.anynews.Utils;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.LanguageFontConfig;
import com.anyNews.anynews.Pojo.StartActivityReqModel;
import com.anyNews.anynews.Pojo.StartingAppRequest;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

/**
 * Way2-style deferred Splash_Screen config refresh for lean returning opens.
 * <p>
 * Lean StartingActivity stays zero-API (logo → Home fast). Home (or another
 * owner) may call {@link #maybeRefreshInBackground} once; this hits the server
 * only when {@link OpenPathCache#shouldRefreshSplashConfig} says the ~24h
 * throttle expired. Never downloads opening/festival banners
 * ({@link FestivalSplashHelper} remains disabled). Font fields are skipped when
 * {@link FontSettingsHelper#APPLY_REMOTE_FONT_SETTINGS} is false (baked
 * production defaults). Other harmless prefs (e.g. update banner subtitle) still apply.
 */
public final class OpenPathSplashConfigRefresher {

    private static final String TAG = "OpenPathSplashCfg";
    /** Same key as StartingActivity blocking splash — coalesce if both enqueue. */
    private static final String DEDUPE_KEY = "splash_v3";
    private static final Gson GSON = new Gson();
    /** One attempt per process open (push/warm stampede guard). */
    private static final AtomicBoolean inFlight = new AtomicBoolean(false);
    /**
     * When true, do not hit Splash_Screen on main-feed cold open; refresh when the
     * user opens Daily Updates, Special, Local, etc. ({@link #maybeRefreshOnAuxiliaryOpen}).
     */
    public static final boolean DEFER_ON_MAIN_FEED_OPEN = true;

    private OpenPathSplashConfigRefresher() {
    }

    /** Splash/admin refresh after user leaves main feed for an auxiliary tab/flow. */
    public static void maybeRefreshOnAuxiliaryOpen(@Nullable Context context) {
        if (!DEFER_ON_MAIN_FEED_OPEN) {
            return;
        }
        maybeRefreshInBackground(context);
    }

    /**
     * Non-blocking. No-op when identity incomplete, throttle fresh, or already
     * refreshing this process.
     */
    public static void maybeRefreshInBackground(@Nullable Context context) {
        if (context == null) {
            return;
        }
        final Context app = context.getApplicationContext();
        try {
            if (!OpenPathCache.canFastOpenHome(app)) {
                return;
            }
            if (!OpenPathCache.shouldRefreshSplashConfig(app)) {
                Logone.v(TAG + ": splash config fresh — skip background refresh");
                return;
            }
            if (!inFlight.compareAndSet(false, true)) {
                Logone.v(TAG + ": splash config refresh already in flight");
                return;
            }
            JsonObject body = buildReturningSplashBody(app);
            if (body == null) {
                inFlight.set(false);
                return;
            }
            APIService api = RestApiClient.getClient().create(APIService.class);
            Call<EncreptionRequest> call = api.Splash_Screen("v3", body);
            Logone.v(TAG + ": throttled background Splash_Screen (configOnly admin/subtitle)");
            NetworkCallDeduper.enqueueUnique(DEDUPE_KEY, call, new Callback<EncreptionRequest>() {
                @Override
                public void onResponse(
                        @NonNull Call<EncreptionRequest> call,
                        @NonNull Response<EncreptionRequest> response) {
                    try {
                        if (!response.isSuccessful() || response.body() == null
                                || response.body().getData() == null) {
                            Logone.w(TAG + ": background splash empty/unsuccessful");
                            inFlight.set(false);
                            return;
                        }
                        String plain = decrypt(response.body().getData());
                        StartActivityReqModel model =
                                GSON.fromJson(plain, StartActivityReqModel.class);
                        if (model == null || !Boolean.TRUE.equals(model.getSuccess())
                                || model.getData() == null) {
                            Logone.w(TAG + ": background splash parse/success=false");
                            inFlight.set(false);
                            return;
                        }
                        applySoftFontConfig(app, model);
                        OpenPathCache.markSplashSuccess(app);
                        Logone.v(TAG + ": background splash config applied + stamped");
                        // TTL stamp gates the next open; clear process lock after success.
                        inFlight.set(false);
                    } catch (Exception e) {
                        Logone.w(e, TAG + ": background splash apply failed");
                        inFlight.set(false);
                    }
                }

                @Override
                public void onFailure(
                        @NonNull Call<EncreptionRequest> call,
                        @NonNull Throwable t) {
                    Logone.w(t, TAG + ": background splash network failed");
                    inFlight.set(false);
                }
            });
        } catch (Exception e) {
            inFlight.set(false);
            Logone.w(e, TAG + ": maybeRefreshInBackground failed");
        }
    }

    private static void applySoftFontConfig(
            @NonNull Context app, @NonNull StartActivityReqModel model) {
        // Remote fonts OFF by default — keep baked production sizes for all languages
        // and avoid custom-font HTTP downloads / layout thrash on daily refresh.
        if (FontSettingsHelper.APPLY_REMOTE_FONT_SETTINGS) {
            LanguageFontConfig fontConfig = model.getData().getFontConfig();
            Map<String, LanguageFontConfig> fontSettings =
                    model.getData().getLanguageFontSettings();
            int languageId = model.getData().getLanguageId() != null
                    ? model.getData().getLanguageId()
                    : 0;
            if (languageId <= 0) {
                languageId = SharedPrefsData.getInt(app, Constants.LanguageID, Constants.PREF_NAME);
            }

            Map<String, String> customFonts = model.getData().getCustomFontUrls();
            if (customFonts != null) {
                FontSettingsHelper.saveCustomFontUrls(app, customFonts);
                FontSettingsHelper.prefetchCustomFonts(app);
            }

            if (fontSettings != null && !fontSettings.isEmpty()) {
                FontSettingsHelper.saveLanguageFontSettings(app, fontSettings);
            } else if (fontConfig != null && languageId > 0) {
                Map<String, LanguageFontConfig> fallback = new HashMap<>();
                fallback.put(String.valueOf(languageId), fontConfig);
                FontSettingsHelper.saveLanguageFontSettings(app, fallback);
            }
        } else {
            Logone.v(TAG + ": skip remote font fields (baked productionDefault)");
        }

        String updateSubtitle = model.getData().getUpdateBannerSubtitle();
        SharedPrefsData.putString(
                app,
                Constants.UPDATE_BANNER_SUBTITLE,
                updateSubtitle != null ? updateSubtitle.trim() : "",
                Constants.PREF_NAME);
    }

    @Nullable
    private static JsonObject buildReturningSplashBody(@NonNull Context app) {
        try {
            StartingAppRequest request = new StartingAppRequest();
            request.setAppVersion(BuildConfig.VERSION_CODE);
            request.setDraftId(SharedPrefsData.getInt(app, Constants.DraftID, Constants.PREF_NAME));
            request.setDeviceID(resolveDeviceId(app));
            request.setInstall_uuid(ensureInstallUuid(app));
            request.setReferralLink("" + SharedPrefsData.getString(
                    app, Constants.REFERAL_LINK, Constants.PREF_NAME));
            request.setReferralLink2("" + SharedPrefsData.getString(
                    app, Constants.REFERAL_LINK2, Constants.PREF_NAME));
            request.setReferredBy(SharedPrefsData.getLong(
                    app, Constants.CUSST_ID, Constants.PREF_NAME));
            String fcm = SharedPrefsData.getString(app, Constants.FCM_TOKEN, Constants.PREF_NAME);
            request.setFcmToken(fcm != null ? fcm : "");
            request.setLanguage_id("" + SharedPrefsData.getInt(
                    app, Constants.LanguageID, Constants.PREF_NAME));
            request.setApp_version_name("" + BuildConfig.VERSION_NAME);
            request.setApp_version_number("" + BuildConfig.VERSION_CODE);

            String json = GSON.toJson(request);
            String encrypted = encrypt(json);
            EncritonRequestData wrapper = new EncritonRequestData();
            wrapper.setData(encrypted);
            return GSON.toJsonTree(wrapper).getAsJsonObject();
        } catch (Exception e) {
            Logone.w(e, TAG + ": buildReturningSplashBody failed");
            return null;
        }
    }

    @NonNull
    private static String resolveDeviceId(@NonNull Context app) {
        String stored = SharedPrefsData.getString(app, Constants.DeviceID, Constants.PREF_NAME);
        if (!TextUtils.isEmpty(stored) && !"null".equalsIgnoreCase(stored)) {
            return stored;
        }
        String androidId = null;
        try {
            androidId = Settings.Secure.getString(
                    app.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception ignored) {
        }
        if (!TextUtils.isEmpty(androidId) && !"null".equalsIgnoreCase(androidId)) {
            SharedPrefsData.putString(app, Constants.DeviceID, androidId, Constants.PREF_NAME);
            return androidId;
        }
        String fallback = "install_" + ensureInstallUuid(app);
        SharedPrefsData.putString(app, Constants.DeviceID, fallback, Constants.PREF_NAME);
        return fallback;
    }

    @NonNull
    private static String ensureInstallUuid(@NonNull Context app) {
        String existing = SharedPrefsData.getString(app, Constants.INSTALL_UUID, Constants.PREF_NAME);
        if (!TextUtils.isEmpty(existing) && !"null".equalsIgnoreCase(existing)) {
            return existing;
        }
        String created = UUID.randomUUID().toString();
        SharedPrefsData.putString(app, Constants.INSTALL_UUID, created, Constants.PREF_NAME);
        return created;
    }
}
