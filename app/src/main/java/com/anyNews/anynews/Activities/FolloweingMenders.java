package com.anyNews.anynews.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.anyNews.anynews.Adapters.FollowersProfileAdopter;
import com.anyNews.anynews.Adapters.FollowingMemberAdopter;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.LanguageModelResponse;
import com.anyNews.anynews.Pojo.PaymentHistoryRequest;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.FollowListCache;
import com.anyNews.anynews.Utils.MediaUrl;
import com.anyNews.anynews.Utils.SelfFollowGuard;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.models.FollowersProfileModel;
import com.anyNews.anynews.models.FollowingMenderModel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

/**
 * People the user follows (default) OR followers of this reporter when
 * {@link #EXTRA_MODE} is {@link #MODE_FOLLOWERS}.
 * Following mode always offers Discover / Explore suggestions (same
 * {@code follow_more} API as the former standalone Follow menu row).
 */
public class FolloweingMenders extends AppCompatActivity {

    public static final String EXTRA_MODE = "extra_follow_mode";
    public static final String MODE_FOLLOWING = "following";
    public static final String MODE_FOLLOWERS = "followers";

    /** Always show Explore on Following — replaces the separate Follow menu. */
    private static final int SUGGESTIONS_THRESHOLD = Integer.MAX_VALUE;

    RecyclerView followeing_menbers;
    RecyclerView following_suggestions_list;
    ImageView back;
    LinearLayoutManager linearLayoutManager;
    FollowingMemberAdopter districsAdopter;
    FollowersProfileAdopter suggestionsAdapter;
    ArrayList<FollowingMenderModel.Datum> paymentHistoryModel;
    ArrayList<FollowersProfileModel.Datum> suggestionModels;
    LinearLayout layout_with_out_data, layout_with_data, linear_no_recyclerView, no_data_scrren;
    LinearLayout layout_following_suggestions;
    NestedScrollView following_scroll;
    TextView menbers_list, suggestions_header;

