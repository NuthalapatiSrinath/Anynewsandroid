package com.anyNews.anynews.Activities;

import android.app.Dialog;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.AdvertiseChatLabels;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.bumptech.glide.Glide;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Profile → Advertise on AnyNews / Birthday Special:
 * lead form, then WhatsApp-style chat (text + inline image/video).
 * Media URLs are stored server-side but never shown as bare text.
 */
public class AdvertiseOnAnyNewsActivity extends AppCompatActivity {

    public static final String EXTRA_MODE = "mode";
    public static final String MODE_ADVERTISE = "advertise";
    public static final String MODE_BIRTHDAY = "birthday";

    private static final String PREF_THREAD_ID = "advertise_thread_id";
    private static final String PREF_BIRTHDAY_THREAD_ID = "birthday_thread_id";

    private String mode = MODE_ADVERTISE;
    private boolean birthdayMode;

    private TextView titleView;
    private TextView offerText;
    private View formScroll;
    private LinearLayout form;
    private LinearLayout chat;
    private EditText nameInput;
    private EditText mobileInput;
    private EditText promoInput;
    private EditText locationInput;
    private EditText messageInput;
    private View birthdayPhotoWrap;
    private ImageView birthdayPhotoPreview;
    private TextView birthdayPhotoHint;
    private TextView startBtn;
    private RecyclerView messagesList;
    private MessagesAdapter adapter;
    private long threadId;
    private boolean busy;
    private boolean systemBarInsetsInstalled;
    private boolean messagesLoading;
    private Uri birthdayPhotoUri;
    private String pendingPickKind = "image"; // image | video | birthday_photo

