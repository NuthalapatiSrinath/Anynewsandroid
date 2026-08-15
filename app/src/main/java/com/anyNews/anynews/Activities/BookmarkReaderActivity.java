package com.anyNews.anynews.Activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Utils.BookmarkStore;
import com.anyNews.anynews.Utils.ModelClass;
import com.bumptech.glide.Glide;

/**
 * Offline article reader for a bookmarked post (title, body, image, metadata).
 */
public class BookmarkReaderActivity extends AppCompatActivity {

    private ModelClass.Datum post;
    private ImageView toggle;
    /** Install WindowInsets listener once (re-apply via requestApplyInsets). */
    private boolean systemBarInsetsInstalled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_bookmark_reader);
        applySystemBarInsets();

        long postId = getIntent().getLongExtra(BookmarksActivity.EXTRA_POST_ID, 0L);
        if (postId <= 0) {
            // Support Integer extras from older call sites
            postId = getIntent().getIntExtra(BookmarksActivity.EXTRA_POST_ID, 0);
        }
        post = BookmarkStore.getLocalPost(this, postId);
        if (post == null) {
            // Read News tab caches postcards locally (no server fetch).
            try {
                post = com.anyNews.anynews.Utils.ReadArticleStore.getCached(this, postId);
            } catch (Exception ignored) {
            }
        }
        if (post == null) {
            Toast.makeText(this, R.string.no_bookmarks, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        findViewById(R.id.reader_back).setOnClickListener(v -> finish());
        toggle = findViewById(R.id.reader_bookmark_toggle);
        TextView title = findViewById(R.id.reader_title);
        TextView meta = findViewById(R.id.reader_meta);
        TextView body = findViewById(R.id.reader_body);
        ImageView image = findViewById(R.id.reader_image);

        title.setText(post.getTitle() != null ? post.getTitle() : "");
        body.setText(post.getDescription() != null ? post.getDescription() : "");

        String metaLine = "";
        if (!TextUtils.isEmpty(post.getWriterName())) {
            metaLine = post.getWriterName();
        }
        if (!TextUtils.isEmpty(post.getPost_date_time())) {
            metaLine =
                    TextUtils.isEmpty(metaLine)
                            ? post.getPost_date_time()
                            : metaLine + " · " + post.getPost_date_time();
        }
        meta.setText(metaLine);

        String imageUrl = post.getThumbnail();
        if (TextUtils.isEmpty(imageUrl)
                && post.getImages() != null
                && !post.getImages().isEmpty()) {
            imageUrl = post.getImages().get(0);
        }
        if (TextUtils.isEmpty(imageUrl)) {
            imageUrl = post.getBackgroundImage();
        }
        if (!TextUtils.isEmpty(imageUrl)) {
            Glide.with(this).load(imageUrl).centerCrop().into(image);
            image.setVisibility(android.view.View.VISIBLE);
        } else {
            image.setVisibility(android.view.View.GONE);
        }

        refreshToggleIcon();
        toggle.setOnClickListener(
                v ->
                        BookmarkStore.toggle(
                                this,
                                post,
                                (bookmarked, toastMsg) -> {
                                    if (toastMsg != null) {
                                        Toast.makeText(
                                                        this,
                                                        bookmarked
                                                                ? getString(
                                                                        R.string.bookmark_saved)
                                                                : getString(
                                                                        R.string
                                                                                .bookmark_removed),
                                                        Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                    refreshToggleIcon();
                                    if (!bookmarked) {
                                        finish();
                                    }
                                }));
    }

    @Override
    protected void onStart() {
        super.onStart();
        applySystemBarInsets();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applySystemBarInsets();
    }

    /**
     * Content must not draw under the status bar.
     * targetSdk 35 forces edge-to-edge and ignores setStatusBarColor; setDecorFitsSystemWindows
     * alone is not enough. Pad the brand-orange header with statusBars inset so back/title sit
     * below the clock/icons while orange fills the status-bar region. White system icons.
     */
    private void applySystemBarInsets() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        int orange = ContextCompat.getColor(this, R.color.brand_orange);
        getWindow().setStatusBarColor(orange);
        getWindow().setNavigationBarColor(
                ContextCompat.getColor(this, R.color.background_color));

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(false);
            boolean lightNav =
                    (getResources().getConfiguration().uiMode
                                    & Configuration.UI_MODE_NIGHT_MASK)
                            != Configuration.UI_MODE_NIGHT_YES;
            controller.setAppearanceLightNavigationBars(lightNav);
        }

        final View root = findViewById(R.id.reader_root);
        final View header = findViewById(R.id.reader_header);
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
            final int rootPadBottom = root.getPaddingBottom();

            ViewCompat.setOnApplyWindowInsetsListener(
                    root,
                    (v, windowInsets) -> {
                        Insets status =
                                windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());
                        Insets bars =
                                windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                        int top = status.top;
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
                        v.setPadding(
                                v.getPaddingLeft(),
                                v.getPaddingTop(),
                                v.getPaddingRight(),
                                rootPadBottom + bars.bottom);
                        return WindowInsetsCompat.CONSUMED;
                    });
        }
        ViewCompat.requestApplyInsets(root);
    }

    private void refreshToggleIcon() {
        boolean saved =
                post != null
                        && post.getPostId() != null
                        && BookmarkStore.isBookmarked(this, post.getPostId());
        toggle.setImageResource(
                saved
                        ? R.drawable.ic_action_bookmark
                        : R.drawable.ic_action_bookmark_outline);
    }
}
