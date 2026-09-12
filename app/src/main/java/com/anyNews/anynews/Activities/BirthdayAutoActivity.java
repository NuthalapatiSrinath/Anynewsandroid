package com.anyNews.anynews.Activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.DistrictLabelHelper;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fully automatic Birthday Special — logged-in users schedule a banner post.
 * Server composites the admin-style poster and cron publishes on the chosen date.
 */
public class BirthdayAutoActivity extends AppCompatActivity {

    private ImageView preview;
    private EditText nameInput;
    private EditText addressInput;
    private EditText specialInput;
    private EditText titleInput;
    private CheckBox nameBold;
    private CheckBox addrBold;
    private SeekBar zoomBar;
    private TextView scheduleInfo;
    private Button pickDateBtn;
    private ProgressBar progress;
    private Uri photoUri;
    private String scheduleDate = "";
    private String earliestDate = "";
    private String publishLabel = "02:00 IST";
    private String cutoffLabel = "00:00 IST";
    private boolean busy;

    private final ActivityResultLauncher<String> pickPhoto =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri == null) return;
                        photoUri = uri;
                        if (preview != null) {
                            Glide.with(this).load(uri).centerCrop().into(preview);
                        }
                    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SharedPrefsData.getBool(this, Constants.is_login, Constants.PREF_NAME)) {
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

        preview = findViewById(R.id.birthday_auto_preview);
        nameInput = findViewById(R.id.birthday_auto_name);
        addressInput = findViewById(R.id.birthday_auto_address);
        specialInput = findViewById(R.id.birthday_auto_special);
        titleInput = findViewById(R.id.birthday_auto_post_title);
        nameBold = findViewById(R.id.birthday_auto_name_bold);
        addrBold = findViewById(R.id.birthday_auto_addr_bold);
        zoomBar = findViewById(R.id.birthday_auto_zoom);
        scheduleInfo = findViewById(R.id.birthday_auto_schedule_info);
        pickDateBtn = findViewById(R.id.birthday_auto_pick_date);
        progress = findViewById(R.id.birthday_auto_progress);

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

        loadConfig();
    }

    private void autofillAddress() {
        String district = DistrictLabelHelper.resolveDisplayLabel(this);
        int stateId = SharedPrefsData.getInt(this, Constants.StateID, Constants.PREF_NAME);
        String address = DistrictLabelHelper.isUsable(district) ? district : "";
        if (stateId > 0 && !TextUtils.isEmpty(address)) {
            // Keep district; user can append state name if needed.
            address = address.trim();
        }
        if (addressInput != null) {
            addressInput.setText(address);
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
                                    if (root == null || !root.has("success") || !root.get("success").getAsBoolean()) {
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
        if (photoUri == null) {
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
        setBusy(true);
        uploadThenSchedule(name, address);
    }

    private void uploadThenSchedule(String name, String address) {
        try {
            InputStream is = getContentResolver().openInputStream(photoUri);
            if (is == null) {
                setBusy(false);
                Toast.makeText(this, R.string.advertise_upload_failed, Toast.LENGTH_SHORT).show();
                return;
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) > 0) bos.write(buf, 0, n);
            is.close();
            RequestBody req = RequestBody.create(MediaType.parse("image/jpeg"), bos.toByteArray());
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

    private void postSchedule(String name, String address, String photoUrl) {
        JsonObject body = new JsonObject();
        body.addProperty("name", name);
        body.addProperty("address", address);
        body.addProperty("photo_url", photoUrl);
        body.addProperty("schedule_date", scheduleDate);
        body.addProperty(
                "title",
                titleInput != null ? titleInput.getText().toString().trim() : ("Happy Birthday " + name));
        body.addProperty(
                "special_text",
                specialInput != null ? specialInput.getText().toString().trim() : "");
        float zoom = 1f;
        if (zoomBar != null) {
            zoom = 1f + (zoomBar.getProgress() / 10f);
        }
        body.addProperty("zoom", zoom);
        body.addProperty("pan_x", 0);
        body.addProperty("pan_y", 0);

        JsonObject nameStyle = new JsonObject();
        nameStyle.addProperty("fontId", "great-vibes");
        nameStyle.addProperty("size", 52);
        nameStyle.addProperty("bold", nameBold != null && nameBold.isChecked());
        body.add("name_style", nameStyle);

        JsonObject addrStyle = new JsonObject();
        addrStyle.addProperty("fontId", "great-vibes");
        addrStyle.addProperty("size", 40);
        addrStyle.addProperty("bold", addrBold != null && addrBold.isChecked());
        body.add("address_style", addrStyle);

        JsonObject specialStyle = new JsonObject();
        specialStyle.addProperty("fontId", "merriweather");
        specialStyle.addProperty("size", 15);
        specialStyle.addProperty("bold", false);
        body.add("special_style", specialStyle);

        try {
            String uid = SharedPrefsData.getString(this, Constants.USER_ID, Constants.PREF_NAME);
            if (!TextUtils.isEmpty(uid)) body.addProperty("user_id", uid);
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
            String mobile =
                    SharedPrefsData.getString(this, Constants.PhoneNumber, Constants.PREF_NAME);
            if (!TextUtils.isEmpty(mobile)) body.addProperty("mobile", mobile);
            String uname =
                    SharedPrefsData.getString(this, Constants.USER_NAME, Constants.PREF_NAME);
            if (!TextUtils.isEmpty(uname)) body.addProperty("user_name", uname);
        } catch (Exception ignored) {
        }

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
}
