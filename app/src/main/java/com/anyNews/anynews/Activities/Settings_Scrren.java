package com.anyNews.anynews.Activities;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.GetSettingsResponse;
import com.anyNews.anynews.Pojo.LanguageModelResponse;
import com.anyNews.anynews.Pojo.LanguageSelectionRequest;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.NightModeHelper;
import com.anyNews.anynews.Utils.ProfileHubConfigHelper;
import com.anyNews.anynews.Utils.ContactUsChooser;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.ExplainReasonCallback;
import com.permissionx.guolindev.callback.ForwardToSettingsCallback;
import com.permissionx.guolindev.callback.RequestCallback;
import com.permissionx.guolindev.request.ExplainScope;
import com.permissionx.guolindev.request.ForwardScope;

import java.io.File;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

/**
 * Settings screen matching light/dark mocks: banner, toggle card, action pair, contact rows.
 * Pref keys live on {@link Constants} under PREF_NAME ("my_pref").
 * Night mode via {@link NightModeHelper} (smooth switch, no freeze).
 * Toggle-column visibility from admin {@link ProfileHubConfigHelper} splash flags.
 */
public class Settings_Scrren extends AppCompatActivity {

    private ImageView goBack;
    private TextView appVersionId;
    private TextView settingsTitle;
    private SwitchCompat switchNotifications;
    private SwitchCompat switchNight;
    private SwitchCompat switchLocal;
    private SwitchCompat switchRewards;

