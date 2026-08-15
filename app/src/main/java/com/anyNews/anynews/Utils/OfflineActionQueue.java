package com.anyNews.anynews.Utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.LikeSelectionRequest;
import com.anyNews.anynews.Pojo.ListOfCommentsRequest;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import retrofit2.Response;

import static com.anyNews.anynews.Utils.MyUtils.encrypt;

/**
 * Disk-backed queue for engagement mutations performed while offline.
 * Flushes to existing v3 APIs ({@code update_like}, {@code comment}) when
 * connectivity returns. Original offline timestamps travel as
 * {@code offline}/{@code offline_at}/{@code created_at_ms} when the API body accepts them.
 */
public final class OfflineActionQueue {

    private static final String TAG = "OfflineActionQueue";
    private static final String FILE_NAME = "offline_action_queue_v1.json";
    private static final Gson GSON = new Gson();
    private static final Type LIST_TYPE = new TypeToken<List<Action>>() {}.getType();
    private static final int MAX_QUEUE = 400;

    public static final String TYPE_LIKE = "LIKE";
    public static final String TYPE_COMMENT = "COMMENT";

    private static final ExecutorService IO =
            Executors.newSingleThreadExecutor(
                    r -> {
                        Thread t = new Thread(r, "OfflineActionQueue-IO");
                        t.setPriority(Thread.NORM_PRIORITY - 1);
                        return t;
                    });
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final AtomicBoolean flushInFlight = new AtomicBoolean(false);

    private OfflineActionQueue() {}

    public static final class Action {
        public String id;
        public String type;
        public int draft_id;
        public long post_id;
        public long sub_post_id;
        public int like_type;
        public String comment;
        public Long parent_comment_id;
        public String user_email;
        public String device_id;
        public long created_at_ms;
        public boolean offline = true;
    }

    public static void enqueueLike(
            @NonNull Context context,
            long postId,
            long subPostId,
            int likeType) {
        Action a = base(context, TYPE_LIKE, postId, subPostId);
        a.like_type = likeType;
        enqueue(context, a);
    }

    public static void enqueueComment(
            @NonNull Context context,
            long postId,
            long subPostId,
            @Nullable String comment,
            @Nullable Long parentCommentId,
            @Nullable String userEmail) {
        Action a = base(context, TYPE_COMMENT, postId, subPostId);
        a.comment = comment != null ? comment : "";
        a.parent_comment_id = parentCommentId;
        a.user_email = userEmail;
        enqueue(context, a);
    }

    @NonNull
    private static Action base(
            @NonNull Context context, @NonNull String type, long postId, long subPostId) {
        Action a = new Action();
        a.id = UUID.randomUUID().toString();
        a.type = type;
        a.draft_id = SharedPrefsData.getInt(context, Constants.DraftID, Constants.PREF_NAME);
        a.post_id = postId;
        a.sub_post_id = subPostId;
        a.created_at_ms = System.currentTimeMillis();
        a.offline = true;
        try {
            Allsharedpreference prefs = new Allsharedpreference(context.getApplicationContext());
            a.device_id = prefs.getShared_String(Allsharedpreference.DEVICE_ID);
            if (a.device_id == null) a.device_id = "";
        } catch (Exception ignored) {
            a.device_id = "";
        }
        return a;
    }

    private static void enqueue(@NonNull Context context, @NonNull Action action) {
        final Context app = context.getApplicationContext();
        IO.execute(
                () -> {
                    List<Action> ops = loadSync(app);
                    ops.add(action);
                    while (ops.size() > MAX_QUEUE) {
                        ops.remove(0);
                    }
                    writeSync(app, ops);
                    Logone.v(TAG, "enqueued " + action.type + " post=" + action.post_id);
                });
    }

    /** Fire-and-forget flush when online (connectivity restore / resume). */
    public static void flushAll(@Nullable Context context) {
        if (context == null) return;
        final Context app = context.getApplicationContext();
        if (!BookmarkStore.isNetworkAvailable(app)) return;
        if (!flushInFlight.compareAndSet(false, true)) return;
        IO.execute(
                () -> {
                    try {
                        List<Action> ops = loadSync(app);
                        if (ops.isEmpty()) return;
                        List<Action> remaining = new ArrayList<>();
                        for (Action op : ops) {
                            if (op == null || TextUtils.isEmpty(op.type)) continue;
                            boolean ok = false;
                            try {
                                if (TYPE_LIKE.equals(op.type)) {
                                    ok = postLikeBlocking(app, op);
                                } else if (TYPE_COMMENT.equals(op.type)) {
                                    ok = postCommentBlocking(app, op);
                                } else {
                                    ok = true; // drop unknown
                                }
                            } catch (Exception e) {
                                Logone.w(e, TAG + " flush item failed");
                                ok = false;
                            }
                            if (!ok) remaining.add(op);
                        }
                        writeSync(app, remaining);
                    } finally {
                        flushInFlight.set(false);
                    }
                });
    }

