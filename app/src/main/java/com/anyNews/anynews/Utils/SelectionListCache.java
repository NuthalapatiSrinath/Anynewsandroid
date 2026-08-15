package com.anyNews.anynews.Utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.LanguageSelectionRequest;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.models.DistricsModel;
import com.anyNews.anynews.models.LanguageModel;
import com.anyNews.anynews.models.RegionModel;
import com.anyNews.anynews.models.StatesModel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

/**
 * Disk + memory cache for language / state / district / region pickers so
 * selection screens paint instantly on open (no full-screen Loading...).
 * Soft network refresh updates cache in the background.
 * <p>
 * Cache keys (under app cacheDir + memory map):
 * <ul>
 *   <li>{@code selection_languages.json} — languages list</li>
 *   <li>{@code selection_states_lang_{languageId}.json} — states for a language</li>
 *   <li>{@code selection_districts_lang_{languageId}_state_{stateId}.json}</li>
 *   <li>{@code selection_regions_lang_{languageId}.json}</li>
 * </ul>
 */
public final class SelectionListCache {

    private static final String TAG = "SelectionListCache";
    private static final Gson GSON = new Gson();

    /** Keep picker lists usable for long reopens; soft-refresh keeps them fresh. */
    public static final long TTL_MS = 30L * 24 * 60 * 60 * 1000L;

    private static final String KEY_LANGUAGES = "languages";
    private static final String FILE_LANGUAGES = "selection_languages.json";

    private static final ConcurrentHashMap<String, Snapshot> MEMORY = new ConcurrentHashMap<>();
    private static final AtomicBoolean prefetchInFlight = new AtomicBoolean(false);

    private SelectionListCache() {
    }

    public static final class Snapshot {
        public long savedAt;
        public int languageId;
        public int stateId;
        /** Full decrypted API model JSON (LanguageModel / StatesModel / etc.). */
        public String payloadJson;
    }

    // ── Languages ───────────────────────────────────────────────────────

    @Nullable
    public static LanguageModel loadLanguagesModel(Context context) {
        Snapshot snap = loadSnapshot(context, KEY_LANGUAGES, FILE_LANGUAGES, 0, 0);
        if (snap == null || TextUtils.isEmpty(snap.payloadJson)) return null;
        try {
            LanguageModel model = GSON.fromJson(snap.payloadJson, LanguageModel.class);
            if (model == null || model.getData() == null || model.getData().isEmpty()) return null;
            return model;
        } catch (Exception e) {
            Logone.w(e, TAG + " loadLanguages parse failed");
            return null;
        }
    }

    @Nullable
    public static List<LanguageModel.Datum> loadLanguages(Context context) {
        LanguageModel model = loadLanguagesModel(context);
        return model != null ? model.getData() : null;
    }

    public static void saveLanguages(Context context, LanguageModel model) {
        if (context == null || model == null
                || model.getData() == null || model.getData().isEmpty()) {
            return;
        }
        Snapshot snap = new Snapshot();
        snap.savedAt = System.currentTimeMillis();
        snap.payloadJson = GSON.toJson(model);
        persist(context, KEY_LANGUAGES, FILE_LANGUAGES, snap);
    }

    public static void saveLanguages(Context context, List<LanguageModel.Datum> list) {
        if (list == null || list.isEmpty()) return;
        LanguageModel model = new LanguageModel();
        model.setSuccess(true);
        model.setData(list);
        saveLanguages(context, model);
    }

    // ── States (keyed by languageId) ────────────────────────────────────

    @Nullable
    public static StatesModel loadStatesModel(Context context, int languageId) {
        if (languageId <= 0) return null;
        Snapshot snap = loadSnapshot(context, statesKey(languageId), statesFile(languageId), languageId, 0);
        if (snap == null || TextUtils.isEmpty(snap.payloadJson)) return null;
        try {
            StatesModel model = GSON.fromJson(snap.payloadJson, StatesModel.class);
            if (model == null || model.getData() == null || model.getData().isEmpty()) return null;
            return model;
        } catch (Exception e) {
            Logone.w(e, TAG + " loadStates parse failed");
            return null;
        }
    }

