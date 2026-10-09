package com.anyNews.anynews.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.PorterDuff;
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

import org.json.JSONArray;
import org.json.JSONObject;

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
        applyLive(TtdConfigHelper.liveJson(this));
        wireClicks();
        clipHeroPhoto();
        refreshFromRedis();
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
        hero.setAdjustViewBounds(true);
        hero.setScaleType(ImageView.ScaleType.FIT_CENTER);
    }

    private void bindUi(@NonNull TtdModels.DayPack p) {
        setText(R.id.ttd_top_subtitle, TtdLabels.topSubtitle(this));
        setText(R.id.ttd_greeting, TtdLabels.heroBody(this));
        setText(R.id.ttd_date, TtdLabels.today(this));
        setText(R.id.ttd_darshan_heading, TtdLabels.darshanHeading(this));
        setText(R.id.ttd_label_sarva, TtdLabels.sarvaLabel(this));
        setText(R.id.ttd_stat_sarva, "");
        setText(R.id.ttd_stat_special, "");
        setText(R.id.ttd_stat_timings, "");
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
        bindNewsList(null);
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

    private void bindNewsList(@Nullable JSONObject live) {
        LinearLayout list = findViewById(R.id.ttd_news_list);
        if (list == null) return;
        list.removeAllViews();
        JSONArray news = live != null ? live.optJSONArray("news") : null;
        if (news == null || news.length() == 0) return;
        int lang = com.anyNews.anynews.Utils.DailyUpdatesShareLanguage.effective(this);
        float density = getResources().getDisplayMetrics().density;
        for (int i = 0; i < news.length(); i++) {
            JSONObject row = news.optJSONObject(i);
            if (row == null) continue;
            String title = row.optString("title", "").trim();
            if (!titleMatchesLanguage(title, lang)) continue;
            String date = TtdLabels.newsDate(row.optString("date_iso", ""), this);

            LinearLayout line = new LinearLayout(this);
            line.setOrientation(LinearLayout.HORIZONTAL);
            line.setGravity(Gravity.CENTER_VERTICAL);
            line.setPadding(0, (int) (8 * density), 0, (int) (8 * density));

            TextView bullet = new TextView(this);
            bullet.setText("●");
            bullet.setTextColor(ACCENT);
            bullet.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            line.addView(bullet);

            TextView body = new TextView(this);
            LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            blp.setMargins((int) (8 * density), 0, (int) (8 * density), 0);
            body.setLayoutParams(blp);
            body.setText(title);
            body.setTextColor(0xFFE8E8E8);
            body.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            line.addView(body);

            TextView dateTv = new TextView(this);
            dateTv.setText(date);
            dateTv.setTextColor(0x99FFFFFF);
            dateTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            line.addView(dateTv);

            line.setClickable(false);
            list.addView(line);
        }
    }

    /** Paint the saved Redis slice. Missing facts stay blank — nothing is invented. */
    private void applyLive(@Nullable JSONObject live) {
        if (live == null) {
            bindNewsList(null);
            return;
        }
        JSONObject facts = darshanFacts(live);
        int hours = facts.optInt("sarva_hours", 0);
        setText(R.id.ttd_stat_sarva, TtdLabels.hoursCount(hours, this));
        if (facts.has("special_inr") && !facts.isNull("special_inr")
                && facts.optInt("special_inr", 0) > 0) {
            setText(R.id.ttd_stat_special, formatInr(facts.optInt("special_inr", 0)));
        } else {
            setText(R.id.ttd_stat_special,
                    TtdLabels.specialName(facts.optString("special_name", ""), this));
        }
        JSONArray windows = facts.optJSONArray("windows");
        StringBuilder times = new StringBuilder();
        if (windows != null) {
            for (int i = 0; i < windows.length(); i++) {
                String w = windows.optString(i, "").trim();
                if (w.isEmpty()) continue;
                if (times.length() > 0) times.append('\n');
                times.append(w);
            }
        }
        setText(R.id.ttd_stat_timings, times.toString());
        bindNewsList(facts);
    }

    /**
     * The Redis slice is flat ({@code sarva_hours}, {@code windows},
     * {@code special_inr}, {@code special_name}). Splash may hand the
     * whole {@code ttd} object with that slice under {@code live}.
     */
    @NonNull
    private static JSONObject darshanFacts(@NonNull JSONObject live) {
        if (live.has("sarva_hours") || live.has("windows") || live.has("special_name")
                || live.has("special_inr")) {
            return live;
        }
        JSONObject inner = live.optJSONObject("live");
        return inner != null ? inner : live;
    }

    private void refreshFromRedis() {
        try {
            com.anyNews.anynews.Services.APIService api =
                    com.anyNews.anynews.Services.RestApiClient.getClient()
                            .create(com.anyNews.anynews.Services.APIService.class);
            int lang = com.anyNews.anynews.Utils.DailyUpdatesShareLanguage.effective(this);
            api.ttdDataset("v3", lang).enqueue(new retrofit2.Callback<com.google.gson.JsonObject>() {
                @Override
                public void onResponse(
                        @NonNull retrofit2.Call<com.google.gson.JsonObject> call,
                        @NonNull retrofit2.Response<com.google.gson.JsonObject> response) {
                    if (isFinishing() || response.body() == null) return;
                    try {
                        JSONObject body = new JSONObject(response.body().toString());
                        if (!body.optBoolean("ok", false)) return;
                        TtdConfigHelper.saveLiveIfNewer(TtdDetailActivity.this, body);
                        JSONObject saved = TtdConfigHelper.liveJson(TtdDetailActivity.this);
                        runOnUiThread(() -> {
                            if (!isFinishing()) applyLive(saved);
                        });
                    } catch (Exception ignored) {
                    }
                }

                @Override
                public void onFailure(
                        @NonNull retrofit2.Call<com.google.gson.JsonObject> call,
                        @NonNull Throwable t) {
                    /* keep the splash / feed pack */
                }
            });
        } catch (Exception ignored) {
        }
    }

    private static boolean titleMatchesLanguage(@NonNull String title, int lang) {
        if (title.isEmpty()) return false;
        if (lang == 0 || lang == 7) return true;
        int start;
        int end;
        switch (lang) {
            case 1: start = 0x0D00; end = 0x0D7F; break;
            case 2: start = 0x0900; end = 0x097F; break;
            case 3: start = 0x0C00; end = 0x0C7F; break;
            case 4: start = 0x0B80; end = 0x0BFF; break;
            case 5: start = 0x0C80; end = 0x0CFF; break;
            case 6: start = 0x0B00; end = 0x0B7F; break;
            default: return true;
        }
        for (int i = 0; i < title.length(); ) {
            int cp = title.codePointAt(i);
            if (cp >= start && cp <= end) return true;
            i += Character.charCount(cp);
        }
        return false;
    }

    @NonNull
    private static String formatInr(int amount) {
        String digits = Integer.toString(amount);
        if (digits.length() <= 3) return "₹ " + digits;
        String tail = digits.substring(digits.length() - 3);
        String head = digits.substring(0, digits.length() - 3);
        StringBuilder sb = new StringBuilder();
        int i = head.length() % 2;
        if (i > 0) sb.append(head, 0, i);
        for (; i < head.length(); i += 2) {
            if (sb.length() > 0) sb.append(',');
            sb.append(head, i, Math.min(i + 2, head.length()));
        }
        if (sb.length() > 0) sb.append(',');
        sb.append(tail);
        return "₹ " + sb;
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
        DailyUpdatesShareLinks.launch(this, "", "ttd", getString(R.string.ttd_share));
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
