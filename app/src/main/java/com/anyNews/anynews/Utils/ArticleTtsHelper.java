package com.anyNews.anynews.Utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
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

import com.anyNews.anynews.R;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

/**
 * Article-body Text-to-Speech for half-image cards.
 * Toggle speaker → speak body / stop; pulse glow while speaking.
 * Voice/locale/engine from {@link ArticleTtsConfigHelper} (admin splash).
 */
public final class ArticleTtsHelper {

    private static final String UTTERANCE_ID = "article_tts_body";
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private static TextToSpeech tts;
    private static boolean ttsReady;
    private static boolean ttsInitializing;
    private static String preferredEngine = "";
    private static Long speakingPostId;
    private static WeakReference<ImageView> speakingBtnRef;
    private static WeakReference<View> pulseViewRef;
    private static AnimatorSet pulseAnim;

    private ArticleTtsHelper() {}

    /** Hide speaker when admin master is OFF. */
    public static void bindButton(
            @Nullable Context context,
            @Nullable ImageView speakerBtn,
            @Nullable View pulseView,
            @Nullable Long postId,
            @Nullable CharSequence bodyText) {
        if (speakerBtn == null) return;
        if (context == null || !ArticleTtsConfigHelper.isEnabled(context)) {
            speakerBtn.setVisibility(View.GONE);
            if (pulseView != null) {
                pulseView.setVisibility(View.GONE);
                stopPulse(pulseView);
            }
            speakerBtn.setOnClickListener(null);
            return;
        }
        speakerBtn.setVisibility(View.VISIBLE);
        final boolean thisSpeaking =
                speakingPostId != null
                        && postId != null
                        && speakingPostId.equals(postId)
                        && isSpeaking();
        applyUiState(speakerBtn, pulseView, thisSpeaking);
        final Context app = context.getApplicationContext();
        final Long id = postId;
        final String text = sanitizeBody(bodyText);
        speakerBtn.setOnClickListener(
                v -> toggle(app, id, text, speakerBtn, pulseView));
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
            @Nullable View pulseView) {
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
        speak(app, postId, text, speakerBtn, pulseView);
    }

    private static void speak(
            @NonNull Context app,
            @Nullable Long postId,
            @NonNull String text,
            @NonNull ImageView speakerBtn,
            @Nullable View pulseView) {
        stopSpeaking(false);
        speakingPostId = postId;
        speakingBtnRef = new WeakReference<>(speakerBtn);
        pulseViewRef = pulseView == null ? null : new WeakReference<>(pulseView);
        applyUiState(speakerBtn, pulseView, true);
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
        final Runnable queued = onReady;
        if (ttsInitializing && tts != null) {
            MAIN.postDelayed(
                    () -> {
                        if (ttsReady) queued.run();
                        else ensureTts(app, queued);
                    },
                    200);
            return;
        }
        shutdownTtsEngine();
        ttsInitializing = true;
        preferredEngine = enginePkg;
        TextToSpeech.OnInitListener listener =
                status -> {
                    ttsInitializing = false;
                    ttsReady = status == TextToSpeech.SUCCESS;
                    if (!ttsReady) {
                        stopSpeaking(true);
                        return;
                    }
                    tts.setOnUtteranceProgressListener(
                            new UtteranceProgressListener() {
                                @Override
                                public void onStart(String utteranceId) {
                                    MAIN.post(() -> startPulseOnSpeaking());
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
                                    // Voice activity → keep pulse alive / nudge.
                                    MAIN.post(() -> nudgePulse());
                                }
                            });
                    queued.run();
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
            String voiceWanted = ArticleTtsConfigHelper.getVoice(app);
            if (!TextUtils.isEmpty(voiceWanted)
                    && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Set<Voice> voices = tts.getVoices();
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
            }
            tts.setSpeechRate(1.0f);
            tts.setPitch(1.0f);
        } catch (Exception ignored) {
        }
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
            ImageView btn = speakingBtnRef == null ? null : speakingBtnRef.get();
            View pulse = pulseViewRef == null ? null : pulseViewRef.get();
            applyUiState(btn, pulse, false);
        }
        speakingBtnRef = null;
        pulseViewRef = null;
        stopPulse(null);
    }

    private static void shutdownTtsEngine() {
        ttsReady = false;
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

    private static void applyUiState(
            @Nullable ImageView speakerBtn, @Nullable View pulseView, boolean speaking) {
        if (speakerBtn != null) {
            speakerBtn.setImageResource(
                    speaking ? R.drawable.ic_article_tts_on : R.drawable.ic_article_tts_off);
            speakerBtn.setContentDescription(speaking ? "Stop reading" : "Read aloud");
        }
        if (speaking) {
            startPulse(pulseView);
        } else {
            stopPulse(pulseView);
        }
    }

    private static void startPulseOnSpeaking() {
        View pulse = pulseViewRef == null ? null : pulseViewRef.get();
        ImageView btn = speakingBtnRef == null ? null : speakingBtnRef.get();
        applyUiState(btn, pulse, true);
    }

    private static void nudgePulse() {
        View pulse = pulseViewRef == null ? null : pulseViewRef.get();
        if (pulse == null || pulseAnim == null || !pulseAnim.isRunning()) {
            startPulse(pulse);
        }
    }

    private static void startPulse(@Nullable View pulseView) {
        if (pulseView == null) return;
        stopPulseAnimatorOnly();
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

    private static void stopPulse(@Nullable View pulseView) {
        stopPulseAnimatorOnly();
        View pulse = pulseView;
        if (pulse == null && pulseViewRef != null) {
            pulse = pulseViewRef.get();
        }
        if (pulse != null) {
            pulse.setVisibility(View.GONE);
            pulse.setAlpha(0f);
            pulse.setScaleX(1f);
            pulse.setScaleY(1f);
        }
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
        // Strip leftover markup / collapse whitespace for cleaner speech.
        s = s.replaceAll("<[^>]+>", " ");
        s = s.replaceAll("\\s+", " ").trim();
        // Android TTS has practical utterance limits; keep a safe chunk.
        if (s.length() > 3500) {
            s = s.substring(0, 3500);
        }
        return s;
    }
}