    @Nullable
    public static List<StatesModel.Datum> loadStates(Context context, int languageId) {
        StatesModel model = loadStatesModel(context, languageId);
        return model != null ? model.getData() : null;
    }

    @Nullable
    public static String loadStatesNextScreen(Context context, int languageId) {
        StatesModel model = loadStatesModel(context, languageId);
        return model != null ? model.getNextScreen() : null;
    }

    public static void saveStates(Context context, int languageId, StatesModel model) {
        if (context == null || languageId <= 0 || model == null
                || model.getData() == null || model.getData().isEmpty()) {
            return;
        }
        Snapshot snap = new Snapshot();
        snap.savedAt = System.currentTimeMillis();
        snap.languageId = languageId;
        snap.payloadJson = GSON.toJson(model);
        persist(context, statesKey(languageId), statesFile(languageId), snap);
        StateLandmarkPrefetcher.prefetch(context, model.getData(), null);
    }

    public static void saveStates(Context context, int languageId,
                                  List<StatesModel.Datum> list, @Nullable String nextScreen) {
        if (list == null || list.isEmpty()) return;
        StatesModel model = new StatesModel();
        model.setSuccess(true);
        model.setData(list);
        model.setNextScreen(nextScreen);
        saveStates(context, languageId, model);
    }

    // ── Districts (keyed by languageId + stateId) ───────────────────────

    @Nullable
    public static DistricsModel loadDistrictsModel(Context context, int languageId, int stateId) {
        if (languageId <= 0 || stateId <= 0) return null;
        Snapshot snap = loadSnapshot(context, districtsKey(languageId, stateId),
                districtsFile(languageId, stateId), languageId, stateId);
        if (snap == null || TextUtils.isEmpty(snap.payloadJson)) return null;
        try {
            DistricsModel model = GSON.fromJson(snap.payloadJson, DistricsModel.class);
            if (model == null || model.getData() == null || model.getData().isEmpty()) return null;
            return model;
        } catch (Exception e) {
            Logone.w(e, TAG + " loadDistricts parse failed");
            return null;
        }
    }

    @Nullable
    public static List<DistricsModel.Datum> loadDistricts(Context context, int languageId, int stateId) {
        DistricsModel model = loadDistrictsModel(context, languageId, stateId);
        return model != null ? model.getData() : null;
    }

    public static void saveDistricts(Context context, int languageId, int stateId, DistricsModel model) {
        if (context == null || languageId <= 0 || stateId <= 0 || model == null
                || model.getData() == null || model.getData().isEmpty()) {
            return;
        }
        Snapshot snap = new Snapshot();
        snap.savedAt = System.currentTimeMillis();
        snap.languageId = languageId;
        snap.stateId = stateId;
        snap.payloadJson = GSON.toJson(model);
        persist(context, districtsKey(languageId, stateId), districtsFile(languageId, stateId), snap);
    }

    public static void saveDistricts(Context context, int languageId, int stateId,
                                     List<DistricsModel.Datum> list) {
        if (list == null || list.isEmpty()) return;
        DistricsModel model = new DistricsModel();
        model.setSuccess(true);
        model.setData(list);
        saveDistricts(context, languageId, stateId, model);
    }

    // ── Regions / areas (keyed by languageId) ───────────────────────────

    @Nullable
    public static RegionModel loadRegionsModel(Context context, int languageId) {
        if (languageId <= 0) return null;
        Snapshot snap = loadSnapshot(context, regionsKey(languageId), regionsFile(languageId), languageId, 0);
        if (snap == null || TextUtils.isEmpty(snap.payloadJson)) return null;
        try {
            RegionModel model = GSON.fromJson(snap.payloadJson, RegionModel.class);
            if (model == null || model.getData() == null || model.getData().isEmpty()) return null;
            return model;
        } catch (Exception e) {
            Logone.w(e, TAG + " loadRegions parse failed");
            return null;
        }
    }

    @Nullable
    public static List<RegionModel.Datum> loadRegions(Context context, int languageId) {
        RegionModel model = loadRegionsModel(context, languageId);
        return model != null ? model.getData() : null;
    }