    Allsharedpreference prefShare;
    LanguageModelResponse languageModelResponse;
    private String listMode = MODE_FOLLOWING;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followeing_menders);
        layout_with_out_data = findViewById(R.id.follow_layout_with_out_data);
        layout_with_data = findViewById(R.id.follow_layout_with_data);
        linear_no_recyclerView = findViewById(R.id.linear_no_recyclerView);
        followeing_menbers = findViewById(R.id.followeing_menbers);
        following_suggestions_list = findViewById(R.id.following_suggestions_list);
        layout_following_suggestions = findViewById(R.id.layout_following_suggestions);
        following_scroll = findViewById(R.id.following_scroll);
        no_data_scrren = findViewById(R.id.no_data_scrren);
        menbers_list = findViewById(R.id.menbers_list);
        suggestions_header = findViewById(R.id.suggestions_header);
        back = findViewById(R.id.back);
        back.setOnClickListener(view -> finish());

        listMode = getIntent() != null ? getIntent().getStringExtra(EXTRA_MODE) : null;
        if (listMode == null || listMode.trim().isEmpty()) {
            listMode = MODE_FOLLOWING;
        }
        listMode = listMode.trim().toLowerCase();
        if (!MODE_FOLLOWERS.equals(listMode)) {
            listMode = MODE_FOLLOWING;
        }

        setStaticdata();
        // Instant paint from disk/memory — never block first frame on spinner alone.
        paintCachedListInstant();
        getPaymentHistory();
    }

    private boolean isFollowersMode() {
        return MODE_FOLLOWERS.equals(listMode);
    }

    private String cacheType() {
        return isFollowersMode() ? FollowListCache.TYPE_FOLLOWERS : FollowListCache.TYPE_FOLLOWING;
    }

    /** Show last known list immediately (earnings-style instant open). */
    private void paintCachedListInstant() {
        List<FollowingMenderModel.Datum> cached = FollowListCache.load(this, cacheType());
        if (cached != null && !cached.isEmpty()) {
            applyFollowingList(SelfFollowGuard.withoutSelfMembers(this, cached), true);
            // Soft background refresh still runs via getPaymentHistory().
            return;
        }
        showLoading();
    }

    private void setStaticdata() {
        prefShare = new Allsharedpreference(FolloweingMenders.this);
        try {
            Gson gson = new Gson();
            String Translation = prefShare.getShared_String(Allsharedpreference.Translator);
            languageModelResponse = gson.fromJson(Translation, LanguageModelResponse.class);
            if (isFollowersMode()) {
                if (menbers_list != null) {
                    if (languageModelResponse != null && languageModelResponse.getData() != null
                            && languageModelResponse.getData().getFollowers() != null
                            && !languageModelResponse.getData().getFollowers().trim().isEmpty()) {
                        menbers_list.setText(" " + languageModelResponse.getData().getFollowers());
                    } else {
                        menbers_list.setText(" " + getString(R.string.profile_followers_label));
                    }
                }
            } else if (languageModelResponse != null && languageModelResponse.getData() != null
                    && languageModelResponse.getData().getMembers() != null) {
                menbers_list.setText(" " + languageModelResponse.getData().getMembers());
            }
            if (!isFollowersMode()) {
                bindSuggestionsHeader();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void bindSuggestionsHeader() {
        if (suggestions_header == null) return;
        String label = null;
        try {
            if (languageModelResponse != null && languageModelResponse.getData() != null
                    && languageModelResponse.getData().getDiscover() != null) {
                label = languageModelResponse.getData().getDiscover().trim();
            }
        } catch (Exception ignored) {
        }
        if (label == null || label.isEmpty()) {
            label = getString(R.string.people_you_may_follow);
        }
        suggestions_header.setText(label);
    }

    private void showLoading() {
        if (layout_with_out_data != null) layout_with_out_data.setVisibility(View.VISIBLE);
        if (following_scroll != null) following_scroll.setVisibility(View.GONE);
        if (no_data_scrren != null) no_data_scrren.setVisibility(View.GONE);
    }

    private void showContentScroll() {
        if (layout_with_out_data != null) layout_with_out_data.setVisibility(View.GONE);
        if (following_scroll != null) following_scroll.setVisibility(View.VISIBLE);
        if (no_data_scrren != null) no_data_scrren.setVisibility(View.GONE);
    }

    private void showEmptyFallback() {
        if (layout_with_out_data != null) layout_with_out_data.setVisibility(View.GONE);
        if (following_scroll != null) following_scroll.setVisibility(View.GONE);
        if (no_data_scrren != null) no_data_scrren.setVisibility(View.VISIBLE);
    }

    private JsonObject set_payment_history() {
        PaymentHistoryRequest requestModel = new PaymentHistoryRequest();
        requestModel.setDraftId(SharedPrefsData.getInt(
                FolloweingMenders.this, Constants.DraftID, Constants.PREF_NAME));
        String jsonString = new Gson().toJson(requestModel);
        showLog("API REQUEST E" + jsonString);
        String encrpt_data = encrypt(jsonString);
        EncritonRequestData Encriton_Request = new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }

    public void getPaymentHistory() {
        JsonObject otp = set_payment_history();
        showLog("API REQ   " + otp);
        APIService apiService = RestApiClient.getClient().create(APIService.class);

        Call<EncreptionRequest> call = isFollowersMode()
                ? apiService.list_followers("v3", otp)
                : apiService.get_follow_list("v3", otp);

        call.enqueue(new Callback<EncreptionRequest>() {
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                if (response.body() == null || response.body().getData() == null) {
                    if (!hasVisibleList()) {
                        applyFollowingList(new ArrayList<>(), false);
                    }
                    return;
                }
                try {
                    String encrpt_data = decrypt(response.body().getData());
                    showLog("API REQUEST R" + encrpt_data);
                    FollowingMenderModel languageModel =
                            new Gson().fromJson(encrpt_data, FollowingMenderModel.class);
                    ArrayList<FollowingMenderModel.Datum> list = new ArrayList<>();
                    if (languageModel != null && Boolean.TRUE.equals(languageModel.getSuccess())
                            && languageModel.getData() != null) {
                        list.addAll(languageModel.getData());
                    }
                    list = SelfFollowGuard.withoutSelfMembers(FolloweingMenders.this, list);
                    FollowListCache.save(FolloweingMenders.this, cacheType(), list);
                    applyFollowingList(list, false);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (!hasVisibleList()) {
                        applyFollowingList(new ArrayList<>(), false);
                    }
                }
            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                showLog((isFollowersMode() ? "list_followers" : "get_follow_list")
                        + " failed: " + (t != null ? t.getMessage() : "null"));
                if (!hasVisibleList()) {
                    applyFollowingList(new ArrayList<>(), false);
                }
            }
        });
    }

    private boolean hasVisibleList() {
        return paymentHistoryModel != null && !paymentHistoryModel.isEmpty();
    }

    private void applyFollowingList(ArrayList<FollowingMenderModel.Datum> list) {
        applyFollowingList(list, false);
    }

    private void applyFollowingList(ArrayList<FollowingMenderModel.Datum> list, boolean fromCache) {
        paymentHistoryModel = list != null ? list : new ArrayList<>();
        int count = paymentHistoryModel.size();

        if (count > 0) {
            linearLayoutManager = new LinearLayoutManager(FolloweingMenders.this);
            followeing_menbers.setLayoutManager(linearLayoutManager);
            followeing_menbers.setNestedScrollingEnabled(false);
            districsAdopter = new FollowingMemberAdopter(FolloweingMenders.this, paymentHistoryModel);
            followeing_menbers.setAdapter(districsAdopter);
            layout_with_data.setVisibility(View.VISIBLE);
            // Members title for thin/non-empty following list.
            menbers_list.setVisibility(View.VISIBLE);
            showContentScroll();
        } else {
            layout_with_data.setVisibility(View.GONE);
            if (followeing_menbers != null) {
                followeing_menbers.setAdapter(null);
            }
            // Empty following: main title becomes Discover-style header for full suggestions UI.
            if (!isFollowersMode()) {
                menbers_list.setVisibility(View.GONE);
            }
        }

        // Followers list never shows "discover" suggestions.
        if (isFollowersMode()) {
            if (layout_following_suggestions != null) {
                layout_following_suggestions.setVisibility(View.GONE);
            }
            if (count == 0 && !fromCache) {
                showEmptyFallback();
            } else if (count == 0) {
                // Cached empty was painted as loading; if still empty after network handled above.
                showEmptyFallback();
            }
            return;
        }

        if (count <= SUGGESTIONS_THRESHOLD) {
            loadSuggestions(count);
        } else {
            if (layout_following_suggestions != null) {
                layout_following_suggestions.setVisibility(View.GONE);
            }
            if (count == 0) {
                showEmptyFallback();
            }
        }
    }

    private void loadSuggestions(int followingCount) {
        suggestionModels = new ArrayList<>();
        JsonObject otp = set_payment_history();
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        apiService.follow_more("v3", otp).enqueue(new Callback<EncreptionRequest>() {
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                if (response.body() == null || response.body().getData() == null) {
                    onSuggestionsResult(followingCount, false);
                    return;
                }
                try {
                    String encrpt_data = decrypt(response.body().getData());
                    showLog("follow_more R" + encrpt_data);
                    FollowersProfileModel model =
                            new Gson().fromJson(encrpt_data, FollowersProfileModel.class);
                    if (model != null && Boolean.TRUE.equals(model.getSuccess())
                            && model.getData() != null && !model.getData().isEmpty()) {
                        suggestionModels.addAll(SelfFollowGuard.withoutSelfSuggestions(
                                FolloweingMenders.this, model.getData()));
                        for (FollowersProfileModel.Datum item : suggestionModels) {
                            MediaUrl.normalizeFollowProfile(item);
                        }
                        if (suggestionModels.isEmpty()) {
                            onSuggestionsResult(followingCount, false);
                            return;
                        }
                        following_suggestions_list.setLayoutManager(
                                new GridLayoutManager(FolloweingMenders.this, 3));
                        following_suggestions_list.setNestedScrollingEnabled(false);
                        suggestionsAdapter = new FollowersProfileAdopter(
                                FolloweingMenders.this, suggestionModels);
                        following_suggestions_list.setAdapter(suggestionsAdapter);
                        onSuggestionsResult(followingCount, true);
                    } else {
                        onSuggestionsResult(followingCount, false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onSuggestionsResult(followingCount, false);
                }
            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                showLog("follow_more failed: " + t.getMessage());
                onSuggestionsResult(followingCount, false);
            }
        });
    }

    private void onSuggestionsResult(int followingCount, boolean hasSuggestions) {
        if (hasSuggestions && layout_following_suggestions != null) {
            layout_following_suggestions.setVisibility(View.VISIBLE);
            // Full-screen suggestions (0 following): elevate Discover title to page header.
            if (followingCount == 0) {
                menbers_list.setVisibility(View.VISIBLE);
                bindSuggestionsHeader();
                menbers_list.setText(suggestions_header.getText());
                suggestions_header.setVisibility(View.GONE);
            } else {
                suggestions_header.setVisibility(View.VISIBLE);
                bindSuggestionsHeader();
            }
            showContentScroll();
        } else {
            if (layout_following_suggestions != null) {
                layout_following_suggestions.setVisibility(View.GONE);
            }
            if (followingCount == 0) {
                showEmptyFallback();
            } else {
                // Thin list only; hide suggestion block.
                showContentScroll();
            }
        }
    }
}
