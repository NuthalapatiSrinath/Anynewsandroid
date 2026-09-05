package com.anyNews.anynews.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Outline;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.JobsConfigHelper;
import com.anyNews.anynews.Utils.JobsLabels;
import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Jobs hub — Daily Updates card → this activity.
 * Data from splash cache + optional /get_jobs refresh. Empty until admin posts.
 */
public class JobsPageActivity extends AppCompatActivity {

    private LinearLayout listContainer;
    private TextView emptyView;
    private String selectedCategory = "all";
    private String searchQuery = "";
    private final List<JSONObject> allItems = new ArrayList<>();

    private static final int[] PLACEHOLDER_LOGOS = {
            R.drawable.jobs_company_01,
            R.drawable.jobs_company_02,
            R.drawable.jobs_company_03,
            R.drawable.jobs_company_04,
            R.drawable.jobs_company_05,
            R.drawable.jobs_company_06,
            R.drawable.jobs_company_07,
            R.drawable.jobs_company_08,
            R.drawable.jobs_company_09,
            R.drawable.jobs_company_10,
            R.drawable.jobs_company_11,
            R.drawable.jobs_company_12,
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!JobsConfigHelper.isEnabled(this)) {
            Toast.makeText(this, JobsLabels.pageTitle(this), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setContentView(R.layout.activity_jobs_page);
        applySystemBars();
        clipBanner();
        listContainer = findViewById(R.id.jobs_list);
        emptyView = findViewById(R.id.jobs_empty);
        bindChrome();
        wireClicks();
        loadFromCache();
        refreshFromNetwork();
    }

    private void applySystemBars() {
        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#060B18"));
        window.setNavigationBarColor(Color.parseColor("#060B18"));
        WindowInsetsControllerCompat c =
                WindowCompat.getInsetsController(window, window.getDecorView());
        if (c != null) {
            c.setAppearanceLightStatusBars(false);
            c.setAppearanceLightNavigationBars(false);
        }
        View root = findViewById(R.id.jobs_root);
        if (root == null) return;
        final int padL = root.getPaddingLeft();
        final int padT = root.getPaddingTop();
        final int padR = root.getPaddingRight();
        final int padB = root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(padL + bars.left, padT + bars.top, padR + bars.right, padB + bars.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private void clipBanner() {
        View frame = findViewById(R.id.jobs_banner_frame);
        if (frame == null) return;
        final float radius = 14f * getResources().getDisplayMetrics().density;
        frame.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
            }
        });
        frame.setClipToOutline(true);
    }

