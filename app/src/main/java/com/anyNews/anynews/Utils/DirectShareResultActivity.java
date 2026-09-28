package com.anyNews.anynews.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;

/**
 * Starts a packaged share (WhatsApp) and reports only a completed send.
 *
 * <p>{@link Activity#RESULT_OK} is what WhatsApp sets after the user taps Send.
 * Backing out of the chat picker or the composer finishes with
 * {@link Activity#RESULT_CANCELED} and does not run the callback. Launch itself
 * does not run it. The chooser {@code SHARE_TARGET_CHOSEN} callback is not used
 * here: for a direct package that fires when WhatsApp's activity starts.
 */
public final class DirectShareResultActivity extends Activity {

    static final String EXTRA_SEND = "com.anyNews.anynews.extra.DIRECT_SHARE_SEND";
    private static final String EXTRA_GEN = "com.anyNews.anynews.extra.DIRECT_SHARE_GEN";
    private static final String STATE_LAUNCHED = "launched";
    private static final int REQ_TARGET = 0x5753;

    private static int nextGen;
    @Nullable
    private static Pending pending;

    private int gen;
    private boolean launched;
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
            return;
        }
        Intent send = readSendIntent(getIntent());
        if (send == null) {
            clearIf(gen);
            finish();
            overridePendingTransition(0, 0);
            return;
        }
        send = new Intent(send);
        // NEW_TASK drops the activity result, so a finished send would never count.
        send.setFlags(send.getFlags() & ~Intent.FLAG_ACTIVITY_NEW_TASK);
        sendIntent = send;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start after resume. Starting from onCreate on a translucent activity
        // can deliver RESULT_CANCELED before WhatsApp is shown.
        if (launched || sendIntent == null) {
            return;
        }
        launched = true;
        try {
            startActivityForResult(sendIntent, REQ_TARGET);
        } catch (RuntimeException e) {
            clearIf(gen);
            finish();
            overridePendingTransition(0, 0);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_LAUNCHED, launched);
        outState.putInt(EXTRA_GEN, gen);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQ_TARGET) {
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

    @Nullable
    private static Intent readSendIntent(Intent host) {
        if (host == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return host.getParcelableExtra(EXTRA_SEND, Intent.class);
        }
        return host.getParcelableExtra(EXTRA_SEND);
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
