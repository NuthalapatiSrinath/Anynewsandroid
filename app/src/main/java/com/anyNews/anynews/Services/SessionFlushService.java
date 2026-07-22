package com.anyNews.anynews.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.anyNews.anynews.Utils.SessionAnalyticsTracker;

/**
 * Stays alive while a session is open so {@link #onTaskRemoved} fires when the user
 * swipes the app away from Recents — giving us a last chance to POST /session/close
 * before the process is killed.
 */
public class SessionFlushService extends Service {

    public static final String ACTION_WATCH = "com.anyNews.anynews.session.WATCH";
    public static final String ACTION_FLUSH = "com.anyNews.anynews.session.FLUSH";

    public static void startWatch(Context context) {
        try {
            Intent i = new Intent(context, SessionFlushService.class);
            i.setAction(ACTION_WATCH);
            context.getApplicationContext().startService(i);
        } catch (Exception ignored) {
        }
    }

    public static void stopWatch(Context context) {
        try {
            context.getApplicationContext().stopService(new Intent(context, SessionFlushService.class));
        } catch (Exception ignored) {
        }
    }

    public static void enqueueFlush(Context context) {
        try {
            Intent i = new Intent(context, SessionFlushService.class);
            i.setAction(ACTION_FLUSH);
            context.getApplicationContext().startService(i);
        } catch (Exception ignored) {
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : null;
        if (ACTION_FLUSH.equals(action)) {
            SessionAnalyticsTracker.get().flushPendingCloseFromService(getApplicationContext(), () -> {
                stopSelf(startId);
            });
            return START_NOT_STICKY;
        }
        // WATCH — keep process eligible for onTaskRemoved while session is open.
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // Recents swipe — close + flush before the system kills us.
        SessionAnalyticsTracker.get().onTaskRemoved(getApplicationContext());
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
