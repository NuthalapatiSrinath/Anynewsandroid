package com.anyNews.anynews.Utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

/**
 * Starts a packaged share (WhatsApp) and reports only a completed send.
 *
 * <p>{@link Activity#RESULT_OK} is what WhatsApp sets after the user taps Send.
 * Backing out of the chat picker or the composer finishes with
 * {@link Activity#RESULT_CANCELED} and does not run the callback. Launch itself
 * does not run it. The chooser {@code SHARE_TARGET_CHOSEN} callback is not used
 * here: for a direct package that fires when WhatsApp's activity starts.
 *
 * <p>If the packaged target cannot be shown (disabled WhatsApp, Business-only
 * mismatch, or an OEM that drops the start), the system share sheet opens
 * instead of leaving the user with a tap that does nothing. That fallback
 * does not increment the WhatsApp send count.
 */
public final class DirectShareResultActivity extends Activity {

    static final String EXTRA_SEND = "com.anyNews.anynews.extra.DIRECT_SHARE_SEND";
    private static final String EXTRA_GEN = "com.anyNews.anynews.extra.DIRECT_SHARE_GEN";
    private static final String STATE_LAUNCHED = "launched";
    private static final int REQ_TARGET = 0x5753;
    /** If WhatsApp never pauses us, the start was swallowed. */
    private static final long TARGET_APPEAR_MS = 1500L;

    private static int nextGen;
    @Nullable
    private static Pending pending;

    private int gen;
    private boolean launched;
    private boolean sawPause;
    private boolean handedOff;
    private long startedAt;
    @Nullable
    private Intent sendIntent;

    static void launch(Context context, Intent sendIntent, Runnable onSent) {
        int token = ++nextGen;
        pending = new Pending(token, onSent);
        Intent relay = new Intent(context, DirectShareResultActivity.class);
        relay.putExtra(EXTRA_SEND, sendIntent);
        relay.putExtra(EXTRA_GEN, token);
        if (!(context instanceof Activity)) {
            relay.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        try {
            context.startActivity(relay);
        } catch (RuntimeException e) {
            clearIf(token);
            throw e;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        gen = getIntent().getIntExtra(EXTRA_GEN, 0);
        if (savedInstanceState != null) {
            launched = savedInstanceState.getBoolean(STATE_LAUNCHED, false);
            gen = savedInstanceState.getInt(EXTRA_GEN, gen);
        }
        if (launched) {
            Pending current = pending;
            if (current == null || current.gen != gen) {
                finish();
                overridePendingTransition(0, 0);
            }
            return;
        }
        Intent send = readSendIntent(getIntent());
        if (send == null) {
            clearIf(gen);
            Toast.makeText(this, "Unable to share video", Toast.LENGTH_SHORT).show();
            finish();
            overridePendingTransition(0, 0);
            return;
        }
        send = new Intent(send);
        prepareSendIntent(send);
        sendIntent = send;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFinishing() || launched || sendIntent == null) {
            return;
        }
        launched = true;
        // A real window must exist before the target start. Some phones treat
        // a start from an empty translucent activity as a background launch
        // and drop it without an error.
        View root = new View(this);
        setContentView(root);
        root.post(this::startTarget);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sawPause = true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_LAUNCHED, launched);
        outState.putInt(EXTRA_GEN, gen);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQ_TARGET && !handedOff) {
            boolean neverShown = !sawPause
                    && startedAt > 0L
                    && (SystemClock.elapsedRealtime() - startedAt) < 500L;
            if (resultCode != RESULT_OK && neverShown) {
                openChooserFallback();
                return;
            }
            handedOff = true;
            Pending mine = takeIf(gen);
            // WhatsApp sets RESULT_OK only after Send. Cancel / back is RESULT_CANCELED.
            if (mine != null && resultCode == RESULT_OK && mine.callback != null) {
                try {
                    mine.callback.run();
                } catch (Exception ignored) {
                }
            }
        }
        finish();
        overridePendingTransition(0, 0);
    }

    private void startTarget() {
        if (handedOff || isFinishing() || sendIntent == null) {
            return;
        }
        prepareSendIntent(sendIntent);
        startedAt = SystemClock.elapsedRealtime();
        try {
            startActivityForResult(sendIntent, REQ_TARGET);
        } catch (RuntimeException e) {
            openChooserFallback();
            return;
        }
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!handedOff && !sawPause && !isFinishing()) {
                openChooserFallback();
            }
        }, TARGET_APPEAR_MS);
    }

    /**
     * Packaged WhatsApp did not appear. Open the system sheet so the video
     * still leaves the phone. Does not run the Send-count callback.
     */
    private void openChooserFallback() {
        if (handedOff) {
            return;
        }
        handedOff = true;
        takeIf(gen);
        Intent bare = sendIntent != null ? new Intent(sendIntent) : null;
        if (bare == null) {
            Toast.makeText(this, "Unable to share video", Toast.LENGTH_SHORT).show();
            finish();
            overridePendingTransition(0, 0);
            return;
        }
        bare.setPackage(null);
        bare.setComponent(null);
        bare.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            ShareOutboundHelper.launchChooserOnTargetChosen(this, bare, "Share Video", null);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to share video", Toast.LENGTH_SHORT).show();
        }
        // Finish on the next turn. Finishing in the same call drops the chooser
        // on some phones, which looks like the tap did nothing.
        new Handler(Looper.getMainLooper()).post(() -> {
            if (!isFinishing()) {
                finish();
                overridePendingTransition(0, 0);
            }
        });
    }

    private void prepareSendIntent(Intent send) {
        // NEW_TASK drops the activity result, so a finished send would never count.
        send.setFlags(send.getFlags() & ~Intent.FLAG_ACTIVITY_NEW_TASK);
        send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri stream = streamUri(send);
        if (stream != null && send.getClipData() == null) {
            send.setClipData(ClipData.newUri(getContentResolver(), "shared_video", stream));
        }
        String pkg = send.getPackage();
        if (pkg != null && stream != null) {
            try {
                grantUriPermission(pkg, stream, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception ignored) {
            }
        }
    }

    @Nullable
    private Uri streamUri(Intent send) {
        if (send == null) {
            return null;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return send.getParcelableExtra(Intent.EXTRA_STREAM, Uri.class);
            }
            return send.getParcelableExtra(Intent.EXTRA_STREAM);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private static Intent readSendIntent(Intent host) {
        if (host == null) {
            return null;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return host.getParcelableExtra(EXTRA_SEND, Intent.class);
            }
            return host.getParcelableExtra(EXTRA_SEND);
        } catch (Exception e) {
            return null;
        }
    }

    private static void clearIf(int token) {
        Pending current = pending;
        if (current != null && current.gen == token) {
            pending = null;
        }
    }

    @Nullable
    private static Pending takeIf(int token) {
        Pending current = pending;
        if (current != null && current.gen == token) {
            pending = null;
            return current;
        }
        return null;
    }

    private static final class Pending {
        final int gen;
        @Nullable
        final Runnable callback;

        Pending(int gen, @Nullable Runnable callback) {
            this.gen = gen;
            this.callback = callback;
        }
    }
}
