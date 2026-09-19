package com.anyNews.anynews.Activities;

import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.ListAllArticlesRequest;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.ModelClass;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.Utils.StatusVideoMedia;
import com.anyNews.anynews.Utils.StatusVideosCache;
import com.anyNews.anynews.Utils.StatusVideosDaysConfig;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Language-filtered Status Videos hub — instant paint from disk cache, then one
 * network {@code status_videos=true} pack (Redis ~1h on server: full_video +
 * status_videos_only posts, last admin {@code status_videos_days} / default 5,
 * published_time &lt;= now). Falls back to a short Special crawl only when the
 * pack path returns nothing usable.
 */
public class StatusVideosActivity extends AppCompatActivity {

    private static final Pattern YOUTUBE_ID = Pattern.compile(
            "(?:youtu\\.be/|youtube(?:-nocookie)?\\.com/(?:watch\\?(?:.*&)?v=|embed/|v/|shorts/))([\\w-]{11})",
            Pattern.CASE_INSENSITIVE);

    /** Fallback Special crawl page size (service.js toLimit). */
    private static final int PAGE_SIZE = 8;
    /** Enough Special pages to cover ~5 days of mixed full_image/full_video. */
    private static final int MAX_PAGES = 24;
    /** Match server STATUS_VIDEOS_LIMIT — not a tiny cap. */
    private static final int MAX_VIDEOS = 30;

    private RecyclerView listView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private final List<ModelClass.Datum> items = new ArrayList<>();
    private final Set<Long> seenPostIds = new HashSet<>();
    private StatusAdapter adapter;
    private APIService api;

