package com.anyNews.anynews.Activities;

import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.LanguageModelResponse;
import com.anyNews.anynews.Pojo.LikeDownloadRespouse;
import com.anyNews.anynews.Pojo.LikeSelectionRequest;
import com.anyNews.anynews.Pojo.WriterProfileRequest;
import com.anyNews.anynews.Pojo.WriterProfileResponse;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.AutoNewsBylineHelper;
import com.anyNews.anynews.Utils.AvatarImageHelper;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.ModelClass;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WriterProfileActivity extends AppCompatActivity {

  /** Writer id (legacy extra name). */
  public static final String EXTRA_SESSION_ID = "EXTRA_SESSION_ID";

  /** Feed card snapshot — skip auto get_writer_profile when name + profile are present. */
  public static final String EXTRA_FEED_WRITER_NAME = "EXTRA_FEED_WRITER_NAME";
  public static final String EXTRA_FEED_WRITER_PROFILE = "EXTRA_FEED_WRITER_PROFILE";
  public static final String EXTRA_FEED_WRITER_ROLE = "EXTRA_FEED_WRITER_ROLE";
  public static final String EXTRA_FEED_IS_FOLLOW = "EXTRA_FEED_IS_FOLLOW";

  ArrayList<WriterProfileResponse.Data> followersProfileModels;
  TextView username_text, local_distic_name, like_count, followers_count, following_count,
      articles_count, logintext;
  ImageView profile_image, img_back, neo_settings;
  String writerId;
  TextView like, articlename, followers_text, following_text;
  Allsharedpreference prefShare;
  LanguageModelResponse languageModelResponse;
  LinearLayout layout_with_data, layout_with_out_data;

  LinearLayout login_button_selected, login_button_selected_login;
  TextView login_button_selected_login_logintext;

  LinearLayout login_button;

  int post_id = 0;

  /** Opened from flip feed with writer fields on the post row. */
  private boolean feedWriterPreview = false;
  private boolean writerProfileApiInFlight = false;

  /**
   * Launch full writer screen from a feed {@link ModelClass.Datum} without triggering
   * get_writer_profile on open when name + profile are already on the row.
   */
  public static void launchFromFeed(@Nullable Context context, @Nullable ModelClass.Datum item) {
    if (context == null || item == null || item.getWriterId() == null) {
      return;
    }
    Intent intent = new Intent(context, WriterProfileActivity.class);
    intent.putExtra(EXTRA_SESSION_ID, item.getWriterId().toString());
    if (!TextUtils.isEmpty(item.getWriterName())) {
      intent.putExtra(EXTRA_FEED_WRITER_NAME, item.getWriterName());
    }
    if (!TextUtils.isEmpty(item.getWriterProfile())) {
      intent.putExtra(EXTRA_FEED_WRITER_PROFILE, item.getWriterProfile());
    }
    if (!TextUtils.isEmpty(item.getRole())) {
      intent.putExtra(EXTRA_FEED_WRITER_ROLE, item.getRole());
    }
    if (item.getIsFollow() != null) {
      intent.putExtra(EXTRA_FEED_IS_FOLLOW, item.getIsFollow());
    }
    context.startActivity(intent);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_writer_profile);
    like_count = findViewById(R.id.like_count);
    followers_count = findViewById(R.id.followers_count);
    following_count = findViewById(R.id.following_count);
    articles_count = findViewById(R.id.articles_count);
    username_text = findViewById(R.id.username_text);
    local_distic_name = findViewById(R.id.local_distic_name);
    profile_image = findViewById(R.id.profile_image);
    img_back = (ImageView) findViewById(R.id.img_back);
    neo_settings = (ImageView) findViewById(R.id.neo_settings);
    logintext = findViewById(R.id.logintext);
    login_button = findViewById(R.id.login_button);
    login_button_selected_login_logintext = findViewById(R.id.login_button_selected_login_logintext);
    login_button_selected = findViewById(R.id.login_button_selected);
    login_button_selected_login = findViewById(R.id.login_button_selected_login);
    layout_with_data = findViewById(R.id.layout_with_data);
    layout_with_out_data = findViewById(R.id.layout_with_out_data);
    layout_with_out_data.setVisibility(View.VISIBLE);
    layout_with_data.setVisibility(View.GONE);
    articlename = findViewById(R.id.articlename);
    like = findViewById(R.id.like);
    followers_text = findViewById(R.id.followers_text);
    following_text = findViewById(R.id.following_text);
    writerId = getIntent().getStringExtra(EXTRA_SESSION_ID);

    feedWriterPreview = applyFeedWriterPreviewFromIntent();
    if (!feedWriterPreview) {
      getWriterProfileData(false);
    } else {
      wireFeedPreviewRefresh();
    }

    img_back.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            finish();
          }
        });
    neo_settings.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            Intent intent = new Intent(WriterProfileActivity.this, Settings_Scrren.class);
            startActivity(intent);
          }
        });
    login_button.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            login_button_selected.setVisibility(View.VISIBLE);
            login_button.setVisibility(View.GONE);
            set_follow_un_follow(Long.parseLong(writerId), 0);
          }
        });
    login_button_selected.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            login_button_selected.setVisibility(View.GONE);
            login_button.setVisibility(View.VISIBLE);
            set_follow_un_follow(Long.parseLong(writerId), 1);
          }
        });
    setStaticdata();
  }

  /** Tap avatar to load full writer stats after feed-only preview. */
  private void wireFeedPreviewRefresh() {
    View.OnClickListener refresh =
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            getWriterProfileData(true);
          }
        };
    profile_image.setOnClickListener(refresh);
    username_text.setOnClickListener(refresh);
  }

  private boolean applyFeedWriterPreviewFromIntent() {
    Intent in = getIntent();
    if (in == null) {
      return false;
    }
    String name = in.getStringExtra(EXTRA_FEED_WRITER_NAME);
    String profile = in.getStringExtra(EXTRA_FEED_WRITER_PROFILE);
    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(profile)) {
      return false;
    }

    String displayName = AutoNewsBylineHelper.displayWriterName(name);
    if (TextUtils.isEmpty(displayName)) {
      displayName = "Journalist";
    }
    username_text.setText(displayName);

    String role = in.getStringExtra(EXTRA_FEED_WRITER_ROLE);
    if (!TextUtils.isEmpty(role)) {
      local_distic_name.setText(role);
      local_distic_name.setVisibility(View.VISIBLE);
    } else {
      local_distic_name.setText("");
      local_distic_name.setVisibility(View.GONE);
    }

    AvatarImageHelper.loadGlideCircle(WriterProfileActivity.this, profile_image, profile);

    if (in.hasExtra(EXTRA_FEED_IS_FOLLOW)) {
      int isFollow = in.getIntExtra(EXTRA_FEED_IS_FOLLOW, 0);
      if (isFollow == 0) {
        login_button.setVisibility(View.GONE);
        login_button_selected.setVisibility(View.VISIBLE);
      } else {
        login_button.setVisibility(View.VISIBLE);
        login_button_selected.setVisibility(View.GONE);
      }
    }

    like_count.setText("—");
    followers_count.setText("—");
    following_count.setText("—");
    articles_count.setText("—");

    layout_with_out_data.setVisibility(View.GONE);
    layout_with_data.setVisibility(View.VISIBLE);
    return true;
  }

  private JsonObject set_follow_un_follow_request(Long writer_id, int is_follow) {
    LikeSelectionRequest requestModel = new LikeSelectionRequest();
    requestModel.setDraft_id(
        (long)
            SharedPrefsData.getInt(
                WriterProfileActivity.this, Constants.DraftID, Constants.PREF_NAME));
    requestModel.setWriter_id(writer_id);
    requestModel.setIs_follow(is_follow);
    String jsonString = new com.google.gson.Gson().toJson(requestModel);
    String encrpt_data = null;
    encrpt_data = encrypt(jsonString);
    showLog("API REQUEST E" + jsonString.toString());
    EncritonRequestData Encriton_Request = new EncritonRequestData();
    Encriton_Request.setData(encrpt_data);
    return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
  }

  private void set_follow_un_follow(Long writer_id, int is_follow) {
    JsonObject jsonObject = set_follow_un_follow_request(writer_id, is_follow);
    showLog("API REQ   " + jsonObject.toString());
    APIService apiService = RestApiClient.getClient().create(APIService.class);
    Call<EncreptionRequest> apiResponse = apiService.update_follow("v3", jsonObject);
    apiResponse.enqueue(
        new Callback<EncreptionRequest>() {
          @Override
          public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
            EncreptionRequest encreptionRequest = response.body();
            showLog("API RES   " + response.body().getData());
            String encrpt_data = null;
            encrpt_data = decrypt(encreptionRequest.getData());
            showLog("ENCRYPT" + encrpt_data);
            Gson gsonObj = new Gson();
            LikeDownloadRespouse likeDownloadRespouse =
                gsonObj.fromJson(encrpt_data, LikeDownloadRespouse.class);
            if (likeDownloadRespouse.getSuccess()) {
              post_id = is_follow;
            }
          }

          @Override
          public void onFailure(Call<EncreptionRequest> call, Throwable t) {
            //   Toast.makeText(WriterProfileActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
          }
        });
  }

  private void setStaticdata() {
    prefShare = new Allsharedpreference(WriterProfileActivity.this);
    Gson gson = new Gson();
    String Translation = prefShare.getShared_String(Allsharedpreference.Translator);
    languageModelResponse = gson.fromJson(Translation, LanguageModelResponse.class);
    articlename.setText(" " + languageModelResponse.getData().getArticles());
    like.setText(" " + languageModelResponse.getData().getLikes());
    followers_text.setText(" " + languageModelResponse.getData().getFollowers());
    following_text.setText(" " + languageModelResponse.getData().getFollowing());
  }

  private JsonObject set_writer_profile() {
    WriterProfileRequest requestModel = new WriterProfileRequest();
    requestModel.setDraftId(
        SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
    requestModel.setWriterId(Integer.parseInt(writerId));
    String jsonString = new com.google.gson.Gson().toJson(requestModel);
    String encrpt_data = null;
    encrpt_data = encrypt(jsonString);
    showLog("API REQUEST E" + jsonString.toString());
    EncritonRequestData Encriton_Request = new EncritonRequestData();
    Encriton_Request.setData(encrpt_data);
    return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
  }

  /** @param userInitiated true when user tapped avatar/name after a feed preview. */
  public void getWriterProfileData(boolean userInitiated) {
    if (writerProfileApiInFlight) {
      return;
    }
    writerProfileApiInFlight = true;
    if (!userInitiated && !feedWriterPreview) {
      layout_with_out_data.setVisibility(View.VISIBLE);
      layout_with_data.setVisibility(View.GONE);
    } else if (userInitiated) {
      layout_with_out_data.setVisibility(View.VISIBLE);
    }

    followersProfileModels = new ArrayList<>();
    JsonObject otp = set_writer_profile();
    APIService apiService = RestApiClient.getClient().create(APIService.class);
    Call<EncreptionRequest> apiResponse = apiService.writer_profile("v3", otp);
    apiResponse.enqueue(
        new Callback<EncreptionRequest>() {
          @Override
          public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
            writerProfileApiInFlight = false;
            EncreptionRequest encreptionRequest = response.body();
            if (encreptionRequest == null || encreptionRequest.getData() == null) {
              if (!feedWriterPreview) {
                Toast.makeText(WriterProfileActivity.this, "No data found", Toast.LENGTH_SHORT).show();
                finish();
              }
              layout_with_out_data.setVisibility(View.GONE);
              return;
            }
            showLog("API RES   " + response.body().getData());
            String encrpt_data = decrypt(encreptionRequest.getData());
            showLog("Writer's Data Response:" + encrpt_data);
            Gson gsonObj = new Gson();
            WriterProfileResponse writerResponse =
                gsonObj.fromJson(encrpt_data, WriterProfileResponse.class);

            if (writerResponse.getSuccess()) {
              like_count.setText(writerResponse.getData().getLikes().toString());
              followers_count.setText(writerResponse.getData().getFollowers().toString());
              following_count.setText(writerResponse.getData().getFollowing().toString());
              articles_count.setText(writerResponse.getData().getArticles().toString());
              local_distic_name.setText(writerResponse.getData().getLocation().toString());
              username_text.setText(writerResponse.getData().getName().toString());
              if (writerResponse.getData().getIsFollow().toString().equals("0")) {
                login_button.setVisibility(View.GONE);
                login_button_selected.setVisibility(View.VISIBLE);
              } else {
                login_button.setVisibility(View.VISIBLE);
                login_button_selected.setVisibility(View.GONE);
              }
              AvatarImageHelper.loadGlideCircle(
                  WriterProfileActivity.this,
                  profile_image,
                  writerResponse.getData().getProfilePic());
              layout_with_out_data.setVisibility(View.GONE);
              layout_with_data.setVisibility(View.VISIBLE);
              feedWriterPreview = false;
              profile_image.setOnClickListener(null);
              username_text.setOnClickListener(null);
            } else {
              if (!feedWriterPreview) {
                Toast.makeText(WriterProfileActivity.this, "No data found", Toast.LENGTH_SHORT).show();
                finish();
              } else {
                layout_with_out_data.setVisibility(View.GONE);
              }
            }
          }

          @Override
          public void onFailure(Call<EncreptionRequest> call, Throwable t) {
            writerProfileApiInFlight = false;
            if (!feedWriterPreview) {
              layout_with_out_data.setVisibility(View.GONE);
            }
          }
        });
  }
}