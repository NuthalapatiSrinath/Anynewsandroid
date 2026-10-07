package com.anyNews.anynews.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Utils.DailyUpdatesShareLanguage;
import com.anyNews.anynews.Utils.DailyUpdatesShareLinks;
import com.anyNews.anynews.Utils.TtdConfigHelper;
import com.anyNews.anynews.Utils.TtdLabels;
import com.anyNews.anynews.ttd.TtdModels;
import com.anyNews.anynews.ttd.TtdRepository;

/**
 * TTD detail — dark + orange mock (hero temple photo, distinct icons, i18n via
 * {@link TtdLabels}). URLs + section visibility from admin {@link TtdConfigHelper}.
 * External link-outs only for Important Links + scheduled booking CTA.
 */
public class TtdDetailActivity extends AppCompatActivity {

    public static final String EXTRA_FROM_CALENDAR = "from_calendar";

    private static final int ACCENT = 0xFFFF8A3D;
    private static final String[] SEVA_PRICES = {
            "₹ 150", "₹ 500", "₹ 1,000", "₹ 10,000", "₹ 25,000"
    };
    private static final int[] SEVA_ICONS = {
            R.drawable.ic_ttd_lamp,
            R.drawable.ic_ttd_flower,
            R.drawable.ic_ttd_kalasha,
            R.drawable.ic_ttd_crown,
            R.drawable.ic_ttd_brahmotsavam
    };