    public static void saveRegions(Context context, int languageId, RegionModel model) {
        if (context == null || languageId <= 0 || model == null
                || model.getData() == null || model.getData().isEmpty()) {
            return;
        }
        Snapshot snap = new Snapshot();
        snap.savedAt = System.currentTimeMillis();
        snap.languageId = languageId;
        snap.payloadJson = GSON.toJson(model);
        persist(context, regionsKey(languageId), regionsFile(languageId), snap);
    }

    public static boolean languagesUnchanged(Context context, LanguageModel fresh) {
        if (fresh == null || fresh.getData() == null || fresh.getData().isEmpty()) return false;
        Snapshot snap = loadSnapshot(context, KEY_LANGUAGES, FILE_LANGUAGES, 0, 0);
        if (snap == null || TextUtils.isEmpty(snap.payloadJson)) return false;
        return snap.payloadJson.equals(GSON.toJson(fresh));
    }

    public static boolean statesUnchanged(Context context, int languageId, StatesModel fresh) {
        if (fresh == null || fresh.getData() == null || fresh.getData().isEmpty() || languageId <= 0) {
            return false;
        }
        Snapshot snap = loadSnapshot(context, statesKey(languageId), statesFile(languageId), languageId, 0);
        if (snap == null || TextUtils.isEmpty(snap.payloadJson)) return false;
        return snap.payloadJson.equals(GSON.toJson(fresh));
    }

    public static boolean districtsUnchanged(Context context, int languageId, int stateId,
                                             DistricsModel fresh) {
        if (fresh == null || fresh.getData() == null || fresh.getData().isEmpty()
                || languageId <= 0 || stateId <= 0) {
            return false;
        }
        Snapshot snap = loadSnapshot(context, districtsKey(languageId, stateId),
                districtsFile(languageId, stateId), languageId, stateId);
        if (snap == null || TextUtils.isEmpty(snap.payloadJson)) return false;
        return snap.payloadJson.equals(GSON.toJson(fresh));
    }

    public static boolean regionsUnchanged(Context context, int languageId, RegionModel fresh) {
        if (fresh == null || fresh.getData() == null || fresh.getData().isEmpty() || languageId <= 0) {
            return false;
        }
        Snapshot snap = loadSnapshot(context, regionsKey(languageId), regionsFile(languageId), languageId, 0);
        if (snap == null || TextUtils.isEmpty(snap.payloadJson)) return false;
        return snap.payloadJson.equals(GSON.toJson(fresh));
    }

    /**
     * After splash/home: warm languages + current-language states (and districts if state set)
     * so profile language/location pickers open fully painted.
     */
    public static void prefetchInBackground(Context context) {
        if (context == null) return;
        if (!prefetchInFlight.compareAndSet(false, true)) return;
        final Context app = context.getApplicationContext();
        // Slight delay so we do not contend with home feed cold-open.
        new Handler(Looper.getMainLooper()).postDelayed(() ->
                new Thread(() -> {
                    try {
                        prefetchLanguagesSync(app);
                        int languageId = SharedPrefsData.getInt(app, Constants.LanguageID, Constants.PREF_NAME);
                        if (languageId > 0) {
                            prefetchStatesSync(app, languageId);
                            int stateId = SharedPrefsData.getInt(app, Constants.StateID, Constants.PREF_NAME);
                            if (stateId > 0) {
                                prefetchDistrictsSync(app, languageId, stateId);
                            }
                        }
                    } catch (Exception e) {
                        Logone.w(e, TAG + " prefetch failed");
                    } finally {
                        prefetchInFlight.set(false);
                    }
                }, "SelectionListCache-prefetch").start(), 1500L);
    }

