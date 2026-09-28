package com.anyNews.anynews.Services;

import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.anyNews.anynews.Activities.HomePage;
import com.anyNews.anynews.Pojo.AddLikeResponse;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.StartingAppRequest;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.ForceLogoutHelper;
import com.anyNews.anynews.Utils.Logone;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * FCM receive + tray UI. Compact row: [article thumb LEFT | optional red brand logo] | headline.
 * Never injects white monochrome launcher as large content logo. setSmallIcon remains for the
 * status bar (system monochrome). Admin knobs via splash + FCM data (include_logo, sizes).
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String STYLE_TEXT_ONLY = "text_only";
    private static final String STYLE_COMPACT = "compact_with_image";
    private static final int DEFAULT_LEFT_DP = 48;
    private static final int DEFAULT_THUMB_DP = 48;
    private static final int DEFAULT_TITLE_SP = 13;
    private static final int BIG_PICTURE_MAX_W = 720;
    private static final int BIG_PICTURE_MAX_H = 360;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    String type_firebase = "push_receive";
    String device_id = "";
    long post_id_long, sent_at, draft_id = 0;
    String pn_color = "#000000";
    boolean showPNTime = true;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        try {
            showLog("PAYLOAD " + remoteMessage.getData());

            Map<String, String> params = remoteMessage.getData();
            if (params == null || params.isEmpty()) {
                if (remoteMessage.getNotification() != null) {
                    String title = remoteMessage.getNotification().getTitle();
                    if (title == null || title.isEmpty()) {
                        title = "AnyNews";
                    }
                    presentNewsNotification(
                            title, "0", "{}", pn_color, true, null, null, null);
                }
                return;
            }

            JSONObject object = new JSONObject(params);

            String type = object.optString("type", "");
            if ("force_logout".equals(type)) {
                handleForceLogout(object);
                return;
            }
            if ("reporter_status_changed".equals(type)) {
                handleReporterStatusChanged(object);
                return;
            }

            if ("comment_reply".equals(type)) {
                sendCommentReplyNotification(object);
                return;
            }

            String image = object.optString("image", "");
            String title = object.optString("pn_title", object.optString("title", "AnyNews"));
            String post_id = object.optString("post_id", "0");
            if (object.has("pn_color")) {
                pn_color = object.getString("pn_color");
            }
            device_id = object.optString("device_id", "");
            int language_id = object.optInt("language_id", 0);

            try {
                post_id_long = Long.parseLong(post_id);
                sent_at = object.optLong("sent_at", System.currentTimeMillis());
                draft_id = object.optLong("draft_id", 0);
                showPNTime = !object.has("showPNTime") || object.optBoolean("showPNTime", true);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            PushAppearance appearance = resolveAppearance(object);
            // Show immediately (no white launcher logo); hydrate images async.
            presentNewsNotification(
                    title,
                    post_id,
                    object.toString(),
                    pn_color,
                    showPNTime,
                    null,
                    null,
                    appearance);

            if (shouldLoadArticleImage(appearance, image)
                    || (appearance.includeLogo
                    && appearance.logoUrl != null
                    && !appearance.logoUrl.isEmpty())) {
                new LoadNotificationImagesTask(
                        image,
                        title,
                        post_id,
                        object.toString(),
                        pn_color,
                        showPNTime,
                        appearance)
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

            Logone.e("NotificationData", object.toString());
            long current_time_firebase = System.currentTimeMillis();
            writePushReceive(
                    "User",
                    type_firebase,
                    post_id_long,
                    current_time_firebase,
                    language_id,
                    sent_at,
                    draft_id,
                    device_id,
                    showPNTime);

        } catch (Exception e) {
            e.printStackTrace();
            Logone.e("NotificationData", e.getMessage());
        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        SharedPrefsData.putString(getApplicationContext(), Constants.FCM_TOKEN, s, Constants.PREF_NAME);
        int draftId = SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
        if (draftId != 0) {
            sendFcmTokenToServer(s);
        }
    }

    private void sendFcmTokenToServer(String token) {
        try {
            StartingAppRequest requestModel = new StartingAppRequest();
            requestModel.setDraftId(draftIdFromPrefs());
            requestModel.setFcmToken(token);
            String jsonString = new Gson().toJson(requestModel);
            String encrpt_data = encrypt(jsonString);
            EncritonRequestData encritonRequestData = new EncritonRequestData();
            encritonRequestData.setData(encrpt_data);
            JsonObject body = new Gson().toJsonTree(encritonRequestData).getAsJsonObject();
            String versionId = SharedPrefsData.getString(getApplicationContext(), Constants.Version_ID, Constants.PREF_NAME);
            if (versionId == null || versionId.isEmpty()) {
                versionId = "v3";
            }
            APIService apiService = RestApiClient.getClient().create(APIService.class);
            apiService.tokens(versionId, body).enqueue(new Callback<AddLikeResponse>() {
                @Override
                public void onResponse(Call<AddLikeResponse> call, Response<AddLikeResponse> response) {
                    showLog("FCM token updated on server");
                }

                @Override
                public void onFailure(Call<AddLikeResponse> call, Throwable t) {
                    Logone.e("FCM Token", "Failed to update token on server", t);
                }
            });
        } catch (Exception e) {
            Logone.e("FCM Token", "Error sending token to server", e);
        }
    }

    private int draftIdFromPrefs() {
        return SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
    }

    /**
     * Stable tray + PendingIntent requestCode per post. Never 0 (collides across
     * missing-id pushes under FLAG_UPDATE_CURRENT). When post id is missing/0,
     * diversify with object/title hash so notification-only payloads do not share
     * one PendingIntent.
     */
    private int safeNotificationId(String postId) {
        return safeNotificationId(postId, null);
    }

    private int safeNotificationId(String postId, @Nullable String diversifier) {
        if (postId == null || postId.trim().isEmpty() || "null".equalsIgnoreCase(postId.trim())) {
            return diversifiedFallbackId(diversifier);
        }
        String raw = postId.trim();
        try {
            long v = Long.parseLong(raw);
            if (v == 0L) {
                return diversifiedFallbackId(diversifier);
            }
            // Keep positive int identity; large ids hash to a stable non-zero code.
            if (v > 0 && v <= Integer.MAX_VALUE) {
                return (int) v;
            }
            int h = Long.hashCode(v);
            return h != 0 ? Math.abs(h) : diversifiedFallbackId(diversifier);
        } catch (NumberFormatException e) {
            int h = raw.hashCode();
            return h != 0 ? Math.abs(h) : diversifiedFallbackId(diversifier);
        }
    }

    private static int diversifiedFallbackId(@Nullable String diversifier) {
        if (diversifier != null && !diversifier.trim().isEmpty()) {
            int h = diversifier.trim().hashCode();
            if (h != 0) {
                return Math.abs(h);
            }
        }
        return 1;
    }

    /**
     * Action prefix so each post is a different {@link Intent#filterEquals} identity.
     * Extras are not part of PendingIntent identity; a shared action let a later
     * notification overwrite this post's id under {@code FLAG_UPDATE_CURRENT}.
     */
    public static final String ACTION_OPEN_PUSH_PREFIX = "com.anyNews.anynews.OPEN_PUSH/";

    /** Numeric post id. {@code TITLE} is sometimes overwritten with the visible headline. */
    public static final String EXTRA_PUSH_POST_ID = "post_id";

    private static final int RECENT_PUSH_LOG_CAP = 24;
    private static final long HISTORY_PUSH_MAX_AGE_MS = 24L * 60L * 60L * 1000L;

    public static boolean isOpenPushAction(@Nullable String action) {
        return action != null && action.startsWith(ACTION_OPEN_PUSH_PREFIX);
    }

    /**
     * Post id carried by a push tap. Numeric extras and the data URI win. A
     * non-numeric {@code TITLE} is the visible headline (notification history
     * rebuilds sometimes do this) and is matched against the tray log.
     */
    @Nullable
    public static String resolvePushPostId(@Nullable Context context, @Nullable Intent intent) {
        if (intent == null) {
            return null;
        }
        String fromAction = postIdFromOpenAction(intent.getAction());
        if (isNumericPostId(fromAction)) {
            return fromAction.trim();
        }
        String fromUri = postIdFromPushUri(intent.getData());
        if (isNumericPostId(fromUri)) {
            return fromUri.trim();
        }
        String postExtra = intent.getStringExtra(EXTRA_PUSH_POST_ID);
        if (isNumericPostId(postExtra)) {
            return postExtra.trim();
        }
        String titile = intent.getStringExtra("TITILE");
        if (isNumericPostId(titile)) {
            return titile.trim();
        }
        String title = intent.getStringExtra("TITLE");
        if (isNumericPostId(title)) {
            return title.trim();
        }
        String fromObject = postIdFromObjectJson(intent.getStringExtra("OBJECT"));
        if (isNumericPostId(fromObject)) {
            return fromObject.trim();
        }
        if (context != null && isPushShapedIntent(intent)) {
            String matched = matchRememberedTitle(context, intent);
            if (isNumericPostId(matched)) {
                return matched;
            }
        }
        return null;
    }

    /**
     * Stock notification history falls back to the launcher activity when the
     * archived notification has no activity {@link PendingIntent}. Referrer is
     * Settings or SystemUI, not the home launcher.
     */
    public static boolean isHistoryLauncherFallback(
            @Nullable Activity activity, @Nullable Intent intent) {
        if (activity == null || intent == null) {
            return false;
        }
        if (!Intent.ACTION_MAIN.equals(intent.getAction())) {
            return false;
        }
        if (intent.getCategories() == null
                || !intent.getCategories().contains(Intent.CATEGORY_LAUNCHER)) {
            return false;
        }
        Uri referrer;
        try {
            referrer = activity.getReferrer();
        } catch (Exception e) {
            return false;
        }
        if (referrer == null || referrer.getHost() == null) {
            return false;
        }
        String host = referrer.getHost().toLowerCase();
        return "com.android.settings".equals(host)
                || "com.android.systemui".equals(host)
                || host.contains("notification");
    }

    /**
     * Post id for a history launcher fallback that kept the visible headline
     * ({@code android.title} / {@code TITLE}) but dropped the numeric id.
     * No match when the intent has no headline — a Settings "Open" must stay
     * a normal launch.
     */
    @Nullable
    public static String resolveHistoryDisplayedTitle(
            @Nullable Context context, @Nullable Intent intent) {
        if (context == null || intent == null) {
            return null;
        }
        return matchRememberedTitle(context, intent);
    }

    private static void rememberDisplayedPush(
            @NonNull Context context, @Nullable String postId, @Nullable String title) {
        if (!isNumericPostId(postId)) {
            return;
        }
        String id = postId.trim();
        String plain = plainPushTitle(title);
        long now = System.currentTimeMillis();
        String prev = SharedPrefsData.getString(
                context.getApplicationContext(),
                com.anyNews.anynews.Utils.Constants.RECENT_PUSH_OPEN_LOG,
                com.anyNews.anynews.Utils.Constants.PREF_NAME);
        StringBuilder sb = new StringBuilder();
        sb.append(now).append('\t').append(id).append('\t').append(plain);
        int kept = 1;
        if (prev != null) {
            String[] lines = prev.split("\n");
            for (String line : lines) {
                ParsedPush remembered = parseRememberedLine(line);
                if (remembered == null || id.equals(remembered.postId)) {
                    continue;
                }
                sb.append('\n').append(line);
                kept++;
                if (kept >= RECENT_PUSH_LOG_CAP) {
                    break;
                }
            }
        }
        SharedPrefsData.putStringImmediate(
                context.getApplicationContext(),
                com.anyNews.anynews.Utils.Constants.RECENT_PUSH_OPEN_LOG,
                sb.toString(),
                com.anyNews.anynews.Utils.Constants.PREF_NAME);
    }

    private static boolean isPushShapedIntent(@NonNull Intent intent) {
        if (isOpenPushAction(intent.getAction()) || isPushOpenUri(intent.getData())) {
            return true;
        }
        try {
            if (intent.getBooleanExtra("notification", false)) {
                return true;
            }
        } catch (Exception ignored) {
        }
        return isNumericPostId(intent.getStringExtra("TITILE"))
                || isNumericPostId(intent.getStringExtra(EXTRA_PUSH_POST_ID));
    }

    @Nullable
    private static String postIdFromOpenAction(@Nullable String action) {
        if (!isOpenPushAction(action)) {
            return null;
        }
        String rest = action.substring(ACTION_OPEN_PUSH_PREFIX.length());
        int slash = rest.indexOf('/');
        if (slash >= 0) {
            rest = rest.substring(0, slash);
        }
        return rest;
    }

    @Nullable
    private static String postIdFromObjectJson(@Nullable String objectJson) {
        if (objectJson == null || objectJson.trim().isEmpty() || "{}".equals(objectJson.trim())) {
            return null;
        }
        try {
            org.json.JSONObject object = new org.json.JSONObject(objectJson);
            String postId = object.optString("post_id", "");
            return postId;
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private static String matchRememberedTitle(@NonNull Context context, @NonNull Intent intent) {
        String[] keys = new String[] {
                "TITLE",
                "TITILE",
                "TITLE_REWARDS",
                "title",
                "pn_title",
                Notification.EXTRA_TITLE,
                Notification.EXTRA_TEXT
        };
        for (String key : keys) {
            String raw;
            try {
                raw = intent.getStringExtra(key);
            } catch (Exception e) {
                raw = null;
            }
            if (raw == null || isNumericPostId(raw)) {
                continue;
            }
            String matched = findRememberedByTitle(context, raw);
            if (matched != null) {
                return matched;
            }
        }
        return null;
    }

    @Nullable
    private static String findRememberedByTitle(@NonNull Context context, @Nullable String title) {
        String want = plainPushTitle(title);
        if (want.isEmpty()) {
            return null;
        }
        String raw = SharedPrefsData.getString(
                context.getApplicationContext(),
                com.anyNews.anynews.Utils.Constants.RECENT_PUSH_OPEN_LOG,
                com.anyNews.anynews.Utils.Constants.PREF_NAME);
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        long now = System.currentTimeMillis();
        String[] lines = raw.split("\n");
        for (String line : lines) {
            ParsedPush remembered = parseRememberedLine(line);
            if (remembered == null) {
                continue;
            }
            if (now - remembered.at > HISTORY_PUSH_MAX_AGE_MS) {
                continue;
            }
            if (want.equals(remembered.title)) {
                return remembered.postId;
            }
        }
        return null;
    }

    @Nullable
    private static ParsedPush parseRememberedLine(@Nullable String line) {
        if (line == null || line.isEmpty()) {
            return null;
        }
        int tab = line.indexOf('\t');
        if (tab <= 0) {
            return null;
        }
        int tab2 = line.indexOf('\t', tab + 1);
        if (tab2 < 0) {
            return null;
        }
        try {
            long at = Long.parseLong(line.substring(0, tab).trim());
            String postId = line.substring(tab + 1, tab2).trim();
            String title = line.substring(tab2 + 1).trim();
            if (!isNumericPostId(postId)) {
                return null;
            }
            ParsedPush parsed = new ParsedPush();
            parsed.at = at;
            parsed.postId = postId;
            parsed.title = title;
            return parsed;
        } catch (Exception e) {
            return null;
        }
    }

    @NonNull
    private static String plainPushTitle(@Nullable String title) {
        if (title == null) {
            return "";
        }
        String plain = title.replace("[", "").replace("]", "");
        plain = plain.replace('\n', ' ').replace('\t', ' ').trim();
        while (plain.contains("  ")) {
            plain = plain.replace("  ", " ");
        }
        return plain;
    }

    private static boolean isNumericPostId(@Nullable String postId) {
        if (!isUsablePostId(postId)) {
            return false;
        }
        try {
            return Long.parseLong(postId.trim()) > 0L;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static final class ParsedPush {
        long at;
        String postId;
        String title;
    }

    /**
     * Build the tap Intent. Targets {@link HomePage} with CLEAR_TOP|SINGLE_TOP so a
     * foreground feed receives {@code onNewIntent} (splash singleTask + finish() used
     * to drop TITLE). Unique action + data URI keep PendingIntent extras from colliding.
     */
    @NonNull
    private Intent buildPushOpenIntent(@NonNull String postId, @Nullable String title, @Nullable String objectJson) {
        Intent resultIntent = new Intent(getApplicationContext(), HomePage.class);
        resultIntent.putExtra("notification", true);
        resultIntent.putExtra("TITILE", postId);
        resultIntent.putExtra("TITLE", postId);
        resultIntent.putExtra(EXTRA_PUSH_POST_ID, postId);
        if (title != null) {
            resultIntent.putExtra("TITLE_REWARDS", title);
        }
        if (objectJson != null) {
            resultIntent.putExtra("OBJECT", objectJson);
        }
        String diversifier = objectJson != null ? objectJson : title;
        int notificationId = safeNotificationId(postId, diversifier);
        String actionId = isNumericPostId(postId) ? postId.trim() : String.valueOf(notificationId);
        resultIntent.setAction(ACTION_OPEN_PUSH_PREFIX + actionId);
        // Same int passed to notify(). Tap handlers cancel this id; auto-cancel
        // does not run when RemoteViews own the click.
        resultIntent.putExtra(EXTRA_PUSH_NOTIFICATION_ID, notificationId);
        try {
            resultIntent.setData(Uri.parse("anynews://push/" + postId + "/" + notificationId));
        } catch (Exception ignored) {
        }
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        // Do NOT write PENDING_PUSH_POST here — this runs on FCM receive, not tap.
        // Receive-time sticky made launcher/cold opens land on the latest push, and
        // stacked notifs all shared one sticky (last writer wins). Extras + data URI
        // carry the post id; HomePage/StartingActivity persist sticky only on tap.
        return resultIntent;
    }

    @NonNull
    private PendingIntent buildPushPendingIntent(@NonNull Intent resultIntent, int notificationId) {
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 31) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.getActivity(
                getApplicationContext(),
                notificationId,
                resultIntent,
                flags);
    }

    /** Extra on the tap Intent. Value is the id passed to {@code notify()}. */
    public static final String EXTRA_PUSH_NOTIFICATION_ID = "push_notification_id";

    /** Group key used by article pushes. Must match the builder in {@code sendNotification}. */
    private static final String PUSH_GROUP_KEY = "com.anyNews.anynews.Services.WORK_EMAIL";

    /**
     * Remove the tray row for this tap. {@code setAutoCancel(true)} does not fire
     * when custom RemoteViews handle the click via {@code setOnClickPendingIntent}.
     * Cancels only this id so other stacked pushes stay.
     */
    public static void dismissOpenedPush(@Nullable Context context, @Nullable Intent intent) {
        if (context == null || intent == null) {
            return;
        }
        int id = pushNotificationIdFromIntent(intent);
        if (id == 0) {
            return;
        }
        try {
            NotificationManager nm = (NotificationManager) context.getApplicationContext()
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm == null) {
                return;
            }
            nm.cancel(id);
            cancelOrphanPushGroupSummary(nm, id);
        } catch (Exception ignored) {
        }
    }

    /**
     * Tray id for a push open. Prefer {@link #EXTRA_PUSH_NOTIFICATION_ID}.
     * The FCM data URI is {@code anynews://push/{postId}/{notificationId}} and that
     * second segment is the {@code notify()} id. Splash handoff used to replace it
     * with {@code hashCode(postId)}, which does not match {@code notify()} — when
     * those two differ and the post id fits in an int, the post id is the tray id
     * ({@link #safeNotificationId} returns that int).
     */
    public static int pushNotificationIdFromIntent(@Nullable Intent intent) {
        if (intent == null) {
            return 0;
        }
        try {
            int extra = intent.getIntExtra(EXTRA_PUSH_NOTIFICATION_ID, 0);
            if (extra != 0) {
                return extra;
            }
        } catch (Exception ignored) {
        }
        int uriId = notificationIdFromPushUri(intent.getData());
        int postId = positiveIntPostId(postIdFromPushOpenIntent(intent));
        if (uriId != 0 && postId != 0 && uriId != postId) {
            return postId;
        }
        if (uriId != 0) {
            return uriId;
        }
        return postId;
    }

    @Nullable
    private static String postIdFromPushOpenIntent(@NonNull Intent intent) {
        String fromUri = postIdFromPushUri(intent.getData());
        if (isUsablePostId(fromUri)) {
            return fromUri;
        }
        String titile = intent.getStringExtra("TITILE");
        if (isUsablePostId(titile)) {
            return titile;
        }
        String title = intent.getStringExtra("TITLE");
        if (isUsablePostId(title)) {
            return title;
        }
        return null;
    }

    @Nullable
    private static String postIdFromPushUri(@Nullable Uri uri) {
        if (!isPushOpenUri(uri)) {
            return null;
        }
        String path = uri.getPath();
        if (path == null || path.length() < 2) {
            return null;
        }
        String seg = path.startsWith("/") ? path.substring(1) : path;
        int slash = seg.indexOf('/');
        if (slash > 0) {
            seg = seg.substring(0, slash);
        }
        return seg;
    }

    private static int notificationIdFromPushUri(@Nullable Uri uri) {
        if (!isPushOpenUri(uri) || uri.getPath() == null) {
            return 0;
        }
        String seg = uri.getPath();
        if (seg.startsWith("/")) {
            seg = seg.substring(1);
        }
        int slash = seg.indexOf('/');
        if (slash < 0 || slash >= seg.length() - 1) {
            return 0;
        }
        String idPart = seg.substring(slash + 1);
        int cut = idPart.indexOf('/');
        if (cut >= 0) {
            idPart = idPart.substring(0, cut);
        }
        try {
            return Integer.parseInt(idPart.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private static boolean isPushOpenUri(@Nullable Uri uri) {
        return uri != null
                && "anynews".equalsIgnoreCase(uri.getScheme())
                && "push".equalsIgnoreCase(uri.getHost());
    }

    private static boolean isUsablePostId(@Nullable String postId) {
        if (postId == null) {
            return false;
        }
        String trimmed = postId.trim();
        return !trimmed.isEmpty()
                && !"null".equalsIgnoreCase(trimmed)
                && !"0".equals(trimmed);
    }

    /** Numeric post ids that fit in an int are the tray id. Never 0. */
    private static int positiveIntPostId(@Nullable String postId) {
        if (!isUsablePostId(postId)) {
            return 0;
        }
        try {
            long v = Long.parseLong(postId.trim());
            if (v > 0 && v <= Integer.MAX_VALUE) {
                return (int) v;
            }
        } catch (NumberFormatException ignored) {
        }
        return 0;
    }

    /** Drop an empty system group header after the opened child is gone. */
    private static void cancelOrphanPushGroupSummary(@NonNull NotificationManager nm, int openedId) {
        if (Build.VERSION.SDK_INT < 23) {
            return;
        }
        try {
            StatusBarNotification[] active = nm.getActiveNotifications();
            if (active == null) {
                return;
            }
            boolean otherChild = false;
            for (StatusBarNotification sbn : active) {
                if (sbn == null || sbn.getId() == openedId) {
                    continue;
                }
                Notification n = sbn.getNotification();
                if (n == null || !PUSH_GROUP_KEY.equals(n.getGroup())) {
                    continue;
                }
                if ((n.flags & Notification.FLAG_GROUP_SUMMARY) == 0) {
                    otherChild = true;
                    break;
                }
            }
            if (otherChild) {
                return;
            }
            for (StatusBarNotification sbn : active) {
                if (sbn == null) {
                    continue;
                }
                Notification n = sbn.getNotification();
                if (n == null || !PUSH_GROUP_KEY.equals(n.getGroup())) {
                    continue;
                }
                if ((n.flags & Notification.FLAG_GROUP_SUMMARY) != 0) {
                    nm.cancel(sbn.getTag(), sbn.getId());
                }
            }
        } catch (Exception ignored) {
        }
    }

    /** Custom RemoteViews swallow taps on many OEMs unless each root has a PI. */
    private void bindRemoteViewClicks(
            @NonNull RemoteViews collapsed,
            @NonNull RemoteViews expanded,
            @NonNull PendingIntent pendingIntent) {
        try {
            collapsed.setOnClickPendingIntent(R.id.notification_root_collapsed, pendingIntent);
            collapsed.setOnClickPendingIntent(R.id.notification_titleE, pendingIntent);
            collapsed.setOnClickPendingIntent(R.id.notification_titleTEL, pendingIntent);
            collapsed.setOnClickPendingIntent(R.id.notification_titleTa, pendingIntent);
            collapsed.setOnClickPendingIntent(R.id.imageViewIcon, pendingIntent);
        } catch (Exception ignored) {
        }
        try {
            expanded.setOnClickPendingIntent(R.id.notification_root_expanded, pendingIntent);
            expanded.setOnClickPendingIntent(R.id.notification_title, pendingIntent);
            expanded.setOnClickPendingIntent(R.id.imageViewIconExpanded, pendingIntent);
            expanded.setOnClickPendingIntent(R.id.notificationView, pendingIntent);
        } catch (Exception ignored) {
        }
    }

    private int safeParseColor(String colorString) {
        try {
            return Color.parseColor(colorString);
        } catch (IllegalArgumentException e) {
            return Color.parseColor("#000000");
        }
    }

    private boolean canPostNotifications() {
        // App Settings → Notifications off: OS may still deliver FCM data; we skip tray UI.
        if (!SharedPrefsData.getBool(
                getApplicationContext(),
                Constants.NOTIFICATION_ENABLED,
                Constants.PREF_NAME,
                true)) {
            return false;
        }
        try {
            if (!NotificationManagerCompat.from(getApplicationContext()).areNotificationsEnabled()) {
                return false;
            }
        } catch (Exception ignored) {
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                            getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.max(1, Math.round(dp * density));
    }

    private int prefIntClamped(String key, int fallback, int min, int max) {
        int v = SharedPrefsData.getInt(getApplicationContext(), key, Constants.PREF_NAME);
        if (v <= 0) {
            v = fallback;
        }
        return Math.max(min, Math.min(max, v));
    }

    /** Prefs from splash + optional per-push FCM extras (same keys). */
    private PushAppearance resolveAppearance(@Nullable JSONObject object) {
        PushAppearance a = new PushAppearance();
        a.showArticleImage = SharedPrefsData.getBool(
                getApplicationContext(), Constants.PN_SHOW_ARTICLE_IMAGE, Constants.PREF_NAME, true);
        String mode = SharedPrefsData.getString(
                getApplicationContext(), Constants.PN_STYLE_MODE, Constants.PREF_NAME);
        a.styleMode = normalizeStyleMode(mode);
        String logo = SharedPrefsData.getString(
                getApplicationContext(), Constants.PN_LARGE_ICON_URL, Constants.PREF_NAME);
        a.logoUrl = logo != null ? logo.trim() : "";
        a.includeLogo = SharedPrefsData.getBool(
                getApplicationContext(), Constants.PN_INCLUDE_LOGO_DEFAULT, Constants.PREF_NAME, false);
        a.leftIconSizeDp = prefIntClamped(Constants.PN_LEFT_ICON_SIZE_DP, DEFAULT_LEFT_DP, 24, 96);
        a.thumbSizeDp = prefIntClamped(Constants.PN_THUMB_SIZE_DP, DEFAULT_THUMB_DP, 24, 120);
        a.titleSizeSp = prefIntClamped(Constants.PN_TITLE_SIZE_SP, DEFAULT_TITLE_SP, 10, 22);

        if (object != null) {
            if (object.has("pn_show_article_image")) {
                a.showArticleImage = parseTruthy(object.optString("pn_show_article_image", "true"), true);
            } else if (object.has("show_article_image")) {
                a.showArticleImage = parseTruthy(object.optString("show_article_image", "true"), true);
            }
            if (object.has("pn_style_mode")) {
                a.styleMode = normalizeStyleMode(object.optString("pn_style_mode", STYLE_COMPACT));
            } else if (object.has("pn_style")) {
                a.styleMode = normalizeStyleMode(object.optString("pn_style", STYLE_COMPACT));
            }
            if (object.has("pn_large_icon_url")) {
                a.logoUrl = object.optString("pn_large_icon_url", "").trim();
            } else if (object.has("push_logo_url")) {
                a.logoUrl = object.optString("push_logo_url", "").trim();
            } else if (object.has("large_icon_url")) {
                a.logoUrl = object.optString("large_icon_url", "").trim();
            }
            if (object.has("include_logo")) {
                a.includeLogo = parseTruthy(object.optString("include_logo", "false"), false);
            } else if (object.has("include_logo_in_push")) {
                a.includeLogo = parseTruthy(object.optString("include_logo_in_push", "false"), false);
            } else if (object.has("push_include_logo")) {
                a.includeLogo = parseTruthy(object.optString("push_include_logo", "false"), false);
            }
            if (object.has("pn_left_icon_size_dp")) {
                a.leftIconSizeDp = clampInt(object.optInt("pn_left_icon_size_dp", DEFAULT_LEFT_DP), 24, 96);
            } else if (object.has("push_left_icon_size_dp")) {
                a.leftIconSizeDp = clampInt(object.optInt("push_left_icon_size_dp", DEFAULT_LEFT_DP), 24, 96);
            }
            if (object.has("pn_thumb_size_dp")) {
                a.thumbSizeDp = clampInt(object.optInt("pn_thumb_size_dp", DEFAULT_THUMB_DP), 24, 120);
            } else if (object.has("push_thumb_size_dp")) {
                a.thumbSizeDp = clampInt(object.optInt("push_thumb_size_dp", DEFAULT_THUMB_DP), 24, 120);
            }
            if (object.has("pn_title_size_sp")) {
                a.titleSizeSp = clampInt(object.optInt("pn_title_size_sp", DEFAULT_TITLE_SP), 10, 22);
            }
        }
        return a;
    }

    private static int clampInt(int v, int min, int max) {
        if (v <= 0) {
            return min;
        }
        return Math.max(min, Math.min(max, v));
    }

    private static boolean shouldLoadArticleImage(PushAppearance appearance, String imageUrl) {
        if (appearance == null || !appearance.showArticleImage) {
            return false;
        }
        if (STYLE_TEXT_ONLY.equals(appearance.styleMode)) {
            return false;
        }
        return imageUrl != null && !imageUrl.trim().isEmpty()
                && !"null".equalsIgnoreCase(imageUrl.trim())
                && !"undefined".equalsIgnoreCase(imageUrl.trim());
    }

    private static String normalizeStyleMode(String raw) {
        if (raw != null && STYLE_TEXT_ONLY.equalsIgnoreCase(raw.trim())) {
            return STYLE_TEXT_ONLY;
        }
        return STYLE_COMPACT;
    }

    private static boolean parseTruthy(String raw, boolean defaultValue) {
        if (raw == null || raw.isEmpty()) {
            return defaultValue;
        }
        String v = raw.trim().toLowerCase();
        if ("false".equals(v) || "0".equals(v) || "no".equals(v) || "off".equals(v)) {
            return false;
        }
        if ("true".equals(v) || "1".equals(v) || "yes".equals(v) || "on".equals(v)) {
            return true;
        }
        return defaultValue;
    }

    public SpannableStringBuilder makeSpannable(String text, String regex, String colorCode) {
        int colorIndex = 0;
        ArrayList<String> colorCodeList = new ArrayList<>();
        if (colorCode != null && colorCode.contains(",")) {
            colorCodeList = new ArrayList<>(Arrays.asList(colorCode.split(",")));
        } else if (colorCode != null) {
            colorCodeList.add(colorCode);
        } else {
            colorCodeList.add("#000000");
        }

        StringBuffer sb = new StringBuffer();
        SpannableStringBuilder spannable = new SpannableStringBuilder();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text != null ? text : "");

        while (matcher.find()) {
            sb.setLength(0);
            String group = matcher.group();
            String spanText = group.substring(1, group.length() - 1);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(spanText));
            spannable.append(sb.toString());
            int start = spannable.length() - spanText.length();
            final StyleSpan boldSpan = new StyleSpan(android.graphics.Typeface.BOLD);
            spannable.setSpan(
                    new ForegroundColorSpan(safeParseColor(colorCodeList.get(colorIndex % colorCodeList.size()))),
                    start,
                    spannable.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(boldSpan, start, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            colorIndex++;
        }

        sb.setLength(0);
        matcher.appendTail(sb);
        spannable.append(sb.toString());
        return spannable;
    }

    /**
     * Full-color red AnyNews brand mark for custom RemoteViews only.
     * Never {@code ic_launcher} adaptive monochrome / white glyph look.
     */
    private Bitmap redBrandLogoBitmap(int sizePx) {
        try {
            Bitmap src = BitmapFactory.decodeResource(getResources(), R.drawable.app_share_logo);
            if (src == null) {
                src = BitmapFactory.decodeResource(getResources(), R.drawable.new_splash_app_logo);
            }
            if (src == null) {
                src = BitmapFactory.decodeResource(getResources(), R.drawable.app_splash_logo);
            }
            return scaleCenterCrop(src, sizePx, sizePx, true);
        } catch (Exception e) {
            return null;
        }
    }

    private void applyRemoteViewSizes(RemoteViews views, int iconViewId, int titleViewId,
                                      int iconDp, int titleSp) {
        try {
            if (Build.VERSION.SDK_INT >= 31) {
                views.setViewLayoutWidth(iconViewId, iconDp, android.util.TypedValue.COMPLEX_UNIT_DIP);
                views.setViewLayoutHeight(iconViewId, iconDp, android.util.TypedValue.COMPLEX_UNIT_DIP);
            }
            if (titleViewId != 0 && titleSp > 0) {
                views.setTextViewTextSize(
                        titleViewId, android.util.TypedValue.COMPLEX_UNIT_SP, titleSp);
            }
        } catch (Exception ignored) {
        }
    }

    @Nullable
    private static Bitmap downloadBitmap(String src, int maxW, int maxH) {
        if (src == null || src.trim().isEmpty()) {
            return null;
        }
        HttpURLConnection connection = null;
        InputStream input = null;
        try {
            URL url = new URL(src.trim());
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(10000);
            connection.setDoInput(true);
            connection.connect();
            input = connection.getInputStream();
            Bitmap raw = BitmapFactory.decodeStream(input);
            if (raw == null) {
                return null;
            }
            return scaleToFit(raw, maxW, maxH);
        } catch (IOException e) {
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ignored) {
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Nullable
    private static Bitmap scaleToFit(@Nullable Bitmap src, int maxW, int maxH) {
        if (src == null) {
            return null;
        }
        int w = src.getWidth();
        int h = src.getHeight();
        if (w <= 0 || h <= 0) {
            return src;
        }
        float scale = Math.min((float) maxW / w, (float) maxH / h);
        if (scale >= 1f) {
            return src;
        }
        int nw = Math.max(1, Math.round(w * scale));
        int nh = Math.max(1, Math.round(h * scale));
        try {
            Bitmap out = Bitmap.createScaledBitmap(src, nw, nh, true);
            if (out != src) {
                src.recycle();
            }
            return out;
        } catch (Exception e) {
            return src;
        }
    }

    @Nullable
    private static Bitmap scaleCenterCrop(@Nullable Bitmap src, int sizeX, int sizeY, boolean rounded) {
        if (src == null) {
            return null;
        }
        try {
            int w = src.getWidth();
            int h = src.getHeight();
            if (w <= 0 || h <= 0) {
                return src;
            }
            float scale = Math.max((float) sizeX / w, (float) sizeY / h);
            int scaledW = Math.max(1, Math.round(w * scale));
            int scaledH = Math.max(1, Math.round(h * scale));
            Bitmap scaled = Bitmap.createScaledBitmap(src, scaledW, scaledH, true);
            int x = Math.max(0, (scaledW - sizeX) / 2);
            int y = Math.max(0, (scaledH - sizeY) / 2);
            Bitmap cropped = Bitmap.createBitmap(scaled, x, y, Math.min(sizeX, scaledW), Math.min(sizeY, scaledH));
            if (scaled != src && scaled != cropped) {
                scaled.recycle();
            }
            if (!rounded) {
                return cropped;
            }
            return roundBitmap(cropped, 10f);
        } catch (Exception e) {
            return src;
        }
    }

    @Nullable
    private static Bitmap roundBitmap(@Nullable Bitmap src, float radiusPx) {
        if (src == null) {
            return null;
        }
        try {
            Bitmap out = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(out);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            Rect rect = new Rect(0, 0, src.getWidth(), src.getHeight());
            RectF rectF = new RectF(rect);
            canvas.drawRoundRect(rectF, radiusPx, radiusPx, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(src, rect, rect, paint);
            if (out != src) {
                src.recycle();
            }
            return out;
        } catch (Exception e) {
            return src;
        }
    }

    private void presentNewsNotification(
            String title,
            String post_id,
            String object,
            String pnColor,
            boolean showPNTime,
            @Nullable Bitmap articleImage,
            @Nullable Bitmap logoImage,
            @Nullable PushAppearance appearance) {

        if (appearance == null) {
            appearance = resolveAppearance(null);
        }
        rememberDisplayedPush(getApplicationContext(), post_id, title);

        boolean showImage = appearance.showArticleImage
                && !STYLE_TEXT_ONLY.equals(appearance.styleMode)
                && articleImage != null;

        int leftDp = appearance.includeLogo ? appearance.leftIconSizeDp : appearance.thumbSizeDp;
        int leftPx = dpToPx(leftDp);
        int titleSp = appearance.titleSizeSp;

        RemoteViews collapsedView = new RemoteViews(getPackageName(), R.layout.new_collapsed_request);
        RemoteViews expandedView = new RemoteViews(getPackageName(), R.layout.new_request);
        final Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Spannable pnTitle = makeSpannable(title, "\\[.*?\\]", pnColor);

        String GROUP_KEY_WORK_EMAIL = PUSH_GROUP_KEY;
        collapsedView.setTextViewText(R.id.notification_titleE, pnTitle);
        collapsedView.setTextViewText(R.id.notification_titleTEL, pnTitle);
        collapsedView.setTextViewText(R.id.notification_titleTa, pnTitle);
        expandedView.setTextViewText(R.id.notification_title, pnTitle);

        applyRemoteViewSizes(collapsedView, R.id.imageViewIcon, R.id.notification_titleE, leftDp, titleSp);
        applyRemoteViewSizes(collapsedView, R.id.imageViewIcon, R.id.notification_titleTEL, leftDp, titleSp);
        applyRemoteViewSizes(collapsedView, R.id.imageViewIcon, R.id.notification_titleTa, leftDp, titleSp);
        applyRemoteViewSizes(expandedView, R.id.imageViewIconExpanded, R.id.notification_title, leftDp, titleSp);

        int languageId = SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
        if (languageId == 4) {
            collapsedView.setViewVisibility(R.id.notification_titleE, View.GONE);
            collapsedView.setViewVisibility(R.id.notification_titleTEL, View.GONE);
            collapsedView.setViewVisibility(R.id.notification_titleTa, View.VISIBLE);
        } else if (languageId == 1) {
            collapsedView.setViewVisibility(R.id.notification_titleE, View.VISIBLE);
            collapsedView.setViewVisibility(R.id.notification_titleTEL, View.GONE);
            collapsedView.setViewVisibility(R.id.notification_titleTa, View.GONE);
        } else {
            collapsedView.setViewVisibility(R.id.notification_titleE, View.GONE);
            collapsedView.setViewVisibility(R.id.notification_titleTEL, View.VISIBLE);
            collapsedView.setViewVisibility(R.id.notification_titleTa, View.GONE);
        }

        // Left: brand logo (if include_logo) OR article tile — never white launcher.
        Bitmap leftIcon = null;
        if (appearance.includeLogo) {
            if (logoImage != null) {
                leftIcon = scaleCenterCrop(logoImage, leftPx, leftPx, true);
            }
            if (leftIcon == null) {
                leftIcon = redBrandLogoBitmap(leftPx);
            }
        } else if (showImage) {
            try {
                Bitmap articleCopy = articleImage.copy(articleImage.getConfig() != null
                        ? articleImage.getConfig()
                        : Bitmap.Config.ARGB_8888, false);
                leftIcon = scaleCenterCrop(
                        articleCopy != null ? articleCopy : articleImage, leftPx, leftPx, true);
            } catch (Exception ignored) {
                leftIcon = null;
            }
        }

        if (leftIcon != null) {
            collapsedView.setImageViewBitmap(R.id.imageViewIcon, leftIcon);
            expandedView.setImageViewBitmap(R.id.imageViewIconExpanded, leftIcon);
            collapsedView.setViewVisibility(R.id.imageViewIcon, View.VISIBLE);
            expandedView.setViewVisibility(R.id.imageViewIconExpanded, View.VISIBLE);
        } else {
            // No logo, no article yet — hide left slot (avoid white app icon).
            collapsedView.setViewVisibility(R.id.imageViewIcon, View.GONE);
            expandedView.setViewVisibility(R.id.imageViewIconExpanded, View.GONE);
        }
        collapsedView.setViewVisibility(R.id.notificationThumb, View.GONE);

        if (showImage) {
            Bitmap big = scaleToFit(articleImage, BIG_PICTURE_MAX_W, BIG_PICTURE_MAX_H);
            if (big != null) {
                expandedView.setImageViewBitmap(R.id.notificationView, big);
                expandedView.setViewVisibility(R.id.notificationView, View.VISIBLE);
            } else {
                expandedView.setViewVisibility(R.id.notificationView, View.GONE);
            }
        } else {
            expandedView.setViewVisibility(R.id.notificationView, View.GONE);
        }

        Intent resultIntent = buildPushOpenIntent(post_id, title, object);
        String diversifier = object != null ? object : title;
        int notificationId = safeNotificationId(post_id, diversifier);
        PendingIntent pendingIntent = buildPushPendingIntent(resultIntent, notificationId);
        bindRemoteViewClicks(collapsedView, expandedView, pendingIntent);

        try {
            if (!canPostNotifications()) {
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null
                        && notificationManager.getNotificationChannel(Constants.Channel_ID) == null) {
                    NotificationChannel channel = new NotificationChannel(
                            Constants.Channel_ID,
                            "Any News",
                            NotificationManager.IMPORTANCE_HIGH
                    );
                    channel.enableVibration(true);
                    notificationManager.createNotificationChannel(channel);
                }
            }

            // Strip [] for plain fallback title (some OEMs fall back to contentTitle).
            String plainTitle = title != null ? title.replaceAll("\\[|\\]", "") : "AnyNews";

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), Constants.Channel_ID)
                    .setSmallIcon(R.drawable.ic_arman_notificatio)
                    .setContentTitle(plainTitle)
                    .setCustomContentView(collapsedView)
                    .setCustomBigContentView(expandedView)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setVibrate(new long[]{1000, 1000, 1000, 1000})
                    .setSound(uri)
                    .setShowWhen(showPNTime)
                    .setGroup(GROUP_KEY_WORK_EMAIL)
                    .setGroupSummary(false)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setOnlyAlertOnce(true);
            // Intentionally omit setLargeIcon — OEMs place it on the RIGHT of the row.

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setColor(getResources().getColor(R.color.colorPrimary));
            }

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
            // Stable id so the async image pass updates the same tray entry.
            int notifyId = notificationId != 0 ? notificationId : Math.abs(title != null ? title.hashCode() : 1);
            if (notifyId == 0) {
                notifyId = 1;
            }
            notificationManager.notify(notifyId, builder.build());
        } catch (Exception e) {
            Log.v("NotificationData", String.valueOf(e.getMessage()));
        }
    }

    private void handleForceLogout(JSONObject object) {
        try {
            // Server already cleared mobile on this draft. Clear local auth only —
            // keep LanguageID / geo / language pack / translator prefs.
            boolean loggedIn = SharedPrefsData.getBool(
                    getApplicationContext(), Constants.is_login, Constants.PREF_NAME);
            if (!loggedIn
                    && SharedPrefsData.getBool(
                            getApplicationContext(),
                            Constants.FORCE_LOGGED_OUT,
                            Constants.PREF_NAME)) {
                showLog("force_logout ignored (already guest)");
                return;
            }
            showLog("force_logout reason=" + object.optString("reason", ""));
            ForceLogoutHelper.clearAuthSession(getApplicationContext(), true, true);
        } catch (Exception e) {
            Logone.e("ForceLogout", "handleForceLogout failed", e);
        }
    }

    private void handleReporterStatusChanged(JSONObject object) {
        try {
            boolean isReporter = "true".equalsIgnoreCase(
                    object.optString("is_reporter", "false"));
            int reporterLangId = object.optInt("reporter_language_id", 0);

            SharedPrefsData.putBool(
                    getApplicationContext(), Constants.is_reporter, isReporter, Constants.PREF_NAME);
            if (reporterLangId > 0) {
                SharedPrefsData.putInt(
                        getApplicationContext(),
                        Constants.reporter_language_id,
                        reporterLangId,
                        Constants.PREF_NAME);
            }

            Intent intent = new Intent(Constants.REPORTER_STATUS_CHANGED);
            intent.putExtra(Constants.is_reporter, isReporter);
            if (reporterLangId > 0) {
                intent.putExtra(Constants.reporter_language_id, reporterLangId);
            }
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            showLog("reporter_status_changed isReporter:" + isReporter);
        } catch (Exception e) {
            Logone.e("ReporterStatus", "handleReporterStatusChanged failed", e);
        }
    }

    // Comment-reply alerts show actor + snippet with a standard notification
    // layout (the news RemoteViews only render a bracket-styled title). Tapping
    // reuses the existing push-open path: StartingActivity routes to the article
    // by post_id, where the comments panel lives.
    private void sendCommentReplyNotification(JSONObject object) {
        try {
            if (!canPostNotifications()) {
                return;
            }

            String actor = object.optString("actor_name", object.optString("title", "Someone"));
            String body = object.optString("body", "");
            if (body == null || body.isEmpty()) {
                body = actor + " has replied to your message";
            }
            String snippet = object.optString("snippet", object.optString("comment", ""));
            String post_id = object.optString("post_id", "0");
            String bigText = body;
            if (snippet != null && !snippet.isEmpty()) {
                bigText = body + "\n\n\"" + snippet + "\"";
            }

            Intent resultIntent = buildPushOpenIntent(post_id, actor, object.toString());
            int notificationId = safeNotificationId(post_id, object.toString());
            PendingIntent pendingIntent = buildPushPendingIntent(resultIntent, notificationId);

            final Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null
                        && notificationManager.getNotificationChannel(Constants.Channel_ID) == null) {
                    NotificationChannel channel = new NotificationChannel(
                            Constants.Channel_ID,
                            "Any News",
                            NotificationManager.IMPORTANCE_HIGH);
                    channel.enableVibration(true);
                    notificationManager.createNotificationChannel(channel);
                }
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), Constants.Channel_ID)
                    .setSmallIcon(R.drawable.ic_arman_notificatio)
                    .setContentTitle(actor)
                    .setContentText(body)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setVibrate(new long[]{1000, 1000, 1000, 1000})
                    .setSound(uri)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            // Red brand mark if largeIcon is used — never white launcher.
            Bitmap logo = redBrandLogoBitmap(dpToPx(48));
            if (logo != null) {
                builder.setLargeIcon(logo);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setColor(getResources().getColor(R.color.colorPrimary));
            }

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
            notificationManager.notify(notificationId != 0 ? notificationId : 1, builder.build());
        } catch (Exception e) {
            Log.v("NotificationData", String.valueOf(e.getMessage()));
        }
    }

    private final class LoadNotificationImagesTask extends AsyncTask<Void, Void, ImageLoadResult> {
        private final String imageUrl;
        private final String title;
        private final String postId;
        private final String objectJson;
        private final String pnColor;
        private final boolean showTime;
        private final PushAppearance appearance;

        LoadNotificationImagesTask(
                String imageUrl,
                String title,
                String postId,
                String objectJson,
                String pnColor,
                boolean showTime,
                PushAppearance appearance) {
            this.imageUrl = imageUrl;
            this.title = title;
            this.postId = postId;
            this.objectJson = objectJson;
            this.pnColor = pnColor;
            this.showTime = showTime;
            this.appearance = appearance;
        }

        @Override
        protected ImageLoadResult doInBackground(Void... params) {
            ImageLoadResult result = new ImageLoadResult();
            int logoPx = dpToPx(appearance != null ? appearance.leftIconSizeDp : DEFAULT_LEFT_DP);
            if (appearance != null && appearance.includeLogo) {
                String logoUrl = appearance.logoUrl;
                if (logoUrl != null && !logoUrl.isEmpty()) {
                    Bitmap remoteLogo = downloadBitmap(logoUrl, logoPx * 2, logoPx * 2);
                    result.logo = scaleCenterCrop(remoteLogo, logoPx, logoPx, true);
                }
                if (result.logo == null) {
                    result.logo = redBrandLogoBitmap(logoPx);
                }
            }
            if (shouldLoadArticleImage(appearance, imageUrl)) {
                result.article = downloadBitmap(imageUrl, BIG_PICTURE_MAX_W, BIG_PICTURE_MAX_H);
            }
            return result;
        }

        @Override
        protected void onPostExecute(ImageLoadResult result) {
            if (result == null) {
                return;
            }
            // Re-present only when we actually fetched something to show.
            if (result.article == null && result.logo == null) {
                return;
            }
            presentNewsNotification(
                    title,
                    postId,
                    objectJson,
                    pnColor,
                    showTime,
                    result.article,
                    result.logo,
                    appearance);
        }
    }

    private static final class ImageLoadResult {
        @Nullable Bitmap article;
        @Nullable Bitmap logo;
    }

    private static final class PushAppearance {
        boolean showArticleImage = true;
        boolean includeLogo = false;
        String styleMode = STYLE_COMPACT;
        String logoUrl = "";
        int leftIconSizeDp = DEFAULT_LEFT_DP;
        int thumbSizeDp = DEFAULT_THUMB_DP;
        int titleSizeSp = DEFAULT_TITLE_SP;
    }

    public void writePushReceive(
            String userId,
            String type_firebase,
            long post_id,
            long current_time,
            int language_id,
            long sent_at,
            long draft_id,
            String device_id,
            boolean showPNTime) {
        PushReceive appOpen = new PushReceive(
                type_firebase, post_id, current_time, language_id, sent_at, draft_id, device_id, showPNTime);
        myRef.child("Events").push().setValue(appOpen);
    }

    @IgnoreExtraProperties
    @Keep
    public class PushReceive {

        public String type;
        public long post_id;
        public long created_at;
        public int language_id;
        public long sent_at;
        public long draft_id;
        public String device_id;
        public boolean showPNTime;

        public PushReceive() {
        }

        public PushReceive(
                String type_firebase,
                long post_id,
                long current_time,
                int language_id,
                long sent_at,
                long draft_id,
                String device_id,
                boolean showPNTime) {
            this.type = type_firebase;
            this.post_id = post_id;
            this.created_at = current_time;
            this.language_id = language_id;
            this.sent_at = sent_at;
            this.draft_id = draft_id;
            this.device_id = device_id;
            this.showPNTime = showPNTime;
        }
    }
}