    @Nullable private TtdModels.DayPack pack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!TtdConfigHelper.isEnabled(this)) {
            Toast.makeText(this, R.string.ttd_disabled, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        DailyUpdatesShareLinks.beginLanguage(this);
        setContentView(R.layout.activity_ttd_detail);
        applySystemBars();

        pack = TtdRepository.loadToday(this);
        bindUi(pack);
        wireClicks();
        clipHeroPhoto();
    }

    private void applySystemBars() {
        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.BLACK);
        window.setNavigationBarColor(Color.BLACK);
        WindowInsetsControllerCompat c =
                WindowCompat.getInsetsController(window, window.getDecorView());
        if (c != null) {
            c.setAppearanceLightStatusBars(false);
            c.setAppearanceLightNavigationBars(false);
        }
        View root = findViewById(R.id.ttd_detail_root);
        if (root == null) return;
        final int padL = root.getPaddingLeft();
        final int padT = root.getPaddingTop();
        final int padR = root.getPaddingRight();
        final int padB = root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(padL + bars.left, padT + bars.top, padR + bars.right,
                    padB + bars.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private void clipHeroPhoto() {
        View card = findViewById(R.id.ttd_hero_card);
        ImageView hero = findViewById(R.id.ttd_hero_image);
        if (hero == null) return;
        final float radius = 10f * getResources().getDisplayMetrics().density;
        ViewOutlineProvider round = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
            }
        };
        if (card != null) {
            card.setOutlineProvider(round);
            card.setClipToOutline(true);
            card.setBackgroundColor(0xFF0B0B0B);
        }
        hero.setOutlineProvider(round);
        hero.setClipToOutline(true);
        hero.setScaleX(1f);
        hero.setScaleY(1f);
        hero.setImageResource(R.drawable.img_ttd_hero_banner);
        // MATRIX cover + overscale: crop all edges (hide white fringe); mild end bias
        // keeps "Tirumala Tirupati Devasthanams" on the right readable.
        hero.setScaleType(ImageView.ScaleType.MATRIX);
        hero.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> applyHeroCoverMatrix(hero));
        hero.post(() -> applyHeroCoverMatrix(hero));
    }

    /**
     * Cover-scale the hero drawable with a slight overscale so all four edges are
     * cropped (hides white fringe). Mild end bias keeps the right-side title readable.
     */
    private void applyHeroCoverMatrix(@NonNull ImageView hero) {
        Drawable d = hero.getDrawable();
        if (d == null) return;
        int vw = hero.getWidth();
        int vh = hero.getHeight();
        int dw = d.getIntrinsicWidth();
        int dh = d.getIntrinsicHeight();
        if (vw <= 0 || vh <= 0 || dw <= 0 || dh <= 0) return;
        // Overscale ~8% so thin edge artifacts are clipped on all sides.
        float scale = Math.max((float) vw / dw, (float) vh / dh) * 1.08f;
        float scaledW = dw * scale;
        float scaledH = dh * scale;
        // Bias crop toward the start so right title stays in frame, still inset right.
        float dx = (vw - scaledW) * 0.35f;
        float dy = (vh - scaledH) * 0.5f;
        Matrix m = new Matrix();
        m.setScale(scale, scale);
        m.postTranslate(dx, dy);
        hero.setImageMatrix(m);
    }

    private void bindUi(@NonNull TtdModels.DayPack p) {
        setText(R.id.ttd_top_subtitle, TtdLabels.topSubtitle(this));
        setText(R.id.ttd_greeting, TtdLabels.heroBody(this));
        setText(R.id.ttd_date, TtdLabels.today(this));
        setText(R.id.ttd_darshan_heading, TtdLabels.darshanHeading(this));
        setText(R.id.ttd_label_sarva, TtdLabels.sarvaLabel(this));
        setText(R.id.ttd_stat_sarva, TtdLabels.sarvaValue(this));
        setText(R.id.ttd_label_special, TtdLabels.specialLabel(this));
        setText(R.id.ttd_label_timings, TtdLabels.timingsLabel(this));
        setText(R.id.ttd_book_cta, TtdLabels.bookDarshan(this));
        setText(R.id.ttd_sevas_heading, TtdLabels.sevasHeading(this));
        setText(R.id.ttd_news_heading, TtdLabels.newsHeading(this));
        setText(R.id.ttd_links_heading, TtdLabels.linksHeading(this));
        setText(R.id.ttd_link_official_text, TtdLabels.linkOfficial(this));
        setText(R.id.ttd_link_edarshan_text, TtdLabels.linkEdarshan(this));
        setText(R.id.ttd_link_app_text, TtdLabels.linkApp(this));
        setText(R.id.ttd_blessing_line1, TtdLabels.blessingLine1(this));
        setText(R.id.ttd_blessing, TtdConfigHelper.blessing(this));
        setText(R.id.ttd_flash, p.flashNews);
        setText(R.id.ttd_slotted, p.slottedDarshanStatus);

        ImageView back = findViewById(R.id.ttd_back);
        if (back != null) back.setColorFilter(ACCENT, PorterDuff.Mode.SRC_IN);
        ImageView headerShare = findViewById(R.id.ttd_header_share);
        if (headerShare != null) {
            headerShare.setImageResource(R.drawable.ic_share_icon);
            headerShare.setColorFilter(ACCENT, PorterDuff.Mode.SRC_IN);
        }

        // Quick tiles are informational only — no accidental external link-outs.
        bindQuick(R.id.ttd_quick_darshan, R.drawable.ic_ttd_darshan,
                TtdLabels.quickDarshan(this), TtdLabels.quickDarshanSub(this));
        bindQuick(R.id.ttd_quick_room, R.drawable.ic_ttd_bed,
                TtdLabels.quickRoom(this), TtdLabels.quickRoomSub(this));
        bindQuick(R.id.ttd_quick_anna, R.drawable.ic_ttd_anna,
                TtdLabels.quickAnna(this), TtdLabels.quickAnnaSub(this));
        bindQuick(R.id.ttd_quick_hundi, R.drawable.ic_ttd_hundi,
                TtdLabels.quickHundi(this), TtdLabels.quickHundiSub(this));

        View quick = findViewById(R.id.ttd_quick_row);
        if (quick != null) {
            quick.setVisibility(TtdConfigHelper.showQuick(this) ? View.VISIBLE : View.GONE);
        }
        View darshanCard = findViewById(R.id.ttd_darshan_card);
        if (darshanCard != null) {
            darshanCard.setVisibility(
                    TtdConfigHelper.showDarshan(this) ? View.VISIBLE : View.GONE);
        }
        View sevas = findViewById(R.id.ttd_sevas_section);
        if (sevas != null) {
            sevas.setVisibility(TtdConfigHelper.showSevas(this) ? View.VISIBLE : View.GONE);
        }
        View news = findViewById(R.id.ttd_news_section);
        if (news != null) {
            news.setVisibility(TtdConfigHelper.showNews(this) ? View.VISIBLE : View.GONE);
        }
        View links = findViewById(R.id.ttd_links_section);
        if (links != null) {
            links.setVisibility(TtdConfigHelper.showLinks(this) ? View.VISIBLE : View.GONE);
        }

        TextView book = findViewById(R.id.ttd_book_cta);
        if (book != null) {
            boolean show = TtdConfigHelper.isBookingCtaVisibleToday(this);
            book.setVisibility(show ? View.VISIBLE : View.GONE);
            book.setEnabled(show);
        }

        bindSevasRow();
        bindNewsList(p);
    }

    private void bindQuick(int includeId, int iconRes, @NonNull String title,
                           @NonNull String sub) {
        View root = findViewById(includeId);
        if (root == null) return;
        ImageView icon = root.findViewById(R.id.ttd_quick_icon);
        TextView titleTv = root.findViewById(R.id.ttd_quick_title);
        TextView subTv = root.findViewById(R.id.ttd_quick_sub);
        if (icon != null) {
            icon.setImageResource(iconRes);
            icon.clearColorFilter();
        }
        if (titleTv != null) {
            titleTv.setText(title);
            titleTv.setTextColor(ACCENT);
        }
        if (subTv != null) subTv.setText(sub);
        root.setClickable(false);
        root.setOnClickListener(null);
    }

    private void bindSevasRow() {
        LinearLayout row = findViewById(R.id.ttd_sevas_row);
        if (row == null) return;
        row.removeAllViews();
        float density = getResources().getDisplayMetrics().density;
        for (int i = 0; i < SEVA_PRICES.length; i++) {
            LinearLayout tile = new LinearLayout(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    (int) (92 * density), ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins((int) (4 * density), 0, (int) (4 * density), 0);
            tile.setLayoutParams(lp);
            tile.setOrientation(LinearLayout.VERTICAL);
            tile.setGravity(Gravity.CENTER_HORIZONTAL);
            tile.setBackgroundResource(R.drawable.bg_ttd_quick_tile);
            tile.setPadding((int) (6 * density), (int) (10 * density),
                    (int) (6 * density), (int) (10 * density));

            ImageView ic = new ImageView(this);
            ic.setLayoutParams(new LinearLayout.LayoutParams(
                    (int) (28 * density), (int) (28 * density)));
            ic.setImageResource(SEVA_ICONS[i]);
            tile.addView(ic);

            TextView name = new TextView(this);
            name.setText(TtdLabels.sevaName(i, this));
            name.setTextColor(0xFFFFFFFF);
            name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            name.setGravity(Gravity.CENTER);
            name.setPadding(0, (int) (6 * density), 0, 0);
            tile.addView(name);

            TextView price = new TextView(this);
            price.setText(SEVA_PRICES[i]);
            price.setTextColor(ACCENT);
            price.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            price.setGravity(Gravity.CENTER);
            price.setPadding(0, (int) (2 * density), 0, 0);
            tile.addView(price);

            // Informational tiles only — no website open.
            tile.setClickable(false);
            row.addView(tile);
        }
    }

    private void bindNewsList(@NonNull TtdModels.DayPack p) {
        LinearLayout list = findViewById(R.id.ttd_news_list);
        if (list == null) return;
        list.removeAllViews();
        String[] dates = {"08 Aug, 2026", "12 Aug, 2026", "20 Aug, 2026"};
        float density = getResources().getDisplayMetrics().density;
        for (int i = 0; i < 3; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, (int) (8 * density), 0, (int) (8 * density));

            TextView bullet = new TextView(this);
            bullet.setText("●");
            bullet.setTextColor(ACCENT);
            bullet.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            row.addView(bullet);

            TextView body = new TextView(this);
            LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            blp.setMargins((int) (8 * density), 0, (int) (8 * density), 0);
            body.setLayoutParams(blp);
            body.setText(TtdLabels.newsLine(i, this));
            body.setTextColor(0xFFE8E8E8);
            body.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            row.addView(body);

            TextView date = new TextView(this);
            date.setText(dates[i]);
            date.setTextColor(0x99FFFFFF);
            date.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            row.addView(date);

            // News bullets are display-only — do not open TTD websites.
            row.setClickable(false);
            list.addView(row);
        }
    }

    @Override
    protected void onDestroy() {
        DailyUpdatesShareLanguage.end();
        super.onDestroy();
    }

    private void wireClicks() {
        View back = findViewById(R.id.ttd_back);
        if (back != null) back.setOnClickListener(v -> finish());

        View headerShare = findViewById(R.id.ttd_header_share);
        if (headerShare != null) {
            headerShare.setOnClickListener(v -> sharePack());
        }

        TextView book = findViewById(R.id.ttd_book_cta);
        if (book != null) {
            book.setOnClickListener(v -> {
                if (!TtdConfigHelper.isBookingCtaVisibleToday(this)) return;
                openUrl(TtdConfigHelper.bookingUrl(this));
            });
        }
        // Important Links — intentional main CTAs
        View linkOfficial = findViewById(R.id.ttd_link_official);
        if (linkOfficial != null) {
            linkOfficial.setOnClickListener(v -> openUrl(TtdConfigHelper.officialUrl(this)));
        }
        View linkEd = findViewById(R.id.ttd_link_edarshan);
        if (linkEd != null) {
            linkEd.setOnClickListener(v -> openUrl(TtdConfigHelper.edarshanUrl(this)));
        }
        View linkApp = findViewById(R.id.ttd_link_app);
        if (linkApp != null) {
            linkApp.setOnClickListener(v -> openUrl(TtdConfigHelper.appUrl(this)));
        }
        TextView openOfficial = findViewById(R.id.ttd_open_official);
        if (openOfficial != null) {
            openOfficial.setOnClickListener(v -> openUrl(TtdConfigHelper.officialUrl(this)));
        }
        TextView openBooking = findViewById(R.id.ttd_open_booking);
        if (openBooking != null) {
            openBooking.setOnClickListener(v -> {
                if (!TtdConfigHelper.isBookingCtaVisibleToday(this)) return;
                openUrl(TtdConfigHelper.bookingUrl(this));
            });
        }
    }

    private void sharePack() {
        if (pack == null) return;
        String text = TtdLabels.heroTitle(this) + "\n"
                + TtdLabels.heroSubtitle(this) + "\n"
                + pack.dateLabel + "\n\n"
                + TtdLabels.newsLine(0, this) + "\n\n"
                + TtdConfigHelper.officialUrl(this) + "\n"
                + TtdConfigHelper.bookingUrl(this);
        DailyUpdatesShareLinks.launch(this, text, "ttd", getString(R.string.ttd_share));
    }

    private void openUrl(@Nullable String url) {
        if (url == null || url.trim().isEmpty()) {
            Toast.makeText(this, R.string.ttd_open_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!TtdRepository.isAllowedExternalUrl(url)
                && !url.startsWith("https://") && !url.startsWith("http://")) {
            Toast.makeText(this, R.string.ttd_open_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(this, R.string.ttd_open_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void setText(int id, @Nullable String value) {
        TextView tv = findViewById(id);
        if (tv != null) tv.setText(value != null ? value : "");
    }
}
