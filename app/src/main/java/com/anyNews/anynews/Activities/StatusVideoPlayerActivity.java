package com.anyNews.anynews.Activities;

import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.anyNews.anynews.Pojo.DailogSendRespouse;
import com.anyNews.anynews.Pojo.DialogSendRequest;
import com.anyNews.anynews.Pojo.DilogeResest;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.GetReportIssueResponce;
import com.anyNews.anynews.Pojo.LikeDownloadRespouse;
import com.anyNews.anynews.Pojo.LikeSelectionRequest;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.AvatarImageHelper;
import com.anyNews.anynews.Utils.AutoNewsBylineHelper;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.ModelClass;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.Utils.StatusVideoMedia;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Full-screen vertical pager over the Status Videos list only.
 * Starts at the tapped index; swipe plays the next status item — never Home Special.
 * Right-side chrome mirrors Special full_video (like / comment / share / report).
 */
public class StatusVideoPlayerActivity extends AppCompatActivity {

    public static final String EXTRA_START_INDEX = "start_index";

    /** Handed from StatusVideosActivity (avoid large Intent payloads). Cleared on destroy. */
    @Nullable
    static ArrayList<ModelClass.Datum> sPlaylist;

    private RecyclerView pager;
    private LinearLayoutManager layoutManager;
    private PlayerAdapter adapter;
    private int activePosition = RecyclerView.NO_POSITION;
    private APIService api;

    public static void launch(android.app.Activity from, List<ModelClass.Datum> items, int index) {
        if (from == null || items == null || items.isEmpty()) return;
        int safe = Math.max(0, Math.min(index, items.size() - 1));
        sPlaylist = new ArrayList<>(items);
        Intent intent = new Intent(from, StatusVideoPlayerActivity.class);
        intent.putExtra(EXTRA_START_INDEX, safe);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_status_video_player);

        ArrayList<ModelClass.Datum> playlist = sPlaylist;
        if (playlist == null || playlist.isEmpty()) {
            finish();
            return;
        }

        api = RestApiClient.getClient().create(APIService.class);

        int start = getIntent().getIntExtra(EXTRA_START_INDEX, 0);
        if (start < 0 || start >= playlist.size()) {
            start = 0;
        }

        pager = findViewById(R.id.status_player_pager);
        findViewById(R.id.status_player_close).setOnClickListener(v -> finish());

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        pager.setLayoutManager(layoutManager);
        activePosition = start;
        adapter = new PlayerAdapter(playlist);
        pager.setAdapter(adapter);
        pager.setHasFixedSize(true);
        pager.setItemViewCacheSize(1);
        new PagerSnapHelper().attachToRecyclerView(pager);

        pager.scrollToPosition(start);
        // Prefer onBind-driven play; also schedule a retry if VH was not ready.
        final int startIndex = start;
        pager.post(() -> settleOn(startIndex));
        pager.postDelayed(() -> settleOn(startIndex), 120);

