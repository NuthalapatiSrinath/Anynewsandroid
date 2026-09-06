package com.anyNews.anynews.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.ProfileHubConfigHelper;
import com.anyNews.anynews.Utils.ProfileHubLabels;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Become-a-Reporter applicant sandbox (max 3 sample posts).
 * Sample articles use the full {@link PostArticles} UI in applicant mode
 * (posts go to reporter_applicants, not the live feed).
 */
public class BecomeAReporterActivity extends AppCompatActivity {

    private static final int REQ_PROFILE_PHOTO = 7101;
    private static final int REQ_SAMPLE_POST = 7103;
    /** Last uploaded/saved applicant photo_url (survives back/reopen until Save hits server). */
    private static final String PREF_APPLICANT_PHOTO_URL = "become_reporter_applicant_photo_url";

    private TextView statusView;
    private EditText nameInput;
    private EditText phoneInput;
    private EditText emailInput;
    private ImageView photoView;
    private TextView pickPhotoLabel;
    private LinearLayout postsSection;
    private LinearLayout postsList;
    private TextView addPostBtn;
    private ProgressBar progress;
    private View profileCard;
    private View saveProfileBtn;
    private View composeCard;

    private String photoUrl = "";
    /** Content URI shown as instant preview; blocks Glide remote swap that clears the ImageView. */
    @Nullable
    private String localPreviewUri;
    private String contactWhatsapp = "";
    private JSONObject applicant;
    private boolean uploading;
    private boolean applicantLoadStarted;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_become_a_reporter);

        TextView title = findViewById(R.id.become_reporter_title);
        if (title != null) title.setText(ProfileHubLabels.becomeAReporter(this));

        statusView = findViewById(R.id.become_reporter_status);
        nameInput = findViewById(R.id.become_reporter_name);
        phoneInput = findViewById(R.id.become_reporter_phone);
        emailInput = findViewById(R.id.become_reporter_email);
        photoView = findViewById(R.id.become_reporter_photo);
        pickPhotoLabel = findViewById(R.id.become_reporter_pick_photo);
        postsSection = findViewById(R.id.become_reporter_posts_section);
        postsList = findViewById(R.id.become_reporter_posts_list);
        addPostBtn = findViewById(R.id.become_reporter_add_post);
        progress = findViewById(R.id.become_reporter_progress);
        profileCard = findViewById(R.id.become_reporter_profile_card);
        saveProfileBtn = findViewById(R.id.become_reporter_save_profile);
        composeCard = findViewById(R.id.become_reporter_compose);
        // Inline half-form retired — samples use full PostArticles UI.
        if (composeCard != null) composeCard.setVisibility(View.GONE);

        View back = findViewById(R.id.become_reporter_back);
        if (back != null) back.setOnClickListener(v -> finish());

        View pickPhoto = findViewById(R.id.become_reporter_pick_photo);
        if (pickPhoto != null) {
            pickPhoto.setOnClickListener(v -> pickImage(REQ_PROFILE_PHOTO));
        }
        if (saveProfileBtn != null) {
            saveProfileBtn.setOnClickListener(v -> saveProfile());
        }
        if (addPostBtn != null) {
            addPostBtn.setOnClickListener(v -> openSamplePostFlow());
        }

        String mobile = SharedPrefsData.getString(this, Constants.Selected_Phone_Number, Constants.PREF_NAME);
        if (phoneInput != null && !TextUtils.isEmpty(mobile)) {
            phoneInput.setText(mobile);
        }

        // Paint last known photo immediately (before network) so reopen is never a blank cradle.
        restoreCachedPhotoPreview();
        loadApplicant();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ensurePhotoVisible();
        // Soft refresh after first create pass so back/reopen rebinds server photo_url.
        if (applicantLoadStarted) {
            loadApplicant();
        }
        applicantLoadStarted = true;
    }

    private void pickImage(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select image"), requestCode);
    }

    /** Full reporter create-post UI, routed to reporter_applicants (max 3). */
    private void openSamplePostFlow() {
        Intent intent = new Intent(this, PostArticles.class);
        intent.putExtra("POST_VIDEO", "0");
        intent.putExtra(PostArticles.EXTRA_APPLICANT_SAMPLE, true);
        startActivityForResult(intent, REQ_SAMPLE_POST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_SAMPLE_POST) {
            loadApplicant();
            return;
        }
        if (resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        if (requestCode == REQ_PROFILE_PHOTO) {
            uploadImage(uri);
        }
    }

    private void setBusy(boolean busy) {
        uploading = busy;
        if (progress != null) progress.setVisibility(busy ? View.VISIBLE : View.GONE);
    }

    private void uploadImage(Uri uri) {
        // Instant local preview + Change photo label while upload runs.
        // Keep this URI until remote bind succeeds — Glide remote swap clears ImageView
        // to the pink cradle (bg_profile_icon_circle_dark) for ~1s / on CDN miss.
        localPreviewUri = uri.toString();
        showProfilePhoto(localPreviewUri, true);
        setBusy(true);
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            if (is == null) {
                setBusy(false);
                Toast.makeText(this, "Could not read image", Toast.LENGTH_SHORT).show();
                return;
            }
            Bitmap bmp = BitmapFactory.decodeStream(is);
            is.close();
            if (bmp == null) {
                setBusy(false);
                return;
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 85, bos);
            byte[] bytes = bos.toByteArray();
            RequestBody req = RequestBody.create(MediaType.parse("image/jpeg"), bytes);
            MultipartBody.Part part = MultipartBody.Part.createFormData(
                    "file", "applicant_" + System.currentTimeMillis() + ".jpg", req);
            APIService api = RestApiClient.getClient().create(APIService.class);
            api.uploadImage(part).enqueue(new Callback<com.anyNews.anynews.Pojo.UserImagePojoResponse>() {
                @Override
                public void onResponse(Call<com.anyNews.anynews.Pojo.UserImagePojoResponse> call,
                                       Response<com.anyNews.anynews.Pojo.UserImagePojoResponse> response) {
                    setBusy(false);
                    try {
                        String url = extractUploadUrl(response);
                        if (TextUtils.isEmpty(url)) {
                            Toast.makeText(BecomeAReporterActivity.this,
                                    "Upload failed", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        photoUrl = url;
                        cachePhotoUrl(url);
                        // Do NOT Glide-load remote yet — keeps local preview; avoids flash-clear.
                        updatePhotoLabel(true);
                    } catch (Exception e) {
                        Toast.makeText(BecomeAReporterActivity.this,
                                "Upload parse error", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<com.anyNews.anynews.Pojo.UserImagePojoResponse> call,
                                      Throwable t) {
                    setBusy(false);
                    Toast.makeText(BecomeAReporterActivity.this,
                            "Upload failed", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            setBusy(false);
            Toast.makeText(this, "Upload error", Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    private String extractUploadUrl(
            Response<com.anyNews.anynews.Pojo.UserImagePojoResponse> response) {
        try {
            if (response.isSuccessful() && response.body() != null
                    && Boolean.TRUE.equals(response.body().getSuccess())
                    && response.body().getExtras() != null) {
                com.anyNews.anynews.Pojo.UserImagePojoResponse.Extras ex =
                        response.body().getExtras();
                String url = firstNonEmpty(
                        ex.getImageOriginal(),
                        ex.getImage900(),
                        ex.getImage550(),
                        ex.getImage250());
                if (!TextUtils.isEmpty(url)) return url;
            }
        } catch (Exception ignored) {
        }
        try {
            if (response.body() != null) {
                String json = new com.google.gson.Gson().toJson(response.body());
                JSONObject o = new JSONObject(json);
                if (o.optBoolean("success", false)) {
                    JSONObject ex = o.optJSONObject("extras");
                    if (ex != null) {
                        return firstNonEmpty(
                                ex.optString("ImageOriginal", null),
                                ex.optString("Image900", null),
                                ex.optString("Image550", null),
                                ex.optString("Image250", null),
                                ex.optString("url", null));
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void showProfilePhoto(@Nullable String urlOrUri, boolean localPreview) {
        if (photoView == null) return;
        photoView.setColorFilter(null);
        photoView.clearColorFilter();
        photoView.setImageTintList(null);
        if (TextUtils.isEmpty(urlOrUri)) {
            if (!TextUtils.isEmpty(localPreviewUri) || !TextUtils.isEmpty(photoUrl)) {
                // Never wipe a good preview on empty/partial payloads.
                updatePhotoLabel(true);
                return;
            }
            photoView.setImageResource(R.drawable.ic_hub_become_reporter);
            updatePhotoLabel(false);
            return;
        }
        Drawable current = photoView.getDrawable();
        final boolean keepLocalUntilRemoteOk = !localPreview && !TextUtils.isEmpty(localPreviewUri);
        Glide.with(this)
                .load(urlOrUri)
                .circleCrop()
                .dontAnimate()
                .placeholder(current)
                .error(current)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        // Keep local / previous drawable; do not fall back to empty cradle.
                        return current != null;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target, DataSource dataSource,
                                                   boolean isFirstResource) {
                        if (keepLocalUntilRemoteOk) {
                            localPreviewUri = null;
                        }
                        return false;
                    }
                })
                .into(photoView);
        updatePhotoLabel(true);
    }

    private void updatePhotoLabel(boolean hasPhoto) {
        if (pickPhotoLabel == null) return;
        pickPhotoLabel.setText(hasPhoto
                ? R.string.profile_change_photo
                : R.string.become_reporter_add_photo);
    }

    private String photoCacheKey() {
        int draft = SharedPrefsData.getInt(this, Constants.DraftID, Constants.PREF_NAME);
        String phone = SharedPrefsData.getString(this, Constants.Selected_Phone_Number, Constants.PREF_NAME);
        if (TextUtils.isEmpty(phone)) phone = "";
        return PREF_APPLICANT_PHOTO_URL + "_" + draft + "_" + phone;
    }

    private void cachePhotoUrl(@Nullable String url) {
        if (TextUtils.isEmpty(url)) return;
        try {
            SharedPrefsData.putString(this, photoCacheKey(), url, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }

    @Nullable
    private String readCachedPhotoUrl() {
        try {
            String cached = SharedPrefsData.getString(this, photoCacheKey(), Constants.PREF_NAME);
            return TextUtils.isEmpty(cached) ? null : cached;
        } catch (Exception e) {
            return null;
        }
    }

    private void restoreCachedPhotoPreview() {
        String cached = readCachedPhotoUrl();
        if (TextUtils.isEmpty(cached)) return;
        if (TextUtils.isEmpty(photoUrl)) photoUrl = cached;
        showProfilePhoto(cached, false);
    }

    /** Re-bind last uploaded / cached photo if the ImageView was cleared. */
    private void ensurePhotoVisible() {
        if (photoView == null) return;
        if (!TextUtils.isEmpty(localPreviewUri)) {
            showProfilePhoto(localPreviewUri, true);
            return;
        }
        String toShow = !TextUtils.isEmpty(photoUrl) ? photoUrl : readCachedPhotoUrl();
        if (TextUtils.isEmpty(toShow)) return;
        if (TextUtils.isEmpty(photoUrl)) photoUrl = toShow;
        showProfilePhoto(toShow, false);
    }

    private JsonObject baseBody() {
        JsonObject body = new JsonObject();
        body.addProperty("draft_id",
                SharedPrefsData.getInt(this, Constants.DraftID, Constants.PREF_NAME));
        body.addProperty("source", "become_a_reporter");
        try {
            body.addProperty("language_id",
                    SharedPrefsData.getInt(this, Constants.LanguageID, Constants.PREF_NAME));
        } catch (Exception ignored) {
        }
        return body;
    }

    private void loadApplicant() {
        setBusy(true);
        APIService api = RestApiClient.getClient().create(APIService.class);
        api.reporter_applicant_get("v3", baseBody()).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                setBusy(false);
                try {
                    if (!response.isSuccessful() || response.body() == null) {
                        statusView.setText("Could not load application");
                        return;
                    }
                    JsonObject root = response.body();
                    if (!root.has("success") || !root.get("success").getAsBoolean()) {
                        statusView.setText(root.has("msg")
                                ? root.get("msg").getAsString()
                                : "Could not load application");
                        return;
                    }
                    JSONObject data = new JSONObject(root.get("data").toString());
                    contactWhatsapp = data.optString("contact_whatsapp", "");
                    if (TextUtils.isEmpty(contactWhatsapp)) {
                        try {
                            contactWhatsapp = SharedPrefsData.getString(
                                    BecomeAReporterActivity.this,
                                    Constants.FULL_JATAKAM_WHATSAPP,
                                    Constants.PREF_NAME);
                        } catch (Exception ignored) {
                        }
                    }
                    if (data.optBoolean("already_reporter", false)) {
                        statusView.setText("You are already an AnyNews reporter.");
                        if (profileCard != null) profileCard.setVisibility(View.GONE);
                        if (postsSection != null) postsSection.setVisibility(View.GONE);
                        cacheReporterDeskUiState(Constants.REPORTER_DESK_HIDDEN);
                        return;
                    }
                    if (!data.optBoolean("hiring_enabled", true)) {
                        String msg = data.optString("not_hiring_message", "").trim();
                        if (TextUtils.isEmpty(msg)) {
                            msg = ProfileHubConfigHelper.getNotHiringMessage(
                                    BecomeAReporterActivity.this);
                        }
                        try {
                            ProfileHubConfigHelper.saveNotHiringMessages(
                                    BecomeAReporterActivity.this,
                                    data.optJSONObject("not_hiring_messages"));
                        } catch (Exception ignored) {
                        }
                        statusView.setText(msg);
                        new android.app.AlertDialog.Builder(BecomeAReporterActivity.this)
                                .setMessage(msg)
                                .setPositiveButton(android.R.string.ok,
                                        (d, w) -> finish())
                                .setCancelable(false)
                                .show();
                        return;
                    }
                    if (!data.optBoolean("logged_in", false)) {
                        statusView.setText(R.string.become_reporter_login_required);
                        return;
                    }
                    JSONObject a = data.optJSONObject("applicant");
                    applyApplicant(a);
                } catch (Exception e) {
                    statusView.setText("Parse error");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                setBusy(false);
                statusView.setText("Network error");
            }
        });
    }

    private void applyApplicant(@Nullable JSONObject a) {
        applicant = a;
        if (a == null) {
            statusView.setText("Complete your profile, then submit up to 3 sample articles.");
            if (postsSection != null) postsSection.setVisibility(View.GONE);
            bindApplicantPhoto("");
            cacheReporterDeskUiState(Constants.REPORTER_DESK_HIDDEN);
            return;
        }
        String status = a.optString("status", "");
        if ("rejected".equals(status)) {
            String wa = contactWhatsapp;
            if (TextUtils.isEmpty(wa)) {
                wa = a.optString("contact_whatsapp", "");
            }
            String msg = getString(R.string.become_reporter_rejected_msg);
            if (!TextUtils.isEmpty(wa)) {
                msg = msg + "\n" + getString(R.string.become_reporter_rejected_whatsapp, wa);
            }
            statusView.setText(msg);
            if (profileCard != null) profileCard.setVisibility(View.GONE);
            if (postsSection != null) postsSection.setVisibility(View.GONE);
            cacheReporterDeskUiState(a.optBoolean("reject_ui_visible", true)
                    ? Constants.REPORTER_DESK_REJECTED
                    : Constants.REPORTER_DESK_HIDDEN);
            return;
        }

        // Keep Profile desk card stable (no flicker) when user returns.
        if (a.optBoolean("show_awaiting_approval", false)) {
            cacheReporterDeskUiState(Constants.REPORTER_DESK_PENDING);
        } else if (a.optBoolean("reject_ui_visible", false)) {
            cacheReporterDeskUiState(Constants.REPORTER_DESK_REJECTED);
        }
        int remaining = a.optInt("posts_remaining", 3);
        String schedule = a.optString("start_posting_at", "");
        StringBuilder sb = new StringBuilder();
        sb.append("Status: ").append(status);
        if (a.optBoolean("profile_complete", false)) {
            sb.append(" · Posts left: ").append(remaining);
        } else {
            sb.append(" · ").append(getString(R.string.become_reporter_profile_blocked));
        }
        if (!TextUtils.isEmpty(schedule) && "scheduled".equals(status)) {
            sb.append("\nStart after: ").append(schedule);
        }
        if (a.has("score") && !a.isNull("score")) {
            sb.append("\nScore: ").append(a.optInt("score"));
        }
        statusView.setText(sb.toString());

        if (nameInput != null) nameInput.setText(a.optString("name", ""));
        if (phoneInput != null) {
            String p = a.optString("phone", a.optString("mobile_number", ""));
            if (!TextUtils.isEmpty(p)) phoneInput.setText(p);
        }
        if (emailInput != null) emailInput.setText(a.optString("email", ""));
        bindApplicantPhoto(a.optString("photo_url", ""));

        boolean complete = a.optBoolean("profile_complete", false);
        if (postsSection != null) {
            postsSection.setVisibility(complete ? View.VISIBLE : View.GONE);
        }
        renderPosts(a.optJSONArray("posts"));
        boolean canPost = a.optBoolean("can_post", false);
        if (addPostBtn != null) {
            addPostBtn.setVisibility(canPost ? View.VISIBLE : View.GONE);
        }
        if (composeCard != null) composeCard.setVisibility(View.GONE);
        if ("active_reporter".equals(status) || a.optBoolean("is_reporter_now", false)) {
            statusView.setText("Approved — you are now a reporter. Use Profile → Reporter desk.");
            if (addPostBtn != null) addPostBtn.setVisibility(View.GONE);
            cacheReporterDeskUiState(Constants.REPORTER_DESK_HIDDEN);
        }
    }

    /** Prefs used by Profile desk so it paints instantly without GONE→VISIBLE flicker. */
    private void cacheReporterDeskUiState(int state) {
        try {
            SharedPrefsData.putInt(getApplicationContext(), Constants.REPORTER_DESK_UI_STATE,
                    state, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }

    /**
     * Bind photo from server without wiping an in-progress local preview or cached upload.
     * Partial get/upsert responses often omit photo_url until Save — never clear the cradle.
     */
    private void bindApplicantPhoto(@Nullable String serverPhoto) {
        String fromServer = serverPhoto != null ? serverPhoto.trim() : "";
        if (!TextUtils.isEmpty(fromServer)) {
            photoUrl = fromServer;
            cachePhotoUrl(fromServer);
            if (TextUtils.isEmpty(localPreviewUri)) {
                showProfilePhoto(photoUrl, false);
            } else {
                updatePhotoLabel(true);
            }
            return;
        }
        if (TextUtils.isEmpty(photoUrl)) {
            String cached = readCachedPhotoUrl();
            if (!TextUtils.isEmpty(cached)) photoUrl = cached;
        }
        if (!TextUtils.isEmpty(localPreviewUri)) {
            updatePhotoLabel(true);
            return;
        }
        if (!TextUtils.isEmpty(photoUrl)) {
            showProfilePhoto(photoUrl, false);
            return;
        }
        updatePhotoLabel(false);
    }

    private void renderPosts(@Nullable JSONArray posts) {
        if (postsList == null) return;
        postsList.removeAllViews();
        if (posts == null || posts.length() == 0) {
            TextView empty = new TextView(this);
            empty.setText("No sample articles yet.");
            empty.setTextColor(getResources().getColor(R.color.profile_way2_guest_sub));
            empty.setTextSize(13f);
            postsList.addView(empty);
            return;
        }
        for (int i = 0; i < posts.length(); i++) {
            JSONObject p = posts.optJSONObject(i);
            if (p == null) continue;
            TextView row = new TextView(this);
            row.setText((i + 1) + ". " + p.optString("title", "Untitled")
                    + "  ·  " + p.optString("status", "pending_review"));
            row.setTextColor(getResources().getColor(R.color.profile_way2_menu_text));
            row.setTextSize(14f);
            row.setPadding(0, 8, 0, 8);
            postsList.addView(row);
        }
    }

    private void saveProfile() {
        if (uploading) return;
        String name = nameInput != null ? nameInput.getText().toString().trim() : "";
        String phone = phoneInput != null ? phoneInput.getText().toString().trim() : "";
        String email = emailInput != null ? emailInput.getText().toString().trim() : "";
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(photoUrl) || TextUtils.isEmpty(phone)) {
            Toast.makeText(this, R.string.become_reporter_profile_blocked, Toast.LENGTH_LONG).show();
            return;
        }
        setBusy(true);
        JsonObject body = baseBody();
        body.addProperty("name", name);
        body.addProperty("photo_url", photoUrl);
        body.addProperty("phone", phone);
        body.addProperty("email", email);
        body.addProperty("language_id",
                SharedPrefsData.getInt(this, Constants.LanguageID, Constants.PREF_NAME));
        APIService api = RestApiClient.getClient().create(APIService.class);
        api.reporter_applicant_upsert_profile("v3", body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                setBusy(false);
                try {
                    JsonObject root = response.body();
                    if (root == null || !root.has("success") || !root.get("success").getAsBoolean()) {
                        String msg = root != null && root.has("msg")
                                ? root.get("msg").getAsString() : "Save failed";
                        Toast.makeText(BecomeAReporterActivity.this, msg, Toast.LENGTH_LONG).show();
                        return;
                    }
                    Toast.makeText(BecomeAReporterActivity.this, "Profile saved", Toast.LENGTH_SHORT).show();
                    if (root.has("data")) {
                        applyApplicant(new JSONObject(root.get("data").toString()));
                    } else {
                        loadApplicant();
                    }
                } catch (Exception e) {
                    Toast.makeText(BecomeAReporterActivity.this, "Save error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                setBusy(false);
                Toast.makeText(BecomeAReporterActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (!TextUtils.isEmpty(v)) return v;
        }
        return null;
    }
}