    private final ActivityResultLauncher<String> pickMedia =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri == null) return;
                        if ("birthday_photo".equals(pendingPickKind)) {
                            birthdayPhotoUri = uri;
                            if (birthdayPhotoPreview != null) {
                                Glide.with(this).load(uri).centerCrop().into(birthdayPhotoPreview);
                            }
                            if (birthdayPhotoHint != null) {
                                birthdayPhotoHint.setVisibility(View.GONE);
                            }
                        } else {
                            uploadAndSendMedia(uri, pendingPickKind);
                        }
                    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertise_on_anynews);
        applySystemBarInsets();

        String extra = getIntent() != null ? getIntent().getStringExtra(EXTRA_MODE) : null;
        mode = MODE_BIRTHDAY.equalsIgnoreCase(extra) ? MODE_BIRTHDAY : MODE_ADVERTISE;
        birthdayMode = MODE_BIRTHDAY.equals(mode);

        titleView = findViewById(R.id.advertise_title);
        offerText = findViewById(R.id.advertise_offer_text);
        formScroll = findViewById(R.id.advertise_form_scroll);
        form = findViewById(R.id.advertise_form);
        chat = findViewById(R.id.advertise_chat);
        nameInput = findViewById(R.id.advertise_name);
        mobileInput = findViewById(R.id.advertise_mobile);
        promoInput = findViewById(R.id.advertise_promo);
        locationInput = findViewById(R.id.birthday_location);
        messageInput = findViewById(R.id.advertise_input);
        birthdayPhotoWrap = findViewById(R.id.birthday_photo_wrap);
        birthdayPhotoPreview = findViewById(R.id.birthday_photo_preview);
        birthdayPhotoHint = findViewById(R.id.birthday_photo_hint);
        startBtn = findViewById(R.id.advertise_start_btn);
        messagesList = findViewById(R.id.advertise_messages);

        if (offerText != null) {
            offerText.setMovementMethod(new ScrollingMovementMethod());
        }

        ImageButton back = findViewById(R.id.advertise_back);
        if (back != null) back.setOnClickListener(v -> finish());

        if (startBtn != null) startBtn.setOnClickListener(v -> startThread());

        TextView sendBtn = findViewById(R.id.advertise_send);
        if (sendBtn != null) sendBtn.setOnClickListener(v -> sendText());

        ImageButton attach = findViewById(R.id.advertise_attach);
        if (attach != null) {
            attach.setOnClickListener(v -> showAttachChooser());
        }

        if (birthdayPhotoWrap != null) {
            birthdayPhotoWrap.setOnClickListener(
                    v -> {
                        pendingPickKind = "birthday_photo";
                        pickMedia.launch("image/*");
                    });
        }

        adapter = new MessagesAdapter();
        if (messagesList != null) {
            messagesList.setLayoutManager(new LinearLayoutManager(this));
            messagesList.setAdapter(adapter);
        }

        applyModeChrome();
        prefillIdentity();

        if (!birthdayMode) {
            loadOffer();
        }

        threadId =
                SharedPrefsData.getLong(
                        this,
                        birthdayMode ? PREF_BIRTHDAY_THREAD_ID : PREF_THREAD_ID,
                        Constants.PREF_NAME);
        if (threadId > 0) {
            showChat();
            loadMessages(false);
        }
    }

    private void applyModeChrome() {
        if (titleView != null) {
            titleView.setText(
                    birthdayMode
                            ? AdvertiseChatLabels.birthdayTitle(this)
                            : AdvertiseChatLabels.advertiseTitle(this));
        }
        if (birthdayMode) {
            if (offerText != null) {
                offerText.setText(AdvertiseChatLabels.birthdayOffer(this));
                offerText.setTextColor(Color.parseColor("#FF6B00"));
            }
            if (promoInput != null) promoInput.setVisibility(View.GONE);
            if (locationInput != null) {
                locationInput.setVisibility(View.VISIBLE);
                locationInput.setHint(AdvertiseChatLabels.hintLocation(this));
            }
            if (birthdayPhotoWrap != null) birthdayPhotoWrap.setVisibility(View.VISIBLE);
            if (birthdayPhotoHint != null) {
                birthdayPhotoHint.setText(AdvertiseChatLabels.pickPhoto(this));
                birthdayPhotoHint.setVisibility(View.VISIBLE);
            }
            if (startBtn != null) startBtn.setText(AdvertiseChatLabels.startBirthday(this));
        } else {
            if (promoInput != null) promoInput.setVisibility(View.VISIBLE);
            if (locationInput != null) locationInput.setVisibility(View.GONE);
            if (birthdayPhotoWrap != null) birthdayPhotoWrap.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        applySystemBarInsets();
        if (threadId > 0) {
            loadMessages(adapter != null && adapter.lastId() > 0);
        }
    }

    private void applySystemBarInsets() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        int orange = Color.parseColor("#FFFF6B00");
        getWindow().setStatusBarColor(orange);
        getWindow().setNavigationBarColor(Color.parseColor("#FFF7F2"));

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(false);
            boolean lightNav =
                    (getResources().getConfiguration().uiMode
                                    & Configuration.UI_MODE_NIGHT_MASK)
                            != Configuration.UI_MODE_NIGHT_YES;
            controller.setAppearanceLightNavigationBars(lightNav);
        }

        final View root = findViewById(R.id.advertise_root);
        final View header = findViewById(R.id.advertise_header);
        if (root == null || header == null) return;

        if (!systemBarInsetsInstalled) {
            systemBarInsetsInstalled = true;
            final int baseHeaderHeightPx = header.getLayoutParams().height;
            final int headerPadL = header.getPaddingLeft();
            final int headerPadT = header.getPaddingTop();
            final int headerPadR = header.getPaddingRight();
            final int headerPadB = header.getPaddingBottom();
            final int rootPadBottom = root.getPaddingBottom();

            ViewCompat.setOnApplyWindowInsetsListener(
                    root,
                    (v, windowInsets) -> {
                        Insets status =
                                windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());
                        Insets bars =
                                windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                        int top = status.top;
                        if (baseHeaderHeightPx > 0) {
                            ViewGroup.LayoutParams lp = header.getLayoutParams();
                            lp.height = baseHeaderHeightPx + top;
                            header.setLayoutParams(lp);
                        }
                        header.setPadding(
                                headerPadL + bars.left,
                                headerPadT + top,
                                headerPadR + bars.right,
                                headerPadB);
                        v.setPadding(
                                v.getPaddingLeft(),
                                v.getPaddingTop(),
                                v.getPaddingRight(),
                                rootPadBottom + bars.bottom);
                        return WindowInsetsCompat.CONSUMED;
                    });
        }
        ViewCompat.requestApplyInsets(root);
    }

    private void prefillIdentity() {
        try {
            String name =
                    SharedPrefsData.getString(this, Constants.USER_NAME, Constants.PREF_NAME);
            String mobile =
                    SharedPrefsData.getString(
                            this, Constants.Selected_Phone_Number, Constants.PREF_NAME);
            if (nameInput != null && !TextUtils.isEmpty(name)) nameInput.setText(name);
            if (mobileInput != null && !TextUtils.isEmpty(mobile)) mobileInput.setText(mobile);
        } catch (Exception ignored) {
        }
    }

    private void loadOffer() {
        APIService api = RestApiClient.getClient().create(APIService.class);
        JsonObject body = new JsonObject();
        api.advertise_offer("v3", body)
                .enqueue(
                        new Callback<JsonObject>() {
                            @Override
                            public void onResponse(
                                    Call<JsonObject> call, Response<JsonObject> response) {
                                try {
                                    JsonObject root = response.body();
                                    if (root == null || !root.has("data")) return;
                                    applyOffer(root.getAsJsonObject("data"));
                                } catch (Exception ignored) {
                                }
                            }

                            @Override
                            public void onFailure(Call<JsonObject> call, Throwable t) {}
                        });
    }

    private void applyOffer(JsonObject d) {
        if (offerText == null || d == null || birthdayMode) return;
        String text =
                d.has("offer_text") && !d.get("offer_text").isJsonNull()
                        ? d.get("offer_text").getAsString()
                        : getString(R.string.advertise_offer_default);
        offerText.setText(text);
        try {
            String color =
                    d.has("offer_color") ? d.get("offer_color").getAsString() : "#FF6B00";
            offerText.setTextColor(Color.parseColor(color));
        } catch (Exception e) {
            offerText.setTextColor(Color.parseColor("#FF6B00"));
        }
        try {
            float sp =
                    d.has("offer_size_sp")
                            ? (float) d.get("offer_size_sp").getAsDouble()
                            : 15f;
            offerText.setTextSize(TypedValue.COMPLEX_UNIT_SP, Math.max(10f, Math.min(28f, sp)));
        } catch (Exception ignored) {
        }
        try {
            String w =
                    d.has("offer_weight") ? d.get("offer_weight").getAsString() : "normal";
            offerText.setTypeface(
                    null,
                    "bold".equalsIgnoreCase(w) ? Typeface.BOLD : Typeface.NORMAL);
        } catch (Exception ignored) {
        }
    }

    private void startThread() {
        if (busy) return;
        String name = nameInput != null ? nameInput.getText().toString().trim() : "";
        String mobile = mobileInput != null ? mobileInput.getText().toString().trim() : "";
        if (name.length() < 2) {
            Toast.makeText(this, R.string.advertise_need_name, Toast.LENGTH_SHORT).show();
            return;
        }
        if (mobile.replaceAll("\\D", "").length() < 10) {
            Toast.makeText(this, R.string.advertise_need_mobile, Toast.LENGTH_SHORT).show();
            return;
        }
        if (birthdayMode) {
            String location =
                    locationInput != null ? locationInput.getText().toString().trim() : "";
            if (location.length() < 2) {
                Toast.makeText(this, AdvertiseChatLabels.needLocation(this), Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            if (birthdayPhotoUri == null) {
                Toast.makeText(this, AdvertiseChatLabels.needPhoto(this), Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            busy = true;
            Toast.makeText(this, R.string.advertise_uploading, Toast.LENGTH_SHORT).show();
            uploadBirthdayThenStart(name, mobile, location, birthdayPhotoUri);
            return;
        }

        String promo = promoInput != null ? promoInput.getText().toString().trim() : "";
        if (promo.length() < 3) {
            Toast.makeText(this, R.string.advertise_need_promo, Toast.LENGTH_SHORT).show();
            return;
        }
        busy = true;
        postStartThread(name, mobile, promo, null, null);
    }

    private void uploadBirthdayThenStart(
            String name, String mobile, String location, Uri photoUri) {
        try {
            InputStream is = getContentResolver().openInputStream(photoUri);
            if (is == null) {
                busy = false;
                Toast.makeText(this, R.string.advertise_upload_failed, Toast.LENGTH_SHORT).show();
                return;
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) > 0) bos.write(buf, 0, n);
            is.close();
            RequestBody req =
                    RequestBody.create(MediaType.parse("image/jpeg"), bos.toByteArray());
            MultipartBody.Part part =
                    MultipartBody.Part.createFormData(
                            "file", "bday_" + System.currentTimeMillis() + ".jpg", req);
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
                                            busy = false;
                                            Toast.makeText(
                                                            AdvertiseOnAnyNewsActivity.this,
                                                            R.string.advertise_upload_failed,
                                                            Toast.LENGTH_SHORT)
                                                    .show();
                                            return;
                                        }
                                        postStartThread(name, mobile, null, location, url);
                                    } catch (Exception e) {
                                        busy = false;
                                        Toast.makeText(
                                                        AdvertiseOnAnyNewsActivity.this,
                                                        R.string.advertise_upload_failed,
                                                        Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<JsonObject> call, Throwable t) {
                                    busy = false;
                                    Toast.makeText(
                                                    AdvertiseOnAnyNewsActivity.this,
                                                    R.string.advertise_upload_failed,
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                }
                            });
        } catch (Exception e) {
            busy = false;
            Toast.makeText(this, R.string.advertise_upload_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void postStartThread(
            String name, String mobile, String promo, String location, String photoUrl) {
        JsonObject body = new JsonObject();
        body.addProperty("name", name);
        body.addProperty("mobile", mobile);
        body.addProperty("kind", birthdayMode ? MODE_BIRTHDAY : MODE_ADVERTISE);
        if (birthdayMode) {
            body.addProperty("location", location != null ? location : "");
            body.addProperty("photo_url", photoUrl != null ? photoUrl : "");
            body.addProperty(
                    "promotion_description",
                    "Birthday Special — " + (location != null ? location : ""));
        } else {
            body.addProperty("promotion_description", promo != null ? promo : "");
        }
        try {
            int draft =
                    SharedPrefsData.getInt(this, Constants.DraftID, Constants.PREF_NAME);
            // Birthday must not attach news DraftID — it rotates and denied media send.
            if (!birthdayMode && draft > 0) body.addProperty("draft_id", draft);
            String uid = SharedPrefsData.getString(this, Constants.USER_ID, Constants.PREF_NAME);
            if (!TextUtils.isEmpty(uid)) body.addProperty("user_id", uid);
        } catch (Exception ignored) {
        }
        APIService api = RestApiClient.getClient().create(APIService.class);
        api.advertise_start("v3", body)
                .enqueue(
                        new Callback<JsonObject>() {
                            @Override
                            public void onResponse(
                                    Call<JsonObject> call, Response<JsonObject> response) {
                                busy = false;
                                try {
                                    JsonObject root = response.body();
                                    if (root == null
                                            || !root.has("success")
                                            || !root.get("success").getAsBoolean()) {
                                        String msg =
                                                root != null && root.has("msg")
                                                        ? root.get("msg").getAsString()
                                                        : "Failed";
                                        Toast.makeText(
                                                        AdvertiseOnAnyNewsActivity.this,
                                                        msg,
                                                        Toast.LENGTH_SHORT)
                                                .show();
                                        return;
                                    }
                                    JsonObject data = root.getAsJsonObject("data");
                                    JsonObject thread = data.getAsJsonObject("thread");
                                    threadId = thread.get("thread_id").getAsLong();
                                    SharedPrefsData.putLong(
                                            AdvertiseOnAnyNewsActivity.this,
                                            birthdayMode
                                                    ? PREF_BIRTHDAY_THREAD_ID
                                                    : PREF_THREAD_ID,
                                            threadId,
                                            Constants.PREF_NAME);
                                    showChat();
                                    JsonArray msgs = data.getAsJsonArray("messages");
                                    adapter.setAll(parseMessages(msgs));
                                    scrollToEnd();
                                } catch (Exception e) {
                                    Toast.makeText(
                                                    AdvertiseOnAnyNewsActivity.this,
                                                    "Failed",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }

                            @Override
                            public void onFailure(Call<JsonObject> call, Throwable t) {
                                busy = false;
                                Toast.makeText(
                                                AdvertiseOnAnyNewsActivity.this,
                                                "Network error",
                                                Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });
    }

    private void showChat() {
        if (formScroll != null) formScroll.setVisibility(View.GONE);
        if (form != null) form.setVisibility(View.GONE);
        if (chat != null) chat.setVisibility(View.VISIBLE);
    }

    private void loadMessages(boolean deltaOnly) {
        if (threadId <= 0 || messagesLoading) return;
        messagesLoading = true;
        final boolean useAfter = deltaOnly && adapter.lastId() > 0;
        final long afterId = useAfter ? adapter.lastId() : 0;
        JsonObject body = new JsonObject();
        body.addProperty("thread_id", threadId);
        if (useAfter) {
            body.addProperty("after_id", afterId);
        }
        body.addProperty("limit", 80);
        APIService api = RestApiClient.getClient().create(APIService.class);
        api.advertise_messages("v3", body)
                .enqueue(
                        new Callback<JsonObject>() {
                            @Override
                            public void onResponse(
                                    Call<JsonObject> call, Response<JsonObject> response) {
                                messagesLoading = false;
                                try {
                                    JsonObject root = response.body();
                                    if (root == null || !root.has("data")) return;
                                    JsonArray msgs =
                                            root.getAsJsonObject("data").getAsJsonArray("messages");
                                    List<ChatMsg> list = parseMessages(msgs);
                                    if (useAfter) {
                                        adapter.append(list);
                                    } else {
                                        adapter.setAll(list);
                                    }
                                    scrollToEnd();
                                } catch (Exception ignored) {
                                }
                            }

                            @Override
                            public void onFailure(Call<JsonObject> call, Throwable t) {
                                messagesLoading = false;
                            }
                        });
    }

    private void sendText() {
        if (busy || threadId <= 0) return;
        String text = messageInput != null ? messageInput.getText().toString().trim() : "";
        if (TextUtils.isEmpty(text)) return;
        sendMessage("text", text, "");
        if (messageInput != null) messageInput.setText("");
    }

    private void showAttachChooser() {
        if (busy || threadId <= 0) return;
        String[] items =
                new String[] {
                    AdvertiseChatLabels.pickImage(this), AdvertiseChatLabels.pickVideo(this)
                };
        new AlertDialog.Builder(this)
                .setItems(
                        items,
                        (d, which) -> {
                            if (which == 0) {
                                pendingPickKind = "image";
                                pickMedia.launch("image/*");
                            } else {
                                pendingPickKind = "video";
                                pickMedia.launch("video/*");
                            }
                        })
                .show();
    }

    private void uploadAndSendMedia(Uri uri, String kind) {
        if (busy || threadId <= 0) return;
        busy = true;
        Toast.makeText(this, R.string.advertise_uploading, Toast.LENGTH_SHORT).show();
        try {
            String mime = getContentResolver().getType(uri);
            if (mime == null) {
                mime = "video".equals(kind) ? "video/mp4" : "image/jpeg";
            }
            String type = mime.startsWith("video") || "video".equals(kind) ? "video" : "image";
            String name =
                    "adv_"
                            + System.currentTimeMillis()
                            + (type.equals("video") ? ".mp4" : ".jpg");
            InputStream is = getContentResolver().openInputStream(uri);
            if (is == null) {
                busy = false;
                return;
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) > 0) bos.write(buf, 0, n);
            is.close();
            RequestBody req = RequestBody.create(MediaType.parse(mime), bos.toByteArray());
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", name, req);
            APIService api = RestApiClient.getClient().create(APIService.class);
            api.uploadFile(part)
                    .enqueue(
                            new Callback<JsonObject>() {
                                @Override
                                public void onResponse(
                                        Call<JsonObject> call, Response<JsonObject> response) {
                                    busy = false;
                                    try {
                                        String url = extractUploadUrl(response.body());
                                        if (TextUtils.isEmpty(url)) {
                                            Toast.makeText(
                                                            AdvertiseOnAnyNewsActivity.this,
                                                            R.string.advertise_upload_failed,
                                                            Toast.LENGTH_SHORT)
                                                    .show();
                                            return;
                                        }
                                        sendMessage(type, "", url);
                                    } catch (Exception e) {
                                        Toast.makeText(
                                                        AdvertiseOnAnyNewsActivity.this,
                                                        R.string.advertise_upload_failed,
                                                        Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<JsonObject> call, Throwable t) {
                                    busy = false;
                                    Toast.makeText(
                                                    AdvertiseOnAnyNewsActivity.this,
                                                    R.string.advertise_upload_failed,
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                }
                            });
        } catch (Exception e) {
            busy = false;
            Toast.makeText(this, R.string.advertise_upload_failed, Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    private static String extractUploadUrl(@Nullable JsonObject root) {
        if (root == null) return null;
        try {
            if (root.has("extras") && root.getAsJsonObject("extras").has("url")) {
                return root.getAsJsonObject("extras").get("url").getAsString();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void sendMessage(String type, String text, String mediaUrl) {
        if (busy || threadId <= 0) return;
        busy = true;
        JsonObject body = new JsonObject();
        body.addProperty("thread_id", threadId);
        body.addProperty("type", type != null ? type : "text");
        body.addProperty("text", text != null ? text : "");
        if (!TextUtils.isEmpty(mediaUrl)) {
            body.addProperty("media_url", mediaUrl);
        }
        try {
            // Prefer user_id ownership. Never send news DraftID for birthday chat.
            String uid = SharedPrefsData.getString(this, Constants.USER_ID, Constants.PREF_NAME);
            if (!TextUtils.isEmpty(uid)) body.addProperty("user_id", uid);
            if (!birthdayMode) {
                int draft =
                        SharedPrefsData.getInt(this, Constants.DraftID, Constants.PREF_NAME);
                if (draft > 0) body.addProperty("draft_id", draft);
            }
        } catch (Exception ignored) {
        }
        APIService api = RestApiClient.getClient().create(APIService.class);
        api.advertise_send("v3", body)
                .enqueue(
                        new Callback<JsonObject>() {
                            @Override
                            public void onResponse(
                                    Call<JsonObject> call, Response<JsonObject> response) {
                                busy = false;
                                try {
                                    JsonObject root = response.body();
                                    if (root != null
                                            && root.has("success")
                                            && root.get("success").getAsBoolean()) {
                                        JsonObject msg =
                                                root.getAsJsonObject("data")
                                                        .getAsJsonObject("message");
                                        adapter.appendOne(parseOne(msg));
                                        scrollToEnd();
                                    } else {
                                        String m =
                                                root != null && root.has("msg")
                                                        ? root.get("msg").getAsString()
                                                        : "Send failed";
                                        Toast.makeText(
                                                        AdvertiseOnAnyNewsActivity.this,
                                                        m,
                                                        Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(
                                                    AdvertiseOnAnyNewsActivity.this,
                                                    "Send failed",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }

                            @Override
                            public void onFailure(Call<JsonObject> call, Throwable t) {
                                busy = false;
                                Toast.makeText(
                                                AdvertiseOnAnyNewsActivity.this,
                                                "Network error",
                                                Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });
    }

    private void scrollToEnd() {
        if (messagesList == null || adapter.getItemCount() == 0) return;
        messagesList.post(
                () -> messagesList.scrollToPosition(adapter.getItemCount() - 1));
    }

    private void openMediaFullscreen(ChatMsg m) {
        if (m == null || TextUtils.isEmpty(m.mediaUrl)) return;
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        root.setLayoutParams(
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

        if ("video".equals(m.type)) {
            VideoView vv = new VideoView(this);
            FrameLayout.LayoutParams lp =
                    new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            Gravity.CENTER);
            root.addView(vv, lp);
            vv.setVideoURI(Uri.parse(m.mediaUrl));
            vv.setOnPreparedListener(
                    mp -> {
                        mp.setLooping(true);
                        vv.start();
                    });
            dialog.setOnDismissListener(d -> {
                try {
                    vv.stopPlayback();
                } catch (Exception ignored) {
                }
            });
        } else {
            ImageView iv = new ImageView(this);
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            root.addView(
                    iv,
                    new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
            Glide.with(this).load(m.mediaUrl).into(iv);
        }

        ImageButton close = new ImageButton(this);
        close.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        close.setBackgroundColor(Color.TRANSPARENT);
        close.setColorFilter(Color.WHITE);
        FrameLayout.LayoutParams closeLp =
                new FrameLayout.LayoutParams(
                        (int) dp(44), (int) dp(44), Gravity.TOP | Gravity.START);
        closeLp.setMargins((int) dp(12), (int) dp(12), 0, 0);
        root.addView(close, closeLp);
        close.setOnClickListener(v -> dialog.dismiss());
        root.setOnClickListener(v -> dialog.dismiss());

        dialog.setContentView(root);
        Window w = dialog.getWindow();
        if (w != null) {
            w.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
        dialog.show();
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }

    private List<ChatMsg> parseMessages(JsonArray arr) {
        List<ChatMsg> out = new ArrayList<>();
        if (arr == null) return out;
        for (JsonElement el : arr) {
            if (el != null && el.isJsonObject()) {
                ChatMsg m = parseOne(el.getAsJsonObject());
                if (m != null) out.add(m);
            }
        }
        return out;
    }

    private ChatMsg parseOne(JsonObject o) {
        if (o == null) return null;
        ChatMsg m = new ChatMsg();
        m.id = o.has("message_id") ? o.get("message_id").getAsLong() : 0;
        m.sender = o.has("sender") ? o.get("sender").getAsString() : "user";
        m.type = o.has("type") ? o.get("type").getAsString() : "text";
        m.text = o.has("text") && !o.get("text").isJsonNull() ? o.get("text").getAsString() : "";
        m.mediaUrl =
                o.has("media_url") && !o.get("media_url").isJsonNull()
                        ? o.get("media_url").getAsString()
                        : "";
        m.createdAtMs = parseCreatedAtMs(o);
        return m;
    }

    private static long parseCreatedAtMs(JsonObject o) {
        if (o == null || !o.has("createdAt") || o.get("createdAt").isJsonNull()) {
            return System.currentTimeMillis();
        }
        try {
            JsonElement el = o.get("createdAt");
            if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) {
                return el.getAsLong();
            }
            String s = el.getAsString();
            if (TextUtils.isEmpty(s)) return System.currentTimeMillis();
            SimpleDateFormat[] formats =
                    new SimpleDateFormat[] {
                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US),
                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US),
                    };
            for (SimpleDateFormat f : formats) {
                f.setTimeZone(TimeZone.getTimeZone("UTC"));
                try {
                    Date d = f.parse(s);
                    if (d != null) return d.getTime();
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
        return System.currentTimeMillis();
    }

    static final class ChatMsg {
        long id;
        String sender;
        String type;
        String text;
        String mediaUrl;
        long createdAtMs;
    }

    final class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.VH> {
        private final List<ChatMsg> items = new ArrayList<>();
        private final Set<Long> seenIds = new HashSet<>();
        private final SimpleDateFormat timeFmt =
                new SimpleDateFormat("h:mm a", Locale.getDefault());

        void setAll(List<ChatMsg> list) {
            items.clear();
            seenIds.clear();
            if (list != null) {
                for (ChatMsg m : list) {
                    if (m == null) continue;
                    if (m.id > 0 && !seenIds.add(m.id)) continue;
                    items.add(m);
                }
            }
            notifyDataSetChanged();
        }

        void append(List<ChatMsg> list) {
            if (list == null || list.isEmpty()) return;
            int start = items.size();
            int added = 0;
            for (ChatMsg m : list) {
                if (m == null) continue;
                if (m.id > 0 && !seenIds.add(m.id)) continue;
                items.add(m);
                added++;
            }
            if (added > 0) notifyItemRangeInserted(start, added);
        }

        void appendOne(ChatMsg m) {
            if (m == null) return;
            if (m.id > 0 && !seenIds.add(m.id)) return;
            items.add(m);
            notifyItemInserted(items.size() - 1);
        }

        long lastId() {
            if (items.isEmpty()) return 0;
            return items.get(items.size() - 1).id;
        }

        private GradientDrawable bubbleBg(boolean mine) {
            GradientDrawable gd = new GradientDrawable();
            gd.setShape(GradientDrawable.RECTANGLE);
            float r = dp(16);
            float tip = dp(4);
            if (mine) {
                gd.setColor(Color.parseColor("#FFFF6B00"));
                gd.setCornerRadii(new float[] {r, r, tip, tip, r, r, r, r});
            } else {
                gd.setColor(Color.parseColor("#FFFFFFFF"));
                gd.setCornerRadii(new float[] {tip, tip, r, r, r, r, r, r});
            }
            return gd;
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            LinearLayout row = new LinearLayout(parent.getContext());
            row.setOrientation(LinearLayout.VERTICAL);
            RecyclerView.LayoutParams rowLp =
                    new RecyclerView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
            rowLp.bottomMargin = (int) dp(8);
            row.setLayoutParams(rowLp);

            LinearLayout bubble = new LinearLayout(parent.getContext());
            bubble.setOrientation(LinearLayout.VERTICAL);
            int padH = (int) dp(8);
            int padV = (int) dp(6);
            bubble.setPadding(padH, padV, padH, padV);
            // Fixed bubble width — WRAP_CONTENT + MATCH_PARENT media collapses to a thin
            // orange bar (WhatsApp-style image bubbles need a real width).
            int bubbleW =
                    Math.max(
                            (int) dp(200),
                            (int) (parent.getResources().getDisplayMetrics().widthPixels * 0.72f));
            LinearLayout.LayoutParams bubbleLp =
                    new LinearLayout.LayoutParams(bubbleW, ViewGroup.LayoutParams.WRAP_CONTENT);

            FrameLayout mediaFrame = new FrameLayout(parent.getContext());
            ImageView media = new ImageView(parent.getContext());
            media.setScaleType(ImageView.ScaleType.CENTER_CROP);
            media.setAdjustViewBounds(false);
            int mediaH = (int) dp(200);
            FrameLayout.LayoutParams mediaLp =
                    new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mediaH);
            mediaFrame.addView(media, mediaLp);

            TextView playOverlay = new TextView(parent.getContext());
            playOverlay.setText(AdvertiseChatLabels.tapToPlay(AdvertiseOnAnyNewsActivity.this));
            playOverlay.setTextColor(Color.WHITE);
            playOverlay.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
            playOverlay.setTypeface(null, Typeface.BOLD);
            playOverlay.setGravity(Gravity.CENTER);
            playOverlay.setBackgroundColor(Color.parseColor("#66000000"));
            mediaFrame.addView(
                    playOverlay,
                    new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));

            TextView text = new TextView(parent.getContext());
            text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.5f);
            int tPad = (int) dp(4);
            text.setPadding(tPad, tPad, tPad, 0);

            bubble.addView(
                    mediaFrame,
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, mediaH));
            bubble.addView(
                    text,
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

            TextView time = new TextView(parent.getContext());
            time.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
            time.setTextColor(Color.parseColor("#FF9A8F86"));
            LinearLayout.LayoutParams timeLp =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
            timeLp.topMargin = (int) dp(2);
            time.setLayoutParams(timeLp);

            row.addView(bubble, bubbleLp);
            row.addView(time);
            return new VH(row, bubble, mediaFrame, media, playOverlay, text, time);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            ChatMsg m = items.get(position);
            boolean mine = !"admin".equals(m.sender);
            holder.row.setGravity(mine ? Gravity.END : Gravity.START);
            holder.bubble.setBackground(bubbleBg(mine));
            holder.text.setTextColor(mine ? Color.WHITE : Color.parseColor("#FF1C1917"));

            boolean hasMedia =
                    !TextUtils.isEmpty(m.mediaUrl)
                            && ("image".equals(m.type) || "video".equals(m.type));
            LinearLayout.LayoutParams bubbleLp =
                    (LinearLayout.LayoutParams) holder.bubble.getLayoutParams();
            if (bubbleLp != null) {
                if (hasMedia) {
                    int bubbleW =
                            Math.max(
                                    (int) dp(200),
                                    (int)
                                            (holder.row.getResources()
                                                            .getDisplayMetrics()
                                                            .widthPixels
                                                    * 0.72f));
                    bubbleLp.width = bubbleW;
                } else {
                    bubbleLp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                }
                holder.bubble.setLayoutParams(bubbleLp);
            }
            if (hasMedia) {
                holder.mediaFrame.setVisibility(View.VISIBLE);
                // Placeholder so the bubble stays full-width before Glide paints.
                holder.media.setBackgroundColor(Color.parseColor("#33FFFFFF"));
                Glide.with(holder.media.getContext())
                        .load(m.mediaUrl)
                        .centerCrop()
                        .into(holder.media);
                holder.playOverlay.setVisibility(
                        "video".equals(m.type) ? View.VISIBLE : View.GONE);
                holder.mediaFrame.setOnClickListener(v -> openMediaFullscreen(m));
            } else {
                holder.mediaFrame.setVisibility(View.GONE);
                holder.mediaFrame.setOnClickListener(null);
                holder.media.setBackgroundColor(Color.TRANSPARENT);
                Glide.with(holder.media.getContext()).clear(holder.media);
            }

            if (!TextUtils.isEmpty(m.text)) {
                holder.text.setVisibility(View.VISIBLE);
                holder.text.setText(m.text);
            } else if (!hasMedia) {
                holder.text.setVisibility(View.VISIBLE);
                holder.text.setText("");
            } else {
                // Media-only bubble — never show "[image] url"
                holder.text.setVisibility(View.GONE);
            }

            holder.time.setText(timeFmt.format(new Date(m.createdAtMs)));
            LinearLayout.LayoutParams timeLp =
                    (LinearLayout.LayoutParams) holder.time.getLayoutParams();
            timeLp.gravity = mine ? Gravity.END : Gravity.START;
            holder.time.setLayoutParams(timeLp);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class VH extends RecyclerView.ViewHolder {
            final LinearLayout row;
            final LinearLayout bubble;
            final FrameLayout mediaFrame;
            final ImageView media;
            final TextView playOverlay;
            final TextView text;
            final TextView time;

            VH(
                    LinearLayout itemView,
                    LinearLayout bubble,
                    FrameLayout mediaFrame,
                    ImageView media,
                    TextView playOverlay,
                    TextView text,
                    TextView time) {
                super(itemView);
                this.row = itemView;
                this.bubble = bubble;
                this.mediaFrame = mediaFrame;
                this.media = media;
                this.playOverlay = playOverlay;
                this.text = text;
                this.time = time;
            }
        }
    }
}