    private int pagesFetched;
    private long skip;
    private long lastPostId;
    private long actualSkip;
    private boolean loadingFinished;
    private boolean paintedFromCache;
    private boolean useStatusPackEndpoint = true;
    private Call<EncreptionRequest> inFlightCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_status_videos);

        SharedPrefsData.putBool(
                this, Constants.STATUS_VIDEOS_BADGE_SEEN, true, Constants.PREF_NAME);

        listView = findViewById(R.id.status_videos_list);
        progressBar = findViewById(R.id.status_videos_progress);
        emptyView = findViewById(R.id.status_videos_empty);
        findViewById(R.id.status_videos_back).setOnClickListener(v -> finish());

        adapter = new StatusAdapter();
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.setAdapter(adapter);
        listView.setItemAnimator(null);
        api = RestApiClient.getClient().create(APIService.class);

        // Disk/memory first paint — do not wait on network when warm.
        ArrayList<ModelClass.Datum> cached = StatusVideosCache.loadForInstantPaint(this);
        if (cached != null && !cached.isEmpty()) {
            applyItems(cached, /*fromCache*/ true);
            paintedFromCache = true;
            progressBar.setVisibility(View.GONE);
            showLog("StatusVideos cache paint n=" + cached.size()
                    + " warm=" + StatusVideosCache.isWarm(this));
        } else {
            progressBar.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }

        // Soft refresh (always when cold; soft when cache warm).
        startNetworkLoad();
    }

    @Override
    protected void onDestroy() {
        if (inFlightCall != null) {
            inFlightCall.cancel();
            inFlightCall = null;
        }
        super.onDestroy();
    }

    private void startNetworkLoad() {
        pagesFetched = 0;
        skip = 0L;
        lastPostId = 0L;
        actualSkip = 0L;
        loadingFinished = false;
        if (!paintedFromCache) {
            items.clear();
            seenPostIds.clear();
            adapter.notifyDataSetChanged();
        }
        if (useStatusPackEndpoint) {
            fetchStatusVideosPack();
        } else {
            fetchNextSpecialPage();
        }
    }

    private void applyItems(List<ModelClass.Datum> next, boolean fromCache) {
        items.clear();
        seenPostIds.clear();
        final long nowMs = System.currentTimeMillis();
        final long windowMs = StatusVideosDaysConfig.getWindowMs(this);
        if (next != null) {
            for (ModelClass.Datum d : next) {
                if (d == null || isAd(d)) continue;
                // Pack already curated (full_video + status_videos_only). Special
                // fallback / stale disk still require a playable video type.
                if (!fromCache && !useStatusPackEndpoint && !isVideoPost(d)) continue;
                if (fromCache && !isStatusVideosListItem(d)) continue;
                // Live + last admin days (drop schedules and older disk rows).
                if (!isPublishedInStatusWindow(d, nowMs, windowMs)) continue;
                Long id = d.getPostId();
                if (id != null && !seenPostIds.add(id)) continue;
                items.add(d);
            }
        }
        adapter.notifyDataSetChanged();
        boolean empty = items.isEmpty();
        emptyView.setVisibility(empty && !fromCache ? View.VISIBLE : View.GONE);
        listView.setVisibility(empty && !fromCache ? View.GONE : View.VISIBLE);
        if (empty && !fromCache) {
            emptyView.setText(R.string.no_status_videos_hint);
        }
    }

    /** Single-shot status_videos=true pack (server Redis ~1h). */
    private void fetchStatusVideosPack() {
        ListAllArticlesRequest requestModel = buildBaseRequest();
        requestModel.setSpecial_posts(false);
        requestModel.setStatus_videos(true);
        requestModel.setSkip(0L);
        requestModel.setLastPostId(0L);
        requestModel.setActualSkip(0L);

        enqueueRequest(requestModel, /*isPack*/ true);
    }

    /** Legacy multi-page Special filter when pack endpoint is unavailable. */
    private void fetchNextSpecialPage() {
        if (isFinishing() || loadingFinished) {
            return;
        }
        ListAllArticlesRequest requestModel = buildBaseRequest();
        requestModel.setSpecial_posts(true);
        requestModel.setStatus_videos(false);
        requestModel.setSkip(skip);
        requestModel.setLastPostId(lastPostId);
        requestModel.setActualSkip(actualSkip);
        enqueueRequest(requestModel, /*isPack*/ false);
    }

    private ListAllArticlesRequest buildBaseRequest() {
        ListAllArticlesRequest requestModel = new ListAllArticlesRequest();
        requestModel.setDraftId(SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
        requestModel.setCategoryId(0);
        requestModel.setLanguageId(SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME));
        requestModel.setPushPostId(0L);
        requestModel.setSharePostId(0L);
        requestModel.setApp_version_name("" + BuildConfig.VERSION_NAME);
        requestModel.setApp_version_number("" + BuildConfig.VERSION_CODE);
        requestModel.setSdk_version(Build.VERSION.SDK_INT);
        requestModel.setNotif_perm_allowed(1);
        requestModel.setUser_session_id(
                SharedPrefsData.getString(getApplicationContext(), Constants.USER_SESSION_ID, Constants.PREF_NAME));
        try {
            requestModel.setScreenHeight(getResources().getDisplayMetrics().heightPixels);
            requestModel.setScreenWidth(getResources().getDisplayMetrics().widthPixels);
        } catch (Exception ignored) {
        }
        return requestModel;
    }

    private void enqueueRequest(ListAllArticlesRequest requestModel, boolean isPack) {
        String jsonString = new Gson().toJson(requestModel);
        String encrypted = encrypt(jsonString);
        showLog("StatusVideos REQ pack=" + isPack + " page=" + pagesFetched + " " + jsonString);
        EncritonRequestData body = new EncritonRequestData();
        body.setData(encrypted);
        JsonObject otp = new Gson().toJsonTree(body).getAsJsonObject();

        if (inFlightCall != null) {
            inFlightCall.cancel();
        }
        inFlightCall = api.List_All_Articles("v3", otp);
        inFlightCall.enqueue(new Callback<EncreptionRequest>() {
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                if (call.isCanceled() || isFinishing()) {
                    return;
                }
                if (isPack) {
                    handlePackResponse(response);
                } else {
                    handleSpecialPageResponse(response);
                }
            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                if (call.isCanceled() || isFinishing()) {
                    return;
                }
                showLog("StatusVideos fail: " + (t != null ? t.getMessage() : "null"));
                if (isPack && items.isEmpty()) {
                    // Cold open + pack failed → Special crawl fallback.
                    useStatusPackEndpoint = false;
                    pagesFetched = 0;
                    skip = 0L;
                    lastPostId = 0L;
                    actualSkip = 0L;
                    loadingFinished = false;
                    fetchNextSpecialPage();
                    return;
                }
                finishLoading();
            }
        });
    }

    private void handlePackResponse(Response<EncreptionRequest> response) {
        try {
            if (response.body() != null && response.body().getData() != null) {
                String plain = decrypt(response.body().getData());
                showLog("StatusVideos PACK bytes=" + (plain != null ? plain.length() : 0));
                ModelClass parsed = new Gson().fromJson(plain, ModelClass.class);
                List<ModelClass.Datum> page = (parsed != null) ? parsed.getData() : null;
                ArrayList<ModelClass.Datum> videos = new ArrayList<>();
                if (page != null) {
                    for (ModelClass.Datum d : page) {
                        if (d == null || isAd(d)) continue;
                        // Trust server pack (full_video + status_videos_only exclusives).
                        if (!isStatusVideosListItem(d)) continue;
                        videos.add(d);
                    }
                }
                if (!videos.isEmpty()) {
                    applyItems(videos, false);
                    paintedFromCache = false;
                    StatusVideosCache.save(this, items);
                    finishLoading();
                    prefetchVisibleThumbs();
                    return;
                }
            }
        } catch (Exception e) {
            showLog("StatusVideos pack parse: " + e.getMessage());
        }
        // Empty pack or old server: crawl Special once path if no disk paint.
        if (items.isEmpty()) {
            showLog("StatusVideos pack empty → Special crawl");
            useStatusPackEndpoint = false;
            pagesFetched = 0;
            skip = 0L;
            lastPostId = 0L;
            actualSkip = 0L;
            loadingFinished = false;
            seenPostIds.clear();
            fetchNextSpecialPage();
            return;
        }
        // Keep warm paint; stop spinner.
        finishLoading();
    }

    private void handleSpecialPageResponse(Response<EncreptionRequest> response) {
        int pageVideoHits = 0;
        int pageAll = 0;
        boolean hasMore = false;
        // When replacing a cache paint, clear once on first page for network truth.
        if (pagesFetched == 0 && paintedFromCache) {
            items.clear();
            seenPostIds.clear();
            adapter.notifyDataSetChanged();
            paintedFromCache = false;
        }
        final int insertStart = items.size();
        try {
            if (response.body() != null && response.body().getData() != null) {
                String plain = decrypt(response.body().getData());
                showLog("StatusVideos RES page=" + pagesFetched
                        + " bytes=" + (plain != null ? plain.length() : 0));
                ModelClass parsed = new Gson().fromJson(plain, ModelClass.class);
                if (parsed != null && parsed.getData() != null) {
                    List<ModelClass.Datum> page = parsed.getData();
                    pageAll = page.size();
                    if (parsed.getSkip() != null) {
                        skip = parsed.getSkip();
                    } else {
                        skip = skip + PAGE_SIZE;
                    }
                    if (parsed.getLastPostId() != null) {
                        lastPostId = parsed.getLastPostId();
                    }
                    if (parsed.getActualSkip() != null) {
                        actualSkip = parsed.getActualSkip();
                    } else {
                        actualSkip = actualSkip + PAGE_SIZE;
                    }

                    final long nowMs = System.currentTimeMillis();
                    final long windowMs = StatusVideosDaysConfig.getWindowMs(StatusVideosActivity.this);
                    for (ModelClass.Datum d : page) {
                        if (d == null || isAd(d)) continue;
                        logPostFields(d);
                        if (!isVideoPost(d)) continue;
                        if (!isPublishedInStatusWindow(d, nowMs, windowMs)) continue;
                        Long id = d.getPostId();
                        if (id != null && !seenPostIds.add(id)) continue;
                        items.add(d);
                        pageVideoHits++;
                    }
                    hasMore = pageAll > 0;
                }
            }
        } catch (Exception e) {
            showLog("StatusVideos parse: " + e.getMessage());
        }

        pagesFetched++;
        final int inserted = items.size() - insertStart;
        showLog("StatusVideos page=" + pagesFetched
                + " all=" + pageAll
                + " videoHits=" + pageVideoHits
                + " totalVideos=" + items.size());

        if (inserted > 0 || (pagesFetched == 1 && !items.isEmpty())) {
            emptyView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            if (pagesFetched == 1) {
                adapter.notifyDataSetChanged();
            } else if (inserted > 0) {
                adapter.notifyItemRangeInserted(insertStart, inserted);
            }
        }

        boolean keepPaging = hasMore
                && pagesFetched < MAX_PAGES
                && items.size() < MAX_VIDEOS
                && pageAll > 0;
        if (keepPaging) {
            progressBar.setVisibility(View.VISIBLE);
            fetchNextSpecialPage();
            return;
        }

        if (!items.isEmpty()) {
            StatusVideosCache.save(this, items);
        }
        finishLoading();
    }

    private void finishLoading() {
        loadingFinished = true;
        inFlightCall = null;
        progressBar.setVisibility(View.GONE);
        boolean empty = items.isEmpty();
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        listView.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) {
            emptyView.setText(R.string.no_status_videos_hint);
        }
        showLog("StatusVideos done total=" + items.size() + " pages=" + pagesFetched);
    }

    private void prefetchVisibleThumbs() {
        try {
            int n = Math.min(items.size(), 6);
            for (int i = 0; i < n; i++) {
                String url = resolveThumbnailUrl(items.get(i));
                if (TextUtils.isEmpty(url)) continue;
                Glide.with(getApplicationContext())
                        .load(url)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .preload();
            }
        } catch (Exception ignored) {
        }
    }

    private static void logPostFields(ModelClass.Datum d) {
        try {
            showLog("StatusVideos post"
                    + " id=" + d.getPostId()
                    + " post_type=" + d.getPostType()
                    + " media_type=" + d.getMediaType()
                    + " video_url=" + summarizeUrl(d.getVideoUrl())
                    + " downloadUrl=" + summarizeUrl(d.getDownloadUrl())
                    + " videos=" + (d.getVideos() != null ? d.getVideos().size() : 0)
                    + " thumbnails=" + (d.getThumbnails() != null ? d.getThumbnails().size() : 0)
                    + " thumbnail=" + summarizeUrl(d.getThumbnail())
                    + " images0=" + summarizeUrl(firstImage(d))
                    + " bg=" + summarizeUrl(d.getBackgroundImage()));
        } catch (Exception ignored) {
        }
    }

    private static String summarizeUrl(String url) {
        if (TextUtils.isEmpty(url) || "null".equalsIgnoreCase(url.trim())) {
            return "empty";
        }
        String t = url.trim();
        return t.length() > 48 ? t.substring(0, 48) + "…" : t;
    }

    private static boolean isAd(ModelClass.Datum d) {
        try {
            if ("ad".equalsIgnoreCase(normalize(d.getPostType()))) return true;
            if (d.isLoad_ads()) return true;
            String ad = d.getAd_screen_type();
            return ad != null && !ad.isEmpty()
                    && !"null".equalsIgnoreCase(ad)
                    && !"0".equals(ad.trim());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Status Videos: {@code full_video} or any row with a playable video payload
     * (status_videos_only exclusives from the admin page).
     * Half layout is {@code normal_video}; galleries are excluded from Special crawl.
     */
    static boolean isVideoPost(ModelClass.Datum d) {
        if (d == null) return false;
        return "full_video".equals(normalize(d.getPostType()));
    }

    /** Pack / disk: full_video or any playable video (exclusive posts). */
    static boolean isStatusVideosListItem(ModelClass.Datum d) {
        if (d == null) return false;
        if (isVideoPost(d)) return true;
        return hasVideoPayload(d);
    }

    /**
     * Published and due (not scheduled) within the admin lookback window.
     * Matches server {@code statusVideosPack} published_time gate + lookback.
     */
    static boolean isPublishedInStatusWindow(
            ModelClass.Datum d, long nowMs, long windowMs) {
        if (d == null) return false;
        Long pub = d.getPublishedTime();
        if (pub == null || pub <= 0L) return false;
        if (pub > nowMs + 1000L) return false; // scheduled / future
        long win = windowMs > 0L ? windowMs : StatusVideosDaysConfig.DEFAULT_DAYS * 24L * 60L * 60L * 1000L;
        return pub >= nowMs - win;
    }

    /** @deprecated Prefer {@link #isPublishedInStatusWindow(ModelClass.Datum, long, long)}. */
    static boolean isPublishedInStatusWindow(ModelClass.Datum d, long nowMs) {
        return isPublishedInStatusWindow(
                d, nowMs, StatusVideosDaysConfig.DEFAULT_DAYS * 24L * 60L * 60L * 1000L);
    }

    static boolean hasVideoPayload(ModelClass.Datum d) {
        if (isPlayableVideoUrl(d.getVideoUrl())) return true;
        if (isPlayableVideoUrl(d.getDownloadUrl())) return true;
        if (galleryHasVideo(d.getVideos())) return true;
        if (youtubeIdFromUrl(d.getVideoUrl()) != null) return true;
        return false;
    }

    private static boolean galleryHasVideo(List<Object> list) {
        if (list == null || list.isEmpty()) return false;
        for (Object o : list) {
            if (o == null) continue;
            if (o instanceof String) {
                if (isPlayableVideoUrl((String) o) || youtubeIdFromUrl((String) o) != null) {
                    return true;
                }
            } else if (o instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) o;
                Object url = firstMapValue(map, "url", "video_url", "videoUrl", "src", "link", "file");
                if (url != null && (isPlayableVideoUrl(String.valueOf(url))
                        || youtubeIdFromUrl(String.valueOf(url)) != null)) {
                    return true;
                }
            } else {
                String s = String.valueOf(o);
                if (isPlayableVideoUrl(s) || youtubeIdFromUrl(s) != null) return true;
            }
        }
        return false;
    }

    private static Object firstMapValue(Map<?, ?> map, String... keys) {
        if (map == null) return null;
        for (String key : keys) {
            if (map.containsKey(key) && map.get(key) != null) {
                return map.get(key);
            }
            for (Map.Entry<?, ?> e : map.entrySet()) {
                if (e.getKey() != null && key.equalsIgnoreCase(String.valueOf(e.getKey()))
                        && e.getValue() != null) {
                    return e.getValue();
                }
            }
        }
        return null;
    }

    private static boolean isPlayableVideoUrl(String url) {
        if (TextUtils.isEmpty(url) || "null".equalsIgnoreCase(url.trim())) {
            return false;
        }
        String u = url.trim().toLowerCase(Locale.US);
        if (u.length() == 11 && !u.contains("/") && !u.contains(".") && u.matches("[\\w-]{11}")) {
            return true;
        }
        if (!(u.startsWith("http") || u.startsWith("//"))) {
            return false;
        }
        if (u.contains("youtube") || u.contains("youtu.be")) {
            return true;
        }
        if (StatusVideoMedia.isStreamOnlyUrl(url)) {
            return true;
        }
        if (u.contains(".jpg") || u.contains(".jpeg") || u.contains(".png")
                || u.contains(".webp") || u.contains(".gif") || u.contains(".bmp")
                || u.contains("hqdefault") || u.contains("mqdefault")
                || u.contains("maxresdefault") || u.contains("sddefault")
                || u.contains("img.youtube")) {
            return false;
        }
        if ((u.contains("thumb") || u.contains("poster") || u.contains("/image"))
                && !u.contains("/video")) {
            return false;
        }
        return true;
    }

    private static String normalize(String value) {
        return value != null ? value.trim().toLowerCase(Locale.US) : "";
    }

    static String resolveThumbnailUrl(ModelClass.Datum item) {
        return StatusVideoMedia.resolveThumbnailUrl(item);
    }

    private static String firstImage(ModelClass.Datum item) {
        if (item == null || item.getImages() == null || item.getImages().isEmpty()) return null;
        String s = item.getImages().get(0);
        return TextUtils.isEmpty(s) ? null : s;
    }

    private static String youtubeIdFromUrl(String url) {
        if (TextUtils.isEmpty(url)) return null;
        String trimmed = url.trim();
        if (trimmed.length() == 11 && !trimmed.contains("/") && !trimmed.contains(".")
                && trimmed.matches("[\\w-]{11}")) {
            return trimmed;
        }
        Matcher m = YOUTUBE_ID.matcher(trimmed);
        if (m.find()) {
            return m.group(1);
        }
        int vi = trimmed.toLowerCase(Locale.US).indexOf("/vi/");
        if (vi >= 0 && trimmed.length() >= vi + 15) {
            String id = trimmed.substring(vi + 4, vi + 15);
            if (id.matches("[\\w-]{11}")) return id;
        }
        return null;
    }

    private void openStatusPlayer(int index) {
        if (index < 0 || index >= items.size()) return;
        StatusVideoPlayerActivity.launch(this, items, index);
    }

    private class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.VH> {

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_status_video, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            ModelClass.Datum item = items.get(position);
            holder.title.setText(item.getTitle() != null ? item.getTitle() : "");
            String date = item.getPost_date_time();
            if (TextUtils.isEmpty(date) && item.getPublishedTime() != null) {
                date = String.valueOf(item.getPublishedTime());
            }
            holder.date.setText(date != null ? date : "");

            holder.thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
            String imageUrl = resolveThumbnailUrl(item);
            showLog("StatusVideos bind id=" + item.getPostId()
                    + " thumb=" + summarizeUrl(imageUrl)
                    + " raw=" + summarizeUrl(item.getThumbnail())
                    + " img0=" + summarizeUrl(firstImage(item))
                    + " video=" + summarizeUrl(item.getVideoUrl()));

            Glide.with(holder.thumb.getContext()).clear(holder.thumb);
            if (!TextUtils.isEmpty(imageUrl)) {
                Glide.with(holder.thumb.getContext())
                        .load(imageUrl)
                        .centerCrop()
                        .placeholder(R.color.image_placeholder)
                        .error(R.color.image_placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .dontAnimate()
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                    Target<Drawable> target, boolean isFirstResource) {
                                showLog("StatusVideos glide fail id=" + item.getPostId()
                                        + " url=" + summarizeUrl(String.valueOf(model))
                                        + " err=" + (e != null ? e.getMessage() : "null"));
                                String fallback = item.getThumbnail();
                                if (!TextUtils.isEmpty(fallback)
                                        && !fallback.equals(String.valueOf(model))) {
                                    Glide.with(holder.thumb.getContext())
                                            .load(StatusVideoMedia.ensureHttps(fallback.trim()))
                                            .centerCrop()
                                            .error(R.color.image_placeholder)
                                            .into(holder.thumb);
                                    return true;
                                }
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model,
                                    Target<Drawable> target, DataSource dataSource,
                                    boolean isFirstResource) {
                                return false;
                            }
                        })
                        .into(holder.thumb);
            } else {
                holder.thumb.setImageDrawable(null);
                holder.thumb.setBackgroundResource(R.color.image_placeholder);
            }
            final int pos = position;
            holder.itemView.setOnClickListener(v -> {
                int p = holder.getBindingAdapterPosition();
                if (p == RecyclerView.NO_POSITION) {
                    p = pos;
                }
                openStatusPlayer(p);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class VH extends RecyclerView.ViewHolder {
            final ImageView thumb;
            final TextView title;
            final TextView date;

            VH(View itemView) {
                super(itemView);
                thumb = itemView.findViewById(R.id.status_video_thumb);
                title = itemView.findViewById(R.id.status_video_title);
                date = itemView.findViewById(R.id.status_video_date);
            }
        }
    }
}
