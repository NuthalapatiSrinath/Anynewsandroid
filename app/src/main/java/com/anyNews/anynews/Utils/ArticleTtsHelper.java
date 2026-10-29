package com.anyNews.anynews.Utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.anyNews.anynews.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Article-body Text-to-Speech for half-image cards.
 * Toggle speaker → speak body / stop; pulse glow while speaking.
 * Voice/locale/engine + placement from {@link ArticleTtsConfigHelper}.
 *
 * <p>Warm the engine on splash/Home so the first speaker tap is not blocked by
 * {@code new TextToSpeech()} + {@code getVoices()}. Stop on app background via
 * {@link ProcessLifecycleOwner} (and callers may also stop from Home onPause).
 */
public final class ArticleTtsHelper {

    private static final String UTTERANCE_PREFIX = "article_tts_body:";
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    /** Image overlay uses white glyphs; engagement row uses tintable action icons. */
    public static final int STYLE_IMAGE = 0;
    public static final int STYLE_ENGAGEMENT = 1;
    /** Peak uniform scale of the pink speaker ring. Layout padding must fit this. */
    static final float PULSE_MAX_SCALE = 1.55f;

    private static TextToSpeech tts;
    private static boolean ttsReady;
    private static boolean ttsInitializing;
    private static String preferredEngine = "";
    private static String appliedVoiceKey = "";
    private static boolean voicesPreloaded;
    private static Long speakingPostId;
    /** Id of the utterance we still want. Cleared on stop so a late onDone/onStart cannot restart UI or cut a newer read. */
    private static String activeUtteranceId;
    private static int utteranceSeq;
    private static final List<SpeakerSlot> activeSlots = new ArrayList<>();
    private static final List<Runnable> pendingReady = new ArrayList<>();
    private static AnimatorSet pulseAnim;
    private static WeakReference<View> pulseAnimTarget;
    private static final AtomicBoolean lifecycleRegistered = new AtomicBoolean(false);
    /**
     * After a fail-path Google engine recovery succeeds once, keep preferring Google for
     * this process so the next tap does not re-hit a broken system engine. Never set on
     * the happy path.
     */
    private static boolean sessionPreferGoogle;
    /** One Google-engine retry per speaker tap; reset at the start of {@link #speak}. */
    private static boolean googleTriedThisSpeak;
    /** Toast + install intents once per tap (never every frame / warm-up). */
    private static final AtomicBoolean helpPromptedThisSpeak = new AtomicBoolean(false);
    /** Transient: next {@link #ensureTts} must bind {@code com.google.android.tts}. */
    private static boolean forceGoogleNextInit;
    /**
     * Bumped on every {@link #stopAll}/{@link #stopIfNotCurrent} so a late Google-engine
     * {@code onInit} / fail-path retry cannot resume speak after the user left the article.
     */
    private static int speakEpoch;

    private ArticleTtsHelper() {}

    /**
     * Call once from {@link Application#onCreate()}. Registers process-background
     * stop and posts an early warm-up when TTS is already enabled in prefs.
     */
    public static void init(@NonNull Application application) {
        ensureAppLifecycle(application);
        MAIN.post(() -> warmUp(application));
    }

    /**
     * Init the TTS engine + locale/voice catalog before the user taps speaker.
     * Safe to call repeatedly (splash after config, Home onCreate, etc.).
     */
    public static void warmUp(@Nullable Context context) {
        if (context == null) return;
        final Context app = context.getApplicationContext();
        ensureAppLifecycle(app);
        if (!ArticleTtsConfigHelper.isEnabled(app)) {
            return;
        }
        ensureTts(
                app,
                () -> {
                    applyVoiceConfig(app);
                    preloadVoices();
                });
    }

    private static void ensureAppLifecycle(@NonNull Context context) {
        if (!lifecycleRegistered.compareAndSet(false, true)) {
            return;
        }
        try {
            ProcessLifecycleOwner.get()
                    .getLifecycle()
                    .addObserver(
                            new LifecycleObserver() {
                                @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
                                public void onAppBackground() {
                                    // Recents / Home / other app — stop voice immediately.
                                    stopAll();
                                }
                            });
        } catch (Exception ignored) {
            lifecycleRegistered.set(false);
        }
    }