    private LanguageModelResponse languageModelResponse;
    private Allsharedpreference prefShare;
    private boolean suppressToggleCallbacks;
    private boolean pendingEnableNotifications;
    /** User turned notifications OFF while system still allows — re-check after Settings. */
    private boolean pendingDisableNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.settings_page_bg));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.settings_page_bg));
        setContentView(R.layout.activity_settings__scrren);

        bindViews();
        applyToggleVisibility();
        applyLocalizedLabels();
        setVersionLabel();
        wireClicks();
        loadLocalToggleState();
        getSettingsApi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyToggleVisibility();
        boolean systemOn = isSystemNotificationsEnabled();
        if (pendingEnableNotifications && systemOn) {
            pendingEnableNotifications = false;
            persistNotificationPreference(true);
            updateNotificationApi(true);
        } else if (pendingDisableNotifications && !systemOn) {
            pendingDisableNotifications = false;
            persistNotificationPreference(false);
            updateNotificationApi(false);
        } else {
            // Soft-align FCM pref if user changed OS settings outside this toggle.
            boolean pref = SharedPrefsData.getBool(
                    this, Constants.NOTIFICATION_ENABLED, Constants.PREF_NAME, true);
            if (pref != systemOn) {
                persistNotificationPreference(systemOn);
            }
        }
        // Always mirror Android system app-notification permission — never a fake in-app flag.
        applyNotificationSwitchState();
    }

    /** Real phone Settings → App notifications (POST_NOTIFICATIONS / channel master). */
    private boolean isSystemNotificationsEnabled() {
        try {
            return NotificationManagerCompat.from(this).areNotificationsEnabled();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Toggle ON iff system notifications are allowed for this app; OFF otherwise.
     */
    private void applyNotificationSwitchState() {
        if (switchNotifications == null) return;
        suppressToggleCallbacks = true;
        switchNotifications.setChecked(isSystemNotificationsEnabled());
        suppressToggleCallbacks = false;
    }

    private void persistNotificationPreference(boolean enabled) {
        SharedPrefsData.putBool(
                this, Constants.NOTIFICATION_ENABLED, enabled, Constants.PREF_NAME);
        SharedPrefsData.putBool(
                this, Constants.switchNotification, enabled, Constants.PREF_NAME);
    }

    private void openAppNotificationSettings() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                startActivity(intent);
                return;
            }
        } catch (Exception ignored) {
        }
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, getPackageName(), Toast.LENGTH_SHORT).show();
        }
    }

    private void bindViews() {
        goBack = findViewById(R.id.go_back);
        appVersionId = findViewById(R.id.app_version_id);
        settingsTitle = findViewById(R.id.settings_title);
        switchNotifications = findViewById(R.id.switch_notifications);
        switchNight = findViewById(R.id.switch_night);
        switchLocal = findViewById(R.id.switch_local);
        switchRewards = findViewById(R.id.switch_rewards);
    }

    /**
     * Admin flags hide toggle columns; GONE cells drop out of the weight row so
     * remaining columns fill without empty holes (weightSum = visible count).
     */
    private void applyToggleVisibility() {
        View cellNotif = findViewById(R.id.cell_notifications);
        View cellNight = findViewById(R.id.cell_night);
        View cellLocal = findViewById(R.id.cell_local);
        View cellRewards = findViewById(R.id.cell_rewards);
        setCellVisible(cellNotif, ProfileHubConfigHelper.isSettingsShowNotifications(this));
        setCellVisible(cellNight, ProfileHubConfigHelper.isSettingsShowNightMode(this));
        setCellVisible(cellLocal, ProfileHubConfigHelper.isSettingsShowLocalStories(this));
        // Visible whenever admin master is ON. The switch writes the flag the hub reads.
        setCellVisible(
                cellRewards,
                com.anyNews.anynews.Utils.DistrictPinVisibilityConfig.rewardsMasterEnabled(this));

        LinearLayout row = findViewById(R.id.row_settings_toggles);
        if (row == null) return;
        int visible = 0;
        for (int i = 0; i < row.getChildCount(); i++) {
            if (row.getChildAt(i).getVisibility() == View.VISIBLE) visible++;
        }
        if (visible <= 0) {
            row.setVisibility(View.GONE);
        } else {
            row.setVisibility(View.VISIBLE);
            row.setWeightSum(visible);
        }
    }

    private static void setCellVisible(View cell, boolean show) {
        if (cell != null) {
            cell.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void applyLocalizedLabels() {
        prefShare = new Allsharedpreference(this);
        try {
            String translation = prefShare.getShared_String(Allsharedpreference.Translator);
            languageModelResponse = new Gson().fromJson(translation, LanguageModelResponse.class);
        } catch (Exception ignored) {
            languageModelResponse = null;
        }

        LanguageModelResponse.Data d =
                languageModelResponse != null ? languageModelResponse.getData() : null;

        if (settingsTitle != null) {
            settingsTitle.setText(
                    firstNonEmpty(
                            d != null ? d.getSettings() : null, getString(R.string.settings_title)));
        }

        setText(R.id.banner_missing_link, getString(R.string.settings_missing_notifications));
        setText(R.id.banner_missing_sub, getString(R.string.settings_missing_notifications_sub));
        setText(R.id.banner_know_more, getString(R.string.settings_know_more));

        setText(
                R.id.label_notifications,
                firstNonEmpty(
                        d != null ? d.getNotifications() : null,
                        getString(R.string.settings_notifications)));
        setText(R.id.label_night, getString(R.string.settings_night_mode));
        setText(
                R.id.label_local,
                firstNonEmpty(
                        d != null ? d.getLocal() : null, getString(R.string.settings_local_stories)));
        setText(R.id.label_rewards, getString(R.string.settings_rewards));

        setText(R.id.label_bookmarks, getString(R.string.settings_bookmarks));
        setText(R.id.label_clear_cache, getString(R.string.settings_clear_cache));

        bindLinkRow(
                R.id.row_rate,
                firstNonEmpty(
                        d != null ? d.getRateThisApp() : null,
                        getString(R.string.settings_rate_app)));
        bindLinkRow(
                R.id.row_terms,
                firstNonEmpty(
                        d != null ? d.getTermsAndConditions() : null,
                        getString(R.string.settings_terms)));
        bindLinkRow(
                R.id.row_privacy,
                firstNonEmpty(
                        d != null ? d.getPrivacy() : null, getString(R.string.settings_privacy)));
        bindLinkRow(
                R.id.row_contact,
                firstNonEmpty(
                        d != null ? d.getContactUs() : null, getString(R.string.settings_contact)));

        TextView section = findViewById(R.id.section_contact);
        if (section != null) {
            section.setText(
                    firstNonEmpty(
                            d != null ? d.getContactUs() : null,
                            getString(R.string.settings_contact_section)));
        }
    }

    private void setText(int id, String value) {
        TextView tv = findViewById(id);
        if (tv != null && value != null) tv.setText(value);
    }

    private void bindLinkRow(int includeId, String label) {
        View row = findViewById(includeId);
        if (row == null) return;
        TextView tv = row.findViewById(R.id.settings_link_label);
        if (tv != null) tv.setText(label);
    }

    private static boolean notEmpty(String s) {
        return s != null && !s.trim().isEmpty() && !"null".equalsIgnoreCase(s.trim());
    }

    private static String firstNonEmpty(String a, String b) {
        return notEmpty(a) ? a.trim() : b;
    }

    private void setVersionLabel() {
        String version = "";
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = info.versionName != null ? info.versionName : "";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (languageModelResponse != null
                && languageModelResponse.getData() != null
                && notEmpty(languageModelResponse.getData().getVersion())) {
            String prefix = languageModelResponse.getData().getVersion().trim();
            if (prefix.regionMatches(true, 0, "App Version", 0, "App Version".length())) {
                appVersionId.setText(getString(R.string.settings_app_version, version));
            } else {
                appVersionId.setText(prefix + " " + version);
            }
        } else {
            appVersionId.setText(getString(R.string.settings_app_version, version));
        }
    }

    private void loadLocalToggleState() {
        suppressToggleCallbacks = true;
        boolean night = NightModeHelper.isEnabled(this);
        boolean local =
                SharedPrefsData.getBool(this, Constants.LOCAL_STORIES, Constants.PREF_NAME, false);

        if (switchNight != null) switchNight.setChecked(night);
        if (switchLocal != null) switchLocal.setChecked(local);
        boolean rewardsOn =
                com.anyNews.anynews.Utils.DistrictPinVisibilityConfig.userRewardsEnabled(this);
        if (switchRewards != null) switchRewards.setChecked(rewardsOn);
        // Persist whatever the switch shows so Profile/feed read the same ON/OFF.
        com.anyNews.anynews.Utils.DistrictPinVisibilityConfig.setUserRewardsEnabled(this, rewardsOn);
        suppressToggleCallbacks = false;
        applyNotificationSwitchState();
        syncRewardsSwitchWithServer();
    }

    private void wireClicks() {
        goBack.setOnClickListener(v -> finish());

        View.OnClickListener openHelp =
                v -> startActivity(new Intent(Settings_Scrren.this, NotificationHelpActivity.class));
        View banner = findViewById(R.id.banner_missing_notifications);
        if (banner != null) banner.setOnClickListener(openHelp);
        View bannerLink = findViewById(R.id.banner_missing_link);
        if (bannerLink != null) bannerLink.setOnClickListener(openHelp);
        View knowMore = findViewById(R.id.banner_know_more);
        if (knowMore != null) knowMore.setOnClickListener(openHelp);

        if (switchNotifications != null) {
            switchNotifications.setOnCheckedChangeListener(
                    (buttonView, isChecked) -> {
                        if (suppressToggleCallbacks) return;
                        if (isChecked) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                                    && ContextCompat.checkSelfPermission(
                                                    this, Manifest.permission.POST_NOTIFICATIONS)
                                            != PackageManager.PERMISSION_GRANTED) {
                                pendingEnableNotifications = true;
                                applyNotificationSwitchState();
                                PermissionX.init(this)
                                        .permissions(Manifest.permission.POST_NOTIFICATIONS)
                                        .onExplainRequestReason(
                                                new ExplainReasonCallback() {
                                                    @Override
                                                    public void onExplainReason(
                                                            @NonNull ExplainScope scope,
                                                            @NonNull List<String> deniedList) {
                                                        scope.showRequestReasonDialog(
                                                                deniedList,
                                                                "Please allow notifications",
                                                                "OK",
                                                                "Cancel");
                                                    }
                                                })
                                        .onForwardToSettings(
                                                new ForwardToSettingsCallback() {
                                                    @Override
                                                    public void onForwardToSettings(
                                                            @NonNull ForwardScope scope,
                                                            @NonNull List<String> deniedList) {
                                                        scope.showForwardToSettingsDialog(
                                                                deniedList,
                                                                "Please allow notifications in system settings",
                                                                "OK",
                                                                "Cancel");
                                                    }
                                                })
                                        .request(
                                                new RequestCallback() {
                                                    @Override
                                                    public void onResult(
                                                            boolean allGranted,
                                                            @NonNull List<String> grantedList,
                                                            @NonNull List<String> deniedList) {
                                                        if (allGranted
                                                                && isSystemNotificationsEnabled()) {
                                                            pendingEnableNotifications = false;
                                                            persistNotificationPreference(true);
                                                            updateNotificationApi(true);
                                                            applyNotificationSwitchState();
                                                        } else if (!allGranted
                                                                && !isSystemNotificationsEnabled()) {
                                                            openAppNotificationSettings();
                                                        } else {
                                                            applyNotificationSwitchState();
                                                        }
                                                    }
                                                });
                                return;
                            }
                            if (!isSystemNotificationsEnabled()) {
                                // Cannot pretend enabled — open phone Settings and re-check on return.
                                pendingEnableNotifications = true;
                                applyNotificationSwitchState();
                                openAppNotificationSettings();
                                return;
                            }
                            pendingEnableNotifications = false;
                            persistNotificationPreference(true);
                            updateNotificationApi(true);
                        } else {
                            // Apps cannot silently revoke notification access on modern Android.
                            pendingEnableNotifications = false;
                            if (!isSystemNotificationsEnabled()) {
                                persistNotificationPreference(false);
                                updateNotificationApi(false);
                                return;
                            }
                            pendingDisableNotifications = true;
                            applyNotificationSwitchState(); // snap back to system ON until user disables
                            openAppNotificationSettings();
                        }
                    });
        }

        if (switchNight != null) {
            switchNight.setOnCheckedChangeListener(
                    (buttonView, isChecked) -> {
                        if (suppressToggleCallbacks) return;
                        NightModeHelper.setUserNightModeFromSettings(Settings_Scrren.this, isChecked);
                    });
        }

        if (switchLocal != null) {
            switchLocal.setOnCheckedChangeListener(
                    (buttonView, isChecked) -> {
                        if (suppressToggleCallbacks) return;
                        SharedPrefsData.putBool(
                                this, Constants.LOCAL_STORIES, isChecked, Constants.PREF_NAME);
                    });
        }

        if (switchRewards != null) {
            switchRewards.setOnCheckedChangeListener(
                    (buttonView, isChecked) -> {
                        if (suppressToggleCallbacks) return;
                        com.anyNews.anynews.Utils.DistrictPinVisibilityConfig.setUserRewardsEnabled(
                                Settings_Scrren.this, isChecked);
                        pushRewardsEnabled(isChecked);
                    });
        }

        View btnBookmarks = findViewById(R.id.btn_bookmarks);
        if (btnBookmarks != null) {
            btnBookmarks.setOnClickListener(
                    v ->
                            startActivity(
                                    new Intent(Settings_Scrren.this, BookmarksActivity.class)));
        }

        View btnClear = findViewById(R.id.btn_clear_cache);
        if (btnClear != null) {
            btnClear.setOnClickListener(v -> clearAppCache());
        }

        View rowRate = findViewById(R.id.row_rate);
        if (rowRate != null) rowRate.setOnClickListener(v -> openPlayStoreRate());
        View rowTerms = findViewById(R.id.row_terms);
        if (rowTerms != null) rowTerms.setOnClickListener(v -> openLegalWeb("1"));
        View rowPrivacy = findViewById(R.id.row_privacy);
        if (rowPrivacy != null) rowPrivacy.setOnClickListener(v -> openLegalWeb("2"));
        View rowContact = findViewById(R.id.row_contact);
        if (rowContact != null) rowContact.setOnClickListener(v -> openContactDialog());
    }

    private void openPlayStoreRate() {
        final String appPackageName = getPackageName();
        try {
            Intent market =
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + appPackageName));
            market.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(market);
        } catch (ActivityNotFoundException e) {
            try {
                startActivity(
                        new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                        "https://play.google.com/store/apps/details?id="
                                                + appPackageName)));
            } catch (ActivityNotFoundException ignored) {
                Toast.makeText(this, appPackageName, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openLegalWeb(String sessionId) {
        Intent intent = new Intent(this, TermsAndCondition.class);
        intent.putExtra("EXTRA_SESSION_ID", sessionId);
        startActivity(intent);
    }

    private void openContactDialog() {
        ContactUsChooser.show(this, "Contact — AnyNews");
    }

    private void openMailto(String subject) {
        ContactUsChooser.show(this, subject);
    }

    private void clearAppCache() {
        try {
            Glide.get(this).clearMemory();
        } catch (Exception ignored) {
        }
        new Thread(
                        () -> {
                            try {
                                Glide.get(getApplicationContext()).clearDiskCache();
                            } catch (Exception ignored) {
                            }
                            try {
                                if (ImageLoader.getInstance().isInited()) {
                                    ImageLoader.getInstance().clearDiskCache();
                                    ImageLoader.getInstance().clearMemoryCache();
                                }
                            } catch (Exception ignored) {
                            }
                            deleteDir(getCacheDir());
                            File external = getExternalCacheDir();
                            if (external != null) deleteDir(external);
                            runOnUiThread(
                                    () ->
                                            Toast.makeText(
                                                            Settings_Scrren.this,
                                                            R.string.settings_cache_cleared,
                                                            Toast.LENGTH_SHORT)
                                                    .show());
                        })
                .start();
    }

    private static boolean deleteDir(File dir) {
        if (dir == null || !dir.exists()) return false;
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteDir(child);
                }
            }
        }
        return dir.delete();
    }

    private JsonObject buildSettingsBody() {
        LanguageSelectionRequest requestModel = new LanguageSelectionRequest();
        requestModel.setDraftId(
                SharedPrefsData.getInt(
                        getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
        String jsonString = new Gson().toJson(requestModel);
        String encrpt_data = encrypt(jsonString);
        showLog("API REQUEST E" + jsonString);
        EncritonRequestData body = new EncritonRequestData();
        body.setData(encrpt_data);
        return new Gson().toJsonTree(body).getAsJsonObject();
    }

    private JsonObject buildUpdateNotificationBody(boolean value) {
        LanguageSelectionRequest requestModel = new LanguageSelectionRequest();
        requestModel.setDraftId(
                SharedPrefsData.getInt(this, Constants.DraftID, Constants.PREF_NAME));
        requestModel.setProperty("notification_flag");
        requestModel.setValue(value);
        // Explicit only while still due (server also converts when shown_at already set).
        if (value
                && SharedPrefsData.getBool(
                        this, Constants.NOTIFICATION_REPROMPT_DUE, Constants.PREF_NAME, false)) {
            requestModel.setViaReprompt(true);
        }
        String jsonString = new Gson().toJson(requestModel);
        String encrpt_data = encrypt(jsonString);
        EncritonRequestData body = new EncritonRequestData();
        body.setData(encrpt_data);
        return new Gson().toJsonTree(body).getAsJsonObject();
    }

    public void getSettingsApi() {
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.get_settings("v3", buildSettingsBody());
        apiResponse.enqueue(
                new Callback<EncreptionRequest>() {
                    @Override
                    public void onResponse(
                            Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                        try {
                            if (response.body() == null || response.body().getData() == null)
                                return;
                            String plain = decrypt(response.body().getData());
                            GetSettingsResponse model =
                                    new Gson().fromJson(plain, GetSettingsResponse.class);
                            if (model != null
                                    && model.getSuccess()
                                    && model.getData() != null
                                    && model.getData().getNotificationFlag() != null) {
                                // Keep server/FCM prefs in sync when possible, but UI always
                                // mirrors NotificationManagerCompat.areNotificationsEnabled().
                                boolean systemOn = isSystemNotificationsEnabled();
                                persistNotificationPreference(systemOn);
                                applyNotificationSwitchState();
                                if (model.getData().getNotificationRepromptDue() != null) {
                                    SharedPrefsData.putBool(
                                            Settings_Scrren.this,
                                            Constants.NOTIFICATION_REPROMPT_DUE,
                                            Boolean.TRUE.equals(
                                                    model.getData().getNotificationRepromptDue()),
                                            Constants.PREF_NAME);
                                }
                                if (model.getData().getNotificationRepromptEnabled() != null) {
                                    SharedPrefsData.putBool(
                                            Settings_Scrren.this,
                                            Constants.NOTIFICATION_REPROMPT_ENABLED,
                                            Boolean.TRUE.equals(
                                                    model.getData()
                                                            .getNotificationRepromptEnabled()),
                                            Constants.PREF_NAME);
                                }
                                SharedPrefsData.putBool(
                                        Settings_Scrren.this,
                                        Constants.PROMPT_NOTIFICATIONS_ON_OPEN,
                                        Boolean.TRUE.equals(
                                                model.getData()
                                                        .getPromptNotificationsOnOpen()),
                                        Constants.PREF_NAME);
                            }
                        } catch (Exception e) {
                            showLog("settings parse error " + e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                        showLog(
                                "settings get failed "
                                        + (t != null ? t.getMessage() : ""));
                    }
                });
    }

    /**
     * Local OFF is pushed so a failed earlier save still reaches the server.
     * Local ON only reads: an explicit server OFF updates this device.
     * Missing server flag stays ON (old apps).
     */
    private void syncRewardsSwitchWithServer() {
        if (!com.anyNews.anynews.rewards.RewardsAuth.isLoggedIn(this)) return;
        boolean localOn =
                com.anyNews.anynews.Utils.DistrictPinVisibilityConfig.userRewardsEnabled(this);
        if (!localOn) {
            pushRewardsEnabled(false);
            return;
        }
        try {
            APIService apiService = RestApiClient.getClient().create(APIService.class);
            apiService
                    .rewards_set_enabled(
                            "v3", com.anyNews.anynews.rewards.RewardsAuth.authBody(this))
                    .enqueue(
                            new Callback<JsonObject>() {
                                @Override
                                public void onResponse(
                                        Call<JsonObject> call, Response<JsonObject> response) {
                                    applyServerRewardsOff(response != null ? response.body() : null);
                                }

                                @Override
                                public void onFailure(Call<JsonObject> call, Throwable t) {}
                            });
        } catch (Exception ignored) {
        }
    }

    private void pushRewardsEnabled(boolean enabled) {
        if (!com.anyNews.anynews.rewards.RewardsAuth.isLoggedIn(this)) return;
        try {
            JsonObject body = com.anyNews.anynews.rewards.RewardsAuth.authBody(this);
            body.addProperty("rewards_enabled", enabled);
            APIService apiService = RestApiClient.getClient().create(APIService.class);
            apiService
                    .rewards_set_enabled("v3", body)
                    .enqueue(
                            new Callback<JsonObject>() {
                                @Override
                                public void onResponse(
                                        Call<JsonObject> call, Response<JsonObject> response) {}

                                @Override
                                public void onFailure(Call<JsonObject> call, Throwable t) {}
                            });
        } catch (Exception ignored) {
        }
    }

    private void applyServerRewardsOff(JsonObject body) {
        if (body == null || !body.has("rewards_enabled_explicit") || !body.has("rewards_enabled")) {
            return;
        }
        try {
            if (body.get("rewards_enabled_explicit").isJsonNull()
                    || !body.get("rewards_enabled_explicit").getAsBoolean()) {
                return;
            }
            if (body.get("rewards_enabled").isJsonNull()
                    || body.get("rewards_enabled").getAsBoolean()) {
                return;
            }
        } catch (Exception ignored) {
            return;
        }
        runOnUiThread(
                () -> {
                    suppressToggleCallbacks = true;
                    com.anyNews.anynews.Utils.DistrictPinVisibilityConfig.setUserRewardsEnabled(
                            Settings_Scrren.this, false);
                    if (switchRewards != null) switchRewards.setChecked(false);
                    suppressToggleCallbacks = false;
                });
    }

    private void updateNotificationApi(boolean value) {
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse =
                apiService.update_settings("v3", buildUpdateNotificationBody(value));
        apiResponse.enqueue(
                new Callback<EncreptionRequest>() {
                    @Override
                    public void onResponse(
                            Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                        // server ack only
                    }

                    @Override
                    public void onFailure(Call<EncreptionRequest> call, Throwable t) {}
                });
    }

    public static void applyStoredNightMode(Context context) {
        NightModeHelper.applyStoredPreference(context);
    }
}
