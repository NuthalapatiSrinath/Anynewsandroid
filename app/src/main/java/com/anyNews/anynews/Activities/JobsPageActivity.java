package com.anyNews.anynews.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Base64;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
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
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.JobsConfigHelper;
import com.anyNews.anynews.Utils.JobsLabels;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Udyogalu — Way2-style jobs feed.
 * Title only (no district). Cards: logo, title, time, description, salary, report/share/call.
 */
public class JobsPageActivity extends AppCompatActivity {

    public static final String EXTRA_FOCUS_JOB_ID = "EXTRA_FOCUS_JOB_ID";

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("(?:\\+?\\d[\\d\\s\\-]{8,}\\d)");

    private LinearLayout listContainer;
    private TextView emptyView;
    private String selectedCategory = "all";
    private String searchQuery = "";
    private final List<JSONObject> allItems = new ArrayList<>();
    private final List<JSONObject> categories = new ArrayList<>();
    /** Pin this job to the top when opened from a share deep link. */
    private int focusJobId = 0;

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
        setContentView(R.layout.activity_jobs_page);
        // Hub flag from splash — still show UI so local debug / offline works;
        // empty list when disabled or no posts.
        resolveFocusJobId();
        applySystemBars();
        listContainer = findViewById(R.id.jobs_list);
        emptyView = findViewById(R.id.jobs_empty);
        bindChrome();
        View back = findViewById(R.id.jobs_back);
        if (back != null) back.setOnClickListener(v -> finish());
        if (!JobsConfigHelper.isEnabled(this)) {
            if (emptyView != null) {
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText(JobsLabels.empty(this));
            }
        }
        loadFromCache();
        refreshFromNetwork();
    }

    /** Prefer intent extra, then sticky pref, then intent data URI (/job/{id}). */
    private void resolveFocusJobId() {
        try {
            focusJobId = getIntent() != null
                    ? getIntent().getIntExtra(EXTRA_FOCUS_JOB_ID, 0) : 0;
        } catch (Exception ignored) {
            focusJobId = 0;
        }
        if (focusJobId <= 0) {
            try {
                String pending = SharedPrefsData.getString(
                        getApplicationContext(), Constants.PENDING_SHARE_JOB, Constants.PREF_NAME);
                if (pending != null && !pending.trim().isEmpty() && !pending.equals("0")) {
                    focusJobId = Integer.parseInt(pending.trim().replaceAll("[^0-9]", ""));
                }
            } catch (Exception ignored) {
            }
        }
        if (focusJobId <= 0 && getIntent() != null && getIntent().getData() != null) {
            focusJobId = parseJobIdFromUri(getIntent().getData());
        }
        // Consume one-shot sticky so Reload / next open does not re-pin.
        SharedPrefsData.putString(
                getApplicationContext(), Constants.PENDING_SHARE_JOB, "", Constants.PREF_NAME);
    }

    private int parseJobIdFromUri(@Nullable Uri uri) {
        if (uri == null) return 0;
        try {
            String q = uri.getQueryParameter("jobId");
            if (TextUtils.isEmpty(q)) q = uri.getQueryParameter("job_id");
            if (!TextUtils.isEmpty(q) && q.matches("\\d+")) {
                return Integer.parseInt(q);
            }
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (scheme != null && "anynews".equalsIgnoreCase(scheme)
                    && host != null && "job".equalsIgnoreCase(host)) {
                String path = uri.getPath();
                if (path != null) {
                    String trimmed = path.startsWith("/") ? path.substring(1) : path;
                    int slash = trimmed.indexOf('/');
                    String idPart = slash >= 0 ? trimmed.substring(0, slash) : trimmed;
                    int id = decodeJobIdToken(idPart);
                    if (id > 0) return id;
                }
            }
            String path = uri.getPath();
            if (path == null || path.isEmpty()) return 0;
            String[] segments = path.split("/");
            for (int i = 0; i < segments.length; i++) {
                String seg = segments[i];
                if (seg == null || seg.isEmpty()) continue;
                if ("job".equalsIgnoreCase(seg) && i + 1 < segments.length) {
                    int id = decodeJobIdToken(segments[i + 1]);
                    if (id > 0) return id;
                }
                if (seg.length() > 4 && seg.regionMatches(true, 0, "job-", 0, 4)) {
                    int id = decodeJobIdToken(seg.substring(4));
                    if (id > 0) return id;
                }
            }
        } catch (Exception ignored) {
        }
        return 0;
    }

    private int decodeJobIdToken(@Nullable String token) {
        if (token == null || token.isEmpty()) return 0;
        String t = token.trim();
        if (t.matches("\\d+")) {
            try {
                return Integer.parseInt(t);
            } catch (Exception e) {
                return 0;
            }
        }
        try {
            byte[] raw = Base64.decode(t, Base64.DEFAULT);
            if (raw != null && raw.length > 0) {
                String decoded = new String(raw, StandardCharsets.UTF_8).trim();
                if (decoded.matches("\\d+")) {
                    return Integer.parseInt(decoded);
                }
            }
        } catch (Exception ignored) {
        }
        return 0;
    }

    /** Move the shared job to index 0 so the list opens on that exact card. */
    private void pinFocusJob() {
        if (focusJobId <= 0 || allItems.isEmpty()) return;
        for (int i = 0; i < allItems.size(); i++) {
            if (allItems.get(i).optInt("job_id", 0) == focusJobId) {
                if (i > 0) {
                    JSONObject job = allItems.remove(i);
                    allItems.add(0, job);
                }
                selectedCategory = "all";
                searchQuery = "";
                EditText search = findViewById(R.id.jobs_search);
                if (search != null && !TextUtils.isEmpty(search.getText())) {
                    search.setText("");
                }
                return;
            }
        }
    }

    private void applySystemBars() {
        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#FFF2F3F5"));
        window.setNavigationBarColor(Color.parseColor("#FFF2F3F5"));
        WindowInsetsControllerCompat c =
                WindowCompat.getInsetsController(window, window.getDecorView());
        if (c != null) {
            c.setAppearanceLightStatusBars(true);
            c.setAppearanceLightNavigationBars(true);
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

    private void bindChrome() {
        setText(R.id.jobs_top_title, JobsLabels.pageTitle(this));
        setText(R.id.jobs_warning_text, JobsLabels.feeWarning(this));
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

    private void loadFromCache() {
        applyPayload(JobsConfigHelper.jobsJson(this));
    }

    private void refreshFromNetwork() {
        try {
            APIService api = RestApiClient.getClient().create(APIService.class);
            JsonObject body = new JsonObject();
            body.addProperty("limit", 100);
            try {
                int lang = SharedPrefsData.getInt(
                        getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
                if (lang > 0) body.addProperty("language_id", lang);
            } catch (Exception ignored) {
            }
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
                }
            });
        } catch (Exception ignored) {
        }
    }

    private void applyPayload(@Nullable JSONObject payload) {
        allItems.clear();
        categories.clear();
        if (payload != null) {
            JSONArray items = payload.optJSONArray("items");
            if (items != null) {
                for (int i = 0; i < items.length(); i++) {
                    JSONObject j = items.optJSONObject(i);
                    if (j != null) allItems.add(j);
                }
            }
            JSONArray cats = payload.optJSONArray("categories");
            if (cats != null) {
                for (int i = 0; i < cats.length(); i++) {
                    JSONObject c = cats.optJSONObject(i);
                    if (c != null && c.optBoolean("enabled", true)) categories.add(c);
                }
            }
        }
        pinFocusJob();
        rebuildCategories();
        renderList();
    }

    private void rebuildCategories() {
        LinearLayout row = findViewById(R.id.jobs_categories);
        if (row == null) return;
        row.removeAllViews();
        addCategoryChip(row, "all", JobsLabels.catAll(this));
        if (categories.isEmpty()) {
            // Fallback chips until admin categories load
            addCategoryChip(row, "office", "ఆఫీస్");
            addCategoryChip(row, "hotel", "హోటల్");
            addCategoryChip(row, "teaching", "టీచింగ్");
            addCategoryChip(row, "sales", "సేల్స్");
            addCategoryChip(row, "banking", "బ్యాంకింగ్");
            return;
        }
        for (JSONObject c : categories) {
            String key = c.optString("key", "");
            if (TextUtils.isEmpty(key)) continue;
            String label = JobsConfigHelper.localized(this, c, "label_te", "label_en");
            if (TextUtils.isEmpty(label)) label = key;
            addCategoryChip(row, key, label);
        }
    }

    private void addCategoryChip(@NonNull LinearLayout row, @NonNull String key,
                                 @NonNull String label) {
        float dens = getResources().getDisplayMetrics().density;
        boolean selected = key.equals(selectedCategory);
        TextView chip = new TextView(this);
        chip.setText(label);
        chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        chip.setTextColor(selected ? Color.parseColor("#FF1E88E5") : Color.parseColor("#FF475569"));
        chip.setBackgroundResource(selected
                ? R.drawable.bg_jobs_chip_selected
                : R.drawable.bg_jobs_chip);
        int hPad = (int) (14 * dens);
        int vPad = (int) (7 * dens);
        chip.setPadding(hPad, vPad, hPad, vPad);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, (int) (8 * dens), 0);
        chip.setLayoutParams(lp);
        chip.setOnClickListener(v -> {
            selectedCategory = key;
            rebuildCategories();
            renderList();
        });
        row.addView(chip);
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
    }

    private boolean matchesFilter(@NonNull JSONObject job) {
        // Always show the deep-linked shared job (cross-language, any category).
        if (focusJobId > 0 && job.optInt("job_id", 0) == focusJobId) {
            return true;
        }
        try {
            int appLang = SharedPrefsData.getInt(
                    getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
            int jobLang = job.optInt("language_id", 0);
            // Language-specific jobs: hide other languages when job has a language_id.
            if (appLang > 0 && jobLang > 0 && appLang != jobLang) return false;
        } catch (Exception ignored) {
        }
        if (!"all".equals(selectedCategory)) {
            String cat = job.optString("category", "");
            if (!selectedCategory.equalsIgnoreCase(cat)) return false;
        }
        if (TextUtils.isEmpty(searchQuery)) return true;
        String blob = (
                JobsConfigHelper.localized(this, job, "title_te", "title_en") + " "
                        + JobsConfigHelper.localized(this, job, "company_te", "company_en") + " "
                        + JobsConfigHelper.localized(this, job, "location_te", "location_en") + " "
                        + JobsConfigHelper.localized(this, job, "description_te", "description_en") + " "
                        + job.optString("phone", "")
        ).toLowerCase();
        return blob.contains(searchQuery);
    }

    private void bindJobCard(@NonNull View card, @NonNull JSONObject job) {
        TextView title = card.findViewById(R.id.job_title);
        TextView time = card.findViewById(R.id.job_time);
        TextView description = card.findViewById(R.id.job_description);
        TextView salary = card.findViewById(R.id.job_salary);
        ImageView logo = card.findViewById(R.id.job_logo);
        ImageView report = card.findViewById(R.id.job_report);
        ImageView share = card.findViewById(R.id.job_share);
        ImageView call = card.findViewById(R.id.job_call);

        String titleText = JobsConfigHelper.localized(this, job, "title_te", "title_en");
        if (title != null) title.setText(titleText);

        long pubMs = 0L;
        String pub = job.optString("published_at", "");
        if (!TextUtils.isEmpty(pub)) pubMs = parseIsoMs(pub);
        if (time != null) {
            if (pubMs <= 0) pubMs = System.currentTimeMillis();
            time.setText(JobsLabels.timeAgo(this, pubMs));
        }

        String desc = JobsConfigHelper.localized(this, job, "description_te", "description_en");
        if (TextUtils.isEmpty(desc)) {
            // Fallback: company + location blurb
            String company = JobsConfigHelper.localized(this, job, "company_te", "company_en");
            String location = JobsConfigHelper.localized(this, job, "location_te", "location_en");
            StringBuilder sb = new StringBuilder();
            if (!TextUtils.isEmpty(company)) sb.append(company);
            if (!TextUtils.isEmpty(location)) {
                if (sb.length() > 0) sb.append(" · ");
                sb.append(location);
            }
            desc = sb.toString();
        }
        String phone = resolvePhone(job, desc);
        if (description != null) {
            bindDescriptionWithPhone(description, desc, phone);
        }

        if (salary != null) {
            String sal = JobsConfigHelper.localized(this, job, "salary_te", "salary_en");
            salary.setText(TextUtils.isEmpty(sal) ? "—" : sal);
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

        if (report != null) {
            report.setOnClickListener(v -> showReportDialog(job));
        }
        if (share != null) {
            share.setOnClickListener(v -> shareJob(job));
        }
        if (call != null) {
            final String dialPhone = phone;
            call.setOnClickListener(v -> dialNumber(dialPhone));
        }
    }

    @NonNull
    private String resolvePhone(@NonNull JSONObject job, @Nullable String desc) {
        String phone = job.optString("phone", "").trim();
        if (!TextUtils.isEmpty(phone)) return phone.replaceAll("[^\\d+]", "");
        if (desc != null) {
            Matcher m = PHONE_PATTERN.matcher(desc);
            if (m.find()) return m.group().replaceAll("[^\\d+]", "");
        }
        return "";
    }

    private void bindDescriptionWithPhone(
            @NonNull TextView tv, @Nullable String desc, @Nullable String phone) {
        if (TextUtils.isEmpty(desc)) {
            tv.setText("");
            return;
        }
        SpannableString ss = new SpannableString(desc);
        Matcher m = PHONE_PATTERN.matcher(desc);
        boolean linked = false;
        while (m.find()) {
            final String raw = m.group();
            final String digits = raw.replaceAll("[^\\d+]", "");
            int start = m.start();
            int end = m.end();
            ss.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(new ForegroundColorSpan(Color.parseColor("#FF1565C0")),
                    start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    dialNumber(digits);
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            linked = true;
        }
        // If phone field set but not in text, append tappable number
        if (!linked && !TextUtils.isEmpty(phone)) {
            String append = (desc.endsWith(" ") || desc.endsWith("\n") ? "" : " ") + "☎ " + phone;
            SpannableString full = new SpannableString(desc + append);
            int start = desc.length() + (append.startsWith(" ") ? 1 : 0);
            // underline from phone digits
            int phoneStart = full.toString().lastIndexOf(phone);
            if (phoneStart >= 0) {
                int phoneEnd = phoneStart + phone.length();
                full.setSpan(new UnderlineSpan(), phoneStart, phoneEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                full.setSpan(new ForegroundColorSpan(Color.parseColor("#FF1565C0")),
                        phoneStart, phoneEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                full.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        dialNumber(phone);
                    }
                }, phoneStart, phoneEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            tv.setText(full);
            tv.setMovementMethod(LinkMovementMethod.getInstance());
            tv.setHighlightColor(Color.TRANSPARENT);
            return;
        }
        tv.setText(ss);
        if (linked) {
            tv.setMovementMethod(LinkMovementMethod.getInstance());
            tv.setHighlightColor(Color.TRANSPARENT);
        }
    }

    private void dialNumber(@Nullable String phone) {
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, JobsLabels.noPhone(this), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, JobsLabels.noPhone(this), Toast.LENGTH_SHORT).show();
        }
    }

    private void shareJob(@NonNull JSONObject job) {
        String title = JobsConfigHelper.localized(this, job, "title_te", "title_en");
        String location = JobsConfigHelper.localized(this, job, "location_te", "location_en");
        if (TextUtils.isEmpty(location)) {
            location = JobsConfigHelper.localized(this, job, "company_te", "company_en");
        }
        String url = job.optString("share_url", "");
        int jobId = job.optInt("job_id", 0);
        // Always emit article-style /job/{id}. Rewrite legacy /job-{base64} from cache.
        if (jobId > 0 && (TextUtils.isEmpty(url)
                || url.contains("/job-")
                || !url.contains("/job/"))) {
            url = "https://anynews.co.in/job/" + jobId;
        }
        String text = JobsLabels.shareMessage(this, title, location, url);
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, title);
            i.putExtra(Intent.EXTRA_TEXT, text);
            // Prefer WhatsApp when available (Way2-style share)
            try {
                i.setPackage("com.whatsapp");
                startActivity(i);
            } catch (Exception noWa) {
                i.setPackage(null);
                startActivity(Intent.createChooser(i, JobsLabels.pageTitle(this)));
            }
        } catch (Exception ignored) {
        }
    }

    private void showReportDialog(@NonNull JSONObject job) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_report_job);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.9f),
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        TextView title = dialog.findViewById(R.id.report_title);
        TextView reasonLabel = dialog.findViewById(R.id.report_reason_label);
        TextView mobileLabel = dialog.findViewById(R.id.report_mobile_label);
        EditText reason = dialog.findViewById(R.id.report_reason);
        EditText mobile = dialog.findViewById(R.id.report_mobile);
        TextView close = dialog.findViewById(R.id.report_close);
        TextView submit = dialog.findViewById(R.id.report_submit);

        if (title != null) title.setText(JobsLabels.reportTitle(this));
        if (reasonLabel != null) reasonLabel.setText(JobsLabels.reportReasonLabel(this));
        if (mobileLabel != null) mobileLabel.setText(JobsLabels.reportMobileLabel(this));
        if (reason != null) reason.setHint(JobsLabels.reportReasonHint(this));
        if (mobile != null) mobile.setHint(JobsLabels.reportMobileHint(this));
        if (close != null) {
            close.setText(JobsLabels.reportClose(this));
            close.setOnClickListener(v -> dialog.dismiss());
        }
        if (submit != null) {
            submit.setText(JobsLabels.reportSubmit(this));
            submit.setOnClickListener(v -> {
                String r = reason != null ? reason.getText().toString().trim() : "";
                String m = mobile != null ? mobile.getText().toString().trim() : "";
                if (TextUtils.isEmpty(r)) {
                    Toast.makeText(this, JobsLabels.reportNeedReason(this), Toast.LENGTH_SHORT).show();
                    return;
                }
                submitReport(job.optInt("job_id", 0), r, m, dialog);
            });
        }
        dialog.show();
    }

    private void submitReport(int jobId, @NonNull String reason, @NonNull String mobile,
                              @NonNull Dialog dialog) {
        try {
            APIService api = RestApiClient.getClient().create(APIService.class);
            JsonObject body = new JsonObject();
            body.addProperty("job_id", jobId);
            body.addProperty("reason", reason);
            body.addProperty("mobile", mobile);
            try {
                int uid = SharedPrefsData.getInt(
                        getApplicationContext(), Constants.USER_ID, Constants.PREF_NAME);
                if (uid > 0) body.addProperty("user_id", uid);
            } catch (Exception ignored) {
            }
            api.report_job("v3", body).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call,
                                       @NonNull Response<JsonObject> response) {
                    dialog.dismiss();
                    Toast.makeText(JobsPageActivity.this,
                            JobsLabels.reportThanks(JobsPageActivity.this),
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    dialog.dismiss();
                    Toast.makeText(JobsPageActivity.this,
                            JobsLabels.reportThanks(JobsPageActivity.this),
                            Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            dialog.dismiss();
        }
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