    private static void prefetchLanguagesSync(Context context) {
        if (isFreshEnough(MEMORY.get(KEY_LANGUAGES)) && loadLanguages(context) != null) {
            return;
        }
        final Object lock = new Object();
        final AtomicBoolean done = new AtomicBoolean(false);
        Handler main = new Handler(Looper.getMainLooper());
        main.post(() -> {
            try {
                APIService api = RestApiClient.getClient().create(APIService.class);
                api.Languages_All_Languages("v3").enqueue(new Callback<EncreptionRequest>() {
                    @Override
                    public void onResponse(Call<EncreptionRequest> call,
                                           Response<EncreptionRequest> response) {
                        try {
                            if (response != null && response.isSuccessful()
                                    && response.body() != null
                                    && !TextUtils.isEmpty(response.body().getData())) {
                                LanguageModel model = GSON.fromJson(
                                        decrypt(response.body().getData()), LanguageModel.class);
                                if (model != null && Boolean.TRUE.equals(model.getSuccess())
                                        && model.getData() != null && !model.getData().isEmpty()) {
                                    saveLanguages(context, model);
                                }
                            }
                        } catch (Exception e) {
                            Logone.w(e, TAG + " prefetch languages parse");
                        }
                        signal(lock, done);
                    }

                    @Override
                    public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                        signal(lock, done);
                    }
                });
            } catch (Exception e) {
                signal(lock, done);
            }
        });
        await(lock, done, 12_000L);
    }

    private static void prefetchStatesSync(Context context, int languageId) {
        if (isFreshEnough(MEMORY.get(statesKey(languageId))) && loadStates(context, languageId) != null) {
            return;
        }
        final Object lock = new Object();
        final AtomicBoolean done = new AtomicBoolean(false);
        Handler main = new Handler(Looper.getMainLooper());
        main.post(() -> {
            try {
                JsonObject body = buildLangBody(context, languageId, 0);
                APIService api = RestApiClient.getClient().create(APIService.class);
                api.Get_states_requiest("v3", body).enqueue(new Callback<EncreptionRequest>() {
                    @Override
                    public void onResponse(Call<EncreptionRequest> call,
                                           Response<EncreptionRequest> response) {
                        try {
                            if (response != null && response.isSuccessful()
                                    && response.body() != null
                                    && !TextUtils.isEmpty(response.body().getData())) {
                                StatesModel model = GSON.fromJson(
                                        decrypt(response.body().getData()), StatesModel.class);
                                if (model != null && Boolean.TRUE.equals(model.getSuccess())
                                        && model.getData() != null && !model.getData().isEmpty()) {
                                    saveStates(context, languageId, model);
                                }
                            }
                        } catch (Exception e) {
                            Logone.w(e, TAG + " prefetch states parse");
                        }
                        signal(lock, done);
                    }

                    @Override
                    public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                        signal(lock, done);
                    }
                });
            } catch (Exception e) {
                signal(lock, done);
            }
        });
        await(lock, done, 12_000L);
    }

    private static void prefetchDistrictsSync(Context context, int languageId, int stateId) {
        if (isFreshEnough(MEMORY.get(districtsKey(languageId, stateId)))
                && loadDistricts(context, languageId, stateId) != null) {
            return;
        }
        final Object lock = new Object();
        final AtomicBoolean done = new AtomicBoolean(false);
        Handler main = new Handler(Looper.getMainLooper());
        main.post(() -> {
            try {
                JsonObject body = buildLangBody(context, languageId, stateId);
                APIService api = RestApiClient.getClient().create(APIService.class);
                api.Get_districts("v3", body).enqueue(new Callback<EncreptionRequest>() {
                    @Override
                    public void onResponse(Call<EncreptionRequest> call,
                                           Response<EncreptionRequest> response) {
                        try {
                            if (response != null && response.isSuccessful()
                                    && response.body() != null
                                    && !TextUtils.isEmpty(response.body().getData())) {
                                DistricsModel model = GSON.fromJson(
                                        decrypt(response.body().getData()), DistricsModel.class);
                                if (model != null && Boolean.TRUE.equals(model.getSuccess())
                                        && model.getData() != null && !model.getData().isEmpty()) {
                                    saveDistricts(context, languageId, stateId, model);
                                }
                            }
                        } catch (Exception e) {
                            Logone.w(e, TAG + " prefetch districts parse");
                        }
                        signal(lock, done);
                    }

                    @Override
                    public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                        signal(lock, done);
                    }
                });
            } catch (Exception e) {
                signal(lock, done);
            }
        });
        await(lock, done, 12_000L);
    }

    private static boolean isFreshEnough(@Nullable Snapshot snap) {
        return snap != null
                && (System.currentTimeMillis() - snap.savedAt) < 24L * 60 * 60 * 1000L;
    }

    private static JsonObject buildLangBody(Context context, int languageId, int stateId) {
        LanguageSelectionRequest requestModel = new LanguageSelectionRequest();
        requestModel.setDraftId(SharedPrefsData.getInt(context, Constants.DraftID, Constants.PREF_NAME));
        requestModel.setLanguageId(languageId);
        if (stateId > 0) {
            requestModel.setStateId(stateId);
        }
        String jsonString = GSON.toJson(requestModel);
        EncritonRequestData enc = new EncritonRequestData();
        enc.setData(encrypt(jsonString));
        return GSON.toJsonTree(enc).getAsJsonObject();
    }

    private static void signal(Object lock, AtomicBoolean done) {
        synchronized (lock) {
            done.set(true);
            lock.notifyAll();
        }
    }

    private static void await(Object lock, AtomicBoolean done, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        synchronized (lock) {
            while (!done.get()) {
                long left = deadline - System.currentTimeMillis();
                if (left <= 0) break;
                try {
                    lock.wait(left);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private static String statesKey(int languageId) {
        return "states_" + languageId;
    }

    private static String statesFile(int languageId) {
        return "selection_states_lang_" + languageId + ".json";
    }

    private static String districtsKey(int languageId, int stateId) {
        return "districts_" + languageId + "_" + stateId;
    }

    private static String districtsFile(int languageId, int stateId) {
        return "selection_districts_lang_" + languageId + "_state_" + stateId + ".json";
    }

    private static String regionsKey(int languageId) {
        return "regions_" + languageId;
    }

    private static String regionsFile(int languageId) {
        return "selection_regions_lang_" + languageId + ".json";
    }

    @Nullable
    private static Snapshot loadSnapshot(Context context, String memKey, String fileName,
                                         int expectLang, int expectState) {
        if (context == null) return null;
        Snapshot mem = MEMORY.get(memKey);
        if (isSnapshotUsable(mem, expectLang, expectState)) {
            return mem;
        }
        try {
            File file = new File(context.getApplicationContext().getCacheDir(), fileName);
            if (!file.exists() || file.length() < 8) return null;
            String json = readUtf8(file);
            if (TextUtils.isEmpty(json)) return null;
            Snapshot snap = GSON.fromJson(json, Snapshot.class);
            if (!isSnapshotUsable(snap, expectLang, expectState)) return null;
            MEMORY.put(memKey, snap);
            return snap;
        } catch (Exception e) {
            Logone.w(e, TAG + " loadSnapshot failed key=" + memKey);
            return null;
        }
    }

    private static boolean isSnapshotUsable(@Nullable Snapshot snap, int expectLang, int expectState) {
        if (snap == null || TextUtils.isEmpty(snap.payloadJson)) return false;
        if ((System.currentTimeMillis() - snap.savedAt) > TTL_MS) return false;
        if (expectLang > 0 && snap.languageId > 0 && snap.languageId != expectLang) return false;
        if (expectState > 0 && snap.stateId > 0 && snap.stateId != expectState) return false;
        return true;
    }

    private static void persist(Context context, String memKey, String fileName, Snapshot snap) {
        try {
            MEMORY.put(memKey, snap);
            File file = new File(context.getApplicationContext().getCacheDir(), fileName);
            writeUtf8(file, GSON.toJson(snap));
        } catch (Exception e) {
            Logone.w(e, TAG + " persist failed key=" + memKey);
        }
    }

    private static String readUtf8(File file) throws Exception {
        StringBuilder sb = new StringBuilder((int) Math.min(file.length(), 2_000_000));
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            char[] buf = new char[8192];
            int n;
            while ((n = br.read(buf)) >= 0) {
                sb.append(buf, 0, n);
            }
        }
        return sb.toString();
    }

    private static void writeUtf8(File file, String content) throws Exception {
        File tmp = new File(file.getAbsolutePath() + ".tmp");
        try (FileOutputStream fos = new FileOutputStream(tmp, false)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
            fos.flush();
        }
        if (file.exists() && !file.delete()) {
            // best-effort
        }
        if (!tmp.renameTo(file)) {
            try (FileOutputStream fos = new FileOutputStream(file, false)) {
                fos.write(content.getBytes(StandardCharsets.UTF_8));
            }
            //noinspection ResultOfMethodCallIgnored
            tmp.delete();
        }
    }
}
