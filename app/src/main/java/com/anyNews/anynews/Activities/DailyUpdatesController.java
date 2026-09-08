package com.anyNews.anynews.Activities;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.LanguageSelectionRequest;
import com.anyNews.anynews.Pojo.ListAllArticlesRequest;
import com.anyNews.anynews.Pojo.UpdateStateResponse;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.DailyUpdatesArtHelper;
import com.anyNews.anynews.Utils.DailyUpdatesConfigHelper;
import com.anyNews.anynews.Utils.DistrictLabelHelper;
import com.anyNews.anynews.Utils.FlashNewsChipHelper;
import com.anyNews.anynews.Utils.FooterTabLabels;
import com.anyNews.anynews.Utils.JobsConfigHelper;
import com.anyNews.anynews.Utils.LanguageFeedInvalidator;
import com.anyNews.anynews.Utils.LocalUpdatesConfigHelper;
import com.anyNews.anynews.Utils.ModelClass;
import com.anyNews.anynews.Utils.SelectionListCache;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.Utils.TtdConfigHelper;
import com.anyNews.anynews.models.DistricsModel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

/**
 * Embedded Daily Updates page (HomePage content host above footer).
 * Mana Zilla shows 2 latest local (district) headlines as floating TextViews.
 */
public final class DailyUpdatesController {

    private static final String CACHE_T1 = "du_mana_title_1";
    private static final String CACHE_T2 = "du_mana_title_2";
    private static final String CACHE_DIST = "du_mana_cache_dist";
    private static final String CACHE_AT = "du_mana_cache_at";
    private static final long CACHE_TTL_MS = 3 * 60 * 1000L;

    public interface HostCallbacks {
        /** Open Flash News feed (kept reachable from the Daily Updates page chip). */
        void openFlashNewsFromDailyUpdates();

        /** Leave Daily Updates and open Local news for the selected district. */
        void openLocalNewsFromDailyUpdates();

        /** Leave Daily Updates and open Telugu calendar / Panchangam (same as footer tab). */
        void openPanchangamFromDailyUpdates();

        /** Leave Daily Updates and open Raasi Phalalu page in the calendar host. */
        void openRashiFromDailyUpdates();
    }

    @NonNull
    private final AppCompatActivity host;
    @NonNull
    private final View root;
    @Nullable
    private final HostCallbacks callbacks;

    @Nullable
    private ObjectAnimator liveBlink;
    @Nullable
    private Call<EncreptionRequest> manaFetchCall;
    private boolean manaFetchInFlight;
    private int manaFetchSeq;

    private DailyUpdatesController(@NonNull AppCompatActivity host,
                                   @NonNull View root,
                                   @Nullable HostCallbacks callbacks) {
        this.host = host;
        this.root = root;
        this.callbacks = callbacks;
        bindLabels();
        wireClicks();
        bindSummariesAndGrid();
        // Content-start padding AFTER reflowGrid — reparenting must not leave XML 64dp.
        applyDynamicChrome();
        bindManaZilla();
        bindFlashStripVisibility();
        bindStockStrip();
        startLiveBlink();
        applyCardBackgroundArt();
        applyRoundedCardClips();
    }