    /**
     * Bind a speaker button. Visibility is controlled by the caller via {@code show}
     * (master + placement). Speaks sanitized body; pulse while speaking.
     */
    public static void bindButton(
            @Nullable Context context,
            @Nullable ImageView speakerBtn,
            @Nullable View pulseView,
            @Nullable Long postId,
            @Nullable CharSequence bodyText,
            boolean show,
            int style) {
        if (speakerBtn == null) return;
        if (context == null || !show) {
            speakerBtn.setVisibility(View.GONE);
            if (pulseView != null) {
                pulseView.setVisibility(View.GONE);
                stopPulseOn(pulseView);
            }
            speakerBtn.setOnClickListener(null);
            unregisterSlot(speakerBtn);
            return;
        }
        speakerBtn.setVisibility(View.VISIBLE);
        final boolean thisSpeaking = isSpeakingPost(postId) && isSpeaking();
        registerOrUpdateSlot(speakerBtn, pulseView, style, postId);
        applyUiState(speakerBtn, pulseView, thisSpeaking, style);
        final Context app = context.getApplicationContext();
        final Long id = postId;
        final String text = sanitizeBody(bodyText);
        speakerBtn.setOnClickListener(
                v -> toggle(app, id, text, speakerBtn, pulseView, style));
    }

    /** Stop if flipping away from the speaking article. */
    public static void stopIfNotCurrent(@Nullable Long currentPostId) {
        if (speakingPostId == null) return;
        if (currentPostId != null && speakingPostId.equals(currentPostId)) return;
        stopSpeaking(true, true);
    }

    /**
     * Hard stop: leave article / Daily Updates / News reload / Profile / background.
     * Cancels in-flight engine init callbacks so audio cannot restart after navigation.
     */
    public static void stopAll() {
        stopSpeaking(true, true);
    }

