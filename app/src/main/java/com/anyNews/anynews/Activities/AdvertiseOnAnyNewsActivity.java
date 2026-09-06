package com.anyNews.anynews.Activities;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
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
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Profile → Advertise on AnyNews: offer banner, lead form, then text-only chat.
 * No continuous polling — refresh on resume only.
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
    private boolean systemBarInsetsInstalled;
    /** Guards overlapping full/delta fetches that previously duplicated the list. */
    private boolean messagesLoading;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertise_on_anynews);
        applySystemBarInsets();

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
        applySystemBarInsets();
        if (threadId > 0) {
            // Prefer delta when we already have messages; otherwise full replace.
            loadMessages(adapter != null && adapter.lastId() > 0);
        }
    }

    /**
     * targetSdk edge-to-edge draws under the status bar — pad orange header so back/title
     * sit below the clock while orange fills the status-bar region.
     */
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
        sendMessage(text);
        if (messageInput != null) messageInput.setText("");
    }

    private void sendMessage(String text) {
        if (busy || threadId <= 0) return;
        busy = true;
        JsonObject body = new JsonObject();
        body.addProperty("thread_id", threadId);
        body.addProperty("type", "text");
        body.addProperty("text", text != null ? text : "");
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

        private float dp(float v) {
            return v * getResources().getDisplayMetrics().density;
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
            int gap = (int) dp(8);
            rowLp.bottomMargin = gap;
            row.setLayoutParams(rowLp);

            TextView bubble = new TextView(parent.getContext());
            int padH = (int) dp(12);
            int padV = (int) dp(8);
            bubble.setPadding(padH, padV, padH, padV);
            bubble.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.5f);
            bubble.setMaxWidth((int) (parent.getResources().getDisplayMetrics().widthPixels * 0.78f));

            TextView time = new TextView(parent.getContext());
            time.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
            time.setTextColor(Color.parseColor("#FF9A8F86"));
            LinearLayout.LayoutParams timeLp =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
            timeLp.topMargin = (int) dp(2);
            time.setLayoutParams(timeLp);

            row.addView(
                    bubble,
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
            row.addView(time);
            return new VH(row, bubble, time);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            ChatMsg m = items.get(position);
            boolean mine = !"admin".equals(m.sender);
            holder.row.setGravity(mine ? Gravity.END : Gravity.START);
            holder.bubble.setBackground(bubbleBg(mine));
            holder.bubble.setTextColor(mine ? Color.WHITE : Color.parseColor("#FF1C1917"));
            String body = m.text;
            if (TextUtils.isEmpty(body) && !"text".equals(m.type)) {
                body = "[" + m.type + "]";
            }
            holder.bubble.setText(body != null ? body : "");
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
            final TextView bubble;
            final TextView time;

            VH(LinearLayout itemView, TextView bubble, TextView time) {
                super(itemView);
                this.row = itemView;
                this.bubble = bubble;
                this.time = time;
            }
        }
    }
}