        pager.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                    // Keep neighbors muted while flinging; do not stop the active page yet.
                    if (activePosition >= 0) {
                        pauseAllExcept(activePosition);
                    }
                    return;
                }
                int pos = layoutManager.findFirstCompletelyVisibleItemPosition();
                if (pos == RecyclerView.NO_POSITION) {
                    pos = layoutManager.findFirstVisibleItemPosition();
                }
                if (pos != RecyclerView.NO_POSITION) {
                    settleOn(pos);
                }
            }
        });
    }

    /**
     * Single-player discipline: only {@code position} may play/sound.
     * Always pause+mute every other attached page first (VH recycle can leave
     * the previous ExoPlayer/YouTube running when pauseAt(prev) misses).
     */
    private void settleOn(int position) {
        if (isFinishing() || position < 0) return;
        activePosition = position;
        pauseAllExcept(position);
        playAt(position);
        prefetchNeighbor(position - 1);
        prefetchNeighbor(position + 1);
    }

    private void playAt(int position) {
        if (position < 0 || position != activePosition) return;
        RecyclerView.ViewHolder vh = pager.findViewHolderForAdapterPosition(position);
        if (vh instanceof PlayerAdapter.VH) {
            ((PlayerAdapter.VH) vh).play();
        }
    }

    private void pauseAt(int position) {
        if (position < 0) return;
        RecyclerView.ViewHolder vh = pager.findViewHolderForAdapterPosition(position);
        if (vh instanceof PlayerAdapter.VH) {
            ((PlayerAdapter.VH) vh).pauseAndMute();
        }
    }

    /** Pause+mute every attached child except {@code keep} (NO_POSITION = all). */
    private void pauseAllExcept(int keep) {
        if (pager == null) return;
        for (int i = 0; i < pager.getChildCount(); i++) {
            View child = pager.getChildAt(i);
            if (child == null) continue;
            RecyclerView.ViewHolder vh = pager.getChildViewHolder(child);
            if (!(vh instanceof PlayerAdapter.VH)) continue;
            int pos = vh.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION || pos != keep) {
                ((PlayerAdapter.VH) vh).pauseAndMute();
            }
        }
        // Adapter walk covers off-screen holdovers still bound with a player.
        if (adapter != null) {
            for (int i = 0; i < adapter.getItemCount(); i++) {
                if (i == keep) continue;
                RecyclerView.ViewHolder vh = pager.findViewHolderForAdapterPosition(i);
                if (vh instanceof PlayerAdapter.VH) {
                    ((PlayerAdapter.VH) vh).pauseAndMute();
                }
            }
        }
    }

    /** Quiet-prepare adjacent page for snappy settle (never playWhenReady). */
    private void prefetchNeighbor(int position) {
        if (position < 0 || adapter == null || position >= adapter.getItemCount()) return;
        if (position == activePosition) return;
        RecyclerView.ViewHolder vh = pager.findViewHolderForAdapterPosition(position);
        if (vh instanceof PlayerAdapter.VH) {
            ((PlayerAdapter.VH) vh).prepareQuiet();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseAllExcept(RecyclerView.NO_POSITION);
    }

    @Override
    protected void onStop() {
        pauseAllExcept(RecyclerView.NO_POSITION);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (activePosition >= 0) {
            settleOn(activePosition);
        }
    }

    @Override
    protected void onDestroy() {
        pauseAllExcept(RecyclerView.NO_POSITION);
        if (adapter != null) {
            adapter.releaseAll();
        }
        sPlaylist = null;
        super.onDestroy();
    }

    private class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.VH> {
        private final List<ModelClass.Datum> data;

        PlayerAdapter(List<ModelClass.Datum> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_status_video_page, parent, false);
            v.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            holder.bind(data.get(position), position, data.size());
            // Auto-start only the settled page; neighbors stay muted/paused.
            if (position == activePosition) {
                holder.itemView.post(() -> {
                    if (holder.getBindingAdapterPosition() == activePosition) {
                        pauseAllExcept(activePosition);
                        holder.play();
                    } else {
                        holder.pauseAndMute();
                    }
                });
            } else {
                holder.pauseAndMute();
            }
        }

        @Override
        public void onViewAttachedToWindow(@NonNull VH holder) {
            super.onViewAttachedToWindow(holder);
            int pos = holder.getBindingAdapterPosition();
            if (pos != activePosition) {
                holder.pauseAndMute();
            }
        }

        @Override
        public void onViewDetachedFromWindow(@NonNull VH holder) {
            holder.pauseAndMute();
            super.onViewDetachedFromWindow(holder);
        }

        @Override
        public void onViewRecycled(@NonNull VH holder) {
            holder.release();
            super.onViewRecycled(holder);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        void releaseAll() {
            for (int i = 0; i < getItemCount(); i++) {
                RecyclerView.ViewHolder vh = pager.findViewHolderForAdapterPosition(i);
                if (vh instanceof VH) {
                    ((VH) vh).release();
                }
            }
        }

        class VH extends RecyclerView.ViewHolder {
            final ImageView poster;
            final PlayerView exoView;
            final YouTubePlayerView ytView;
            final ProgressBar progress;
            final TextView title;
            final TextView writerName;
            final TextView hint;
            final LinearLayout sideMenu;
            final CircleImageView writerPic;
            final ImageView likeIcon;
            final TextView likeCount;
            final TextView commentCount;
            final TextView shareCount;
            final View likeBtn;
            final View commentBtn;
            final View shareBtn;
            final View reportBtn;

            @Nullable ExoPlayer exo;
            @Nullable YouTubePlayer ytPlayer;
            @Nullable String ytIdPending;
            boolean ytInitialized;
            @Nullable ModelClass.Datum item;
            boolean wantPlay;
            @Nullable String lastStreamUrl;

            VH(View itemView) {
                super(itemView);
                poster = itemView.findViewById(R.id.status_page_poster);
                exoView = itemView.findViewById(R.id.status_page_exo);
                ytView = itemView.findViewById(R.id.status_page_youtube);
                progress = itemView.findViewById(R.id.status_page_progress);
                title = itemView.findViewById(R.id.status_page_title);
                writerName = itemView.findViewById(R.id.status_page_writer);
                hint = itemView.findViewById(R.id.status_page_hint);
                sideMenu = itemView.findViewById(R.id.status_page_side_menu);
                writerPic = itemView.findViewById(R.id.status_page_writer_pic);
                likeIcon = itemView.findViewById(R.id.status_page_like_icon);
                likeCount = itemView.findViewById(R.id.status_page_like_count);
                commentCount = itemView.findViewById(R.id.status_page_comment_count);
                shareCount = itemView.findViewById(R.id.status_page_share_count);
                likeBtn = itemView.findViewById(R.id.status_page_like);
                commentBtn = itemView.findViewById(R.id.status_page_comment);
                shareBtn = itemView.findViewById(R.id.status_page_share);
                reportBtn = itemView.findViewById(R.id.status_page_report);
            }

            void bind(ModelClass.Datum datum, int position, int total) {
                // Soft release players only when switching different posts.
                boolean sameItem = item != null && datum != null
                        && item.getPostId() != null
                        && item.getPostId().equals(datum.getPostId());
                if (!sameItem) {
                    releasePlayersOnly();
                }
                item = datum;
                wantPlay = false;
                lastStreamUrl = null;
                title.setText(datum.getTitle() != null ? datum.getTitle() : "");
                writerName.setText(
                        AutoNewsBylineHelper.displayWriterName(
                                datum.getWriterName() != null ? datum.getWriterName() : ""));
                hint.setText((position + 1) + " / " + total + "  ·  swipe for next");
                bindEngagement(datum);
                wireActions(datum);

                String thumb = StatusVideoMedia.resolveThumbnailUrl(datum);
                poster.setVisibility(View.VISIBLE);
                poster.setScaleType(ImageView.ScaleType.CENTER_CROP);
                if (!TextUtils.isEmpty(thumb)) {
                    Glide.with(poster.getContext())
                            .load(thumb)
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .placeholder(R.color.image_placeholder)
                            .error(R.color.image_placeholder)
                            .into(poster);
                } else {
                    Glide.with(poster.getContext()).clear(poster);
                    poster.setImageDrawable(null);
                    poster.setBackgroundResource(R.color.image_placeholder);
                }

                if (!sameItem) {
                    exoView.setVisibility(View.GONE);
                    ytView.setVisibility(View.GONE);
                }
                progress.setVisibility(View.GONE);
            }

            private void bindEngagement(ModelClass.Datum datum) {
                if (datum == null) return;
                int likes = datum.getLikes() != null ? datum.getLikes() : 0;
                int comments = datum.getComments() != null ? datum.getComments() : 0;
                int shares = datum.getShares() != null ? datum.getShares() : 0;
                likeCount.setText(formatCount(likes));
                commentCount.setText(formatCount(comments));
                shareCount.setText(formatCount(shares));
                int status = datum.getLikeStatus() != null ? datum.getLikeStatus() : 0;
                likeIcon.setImageResource(status == 1
                        ? R.drawable.ic_like_half_selected1
                        : R.drawable.ic_like_half1);
            }

            void refreshEngagement() {
                if (item != null) {
                    bindEngagement(item);
                }
            }

            private void wireActions(ModelClass.Datum datum) {
                AvatarImageHelper.loadPicasso(writerPic, datum.getWriterProfile());
                likeBtn.setOnClickListener(v -> toggleLike(datum));
                shareBtn.setOnClickListener(v -> sharePost(datum));
                reportBtn.setOnClickListener(v -> openReportDialog(datum));
                commentBtn.setOnClickListener(v -> openCommentsForPost(datum));
                writerPic.setOnClickListener(v -> {
                    if (datum.getWriterId() == null) return;
                    Intent intent = new Intent(StatusVideoPlayerActivity.this, WriterProfileActivity.class);
                    intent.putExtra("EXTRA_SESSION_ID", datum.getWriterId().toString());
                    startActivity(intent);
                });
            }

            private boolean isSettledActive() {
                int pos = getBindingAdapterPosition();
                return pos != RecyclerView.NO_POSITION && pos == activePosition;
            }

            void play() {
                if (item == null || isFinishing()) return;
                // Guard: never start audio unless we are the settled/current page.
                if (!isSettledActive()) {
                    pauseAndMute();
                    return;
                }
                wantPlay = true;

                // Prefer YouTube only when media_type says so or URL is youtu-ish.
                String ytId = StatusVideoMedia.resolveYoutubeId(item);
                String stream = StatusVideoMedia.resolveStreamUrl(item);
                // Flipper fallback: raw video_url if helpers return empty and not youtube-ish.
                if (TextUtils.isEmpty(stream) && TextUtils.isEmpty(ytId)
                        && !TextUtils.isEmpty(item.getVideoUrl())) {
                    String raw = item.getVideoUrl().trim();
                    if (raw.startsWith("http") || raw.startsWith("//")) {
                        stream = StatusVideoMedia.ensureHttps(raw);
                    } else if (raw.length() == 11 && raw.matches("[\\w-]{11}")) {
                        ytId = raw;
                    }
                }

                String media = item.getMediaType() != null ? item.getMediaType().trim() : "";
                showLog("StatusPlayer play id=" + item.getPostId()
                        + " pos=" + getBindingAdapterPosition()
                        + " media_type=" + media
                        + " post_type=" + item.getPostType()
                        + " yt=" + ytId
                        + " stream=" + StatusVideoMedia.summarize(stream)
                        + " video_url=" + StatusVideoMedia.summarize(item.getVideoUrl())
                        + " download=" + StatusVideoMedia.summarize(item.getDownloadUrl()));

                // media_type 5/3 → Exo path first (even if a spurious YT id was guessed).
                boolean preferExo = "3".equals(media) || "5".equals(media);
                boolean preferYt = "4".equals(media) || "6".equals(media);

                if (preferExo && !TextUtils.isEmpty(stream)) {
                    startExo(stream, /*play*/ true);
                } else if (preferYt && !TextUtils.isEmpty(ytId)) {
                    startYoutube(ytId, /*play*/ true);
                } else if (!TextUtils.isEmpty(ytId) && !preferExo) {
                    startYoutube(ytId, /*play*/ true);
                } else if (!TextUtils.isEmpty(stream)) {
                    startExo(stream, /*play*/ true);
                } else if (!TextUtils.isEmpty(ytId)) {
                    startYoutube(ytId, /*play*/ true);
                } else {
                    progress.setVisibility(View.GONE);
                    poster.setVisibility(View.VISIBLE);
                    showLog("StatusPlayer NO media for id=" + item.getPostId());
                    Toast.makeText(StatusVideoPlayerActivity.this,
                            "Video unavailable", Toast.LENGTH_SHORT).show();
                }
            }

            /** Mute+pause; used for neighbors and offscreen pages. */
            void pauseAndMute() {
                wantPlay = false;
                progress.setVisibility(View.GONE);
                if (exo != null) {
                    try {
                        exo.setPlayWhenReady(false);
                        exo.setVolume(0f);
                        exo.pause();
                    } catch (Exception ignored) {
                    }
                }
                if (ytPlayer != null) {
                    try {
                        ytPlayer.pause();
                        ytPlayer.mute();
                    } catch (Exception ignored) {
                    }
                }
            }

            /** Prepare stream without sound for adjacent pages. */
            void prepareQuiet() {
                if (item == null || isSettledActive()) return;
                wantPlay = false;
                String stream = StatusVideoMedia.resolveStreamUrl(item);
                if (TextUtils.isEmpty(stream) && !TextUtils.isEmpty(item.getVideoUrl())) {
                    String raw = item.getVideoUrl().trim();
                    String ytCheck = StatusVideoMedia.resolveYoutubeId(item);
                    if ((raw.startsWith("http") || raw.startsWith("//")) && ytCheck == null) {
                        stream = StatusVideoMedia.ensureHttps(raw);
                    }
                }
                String media = item.getMediaType() != null ? item.getMediaType().trim() : "";
                boolean preferExo = "3".equals(media) || "5".equals(media);
                String ytId = StatusVideoMedia.resolveYoutubeId(item);
                if ((preferExo || TextUtils.isEmpty(ytId)) && !TextUtils.isEmpty(stream)) {
                    startExo(stream, /*play*/ false);
                }
                // YouTube iframe init is heavy — skip quiet prepare for YT neighbors.
            }

            void release() {
                wantPlay = false;
                releasePlayersOnly();
                try {
                    // YouTube view must be re-inited after full release; clear init flags.
                    if (ytInitialized) {
                        ytView.release();
                    }
                } catch (Exception ignored) {
                }
                ytInitialized = false;
                ytPlayer = null;
                ytIdPending = null;
                exoView.setVisibility(View.GONE);
                ytView.setVisibility(View.GONE);
                progress.setVisibility(View.GONE);
            }

            private void releasePlayersOnly() {
                if (exo != null) {
                    try {
                        exo.setPlayWhenReady(false);
                        exo.setVolume(0f);
                        exoView.setPlayer(null);
                        exo.release();
                    } catch (Exception ignored) {
                    }
                    exo = null;
                }
                lastStreamUrl = null;
                if (ytPlayer != null) {
                    try {
                        ytPlayer.pause();
                        ytPlayer.mute();
                    } catch (Exception ignored) {
                    }
                }
            }

            private void startExo(String url, boolean play) {
                if (play && !isSettledActive()) {
                    pauseAndMute();
                    return;
                }
                String safeUrl = StatusVideoMedia.ensureHttps(url);
                // Stop YouTube surface if we were on a YT page.
                if (ytPlayer != null) {
                    try {
                        ytPlayer.pause();
                        ytPlayer.mute();
                    } catch (Exception ignored) {
                    }
                }
                ytView.setVisibility(View.GONE);

                if (play) {
                    progress.setVisibility(View.VISIBLE);
                }
                exoView.setVisibility(View.VISIBLE);

                if (exo == null) {
                    exo = new ExoPlayer.Builder(itemView.getContext())
                            .setSeekBackIncrementMs(5000)
                            .setSeekForwardIncrementMs(5000)
                            .build();
                    exoView.setPlayer(exo);
                    // Match Flipper full_video resize.
                    exoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                    exo.addListener(new Player.Listener() {
                        @Override
                        public void onPlaybackStateChanged(int state) {
                            if (!wantPlay || !isSettledActive()) {
                                if (exo != null) {
                                    exo.setPlayWhenReady(false);
                                    exo.setVolume(0f);
                                }
                                return;
                            }
                            if (state == Player.STATE_READY) {
                                progress.setVisibility(View.GONE);
                                poster.setVisibility(View.GONE);
                                showLog("StatusPlayer exo READY");
                            } else if (state == Player.STATE_BUFFERING) {
                                progress.setVisibility(View.VISIBLE);
                            } else if (state == Player.STATE_ENDED) {
                                progress.setVisibility(View.GONE);
                            } else if (state == Player.STATE_IDLE) {
                                progress.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onIsPlayingChanged(boolean isPlaying) {
                            // Second safeguard: if we are no longer the active page, stop.
                            if (isPlaying && (!wantPlay || !isSettledActive()) && exo != null) {
                                exo.setPlayWhenReady(false);
                                exo.setVolume(0f);
                                exo.pause();
                            }
                        }

                        @Override
                        public void onPlayerError(@NonNull PlaybackException error) {
                            showLog("StatusPlayer exo ERROR: " + error.getErrorCodeName()
                                    + " " + error.getMessage()
                                    + " url=" + StatusVideoMedia.summarize(safeUrl));
                            progress.setVisibility(View.GONE);
                            poster.setVisibility(View.VISIBLE);
                            if (wantPlay && isSettledActive()) {
                                Toast.makeText(StatusVideoPlayerActivity.this,
                                        "Couldn't play video", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                if (!safeUrl.equals(lastStreamUrl)) {
                    lastStreamUrl = safeUrl;
                    MediaItem mediaItem = MediaItem.fromUri(Uri.parse(safeUrl));
                    exo.setMediaItem(mediaItem);
                    exo.prepare();
                }
                if (play && wantPlay && isSettledActive()) {
                    exo.setVolume(1f);
                    exo.setPlayWhenReady(true);
                } else {
                    exo.setVolume(0f);
                    exo.setPlayWhenReady(false);
                }
                exo.setRepeatMode(Player.REPEAT_MODE_ONE);
            }

            private void startYoutube(String videoId, boolean play) {
                if (play && !isSettledActive()) {
                    pauseAndMute();
                    return;
                }
                // Stop Exo before showing YouTube (mirrors flip view type switch).
                if (exo != null) {
                    try {
                        exo.setPlayWhenReady(false);
                        exo.setVolume(0f);
                        exoView.setPlayer(null);
                        exo.release();
                    } catch (Exception ignored) {
                    }
                    exo = null;
                    lastStreamUrl = null;
                }
                exoView.setVisibility(View.GONE);
                if (play) {
                    progress.setVisibility(View.VISIBLE);
                }
                ytView.setVisibility(View.VISIBLE);
                ytIdPending = videoId;
                if (ytInitialized && ytPlayer != null) {
                    try {
                        if (play && wantPlay && isSettledActive()) {
                            ytPlayer.unMute();
                            ytPlayer.loadVideo(videoId, 0f);
                        } else {
                            ytPlayer.cueVideo(videoId, 0f);
                            ytPlayer.mute();
                            ytPlayer.pause();
                        }
                        progress.setVisibility(View.GONE);
                        if (play) {
                            poster.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        showLog("StatusPlayer yt reload: " + e.getMessage());
                        progress.setVisibility(View.GONE);
                    }
                    return;
                }
                if (ytInitialized) {
                    // Stuck after release without player — re-init by resetting flag.
                    ytInitialized = false;
                }
                ytInitialized = true;
                Lifecycle lifecycle = getLifecycle();
                try {
                    lifecycle.addObserver(ytView);
                } catch (Exception ignored) {
                }
                AbstractYouTubePlayerListener listener = new AbstractYouTubePlayerListener() {
                    @Override
                    public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                        ytPlayer = youTubePlayer;
                        if (ytIdPending == null) return;
                        // Async ready can race past settle — only load when still active + wantPlay.
                        if (wantPlay && isSettledActive()) {
                            try {
                                youTubePlayer.unMute();
                                youTubePlayer.loadVideo(ytIdPending, 0f);
                                progress.setVisibility(View.GONE);
                                poster.setVisibility(View.GONE);
                                showLog("StatusPlayer yt READY id=" + ytIdPending);
                            } catch (Exception ignored) {
                            }
                        } else {
                            try {
                                youTubePlayer.cueVideo(ytIdPending, 0f);
                                youTubePlayer.mute();
                                youTubePlayer.pause();
                            } catch (Exception ignored) {
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull YouTubePlayer youTubePlayer,
                            @NonNull com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerError error) {
                        showLog("StatusPlayer yt ERROR: " + error);
                        progress.setVisibility(View.GONE);
                        poster.setVisibility(View.VISIBLE);
                    }
                };
                try {
                    IFramePlayerOptions options = new IFramePlayerOptions.Builder(itemView.getContext())
                            .controls(1)
                            .build();
                    ytView.initialize(listener, true, options);
                } catch (Exception e) {
                    showLog("StatusPlayer yt init fail: " + e.getMessage());
                    progress.setVisibility(View.GONE);
                    ytInitialized = false;
                }
            }
        }
    }

    private static String formatCount(int c) {
        if (c >= 1000) {
            float v = c / 1000f;
            return (v >= 10 || v == Math.floor(v))
                    ? String.format(Locale.US, "%.0fK", v)
                    : String.format(Locale.US, "%.1fK", v);
        }
        return String.valueOf(c);
    }

    private void toggleLike(ModelClass.Datum item) {
        if (item == null || item.getPostId() == null) return;
        int oldStatus = item.getLikeStatus() != null ? item.getLikeStatus() : 0;
        int likes = item.getLikes() != null ? item.getLikes() : 0;
        int newStatus;
        if (oldStatus == 1) {
            newStatus = 0;
            likes = Math.max(0, likes - 1);
        } else {
            newStatus = 1;
            likes++;
        }
        item.setLikeStatus(newStatus);
        item.setLikes(likes);
        notifyActiveEngagement();

        LikeSelectionRequest requestModel = new LikeSelectionRequest();
        requestModel.setDraft_id((long) SharedPrefsData.getInt(
                getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
        requestModel.setPost_id(item.getPostId());
        requestModel.setSub_post_id(item.getSubPostId() != null ? item.getSubPostId() : 0L);
        requestModel.setType(newStatus);
        String jsonString = new Gson().toJson(requestModel);
        EncritonRequestData body = new EncritonRequestData();
        body.setData(encrypt(jsonString));
        JsonObject otp = new Gson().toJsonTree(body).getAsJsonObject();
        showLog("StatusPlayer like post=" + item.getPostId() + " type=" + newStatus);
        api.update_like("v3", otp).enqueue(new Callback<EncreptionRequest>() {
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                try {
                    EncreptionRequest body = response.body();
                    if (body == null || body.getData() == null) return;
                    LikeDownloadRespouse likeResp = new Gson().fromJson(
                            decrypt(body.getData()), LikeDownloadRespouse.class);
                    if (likeResp != null && Boolean.TRUE.equals(likeResp.getSuccess())
                            && likeResp.getLikes() != null) {
                        item.setLikes(likeResp.getLikes());
                        notifyActiveEngagement();
                    }
                } catch (Exception e) {
                    showLog("StatusPlayer like parse: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                showLog("StatusPlayer like fail: " + (t != null ? t.getMessage() : "null"));
            }
        });
    }

    private void sharePost(ModelClass.Datum item) {
        if (item == null) return;
        String link = item.getShareLink();
        if (TextUtils.isEmpty(link)) {
            link = "https://anws.in/" + item.getPostId();
        }
        String msg = (item.getTitle() != null ? item.getTitle() + " " : "") + link;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, link);
        intent.putExtra(Intent.EXTRA_TEXT, msg);
        startActivity(Intent.createChooser(intent, "Share"));

        int shares = item.getShares() != null ? item.getShares() : 0;
        item.setShares(shares + 1);
        notifyActiveEngagement();

        if (item.getPostId() != null) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("draft_id",
                    SharedPrefsData.getInt(this, Constants.DraftID, Constants.PREF_NAME));
            jsonObject.addProperty("post_id", item.getPostId());
            jsonObject.addProperty("property", "share");
            api.update_post_count("v3", jsonObject).enqueue(new Callback<EncreptionRequest>() {
                @Override
                public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                }

                @Override
                public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                }
            });
        }
    }

    /** Comments live on the feed drawer — jump to this post and let user open comments. */
    private void openCommentsForPost(ModelClass.Datum item) {
        if (item == null || item.getPostId() == null) return;
        try {
            // Feed owns the comments drawer — open this post on Home Special path.
            Intent intent = new Intent(this, HomePage.class);
            intent.putExtra("SHARE_REDIRECT_ID", String.valueOf(item.getPostId()));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open comments", Toast.LENGTH_SHORT).show();
        }
    }

    private void openReportDialog(ModelClass.Datum item) {
        if (item == null || item.getPostId() == null) return;
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog_layout, null);
            builder.setView(dialogView);

            RadioButton one = dialogView.findViewById(R.id.one);
            RadioButton two = dialogView.findViewById(R.id.two);
            RadioButton three = dialogView.findViewById(R.id.three);
            RadioButton four = dialogView.findViewById(R.id.four);
            TextView report = dialogView.findViewById(R.id.report);
            TextView title = dialogView.findViewById(R.id.title);
            EditText reason = dialogView.findViewById(R.id.full_image_edt_news_headlines);
            Button yes = dialogView.findViewById(R.id.yes);
            ImageView close = dialogView.findViewById(R.id.close_dailog);
            final AlertDialog dialog = builder.create();
            close.setOnClickListener(v -> dialog.dismiss());

            DilogeResest dilogeResest = new DilogeResest();
            dilogeResest.setLanguageId(SharedPrefsData.getInt(
                    getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME));
            JsonObject params = new Gson().toJsonTree(dilogeResest).getAsJsonObject();
            api.reportissue(params).enqueue(new Callback<GetReportIssueResponce>() {
                @Override
                public void onResponse(Call<GetReportIssueResponce> call,
                        Response<GetReportIssueResponce> response) {
                    GetReportIssueResponce body = response.body();
                    if (body == null || body.getData() == null
                            || body.getData().getOptions() == null
                            || body.getData().getOptions().size() < 4) {
                        return;
                    }
                    one.setText("" + body.getData().getOptions().get(0));
                    two.setText("" + body.getData().getOptions().get(1));
                    three.setText("" + body.getData().getOptions().get(2));
                    four.setText("" + body.getData().getOptions().get(3));
                    report.setText("" + body.getData().getTitle());
                    title.setText("" + body.getData().getOtherReason());
                    reason.setHint("" + body.getData().getPlaceholder());

                    yes.setOnClickListener(v -> {
                        String option;
                        if (one.isChecked()) option = one.getText().toString();
                        else if (two.isChecked()) option = two.getText().toString();
                        else if (three.isChecked()) option = three.getText().toString();
                        else option = four.getText().toString();

                        DialogSendRequest req = new DialogSendRequest();
                        req.setDraftId(SharedPrefsData.getInt(
                                getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
                        req.setLanguageId(SharedPrefsData.getInt(
                                getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME));
                        req.setOption(option);
                        req.setPostId(item.getPostId());
                        req.setReason(reason.getText() != null ? reason.getText().toString() : "");
                        api.dailogSend(new Gson().toJsonTree(req).getAsJsonObject())
                                .enqueue(new Callback<DailogSendRespouse>() {
                                    @Override
                                    public void onResponse(Call<DailogSendRespouse> call,
                                            Response<DailogSendRespouse> response) {
                                        DailogSendRespouse b = response.body();
                                        if (b != null) {
                                            Toast.makeText(StatusVideoPlayerActivity.this,
                                                    "" + b.getMsg(), Toast.LENGTH_SHORT).show();
                                        }
                                        dialog.dismiss();
                                    }

                                    @Override
                                    public void onFailure(Call<DailogSendRespouse> call, Throwable t) {
                                        Toast.makeText(StatusVideoPlayerActivity.this,
                                                "Report failed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    });
                }

                @Override
                public void onFailure(Call<GetReportIssueResponce> call, Throwable t) {
                    Toast.makeText(StatusVideoPlayerActivity.this,
                            "Unable to load report options", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show();
        } catch (Exception e) {
            showLog("StatusPlayer report: " + e.getMessage());
        }
    }

    private void notifyActiveEngagement() {
        RecyclerView.ViewHolder vh = pager.findViewHolderForAdapterPosition(activePosition);
        if (vh instanceof PlayerAdapter.VH) {
            ((PlayerAdapter.VH) vh).refreshEngagement();
        }
    }
}
