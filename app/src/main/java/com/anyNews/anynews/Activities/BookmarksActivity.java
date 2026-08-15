package com.anyNews.anynews.Activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Utils.BookmarkStore;
import com.anyNews.anynews.Utils.ModelClass;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Offline-capable list of bookmarked articles.
 */
public class BookmarksActivity extends AppCompatActivity {

    public static final String EXTRA_POST_ID = "bookmark_post_id";

    private RecyclerView listView;
    private TextView emptyView;
    private final List<ModelClass.Datum> items = new ArrayList<>();
    private BookmarksAdapter adapter;
    /** Sequence so stale async reloads cannot clobber a newer full list. */
    private int reloadGeneration;
    /** Install WindowInsets listener once (re-apply via requestApplyInsets). */
    private boolean systemBarInsetsInstalled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_bookmarks);

        listView = findViewById(R.id.bookmarks_list);
        emptyView = findViewById(R.id.bookmarks_empty);
        findViewById(R.id.bookmarks_back).setOnClickListener(v -> finish());
        applySystemBarInsets();

        adapter = new BookmarksAdapter();
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.setAdapter(adapter);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_hold, R.anim.activity_slide_down_out);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Re-apply after any global bar setup so system bars stay correct after recreate.
        applySystemBarInsets();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applySystemBarInsets();
        // Full rehydrate from disk (after theme recreate, reader return, etc.).
        reload();
        BookmarkStore.syncFromServer(this, this::reload);
        BookmarkStore.flushPending(this);
    }

    /**
     * Content must not draw under the status bar.
     * targetSdk 35 forces edge-to-edge and ignores setStatusBarColor; setDecorFitsSystemWindows
     * alone is not enough. Pad the brand-orange header with statusBars inset so title/back sit
     * below the clock/icons while the header still fills the status-bar region solid orange.
     * White system icons; nav bar follows light/night background.
     */
    private void applySystemBarInsets() {
        // Own edge-to-edge; pad explicitly (API 35 does not push layout below bars).
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        int orange = ContextCompat.getColor(this, R.color.brand_orange);
        // Pre-API 35 solid bar; on 35+ the padded orange header is the painted region.
        getWindow().setStatusBarColor(orange);
        getWindow().setNavigationBarColor(
                ContextCompat.getColor(this, R.color.background_color));

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            // Light (white) system icons over brand orange — day and night.
            controller.setAppearanceLightStatusBars(false);
            boolean lightNav =
                    (getResources().getConfiguration().uiMode
                                    & Configuration.UI_MODE_NIGHT_MASK)
                            != Configuration.UI_MODE_NIGHT_YES;
            controller.setAppearanceLightNavigationBars(lightNav);
        }

        final View root = findViewById(R.id.bookmarks_root);
        final View header = findViewById(R.id.bookmarks_header);
        if (root == null || header == null) {
            return;
        }

        if (!systemBarInsetsInstalled) {
            systemBarInsetsInstalled = true;
            final int baseHeaderHeightPx = header.getLayoutParams().height;
            final int headerPadL = header.getPaddingLeft();
            final int headerPadT = header.getPaddingTop();
            final int headerPadR = header.getPaddingRight();
            final int headerPadB = header.getPaddingBottom();
            final int listPadBottom =
                    listView != null ? listView.getPaddingBottom() : 0;
            final int emptyPadBottom =
                    emptyView != null ? emptyView.getPaddingBottom() : 0;

            ViewCompat.setOnApplyWindowInsetsListener(
                    root,
                    (v, windowInsets) -> {
                        Insets status =
                                windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());
                        Insets bars =
                                windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                        int top = status.top;
                        // Keep a full 56dp tool row below the status bar; orange fills both.
                        if (baseHeaderHeightPx > 0) {
                            ViewGroup.LayoutParams lp = header.getLayoutParams();
                            lp.height = baseHeaderHeightPx + top;
                            header.setLayoutParams(lp);
                        }
                        header.setPadding(
                                headerPadL + bars.left,
                                headerPadT + top,
                                headerPadR + bars.right,
                                headerPadB);
                        if (listView != null) {
                            listView.setPadding(
                                    listView.getPaddingLeft(),
                                    listView.getPaddingTop(),
                                    listView.getPaddingRight(),
                                    listPadBottom + bars.bottom);
                        }
                        if (emptyView != null) {
                            emptyView.setPadding(
                                    emptyView.getPaddingLeft(),
                                    emptyView.getPaddingTop(),
                                    emptyView.getPaddingRight(),
                                    emptyPadBottom + bars.bottom);
                        }
                        return WindowInsetsCompat.CONSUMED;
                    });
        }
        ViewCompat.requestApplyInsets(root);
    }

    private void reload() {
        final int gen = ++reloadGeneration;
        BookmarkStore.listLocalAsync(
                this,
                list -> {
                    if (isFinishing() || gen != reloadGeneration) {
                        return;
                    }
                    items.clear();
                    if (list != null) {
                        items.addAll(list);
                    }
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                    boolean empty = items.isEmpty();
                    if (emptyView != null) {
                        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
                    }
                    if (listView != null) {
                        listView.setVisibility(empty ? View.GONE : View.VISIBLE);
                    }
                });
    }

    private class BookmarksAdapter extends RecyclerView.Adapter<BookmarksAdapter.VH> {

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v =
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_bookmark, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            ModelClass.Datum item = items.get(position);
            holder.title.setText(
                    item.getTitle() != null ? item.getTitle() : "");
            String meta = "";
            if (!TextUtils.isEmpty(item.getWriterName())) {
                meta = item.getWriterName();
            }
            if (!TextUtils.isEmpty(item.getPost_date_time())) {
                meta =
                        TextUtils.isEmpty(meta)
                                ? item.getPost_date_time()
                                : meta + " · " + item.getPost_date_time();
            }
            holder.meta.setText(meta);

            String imageUrl = item.getThumbnail();
            if (TextUtils.isEmpty(imageUrl) && item.getImages() != null && !item.getImages().isEmpty()) {
                imageUrl = item.getImages().get(0);
            }
            if (TextUtils.isEmpty(imageUrl)) {
                imageUrl = item.getBackgroundImage();
            }
            if (!TextUtils.isEmpty(imageUrl)) {
                Glide.with(holder.image.getContext())
                        .load(imageUrl)
                        .centerCrop()
                        .into(holder.image);
            } else {
                holder.image.setImageDrawable(null);
            }

            holder.itemView.setOnClickListener(
                    v -> {
                        Intent intent =
                                new Intent(BookmarksActivity.this, BookmarkReaderActivity.class);
                        long postId = item.getPostId() != null ? item.getPostId() : 0L;
                        intent.putExtra(EXTRA_POST_ID, postId);
                        startActivity(intent);
                    });

            holder.remove.setOnClickListener(
                    v -> {
                        BookmarkStore.toggle(
                                BookmarksActivity.this,
                                item,
                                (bookmarked, toastMsg) -> {
                                    if (toastMsg != null) {
                                        Toast.makeText(
                                                        BookmarksActivity.this,
                                                        getString(R.string.bookmark_removed),
                                                        Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                    reload();
                                });
                    });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class VH extends RecyclerView.ViewHolder {
            final ImageView image;
            final TextView title;
            final TextView meta;
            final ImageView remove;

            VH(@NonNull View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.bookmark_item_image);
                title = itemView.findViewById(R.id.bookmark_item_title);
                meta = itemView.findViewById(R.id.bookmark_item_meta);
                remove = itemView.findViewById(R.id.bookmark_item_remove);
            }
        }
    }
}
