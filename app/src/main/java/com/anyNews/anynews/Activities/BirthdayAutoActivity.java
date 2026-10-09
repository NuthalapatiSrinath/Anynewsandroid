package com.anyNews.anynews.Activities;

import android.app.AlertDialog;
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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Ui.BirthdayPosterPreviewView;
import com.anyNews.anynews.Utils.BirthdayFonts;
import com.anyNews.anynews.Utils.ProfileHubConfigHelper;
import com.anyNews.anynews.Utils.BirthdayPhotoNormalizer;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.DistrictLabelHelper;
import com.anyNews.anynews.Utils.SelectionListCache;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.Utils.WalletUserId;
import com.anyNews.anynews.models.StatesModel;
import com.anyNews.anynews.rewards.RewardsAuth;
import com.bumptech.glide.Glide;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
    private Button submitBtn;
    private ProgressBar progress;
    private LinearLayout myListContainer;
    private TextView myListEmpty;
    private Button rot0;
    private Button rot90;
    private Button rot180;
    private Button rot270;
    private Uri photoUri;
    private File normalizedJpegFile;
    private Bitmap previewBitmap;
    private String uploadedPhotoUrl = "";
    private String scheduleDate = "";
    private String earliestDate = "";
    private String latestDate = "";
    /** Admin max days from today (IST); default 10. */
    private int maxDaysAhead = 10;
    private String publishLabel = "02:00 IST";
    private String cutoffLabel = "00:00 IST";
    private boolean busy;
    private float panX;
    private float panY;
    /** Manual clockwise orientation: 0 / 90 / 180 / 270 */
    private int photoRotation;
    /** When > 0, submit calls update instead of create. */
    private long editingBirthdayId;
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
        applySystemBarInsets();
        ImageButton back = findViewById(R.id.birthday_auto_back);
        if (back != null) back.setOnClickListener(v -> finish());

        livePreview = findViewById(R.id.birthday_auto_live_preview);
        serverPreview = findViewById(R.id.birthday_auto_server_preview);
        // Never let server raster sit above the live view (blocks pan + garbles place).
        if (serverPreview != null) {
            serverPreview.setVisibility(View.GONE);
            serverPreview.setClickable(false);
            serverPreview.setFocusable(false);
        }
        // Poster uses the full card width (a height cap used to shrink it and
        // leave a blank gap on the right). Zoom and rotate stay in the scroll.
        if (livePreview != null) {
            livePreview.setArtworkListener(this::updatePosterHint);
        }
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
        submitBtn = findViewById(R.id.birthday_auto_submit);
        progress = findViewById(R.id.birthday_auto_progress);
        myListContainer = findViewById(R.id.birthday_auto_my_list);
        myListEmpty = findViewById(R.id.birthday_auto_my_empty);
        rot0 = findViewById(R.id.birthday_auto_rot_0);
        rot90 = findViewById(R.id.birthday_auto_rot_90);
        rot180 = findViewById(R.id.birthday_auto_rot_180);
        rot270 = findViewById(R.id.birthday_auto_rot_270);

        setupFontSpinners();
        setupRotationButtons();
        autofillAddress();
        // Address field must also paint Telugu (not Latin script face).
        if (addressInput != null) {
            try {
                android.graphics.Typeface tf =
                        BirthdayFonts.typefaceForText(
                                this,
                                BirthdayFonts.DEFAULT_ADDRESS_FONT_ID,
                                false,
                                addressInput.getText() != null
                                        ? addressInput.getText().toString()
                                        : "");
                addressInput.setTypeface(tf);
            } catch (Exception ignored) {
            }
        }
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
        if (submitBtn != null) {
            submitBtn.setOnClickListener(v -> submit());
        }

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
                        try {
                            if (nameFontSpinner != null && nameFontSpinner.getAdapter() instanceof ArrayAdapter) {
                                ((ArrayAdapter<?>) nameFontSpinner.getAdapter()).notifyDataSetChanged();
                            }
                            if (addrFontSpinner != null && addrFontSpinner.getAdapter() instanceof ArrayAdapter) {
                                ((ArrayAdapter<?>) addrFontSpinner.getAdapter()).notifyDataSetChanged();
                            }
                        } catch (Exception ignored) {
                        }
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
        applyBirthdayTemplate(
                ProfileHubConfigHelper.getBirthdayTemplateId(this),
                ProfileHubConfigHelper.getBirthdayTemplateImageUrl(this));
        loadConfig();
        loadMyPosts();
    }

    private void setupRotationButtons() {
        View.OnClickListener listener =
                v -> {
                    int deg = 0;
                    int id = v.getId();
                    if (id == R.id.birthday_auto_rot_90) deg = 90;
                    else if (id == R.id.birthday_auto_rot_180) deg = 180;
                    else if (id == R.id.birthday_auto_rot_270) deg = 270;
                    setPhotoRotation(deg, true);
                };
        if (rot0 != null) rot0.setOnClickListener(listener);
        if (rot90 != null) rot90.setOnClickListener(listener);
        if (rot180 != null) rot180.setOnClickListener(listener);
        if (rot270 != null) rot270.setOnClickListener(listener);
        highlightRotationButtons();
    }

    private void setPhotoRotation(int degrees, boolean fromUser) {
        int r = ((degrees % 360) + 360) % 360;
        r = (Math.round(r / 90f) * 90) % 360;
        if (r != 0 && r != 90 && r != 180 && r != 270) r = 0;
        // Rotate must not reset pan/zoom — only update orientation.
        if (photoRotation == r && fromUser) {
            highlightRotationButtons();
            return;
        }
        photoRotation = r;
        if (livePreview != null) livePreview.setPhotoRotation(r);
        highlightRotationButtons();
        if (fromUser) {
            hideServerPreview();
            scheduleServerPreview();
        }
    }

    private void highlightRotationButtons() {
        styleRotButton(rot0, photoRotation == 0);
        styleRotButton(rot90, photoRotation == 90);
        styleRotButton(rot180, photoRotation == 180);
        styleRotButton(rot270, photoRotation == 270);
    }

    private void styleRotButton(@Nullable Button btn, boolean selected) {
        if (btn == null) return;
        btn.setBackgroundColor(selected ? 0xFFFEE2E2 : 0xFFF3F4F6);
        btn.setTextColor(selected ? 0xFFE63B19 : 0xFF374151);
    }

    private void applySystemBarInsets() {
        final View root = findViewById(R.id.birthday_auto_root);
        final View header = findViewById(R.id.birthday_auto_header);
        if (root == null || header == null) return;
        final int headerPadL = header.getPaddingLeft();
        final int headerPadT = header.getPaddingTop();
        final int headerPadR = header.getPaddingRight();
        final int headerPadB = header.getPaddingBottom();
        final int rootPadBottom = root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(
                root,
                (v, windowInsets) -> {
                    Insets status = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());
                    Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                    header.setPadding(
                            headerPadL + bars.left,
                            headerPadT + status.top,
                            headerPadR + bars.right,
                            headerPadB);
                    v.setPadding(
                            v.getPaddingLeft(),
                            v.getPaddingTop(),
                            v.getPaddingRight(),
                            rootPadBottom + bars.bottom);
                    return WindowInsetsCompat.CONSUMED;
                });
        ViewCompat.requestApplyInsets(root);
    }

    private void setupFontSpinners() {
        BirthdayFonts.ensureFontsDownloaded(this, () -> {
            refreshLiveTexts();
            try {
                if (nameFontSpinner != null && nameFontSpinner.getAdapter() instanceof ArrayAdapter) {
                    ((ArrayAdapter<?>) nameFontSpinner.getAdapter()).notifyDataSetChanged();
                }
                if (addrFontSpinner != null && addrFontSpinner.getAdapter() instanceof ArrayAdapter) {
                    ((ArrayAdapter<?>) addrFontSpinner.getAdapter()).notifyDataSetChanged();
                }
            } catch (Exception ignored) {
            }
        });
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
            nameFontSpinner.setAdapter(BirthdayFonts.fontSampleAdapter(this, spec -> {
                String n = nameInput != null && nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
                return !n.isEmpty() ? n : "Name";
            }));
            nameFontSpinner.setSelection(BirthdayFonts.indexOf(BirthdayFonts.DEFAULT_NAME_FONT_ID));
            nameFontSpinner.setOnItemSelectedListener(styleListener);
        }
        if (addrFontSpinner != null) {
            addrFontSpinner.setAdapter(BirthdayFonts.fontSampleAdapter(this, spec -> {
                String a = addressInput != null && addressInput.getText() != null ? addressInput.getText().toString().trim() : "";
                return !a.isEmpty() ? a : "Address";
            }));
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
                                    // New photo only: reset compose to upright EXIF baseline.
                                    // Do NOT touch these again on upload-complete / preview refresh.
                                    panX = 0f;
                                    panY = 0f;
                                    photoRotation = 0;
                                    if (zoomBar != null) zoomBar.setProgress(0);
                                    hideServerPreview();
                                    // Keep district/state on the plaque after upload.
                                    if (addressInput != null
                                            && TextUtils.isEmpty(
                                                    addressInput.getText() != null
                                                            ? addressInput.getText().toString().trim()
                                                            : "")) {
                                        autofillAddress();
                                    }
                                    if (livePreview != null) {
                                        livePreview.setPan(0f, 0f);
                                        livePreview.setPhotoRotation(0);
                                        livePreview.setZoom(1f);
                                        // Bitmap after EXIF-once normalize — 0° == upright.
                                        livePreview.setPhotoBitmap(previewBitmap);
                                    }
                                    highlightRotationButtons();
                                    Button pickBtn = findViewById(R.id.birthday_auto_pick_photo);
                                    if (pickBtn != null) {
                                        pickBtn.setText(R.string.birthday_change_photo);
                                    }
                                    refreshLiveTexts();
                                    uploadPhotoForPreview();
                                    scrollPhotoControlsIntoView();
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
        if (addrFontSpinner == null || addrFontSpinner.getVisibility() != View.VISIBLE) {
            return selectedNameFontId();
        }
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
        String address =
                addressInput != null ? addressInput.getText().toString() : "";
        livePreview.setBannerTexts(
                nameInput != null ? nameInput.getText().toString() : "",
                address,
                specialInput != null ? specialInput.getText().toString() : "",
                boldN,
                boldA);
        livePreview.setNameStyle(selectedNameFontId(), selectedNameSizePx(), boldN);
        livePreview.setAddressStyle(selectedAddrFontId(), selectedAddrSizePx(), boldA);
        if (addressInput != null) {
            try {
                addressInput.setTypeface(
                        BirthdayFonts.typefaceForText(
                                this, selectedAddrFontId(), boldA, address));
            } catch (Exception ignored) {
            }
        }
        float zoom = 1f;
        if (zoomBar != null) zoom = 1f + (zoomBar.getProgress() / 10f);
        livePreview.setZoom(zoom);
        livePreview.setPan(panX, panY);
        livePreview.setPhotoRotation(photoRotation);
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
                                    // Keep live compose preview as the editor source of truth.
                                    // Overlaying the server raster hid rotate/pan edits and could
                                    // drop/garble district text after upload.
                                    serverPreview.setVisibility(View.GONE);
                                    // Still warm Glide cache for submit confirmation if needed.
                                    Glide.with(BirthdayAutoActivity.this)
                                            .load(url)
                                            .fitCenter()
                                            .preload();
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
                                    if (data.has("latest_schedule_date")) {
                                        latestDate = data.get("latest_schedule_date").getAsString();
                                    }
                                    if (data.has("max_days_ahead")) {
                                        try {
                                            int n = data.get("max_days_ahead").getAsInt();
                                            if (n >= 1 && n <= 365) maxDaysAhead = n;
                                        } catch (Exception ignored) {
                                        }
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
                                    // Clamp a stale selected date if config tightened.
                                    if (!TextUtils.isEmpty(latestDate)
                                            && !TextUtils.isEmpty(scheduleDate)
                                            && scheduleDate.compareTo(latestDate) > 0) {
                                        scheduleDate = latestDate;
                                        if (pickDateBtn != null) {
                                            pickDateBtn.setText(scheduleDate);
                                        }
                                    }
                                    updateScheduleInfo();
                                    if (data.has("birthday_template_id")
                                            && !data.get("birthday_template_id").isJsonNull()) {
                                        String templateId =
                                                data.get("birthday_template_id").getAsString();
                                        String templateUrl = "";
                                        if (data.has("birthday_template_image_url")
                                                && !data.get("birthday_template_image_url")
                                                        .isJsonNull()) {
                                            templateUrl =
                                                    data.get("birthday_template_image_url")
                                                            .getAsString();
                                        }
                                        ProfileHubConfigHelper.saveBirthdayTemplate(
                                                BirthdayAutoActivity.this, templateId, templateUrl);
                                        final String shownId = templateId;
                                        final String shownUrl = templateUrl;
                                        runOnUiThread(
                                                () -> applyBirthdayTemplate(shownId, shownUrl));
                                    }
                                    if (data.has("fonts") && data.get("fonts").isJsonArray()) {
                                        try {
                                            org.json.JSONArray fonts =
                                                    new org.json.JSONArray(
                                                            data.getAsJsonArray("fonts").toString());
                                            BirthdayFonts.applyServerFonts(
                                                    BirthdayAutoActivity.this, fonts);
                                            runOnUiThread(
                                                    () -> {
                                                        String nameId = selectedNameFontId();
                                                        String addrId = selectedAddrFontId();
                                                        setupFontSpinners();
                                                        if (nameFontSpinner != null) {
                                                            nameFontSpinner.setSelection(
                                                                    BirthdayFonts.indexOf(nameId));
                                                        }
                                                        if (addrFontSpinner != null) {
                                                            addrFontSpinner.setSelection(
                                                                    BirthdayFonts.indexOf(addrId));
                                                        }
                                                        BirthdayFonts.ensureFontsDownloaded(
                                                                BirthdayAutoActivity.this,
                                                                () -> {
                                                                    refreshLiveTexts();
                                                                    try {
                                                                        if (nameFontSpinner != null
                                                                                && nameFontSpinner
                                                                                                .getAdapter()
                                                                                        instanceof
                                                                                        ArrayAdapter) {
                                                                            ((ArrayAdapter<?>)
                                                                                            nameFontSpinner
                                                                                                    .getAdapter())
                                                                                    .notifyDataSetChanged();
                                                                        }
                                                                        if (addrFontSpinner != null
                                                                                && addrFontSpinner
                                                                                                .getAdapter()
                                                                                        instanceof
                                                                                        ArrayAdapter) {
                                                                            ((ArrayAdapter<?>)
                                                                                            addrFontSpinner
                                                                                                    .getAdapter())
                                                                                    .notifyDataSetChanged();
                                                                        }
                                                                    } catch (Exception ignored) {
                                                                    }
                                                                });
                                                    });
                                        } catch (Exception ignored) {
                                        }
                                    }
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

    /** Artwork the open composer is actually using (circle or celebration). */
    private String currentPosterTemplateId() {
        if (livePreview != null) return livePreview.isTemplateB() ? "b" : "a";
        return ProfileHubConfigHelper.getBirthdayTemplateId(this);
    }

    /** Template A keeps the circle hint. Template B points at the rounded frame. */
    private void applyBirthdayTemplate(@Nullable String templateId, @Nullable String imageUrl) {
        if (livePreview != null) livePreview.applyTemplate(templateId, imageUrl);
        updatePosterHint(livePreview != null && livePreview.isTemplateB());
    }

    private void updatePosterHint(boolean celebration) {
        TextView hint = findViewById(R.id.birthday_auto_pan_hint);
        if (hint == null) return;
        hint.setText(
                celebration
                        ? R.string.birthday_auto_pan_hint_frame
                        : R.string.birthday_auto_pan_hint);
    }

    private void applyFallbackDates() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"));
        Calendar today = (Calendar) cal.clone();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        earliestDate =
                String.format(
                        Locale.US,
                        "%04d-%02d-%02d",
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH) + 1,
                        cal.get(Calendar.DAY_OF_MONTH));
        today.add(Calendar.DAY_OF_MONTH, maxDaysAhead);
        latestDate =
                String.format(
                        Locale.US,
                        "%04d-%02d-%02d",
                        today.get(Calendar.YEAR),
                        today.get(Calendar.MONTH) + 1,
                        today.get(Calendar.DAY_OF_MONTH));
        if (TextUtils.isEmpty(scheduleDate)) {
            scheduleDate = earliestDate;
            if (pickDateBtn != null) pickDateBtn.setText(scheduleDate);
        }
        updateScheduleInfo();
    }

    private void updateScheduleInfo() {
        if (scheduleInfo == null) return;
        scheduleInfo.setText(
                getString(
                        R.string.birthday_auto_schedule_info_fmt,
                        publishLabel,
                        cutoffLabel,
                        maxDaysAhead));
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
        Calendar max = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"));
        max.set(Calendar.HOUR_OF_DAY, 23);
        max.set(Calendar.MINUTE, 59);
        max.set(Calendar.SECOND, 59);
        max.set(Calendar.MILLISECOND, 999);
        if (!TextUtils.isEmpty(latestDate) && latestDate.length() >= 10) {
            try {
                String[] p = latestDate.split("-");
                max.set(Integer.parseInt(p[0]), Integer.parseInt(p[1]) - 1, Integer.parseInt(p[2]));
            } catch (Exception ignored) {
                max.add(Calendar.DAY_OF_MONTH, maxDaysAhead);
            }
        } else {
            // today (IST) + N — reset from midnight then add days
            Calendar today = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"));
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            today.add(Calendar.DAY_OF_MONTH, maxDaysAhead);
            max.setTimeInMillis(today.getTimeInMillis());
            max.set(Calendar.HOUR_OF_DAY, 23);
            max.set(Calendar.MINUTE, 59);
            max.set(Calendar.SECOND, 59);
            max.set(Calendar.MILLISECOND, 999);
        }
        if (max.before(min)) {
            max.setTimeInMillis(min.getTimeInMillis());
        }
        Calendar start = (Calendar) min.clone();
        if (!TextUtils.isEmpty(scheduleDate) && scheduleDate.length() >= 10) {
            try {
                String[] p = scheduleDate.split("-");
                start.set(Integer.parseInt(p[0]), Integer.parseInt(p[1]) - 1, Integer.parseInt(p[2]));
            } catch (Exception ignored) {
            }
        }
        if (start.before(min)) start = (Calendar) min.clone();
        if (start.after(max)) start = (Calendar) max.clone();
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
        dlg.getDatePicker().setMaxDate(max.getTimeInMillis());
        dlg.show();
    }

    private void setBusy(boolean on) {
        busy = on;
        if (progress != null) progress.setVisibility(on ? View.VISIBLE : View.GONE);
        if (submitBtn != null) submitBtn.setEnabled(!on);
    }

    private void updateSubmitLabel() {
        if (submitBtn == null) return;
        submitBtn.setText(
                editingBirthdayId > 0
                        ? R.string.birthday_auto_update
                        : R.string.birthday_auto_submit);
    }

    private void clearEditingMode() {
        editingBirthdayId = 0;
        updateSubmitLabel();
        applyBirthdayTemplate(
                ProfileHubConfigHelper.getBirthdayTemplateId(this),
                ProfileHubConfigHelper.getBirthdayTemplateImageUrl(this));
    }

    private void submit() {
        if (busy) return;
        if (!isSessionReady()) {
            Toast.makeText(this, R.string.birthday_auto_login_required, Toast.LENGTH_LONG).show();
            return;
        }
        boolean hasLocalPhoto =
                photoUri != null && normalizedJpegFile != null && normalizedJpegFile.exists();
        if (!hasLocalPhoto && TextUtils.isEmpty(uploadedPhotoUrl)) {
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
            postScheduleOrUpdate(name, address, uploadedPhotoUrl);
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
                                        postScheduleOrUpdate(name, address, url);
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
        body.addProperty("photo_rotation", photoRotation);
        body.addProperty("birthday_template_id", currentPosterTemplateId());

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

    private void postScheduleOrUpdate(String name, String address, String photoUrl) {
        JsonObject body = buildPosterBody(photoUrl, true);
        body.addProperty("name", name);
        body.addProperty("address", address);

        APIService api = RestApiClient.getClient().create(APIService.class);
        Call<JsonObject> call;
        final boolean updating = editingBirthdayId > 0;
        if (updating) {
            body.addProperty("birthday_id", editingBirthdayId);
            call = api.birthday_update("v3", body);
        } else {
            call = api.birthday_schedule("v3", body);
        }
        call.enqueue(
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
                            String okMsg =
                                    root.has("msg") && !root.get("msg").isJsonNull()
                                            ? root.get("msg").getAsString()
                                            : null;
                            if (TextUtils.isEmpty(okMsg)) {
                                okMsg =
                                        getString(
                                                updating
                                                        ? R.string.birthday_auto_updated_ok
                                                        : R.string.birthday_auto_scheduled_ok);
                            }
                            Toast.makeText(
                                            BirthdayAutoActivity.this,
                                            okMsg,
                                            Toast.LENGTH_LONG)
                                    .show();
                            clearEditingMode();
                            loadMyPosts();
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

    private void loadMyPosts() {
        if (myListContainer == null) return;
        long uid = resolveUserId();
        if (uid <= 0) {
            myListContainer.removeAllViews();
            if (myListEmpty != null) myListEmpty.setVisibility(View.VISIBLE);
            return;
        }
        JsonObject body = new JsonObject();
        body.addProperty("user_id", uid);
        body.addProperty("status", "all");
        body.addProperty("limit", 30);
        APIService api = RestApiClient.getClient().create(APIService.class);
        api.birthday_my("v3", body)
                .enqueue(
                        new Callback<JsonObject>() {
                            @Override
                            public void onResponse(
                                    Call<JsonObject> call, Response<JsonObject> response) {
                                if (isFinishing()) return;
                                try {
                                    JsonObject root = response.body();
                                    if (root == null
                                            || !root.has("success")
                                            || !root.get("success").getAsBoolean()) {
                                        return;
                                    }
                                    JsonArray arr = null;
                                    if (root.has("data") && root.get("data").isJsonArray()) {
                                        arr = root.getAsJsonArray("data");
                                    }
                                    renderMyPosts(arr);
                                } catch (Exception ignored) {
                                }
                            }

                            @Override
                            public void onFailure(Call<JsonObject> call, Throwable t) {}
                        });
    }

    private void renderMyPosts(@Nullable JsonArray arr) {
        if (myListContainer == null) return;
        myListContainer.removeAllViews();
        if (arr == null || arr.size() == 0) {
            if (myListEmpty != null) myListEmpty.setVisibility(View.VISIBLE);
            return;
        }
        if (myListEmpty != null) myListEmpty.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (JsonElement el : arr) {
            if (el == null || !el.isJsonObject()) continue;
            JsonObject row = el.getAsJsonObject();
            View item = inflater.inflate(R.layout.item_birthday_my_post, myListContainer, false);
            bindMyPostRow(item, row);
            myListContainer.addView(item);
        }
    }

    private void bindMyPostRow(View item, JsonObject row) {
        TextView nameTv = item.findViewById(R.id.birthday_my_name);
        TextView metaTv = item.findViewById(R.id.birthday_my_meta);
        TextView statusTv = item.findViewById(R.id.birthday_my_status);
        ImageView thumb = item.findViewById(R.id.birthday_my_thumb);
        View actions = item.findViewById(R.id.birthday_my_actions);
        Button editBtn = item.findViewById(R.id.birthday_my_edit);
        Button deleteBtn = item.findViewById(R.id.birthday_my_delete);
        TextView viewOnly = item.findViewById(R.id.birthday_my_view_only);

        String name = optString(row, "name");
        String date = optString(row, "schedule_date");
        String status = optString(row, "status");
        String poster = optString(row, "poster_url");
        if (TextUtils.isEmpty(poster)) poster = optString(row, "photo_url");
        long birthdayId = 0;
        try {
            if (row.has("birthday_id") && !row.get("birthday_id").isJsonNull()) {
                birthdayId = row.get("birthday_id").getAsLong();
            }
        } catch (Exception ignored) {
        }
        boolean canEdit =
                row.has("can_edit")
                        && !row.get("can_edit").isJsonNull()
                        && row.get("can_edit").getAsBoolean();
        boolean canDelete =
                row.has("can_delete")
                        && !row.get("can_delete").isJsonNull()
                        && row.get("can_delete").getAsBoolean();
        if (("scheduled".equals(status) || "pending".equals(status)) && !row.has("can_edit")) {
            canEdit = true;
            canDelete = true;
        }
        if ("published".equals(status) || "rejected".equals(status)) {
            canEdit = false;
            canDelete = false;
        }

        if (nameTv != null) nameTv.setText(name);
        if (metaTv != null) {
            metaTv.setText(
                    getString(
                            R.string.birthday_auto_meta_fmt,
                            date,
                            TextUtils.isEmpty(optString(row, "address"))
                                    ? "—"
                                    : optString(row, "address")));
        }
        if (statusTv != null) {
            int label = R.string.birthday_auto_status_scheduled;
            if ("published".equals(status)) label = R.string.birthday_auto_status_published;
            else if ("failed".equals(status)) label = R.string.birthday_auto_status_failed;
            else if ("pending".equals(status)) label = R.string.birthday_auto_status_pending;
            else if ("rejected".equals(status)) label = R.string.birthday_auto_status_rejected;
            statusTv.setText(getString(label));
            int color = 0xFFE63B19;
            if ("published".equals(status)) color = 0xFF059669;
            else if ("pending".equals(status)) color = 0xFFD97706;
            else if ("rejected".equals(status)) color = 0xFFDC2626;
            statusTv.setTextColor(color);
        }
        if (thumb != null && !TextUtils.isEmpty(poster)) {
            Glide.with(this).load(poster).centerCrop().into(thumb);
        }

        final long idFinal = birthdayId;
        final JsonObject rowFinal = row;
        if (canEdit || canDelete) {
            if (actions != null) actions.setVisibility(View.VISIBLE);
            if (viewOnly != null) viewOnly.setVisibility(View.GONE);
            if (editBtn != null) {
                editBtn.setVisibility(canEdit ? View.VISIBLE : View.GONE);
                editBtn.setOnClickListener(v -> beginEditFromRow(rowFinal));
            }
            if (deleteBtn != null) {
                deleteBtn.setVisibility(canDelete ? View.VISIBLE : View.GONE);
                deleteBtn.setOnClickListener(v -> confirmDelete(idFinal));
            }
        } else {
            if (actions != null) actions.setVisibility(View.GONE);
            if (viewOnly != null) viewOnly.setVisibility(View.VISIBLE);
        }
    }

    private void beginEditFromRow(JsonObject row) {
        long id = 0;
        try {
            id = row.get("birthday_id").getAsLong();
        } catch (Exception ignored) {
        }
        if (id <= 0) return;
        editingBirthdayId = id;
        updateSubmitLabel();
        String postedTemplate = optString(row, "template_id");
        if ("a".equalsIgnoreCase(postedTemplate) || "b".equalsIgnoreCase(postedTemplate)) {
            String current = ProfileHubConfigHelper.getBirthdayTemplateId(this);
            String url =
                    postedTemplate.equalsIgnoreCase(current)
                            ? ProfileHubConfigHelper.getBirthdayTemplateImageUrl(this)
                            : "";
            applyBirthdayTemplate(postedTemplate, url);
        }

        if (nameInput != null) nameInput.setText(optString(row, "name"));
        if (addressInput != null) addressInput.setText(optString(row, "address"));
        if (specialInput != null) specialInput.setText(optString(row, "special_text"));
        if (titleInput != null) titleInput.setText(optString(row, "title"));
        scheduleDate = optString(row, "schedule_date");
        if (pickDateBtn != null && !TextUtils.isEmpty(scheduleDate)) {
            pickDateBtn.setText(scheduleDate);
        }

        try {
            float z = row.has("zoom") ? (float) row.get("zoom").getAsDouble() : 1f;
            if (zoomBar != null) {
                int progress = Math.round(Math.max(0f, (z - 1f) * 10f));
                zoomBar.setProgress(Math.max(0, Math.min(20, progress)));
            }
            panX = row.has("pan_x") ? (float) row.get("pan_x").getAsDouble() : 0f;
            panY = row.has("pan_y") ? (float) row.get("pan_y").getAsDouble() : 0f;
            int rot =
                    row.has("photo_rotation")
                            ? row.get("photo_rotation").getAsInt()
                            : 0;
            setPhotoRotation(rot, false);
        } catch (Exception ignored) {
        }

        applyStyleFromJson(row, "name_style", true);
        applyStyleFromJson(row, "address_style", false);

        uploadedPhotoUrl = optString(row, "photo_url");
        photoUri = null;
        normalizedJpegFile = null;
        if (!TextUtils.isEmpty(uploadedPhotoUrl) && livePreview != null) {
            Glide.with(this)
                    .asBitmap()
                    .load(uploadedPhotoUrl)
                    .into(
                            new com.bumptech.glide.request.target.CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(
                                        Bitmap resource,
                                        com.bumptech.glide.request.transition.Transition<
                                                        ? super Bitmap>
                                                transition) {
                                    previewBitmap = resource;
                                    if (livePreview != null) {
                                        livePreview.setPhotoBitmap(resource);
                                        livePreview.setPan(panX, panY);
                                        livePreview.setPhotoRotation(photoRotation);
                                    }
                                    refreshLiveTexts();
                                    scheduleServerPreview();
                                }

                                @Override
                                public void onLoadCleared(
                                        @Nullable android.graphics.drawable.Drawable placeholder) {}
                            });
        }
        refreshLiveTexts();
        Toast.makeText(this, R.string.birthday_auto_update, Toast.LENGTH_SHORT).show();
        Button pickBtn = findViewById(R.id.birthday_auto_pick_photo);
        if (pickBtn != null && photoUri != null) {
            pickBtn.setText(R.string.birthday_change_photo);
        }
        scrollPhotoControlsIntoView();
    }

    /** Bring zoom + 0/90/180/270 into view after photo so rotate is never missed. */
    private void scrollPhotoControlsIntoView() {
        final View target = findViewById(R.id.birthday_auto_rotate_row);
        View parent = findViewById(R.id.birthday_auto_pick_photo);
        // Walk up to ScrollView
        View walk = target != null ? target : parent;
        ScrollView scrollView = null;
        while (walk != null) {
            if (walk instanceof ScrollView) {
                scrollView = (ScrollView) walk;
                break;
            }
            Object p = walk.getParent();
            walk = p instanceof View ? (View) p : null;
        }
        if (scrollView == null || target == null) return;
        final ScrollView sv = scrollView;
        sv.post(
                () -> {
                    try {
                        int y = target.getTop();
                        View p = (View) target.getParent();
                        while (p != null && p != sv) {
                            y += p.getTop();
                            Object gp = p.getParent();
                            p = gp instanceof View ? (View) gp : null;
                        }
                        // Nudge so rotate row sits just under the fold edge.
                        sv.smoothScrollTo(0, Math.max(0, y - 48));
                    } catch (Exception ignored) {
                    }
                });
    }

    private void applyStyleFromJson(JsonObject row, String key, boolean forName) {
        if (!row.has(key) || !row.get(key).isJsonObject()) return;
        JsonObject style = row.getAsJsonObject(key);
        String fontId = optString(style, "fontId");
        int size = 0;
        try {
            size = style.has("size") ? style.get("size").getAsInt() : 0;
        } catch (Exception ignored) {
        }
        boolean bold =
                style.has("bold")
                        && !style.get("bold").isJsonNull()
                        && style.get("bold").getAsBoolean();
        Spinner fontSpinner = forName ? nameFontSpinner : addrFontSpinner;
        Spinner sizeSpinner = forName ? nameSizeSpinner : addrSizeSpinner;
        CheckBox boldBox = forName ? nameBold : addrBold;
        if (fontSpinner != null && !TextUtils.isEmpty(fontId)) {
            fontSpinner.setSelection(BirthdayFonts.indexOf(fontId));
        }
        if (sizeSpinner != null && size > 0) {
            sizeSpinner.setSelection(BirthdayFonts.tierIndexForSize(forName, size));
        }
        if (boldBox != null) boldBox.setChecked(bold);
    }

    private void confirmDelete(long birthdayId) {
        if (birthdayId <= 0) return;
        new AlertDialog.Builder(this)
                .setMessage(R.string.birthday_auto_confirm_delete)
                .setPositiveButton(
                        R.string.birthday_auto_delete,
                        (d, w) -> deleteBirthday(birthdayId))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void deleteBirthday(long birthdayId) {
        long uid = resolveUserId();
        if (uid <= 0) return;
        setBusy(true);
        JsonObject body = new JsonObject();
        body.addProperty("user_id", uid);
        body.addProperty("birthday_id", birthdayId);
        APIService api = RestApiClient.getClient().create(APIService.class);
        api.birthday_cancel("v3", body)
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
                                    if (editingBirthdayId == birthdayId) clearEditingMode();
                                    Toast.makeText(
                                                    BirthdayAutoActivity.this,
                                                    R.string.birthday_auto_deleted_ok,
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                    loadMyPosts();
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

    private static String optString(JsonObject obj, String key) {
        try {
            if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return "";
            return obj.get(key).getAsString();
        } catch (Exception e) {
            return "";
        }
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