    private void bindChrome() {
        setText(R.id.jobs_top_title, JobsLabels.pageTitle(this));
        setText(R.id.jobs_banner_title, JobsLabels.bannerTitle(this));
        setText(R.id.jobs_banner_subtitle, JobsLabels.bannerSubtitle(this));
        setText(R.id.jobs_filter_label, JobsLabels.filter(this));
        setText(R.id.jobs_stat_total_label, JobsLabels.statTotal(this));
        setText(R.id.jobs_stat_companies_label, JobsLabels.statCompanies(this));
        setText(R.id.jobs_stat_locations_label, JobsLabels.statLocations(this));
        setText(R.id.jobs_stat_new_label, JobsLabels.statNew(this));
        setText(R.id.jobs_stat_new_sub, JobsLabels.today(this));
        setText(R.id.jobs_list_title, JobsLabels.latestJobs(this));
        setText(R.id.jobs_sort_label, JobsLabels.sortLatest(this));
        setText(R.id.jobs_see_more_label, JobsLabels.seeMore(this));
        if (emptyView != null) emptyView.setText(JobsLabels.empty(this));
        EditText search = findViewById(R.id.jobs_search);
        if (search != null) {
            search.setHint(JobsLabels.searchHint(this));
            search.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
                @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
                @Override
                public void afterTextChanged(Editable s) {
                    searchQuery = s != null ? s.toString().trim().toLowerCase() : "";
                    renderList();
                }
            });
        }
        rebuildCategories();
    }

    private void wireClicks() {
        View back = findViewById(R.id.jobs_back);
        if (back != null) back.setOnClickListener(v -> finish());
        View share = findViewById(R.id.jobs_share);
        if (share != null) {
            share.setOnClickListener(v -> {
                try {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, JobsLabels.pageTitle(this));
                    i.putExtra(Intent.EXTRA_TEXT, JobsLabels.bannerTitle(this)
                            + "\n" + JobsLabels.bannerSubtitle(this));
                    startActivity(Intent.createChooser(i, JobsLabels.pageTitle(this)));
                } catch (Exception ignored) {
                }
            });
        }
        View filter = findViewById(R.id.jobs_filter_btn);
        if (filter != null) {
            filter.setOnClickListener(v -> showCategoryPicker());
        }
        View more = findViewById(R.id.jobs_see_more);
        if (more != null) {
            more.setOnClickListener(v -> {
                selectedCategory = "all";
                EditText search = findViewById(R.id.jobs_search);
                if (search != null) search.setText("");
                searchQuery = "";
                rebuildCategories();
                renderList();
            });
        }
    }

    private void loadFromCache() {
        JSONObject payload = JobsConfigHelper.jobsJson(this);
        applyPayload(payload);
    }

    private void refreshFromNetwork() {
        try {
            APIService api = RestApiClient.getClient().create(APIService.class);
            JsonObject body = new JsonObject();
            body.addProperty("limit", 100);
            Call<JsonObject> call = api.get_jobs("v3", body);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call,
                                       @NonNull Response<JsonObject> response) {
                    try {
                        if (!response.isSuccessful() || response.body() == null) return;
                        JsonObject root = response.body();
                        if (!root.has("success") || !root.get("success").getAsBoolean()) return;
                        if (!root.has("data")) return;
                        JSONObject data = new JSONObject(root.get("data").toString());
                        JobsConfigHelper.savePayloadJson(JobsPageActivity.this, data);
                        applyPayload(data);
                    } catch (Exception ignored) {
                    }
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    // Keep splash cache.
                }
            });
        } catch (Exception ignored) {
        }
    }

    private void applyPayload(@Nullable JSONObject payload) {
        allItems.clear();
        JSONObject stats = JobsConfigHelper.emptyStats();
        if (payload != null) {
            JSONObject s = payload.optJSONObject("stats");
            if (s != null) stats = s;
            JSONArray items = payload.optJSONArray("items");
            if (items != null) {
                for (int i = 0; i < items.length(); i++) {
                    JSONObject j = items.optJSONObject(i);
                    if (j != null) allItems.add(j);
                }
            }
        }
        setText(R.id.jobs_stat_total, String.valueOf(stats.optInt("total_jobs", allItems.size())));
        setText(R.id.jobs_stat_companies, String.valueOf(stats.optInt("total_companies", 0)));
        setText(R.id.jobs_stat_locations, String.valueOf(stats.optInt("total_locations", 0)));
        setText(R.id.jobs_stat_new, String.valueOf(stats.optInt("new_today", 0)));
        renderList();
    }

    private void rebuildCategories() {
        LinearLayout row = findViewById(R.id.jobs_categories);
        if (row == null) return;
        row.removeAllViews();
        // No "More/మరిన్ని" chip — all categories in the horizontal scroller.
        // Zip has a More Categories asset; deliberately unused.
        addCategoryChip(row, "all", JobsLabels.catAll(this), 0, R.drawable.jobs_cat_all);
        addCategoryChip(row, "it", JobsLabels.catIt(this), 0, R.drawable.jobs_cat_it);
        addCategoryChip(row, "banking", JobsLabels.catBanking(this), 0, R.drawable.jobs_cat_banking);
        addCategoryChip(row, "teaching", JobsLabels.catTeaching(this), 0, R.drawable.jobs_cat_teaching);
        addCategoryChip(row, "healthcare", JobsLabels.catHealthcare(this), 0, R.drawable.jobs_cat_healthcare);
        addCategoryChip(row, "government", JobsLabels.catGovernment(this), 0, R.drawable.jobs_cat_government);
        addCategoryChip(row, "engineering", JobsLabels.catEngineering(this), 0, R.drawable.jobs_cat_engineering);
        addCategoryChip(row, "sales", JobsLabels.catSales(this), 0, R.drawable.jobs_cat_sales);
        addCategoryChip(row, "support", JobsLabels.catSupport(this), 0, R.drawable.jobs_cat_support);
        addCategoryChip(row, "manufacturing", JobsLabels.catManufacturing(this), 0, R.drawable.jobs_cat_manufacturing);
        addCategoryChip(row, "logistics", JobsLabels.catLogistics(this), 0, R.drawable.jobs_cat_logistics);
        addCategoryChip(row, "hospitality", JobsLabels.catHospitality(this), 0, R.drawable.jobs_cat_hospitality);
        addCategoryChip(row, "remote", JobsLabels.catRemote(this), 0, R.drawable.jobs_cat_remote);
    }

    private void addCategoryChip(@NonNull LinearLayout row, @NonNull String key,
                                 @NonNull String label, int bgRes, int iconRes) {
        float dens = getResources().getDisplayMetrics().density;
        LinearLayout chip = new LinearLayout(this);
        chip.setOrientation(LinearLayout.VERTICAL);
        chip.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                (int) (68 * dens), ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins((int) (4 * dens), 0, (int) (4 * dens), 0);
        chip.setLayoutParams(lp);

        FrameCircle iconWrap = new FrameCircle(this);
        int size = (int) (52 * dens);
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(size, size);
        iconWrap.setLayoutParams(ilp);
        iconWrap.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        });
        iconWrap.setClipToOutline(true);

        ImageView iv = new ImageView(this);
        FrameCircle.LayoutParams ivlp = new FrameCircle.LayoutParams(size, size);
        iv.setLayoutParams(ivlp);

        if (iconRes != 0) {
            // Zip category assets already include the circular artwork — fill chip, no nested padding.
            iv.setPadding(0, 0, 0, 0);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setImageResource(iconRes);
        } else {
            // "All" chip: solid blue circle + briefcase.
            iconWrap.setBackgroundResource(bgRes != 0 ? bgRes : R.drawable.bg_jobs_cat_all);
            int pad = (int) (12 * dens);
            iv.setPadding(pad, pad, pad, pad);
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            iv.setImageResource(R.drawable.ic_jobs_briefcase);
            iv.setColorFilter(Color.WHITE);
        }
        iconWrap.addView(iv);
        chip.addView(iconWrap);

        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextColor(key.equals(selectedCategory) ? Color.WHITE : Color.parseColor("#FF94A3B8"));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        tv.setGravity(Gravity.CENTER);
        tv.setMaxLines(2);
        tv.setPadding(0, (int) (4 * dens), 0, 0);
        chip.addView(tv);

        if (key.equals(selectedCategory)) {
            iconWrap.setAlpha(1f);
            iconWrap.setScaleX(1.06f);
            iconWrap.setScaleY(1.06f);
        } else {
            iconWrap.setAlpha(0.9f);
        }

        chip.setOnClickListener(v -> {
            selectedCategory = key;
            rebuildCategories();
            renderList();
        });
        row.addView(chip);
    }

    /** Tiny FrameLayout subclass name for LayoutParams typing. */
    private static final class FrameCircle extends android.widget.FrameLayout {
        FrameCircle(android.content.Context c) { super(c); }
    }

    private void showCategoryPicker() {
        String[] keys = {
                "all", "it", "banking", "teaching", "healthcare",
                "government", "engineering", "sales", "support",
                "manufacturing", "logistics", "hospitality", "remote"
        };
        String[] labels = {
                JobsLabels.catAll(this),
                JobsLabels.catIt(this),
                JobsLabels.catBanking(this),
                JobsLabels.catTeaching(this),
                JobsLabels.catHealthcare(this),
                JobsLabels.catGovernment(this),
                JobsLabels.catEngineering(this),
                JobsLabels.catSales(this),
                JobsLabels.catSupport(this),
                JobsLabels.catManufacturing(this),
                JobsLabels.catLogistics(this),
                JobsLabels.catHospitality(this),
                JobsLabels.catRemote(this),
        };
        new AlertDialog.Builder(this)
                .setTitle(JobsLabels.filter(this))
                .setItems(labels, (d, which) -> {
                    selectedCategory = keys[which];
                    rebuildCategories();
                    renderList();
                })
                .show();
    }

    private void renderList() {
        if (listContainer == null) return;
        listContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        int shown = 0;
        for (JSONObject job : allItems) {
            if (!matchesFilter(job)) continue;
            View card = inflater.inflate(R.layout.item_job_card, listContainer, false);
            bindJobCard(card, job);
            listContainer.addView(card);
            shown++;
        }
        if (emptyView != null) {
            emptyView.setVisibility(shown == 0 ? View.VISIBLE : View.GONE);
            emptyView.setText(JobsLabels.empty(this));
        }
        View seeMore = findViewById(R.id.jobs_see_more);
        if (seeMore != null) {
            // Hide CTA when there are no jobs to browse.
            seeMore.setVisibility(shown == 0 ? View.GONE : View.VISIBLE);
        }
    }

    private boolean matchesFilter(@NonNull JSONObject job) {
        if (!"all".equals(selectedCategory)) {
            String cat = job.optString("category", "");
            if (!selectedCategory.equalsIgnoreCase(cat)) return false;
        }
        if (TextUtils.isEmpty(searchQuery)) return true;
        String blob = (
                JobsConfigHelper.localized(this, job, "title_te", "title_en") + " "
                        + JobsConfigHelper.localized(this, job, "company_te", "company_en") + " "
                        + JobsConfigHelper.localized(this, job, "location_te", "location_en")
        ).toLowerCase();
        return blob.contains(searchQuery);
    }

    private void bindJobCard(@NonNull View card, @NonNull JSONObject job) {
        TextView title = card.findViewById(R.id.job_title);
        TextView company = card.findViewById(R.id.job_company);
        TextView location = card.findViewById(R.id.job_location);
        TextView experience = card.findViewById(R.id.job_experience);
        TextView salary = card.findViewById(R.id.job_salary);
        TextView time = card.findViewById(R.id.job_time);
        TextView badge = card.findViewById(R.id.job_new_badge);
        ImageView logo = card.findViewById(R.id.job_logo);
        ImageView verified = card.findViewById(R.id.job_verified);
        ImageView bookmark = card.findViewById(R.id.job_bookmark);

        if (title != null) {
            title.setText(JobsConfigHelper.localized(this, job, "title_te", "title_en"));
        }
        if (company != null) {
            company.setText(JobsConfigHelper.localized(this, job, "company_te", "company_en"));
        }
        if (location != null) {
            location.setText(JobsConfigHelper.localized(this, job, "location_te", "location_en"));
        }
        if (experience != null) {
            experience.setText(JobsConfigHelper.localized(this, job, "experience_te", "experience_en"));
        }
        if (salary != null) {
            salary.setText(JobsConfigHelper.localized(this, job, "salary_te", "salary_en"));
        }
        if (verified != null) {
            verified.setVisibility(job.optBoolean("verified", true) ? View.VISIBLE : View.GONE);
        }
        if (badge != null) {
            badge.setVisibility(job.optBoolean("is_new", false) ? View.VISIBLE : View.GONE);
        }
        long pubMs = 0L;
        String pub = job.optString("published_at", "");
        if (!TextUtils.isEmpty(pub)) {
            pubMs = parseIsoMs(pub);
        }
        if (time != null) {
            if (pubMs <= 0) pubMs = System.currentTimeMillis();
            time.setText(JobsLabels.timeAgo(this, pubMs));
        }
        if (logo != null) {
            String url = job.optString("logo_url", "");
            int ph = job.optInt("logo_placeholder", job.optInt("job_id", 1));
            int res = PLACEHOLDER_LOGOS[Math.max(0, (ph - 1) % PLACEHOLDER_LOGOS.length)];
            if (!TextUtils.isEmpty(url) && (url.startsWith("http://") || url.startsWith("https://"))) {
                Glide.with(this).load(url).placeholder(res).error(res).into(logo);
            } else {
                logo.setImageResource(res);
            }
        }
        if (bookmark != null) {
            bookmark.setOnClickListener(v ->
                    Toast.makeText(this, JobsLabels.pageTitle(this), Toast.LENGTH_SHORT).show());
        }
        String apply = job.optString("apply_url", "");
        card.setOnClickListener(v -> {
            if (TextUtils.isEmpty(apply)) return;
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(apply)));
            } catch (Exception ignored) {
            }
        });
    }

    private void setText(int id, @Nullable String text) {
        TextView tv = findViewById(id);
        if (tv != null) tv.setText(text != null ? text : "");
    }

    private static long parseIsoMs(@NonNull String iso) {
        try {
            String s = iso.trim();
            if (s.endsWith("Z")) {
                s = s.substring(0, s.length() - 1) + "+0000";
            } else if (s.length() > 5 && (s.charAt(s.length() - 3) == ':'
                    && (s.charAt(s.length() - 6) == '+' || s.charAt(s.length() - 6) == '-'))) {
                // +05:30 → +0530
                s = s.substring(0, s.length() - 3) + s.substring(s.length() - 2);
            }
            java.text.SimpleDateFormat[] formats = new java.text.SimpleDateFormat[] {
                    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", java.util.Locale.US),
                    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", java.util.Locale.US),
                    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US),
            };
            for (java.text.SimpleDateFormat f : formats) {
                f.setLenient(true);
                try {
                    java.util.Date d = f.parse(s);
                    if (d != null) return d.getTime();
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
        return 0L;
    }
}
