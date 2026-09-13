package com.anyNews.anynews.Activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Ui.BirthdayPosterPreviewView;
import com.anyNews.anynews.Utils.BirthdayFonts;
import com.anyNews.anynews.Utils.BirthdayPhotoNormalizer;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.DistrictLabelHelper;
import com.anyNews.anynews.Utils.SelectionListCache;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.Utils.WalletUserId;
import com.anyNews.anynews.models.StatesModel;
import com.anyNews.anynews.rewards.RewardsAuth;
import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fully automatic Birthday Special — logged-in users schedule a banner post.
 * Live poster preview mirrors the admin HAPPY BIRTHDAY template; server composites
 * the final poster and cron publishes on the chosen date.
 */
public class BirthdayAutoActivity extends AppCompatActivity {

    private BirthdayPosterPreviewView livePreview;
    private ImageView serverPreview;
    private EditText nameInput;
    private EditText addressInput;
    private EditText specialInput;
    private EditText titleInput;
    private CheckBox nameBold;
    private CheckBox addrBold;
    private Spinner nameFontSpinner;
    private Spinner nameSizeSpinner;
    private Spinner addrFontSpinner;
    private Spinner addrSizeSpinner;
    private SeekBar zoomBar;
    private TextView scheduleInfo;
    private Button pickDateBtn;
    private ProgressBar progress;
    private Uri photoUri;
    private File normalizedJpegFile;
    private Bitmap previewBitmap;
    private String uploadedPhotoUrl = "";
    private String scheduleDate = "";
    private String earliestDate = "";
    private String publishLabel = "02:00 IST";
    private String cutoffLabel = "00:00 IST";
    private boolean busy;
    private float panX;
    private float panY;
    private long previewSeq;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService photoExecutor = Executors.newSingleThreadExecutor();
    private final Runnable previewRunnable = this::requestServerPreview;
    private final Runnable panPreviewRunnable = this::scheduleServerPreview;

