package com.anyNews.anynews.Utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Application;
import android.content.Context;
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

    private static final String UTTERANCE_ID = "article_tts_body";
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    /** Image overlay uses white glyphs; engagement row uses tintable action icons. */
    public static final int STYLE_IMAGE = 0;
    public static final int STYLE_ENGAGEMENT = 1;

    private static TextToSpeech tts;
    private static boolean ttsReady;
    private static boolean ttsInitializing;
    private static String preferredEngine = "";
    private static String appliedVoiceKey = "";
    private static boolean voicesPreloaded;
    private static Long speakingPostId;
    private static final List<SpeakerSlot> activeSlots = new ArrayList<>();
    private static final List<Runnable> pendingReady = new ArrayList<>();
    private static AnimatorSet pulseAnim;
    private static WeakReference<View> pulseAnimTarget;
    private static final AtomicBoolean lifecycleRegistered = new AtomicBoolean(false);

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
        final boolean thisSpeaking =
                speakingPostId != null
                        && postId != null
                        && speakingPostId.equals(postId)
                        && isSpeaking();
        registerOrUpdateSlot(speakerBtn, pulseView, style);
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
        stopSpeaking(true);
    }

    public static void stopAll() {
        stopSpeaking(true);
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
        final boolean thisSpeaking =
                speakingPostId != null
                        && postId != null
                        && speakingPostId.equals(postId)
                        && isSpeaking();
        if (thisSpeaking) {
            stopSpeaking(true);
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
        stopSpeaking(false);
        speakingPostId = postId;
        registerOrUpdateSlot(speakerBtn, pulseView, style);
        applyAllSlotsUi(true);
        ensureTts(
                app,
                () -> {
                    if (speakingPostId == null
                            || (postId != null && !postId.equals(speakingPostId))) {
                        return;
                    }
                    applyVoiceConfig(app);
                    int result;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Bundle params = new Bundle();
                        params.putString(
                                TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID);
                        result =
                                tts.speak(
                                        text,
                                        TextToSpeech.QUEUE_FLUSH,
                                        params,
                                        UTTERANCE_ID);
                    } else {
                        HashMap<String, String> map = new HashMap<>();
                        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID);
                        result = tts.speak(text, TextToSpeech.QUEUE_FLUSH, map);
                    }
                    if (result != TextToSpeech.SUCCESS) {
                        stopSpeaking(true);
                    }
                });
    }

    private static void ensureTts(@NonNull Context app, @NonNull Runnable onReady) {
        String enginePref = ArticleTtsConfigHelper.getEngine(app);
        String enginePkg =
                ArticleTtsConfigHelper.ENGINE_GOOGLE.equals(enginePref)
                        ? ArticleTtsConfigHelper.GOOGLE_TTS_PACKAGE
                        : "";
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
                        stopSpeaking(true);
                        return;
                    }
                    tts.setOnUtteranceProgressListener(
                            new UtteranceProgressListener() {
                                @Override
                                public void onStart(String utteranceId) {
                                    MAIN.post(() -> applyAllSlotsUi(true));
                                }

                                @Override
                                public void onDone(String utteranceId) {
                                    MAIN.post(() -> stopSpeaking(true));
                                }

                                @Override
                                public void onError(String utteranceId) {
                                    MAIN.post(() -> stopSpeaking(true));
                                }

                                @Override
                                public void onRangeStart(
                                        String utteranceId, int start, int end, int frame) {
                                    MAIN.post(() -> nudgePulse());
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

    private static void applyVoiceConfig(@NonNull Context app) {
        if (tts == null) return;
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
                return;
            }
            Locale locale;
            if (!TextUtils.isEmpty(localeTag)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    locale = Locale.forLanguageTag(localeTag.replace('_', '-'));
                } else {
                    String[] parts = localeTag.replace('-', '_').split("_");
                    locale =
                            parts.length >= 2
                                    ? new Locale(parts[0], parts[1])
                                    : new Locale(parts[0]);
                }
            } else {
                locale = localeForAppLanguage(app);
            }
            int langResult = tts.setLanguage(locale);
            if (langResult == TextToSpeech.LANG_MISSING_DATA
                    || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts.setLanguage(Locale.getDefault());
            }
            if (!TextUtils.isEmpty(voiceWanted)
                    && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Set<Voice> voices = tts.getVoices();
                voicesPreloaded = voices != null;
                if (voices != null) {
                    String needle = voiceWanted.toLowerCase(Locale.US);
                    Voice best = null;
                    for (Voice v : voices) {
                        if (v == null) continue;
                        String name = v.getName() == null ? "" : v.getName().toLowerCase(Locale.US);
                        if (name.contains(needle) || needle.contains(name)) {
                            best = v;
                            break;
                        }
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
        } catch (Exception ignored) {
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

    @NonNull
    private static Locale localeForAppLanguage(@Nullable Context context) {
        int langId = 0;
        try {
            if (context != null) {
                langId =
                        SharedPrefsData.getInt(
                                context.getApplicationContext(),
                                Constants.LanguageID,
                                Constants.PREF_NAME);
            }
        } catch (Exception ignored) {
        }
        // FlipperAdapter: 1=English, 4=Tamil, else Telugu path.
        if (langId == 1) return new Locale("en", "IN");
        if (langId == 4) return new Locale("ta", "IN");
        return new Locale("te", "IN");
    }

    private static void stopSpeaking(boolean resetUi) {
        try {
            if (tts != null && ttsReady) {
                tts.stop();
            }
        } catch (Exception ignored) {
        }
        speakingPostId = null;
        if (resetUi) {
            applyAllSlotsUi(false);
        }
        stopPulseAnimatorOnly();
        pulseAnimTarget = null;
    }

    private static void shutdownTtsEngine() {
        ttsReady = false;
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

    private static void applyAllSlotsUi(boolean speaking) {
        pruneDeadSlots();
        View firstPulse = null;
        for (SpeakerSlot slot : activeSlots) {
            ImageView btn = slot.btnRef.get();
            View pulse = slot.pulseRef == null ? null : slot.pulseRef.get();
            if (btn == null) continue;
            applyUiState(btn, pulse, speaking, slot.style);
            if (speaking && firstPulse == null && pulse != null) {
                firstPulse = pulse;
            } else if (speaking && firstPulse == null) {
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
                View p = slot.pulseRef == null ? null : slot.pulseRef.get();
                if (p != null) {
                    startPulse(p);
                    return;
                }
            }
            for (SpeakerSlot slot : activeSlots) {
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
                ObjectAnimator.ofFloat(pulseView, View.SCALE_X, 1f, 1.55f, 1f);
        ObjectAnimator sy =
                ObjectAnimator.ofFloat(pulseView, View.SCALE_Y, 1f, 1.55f, 1f);
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
            @NonNull ImageView speakerBtn, @Nullable View pulseView, int style) {
        pruneDeadSlots();
        for (SpeakerSlot slot : activeSlots) {
            ImageView existing = slot.btnRef.get();
            if (existing == speakerBtn) {
                slot.pulseRef = pulseView == null ? null : new WeakReference<>(pulseView);
                slot.style = style;
                return;
            }
        }
        activeSlots.add(new SpeakerSlot(speakerBtn, pulseView, style));
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

        SpeakerSlot(@NonNull ImageView btn, @Nullable View pulse, int style) {
            this.btnRef = new WeakReference<>(btn);
            this.pulseRef = pulse == null ? null : new WeakReference<>(pulse);
            this.style = style;
        }
    }
}
