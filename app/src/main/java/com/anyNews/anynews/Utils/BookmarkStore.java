package com.anyNews.anynews.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

/**
 * Offline-first bookmarks: full post JSON on disk + encrypted sync with server.
 *
 * Disk access is serialized: all index / post file RMW holds {@link #DISK}, IO mutations
 * run on a single-thread executor, and file writes are atomic (tmp + rename) so a read
 * never observes a truncated index or post file mid-write.
 */
public final class BookmarkStore {

    private static final String DIR_NAME = "bookmarks_v1";
    private static final String INDEX_FILE = "index.json";
    private static final String PENDING_FILE = "pending_ops.json";
    private static final Gson GSON = new Gson();
    private static final Type LIST_LONG = new TypeToken<List<Long>>() {}.getType();
    private static final Type PENDING_TYPE =
            new TypeToken<List<PendingOp>>() {}.getType();

    /** Guards every index / pending / post-file read and write. */
    private static final Object DISK = new Object();

    private static final ExecutorService IO =
            Executors.newSingleThreadExecutor(
                    r -> {
                        Thread t = new Thread(r, "BookmarkStore-IO");
                        t.setPriority(Thread.NORM_PRIORITY - 1);
                        return t;
                    });
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private BookmarkStore() {}

    public static boolean isNetworkAvailable(@NonNull Context context) {
        try {
            ConnectivityManager cm =
                    (ConnectivityManager)
                            context.getApplicationContext()
                                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                android.net.Network network = cm.getActiveNetwork();
                if (network == null) return false;
                NetworkCapabilities caps = cm.getNetworkCapabilities(network);
                return caps != null
                        && (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                                || caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                                || caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
            }
            NetworkInfo info = cm.getActiveNetworkInfo();
            return info != null && info.isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isBookmarked(@NonNull Context context, long postId) {
        if (postId <= 0) return false;
        synchronized (DISK) {
            return loadIndexLocked(context).contains(postId);
        }
    }

    @Nullable
    public static ModelClass.Datum getLocalPost(@NonNull Context context, long postId) {
        if (postId <= 0) return null;
        synchronized (DISK) {
            return readPostFileLocked(context, postId);
        }
    }

    @NonNull
    public static List<ModelClass.Datum> listLocal(@NonNull Context context) {
        synchronized (DISK) {
            return listLocalLocked(context);
        }
    }

    /**
     * Load bookmarks after any queued disk writes finish (same IO thread). Prefer this for
     * UI reloads so a concurrent save never races a partial read.
     */
    public static void listLocalAsync(
            @NonNull Context context, @NonNull ListCallback callback) {
        final Context app = context.getApplicationContext();
        IO.execute(
                () -> {
                    final List<ModelClass.Datum> list = listLocal(app);
                    MAIN.post(() -> callback.onListed(list));
                });
    }

    /**
     * Bookmark count for UI badges (index size after queued writes finish).
     */
    public static void countLocalAsync(
            @NonNull Context context, @NonNull CountCallback callback) {
        final Context app = context.getApplicationContext();
        IO.execute(
                () -> {
                    final int n;
                    synchronized (DISK) {
                        n = loadIndexLocked(app).size();
                    }
                    MAIN.post(() -> callback.onCounted(n));
                });
    }

    public interface ListCallback {
        void onListed(@NonNull List<ModelClass.Datum> items);
    }

    public interface CountCallback {
        void onCounted(int count);
    }

    @NonNull
    private static List<ModelClass.Datum> listLocalLocked(@NonNull Context context) {
        List<Long> order = loadIndexLocked(context);
        List<ModelClass.Datum> out = new ArrayList<>();
        for (Long id : order) {
            if (id == null || id <= 0) continue;
            ModelClass.Datum d = readPostFileLocked(context, id);
            if (d != null) {
                out.add(d);
            }
            // Keep index entry even if body parse fails temporarily; next save rewrites body.
            // Do not drop the id here — that caused peers to vanish after a bad mid-write read.
        }
        return out;
    }

    /**
     * Save post offline (optimistically bookmarked). Queues server add when offline.
     */
    public static void saveLocal(
            @NonNull Context context, @Nullable ModelClass.Datum post, boolean pendingAdd) {
        if (post == null || post.getPostId() == null || post.getPostId() <= 0) return;
        final Context app = context.getApplicationContext();
        // Snapshot so UI thread mutations cannot corrupt the in-flight write.
        final ModelClass.Datum copy = snapshot(post);
        if (copy == null) return;
        copy.setBookmarkStatus(1);
        IO.execute(() -> saveLocalSync(app, copy, pendingAdd));
    }

    /** Blocking disk write of full post JSON + index (call from IO thread). */
    private static void saveLocalSync(
            @NonNull Context app, @NonNull ModelClass.Datum post, boolean pendingAdd) {
        if (post.getPostId() == null || post.getPostId() <= 0) return;
        final long postId = post.getPostId();
        post.setBookmarkStatus(1);
        synchronized (DISK) {
            ModelClass.Datum existing = readPostFileLocked(app, postId);
            ModelClass.Datum toWrite = mergePreferRich(existing, post);
            writePostFileLocked(app, toWrite);
            List<Long> index = loadIndexLocked(app);
            removeId(index, postId);
            index.add(0, postId);
            writeIndexLocked(app, index);
            if (pendingAdd) {
                queuePendingLocked(app, postId, "add");
            } else {
                removePendingLocked(app, postId, "add");
            }
        }
        prefetchImages(app, post);
    }

    /**
     * When server hydrates a slim post after list_bookmarks, keep longer description
     * / title / images from the already-saved offline copy if present.
     */
    @NonNull
    private static ModelClass.Datum mergePreferRich(
            @Nullable ModelClass.Datum existing, @NonNull ModelClass.Datum incoming) {
        if (existing == null) return incoming;
        if (TextUtils.isEmpty(incoming.getDescription())
                && !TextUtils.isEmpty(existing.getDescription())) {
            incoming.setDescription(existing.getDescription());
        }
        if (TextUtils.isEmpty(incoming.getTitle()) && !TextUtils.isEmpty(existing.getTitle())) {
            incoming.setTitle(existing.getTitle());
        }
        if (TextUtils.isEmpty(incoming.getThumbnail())
                && !TextUtils.isEmpty(existing.getThumbnail())) {
            incoming.setThumbnail(existing.getThumbnail());
        }
        if (TextUtils.isEmpty(incoming.getBackgroundImage())
                && !TextUtils.isEmpty(existing.getBackgroundImage())) {
            incoming.setBackgroundImage(existing.getBackgroundImage());
        }
        if ((incoming.getImages() == null || incoming.getImages().isEmpty())
                && existing.getImages() != null
                && !existing.getImages().isEmpty()) {
            incoming.setImages(existing.getImages());
        }
        if (TextUtils.isEmpty(incoming.getWriterName())
                && !TextUtils.isEmpty(existing.getWriterName())) {
            incoming.setWriterName(existing.getWriterName());
        }
        return incoming;
    }

    public static void removeLocal(
            @NonNull Context context, long postId, boolean pendingRemove) {
        if (postId <= 0) return;
        final Context app = context.getApplicationContext();
        IO.execute(() -> removeLocalSync(app, postId, pendingRemove));
    }

    private static void removeLocalSync(
            @NonNull Context app, long postId, boolean pendingRemove) {
        synchronized (DISK) {
            File f = postFile(app, postId);
            //noinspection ResultOfMethodCallIgnored
            f.delete();
            List<Long> index = loadIndexLocked(app);
            removeId(index, postId);
            writeIndexLocked(app, index);
            if (pendingRemove) {
                queuePendingLocked(app, postId, "remove");
            } else {
                removePendingLocked(app, postId, "remove");
                removePendingLocked(app, postId, "add");
            }
        }
    }

    public interface ToggleCallback {
        void onDone(boolean bookmarked, @Nullable String toastMsg);
    }

    /**
     * Toggle bookmark: offline-first write of full post JSON, then sync if online.
     * Callback runs on the main thread after disk index is updated so list+reader
     * see the save immediately. Bookmark presence is re-checked on the IO thread so
     * a stale main-thread read cannot delete the wrong state under concurrent saves.
     */
    public static void toggle(
            @NonNull Context context,
            @Nullable ModelClass.Datum post,
            @Nullable ToggleCallback callback) {
        if (post == null || post.getPostId() == null || post.getPostId() <= 0) {
            notifyToggle(callback, false, null);
            return;
        }
        final Context app = context.getApplicationContext();
        final long postId = post.getPostId();
        final boolean online = isNetworkAvailable(app);
        final ModelClass.Datum postRef = snapshot(post);
        if (postRef == null) {
            notifyToggle(callback, false, null);
            return;
        }

        IO.execute(
                () -> {
                    final boolean currently;
                    synchronized (DISK) {
                        currently = loadIndexLocked(app).contains(postId);
                    }
                    if (currently) {
                        postRef.setBookmarkStatus(0);
                        removeLocalSync(app, postId, !online);
                        notifyToggle(callback, false, "Removed");
                        if (online) {
                            syncRemove(app, postId, null);
                        }
                    } else {
                        postRef.setBookmarkStatus(1);
                        saveLocalSync(app, postRef, !online);
                        notifyToggle(callback, true, "Saved");
                        if (online) {
                            syncAdd(app, postRef, null);
                        }
                    }
                });
    }

    private static void notifyToggle(
            @Nullable ToggleCallback callback, boolean bookmarked, @Nullable String msg) {
        if (callback == null) return;
        if (Looper.myLooper() == Looper.getMainLooper()) {
            callback.onDone(bookmarked, msg);
        } else {
            MAIN.post(() -> callback.onDone(bookmarked, msg));
        }
    }

    public static void flushPending(@NonNull Context context) {
        final Context app = context.getApplicationContext();
        if (!isNetworkAvailable(app)) return;
        IO.execute(
                () -> {
                    List<PendingOp> ops;
                    synchronized (DISK) {
                        ops = loadPendingLocked(app);
                    }
                    if (ops.isEmpty()) return;
                    for (PendingOp op : new ArrayList<>(ops)) {
                        if (op == null || op.postId <= 0) continue;
                        if ("remove".equals(op.op)) {
                            syncRemove(
                                    app,
                                    op.postId,
                                    success -> {
                                        if (success) {
                                            IO.execute(
                                                    () -> {
                                                        synchronized (DISK) {
                                                            removePendingLocked(
                                                                    app, op.postId, "remove");
                                                        }
                                                    });
                                        }
                                    });
                        } else if ("add".equals(op.op)) {
                            ModelClass.Datum post = getLocalPost(app, op.postId);
                            if (post == null) {
                                synchronized (DISK) {
                                    removePendingLocked(app, op.postId, "add");
                                }
                                continue;
                            }
                            syncAdd(
                                    app,
                                    post,
                                    success -> {
                                        if (success) {
                                            IO.execute(
                                                    () -> {
                                                        synchronized (DISK) {
                                                            removePendingLocked(
                                                                    app, op.postId, "add");
                                                        }
                                                    });
                                        }
                                    });
                        }
                    }
                });
    }

    /**
     * Pull server list when online and merge into local offline store.
     * Merges are applied as one IO batch before {@code onDone} so reload never sees a
     * half-applied server sync and never rewrites local with a partial set.
     */
    public static void syncFromServer(
            @NonNull Context context, @Nullable Runnable onDone) {
        final Context app = context.getApplicationContext();
        if (!isNetworkAvailable(app)) {
            notifyMain(onDone);
            return;
        }
        flushPending(app);
        JsonObject body = buildEncryptedBody(app, 0, 50);
        if (body == null) {
            notifyMain(onDone);
            return;
        }
        APIService api = RestApiClient.getClient().create(APIService.class);
        api.list_bookmarks("v3", body)
                .enqueue(
                        new Callback<EncreptionRequest>() {
                            @Override
                            public void onResponse(
                                    Call<EncreptionRequest> call,
                                    Response<EncreptionRequest> response) {
                                final List<ModelClass.Datum> remote = new ArrayList<>();
                                try {
                                    EncreptionRequest enc = response.body();
                                    if (enc == null || enc.getData() == null) {
                                        notifyMain(onDone);
                                        return;
                                    }
                                    String plain = decrypt(enc.getData());
                                    JsonObject root =
                                            GSON.fromJson(plain, JsonObject.class);
                                    if (root.has("success")
                                            && !root.get("success").getAsBoolean()) {
                                        notifyMain(onDone);
                                        return;
                                    }
                                    JsonArray data = null;
                                    if (root.has("data") && root.get("data").isJsonArray()) {
                                        data = root.getAsJsonArray("data");
                                    }
                                    if (data != null) {
                                        for (JsonElement el : data) {
                                            if (el == null || !el.isJsonObject()) continue;
                                            ModelClass.Datum post =
                                                    GSON.fromJson(el, ModelClass.Datum.class);
                                            if (post != null
                                                    && post.getPostId() != null
                                                    && post.getPostId() > 0) {
                                                remote.add(post);
                                            }
                                        }
                                    }
                                } catch (Exception ignored) {
                                }
                                // Apply merge on IO thread then signal done (keeps full local set).
                                IO.execute(
                                        () -> {
                                            for (ModelClass.Datum post : remote) {
                                                saveLocalSync(app, post, false);
                                            }
                                            notifyMain(onDone);
                                        });
                            }

                            @Override
                            public void onFailure(
                                    Call<EncreptionRequest> call, Throwable t) {
                                notifyMain(onDone);
                            }
                        });
    }

    private static void notifyMain(@Nullable Runnable r) {
        if (r == null) return;
        if (Looper.myLooper() == Looper.getMainLooper()) {
            r.run();
        } else {
            MAIN.post(r);
        }
    }

    private interface SyncCb {
        void onResult(boolean success);
    }

    private static void syncAdd(
            @NonNull Context app,
            @NonNull ModelClass.Datum post,
            @Nullable SyncCb cb) {
        JsonObject body = buildEncryptedBody(app, post.getPostId(), 0);
        if (body == null) {
            if (cb != null) cb.onResult(false);
            return;
        }
        APIService api = RestApiClient.getClient().create(APIService.class);
        api.add_bookmark("v3", body)
                .enqueue(
                        new Callback<EncreptionRequest>() {
                            @Override
                            public void onResponse(
                                    Call<EncreptionRequest> call,
                                    Response<EncreptionRequest> response) {
                                boolean ok = false;
                                ModelClass.Datum hydrated = null;
                                try {
                                    EncreptionRequest enc = response.body();
                                    if (enc != null && enc.getData() != null) {
                                        String plain = decrypt(enc.getData());
                                        JsonObject root =
                                                GSON.fromJson(plain, JsonObject.class);
                                        ok =
                                                root.has("success")
                                                        && root.get("success").getAsBoolean();
                                        if (ok
                                                && root.has("post")
                                                && root.get("post").isJsonObject()) {
                                            hydrated =
                                                    GSON.fromJson(
                                                            root.get("post"),
                                                            ModelClass.Datum.class);
                                        }
                                    }
                                } catch (Exception ignored) {
                                }
                                if (hydrated != null
                                        && hydrated.getPostId() != null
                                        && hydrated.getPostId() > 0) {
                                    final ModelClass.Datum h = hydrated;
                                    IO.execute(() -> saveLocalSync(app, h, false));
                                }
                                if (cb != null) cb.onResult(ok);
                            }

                            @Override
                            public void onFailure(
                                    Call<EncreptionRequest> call, Throwable t) {
                                IO.execute(
                                        () -> {
                                            synchronized (DISK) {
                                                queuePendingLocked(app, post.getPostId(), "add");
                                            }
                                        });
                                if (cb != null) cb.onResult(false);
                            }
                        });
    }

    private static void syncRemove(
            @NonNull Context app, long postId, @Nullable SyncCb cb) {
        JsonObject body = buildEncryptedBody(app, postId, 0);
        if (body == null) {
            if (cb != null) cb.onResult(false);
            return;
        }
        APIService api = RestApiClient.getClient().create(APIService.class);
        api.remove_bookmark("v3", body)
                .enqueue(
                        new Callback<EncreptionRequest>() {
                            @Override
                            public void onResponse(
                                    Call<EncreptionRequest> call,
                                    Response<EncreptionRequest> response) {
                                boolean ok = false;
                                try {
                                    EncreptionRequest enc = response.body();
                                    if (enc != null && enc.getData() != null) {
                                        String plain = decrypt(enc.getData());
                                        JsonObject root =
                                                GSON.fromJson(plain, JsonObject.class);
                                        ok =
                                                root.has("success")
                                                        && root.get("success").getAsBoolean();
                                    }
                                } catch (Exception ignored) {
                                }
                                if (cb != null) cb.onResult(ok);
                            }

                            @Override
                            public void onFailure(
                                    Call<EncreptionRequest> call, Throwable t) {
                                IO.execute(
                                        () -> {
                                            synchronized (DISK) {
                                                queuePendingLocked(app, postId, "remove");
                                            }
                                        });
                                if (cb != null) cb.onResult(false);
                            }
                        });
    }

    @Nullable
    private static JsonObject buildEncryptedBody(
            @NonNull Context app, long postId, int limit) {
        try {
            int draftId =
                    SharedPrefsData.getInt(app, Constants.DraftID, Constants.PREF_NAME);
            if (draftId <= 0) return null;
            JsonObject payload = new JsonObject();
            payload.addProperty("draft_id", draftId);
            if (postId > 0) payload.addProperty("post_id", postId);
            int langId =
                    SharedPrefsData.getInt(app, Constants.LanguageID, Constants.PREF_NAME);
            if (langId > 0) payload.addProperty("language_id", langId);
            if (limit > 0) {
                payload.addProperty("skip", 0);
                payload.addProperty("limit", limit);
            }
            String encrypted = encrypt(payload.toString());
            EncritonRequestData wrap = new EncritonRequestData();
            wrap.setData(encrypted);
            return GSON.toJsonTree(wrap).getAsJsonObject();
        } catch (Exception e) {
            return null;
        }
    }

    private static void prefetchImages(
            @NonNull Context app, @Nullable ModelClass.Datum post) {
        if (post == null) return;
        try {
            if (!TextUtils.isEmpty(post.getThumbnail())) {
                Glide.with(app).downloadOnly().load(post.getThumbnail()).submit();
            }
            if (!TextUtils.isEmpty(post.getBackgroundImage())) {
                Glide.with(app).downloadOnly().load(post.getBackgroundImage()).submit();
            }
            if (post.getImages() != null) {
                for (String url : post.getImages()) {
                    if (!TextUtils.isEmpty(url)) {
                        Glide.with(app).downloadOnly().load(url).submit();
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Nullable
    private static ModelClass.Datum snapshot(@NonNull ModelClass.Datum post) {
        try {
            return GSON.fromJson(GSON.toJson(post), ModelClass.Datum.class);
        } catch (Exception e) {
            return post;
        }
    }

    private static void removeId(@NonNull List<Long> index, long postId) {
        // Never use List.remove(int): long would not box incorrectly, but be explicit
        // so mixed number types from older files cannot leave stale entries behind.
        Iterator<Long> it = index.iterator();
        while (it.hasNext()) {
            Long id = it.next();
            if (id != null && id == postId) {
                it.remove();
            }
        }
    }

    private static File dir(@NonNull Context context) {
        File d = new File(context.getFilesDir(), DIR_NAME);
        if (!d.exists()) {
            //noinspection ResultOfMethodCallIgnored
            d.mkdirs();
        }
        return d;
    }

    private static File postFile(@NonNull Context context, long postId) {
        return new File(dir(context), "post_" + postId + ".json");
    }

    private static File indexFile(@NonNull Context context) {
        return new File(dir(context), INDEX_FILE);
    }

    private static File pendingFile(@NonNull Context context) {
        return new File(dir(context), PENDING_FILE);
    }

    @NonNull
    private static List<Long> loadIndexLocked(@NonNull Context context) {
        File f = indexFile(context);
        if (!f.exists()) return new ArrayList<>();
        try (FileInputStream in = new FileInputStream(f);
                InputStreamReader reader =
                        new InputStreamReader(in, StandardCharsets.UTF_8)) {
            List<Long> list = GSON.fromJson(reader, LIST_LONG);
            if (list == null) return new ArrayList<>();
            // Normalize: drop nulls / invalid, de-dupe while preserving order.
            List<Long> normalized = new ArrayList<>(list.size());
            for (Long id : list) {
                if (id == null || id <= 0) continue;
                if (!normalized.contains(id)) {
                    normalized.add(id);
                }
            }
            return normalized;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static void writeIndexLocked(
            @NonNull Context context, @NonNull List<Long> index) {
        writeJsonAtomic(indexFile(context), index);
    }

    @Nullable
    private static ModelClass.Datum readPostFileLocked(
            @NonNull Context context, long postId) {
        File f = postFile(context, postId);
        if (!f.exists() || f.length() == 0) return null;
        try (FileInputStream in = new FileInputStream(f);
                InputStreamReader reader =
                        new InputStreamReader(in, StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, ModelClass.Datum.class);
        } catch (Exception e) {
            return null;
        }
    }

    private static void writePostFileLocked(
            @NonNull Context context, @NonNull ModelClass.Datum post) {
        writeJsonAtomic(postFile(context, post.getPostId()), post);
    }

    /**
     * Write JSON via a temp file then rename so concurrent readers never observe
     * a truncated target (FileOutputStream would zero the file on open).
     */
    private static void writeJsonAtomic(@NonNull File target, @NonNull Object data) {
        File parent = target.getParentFile();
        if (parent != null && !parent.exists()) {
            //noinspection ResultOfMethodCallIgnored
            parent.mkdirs();
        }
        File tmp = new File(parent, target.getName() + ".tmp");
        try (FileOutputStream out = new FileOutputStream(tmp);
                OutputStreamWriter writer =
                        new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
            GSON.toJson(data, writer);
            writer.flush();
            out.getFD().sync();
        } catch (Exception e) {
            //noinspection ResultOfMethodCallIgnored
            tmp.delete();
            return;
        }
        if (target.exists() && !target.delete()) {
            // Fall through — rename may still replace on some devices.
        }
        if (!tmp.renameTo(target)) {
            // Last resort: direct rewrite (better than losing the update entirely).
            try (FileOutputStream out = new FileOutputStream(target);
                    OutputStreamWriter writer =
                            new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
                writer.flush();
            } catch (Exception ignored) {
            }
            //noinspection ResultOfMethodCallIgnored
            tmp.delete();
        }
    }

    private static class PendingOp {
        long postId;
        String op;
        long ts;
    }

    @NonNull
    private static List<PendingOp> loadPendingLocked(@NonNull Context context) {
        File f = pendingFile(context);
        if (!f.exists()) return new ArrayList<>();
        try (FileInputStream in = new FileInputStream(f);
                InputStreamReader reader =
                        new InputStreamReader(in, StandardCharsets.UTF_8)) {
            List<PendingOp> list = GSON.fromJson(reader, PENDING_TYPE);
            return list != null ? new ArrayList<>(list) : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static void writePendingLocked(
            @NonNull Context context, @NonNull List<PendingOp> ops) {
        writeJsonAtomic(pendingFile(context), ops);
    }

    private static void queuePendingLocked(
            @NonNull Context context, long postId, @NonNull String op) {
        List<PendingOp> ops = loadPendingLocked(context);
        for (Iterator<PendingOp> it = ops.iterator(); it.hasNext(); ) {
            PendingOp o = it.next();
            if (o != null && o.postId == postId) it.remove();
        }
        PendingOp next = new PendingOp();
        next.postId = postId;
        next.op = op;
        next.ts = System.currentTimeMillis();
        ops.add(next);
        writePendingLocked(context, ops);
    }

    private static void removePendingLocked(
            @NonNull Context context, long postId, @NonNull String op) {
        List<PendingOp> ops = loadPendingLocked(context);
        boolean changed = false;
        for (Iterator<PendingOp> it = ops.iterator(); it.hasNext(); ) {
            PendingOp o = it.next();
            if (o != null && o.postId == postId && op.equals(o.op)) {
                it.remove();
                changed = true;
            }
        }
        if (changed) writePendingLocked(context, ops);
    }
}
