package com.anyNews.anynews.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Profile → Advertise on AnyNews: offer banner, lead form, then chat
 * (text / image / video / voice). No continuous polling — refresh on resume only.
 */
public class AdvertiseOnAnyNewsActivity extends AppCompatActivity {

    private static final String PREF_THREAD_ID = "advertise_thread_id";

    private TextView offerText;
    private View formScroll;
    private LinearLayout form;
    private LinearLayout chat;
    private EditText nameInput;
    private EditText mobileInput;
    private EditText promoInput;
    private EditText messageInput;
    private RecyclerView messagesList;
    private MessagesAdapter adapter;
    private long threadId;
    private boolean busy;
    private MediaRecorder recorder;
    private File voiceFile;
    private boolean recording;

    private final ActivityResultLauncher<String> pickMedia =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) uploadAndSendMedia(uri);
                    });

    private final ActivityResultLauncher<String> askMic =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        if (granted) toggleVoiceRecord();
                        else
                            Toast.makeText(this, R.string.advertise_mic_denied, Toast.LENGTH_SHORT)
                                    .show();
                    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertise_on_anynews);

        offerText = findViewById(R.id.advertise_offer_text);
        formScroll = findViewById(R.id.advertise_form_scroll);
        form = findViewById(R.id.advertise_form);
        chat = findViewById(R.id.advertise_chat);
        nameInput = findViewById(R.id.advertise_name);
        mobileInput = findViewById(R.id.advertise_mobile);
        promoInput = findViewById(R.id.advertise_promo);
        messageInput = findViewById(R.id.advertise_input);
        messagesList = findViewById(R.id.advertise_messages);

        if (offerText != null) {
            offerText.setMovementMethod(new ScrollingMovementMethod());
        }

        ImageButton back = findViewById(R.id.advertise_back);
        if (back != null) back.setOnClickListener(v -> finish());

        TextView startBtn = findViewById(R.id.advertise_start_btn);
        if (startBtn != null) startBtn.setOnClickListener(v -> startThread());

        TextView sendBtn = findViewById(R.id.advertise_send);
        if (sendBtn != null) sendBtn.setOnClickListener(v -> sendText());

        ImageButton attach = findViewById(R.id.advertise_attach);
        if (attach != null) {
            attach.setOnClickListener(v -> showAttachChooser());
        }
        ImageButton voice = findViewById(R.id.advertise_voice);
        if (voice != null) {
            voice.setOnClickListener(v -> onVoiceClick());
        }

        adapter = new MessagesAdapter();
        if (messagesList != null) {
            messagesList.setLayoutManager(new LinearLayoutManager(this));
            messagesList.setAdapter(adapter);
        }

        prefillIdentity();
        loadOffer();

        threadId = SharedPrefsData.getLong(this, PREF_THREAD_ID, Constants.PREF_NAME);
        if (threadId > 0) {
            showChat();
            loadMessages(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (threadId > 0) {
            loadMessages(true);
        }
    }

    @Override
    protected void onDestroy() {
        stopRecorderQuiet();
        super.onDestroy();
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
                                    JsonObject d = root.getAsJsonObject("data");
                                    applyOffer(d);
                                } catch (Exception ignored) {
                                }
                            }

                            @Override
                            public void onFailure(Call<JsonObject> call, Throwable t) {}
                        });
    }

    private void applyOffer(JsonObject d) {
        if (offerText == null || d == null) return;
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
        String promo = promoInput != null ? promoInput.getText().toString().trim() : "";
        if (name.length() < 2) {
            Toast.makeText(this, R.string.advertise_need_name, Toast.LENGTH_SHORT).show();
            return;
        }
        if (mobile.replaceAll("\\D", "").length() < 10) {
            Toast.makeText(this, R.string.advertise_need_mobile, Toast.LENGTH_SHORT).show();
            return;
        }
        if (promo.length() < 3) {
            Toast.makeText(this, R.string.advertise_need_promo, Toast.LENGTH_SHORT).show();
            return;
        }
        busy = true;
        JsonObject body = new JsonObject();
        body.addProperty("name", name);
        body.addProperty("mobile", mobile);
        body.addProperty("promotion_description", promo);
        try {
            int draft =
                    SharedPrefsData.getInt(this, Constants.DraftID, Constants.PREF_NAME);
            if (draft > 0) body.addProperty("draft_id", draft);
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
                                    if (root == null || !root.has("success")
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
                                            PREF_THREAD_ID,
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
        if (threadId <= 0) return;
        JsonObject body = new JsonObject();
        body.addProperty("thread_id", threadId);
        if (deltaOnly && adapter.lastId() > 0) {
            body.addProperty("after_id", adapter.lastId());
        }
        body.addProperty("limit", 80);
        APIService api = RestApiClient.getClient().create(APIService.class);
        api.advertise_messages("v3", body)
                .enqueue(
                        new Callback<JsonObject>() {
                            @Override
                            public void onResponse(
                                    Call<JsonObject> call, Response<JsonObject> response) {
                                try {
                                    JsonObject root = response.body();
                                    if (root == null || !root.has("data")) return;
                                    JsonArray msgs =
                                            root.getAsJsonObject("data").getAsJsonArray("messages");
                                    List<ChatMsg> list = parseMessages(msgs);
                                    if (deltaOnly && adapter.lastId() > 0) {
                                        adapter.append(list);
                                    } else {
                                        adapter.setAll(list);
                                    }
                                    scrollToEnd();
                                } catch (Exception ignored) {
                                }
                            }

                            @Override
                            public void onFailure(Call<JsonObject> call, Throwable t) {}
                        });
    }

    private void sendText() {
        if (busy || threadId <= 0) return;
        String text = messageInput != null ? messageInput.getText().toString().trim() : "";
        if (TextUtils.isEmpty(text)) return;
        sendMessage("text", text, "");
        if (messageInput != null) messageInput.setText("");
    }

    private void sendMessage(String type, String text, String mediaUrl) {
        if (busy || threadId <= 0) return;
        busy = true;
        JsonObject body = new JsonObject();
        body.addProperty("thread_id", threadId);
        body.addProperty("type", type);
        body.addProperty("text", text != null ? text : "");
        body.addProperty("media_url", mediaUrl != null ? mediaUrl : "");
        try {
            int draft =
                    SharedPrefsData.getInt(this, Constants.DraftID, Constants.PREF_NAME);
            if (draft > 0) body.addProperty("draft_id", draft);
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

    private void showAttachChooser() {
        CharSequence[] items =
                new CharSequence[] {
                    getString(R.string.advertise_pick_image),
                    getString(R.string.advertise_pick_video)
                };
        new AlertDialog.Builder(this)
                .setTitle(R.string.advertise_attach)
                .setItems(
                        items,
                        (d, which) -> {
                            if (which == 0) pickMedia.launch("image/*");
                            else pickMedia.launch("video/*");
                        })
                .show();
    }

    private void onVoiceClick() {
        if (recording) {
            stopAndSendVoice();
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            askMic.launch(Manifest.permission.RECORD_AUDIO);
        } else {
            toggleVoiceRecord();
        }
    }

    private void toggleVoiceRecord() {
        if (recording) {
            stopAndSendVoice();
            return;
        }
        try {
            voiceFile = new File(getCacheDir(), "adv_voice_" + System.currentTimeMillis() + ".m4a");
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setOutputFile(voiceFile.getAbsolutePath());
            recorder.prepare();
            recorder.start();
            recording = true;
            Toast.makeText(this, R.string.advertise_recording, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            stopRecorderQuiet();
            Toast.makeText(this, R.string.advertise_record_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void stopAndSendVoice() {
        try {
            if (recorder != null) {
                recorder.stop();
            }
        } catch (Exception ignored) {
        }
        stopRecorderQuiet();
        recording = false;
        if (voiceFile != null && voiceFile.exists() && voiceFile.length() > 0) {
            uploadFileBytes(voiceFile, "audio/mp4", voiceFile.getName(), "voice");
        }
    }

    private void stopRecorderQuiet() {
        try {
            if (recorder != null) {
                recorder.release();
            }
        } catch (Exception ignored) {
        }
        recorder = null;
        recording = false;
    }

    private void uploadAndSendMedia(Uri uri) {
        if (busy || threadId <= 0) return;
        busy = true;
        Toast.makeText(this, R.string.advertise_uploading, Toast.LENGTH_SHORT).show();
        try {
            String mime = getContentResolver().getType(uri);
            if (mime == null) mime = "application/octet-stream";
            String type = mime.startsWith("video") ? "video" : "image";
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
            uploadBytes(bos.toByteArray(), mime, name, type);
        } catch (Exception e) {
            busy = false;
            Toast.makeText(this, R.string.advertise_upload_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadFileBytes(File file, String mime, String name, String type) {
        busy = true;
        try {
            byte[] bytes = readFile(file);
            uploadBytes(bytes, mime, name, type);
        } catch (Exception e) {
            busy = false;
            Toast.makeText(this, R.string.advertise_upload_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadBytes(byte[] bytes, String mime, String name, String type) {
        RequestBody req = RequestBody.create(MediaType.parse(mime), bytes);
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
                                    JsonObject root = response.body();
                                    String url = null;
                                    if (root != null
                                            && root.has("extras")
                                            && root.getAsJsonObject("extras").has("url")) {
                                        url =
                                                root.getAsJsonObject("extras")
                                                        .get("url")
                                                        .getAsString();
                                    }
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
    }

    private static byte[] readFile(File file) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = fis.read(buf)) > 0) bos.write(buf, 0, n);
        fis.close();
        return bos.toByteArray();
    }

    private void scrollToEnd() {
        if (messagesList == null || adapter.getItemCount() == 0) return;
        messagesList.post(
                () -> messagesList.scrollToPosition(adapter.getItemCount() - 1));
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
        return m;
    }

    static final class ChatMsg {
        long id;
        String sender;
        String type;
        String text;
        String mediaUrl;
    }

    final class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.VH> {
        private final List<ChatMsg> items = new ArrayList<>();

        void setAll(List<ChatMsg> list) {
            items.clear();
            if (list != null) items.addAll(list);
            notifyDataSetChanged();
        }

        void append(List<ChatMsg> list) {
            if (list == null || list.isEmpty()) return;
            int start = items.size();
            items.addAll(list);
            notifyItemRangeInserted(start, list.size());
        }

        void appendOne(ChatMsg m) {
            if (m == null) return;
            items.add(m);
            notifyItemInserted(items.size() - 1);
        }

        long lastId() {
            if (items.isEmpty()) return 0;
            return items.get(items.size() - 1).id;
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView tv = new TextView(parent.getContext());
            int pad = (int) (12 * getResources().getDisplayMetrics().density);
            tv.setPadding(pad, pad, pad, pad);
            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = pad / 2;
            tv.setLayoutParams(lp);
            tv.setTextSize(14f);
            return new VH(tv);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            ChatMsg m = items.get(position);
            boolean admin = "admin".equals(m.sender);
            holder.tv.setBackgroundColor(
                    admin ? Color.parseColor("#FFFF6B00") : Color.parseColor("#FFF0E8"));
            holder.tv.setTextColor(admin ? Color.WHITE : Color.parseColor("#FF333333"));
            holder.tv.setGravity(admin ? Gravity.END : Gravity.START);
            String body;
            if ("text".equals(m.type) || TextUtils.isEmpty(m.mediaUrl)) {
                body = m.text;
            } else {
                body = "[" + m.type + "] " + m.mediaUrl;
                if (!TextUtils.isEmpty(m.text)) body = m.text + "\n" + body;
            }
            holder.tv.setText(body);
            holder.tv.setOnClickListener(
                    v -> {
                        if (!TextUtils.isEmpty(m.mediaUrl)) {
                            try {
                                startActivity(
                                        new Intent(Intent.ACTION_VIEW, Uri.parse(m.mediaUrl)));
                            } catch (Exception ignored) {
                            }
                        }
                    });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class VH extends RecyclerView.ViewHolder {
            final TextView tv;

            VH(TextView itemView) {
                super(itemView);
                tv = itemView;
            }
        }
    }
}