    @NonNull
    public static DailyUpdatesController attachEmbedded(
            @NonNull AppCompatActivity host,
            @NonNull ViewGroup hostContainer,
            @Nullable HostCallbacks callbacks) {
        hostContainer.removeAllViews();
        View page = LayoutInflater.from(host).inflate(
                R.layout.page_daily_updates, hostContainer, false);
        hostContainer.addView(page, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        return new DailyUpdatesController(host, page, callbacks);
    }

    public void onHostResume() {
        bindLabels();
        syncBellVisibility();
        bindSummariesAndGrid();
        // Re-apply admin insets after grid reflow (splash values → paddingStart).
        applyDynamicChrome();
        bindManaZilla();
        bindFlashStripVisibility();
        bindStockStrip();
        startLiveBlink();
        applyCardBackgroundArt();
        applyRoundedCardClips();
    }

    /**
     * Admin splash knobs: typography, per-box content start, Flash icon size.
     * Defaults match XML when prefs unset.
     */
    private void applyDynamicChrome() {
        applyDuTypography();
        applyCardContentStarts();
        applyFlashChrome();
    }

    private void applyDuTypography() {
        float titleSp = DailyUpdatesConfigHelper.pageTitleTextSizeSp(host);
        float subSp = DailyUpdatesConfigHelper.pageSubtitleTextSizeSp(host);
        float cardTitle = DailyUpdatesConfigHelper.cardTitleTextSizeSp(host);
        float cardBody = DailyUpdatesConfigHelper.cardBodyTextSizeSp(host);
        float cardCta = DailyUpdatesConfigHelper.cardCtaTextSizeSp(host);
        float cardHero = DailyUpdatesConfigHelper.cardHeroTextSizeSp(host);
        float stockTitle = DailyUpdatesConfigHelper.stockTitleTextSizeSp(host);
        float stockBody = DailyUpdatesConfigHelper.stockBodyTextSizeSp(host);
        float stockCta = DailyUpdatesConfigHelper.stockCtaTextSizeSp(host);
        float flashChip = DailyUpdatesConfigHelper.flashChipTextSizeSp(host);
        float flashHead = DailyUpdatesConfigHelper.flashHeadlineTextSizeSp(host);

        setSp(R.id.du_title, titleSp);
        setSp(R.id.du_subtitle, subSp);

        setSp(R.id.du_weather_title, cardTitle);
        setSp(R.id.du_fuel_title, cardTitle);
        setSp(R.id.du_gold_title, cardTitle);
        setSp(R.id.du_ttd_title, cardTitle);
        setSp(R.id.du_jobs_title, cardTitle);
        setSp(R.id.du_panchangam_title, cardTitle);
        setSp(R.id.du_rashi_title, cardTitle);

        setSp(R.id.du_weather_temp, cardHero);
        setSp(R.id.du_jobs_count, cardHero);

        setSp(R.id.du_weather_condition, Math.max(8f, cardBody - 1f));
        setSp(R.id.du_weather_summary, Math.max(8f, cardBody - 2f));
        setSp(R.id.du_fuel_petrol, cardBody);
        setSp(R.id.du_fuel_diesel, cardBody);
        setSp(R.id.du_fuel_petrol_change, Math.max(8f, cardBody - 2f));
        setSp(R.id.du_fuel_diesel_change, Math.max(8f, cardBody - 2f));
        setSp(R.id.du_gold_24k, cardBody);
        setSp(R.id.du_gold_silver, cardBody);
        setSp(R.id.du_gold_24k_change, Math.max(8f, cardBody - 2f));
        setSp(R.id.du_gold_silver_change, Math.max(8f, cardBody - 2f));
        setSp(R.id.du_ttd_bullets, Math.max(8f, cardBody - 1f));
        setSp(R.id.du_jobs_label, cardBody);
        setSp(R.id.du_panchangam_copy, Math.max(8f, cardBody - 1f));
        setSp(R.id.du_rashi_copy, Math.max(8f, cardBody - 1f));

        setSp(R.id.du_weather_details, cardCta);
        setSp(R.id.du_fuel_details, cardCta);
        setSp(R.id.du_gold_details, cardCta);
        setSp(R.id.du_ttd_details, cardCta);
        setSp(R.id.du_jobs_details, cardCta);
        setSp(R.id.du_panchangam_details, cardCta);
        setSp(R.id.du_rashi_details, cardCta);

        setSp(R.id.du_stock_title, stockTitle);
        setSp(R.id.du_stock_subtitle, Math.max(8f, stockBody - 2f));
        setSp(R.id.du_stock_nifty, stockBody);
        setSp(R.id.du_stock_sensex, stockBody);
        setSp(R.id.du_stock_details, stockCta);

        setSp(R.id.du_flash_chip, flashChip);
        setSp(R.id.du_flash_headline, flashHead);
        setSp(R.id.du_flash_more, Math.max(8f, flashChip - 1f));
    }

    private void setSp(int id, float sp) {
        TextView tv = root.findViewById(id);
        if (tv != null) {
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        }
    }

    private void applyCardContentStarts() {
        float d = host.getResources().getDisplayMetrics().density;
        // Horizontal content-start: paddingStart absolute (clears corner art + admin nudge).
        setContentPadStart(R.id.du_weather_content,
                DailyUpdatesConfigHelper.weatherContentStartDp(host), d);
        setContentPadStart(R.id.du_fuel_content,
                DailyUpdatesConfigHelper.fuelContentStartDp(host), d);
        setContentPadStart(R.id.du_gold_content,
                DailyUpdatesConfigHelper.goldContentStartDp(host), d);
        setContentPadStart(R.id.du_ttd_content,
                DailyUpdatesConfigHelper.ttdContentStartDp(host), d);
        setContentPadStart(R.id.du_jobs_content,
                DailyUpdatesConfigHelper.jobsContentStartDp(host), d);
        setContentPadStart(R.id.du_panchangam_content,
                DailyUpdatesConfigHelper.panchangamContentStartDp(host), d);
        setContentPadStart(R.id.du_rashi_content,
                DailyUpdatesConfigHelper.rashiContentStartDp(host), d);
        setContentPadStart(R.id.du_stock_content,
                DailyUpdatesConfigHelper.stockContentStartDp(host), d);
    }

    /** Admin {@code *_content_start_dp} → content column paddingStart (LTR). */
    private void setContentPadStart(int id, int startDp, float density) {
        View v = root.findViewById(id);
        if (v == null) return;
        int startPx = Math.max(0, (int) (startDp * density));
        v.setPaddingRelative(
                startPx,
                v.getPaddingTop(),
                v.getPaddingEnd(),
                v.getPaddingBottom());
        v.setTranslationX(0f);
    }

    private void applyFlashChrome() {
        float d = host.getResources().getDisplayMetrics().density;
        int sizeDp = DailyUpdatesConfigHelper.flashIconSizeDp(host);
        int marginStartDp = DailyUpdatesConfigHelper.flashIconMarginStartDp(host);
        int padStartDp = DailyUpdatesConfigHelper.flashContentPadStartDp(host);

        ImageView flashBg = root.findViewById(R.id.du_flash_bg);
        if (flashBg != null) {
            ViewGroup.LayoutParams lp = flashBg.getLayoutParams();
            int px = Math.max(1, (int) (sizeDp * d));
            lp.width = px;
            // Keep height match_parent; vertical margins keep bolt from clipping.
            flashBg.setLayoutParams(lp);
            if (lp instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
                mlp.setMarginStart((int) (marginStartDp * d));
                flashBg.setLayoutParams(mlp);
            }
            flashBg.setScaleType(ImageView.ScaleType.FIT_CENTER);
            flashBg.setImageMatrix(null);
        }

        View flashContent = root.findViewById(R.id.du_flash_content);
        if (flashContent != null) {
            flashContent.setPadding(
                    (int) (padStartDp * d),
                    flashContent.getPaddingTop(),
                    flashContent.getPaddingRight(),
                    flashContent.getPaddingBottom());
        }
    }

    private void bindLabels() {
        TextView title = root.findViewById(R.id.du_title);
        TextView subtitle = root.findViewById(R.id.du_subtitle);
        TextView flash = root.findViewById(R.id.du_flash_chip);

        if (title != null) {
            title.setText(FooterTabLabels.dailyUpdates(host));
        }
        if (subtitle != null) {
            subtitle.setText(dailyUpdatesSubtitle());
        }
        if (flash != null) {
            flash.setText(flashNewsLabel());
        }
        TextView headline = root.findViewById(R.id.du_flash_headline);
        if (headline != null) {
            headline.setSelected(true);
        }
        TextView flashMore = root.findViewById(R.id.du_flash_more);
        if (flashMore != null) flashMore.setText(moreLabel());
        TextView stockDetails = root.findViewById(R.id.du_stock_details);
        if (stockDetails != null) stockDetails.setText(seeDetailsLabel());

        setTextIfPresent(R.id.du_weather_title, weatherCardTitle());
        setTextIfPresent(R.id.du_weather_details, seeDetailsLabel());
        setTextIfPresent(R.id.du_fuel_title, fuelCardTitle());
        setTextIfPresent(R.id.du_fuel_details, seeDetailsLabel());
        setTextIfPresent(R.id.du_gold_title, goldCardTitle());
        setTextIfPresent(R.id.du_gold_details, seeDetailsLabel());
        setTextIfPresent(R.id.du_ttd_title, ttdCardTitle());
        setTextIfPresent(R.id.du_ttd_details, seeDetailsLabel());
        setTextIfPresent(R.id.du_jobs_title, jobsCardTitle());
        setTextIfPresent(R.id.du_jobs_details, seeDetailsLabel());
        setTextIfPresent(R.id.du_panchangam_title, panchangamCardTitle());
        setTextIfPresent(R.id.du_panchangam_details, seeDetailsLabel());
        setTextIfPresent(R.id.du_rashi_title, rashiCardTitle());
        setTextIfPresent(R.id.du_rashi_details, seeDetailsLabel());

        bindManaLabels();
        syncBellVisibility();
    }

    private void setTextIfPresent(int id, @NonNull String text) {
        TextView tv = root.findViewById(id);
        if (tv != null) tv.setText(text);
    }

    /** Bell only when Android system notifications for this app are disabled. */
    private void syncBellVisibility() {
        View wrap = root.findViewById(R.id.du_bell_wrap);
        View spacer = root.findViewById(R.id.du_header_end_spacer);
        boolean systemOn = isSystemNotificationsEnabled();
        if (wrap != null) {
            // INVISIBLE (not GONE) when system notifs are on — keeps title centered vs end spacer.
            wrap.setVisibility(systemOn ? View.INVISIBLE : View.VISIBLE);
        }
        if (spacer != null) {
            spacer.setVisibility(View.INVISIBLE);
        }
    }

    private boolean isSystemNotificationsEnabled() {
        try {
            return NotificationManagerCompat.from(host).areNotificationsEnabled();
        } catch (Exception e) {
            return true;
        }
    }

    private void openAppNotificationSettings() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, host.getPackageName());
                host.startActivity(intent);
                return;
            }
        } catch (Exception ignored) {
        }
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + host.getPackageName()));
            host.startActivity(intent);
        } catch (Exception ignored) {
        }
    }

    private void bindManaLabels() {
        TextView manaTitle = root.findViewById(R.id.du_mana_title);
        TextView manaChange = root.findViewById(R.id.du_mana_change);
        TextView manaHeadline = root.findViewById(R.id.du_mana_headline_label);
        if (manaTitle != null) {
            String district = DistrictLabelHelper.resolveDisplayLabel(host);
            if (!DistrictLabelHelper.isUsable(district)) {
                district = manaZillaLabel();
            }
            // "జిల్లా: అనకాపల్లి · లోకల్ వార్తలు" — district + local news, not bare name.
            manaTitle.setText(districtTitleWithLocal(district));
        }
        if (manaChange != null) {
            manaChange.setText(changeDistrictLabel());
        }
        if (manaHeadline != null) {
            manaHeadline.setText(todaysMainNewsLabel());
            float sp = DailyUpdatesConfigHelper.manaHeadlineTextSizeSp(host);
            manaHeadline.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, sp);
        }
        applyManaHeadlineOffsets();
        applyManaStateBackground();
    }

    private void applyManaStateBackground() {
        ImageView bg = root.findViewById(R.id.du_mana_bg);
        if (bg == null) return;
        try {
            bg.setImageResource(DailyUpdatesConfigHelper.manaZillaBgResId(host));
        } catch (Exception ignored) {
        }
    }

    /** Shift headline+bullets right/down so they clear the state map art. */
    private void applyManaHeadlineOffsets() {
        View block = root.findViewById(R.id.du_mana_news_block);
        if (block == null) return;
        float density = host.getResources().getDisplayMetrics().density;
        int basePadStart = (int) (72 * density); // clears map better than old 56dp
        int x = DailyUpdatesConfigHelper.manaHeadlineOffsetXDp(host);
        int y = DailyUpdatesConfigHelper.manaHeadlineOffsetYDp(host);
        block.setPadding(
                basePadStart + (int) (x * density),
                (int) Math.max(0, y * density),
                block.getPaddingEnd(),
                block.getPaddingBottom());
        block.setTranslationY(y < 0 ? y * density : 0f);
    }

    @NonNull
    private String districtTitleWithLocal(@NonNull String district) {
        switch (languageId()) {
            case 1: return "ജില്ല: " + district + " · ലോക്കൽ വാർത്തകൾ";
            case 2: return "जिला: " + district + " · लोकल खबरें";
            case 3: return "జిల్లా: " + district + " · లోకల్ వార్తలు";
            case 4: return "மாவட்டம்: " + district + " · உள்ளூர் செய்திகள்";
            case 5: return "ಜಿಲ್ಲೆ: " + district + " · ಸ್ಥಳೀಯ ಸುದ್ದಿ";
            case 6: return "ଜିଲ୍ଲା: " + district + " · ସ୍ଥାନୀୟ ଖବର";
            default: return "District: " + district + " · Local news";
        }
    }

    private void bindSummariesAndGrid() {
        bindWeatherCard();
        bindFuelCard();
        bindGoldCard();
        bindTtdCard();
        bindJobsCard();
        reflowGrid();
    }

    private void bindManaZilla() {
        View mana = root.findViewById(R.id.du_mana_zilla);
        if (mana != null) {
            mana.setVisibility(DailyUpdatesConfigHelper.isManaZillaEnabled(host)
                    ? View.VISIBLE : View.GONE);
        }
        if (!DailyUpdatesConfigHelper.isManaZillaEnabled(host)) {
            return;
        }
        bindManaLabels();
        paintCachedManaTitles();
        fetchManaHeadlines(false);
    }

    private void bindFlashStripVisibility() {
        View flash = root.findViewById(R.id.du_flash_strip);
        if (flash != null) {
            flash.setVisibility(DailyUpdatesConfigHelper.isFlashNewsEnabled(host)
                    ? View.VISIBLE : View.GONE);
        }
        if (DailyUpdatesConfigHelper.isFlashNewsEnabled(host)) {
            ImageView flashBg = root.findViewById(R.id.du_flash_bg);
            if (flashBg != null) {
                flashBg.setScaleType(ImageView.ScaleType.FIT_CENTER);
                flashBg.setImageMatrix(null);
            }
            TextView headline = root.findViewById(R.id.du_flash_headline);
            if (headline != null) {
                String[] heads = DailyUpdatesConfigHelper.flashHeadlines(host);
                if (heads != null && heads.length > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < heads.length && i < 6; i++) {
                        if (i > 0) sb.append(" · ");
                        sb.append(heads[i]);
                    }
                    headline.setText(sb.toString());
                }
                headline.setSelected(true);
            }
        }
    }

    private void bindStockStrip() {
        View strip = root.findViewById(R.id.du_stock_strip);
        if (strip == null) return;
        boolean on = DailyUpdatesConfigHelper.isStockEnabled(host);
        strip.setVisibility(on ? View.VISIBLE : View.GONE);
        if (!on) return;

        float density = host.getResources().getDisplayMetrics().density;
        int offsetY = DailyUpdatesConfigHelper.stockOffsetYDp(host);
        ViewGroup.MarginLayoutParams lp =
                (ViewGroup.MarginLayoutParams) strip.getLayoutParams();
        if (lp != null) {
            lp.topMargin = (int) ((8 + offsetY) * density);
            strip.setLayoutParams(lp);
        }

        // Content column paddingStart from admin (stock_content_start_dp) — also in applyCardContentStarts.
        View content = root.findViewById(R.id.du_stock_content);
        if (content != null) {
            int startDp = DailyUpdatesConfigHelper.stockContentStartDp(host);
            content.setPaddingRelative(
                    (int) (Math.max(0, startDp) * density),
                    content.getPaddingTop(),
                    content.getPaddingEnd(),
                    content.getPaddingBottom());
        }

        TextView title = root.findViewById(R.id.du_stock_title);
        if (title != null) title.setText(DailyUpdatesConfigHelper.stockTitle(host));
        TextView subtitle = root.findViewById(R.id.du_stock_subtitle);
        if (subtitle != null) subtitle.setText(DailyUpdatesConfigHelper.stockSubtitle(host));

        // Left-only candlestick art — never stretch under the text column.
        ImageView bg = root.findViewById(R.id.du_stock_bg);
        if (bg != null) {
            bg.setScaleType(ImageView.ScaleType.FIT_CENTER);
            bg.setImageMatrix(null);
            bg.setTranslationX(0f);
        }

        paintStockSummary(DailyUpdatesConfigHelper.stockSummaryJson(host));
        refreshStockSummaryLive();
    }

    private void paintStockSummary(@Nullable org.json.JSONObject o) {
        TextView nifty = root.findViewById(R.id.du_stock_nifty);
        TextView sensex = root.findViewById(R.id.du_stock_sensex);
        String niftyLabel = indexLabelNifty();
        String sensexLabel = indexLabelSensex();
        if (o == null) {
            if (nifty != null) nifty.setText(niftyLabel + " —");
            if (sensex != null) sensex.setText(sensexLabel + " —");
            return;
        }
        paintStockIndexLine(nifty, niftyLabel, o.optJSONObject("nifty"));
        paintStockIndexLine(sensex, sensexLabel, o.optJSONObject("sensex"));
    }

    private void paintStockIndexLine(@Nullable TextView tv, @NonNull String label,
                                     @Nullable org.json.JSONObject q) {
        if (tv == null) return;
        if (q == null || !q.has("price") || q.isNull("price")) {
            tv.setText(label + " —");
            tv.setTextColor(Color.WHITE);
            return;
        }
        double price = q.optDouble("price", Double.NaN);
        if (Double.isNaN(price)) {
            tv.setText(label + " —");
            return;
        }
        double ch = q.optDouble("change", 0);
        boolean up = ch >= 0;
        String arrow = up ? "▲" : "▼";
        // Single line — avoids hairline / vertical clip on the right-hand text column.
        tv.setText(String.format(Locale.US, "%s %s %s",
                label,
                String.format(Locale.US, "%,.0f", price),
                arrow));
        tv.setTextColor(Color.parseColor(up ? "#FF4ADE80" : "#FFF87171"));
    }

    private void refreshStockSummaryLive() {
        try {
            APIService api = RestApiClient.getClient().create(APIService.class);
            api.stockMarket("v3", "indices", null, null).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call,
                                       @NonNull Response<JsonObject> response) {
                    if (response.body() == null) return;
                    JsonObject body = response.body();
                    if (body.has("enabled") && body.get("enabled").isJsonPrimitive()
                            && !body.get("enabled").getAsBoolean()) {
                        View strip = root.findViewById(R.id.du_stock_strip);
                        if (strip != null) strip.setVisibility(View.GONE);
                        return;
                    }
                    if (!body.has("data") || !body.get("data").isJsonObject()) return;
                    JsonObject data = body.getAsJsonObject("data");
                    org.json.JSONObject pack = new org.json.JSONObject();
                    try {
                        if (data.has("indices") && data.get("indices").isJsonArray()) {
                            for (com.google.gson.JsonElement el : data.getAsJsonArray("indices")) {
                                if (!el.isJsonObject()) continue;
                                JsonObject q = el.getAsJsonObject();
                                String key = q.has("key") ? q.get("key").getAsString() : "";
                                org.json.JSONObject row = new org.json.JSONObject(q.toString());
                                if ("nifty".equals(key)) pack.put("nifty", row);
                                if ("sensex".equals(key)) pack.put("sensex", row);
                            }
                        }
                        pack.put("enabled", true);
                        DailyUpdatesConfigHelper.saveStockSummaryFromSplash(host, pack);
                        paintStockSummary(pack);
                    } catch (Exception ignored) {
                    }
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    /* keep splash cache */
                }
            });
        } catch (Exception ignored) {
        }
    }

    private void applyCardBackgroundArt() {
        // Full-bleed du_bg_* art fills each card; single border = foreground stroke.
        // Do not matrix-crop / left-slice — that created the hard vertical seam.
        // Stock uses a left-only ImageView (not full-bleed) — skip here.
        final int[] cardArtIds = {
                R.id.du_card_weather,
                R.id.du_card_fuel,
                R.id.du_card_gold,
                R.id.du_card_ttd,
                R.id.du_card_jobs,
                R.id.du_card_panchangam,
                R.id.du_card_rashi,
        };
        for (int id : cardArtIds) {
            ImageView art = root.findViewById(id);
            if (art == null) continue;
            art.setScaleType(ImageView.ScaleType.FIT_XY);
            art.setImageMatrix(null);
            art.setTranslationX(0f);
        }
        ImageView stockBg = root.findViewById(R.id.du_stock_bg);
        if (stockBg != null) {
            stockBg.setScaleType(ImageView.ScaleType.FIT_CENTER);
            stockBg.setImageMatrix(null);
            stockBg.setTranslationX(0f);
        }
        // Flash ticker keeps a compact left bolt — not a grid card.
        ImageView flashBg = root.findViewById(R.id.du_flash_bg);
        if (flashBg != null) {
            flashBg.setScaleType(ImageView.ScaleType.FIT_CENTER);
            flashBg.setImageMatrix(null);
        }
    }

    private void startLiveBlink() {
        if (liveBlink != null) {
            liveBlink.cancel();
            liveBlink = null;
        }
        TextView manaLive = root.findViewById(R.id.du_mana_live);
        TextView flashLive = root.findViewById(R.id.du_flash_live);
        TextView primary = manaLive != null && manaLive.getVisibility() == View.VISIBLE
                ? manaLive : flashLive;
        if (primary == null) return;
        if (manaLive != null) manaLive.setAlpha(1f);
        if (flashLive != null) flashLive.setAlpha(1f);
        liveBlink = ObjectAnimator.ofFloat(primary, View.ALPHA, 1f, 0.2f, 1f);
        liveBlink.setDuration(1100L);
        liveBlink.setRepeatCount(ValueAnimator.INFINITE);
        liveBlink.setInterpolator(new LinearInterpolator());
        liveBlink.addUpdateListener(a -> {
            float alpha = (Float) a.getAnimatedValue();
            if (manaLive != null
                    && DailyUpdatesConfigHelper.isManaZillaEnabled(host)) {
                manaLive.setAlpha(alpha);
            }
            if (flashLive != null
                    && DailyUpdatesConfigHelper.isFlashNewsEnabled(host)) {
                flashLive.setAlpha(alpha);
            }
        });
        liveBlink.start();
    }

    private void paintCachedManaTitles() {
        int distId = DistrictLabelHelper.getStoredDistId(host);
        // Prefer splash Redis/memory local headlines when dist matches.
        String[] splash = DailyUpdatesConfigHelper.localHeadlines(host);
        int splashDist = SharedPrefsData.getInt(
                host, Constants.DU_LOCAL_HEADLINES_DIST, Constants.PREF_NAME);
        if (splash != null && splash.length > 0
                && (splashDist <= 0 || splashDist == distId || distId <= 0)) {
            applyManaTitles(
                    splash.length > 0 ? splash[0] : null,
                    splash.length > 1 ? splash[1] : null);
            return;
        }
        int cachedDist = SharedPrefsData.getInt(
                host, CACHE_DIST, Constants.PREF_NAME);
        String t1 = SharedPrefsData.getString(host, CACHE_T1, Constants.PREF_NAME);
        String t2 = SharedPrefsData.getString(host, CACHE_T2, Constants.PREF_NAME);
        // Instant UX: show stale cache for this district while network refreshes.
        if (distId > 0 && cachedDist == distId
                && (!TextUtils.isEmpty(t1) || !TextUtils.isEmpty(t2))) {
            applyManaTitles(t1, t2);
        } else if (!TextUtils.isEmpty(t1) || !TextUtils.isEmpty(t2)) {
            applyManaTitles(t1, t2);
        } else {
            applyManaTitles(null, null);
        }
    }

    private void applyManaTitles(@Nullable String t1, @Nullable String t2) {
        TextView n1 = root.findViewById(R.id.du_mana_news1);
        TextView n2 = root.findViewById(R.id.du_mana_news2);
        boolean has1 = !TextUtils.isEmpty(t1);
        boolean has2 = !TextUtils.isEmpty(t2);
        if (n1 != null) {
            if (has1) {
                n1.setText("• " + t1.trim());
                n1.setVisibility(View.VISIBLE);
            } else {
                n1.setText("");
                n1.setVisibility(View.GONE);
            }
        }
        if (n2 != null) {
            if (has2) {
                n2.setText("• " + t2.trim());
                n2.setVisibility(View.VISIBLE);
            } else {
                n2.setText("");
                n2.setVisibility(View.GONE);
            }
        }
    }

    private void saveManaCache(@Nullable String t1, @Nullable String t2, int distId) {
        SharedPrefsData.putString(host, CACHE_T1, t1 != null ? t1 : "", Constants.PREF_NAME);
        SharedPrefsData.putString(host, CACHE_T2, t2 != null ? t2 : "", Constants.PREF_NAME);
        SharedPrefsData.putInt(host, CACHE_DIST, distId, Constants.PREF_NAME);
        SharedPrefsData.putString(
                host, CACHE_AT, String.valueOf(System.currentTimeMillis()), Constants.PREF_NAME);
    }

    private void fetchManaHeadlines(boolean force) {
        if (manaFetchInFlight && !force) return;
        final int seq = ++manaFetchSeq;
        manaFetchInFlight = true;
        if (manaFetchCall != null) {
            try {
                manaFetchCall.cancel();
            } catch (Exception ignored) {
            }
            manaFetchCall = null;
        }

        try {
            ListAllArticlesRequest requestModel = new ListAllArticlesRequest();
            requestModel.setDraftId(SharedPrefsData.getInt(
                    host, Constants.DraftID, Constants.PREF_NAME));
            // Same filter as Local news feed (district desk).
            requestModel.setCategoryId(-1);
            requestModel.setLanguageId(SharedPrefsData.getInt(
                    host, Constants.LanguageID, Constants.PREF_NAME));
            requestModel.setSkip(0L);
            requestModel.setLastPostId(0L);
            requestModel.setPushPostId(0L);
            requestModel.setSharePostId(0L);
            requestModel.setActualSkip(0L);
            requestModel.setSpecial_posts(false);
            requestModel.setVideo_posts(false);
            requestModel.setPush_posts(false);
            DisplayMetrics dm = host.getResources().getDisplayMetrics();
            requestModel.setScreenHeight(dm.heightPixels);
            requestModel.setScreenWidth(dm.widthPixels);
            requestModel.setUser_session_id(SharedPrefsData.getString(
                    host, Constants.USER_SESSION_ID, Constants.PREF_NAME));

            String jsonString = new Gson().toJson(requestModel);
            EncritonRequestData enc = new EncritonRequestData();
            enc.setData(encrypt(jsonString));
            JsonObject body = new Gson().toJsonTree(enc).getAsJsonObject();

            APIService api = RestApiClient.getClient().create(APIService.class);
            manaFetchCall = api.List_All_Articles("v3", body);
            manaFetchCall.enqueue(new Callback<EncreptionRequest>() {
                @Override
                public void onResponse(@NonNull Call<EncreptionRequest> call,
                                       @NonNull Response<EncreptionRequest> response) {
                    if (seq != manaFetchSeq) return;
                    manaFetchInFlight = false;
                    manaFetchCall = null;
                    if (!response.isSuccessful() || response.body() == null
                            || TextUtils.isEmpty(response.body().getData())) {
                        return;
                    }
                    try {
                        String plain = decrypt(response.body().getData());
                        ModelClass model = new Gson().fromJson(plain, ModelClass.class);
                        String[] titles = pickTwoTitles(model);
                        int distId = DistrictLabelHelper.getStoredDistId(host);
                        saveManaCache(titles[0], titles[1], distId);
                        host.runOnUiThread(() -> {
                            if (seq != manaFetchSeq) return;
                            applyManaTitles(titles[0], titles[1]);
                        });
                    } catch (Exception ignored) {
                    }
                }

                @Override
                public void onFailure(@NonNull Call<EncreptionRequest> call,
                                      @NonNull Throwable t) {
                    if (seq != manaFetchSeq) return;
                    manaFetchInFlight = false;
                    manaFetchCall = null;
                }
            });
        } catch (Exception e) {
            manaFetchInFlight = false;
        }
    }

    @NonNull
    private static String[] pickTwoTitles(@Nullable ModelClass model) {
        String[] out = new String[]{null, null};
        if (model == null || model.getData() == null) return out;
        int i = 0;
        for (ModelClass.Datum d : model.getData()) {
            if (d == null) continue;
            if (d.isAd_check()) continue;
            String pt = d.getPostType();
            if (pt != null) {
                String p = pt.trim().toLowerCase(Locale.US);
                if ("ad".equals(p) || "web_ads".equals(p) || "share_app".equals(p)
                        || "rating".equals(p) || "end".equals(p)) {
                    continue;
                }
            }
            String title = d.getTitle();
            if (TextUtils.isEmpty(title)) continue;
            title = title.trim();
            if (title.isEmpty()) continue;
            out[i++] = title;
            if (i >= 2) break;
        }
        return out;
    }

    private void bindWeatherCard() {
        TextView temp = root.findViewById(R.id.du_weather_temp);
        TextView condition = root.findViewById(R.id.du_weather_condition);
        TextView summary = root.findViewById(R.id.du_weather_summary);
        if (temp != null) {
            temp.setText(LocalUpdatesConfigHelper.weatherTempC(host) + "°C");
        }
        if (condition != null) {
            condition.setText(LocalUpdatesConfigHelper.weatherCondition(host));
        }
        if (summary != null) {
            summary.setText(
                    highLabel() + " " + LocalUpdatesConfigHelper.weatherHighC(host) + "°C"
                            + " | " + lowLabel() + " " + LocalUpdatesConfigHelper.weatherLowC(host) + "°C"
                            + " | " + humidityLabel() + " "
                            + LocalUpdatesConfigHelper.weatherHumidity(host) + "%");
        }
    }

    private void bindFuelCard() {
        TextView petrol = root.findViewById(R.id.du_fuel_petrol);
        TextView petrolCh = root.findViewById(R.id.du_fuel_petrol_change);
        TextView diesel = root.findViewById(R.id.du_fuel_diesel);
        TextView dieselCh = root.findViewById(R.id.du_fuel_diesel_change);
        double p = LocalUpdatesConfigHelper.fuelPetrol(host);
        double d = LocalUpdatesConfigHelper.fuelDiesel(host);
        if (petrol != null) {
            if (p > 0) {
                petrol.setText("పెట్రోల్  ₹"
                        + LocalUpdatesConfigHelper.formatMoney2(p));
            } else {
                petrol.setText("పెట్రోల్  —");
            }
        }
        if (diesel != null) {
            if (d > 0) {
                diesel.setText("డీజిల్  ₹"
                        + LocalUpdatesConfigHelper.formatMoney2(d));
            } else {
                diesel.setText("డీజిల్  —");
            }
        }
        if (p > 0) {
            setTrend(petrolCh, LocalUpdatesConfigHelper.fuelPetrolChange(host), false, true);
        } else if (petrolCh != null) {
            petrolCh.setText("—");
            petrolCh.setTextColor(Color.parseColor("#FF94A3B8"));
        }
        if (d > 0) {
            setTrend(dieselCh, LocalUpdatesConfigHelper.fuelDieselChange(host), false, true);
        } else if (dieselCh != null) {
            dieselCh.setText("—");
            dieselCh.setTextColor(Color.parseColor("#FF94A3B8"));
        }
    }

    private void bindTtdCard() {
        TextView bullets = root.findViewById(R.id.du_ttd_bullets);
        if (bullets != null) {
            bullets.setText(TtdConfigHelper.outsideCardBullets(host));
        }
    }

    private void bindJobsCard() {
        TextView count = root.findViewById(R.id.du_jobs_count);
        if (count != null) {
            count.setText(String.valueOf(LocalUpdatesConfigHelper.jobsTotalCount(host)));
        }
    }

    private void bindGoldCard() {
        TextView g24 = root.findViewById(R.id.du_gold_24k);
        TextView g24ch = root.findViewById(R.id.du_gold_24k_change);
        TextView silver = root.findViewById(R.id.du_gold_silver);
        TextView silverCh = root.findViewById(R.id.du_gold_silver_change);
        boolean real = LocalUpdatesConfigHelper.hasRealGoldRates(host);
        double g10 = LocalUpdatesConfigHelper.gold24k10g(host);
        double skg = LocalUpdatesConfigHelper.silverKg(host);
        if (g24 != null) {
            if (real && g10 > 0) {
                g24.setText("24K  ₹" + LocalUpdatesConfigHelper.formatInr(g10));
            } else {
                g24.setText("24K  —");
            }
        }
        if (silver != null) {
            if (real && skg > 0) {
                silver.setText("వెండి  ₹" + LocalUpdatesConfigHelper.formatInr(skg));
            } else {
                silver.setText("వెండి  —");
            }
        }
        if (real) {
            setTrend(g24ch, LocalUpdatesConfigHelper.gold24kChange(host), true, false);
            setTrend(silverCh, LocalUpdatesConfigHelper.silverKgChange(host), true, false);
        } else {
            if (g24ch != null) {
                g24ch.setText("—");
                g24ch.setTextColor(Color.parseColor("#FF94A3B8"));
            }
            if (silverCh != null) {
                silverCh.setText("—");
                silverCh.setTextColor(Color.parseColor("#FF94A3B8"));
            }
        }
    }

    private void setTrend(@Nullable TextView tv, double change, boolean whole,
                          boolean greenWhenDown) {
        if (tv == null) return;
        boolean up = change >= 0;
        String arrow = up ? "▲ " : "▼ ";
        String num = whole
                ? String.format(Locale.US, "%.0f", Math.abs(change))
                : LocalUpdatesConfigHelper.formatMoney2(Math.abs(change));
        tv.setText(arrow + num);
        if (greenWhenDown) {
            tv.setTextColor(Color.parseColor("#FF4ADE80"));
        } else {
            tv.setTextColor(Color.parseColor(up ? "#FF4ADE80" : "#FFF87171"));
        }
    }

    private void reflowGrid() {
        LinearLayout grid = root.findViewById(R.id.du_grid);
        if (grid == null) return;

        View weather = root.findViewById(R.id.du_card_weather_wrap);
        View fuel = root.findViewById(R.id.du_card_fuel_wrap);
        View gold = root.findViewById(R.id.du_card_gold_wrap);
        View ttd = root.findViewById(R.id.du_card_ttd_wrap);
        View jobs = root.findViewById(R.id.du_card_jobs_wrap);
        View panchangam = root.findViewById(R.id.du_card_panchangam_wrap);
        View rashi = root.findViewById(R.id.du_card_rashi_wrap);

        List<View> visible = new ArrayList<>(7);
        if (weather != null && LocalUpdatesConfigHelper.isWeatherEnabled(host)) {
            visible.add(weather);
        }
        if (fuel != null && LocalUpdatesConfigHelper.isFuelEnabled(host)) {
            visible.add(fuel);
        }
        if (gold != null && LocalUpdatesConfigHelper.isGoldEnabled(host)) {
            visible.add(gold);
        }
        if (ttd != null && TtdConfigHelper.isEnabled(host)) {
            visible.add(ttd);
        }
        if (jobs != null && JobsConfigHelper.isEnabled(host)) {
            visible.add(jobs);
        }
        if (panchangam != null && DailyUpdatesConfigHelper.isPanchangamEnabled(host)) {
            visible.add(panchangam);
        }
        if (rashi != null && DailyUpdatesConfigHelper.isRashiEnabled(host)) {
            visible.add(rashi);
        }

        View[] all = {weather, fuel, gold, ttd, jobs, panchangam, rashi};
        for (View v : all) {
            if (v == null) continue;
            v.setVisibility(View.GONE);
            if (v.getParent() instanceof ViewGroup) {
                ((ViewGroup) v.getParent()).removeView(v);
            }
        }
        grid.removeAllViews();

        float density = host.getResources().getDisplayMetrics().density;
        int cardH = (int) (120 * density);
        int m = (int) (4 * density);

        for (int i = 0; i < visible.size(); i += 2) {
            LinearLayout row = new LinearLayout(host);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setWeightSum(2f);
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(rowLp);

            for (int j = 0; j < 2; j++) {
                if (i + j < visible.size()) {
                    View card = visible.get(i + j);
                    card.setVisibility(View.VISIBLE);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            0, cardH, 1f);
                    lp.setMargins(m, m, m, m);
                    card.setLayoutParams(lp);
                    row.addView(card);
                } else {
                    View spacer = new View(host);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            0, 1, 1f);
                    spacer.setLayoutParams(lp);
                    row.addView(spacer);
                }
            }
            grid.addView(row);
        }
        // Admin *_content_start_dp → paddingStart (must run after reparent).
        applyCardContentStarts();
        applyCardBackgroundArt();
        applyRoundedCardClips();
    }

    private void applyRoundedCardClips() {
        int[] ids = {
                R.id.du_card_weather_wrap,
                R.id.du_card_fuel_wrap,
                R.id.du_card_gold_wrap,
                R.id.du_card_ttd_wrap,
                R.id.du_card_jobs_wrap,
                R.id.du_card_panchangam_wrap,
                R.id.du_card_rashi_wrap,
                R.id.du_mana_zilla,
                R.id.du_flash_strip,
                R.id.du_stock_strip
        };
        for (int id : ids) {
            View v = root.findViewById(id);
            if (v == null) continue;
            v.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
            v.setClipToOutline(true);
            if (v instanceof ViewGroup) {
                ((ViewGroup) v).setClipChildren(true);
                ((ViewGroup) v).setClipToPadding(true);
            }
        }
    }

    private void wireClicks() {
        View panchangamCard = root.findViewById(R.id.du_card_panchangam_wrap);
        if (panchangamCard != null) {
            panchangamCard.setOnClickListener(v -> {
                if (!DailyUpdatesConfigHelper.isPanchangamEnabled(host)) return;
                if (callbacks != null) {
                    callbacks.openPanchangamFromDailyUpdates();
                } else {
                    toast(panchangamCardTitle());
                }
            });
        }

        View rashiCard = root.findViewById(R.id.du_card_rashi_wrap);
        if (rashiCard != null) {
            rashiCard.setOnClickListener(v -> {
                if (!DailyUpdatesConfigHelper.isRashiEnabled(host)) return;
                if (callbacks != null) {
                    callbacks.openRashiFromDailyUpdates();
                } else {
                    toast(rashiCardTitle());
                }
            });
        }

        openFeature(R.id.du_card_weather_wrap, () -> LocalUpdatesConfigHelper.isWeatherEnabled(host),
                WeatherDetailActivity.class, "Weather");
        openFeature(R.id.du_card_fuel_wrap, () -> LocalUpdatesConfigHelper.isFuelEnabled(host),
                FuelDetailActivity.class, "Fuel Prices");
        openFeature(R.id.du_card_gold_wrap, () -> LocalUpdatesConfigHelper.isGoldEnabled(host),
                GoldDetailActivity.class, "Gold & Silver");
        openFeature(R.id.du_card_jobs_wrap, () -> JobsConfigHelper.isEnabled(host),
                JobsPageActivity.class, "Jobs");

        View ttd = root.findViewById(R.id.du_card_ttd_wrap);
        if (ttd != null) {
            ttd.setOnClickListener(v -> {
                try {
                    if (TtdConfigHelper.isEnabled(host)) {
                        host.startActivity(new Intent(host, TtdDetailActivity.class));
                    }
                } catch (Exception e) {
                    toast("TTD");
                }
            });
        }

        View.OnClickListener openLocal = v -> {
            if (callbacks != null) {
                callbacks.openLocalNewsFromDailyUpdates();
            } else {
                toast(manaZillaLabel());
            }
        };
        View manaCard = root.findViewById(R.id.du_mana_zilla);
        if (manaCard != null) manaCard.setOnClickListener(openLocal);
        View n1 = root.findViewById(R.id.du_mana_news1);
        if (n1 != null) n1.setOnClickListener(openLocal);
        View n2 = root.findViewById(R.id.du_mana_news2);
        if (n2 != null) n2.setOnClickListener(openLocal);
        View headline = root.findViewById(R.id.du_mana_headline_label);
        if (headline != null) headline.setOnClickListener(openLocal);

        View change = root.findViewById(R.id.du_mana_change);
        View changeRow = root.findViewById(R.id.du_mana_change_row);
        View.OnClickListener openDistrictPicker = v -> showChangeDistrictDialog();
        if (changeRow != null) {
            changeRow.setClickable(true);
            changeRow.setFocusable(true);
            changeRow.setOnClickListener(openDistrictPicker);
        }
        if (change != null) {
            // Must consume the tap so Mana Zilla card does not open Local news.
            change.setClickable(true);
            change.setFocusable(true);
            change.setOnClickListener(openDistrictPicker);
        }
        View changeIcon = root.findViewById(R.id.du_mana_change_icon);
        if (changeIcon != null) {
            changeIcon.setOnClickListener(openDistrictPicker);
        }

        View bell = root.findViewById(R.id.du_btn_bell);
        if (bell != null) {
            bell.setOnClickListener(v -> openAppNotificationSettings());
        }
        View bellWrap = root.findViewById(R.id.du_bell_wrap);
        if (bellWrap != null) {
            bellWrap.setOnClickListener(v -> openAppNotificationSettings());
        }

        View.OnClickListener openFlash = v -> {
            if (!DailyUpdatesConfigHelper.isFlashNewsEnabled(host)) return;
            if (callbacks != null) {
                callbacks.openFlashNewsFromDailyUpdates();
            } else {
                toast(FooterTabLabels.breaking(host));
            }
        };
        View flashStrip = root.findViewById(R.id.du_flash_strip);
        if (flashStrip != null) flashStrip.setOnClickListener(openFlash);
        View flashChip = root.findViewById(R.id.du_flash_chip);
        if (flashChip != null) flashChip.setOnClickListener(openFlash);

        View.OnClickListener openStock = v -> {
            if (!DailyUpdatesConfigHelper.isStockEnabled(host)) return;
            try {
                host.startActivity(new Intent(host, StockMarketActivity.class));
            } catch (Exception e) {
                toast("Stock Market");
            }
        };
        View stockStrip = root.findViewById(R.id.du_stock_strip);
        if (stockStrip != null) stockStrip.setOnClickListener(openStock);
        View stockDetails = root.findViewById(R.id.du_stock_details);
        if (stockDetails != null) stockDetails.setOnClickListener(openStock);
        View flashMore = root.findViewById(R.id.du_flash_more);
        if (flashMore != null) flashMore.setOnClickListener(openFlash);
    }

    private void showChangeDistrictDialog() {
        final int languageId = SharedPrefsData.getInt(host, Constants.LanguageID, Constants.PREF_NAME);
        if (languageId <= 0) {
            toast("Language not set");
            return;
        }
        int stateId = SharedPrefsData.getInt(host, Constants.StateID, Constants.PREF_NAME);
        if (stateId <= 0) {
            int distId = DistrictLabelHelper.getStoredDistId(host);
            stateId = SelectionListCache.findStateIdForDistrict(host, languageId, distId);
            if (stateId > 0) {
                SharedPrefsData.putInt(host, Constants.StateID, stateId, Constants.PREF_NAME);
            }
        }
        if (stateId > 0) {
            loadAndPresentDistricts(languageId, stateId);
            return;
        }
        // Splash often saves district without StateID — discover state via states list.
        discoverStateThenPresentDistricts(languageId);
    }

    private void loadAndPresentDistricts(final int languageId, final int stateId) {
        List<DistricsModel.Datum> cached =
                SelectionListCache.loadDistricts(host, languageId, stateId);
        if (cached != null && !cached.isEmpty()) {
            presentDistrictPicker(new ArrayList<>(cached));
            return;
        }
        fetchDistrictsFromNetwork(languageId, stateId);
    }

    private void fetchDistrictsFromNetwork(final int languageId, final int stateId) {
        try {
            LanguageSelectionRequest requestModel = new LanguageSelectionRequest();
            requestModel.setDraftId(SharedPrefsData.getInt(
                    host, Constants.DraftID, Constants.PREF_NAME));
            requestModel.setLanguageId(languageId);
            requestModel.setStateId(stateId);
            EncritonRequestData enc = new EncritonRequestData();
            enc.setData(encrypt(new Gson().toJson(requestModel)));
            JsonObject body = new Gson().toJsonTree(enc).getAsJsonObject();
            APIService api = RestApiClient.getClient().create(APIService.class);
            api.Get_districts("v3", body).enqueue(new Callback<EncreptionRequest>() {
                @Override
                public void onResponse(@NonNull Call<EncreptionRequest> call,
                                       @NonNull Response<EncreptionRequest> response) {
                    if (!response.isSuccessful() || response.body() == null
                            || TextUtils.isEmpty(response.body().getData())) {
                        host.runOnUiThread(() -> toast("Could not load districts"));
                        return;
                    }
                    try {
                        String plain = decrypt(response.body().getData());
                        DistricsModel model = new Gson().fromJson(plain, DistricsModel.class);
                        if (model != null && model.getData() != null && !model.getData().isEmpty()) {
                            if (stateId > 0) {
                                SelectionListCache.saveDistricts(host, languageId, stateId, model);
                            }
                            host.runOnUiThread(() -> presentDistrictPicker(
                                    new ArrayList<>(model.getData())));
                        } else {
                            host.runOnUiThread(() -> toast("No districts found"));
                        }
                    } catch (Exception e) {
                        host.runOnUiThread(() -> toast("Could not load districts"));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<EncreptionRequest> call,
                                      @NonNull Throwable t) {
                    host.runOnUiThread(() -> toast("Could not load districts"));
                }
            });
        } catch (Exception e) {
            toast("Could not load districts");
        }
    }

    /**
     * When StateID was never persisted (splash-only district), load states for the
     * language and find which state owns the selected district, then show picker.
     */
    private void discoverStateThenPresentDistricts(final int languageId) {
        List<com.anyNews.anynews.models.StatesModel.Datum> cachedStates =
                SelectionListCache.loadStates(host, languageId);
        if (cachedStates != null && !cachedStates.isEmpty()) {
            resolveStateFromStatesList(languageId, new ArrayList<>(cachedStates));
            return;
        }
        try {
            LanguageSelectionRequest requestModel = new LanguageSelectionRequest();
            requestModel.setDraftId(SharedPrefsData.getInt(
                    host, Constants.DraftID, Constants.PREF_NAME));
            requestModel.setLanguageId(languageId);
            EncritonRequestData enc = new EncritonRequestData();
            enc.setData(encrypt(new Gson().toJson(requestModel)));
            JsonObject body = new Gson().toJsonTree(enc).getAsJsonObject();
            APIService api = RestApiClient.getClient().create(APIService.class);
            api.Get_states_requiest("v3", body).enqueue(new Callback<EncreptionRequest>() {
                @Override
                public void onResponse(@NonNull Call<EncreptionRequest> call,
                                       @NonNull Response<EncreptionRequest> response) {
                    if (!response.isSuccessful() || response.body() == null
                            || TextUtils.isEmpty(response.body().getData())) {
                        // Last resort: server Get_districts without state uses first state.
                        host.runOnUiThread(() -> fetchDistrictsFromNetwork(languageId, 0));
                        return;
                    }
                    try {
                        String plain = decrypt(response.body().getData());
                        com.anyNews.anynews.models.StatesModel model =
                                new Gson().fromJson(plain, com.anyNews.anynews.models.StatesModel.class);
                        if (model != null && model.getData() != null && !model.getData().isEmpty()) {
                            SelectionListCache.saveStates(host, languageId, model);
                            host.runOnUiThread(() -> resolveStateFromStatesList(
                                    languageId, new ArrayList<>(model.getData())));
                        } else {
                            host.runOnUiThread(() -> fetchDistrictsFromNetwork(languageId, 0));
                        }
                    } catch (Exception e) {
                        host.runOnUiThread(() -> fetchDistrictsFromNetwork(languageId, 0));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<EncreptionRequest> call,
                                      @NonNull Throwable t) {
                    host.runOnUiThread(() -> fetchDistrictsFromNetwork(languageId, 0));
                }
            });
        } catch (Exception e) {
            fetchDistrictsFromNetwork(languageId, 0);
        }
    }

    private void resolveStateFromStatesList(
            final int languageId,
            @NonNull List<com.anyNews.anynews.models.StatesModel.Datum> states) {
        final int distId = DistrictLabelHelper.getStoredDistId(host);
        // Prefer cache hits for the selected district's state.
        if (distId > 0) {
            for (com.anyNews.anynews.models.StatesModel.Datum st : states) {
                if (st == null || st.getId() == null || st.getId() <= 0) continue;
                List<DistricsModel.Datum> list =
                        SelectionListCache.loadDistricts(host, languageId, st.getId());
                if (list != null) {
                    for (DistricsModel.Datum d : list) {
                        if (d != null && d.getId() != null && d.getId() == distId) {
                            SharedPrefsData.putInt(
                                    host, Constants.StateID, st.getId(), Constants.PREF_NAME);
                            presentDistrictPicker(new ArrayList<>(list));
                            return;
                        }
                    }
                }
            }
        }
        // Fetch districts for the first state (same fallback as server), then show.
        // If multiple states, still open a real picker — user can change within that state;
        // subsequent opens use persisted StateID after pick+Update_district.
        com.anyNews.anynews.models.StatesModel.Datum first = null;
        for (com.anyNews.anynews.models.StatesModel.Datum st : states) {
            if (st != null && st.getId() != null && st.getId() > 0) {
                first = st;
                break;
            }
        }
        if (first == null) {
            toast("Could not load districts");
            return;
        }
        // If we know distId, probe states via network until found (cap few).
        if (distId > 0) {
            probeStatesForDistrict(languageId, states, distId, 0);
            return;
        }
        SharedPrefsData.putInt(host, Constants.StateID, first.getId(), Constants.PREF_NAME);
        loadAndPresentDistricts(languageId, first.getId());
    }

    private void probeStatesForDistrict(
            final int languageId,
            @NonNull final List<com.anyNews.anynews.models.StatesModel.Datum> states,
            final int distId,
            final int index) {
        if (index >= states.size()) {
            // Not found — open first state's list so user still gets a picker.
            for (com.anyNews.anynews.models.StatesModel.Datum st : states) {
                if (st != null && st.getId() != null && st.getId() > 0) {
                    SharedPrefsData.putInt(
                            host, Constants.StateID, st.getId(), Constants.PREF_NAME);
                    loadAndPresentDistricts(languageId, st.getId());
                    return;
                }
            }
            toast("Could not load districts");
            return;
        }
        final com.anyNews.anynews.models.StatesModel.Datum st = states.get(index);
        if (st == null || st.getId() == null || st.getId() <= 0) {
            probeStatesForDistrict(languageId, states, distId, index + 1);
            return;
        }
        final int stateId = st.getId();
        List<DistricsModel.Datum> cached =
                SelectionListCache.loadDistricts(host, languageId, stateId);
        if (cached != null && !cached.isEmpty()) {
            for (DistricsModel.Datum d : cached) {
                if (d != null && d.getId() != null && d.getId() == distId) {
                    SharedPrefsData.putInt(
                            host, Constants.StateID, stateId, Constants.PREF_NAME);
                    presentDistrictPicker(new ArrayList<>(cached));
                    return;
                }
            }
            probeStatesForDistrict(languageId, states, distId, index + 1);
            return;
        }
        try {
            LanguageSelectionRequest requestModel = new LanguageSelectionRequest();
            requestModel.setDraftId(SharedPrefsData.getInt(
                    host, Constants.DraftID, Constants.PREF_NAME));
            requestModel.setLanguageId(languageId);
            requestModel.setStateId(stateId);
            EncritonRequestData enc = new EncritonRequestData();
            enc.setData(encrypt(new Gson().toJson(requestModel)));
            JsonObject body = new Gson().toJsonTree(enc).getAsJsonObject();
            APIService api = RestApiClient.getClient().create(APIService.class);
            api.Get_districts("v3", body).enqueue(new Callback<EncreptionRequest>() {
                @Override
                public void onResponse(@NonNull Call<EncreptionRequest> call,
                                       @NonNull Response<EncreptionRequest> response) {
                    try {
                        if (response.isSuccessful() && response.body() != null
                                && !TextUtils.isEmpty(response.body().getData())) {
                            DistricsModel model = new Gson().fromJson(
                                    decrypt(response.body().getData()), DistricsModel.class);
                            if (model != null && model.getData() != null
                                    && !model.getData().isEmpty()) {
                                SelectionListCache.saveDistricts(
                                        host, languageId, stateId, model);
                                for (DistricsModel.Datum d : model.getData()) {
                                    if (d != null && d.getId() != null
                                            && d.getId() == distId) {
                                        SharedPrefsData.putInt(host, Constants.StateID,
                                                stateId, Constants.PREF_NAME);
                                        host.runOnUiThread(() -> presentDistrictPicker(
                                                new ArrayList<>(model.getData())));
                                        return;
                                    }
                                }
                            }
                        }
                    } catch (Exception ignored) {
                    }
                    host.runOnUiThread(() ->
                            probeStatesForDistrict(languageId, states, distId, index + 1));
                }

                @Override
                public void onFailure(@NonNull Call<EncreptionRequest> call,
                                      @NonNull Throwable t) {
                    host.runOnUiThread(() ->
                            probeStatesForDistrict(languageId, states, distId, index + 1));
                }
            });
        } catch (Exception e) {
            probeStatesForDistrict(languageId, states, distId, index + 1);
        }
    }

    private void presentDistrictPicker(@NonNull List<DistricsModel.Datum> districts) {
        if (host.isFinishing()) return;
        final String[] names = new String[districts.size()];
        for (int i = 0; i < districts.size(); i++) {
            DistricsModel.Datum d = districts.get(i);
            names[i] = d != null && !TextUtils.isEmpty(d.getName()) ? d.getName() : "";
        }
        try {
            new AlertDialog.Builder(host)
                    .setTitle(changeDistrictLabel())
                    .setItems(names, (dialog, which) -> {
                        if (which < 0 || which >= districts.size()) return;
                        DistricsModel.Datum picked = districts.get(which);
                        if (picked == null || picked.getId() == null) return;
                        persistDistrictSelection(picked.getId(), picked.getName());
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        } catch (Exception e) {
            toast("Could not open district list");
        }
    }

    private void persistDistrictSelection(int distId, @Nullable String name) {
        // Optimistic local save so UI / Local feed use the new district immediately.
        DistrictLabelHelper.saveSelected(host, distId, name);
        SharedPrefsData.putBool(host, Constants.IS_LANGUAGE_SELECTED, true, Constants.PREF_NAME);
        // Soft-fill StateID if splash never persisted it (Update_district also syncs server).
        try {
            int lang = languageId();
            int sid = SelectionListCache.findStateIdForDistrict(host, lang, distId);
            if (sid > 0) {
                SharedPrefsData.putInt(host, Constants.StateID, sid, Constants.PREF_NAME);
            }
        } catch (Exception ignored) {
        }
        // Drop warm OpenPath / profile cache so Home + Profile cannot re-paint old district.
        try {
            LanguageFeedInvalidator.onDistrictChanged(host);
        } catch (Exception ignored) {
        }
        // Invalidate mana cache for previous district.
        SharedPrefsData.putInt(host, CACHE_DIST, 0, Constants.PREF_NAME);
        applyManaTitles(null, null);
        bindManaLabels();
        fetchManaHeadlines(true);

        try {
            LanguageSelectionRequest requestModel = new LanguageSelectionRequest();
            requestModel.setDraftId(SharedPrefsData.getInt(
                    host, Constants.DraftID, Constants.PREF_NAME));
            requestModel.setDistId(distId);
            EncritonRequestData enc = new EncritonRequestData();
            enc.setData(encrypt(new Gson().toJson(requestModel)));
            JsonObject body = new Gson().toJsonTree(enc).getAsJsonObject();
            APIService api = RestApiClient.getClient().create(APIService.class);
            api.Update_district("v3", body).enqueue(new Callback<EncreptionRequest>() {
                @Override
                public void onResponse(@NonNull Call<EncreptionRequest> call,
                                       @NonNull Response<EncreptionRequest> response) {
                    if (!response.isSuccessful() || response.body() == null
                            || TextUtils.isEmpty(response.body().getData())) {
                        return;
                    }
                    try {
                        String plain = decrypt(response.body().getData());
                        UpdateStateResponse model =
                                new Gson().fromJson(plain, UpdateStateResponse.class);
                        if (model != null && Boolean.TRUE.equals(model.getSuccess())) {
                            DistrictLabelHelper.saveSelected(host, distId, name);
                        }
                    } catch (Exception ignored) {
                    }
                }

                @Override
                public void onFailure(@NonNull Call<EncreptionRequest> call,
                                      @NonNull Throwable t) {
                    // Prefs already updated locally; server sync can retry later.
                }
            });
        } catch (Exception ignored) {
        }
    }

    private interface EnabledCheck {
        boolean get();
    }

    private void openFeature(int id, @NonNull EnabledCheck enabled,
                             @NonNull Class<?> activity, @NonNull String label) {
        View v = root.findViewById(id);
        if (v == null) return;
        v.setOnClickListener(view -> {
            if (!enabled.get()) return;
            try {
                host.startActivity(new Intent(host, activity));
            } catch (Exception e) {
                toast(label);
            }
        });
    }

    private void toast(@NonNull String label) {
        try {
            Toast.makeText(host, label, Toast.LENGTH_SHORT).show();
        } catch (Exception ignored) {
        }
    }

    private int languageId() {
        try {
            return SharedPrefsData.getInt(
                    host.getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
        } catch (Exception ignored) {
            return 0;
        }
    }

    @NonNull
    private String manaZillaLabel() {
        switch (languageId()) {
            case 1: return "നമ്മുടെ ജില്ല";
            case 2: return "हमारा जिला";
            case 3: return "మన జిల్లా";
            case 4: return "நமது மாவட்டம்";
            case 5: return "ನಮ್ಮ ಜಿಲ್ಲೆ";
            case 6: return "ଆମ ଜିଲ୍ଲା";
            default: return "My District";
        }
    }

    @NonNull
    private String changeDistrictLabel() {
        switch (languageId()) {
            case 1: return "ജില്ല മാറ്റുക";
            case 2: return "जिला बदलें";
            case 3: return "జిల్లా మార్చండి";
            case 4: return "மாவட்டம் மாற்று";
            case 5: return "ಜಿಲ್ಲೆ ಬದಲಿಸಿ";
            case 6: return "ଜିଲ୍ଲା ବଦଳାନ୍ତୁ";
            default: return "Change district";
        }
    }

    @NonNull
    private String todaysMainNewsLabel() {
        switch (languageId()) {
            case 1: return "ഇന്നത്തെ പ്രധാന വാർത്തകൾ";
            case 2: return "आज की मुख्य खबरें";
            case 3: return "ఈరోజు ముఖ్య వార్తలు";
            case 4: return "இன்றைய முக்கிய செய்திகள்";
            case 5: return "ಇಂದಿನ ಮುಖ್ಯ ಸುದ್ದಿಗಳು";
            case 6: return "ଆଜିର ମୁଖ୍ୟ ଖବର";
            default: return "Today's main news";
        }
    }

    @NonNull
    private String dailyUpdatesSubtitle() {
        switch (languageId()) {
            case 1: return "ദൈനംദിന പ്രധാന വിവരങ്ങൾ";
            case 2: return "दैनिक मुख्य जानकारी";
            case 3: return "రోజువారీ ముఖ్య సమాచారం";
            case 4: return "தினசரி முக்கிய தகவல்";
            case 5: return "ದೈನಂದಿನ ಮುಖ್ಯ ಮಾಹಿತಿ";
            case 6: return "ଦୈନିକ ମୁଖ୍ୟ ସୂଚନା";
            default: return "Daily key information";
        }
    }

    @NonNull
    private String flashNewsLabel() {
        return FlashNewsChipHelper.chipLabel(host);
    }

    @NonNull
    private String seeDetailsLabel() {
        switch (languageId()) {
            case 1: return "വിശദാംശങ്ങൾ →";
            case 2: return "विवरण देखें →";
            case 3: return "వివరాలు చూడండి →";
            case 4: return "விவரங்கள் →";
            case 5: return "ವಿವರಗಳು →";
            case 6: return "ବିବରଣୀ →";
            default: return "See details →";
        }
    }

    @NonNull
    private String moreLabel() {
        switch (languageId()) {
            case 1: return "കൂടുതൽ →";
            case 2: return "और →";
            case 3: return "మరిన్ని →";
            case 4: return "மேலும் →";
            case 5: return "ಇನ್ನಷ್ಟು →";
            case 6: return "ଅଧିକ →";
            default: return "More →";
        }
    }

    @NonNull
    private String weatherCardTitle() {
        switch (languageId()) {
            case 1: return "കാലാവസ്ഥ";
            case 2: return "मौसम";
            case 3: return "వాతావరణం";
            case 4: return "வானிலை";
            case 5: return "ಹವಾಮಾನ";
            case 6: return "ପାଣିପାଗ";
            default: return "Weather";
        }
    }

    @NonNull
    private String fuelCardTitle() {
        switch (languageId()) {
            case 1: return "പെട്രോൾ & ഡീസൽ";
            case 2: return "पेट्रोल और डीज़ल";
            case 3: return "పెట్రోల్ & డీజిల్";
            case 4: return "பெட்ரோல் & டீசல்";
            case 5: return "ಪೆಟ್ರೋಲ್ & ಡೀಸೆಲ್";
            case 6: return "ପେଟ୍ରୋଲ୍ ଓ ଡିଜେଲ୍";
            default: return "Petrol & Diesel";
        }
    }

    @NonNull
    private String goldCardTitle() {
        switch (languageId()) {
            case 1: return "സ്വർണം & വെള്ളി";
            case 2: return "सोना और चांदी";
            case 3: return "బంగారం & వెండి";
            case 4: return "தங்கம் & வெள்ளி";
            case 5: return "ಚಿನ್ನ & ಬೆಳ್ಳಿ";
            case 6: return "ସୁନା ଓ ରୂପା";
            default: return "Gold & Silver";
        }
    }

    @NonNull
    private String ttdCardTitle() {
        switch (languageId()) {
            case 1: return "ടിടിഡി വിവരം";
            case 2: return "टीटीडी जानकारी";
            case 3: return "టీటీడీ సమాచారం";
            case 4: return "டிடிடி தகவல்";
            case 5: return "ಟಿಟಿಡಿ ಮಾಹಿತಿ";
            case 6: return "ଟିଟିଡି ସୂଚନା";
            default: return "TTD Info";
        }
    }

    @NonNull
    private String jobsCardTitle() {
        switch (languageId()) {
            case 1: return "ജോലികൾ";
            case 2: return "नौकरियां";
            case 3: return "ఉద్యోగాలు";
            case 4: return "வேலைகள்";
            case 5: return "ಉದ್ಯೋಗಗಳು";
            case 6: return "ଚାକିରି";
            default: return "Jobs";
        }
    }

    @NonNull
    private String panchangamCardTitle() {
        switch (languageId()) {
            case 1: return "പഞ്ചാംഗം";
            case 2: return "पंचांग";
            case 3: return "పంచాంగం";
            case 4: return "பஞ்சாங்கம்";
            case 5: return "ಪಂಚಾಂಗ";
            case 6: return "ପଞ୍ଚାଙ୍ଗ";
            default: return "Panchangam";
        }
    }

    @NonNull
    private String rashiCardTitle() {
        switch (languageId()) {
            case 1: return "രാശിഫലം";
            case 2: return "राशिफल";
            case 3: return "రాశిఫలాలు";
            case 4: return "ராசிபலன்";
            case 5: return "ರಾಶಿಫಲ";
            case 6: return "ରାଶିଫଳ";
            default: return "Rashi Phalalu";
        }
    }

    @NonNull
    private String highLabel() {
        switch (languageId()) {
            case 1: return "ഉയർന്നത്";
            case 2: return "अधिकतम";
            case 3: return "గరిష్టం";
            case 4: return "அதிகபட்சம்";
            case 5: return "ಗರಿಷ್ಠ";
            case 6: return "ସର୍ବାଧିକ";
            default: return "High";
        }
    }

    @NonNull
    private String lowLabel() {
        switch (languageId()) {
            case 1: return "താഴ്ന്നത്";
            case 2: return "न्यूनतम";
            case 3: return "కనిష్టం";
            case 4: return "குறைந்தபட்சம்";
            case 5: return "ಕನಿಷ್ಠ";
            case 6: return "ସର୍ବନିମ୍ନ";
            default: return "Low";
        }
    }

    @NonNull
    private String humidityLabel() {
        switch (languageId()) {
            case 1: return "ആർദ്രത";
            case 2: return "नमी";
            case 3: return "తేమ";
            case 4: return "ஈரப்பதம்";
            case 5: return "ಆರ್ದ್ರತೆ";
            case 6: return "ଆର୍ଦ୍ରତା";
            default: return "Humidity";
        }
    }

    @NonNull
    private String indexLabelNifty() {
        return "Nifty";
    }

    @NonNull
    private String indexLabelSensex() {
        return "Sensex";
    }
}