    private final ActivityResultLauncher<String> pickPhoto =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri == null) return;
                        beginNormalizePhoto(uri);
                    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isSessionReady()) {
            Toast.makeText(this, R.string.birthday_auto_login_required, Toast.LENGTH_LONG).show();
            Intent login = new Intent(this, LoginActivityDetails.class);
            login.putExtra(LoginActivityDetails.EXTRA_RETURN_TO_PROFILE, true);
            startActivity(login);
            finish();
            return;
        }

        setContentView(R.layout.activity_birthday_auto);
        ImageButton back = findViewById(R.id.birthday_auto_back);
        if (back != null) back.setOnClickListener(v -> finish());

        livePreview = findViewById(R.id.birthday_auto_live_preview);
        serverPreview = findViewById(R.id.birthday_auto_server_preview);
        nameInput = findViewById(R.id.birthday_auto_name);
        addressInput = findViewById(R.id.birthday_auto_address);
        specialInput = findViewById(R.id.birthday_auto_special);
        titleInput = findViewById(R.id.birthday_auto_post_title);
        nameBold = findViewById(R.id.birthday_auto_name_bold);
        addrBold = findViewById(R.id.birthday_auto_addr_bold);
        nameFontSpinner = findViewById(R.id.birthday_auto_name_font);
        nameSizeSpinner = findViewById(R.id.birthday_auto_name_size);
        addrFontSpinner = findViewById(R.id.birthday_auto_addr_font);
        addrSizeSpinner = findViewById(R.id.birthday_auto_addr_size);
        zoomBar = findViewById(R.id.birthday_auto_zoom);
        scheduleInfo = findViewById(R.id.birthday_auto_schedule_info);
        pickDateBtn = findViewById(R.id.birthday_auto_pick_date);
        progress = findViewById(R.id.birthday_auto_progress);

        setupFontSpinners();
        autofillAddress();
        String uname =
                SharedPrefsData.getString(this, Constants.USER_NAME, Constants.PREF_NAME);
        if (nameInput != null && !TextUtils.isEmpty(uname)) {
            nameInput.setText(uname);
        }
        if (titleInput != null && !TextUtils.isEmpty(uname)) {
            titleInput.setText("Happy Birthday " + uname);
        }

        findViewById(R.id.birthday_auto_pick_photo)
                .setOnClickListener(v -> pickPhoto.launch("image/*"));
        if (pickDateBtn != null) {
            pickDateBtn.setOnClickListener(v -> openDatePicker());
        }
        findViewById(R.id.birthday_auto_submit).setOnClickListener(v -> submit());

        if (livePreview != null) {
            livePreview.setPanListener(
                    (px, py) -> {
                        panX = px;
                        panY = py;
                        hideServerPreview();
                        mainHandler.removeCallbacks(panPreviewRunnable);
                        mainHandler.postDelayed(panPreviewRunnable, 500);
                    });
        }

        TextWatcher watcher =
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        hideServerPreview();
                        refreshLiveTexts();
                        scheduleServerPreview();
                    }
                };
        if (nameInput != null) nameInput.addTextChangedListener(watcher);
        if (addressInput != null) addressInput.addTextChangedListener(watcher);
        if (specialInput != null) specialInput.addTextChangedListener(watcher);

        CompoundButton.OnCheckedChangeListener boldListener =
                (buttonView, isChecked) -> {
                    hideServerPreview();
                    refreshLiveTexts();
                    scheduleServerPreview();
                };
        if (nameBold != null) nameBold.setOnCheckedChangeListener(boldListener);
        if (addrBold != null) addrBold.setOnCheckedChangeListener(boldListener);

        if (zoomBar != null) {
            zoomBar.setOnSeekBarChangeListener(
                    new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            float zoom = 1f + (progress / 10f);
                            if (livePreview != null) livePreview.setZoom(zoom);
                            if (fromUser) {
                                hideServerPreview();
                                scheduleServerPreview();
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {}

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            scheduleServerPreview();
                        }
                    });
        }

        refreshLiveTexts();
        loadConfig();
    }

    private void setupFontSpinners() {
        AdapterView.OnItemSelectedListener styleListener =
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        hideServerPreview();
                        refreshLiveTexts();
                        scheduleServerPreview();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                };
        if (nameFontSpinner != null) {
            nameFontSpinner.setAdapter(BirthdayFonts.fontSampleAdapter(this));
            nameFontSpinner.setSelection(BirthdayFonts.indexOf(BirthdayFonts.DEFAULT_NAME_FONT_ID));
            nameFontSpinner.setOnItemSelectedListener(styleListener);
        }
        if (addrFontSpinner != null) {
            addrFontSpinner.setAdapter(BirthdayFonts.fontSampleAdapter(this));
            addrFontSpinner.setSelection(BirthdayFonts.indexOf(BirthdayFonts.DEFAULT_ADDRESS_FONT_ID));
            addrFontSpinner.setOnItemSelectedListener(styleListener);
        }
        if (nameSizeSpinner != null) {
            nameSizeSpinner.setAdapter(BirthdayFonts.sizeAdapter(this));
            nameSizeSpinner.setSelection(BirthdayFonts.defaultSizeTierIndex());
            nameSizeSpinner.setOnItemSelectedListener(styleListener);
        }
        if (addrSizeSpinner != null) {
            addrSizeSpinner.setAdapter(BirthdayFonts.sizeAdapter(this));
            addrSizeSpinner.setSelection(BirthdayFonts.defaultSizeTierIndex());
            addrSizeSpinner.setOnItemSelectedListener(styleListener);
        }
    }

    private void beginNormalizePhoto(Uri uri) {
        setBusy(true);
        hideServerPreview();
        uploadedPhotoUrl = "";
        photoExecutor.execute(
                () -> {
                    try {
                        BirthdayPhotoNormalizer.Result result =
                                BirthdayPhotoNormalizer.toJpeg(BirthdayAutoActivity.this, uri);
                        mainHandler.post(
                                () -> {
                                    setBusy(false);
                                    photoUri = result.jpegUri;
                                    normalizedJpegFile = result.jpegFile;
                                    if (previewBitmap != null && !previewBitmap.isRecycled()) {
                                        // previous preview owned by us
                                    }
                                    previewBitmap = result.previewBitmap;
                                    panX = 0f;
                                    panY = 0f;
                                    if (livePreview != null) {
                                        livePreview.setPan(0f, 0f);
                                        livePreview.setPhotoBitmap(previewBitmap);
                                    }
                                    refreshLiveTexts();
                                    uploadPhotoForPreview();
                                });
                    } catch (Exception e) {
                        mainHandler.post(
                                () -> {
                                    setBusy(false);
                                    Toast.makeText(
                                                    BirthdayAutoActivity.this,
                                                    R.string.birthday_auto_photo_heif_failed,
                                                    Toast.LENGTH_LONG)
                                            .show();
                                });
                    }
                });
    }

    private void hideServerPreview() {
        if (serverPreview != null) {
            serverPreview.setVisibility(View.GONE);
            serverPreview.setImageDrawable(null);
        }
    }

    /** Same gate as Rewards / wallet: OTP login flag + resolvable user_id. */
    private boolean isSessionReady() {
        try {
            if (RewardsAuth.isLoggedIn(this)) return true;
        } catch (Exception ignored) {
        }
        try {
            boolean flagged =
                    SharedPrefsData.getBool(this, Constants.is_login, Constants.PREF_NAME);
            long uid = WalletUserId.resolve(this);
            return flagged && uid > 0;
        } catch (Exception ignored) {
            return false;
        }
    }

    private long resolveUserId() {
        return WalletUserId.resolve(this);
    }

    private void autofillAddress() {
        String district = DistrictLabelHelper.resolveDisplayLabel(this);
        String state = resolveStateDisplayName();
        String address;
        if (DistrictLabelHelper.isUsable(district) && DistrictLabelHelper.isUsable(state)) {
            address = district.trim() + ", " + state.trim();
        } else if (DistrictLabelHelper.isUsable(district)) {
            address = district.trim();
        } else if (DistrictLabelHelper.isUsable(state)) {
            address = state.trim();
        } else {
            address = "";
        }
        if (addressInput != null) {
            addressInput.setText(address);
        }
    }

    @Nullable
    private String resolveStateDisplayName() {
        try {
            int lang = SharedPrefsData.getInt(this, Constants.LanguageID, Constants.PREF_NAME);
            int stateId = SharedPrefsData.getInt(this, Constants.StateID, Constants.PREF_NAME);
            if (lang <= 0 || stateId <= 0) return null;
            List<StatesModel.Datum> states = SelectionListCache.loadStates(this, lang);
            if (states == null) return null;
            for (StatesModel.Datum st : states) {
                if (st != null && st.getId() != null && st.getId() == stateId) {
                    String name = st.getName();
                    if (DistrictLabelHelper.isUsable(name)) return name.trim();
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String selectedNameFontId() {
        if (nameFontSpinner == null) return BirthdayFonts.DEFAULT_NAME_FONT_ID;
        return BirthdayFonts.idAt(nameFontSpinner.getSelectedItemPosition());
    }

    private String selectedAddrFontId() {
        if (addrFontSpinner == null) return BirthdayFonts.DEFAULT_ADDRESS_FONT_ID;
        return BirthdayFonts.idAt(addrFontSpinner.getSelectedItemPosition());
    }

    private int selectedNameSizePx() {
        int tier = nameSizeSpinner != null ? nameSizeSpinner.getSelectedItemPosition() : 1;
        return BirthdayFonts.nameSizePx(tier);
    }

    private int selectedAddrSizePx() {
        int tier = addrSizeSpinner != null ? addrSizeSpinner.getSelectedItemPosition() : 1;
        return BirthdayFonts.addressSizePx(tier);
    }

    private void refreshLiveTexts() {
        if (livePreview == null) return;
        boolean boldN = nameBold != null && nameBold.isChecked();
        boolean boldA = addrBold != null && addrBold.isChecked();
        livePreview.setBannerTexts(
                nameInput != null ? nameInput.getText().toString() : "",
                addressInput != null ? addressInput.getText().toString() : "",
                specialInput != null ? specialInput.getText().toString() : "",
                boldN,
                boldA);
        livePreview.setNameStyle(selectedNameFontId(), selectedNameSizePx(), boldN);
        livePreview.setAddressStyle(selectedAddrFontId(), selectedAddrSizePx(), boldA);
        float zoom = 1f;
        if (zoomBar != null) zoom = 1f + (zoomBar.getProgress() / 10f);
        livePreview.setZoom(zoom);
        livePreview.setPan(panX, panY);
    }

    private void scheduleServerPreview() {
        mainHandler.removeCallbacks(previewRunnable);
        if (TextUtils.isEmpty(uploadedPhotoUrl) || photoUri == null) return;
        mainHandler.postDelayed(previewRunnable, 450);
    }

    private void requestServerPreview() {
        if (TextUtils.isEmpty(uploadedPhotoUrl)) return;
        final long seq = ++previewSeq;
        JsonObject body = buildPosterBody(uploadedPhotoUrl, /*includeSchedule*/ false);
        APIService api = RestApiClient.getClient().create(APIService.class);
        api.birthday_preview("v3", body)
                .enqueue(
                        new Callback<JsonObject>() {
                            @Override
                            public void onResponse(
                                    Call<JsonObject> call, Response<JsonObject> response) {
                                if (seq != previewSeq || isFinishing()) return;
                                try {
                                    JsonObject root = response.body();
                                    if (root == null
                                            || !root.has("success")
                                            || !root.get("success").getAsBoolean()) {
                                        return;
                                    }
                                    JsonObject data =
                                            root.has("data") && root.get("data").isJsonObject()
                                                    ? root.getAsJsonObject("data")
                                                    : root;
                                    String url =
                                            data.has("poster_url")
                                                    ? data.get("poster_url").getAsString()
                                                    : "";
                                    if (TextUtils.isEmpty(url) || serverPreview == null) return;
                                    serverPreview.setVisibility(View.VISIBLE);
                                    Glide.with(BirthdayAutoActivity.this)
                                            .load(url)
                                            .fitCenter()
                                            .into(serverPreview);
                                } catch (Exception ignored) {
                                }
                            }

                            @Override
                            public void onFailure(Call<JsonObject> call, Throwable t) {
                                // Local live preview remains the source of truth.
                            }
                        });
    }

    private void uploadPhotoForPreview() {
        if (normalizedJpegFile == null || !normalizedJpegFile.exists()) return;
        try {
            byte[] bytes = readFileBytes(normalizedJpegFile);
            if (bytes == null || bytes.length == 0) return;
            RequestBody req = RequestBody.create(MediaType.parse("image/jpeg"), bytes);
            MultipartBody.Part part =
                    MultipartBody.Part.createFormData(
                            "file", "bday_preview_" + System.currentTimeMillis() + ".jpg", req);
            APIService api = RestApiClient.getClient().create(APIService.class);
            api.uploadFile(part)
                    .enqueue(
                            new Callback<JsonObject>() {
                                @Override
                                public void onResponse(
                                        Call<JsonObject> call, Response<JsonObject> response) {
                                    String url = extractUploadUrl(response.body());
                                    if (!TextUtils.isEmpty(url)) {
                                        uploadedPhotoUrl = url;
                                        scheduleServerPreview();
                                    }
                                }

                                @Override
                                public void onFailure(Call<JsonObject> call, Throwable t) {}
                            });
        } catch (Exception ignored) {
        }
    }

    private void loadConfig() {
        APIService api = RestApiClient.getClient().create(APIService.class);
        api.birthday_config("v3", new JsonObject())
                .enqueue(
                        new Callback<JsonObject>() {
                            @Override
                            public void onResponse(
                                    Call<JsonObject> call, Response<JsonObject> response) {
                                try {
                                    JsonObject root = response.body();
                                    if (root == null
                                            || !root.has("success")
                                            || !root.get("success").getAsBoolean()) {
                                        applyFallbackDates();
                                        return;
                                    }
                                    JsonObject data =
                                            root.has("data") && root.get("data").isJsonObject()
                                                    ? root.getAsJsonObject("data")
                                                    : root;
                                    if (data.has("earliest_schedule_date")) {
                                        earliestDate = data.get("earliest_schedule_date").getAsString();
                                    }
                                    if (data.has("publish_label")) {
                                        publishLabel = data.get("publish_label").getAsString();
                                    }
                                    if (data.has("cutoff_label")) {
                                        cutoffLabel = data.get("cutoff_label").getAsString();
                                    }
                                    if (TextUtils.isEmpty(scheduleDate) && !TextUtils.isEmpty(earliestDate)) {
                                        scheduleDate = earliestDate;
                                        if (pickDateBtn != null) {
                                            pickDateBtn.setText(scheduleDate);
                                        }
                                    }
                                    updateScheduleInfo();
                                } catch (Exception e) {
                                    applyFallbackDates();
                                }
                            }

                            @Override
                            public void onFailure(Call<JsonObject> call, Throwable t) {
                                applyFallbackDates();
                            }
                        });
    }

    private void applyFallbackDates() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"));
        cal.add(Calendar.DAY_OF_MONTH, 1);
        earliestDate =
                String.format(
                        Locale.US,
                        "%04d-%02d-%02d",
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH) + 1,
                        cal.get(Calendar.DAY_OF_MONTH));
        if (TextUtils.isEmpty(scheduleDate)) {
            scheduleDate = earliestDate;
            if (pickDateBtn != null) pickDateBtn.setText(scheduleDate);
        }
        updateScheduleInfo();
    }

    private void updateScheduleInfo() {
        if (scheduleInfo == null) return;
        scheduleInfo.setText(
                getString(R.string.birthday_auto_schedule_info_fmt, publishLabel, cutoffLabel));
    }

    private void openDatePicker() {
        Calendar min = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"));
        min.add(Calendar.DAY_OF_MONTH, 1);
        if (!TextUtils.isEmpty(earliestDate) && earliestDate.length() >= 10) {
            try {
                String[] p = earliestDate.split("-");
                min.set(Integer.parseInt(p[0]), Integer.parseInt(p[1]) - 1, Integer.parseInt(p[2]));
            } catch (Exception ignored) {
            }
        }
        Calendar start = (Calendar) min.clone();
        if (!TextUtils.isEmpty(scheduleDate) && scheduleDate.length() >= 10) {
            try {
                String[] p = scheduleDate.split("-");
                start.set(Integer.parseInt(p[0]), Integer.parseInt(p[1]) - 1, Integer.parseInt(p[2]));
            } catch (Exception ignored) {
            }
        }
        DatePickerDialog dlg =
                new DatePickerDialog(
                        this,
                        (view, year, month, dayOfMonth) -> {
                            scheduleDate =
                                    String.format(
                                            Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                            if (pickDateBtn != null) pickDateBtn.setText(scheduleDate);
                        },
                        start.get(Calendar.YEAR),
                        start.get(Calendar.MONTH),
                        start.get(Calendar.DAY_OF_MONTH));
        dlg.getDatePicker().setMinDate(min.getTimeInMillis());
        dlg.show();
    }

    private void setBusy(boolean on) {
        busy = on;
        if (progress != null) progress.setVisibility(on ? View.VISIBLE : View.GONE);
        View submit = findViewById(R.id.birthday_auto_submit);
        if (submit != null) submit.setEnabled(!on);
    }

    private void submit() {
        if (busy) return;
        if (!isSessionReady()) {
            Toast.makeText(this, R.string.birthday_auto_login_required, Toast.LENGTH_LONG).show();
            return;
        }
        if (photoUri == null || normalizedJpegFile == null || !normalizedJpegFile.exists()) {
            Toast.makeText(this, R.string.birthday_need_photo, Toast.LENGTH_SHORT).show();
            return;
        }
        String name = nameInput != null ? nameInput.getText().toString().trim() : "";
        String address = addressInput != null ? addressInput.getText().toString().trim() : "";
        if (TextUtils.isEmpty(name) || name.length() < 2) {
            Toast.makeText(this, R.string.birthday_auto_name, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(address)) {
            Toast.makeText(this, R.string.birthday_need_location, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(scheduleDate)) {
            Toast.makeText(this, R.string.birthday_auto_need_date, Toast.LENGTH_SHORT).show();
            return;
        }
        long uid = resolveUserId();
        if (uid <= 0) {
            Toast.makeText(this, R.string.birthday_auto_login_required, Toast.LENGTH_LONG).show();
            return;
        }
        setBusy(true);
        if (!TextUtils.isEmpty(uploadedPhotoUrl)) {
            postSchedule(name, address, uploadedPhotoUrl);
        } else {
            uploadThenSchedule(name, address);
        }
    }

    private byte[] readFileBytes(File file) throws Exception {
        try (FileInputStream is = new FileInputStream(file)) {
            byte[] buf = new byte[(int) Math.min(file.length(), Integer.MAX_VALUE)];
            int off = 0;
            while (off < buf.length) {
                int n = is.read(buf, off, buf.length - off);
                if (n < 0) break;
                off += n;
            }
            if (off == buf.length) return buf;
            byte[] exact = new byte[off];
            System.arraycopy(buf, 0, exact, 0, off);
            return exact;
        }
    }

    private void uploadThenSchedule(String name, String address) {
        try {
            byte[] bytes = readFileBytes(normalizedJpegFile);
            if (bytes == null || bytes.length == 0) {
                setBusy(false);
                Toast.makeText(this, R.string.advertise_upload_failed, Toast.LENGTH_SHORT).show();
                return;
            }
            RequestBody req = RequestBody.create(MediaType.parse("image/jpeg"), bytes);
            MultipartBody.Part part =
                    MultipartBody.Part.createFormData(
                            "file", "bday_auto_" + System.currentTimeMillis() + ".jpg", req);
            APIService api = RestApiClient.getClient().create(APIService.class);
            api.uploadFile(part)
                    .enqueue(
                            new Callback<JsonObject>() {
                                @Override
                                public void onResponse(
                                        Call<JsonObject> call, Response<JsonObject> response) {
                                    try {
                                        String url = extractUploadUrl(response.body());
                                        if (TextUtils.isEmpty(url)) {
                                            setBusy(false);
                                            Toast.makeText(
                                                            BirthdayAutoActivity.this,
                                                            R.string.advertise_upload_failed,
                                                            Toast.LENGTH_SHORT)
                                                    .show();
                                            return;
                                        }
                                        uploadedPhotoUrl = url;
                                        postSchedule(name, address, url);
                                    } catch (Exception e) {
                                        setBusy(false);
                                        Toast.makeText(
                                                        BirthdayAutoActivity.this,
                                                        R.string.advertise_upload_failed,
                                                        Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<JsonObject> call, Throwable t) {
                                    setBusy(false);
                                    Toast.makeText(
                                                    BirthdayAutoActivity.this,
                                                    R.string.advertise_upload_failed,
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                }
                            });
        } catch (Exception e) {
            setBusy(false);
            Toast.makeText(this, R.string.advertise_upload_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private JsonObject buildPosterBody(String photoUrl, boolean includeSchedule) {
        JsonObject body = new JsonObject();
        String name = nameInput != null ? nameInput.getText().toString().trim() : "";
        String address = addressInput != null ? addressInput.getText().toString().trim() : "";
        body.addProperty("name", name);
        body.addProperty("address", address);
        body.addProperty("photo_url", photoUrl);
        if (includeSchedule) {
            body.addProperty("schedule_date", scheduleDate);
        }
        body.addProperty(
                "title",
                titleInput != null
                        ? titleInput.getText().toString().trim()
                        : ("Happy Birthday " + name));
        body.addProperty(
                "special_text",
                specialInput != null ? specialInput.getText().toString().trim() : "");
        float zoom = 1f;
        if (zoomBar != null) {
            zoom = 1f + (zoomBar.getProgress() / 10f);
        }
        body.addProperty("zoom", zoom);
        body.addProperty("pan_x", panX);
        body.addProperty("pan_y", panY);

        JsonObject nameStyle = new JsonObject();
        nameStyle.addProperty("fontId", selectedNameFontId());
        nameStyle.addProperty("size", selectedNameSizePx());
        nameStyle.addProperty("bold", nameBold != null && nameBold.isChecked());
        body.add("name_style", nameStyle);

        JsonObject addrStyle = new JsonObject();
        addrStyle.addProperty("fontId", selectedAddrFontId());
        addrStyle.addProperty("size", selectedAddrSizePx());
        addrStyle.addProperty("bold", addrBold != null && addrBold.isChecked());
        body.add("address_style", addrStyle);

        JsonObject specialStyle = new JsonObject();
        specialStyle.addProperty("fontId", BirthdayFonts.DEFAULT_SPECIAL_FONT_ID);
        specialStyle.addProperty("size", 15);
        specialStyle.addProperty("bold", false);
        body.add("special_style", specialStyle);

        try {
            long uid = resolveUserId();
            if (uid > 0) body.addProperty("user_id", uid);
            body.addProperty(
                    "language_id",
                    SharedPrefsData.getInt(this, Constants.LanguageID, Constants.PREF_NAME));
            body.addProperty(
                    "dist_id",
                    SharedPrefsData.getInt(this, Constants.Selected_Distric_Id, Constants.PREF_NAME));
            body.addProperty(
                    "state_id",
                    SharedPrefsData.getInt(this, Constants.StateID, Constants.PREF_NAME));
            body.addProperty("district_name", DistrictLabelHelper.resolveDisplayLabel(this));
            String stateName = resolveStateDisplayName();
            if (!TextUtils.isEmpty(stateName)) body.addProperty("state_name", stateName);
            String mobile =
                    SharedPrefsData.getString(this, Constants.PhoneNumber, Constants.PREF_NAME);
            if (!TextUtils.isEmpty(mobile)) body.addProperty("mobile", mobile);
            String uname =
                    SharedPrefsData.getString(this, Constants.USER_NAME, Constants.PREF_NAME);
            if (!TextUtils.isEmpty(uname)) body.addProperty("user_name", uname);
        } catch (Exception ignored) {
        }
        return body;
    }

    private void postSchedule(String name, String address, String photoUrl) {
        JsonObject body = buildPosterBody(photoUrl, true);
        body.addProperty("name", name);
        body.addProperty("address", address);

        APIService api = RestApiClient.getClient().create(APIService.class);
        api.birthday_schedule("v3", body)
                .enqueue(
                        new Callback<JsonObject>() {
                            @Override
                            public void onResponse(
                                    Call<JsonObject> call, Response<JsonObject> response) {
                                setBusy(false);
                                try {
                                    JsonObject root = response.body();
                                    if (root == null
                                            || !root.has("success")
                                            || !root.get("success").getAsBoolean()) {
                                        String msg =
                                                root != null && root.has("msg")
                                                        ? root.get("msg").getAsString()
                                                        : getString(R.string.advertise_upload_failed);
                                        Toast.makeText(
                                                        BirthdayAutoActivity.this,
                                                        msg,
                                                        Toast.LENGTH_LONG)
                                                .show();
                                        return;
                                    }
                                    Toast.makeText(
                                                    BirthdayAutoActivity.this,
                                                    R.string.birthday_auto_scheduled_ok,
                                                    Toast.LENGTH_LONG)
                                            .show();
                                    finish();
                                } catch (Exception e) {
                                    Toast.makeText(
                                                    BirthdayAutoActivity.this,
                                                    R.string.advertise_upload_failed,
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }

                            @Override
                            public void onFailure(Call<JsonObject> call, Throwable t) {
                                setBusy(false);
                                Toast.makeText(
                                                BirthdayAutoActivity.this,
                                                R.string.advertise_upload_failed,
                                                Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });
    }

    private static String extractUploadUrl(JsonObject body) {
        if (body == null) return "";
        try {
            if (body.has("extras") && body.get("extras").isJsonObject()) {
                JsonObject extras = body.getAsJsonObject("extras");
                if (extras.has("url")) return extras.get("url").getAsString();
            }
            if (body.has("url")) return body.get("url").getAsString();
            if (body.has("data") && body.get("data").isJsonObject()) {
                JsonObject data = body.getAsJsonObject("data");
                if (data.has("url")) return data.get("url").getAsString();
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    @Override
    protected void onDestroy() {
        mainHandler.removeCallbacks(previewRunnable);
        mainHandler.removeCallbacks(panPreviewRunnable);
        photoExecutor.shutdownNow();
        super.onDestroy();
    }
}