    private static boolean postLikeBlocking(@NonNull Context app, @NonNull Action op)
            throws Exception {
        LikeSelectionRequest requestModel = new LikeSelectionRequest();
        requestModel.setDraft_id((long) op.draft_id);
        requestModel.setPost_id(op.post_id);
        requestModel.setSub_post_id(op.sub_post_id);
        requestModel.setType(op.like_type);
        // Optional offline metadata — harmless if server ignores
        JsonObject plain = GSON.toJsonTree(requestModel).getAsJsonObject();
        plain.addProperty("offline", true);
        plain.addProperty("offline_at", op.created_at_ms);
        plain.addProperty("created_at_ms", op.created_at_ms);
        if (!TextUtils.isEmpty(op.device_id)) {
            plain.addProperty("device_id", op.device_id);
        }
        String encrypted = encrypt(plain.toString());
        EncritonRequestData enc = new EncritonRequestData();
        enc.setData(encrypted);
        JsonObject body = GSON.toJsonTree(enc).getAsJsonObject();
        APIService api = RestApiClient.getClient().create(APIService.class);
        Response<EncreptionRequest> response = api.update_like("v3", body).execute();
        return response.isSuccessful() && response.body() != null;
    }

    private static boolean postCommentBlocking(@NonNull Context app, @NonNull Action op)
            throws Exception {
        ListOfCommentsRequest requestModel = new ListOfCommentsRequest();
        requestModel.setDraftId(op.draft_id);
        requestModel.setPostId(op.post_id);
        requestModel.setSubPostId(op.sub_post_id);
        requestModel.setComment(op.comment != null ? op.comment : "");
        if (op.parent_comment_id != null && op.parent_comment_id > 0) {
            requestModel.setParentCommentId(op.parent_comment_id);
        }
        if (!TextUtils.isEmpty(op.user_email)) {
            requestModel.setUserid(op.user_email);
        }
        JsonObject plain = GSON.toJsonTree(requestModel).getAsJsonObject();
        plain.addProperty("offline", true);
        plain.addProperty("offline_at", op.created_at_ms);
        plain.addProperty("created_at_ms", op.created_at_ms);
        if (!TextUtils.isEmpty(op.device_id)) {
            plain.addProperty("device_id", op.device_id);
        }
        String encrypted = encrypt(plain.toString());
        EncritonRequestData enc = new EncritonRequestData();
        enc.setData(encrypted);
        JsonObject body = GSON.toJsonTree(enc).getAsJsonObject();
        APIService api = RestApiClient.getClient().create(APIService.class);
        Response<EncreptionRequest> response = api.comment("v3", body).execute();
        return response.isSuccessful() && response.body() != null;
    }

    @NonNull
    private static List<Action> loadSync(@NonNull Context app) {
        File f = new File(app.getFilesDir(), FILE_NAME);
        if (!f.exists() || f.length() < 2) return new ArrayList<>();
        try (FileInputStream in = new FileInputStream(f);
                InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            List<Action> list = GSON.fromJson(reader, LIST_TYPE);
            if (list == null) return new ArrayList<>();
            // Drop nulls
            Iterator<Action> it = list.iterator();
            while (it.hasNext()) {
                if (it.next() == null) it.remove();
            }
            return list;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static void writeSync(@NonNull Context app, @NonNull List<Action> ops) {
        File f = new File(app.getFilesDir(), FILE_NAME);
        File tmp = new File(f.getAbsolutePath() + ".tmp");
        try (FileOutputStream out = new FileOutputStream(tmp, false);
                OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
            writer.write(GSON.toJson(ops));
            writer.flush();
        } catch (Exception e) {
            Logone.w(e, TAG + " write failed");
            return;
        }
        if (f.exists() && !f.delete()) {
            // best-effort
        }
        if (!tmp.renameTo(f)) {
            try (FileOutputStream out = new FileOutputStream(f, false);
                    OutputStreamWriter writer =
                            new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
                writer.write(GSON.toJson(ops));
            } catch (Exception ignored) {
            }
            //noinspection ResultOfMethodCallIgnored
            tmp.delete();
        }
    }
}