    private static void toggle(
            @NonNull Context app,
            @Nullable Long postId,
            @NonNull String text,
            @NonNull ImageView speakerBtn,
            @Nullable View pulseView,
            int style) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        final boolean thisSpeaking = isSpeakingPost(postId) && isSpeaking();
        if (thisSpeaking) {
            stopSpeaking(true, false);
            return;
        }
        speak(app, postId, text, speakerBtn, pulseView, style);
    }

    private static void speak(
            @NonNull Context app,
            @Nullable Long postId,
            @NonNull String text,
            @NonNull ImageView speakerBtn,
            @Nullable View pulseView,
            int style) {
        stopSpeaking(false, false);
        speakingPostId = postId;
        googleTriedThisSpeak = false;
        helpPromptedThisSpeak.set(false);
        final int epoch = speakEpoch;
        final String utteranceId = UTTERANCE_PREFIX + (++utteranceSeq);
        activeUtteranceId = utteranceId;
        registerOrUpdateSlot(speakerBtn, pulseView, style, postId);
        applyAllSlotsUi(true);
        ensureTts(
                app,
                () -> {
                    if (epoch != speakEpoch) {
                        return;
                    }
                    continueSpeakAfterReady(app, postId, text, utteranceId);
                });
    }

    /**
     * Happy path: same {@link #applyVoiceConfig} + {@code tts.speak} as before. Fail path
     * only: try Google TTS once, then toast + install/settings (never wrong-language speak).
     */
    private static void continueSpeakAfterReady(
            @NonNull Context app,
            @Nullable Long postId,
            @NonNull String text,
            @NonNull String utteranceId) {
        if (!isSpeakAttemptActive(postId, utteranceId)) {
            return;
        }
        // Happy path unchanged: same applyVoiceConfig + speak API.
        // Only abort (Google retry → toast + install/settings) when locale/voice data is missing
        // or speak() rejects the utterance.
        if (!applyVoiceConfig(app)) {
            handleSpeakFailure(app, postId, text, utteranceId, true);
            return;
        }
        int result = enqueueSpeak(text, utteranceId);
        if (result != TextToSpeech.SUCCESS) {
            handleSpeakFailure(app, postId, text, utteranceId, false);
        }
    }

    private static boolean isSpeakAttemptActive(
            @Nullable Long postId, @NonNull String utteranceId) {
        return utteranceId.equals(activeUtteranceId)
                && speakingPostId != null
                && (postId == null || postId.equals(speakingPostId));
    }

    private static int enqueueSpeak(@NonNull String text, @NonNull String utteranceId) {
        if (tts == null) return TextToSpeech.ERROR;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
            return tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId);
        }
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
        return tts.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    /**
     * Fail path only. Prefer Google TTS engine if we have not already, then speak the same
     * text with the same locale rules. If still failing: one toast + open install/settings.
     */
    private static void handleSpeakFailure(
            @NonNull Context app,
            @Nullable Long postId,
            @NonNull String text,
            @NonNull String utteranceId,
            boolean languageMissing) {
        if (!isSpeakAttemptActive(postId, utteranceId)) {
            return;
        }
        if (!googleTriedThisSpeak && !isUsingGoogleEngine()) {
            googleTriedThisSpeak = true;
            forceGoogleNextInit = true;
            ensureTts(
                    app,
                    () -> {
                        if (!isSpeakAttemptActive(postId, utteranceId)) {
                            return;
                        }
                        if (!applyVoiceConfig(app)) {
                            stopSpeaking(true, false);
                            promptMissingLanguageData(app);
                            return;
                        }
                        int result = enqueueSpeak(text, utteranceId);
                        if (result != TextToSpeech.SUCCESS) {
                            stopSpeaking(true, false);
                            if (languageMissing) {
                                promptMissingLanguageData(app);
                            } else {
                                promptMissingEngine(app);
                            }
                            return;
                        }
                        // Google recovered this speak — keep it for the rest of the process.
                        sessionPreferGoogle = true;
                    });
            return;
        }
        stopSpeaking(true, false);
        if (languageMissing) {
            promptMissingLanguageData(app);
        } else {
            promptMissingEngine(app);
        }
    }

    private static boolean isUsingGoogleEngine() {
        return ArticleTtsConfigHelper.GOOGLE_TTS_PACKAGE.equals(preferredEngine);
    }

    private static void ensureTts(@NonNull Context app, @NonNull Runnable onReady) {
        String enginePref = ArticleTtsConfigHelper.getEngine(app);
        boolean wantGoogle =
                forceGoogleNextInit
                        || sessionPreferGoogle
                        || ArticleTtsConfigHelper.ENGINE_GOOGLE.equals(enginePref);
        forceGoogleNextInit = false;
        String enginePkg =
                wantGoogle ? ArticleTtsConfigHelper.GOOGLE_TTS_PACKAGE : "";
        if (tts != null && ttsReady && preferredEngine.equals(enginePkg)) {
            onReady.run();
            return;
        }
        // Already spinning up the same engine — run as soon as onInit fires.
        if (ttsInitializing && tts != null && preferredEngine.equals(enginePkg)) {
            pendingReady.add(onReady);
            return;
        }
        pendingReady.clear();
        pendingReady.add(onReady);
        shutdownTtsEngine();
        ttsInitializing = true;
        preferredEngine = enginePkg;
        TextToSpeech.OnInitListener listener =
                status -> {
                    ttsInitializing = false;
                    ttsReady = status == TextToSpeech.SUCCESS;
                    List<Runnable> queued = new ArrayList<>(pendingReady);
                    pendingReady.clear();
                    if (!ttsReady) {
                        // User tapped speak (speakingPostId set); warmUp never sets it.
                        boolean userSpeak =
                                speakingPostId != null && activeUtteranceId != null;
                        final String failedEngine = preferredEngine;
                        if (userSpeak
                                && !googleTriedThisSpeak
                                && !ArticleTtsConfigHelper.GOOGLE_TTS_PACKAGE.equals(
                                        failedEngine)) {
                            // System/default engine failed to init — try Google once.
                            googleTriedThisSpeak = true;
                            forceGoogleNextInit = true;
                            final List<Runnable> retryQueued = new ArrayList<>(queued);
                            MAIN.post(
                                    () ->
                                            ensureTts(
                                                    app,
                                                    () -> {
                                                        for (Runnable r : retryQueued) {
                                                            try {
                                                                r.run();
                                                            } catch (Exception ignored) {
                                                            }
                                                        }
                                                    }));
                            return;
                        }
                        stopSpeaking(true, false);
                        if (userSpeak) {
                            promptMissingEngine(app);
                        }
                        return;
                    }
                    tts.setOnUtteranceProgressListener(
                            new UtteranceProgressListener() {
                                @Override
                                public void onStart(String utteranceId) {
                                    MAIN.post(() -> {
                                        if (!isActiveUtterance(utteranceId)) return;
                                        applyAllSlotsUi(true);
                                    });
                                }

                                @Override
                                public void onDone(String utteranceId) {
                                    MAIN.post(() -> {
                                        if (!isActiveUtterance(utteranceId)) return;
                                        stopSpeaking(true, false);
                                    });
                                }

                                @Override
                                public void onError(String utteranceId) {
                                    MAIN.post(() -> {
                                        if (!isActiveUtterance(utteranceId)) return;
                                        stopSpeaking(true, false);
                                    });
                                }

                                @Override
                                public void onRangeStart(
                                        String utteranceId, int start, int end, int frame) {
                                    MAIN.post(() -> {
                                        if (!isActiveUtterance(utteranceId)) return;
                                        nudgePulse();
                                    });
                                }
                            });
                    for (Runnable r : queued) {
                        try {
                            r.run();
                        } catch (Exception ignored) {
                        }
                    }
                };
        if (!TextUtils.isEmpty(enginePkg)) {
            tts = new TextToSpeech(app, listener, enginePkg);
        } else {
            tts = new TextToSpeech(app, listener);
        }
    }

    /**
     * Apply admin/app locale + voice. Returns {@code false} only when the reader's
     * language is LANG_MISSING_DATA / LANG_NOT_SUPPORTED (no silent wrong-language
     * speak). Success path timing and voice selection are unchanged.
     */
    private static boolean applyVoiceConfig(@NonNull Context app) {
        if (tts == null) return false;
        try {
            String localeTag = ArticleTtsConfigHelper.getLocale(app);
            String voiceWanted = ArticleTtsConfigHelper.getVoice(app);
            String key =
                    preferredEngine
                            + "|"
                            + localeTag
                            + "|"
                            + voiceWanted
                            + "|"
                            + languageId(app);
            if (key.equals(appliedVoiceKey) && voicesPreloaded) {
                return true;
            }
            Locale appLocale = localeForAppLanguage(app);
            Locale configured =
                    TextUtils.isEmpty(localeTag) ? null : localeFromTag(localeTag);
            // Admin may save one voice (often te-IN). Use it only when it is the
            // reader's language. Hindi/Kannada must not inherit the Telugu voice,
            // or ASCII numbers are spoken in Telugu while the script stays Hindi.
            Locale locale =
                    configured != null && sameLanguage(configured, appLocale)
                            ? configured
                            : appLocale;
            if (!applyEngineLanguage(locale)) {
                // Do not cache — retry after user installs voice data.
                appliedVoiceKey = "";
                return false;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Set<Voice> voices = tts.getVoices();
                voicesPreloaded = voices != null;
                if (voices != null && !voices.isEmpty()) {
                    Voice best = null;
                    // 1) Exact / substring name match from admin (browser voice name),
                    // only when that voice is the same language we just selected.
                    if (!TextUtils.isEmpty(voiceWanted)) {
                        String needle = voiceWanted.toLowerCase(Locale.US);
                        for (Voice v : voices) {
                            if (v == null || !voiceMatchesLanguage(v, locale)) continue;
                            String name =
                                    v.getName() == null
                                            ? ""
                                            : v.getName().toLowerCase(Locale.US);
                            if (name.contains(needle) || needle.contains(name)) {
                                best = v;
                                break;
                            }
                        }
                    }
                    // 2) Fallback: best voice for the resolved locale (language[+country]).
                    if (best == null) {
                        best = bestVoiceForLocale(voices, locale);
                    }
                    if (best != null) {
                        tts.setVoice(best);
                    }
                }
            } else {
                preloadVoices();
            }
            tts.setSpeechRate(1.0f);
            tts.setPitch(1.0f);
            appliedVoiceKey = key;
            return true;
        } catch (Exception ignored) {
            appliedVoiceKey = "";
            return false;
        }
    }

    /** Touch voice catalog once so first speak is not blocked by getVoices(). */
    private static void preloadVoices() {
        if (tts == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            voicesPreloaded = true;
            return;
        }
        if (voicesPreloaded) return;
        try {
            Set<Voice> voices = tts.getVoices();
            voicesPreloaded = voices != null;
        } catch (Exception ignored) {
            voicesPreloaded = false;
        }
    }

    /**
     * Prefer a network/high-quality voice whose locale matches language+country, then
     * language-only. Used when the admin-saved browser voice name is missing on-device.
     */
    @Nullable
    private static Voice bestVoiceForLocale(
            @NonNull Set<Voice> voices, @Nullable Locale wanted) {
        if (wanted == null) return null;
        String wantLang = wanted.getLanguage() == null ? "" : wanted.getLanguage();
        if (TextUtils.isEmpty(wantLang)) return null;
        String wantCountry =
                wanted.getCountry() == null
                        ? ""
                        : wanted.getCountry().toUpperCase(Locale.US);
        Voice langCountry = null;
        Voice langOnly = null;
        for (Voice v : voices) {
            if (v == null || v.getLocale() == null) continue;
            Locale vl = v.getLocale();
            String vlLang = vl.getLanguage() == null ? "" : vl.getLanguage();
            if (!wantLang.equalsIgnoreCase(vlLang)) continue;
            String vlCountry =
                    vl.getCountry() == null
                            ? ""
                            : vl.getCountry().toUpperCase(Locale.US);
            boolean countryMatch =
                    !TextUtils.isEmpty(wantCountry) && wantCountry.equals(vlCountry);
            if (countryMatch) {
                if (langCountry == null || prefersVoice(v, langCountry)) {
                    langCountry = v;
                }
            } else if (langOnly == null || prefersVoice(v, langOnly)) {
                langOnly = v;
            }
        }
        return langCountry != null ? langCountry : langOnly;
    }

    /** Prefer non-network when both exist? Actually network often sounds better for Google. Prefer higher quality / not requiresNetwork if local available — keep simple: prefer default-quality, then any. */
    private static boolean prefersVoice(@NonNull Voice candidate, @NonNull Voice incumbent) {
        // Prefer higher quality score when available; otherwise keep incumbent.
        try {
            return candidate.getQuality() > incumbent.getQuality();
        } catch (Exception ignored) {
            return false;
        }
    }

    private static int languageId(@Nullable Context context) {
        try {
            if (context != null) {
                return SharedPrefsData.getInt(
                        context.getApplicationContext(),
                        Constants.LanguageID,
                        Constants.PREF_NAME);
            }
        } catch (Exception ignored) {
        }
        return 0;
    }

    /**
     * Speak digits in the selected language. If that locale has no voice data,
     * try the language alone, then the device locale only when it is the same
     * language. Never substitute another language (e.g. English) silently.
     *
     * @return true when the engine accepted the reader's language
     */
    private static boolean applyEngineLanguage(@NonNull Locale locale) {
        if (tts == null) return false;
        if (isLanguageSet(tts.setLanguage(locale))) return true;
        String lang = locale.getLanguage();
        if (!TextUtils.isEmpty(lang) && !TextUtils.isEmpty(locale.getCountry())) {
            if (isLanguageSet(tts.setLanguage(new Locale(lang)))) return true;
        }
        Locale device = Locale.getDefault();
        if (sameLanguage(device, locale)) {
            return isLanguageSet(tts.setLanguage(device));
        }
        return false;
    }

    private static boolean isLanguageSet(int result) {
        return result != TextToSpeech.LANG_MISSING_DATA
                && result != TextToSpeech.LANG_NOT_SUPPORTED;
    }

    /**
     * Toast + open install/settings when the reader's language voice pack is missing.
     * Uses {@link TtsLanguageLocales} / LanguageID so every app language gets a message —
     * not Telugu-only. Once per speaker tap.
     */
    private static void promptMissingLanguageData(@NonNull Context app) {
        if (!helpPromptedThisSpeak.compareAndSet(false, true)) {
            return;
        }
        Locale locale = localeForAppLanguage(app);
        String nativeLabel = locale.getDisplayLanguage(locale);
        String enLabel = locale.getDisplayLanguage(Locale.ENGLISH);
        if (TextUtils.isEmpty(nativeLabel)) {
            nativeLabel = TextUtils.isEmpty(enLabel) ? "App" : enLabel;
        }
        if (TextUtils.isEmpty(enLabel)) {
            enLabel = nativeLabel;
        }
        final String msg;
        if (nativeLabel.equalsIgnoreCase(enLabel)) {
            msg = app.getString(R.string.tts_language_missing, enLabel);
        } else {
            msg = app.getString(R.string.tts_language_missing_bilingual, nativeLabel, enLabel);
        }
        showTtsToast(app, msg);
        openTtsInstallOrSettings(app);
    }

    /** Toast + open engine install when TextToSpeech onInit / speak fails. Once per tap. */
    private static void promptMissingEngine(@NonNull Context app) {
        if (!helpPromptedThisSpeak.compareAndSet(false, true)) {
            return;
        }
        Locale locale = localeForAppLanguage(app);
        String nativeLabel = locale.getDisplayLanguage(locale);
        String enLabel = locale.getDisplayLanguage(Locale.ENGLISH);
        final String msg;
        if (!TextUtils.isEmpty(nativeLabel)
                && !TextUtils.isEmpty(enLabel)
                && !nativeLabel.equalsIgnoreCase(enLabel)) {
            msg = app.getString(R.string.tts_engine_missing_bilingual, nativeLabel);
        } else {
            msg = app.getString(R.string.tts_engine_missing);
        }
        showTtsToast(app, msg);
        openTtsInstallOrSettings(app);
    }

    private static void showTtsToast(@NonNull Context app, @NonNull String msg) {
        MAIN.post(
                () -> {
                    try {
                        Toast.makeText(app, msg, Toast.LENGTH_LONG).show();
                    } catch (Exception ignored) {
                    }
                });
    }

    /**
     * Prefer language-pack installer, then system TTS settings, then Play Store
     * Google TTS. Never falls back to speaking a different language.
     */
    private static void openTtsInstallOrSettings(@NonNull Context app) {
        Intent install = new Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
        if (tryStartActivity(app, install)) return;
        Intent settings = new Intent("com.android.settings.TTS_SETTINGS");
        if (tryStartActivity(app, settings)) return;
        Intent market =
                new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                                "market://details?id="
                                        + ArticleTtsConfigHelper.GOOGLE_TTS_PACKAGE));
        if (tryStartActivity(app, market)) return;
        Intent web =
                new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                                "https://play.google.com/store/apps/details?id="
                                        + ArticleTtsConfigHelper.GOOGLE_TTS_PACKAGE));
        tryStartActivity(app, web);
    }

    private static boolean tryStartActivity(@NonNull Context app, @NonNull Intent intent) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (intent.resolveActivity(app.getPackageManager()) != null) {
                app.startActivity(intent);
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    @NonNull
    private static Locale localeFromTag(@NonNull String localeTag) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return Locale.forLanguageTag(localeTag.replace('_', '-'));
        }
        String[] parts = localeTag.replace('-', '_').split("_");
        if (parts.length >= 2 && !TextUtils.isEmpty(parts[0])) {
            return new Locale(parts[0], parts[1]);
        }
        return new Locale(parts.length > 0 && !TextUtils.isEmpty(parts[0]) ? parts[0] : "en");
    }

    private static boolean sameLanguage(@Nullable Locale a, @Nullable Locale b) {
        if (a == null || b == null) return false;
        String al = a.getLanguage();
        String bl = b.getLanguage();
        return !TextUtils.isEmpty(al) && al.equalsIgnoreCase(bl);
    }

    private static boolean voiceMatchesLanguage(@NonNull Voice voice, @NonNull Locale wanted) {
        return voice.getLocale() != null && sameLanguage(voice.getLocale(), wanted);
    }

    @NonNull
    private static Locale localeForAppLanguage(@Nullable Context context) {
        return TtsLanguageLocales.forLanguageId(languageId(context));
    }

    /**
     * @param resetUi speaker pulse / icon
     * @param cancelPending when true (leave article / stopAll), bump epoch, drop
     *     queued {@code ensureTts} runnables, stop + shutdown engine so Google-retry
     *     audio cannot keep playing after Daily Updates / News reload
     */
    private static void stopSpeaking(boolean resetUi, boolean cancelPending) {
        // Drop the active id before tts.stop() so a synchronous onDone cannot
        // clear a read that starts immediately afterwards.
        activeUtteranceId = null;
        speakingPostId = null;
        if (cancelPending) {
            speakEpoch++;
            pendingReady.clear();
            ttsInitializing = false;
            forceGoogleNextInit = false;
        }
        try {
            if (tts != null) {
                tts.stop();
            }
        } catch (Exception ignored) {
        }
        if (cancelPending) {
            shutdownTtsEngine();
        }
        if (resetUi) {
            applyAllSlotsUi(false);
        }
        stopPulseAnimatorOnly();
        pulseAnimTarget = null;
    }

    private static void shutdownTtsEngine() {
        ttsReady = false;
        ttsInitializing = false;
        appliedVoiceKey = "";
        voicesPreloaded = false;
        try {
            if (tts != null) {
                tts.stop();
                tts.shutdown();
            }
        } catch (Exception ignored) {
        }
        tts = null;
    }

    private static boolean isSpeaking() {
        try {
            return tts != null && ttsReady && tts.isSpeaking();
        } catch (Exception e) {
            return speakingPostId != null;
        }
    }

    private static boolean isSpeakingPost(@Nullable Long postId) {
        return speakingPostId != null && postId != null && speakingPostId.equals(postId);
    }

    private static boolean isActiveUtterance(@Nullable String utteranceId) {
        return utteranceId != null && utteranceId.equals(activeUtteranceId);
    }

    private static void applyAllSlotsUi(boolean speaking) {
        pruneDeadSlots();
        View firstPulse = null;
        for (SpeakerSlot slot : activeSlots) {
            ImageView btn = slot.btnRef.get();
            View pulse = slot.pulseRef == null ? null : slot.pulseRef.get();
            if (btn == null) continue;
            // Only the article that was tapped may show the orange speaker.
            // Recycled next/previous cards stay off even if they are already bound.
            boolean slotSpeaking = speaking && isSpeakingPost(slot.postId);
            applyUiState(btn, pulse, slotSpeaking, slot.style);
            if (slotSpeaking && firstPulse == null && pulse != null) {
                firstPulse = pulse;
            } else if (slotSpeaking && firstPulse == null) {
                firstPulse = btn;
            }
        }
        if (speaking) {
            startPulse(firstPulse);
        } else {
            stopPulseAnimatorOnly();
            for (SpeakerSlot slot : activeSlots) {
                View pulse = slot.pulseRef == null ? null : slot.pulseRef.get();
                if (pulse != null) {
                    pulse.setVisibility(View.GONE);
                    pulse.setAlpha(0f);
                    pulse.setScaleX(1f);
                    pulse.setScaleY(1f);
                }
            }
        }
    }

    private static void applyUiState(
            @Nullable ImageView speakerBtn,
            @Nullable View pulseView,
            boolean speaking,
            int style) {
        if (speakerBtn != null) {
            if (style == STYLE_ENGAGEMENT) {
                speakerBtn.setImageResource(
                        speaking ? R.drawable.ic_action_tts_on : R.drawable.ic_action_tts);
                try {
                    EngagementCountHelper.applyIconTint(speakerBtn.getContext(), speakerBtn);
                } catch (Exception ignored) {
                }
            } else {
                speakerBtn.setImageResource(
                        speaking ? R.drawable.ic_article_tts_on : R.drawable.ic_article_tts_off);
                speakerBtn.setImageTintList(null);
            }
            speakerBtn.setContentDescription(speaking ? "Stop reading" : "Read aloud");
        }
        if (!speaking && pulseView != null) {
            pulseView.setVisibility(View.GONE);
            pulseView.setAlpha(0f);
            pulseView.setScaleX(1f);
            pulseView.setScaleY(1f);
        }
    }

    private static void nudgePulse() {
        View pulse = pulseAnimTarget == null ? null : pulseAnimTarget.get();
        if (pulse == null || pulseAnim == null || !pulseAnim.isRunning()) {
            // Prefer dedicated pulse views from active slots.
            pruneDeadSlots();
            for (SpeakerSlot slot : activeSlots) {
                if (!isSpeakingPost(slot.postId)) continue;
                View p = slot.pulseRef == null ? null : slot.pulseRef.get();
                if (p != null) {
                    startPulse(p);
                    return;
                }
            }
            for (SpeakerSlot slot : activeSlots) {
                if (!isSpeakingPost(slot.postId)) continue;
                ImageView btn = slot.btnRef.get();
                if (btn != null) {
                    startPulse(btn);
                    return;
                }
            }
        }
    }

    private static void startPulse(@Nullable View pulseView) {
        if (pulseView == null) return;
        stopPulseAnimatorOnly();
        pulseAnimTarget = new WeakReference<>(pulseView);
        pulseView.setVisibility(View.VISIBLE);
        pulseView.setAlpha(0.55f);
        pulseView.setScaleX(1f);
        pulseView.setScaleY(1f);
        ObjectAnimator sx =
                ObjectAnimator.ofFloat(pulseView, View.SCALE_X, 1f, PULSE_MAX_SCALE, 1f);
        ObjectAnimator sy =
                ObjectAnimator.ofFloat(pulseView, View.SCALE_Y, 1f, PULSE_MAX_SCALE, 1f);
        ObjectAnimator alpha =
                ObjectAnimator.ofFloat(pulseView, View.ALPHA, 0.55f, 0.15f, 0.55f);
        sx.setRepeatCount(ValueAnimator.INFINITE);
        sy.setRepeatCount(ValueAnimator.INFINITE);
        alpha.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnim = new AnimatorSet();
        pulseAnim.playTogether(sx, sy, alpha);
        pulseAnim.setDuration(900);
        pulseAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        pulseAnim.start();
    }

    private static void stopPulseOn(@Nullable View pulseView) {
        if (pulseView == null) return;
        View animTarget = pulseAnimTarget == null ? null : pulseAnimTarget.get();
        if (animTarget == pulseView) {
            stopPulseAnimatorOnly();
            pulseAnimTarget = null;
        }
        pulseView.setVisibility(View.GONE);
        pulseView.setAlpha(0f);
        pulseView.setScaleX(1f);
        pulseView.setScaleY(1f);
    }

    private static void stopPulseAnimatorOnly() {
        if (pulseAnim != null) {
            try {
                pulseAnim.cancel();
            } catch (Exception ignored) {
            }
            pulseAnim = null;
        }
    }

    private static void registerOrUpdateSlot(
            @NonNull ImageView speakerBtn,
            @Nullable View pulseView,
            int style,
            @Nullable Long postId) {
        pruneDeadSlots();
        for (SpeakerSlot slot : activeSlots) {
            ImageView existing = slot.btnRef.get();
            if (existing == speakerBtn) {
                slot.pulseRef = pulseView == null ? null : new WeakReference<>(pulseView);
                slot.style = style;
                slot.postId = postId;
                return;
            }
        }
        activeSlots.add(new SpeakerSlot(speakerBtn, pulseView, style, postId));
    }

    private static void unregisterSlot(@NonNull ImageView speakerBtn) {
        Iterator<SpeakerSlot> it = activeSlots.iterator();
        while (it.hasNext()) {
            SpeakerSlot slot = it.next();
            ImageView btn = slot.btnRef.get();
            if (btn == null || btn == speakerBtn) {
                it.remove();
            }
        }
    }

    private static void pruneDeadSlots() {
        Iterator<SpeakerSlot> it = activeSlots.iterator();
        while (it.hasNext()) {
            SpeakerSlot slot = it.next();
            if (slot.btnRef.get() == null) {
                it.remove();
            }
        }
    }

    /**
     * Strip HTML, emoji, dingbats, and decorative symbols so TTS speaks only
     * words (Telugu / English / etc.). Keeps letters, digits, punctuation.
     */
    @NonNull
    private static String sanitizeBody(@Nullable CharSequence raw) {
        if (raw == null) return "";
        String s = raw.toString().trim();
        if (s.isEmpty()) return "";
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                s = Html.fromHtml(s, Html.FROM_HTML_MODE_LEGACY).toString();
            } else {
                s = Html.fromHtml(s).toString();
            }
        } catch (Exception ignored) {
        }
        s = s.replaceAll("<[^>]+>", " ");
        s = stripEmojiAndDecorativeSymbols(s);
        s = s.replaceAll("\\s+", " ").trim();
        if (s.length() > 3500) {
            s = s.substring(0, 3500);
        }
        return s;
    }

    /**
     * Remove emoji, dingbats, pictographs, variation selectors, ZWJ, and other
     * non-speech symbols (e.g. 👉) while keeping letters/digits/punctuation.
     */
    @NonNull
    static String stripEmojiAndDecorativeSymbols(@NonNull String input) {
        StringBuilder out = new StringBuilder(input.length());
        boolean lastWasSpace = false;
        for (int i = 0; i < input.length(); ) {
            int cp = input.codePointAt(i);
            i += Character.charCount(cp);
            if (shouldStripCodePoint(cp)) {
                if (!lastWasSpace && out.length() > 0) {
                    out.append(' ');
                    lastWasSpace = true;
                }
                continue;
            }
            if (Character.isWhitespace(cp)) {
                if (!lastWasSpace && out.length() > 0) {
                    out.append(' ');
                    lastWasSpace = true;
                }
                continue;
            }
            out.appendCodePoint(cp);
            lastWasSpace = false;
        }
        return out.toString().trim();
    }

    private static boolean shouldStripCodePoint(int cp) {
        // Common TTS-noise: emoji presentation, keycap, ZWJ, variation selectors.
        if (cp == 0x200D || cp == 0xFE0E || cp == 0xFE0F || cp == 0x20E3) {
            return true;
        }
        // Skin tone modifiers.
        if (cp >= 0x1F3FB && cp <= 0x1F3FF) {
            return true;
        }
        int type = Character.getType(cp);
        if (type == Character.OTHER_SYMBOL
                || type == Character.SURROGATE
                || type == Character.PRIVATE_USE
                || type == Character.NON_SPACING_MARK
                        && (cp >= 0xFE00 && cp <= 0xFE0F)) {
            // Keep currency etc. that are OTHER_SYMBOL? Most So symbols are decorative.
            // Letters/digits never hit OTHER_SYMBOL. Currency is CURRENCY_SYMBOL (Sc).
            return true;
        }
        // Misc symbols & dingbats, enclosed alphanumerics used as bullets.
        if ((cp >= 0x2600 && cp <= 0x27BF)
                || (cp >= 0x2300 && cp <= 0x23FF)
                || (cp >= 0x2B00 && cp <= 0x2BFF)
                || (cp >= 0x25A0 && cp <= 0x25FF)
                || (cp >= 0x2190 && cp <= 0x21FF)
                || (cp >= 0x1F000 && cp <= 0x1FAFF)
                || (cp >= 0x1F300 && cp <= 0x1F9FF)) {
            return true;
        }
        // Geometric / box drawing bullets sometimes used in news bodies.
        if (cp == 0x2022 || cp == 0x2023 || cp == 0x25AA || cp == 0x25AB
                || cp == 0x25CF || cp == 0x25CB || cp == 0x25B6 || cp == 0x25BA
                || cp == 0x25C0 || cp == 0x25C6 || cp == 0x25C7 || cp == 0x261E
                || cp == 0x261B || cp == 0x27A4 || cp == 0x2794 || cp == 0x279C) {
            return true;
        }
        return false;
    }

    private static final class SpeakerSlot {
        final WeakReference<ImageView> btnRef;
        @Nullable WeakReference<View> pulseRef;
        int style;
        @Nullable Long postId;

        SpeakerSlot(
                @NonNull ImageView btn,
                @Nullable View pulse,
                int style,
                @Nullable Long postId) {
            this.btnRef = new WeakReference<>(btn);
            this.pulseRef = pulse == null ? null : new WeakReference<>(pulse);
            this.style = style;
            this.postId = postId;
        }
    }
}
