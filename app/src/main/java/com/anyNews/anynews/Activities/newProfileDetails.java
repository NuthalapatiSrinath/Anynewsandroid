package com.anyNews.anynews.Activities;

import static com.anyNews.anynews.App.getContext;
import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

import android.Manifest;
import android.accounts.AccountManager;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.anyNews.anynews.Application.VolleyMultipartRequest;
import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.Pojo.AddProfileResponse;
import com.anyNews.anynews.Pojo.CategoriesRequest;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.LanguageModelResponse;
import com.anyNews.anynews.Pojo.NewUserProfileResponse;
import com.anyNews.anynews.Pojo.ProfileUpdateRequiest;
import com.anyNews.anynews.Pojo.UpdateImageResponse;
import com.anyNews.anynews.Pojo.UserProfileRequest;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.CommonUtilities;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Ui.NeomorphFrameLayout;
import com.anyNews.anynews.models.SearchModel;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.AdPreloader;
import com.anyNews.anynews.Utils.AppHelper;
import com.anyNews.anynews.Utils.AvatarImageHelper;
import com.anyNews.anynews.Utils.ReporterPhotoReminderHelper;
import com.anyNews.anynews.Utils.ReporterPaymentAppConfig;
import com.anyNews.anynews.Utils.ReporterPostGateHelper;
import com.anyNews.anynews.Utils.CommonMethods;
import com.anyNews.anynews.Utils.ForceLogoutHelper;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.CropHelper;
import com.anyNews.anynews.Utils.DeferredMediaPermissions;
import com.anyNews.anynews.Utils.DistrictLabelHelper;
import com.anyNews.anynews.Utils.DistrictPinVisibilityConfig;
import com.anyNews.anynews.Utils.ContactUsChooser;
import com.anyNews.anynews.Utils.LanguageFeedInvalidator;
import com.anyNews.anynews.Utils.Logone;
import com.anyNews.anynews.Utils.MediaUrl;
import com.anyNews.anynews.Utils.FooterStyleHelper;
import com.anyNews.anynews.Utils.FooterTabLabels;
import com.anyNews.anynews.Utils.HomeOfflineModeLabels;
import com.anyNews.anynews.Utils.ProfileHubConfigHelper;
import com.anyNews.anynews.Utils.ProfileHubLabels;
import com.anyNews.anynews.Utils.TranslatorPackHelper;
import com.anyNews.anynews.Utils.ProfilePhotoLabels;
import com.anyNews.anynews.Utils.SessionAnalyticsTracker;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.anyNews.anynews.Utils.ReporterProfileCache;
import com.anyNews.anynews.Utils.CategoriesDiskCache;
import com.anyNews.anynews.Utils.ReporterProfilePrefetcher;
import com.anyNews.anynews.Utils.ProfileAchievementHelper;
import com.anyNews.anynews.Utils.FollowListCache;
import com.anyNews.anynews.Utils.WalletHistoryCache;
import com.anyNews.anynews.Utils.WalletHistoryPrefetcher;
import com.anyNews.anynews.testing.MainActivity;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageView;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.skydoves.progressview.ProgressView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class newProfileDetails extends AppCompatActivity {

    /** Soft-ref so Home can dismiss a warm Profile left under the feed without flashing it. */
    private static WeakReference<newProfileDetails> sAliveProfile;

    /** Finish a stopped Profile instance (warm-reuse under Home) with no animation. */
    public static void finishAliveQuiet() {
        newProfileDetails alive = sAliveProfile != null ? sAliveProfile.get() : null;
        if (alive == null || alive.isFinishing()) {
            return;
        }
        try {
            alive.finish();
            alive.overridePendingTransition(0, 0);
        } catch (Exception ignored) {
        }
    }
    private static final String TAG = "PROFILE_IMAGE";
    TextView local_distic_name;
    ImageView neo_settings;
    ImageView edit_profile_icon;
    View btnNewsNav;
    TextView tvNewsNav;
    String currentUserEmail = "";
    LinearLayout login, share,loadingProfileView,loadingProgressbar;
    String mInvitationUrl;

    TextView followers_count, following_text, follow_more_count, follow_more_text, share_test, share_count, logintext;
    TextView username_text, user_login_number;
    ProgressView progressViewApproval;
    LanguageModelResponse languageModelResponse;
    LinearLayout progressMainLayout;

    Allsharedpreference prefShare;
    ArrayList<NewUserProfileResponse.Data> userProfileResponse;
    LinearLayout layout_follow_more, only_change_language;
    CircleImageView img_upload_photo;
    ImageView back_ground_profile;
    ImageView avatar_add_badge;
    TextView tv_avatar_add_hint;

    AlertDialog alertDialog;
    private int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_IMAGE_REQUEST_CAMARA = 700;
    private static final int STORAGE_PERMISSION_CODE = 123;
    /**
     * Single live reference to the "Add Photo" chooser. Guards against the dialog being
     * shown twice (rapid double-tap / duplicate permission callback), which previously left
     * a second chooser stacked behind the first — so it re-appeared after the first pick.
     */
    private androidx.appcompat.app.AlertDialog imagePickerDialog;
    private Dialog profilePhotoViewerDialog;
    boolean CHECK_VALUE = false;
    CommonMethods mCommonmethods;
    private Uri filePath;
    private Uri cameraOutputUri;
    /** App-owned FileProvider URI — crop after pick; cancel falls back to this. */
    private Uri pendingLocalImageUri;
    private boolean needsCropAfterResume = false;
    private ActivityResultLauncher<CropImageContractOptions> cropImage;
    private ActivityResultLauncher<PickVisualMediaRequest> pickVisualMedia;
    /** Modern Activity Result for gallery chooser (replaces startActivityForResult). */
    private ActivityResultLauncher<Intent> galleryPickLauncher;
    /** Modern Activity Result for camera capture. */
    private ActivityResultLauncher<Intent> cameraCaptureLauncher;
    private Bitmap bitmap;
    private String ImageId, Email = "";
    ProgressBar progressbar;
    LinearLayout imageupload_progressbar;
    boolean User_Image_Available;
    ImageView ic_back_ground_image;

    LinearLayout layout_with_data, layout_with_out_data;

    LinearLayout profile_articales_list, profile_follower_list, profile_following_list, user_articales_list, profile_refer_list, profile_one;

    TextView profile_article_count, profile_like_count, profile_followers_count, profile_following_count,
            writer_following_count, profile_total_amount, earned_total_amount, language_name, only_language_name,textViewLanguageNameUser,textViewFollowMore,textViewShareEarnUser,textViewFollowingUser;

    LinearLayout only_profile_follower_list, change_language, layout_articles_list, layout_likes_list, layout_follower_list, layout_following_list, user_layout_following_list, writer_layout_followmore, layout_follow_more_list, profile_layout_rewards, profile_layout_shareToearn, next_payment, user_profile_total_amount;

    TextView user_follow_more_data, user_share_to_earn, user_rewards, profile_article_name, profile_like_name, profile_followers_name, profile_following_name, writer_following_name, profile_follow_more;

    NeomorphFrameLayout login_neomorph_layout;

    ProgressBar androidProgressBar;
    int progressStatusCounter = 0;
    TextView textView;
    String refer_earn;
    Handler progressHandler = new Handler();
    long RedeemValue, AvailablePoints;
    LinearLayout payment_layout,layoutChangeLang,layoutFollowing,layoutAppShare,layoutFollowMore;
    TextView total_amount, profile_next_payment,check_box,account_delete_name,textViewAppDelete;
    CheckBox checkBox2;

    LinearLayout reportLayout, userLayout,account_delete,layoutAppDelete,layoutAppLogout;
    TextView textViewAppLogout;

    /** True after first onResume; avoids double profile network from onCreate+onResume. */
    private boolean profileResumeArmed = false;
    private int appliedNightUiMask = 0;
    private boolean pendingNightModeRecreate = false;
    /** True when paint path last decided this user is an approved reporter. */
    private boolean showingReporterUi = false;
    /**
     * Last desk UI state applied to the view (-1 = never). Skips no-op
     * GONE/VISIBLE toggles that caused the Reporter card flicker.
     */
    private int appliedReporterDeskState = -1;
    /** Coalesce overlapping {@code reporter_applicant_get} soft-refreshes. */
    private boolean reporterApplicantRefreshInFlight = false;
    /** Soft alpha pulse on Status Videos "New" badge. */
    private ObjectAnimator statusVideosBadgePulse;
    private BottomNavigationView profileBottomNav;

    /** Approved-reporter create FAB (bottom-end speed dial). */
    private View profileReporterFabRoot;
    private View profileReporterFabScrim;
    private View profileReporterFabMenu;
    private com.google.android.material.floatingactionbutton.FloatingActionButton profileReporterFabMain;
    private View profileFabActionArticle;
    private View profileFabActionVideo;
    private View profileFabActionFull;
    private boolean profileReporterFabExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sAliveProfile = new WeakReference<>(this);
        appliedNightUiMask =
                getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        pendingNightModeRecreate = false;
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        // Matching chrome while the large layout inflates — avoids white flash / perceived lag.
        try {
            getWindow().setBackgroundDrawableResource(R.color.profile_hub_scrim);
        } catch (Exception ignored) {
        }
        setContentView(R.layout.activity_new_profile_details);
        // Footer switches must feel like in-shell tabs (no slide-up).
        overridePendingTransition(0, 0);

        // Prefetch next share/house-ad seat while user is on Profile (likely path back to feed).
        try {
            AdPreloader.get().prefetch(getApplicationContext());
        } catch (Exception ignored) {
        }

        // Bind footer chrome immediately so the page "is Profile" on first frame.
        setupProfileFooterNav();

        // CanHub crop — register before resumed (same path as PostArticles/FullPost).
        cropImage = registerForActivityResult(new CropImageContract(),
                new ActivityResultCallback<CropImageView.CropResult>() {
                    @Override
                    public void onActivityResult(CropImageView.CropResult result) {
                        if (result != null && result.isSuccessful() && result.getUriContent() != null) {
                            Log.e(TAG, "crop success uri=" + result.getUriContent());
                            handleProfileCroppedImage(result.getUriContent());
                            return;
                        }
                        if (result != null && result.getError() != null) {
                            Log.e(TAG, "crop error - fallback", result.getError());
                            Toast.makeText(newProfileDetails.this,
                                    "Crop failed - using selected photo",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "crop cancelled - fallback pending=" + pendingLocalImageUri);
                        }
                        if (pendingLocalImageUri != null) {
                            handleProfileCroppedImage(pendingLocalImageUri);
                        }
                    }
                });
        pickVisualMedia = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null) {
                            onExternalProfileImagePicked(uri);
                        }
                    }
                });
        galleryPickLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) {
                        return;
                    }
                    Intent data = result.getData();
                    Uri uri = data.getData();
                    if (uri == null) {
                        return;
                    }
                    try {
                        final int takeFlags = data.getFlags()
                                & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        if (takeFlags != 0) {
                            getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        }
                    } catch (Exception ignored) {
                    }
                    onExternalProfileImagePicked(uri);
                });
        cameraCaptureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != Activity.RESULT_OK) {
                        return;
                    }
                    try {
                        Uri capturedUri = null;
                        if (cameraOutputUri != null) {
                            capturedUri = cameraOutputUri;
                        } else if (result.getData() != null && result.getData().getData() != null) {
                            capturedUri = result.getData().getData();
                        } else if (result.getData() != null && result.getData().getExtras() != null
                                && result.getData().getExtras().get("data") instanceof Bitmap) {
                            capturedUri = saveBitmapToProfileCache(
                                    (Bitmap) result.getData().getExtras().get("data"));
                        }
                        if (capturedUri != null) {
                            onExternalProfileImagePicked(capturedUri);
                        } else {
                            Toast.makeText(this, "Error while capturing image", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error while capturing image", Toast.LENGTH_LONG).show();
                    }
                });

        mCommonmethods = new CommonMethods(newProfileDetails.this);
        layout_with_out_data = findViewById(R.id.layout_with_out_data);
        checkBox2 = findViewById(R.id.checkBox2);
        prefShare = new Allsharedpreference(newProfileDetails.this);
        profile_next_payment = findViewById(R.id.profile_next_payment);
        total_amount = findViewById(R.id.total_amount);
        profile_article_name = findViewById(R.id.profile_article_name);
        profile_like_name = findViewById(R.id.profile_like_name);
        user_share_to_earn = findViewById(R.id.user_share_to_earn);
        user_follow_more_data = findViewById(R.id.user_follow_more_data);
        writer_following_name = findViewById(R.id.writer_following_name);
        profile_follow_more = findViewById(R.id.profile_follow_more);
        profile_followers_name = findViewById(R.id.profile_followers_name);
        user_rewards = findViewById(R.id.user_rewards);
        profile_following_name = findViewById(R.id.profile_following_name);
        layout_with_data = findViewById(R.id.layout_with_data);
        imageupload_progressbar = findViewById(R.id.imageupload_progressbar);
        ic_back_ground_image = findViewById(R.id.ic_back_ground_image);
        back_ground_profile = findViewById(R.id.back_ground_profile);
        neo_settings = (ImageView) findViewById(R.id.neo_settings);
        edit_profile_icon = (ImageView) findViewById(R.id.edit_profile_icon);
        btnNewsNav = findViewById(R.id.btn_news_nav);
        tvNewsNav = findViewById(R.id.tv_news_nav);
        bindNewsNavLabel();
        local_distic_name = (TextView) findViewById(R.id.local_distic_name);
        login = (LinearLayout) findViewById(R.id.login);
        loadingProfileView = (LinearLayout) findViewById(R.id.loadingProfileView);
        loadingProfileView.setVisibility(View.GONE);
        loadingProgressbar = (LinearLayout) findViewById(R.id.loadingProgressbar);
        loadingProgressbar.setVisibility(View.GONE);
        img_upload_photo = findViewById(R.id.img_upload_photo);
        avatar_add_badge = findViewById(R.id.avatar_add_badge);
        tv_avatar_add_hint = findViewById(R.id.tv_avatar_add_hint);
        username_text = findViewById(R.id.username_text);
        logintext = findViewById(R.id.logintext);
        share_test = findViewById(R.id.share_test);
        share_count = findViewById(R.id.share_count);
        payment_layout = findViewById(R.id.payment_layout);
        payment_layout.setVisibility(View.GONE);

        androidProgressBar = findViewById(R.id.horizontal_progress_bar);
        only_change_language = findViewById(R.id.only_change_language);
        only_profile_follower_list = findViewById(R.id.only_profile_follower_list);
        profile_total_amount = findViewById(R.id.profile_total_amount);
        earned_total_amount = findViewById(R.id.earned_total_amount);
        profile_one = findViewById(R.id.profile_one);

        profile_following_list = findViewById(R.id.profile_following_list);
        profile_follower_list = findViewById(R.id.profile_follower_list);
        profile_articales_list = findViewById(R.id.profile_articales_list);
        user_articales_list = findViewById(R.id.user_articales_list);
        profile_article_count = findViewById(R.id.profile_article_count);
        profile_like_count = findViewById(R.id.profile_like_count);
        profile_followers_count = findViewById(R.id.profile_followers_count);
        profile_following_count = findViewById(R.id.profile_following_count);
        writer_following_count = findViewById(R.id.writer_following_count);
        profile_refer_list = findViewById(R.id.profile_refer_list);
        next_payment = findViewById(R.id.next_payment);
        user_profile_total_amount = findViewById(R.id.user_profile_total_amount);
        user_login_number = findViewById(R.id.user_login_number);
        progressViewApproval = findViewById(R.id.progressView1);
        progressMainLayout = findViewById(R.id.progressMainLayout);
        progressMainLayout.setVisibility(View.GONE);
        login_neomorph_layout = findViewById(R.id.login_neomorph_layout);
        language_name = findViewById(R.id.language_name);

        layout_articles_list = findViewById(R.id.layout_articles_list);
        layout_likes_list = findViewById(R.id.layout_likes_list);
        layout_follower_list = findViewById(R.id.layout_follower_list);
        layout_following_list = findViewById(R.id.layout_following_list);
        layout_follow_more_list = findViewById(R.id.layout_follow_more_list);
        change_language = findViewById(R.id.change_language);
        user_layout_following_list = findViewById(R.id.user_layout_following_list);
        writer_layout_followmore = findViewById(R.id.writer_layout_followmore);
        profile_layout_rewards = findViewById(R.id.profile_layout_rewards);
        profile_layout_shareToearn = findViewById(R.id.profile_layout_shareToearn);
        only_language_name = findViewById(R.id.only_language_name);
        check_box = findViewById(R.id.check_box);
        userLayout = findViewById(R.id.userLayout);
        reportLayout = findViewById(R.id.reportLayout);
        layoutChangeLang = findViewById(R.id.layoutChangeLang);
        layoutFollowing = findViewById(R.id.layoutFollowing);
        layoutAppShare = findViewById(R.id.layoutAppShare);
        layoutFollowMore = findViewById(R.id.layoutFollowMore);
        textViewLanguageNameUser = findViewById(R.id.textViewLanguageNameUser);
        textViewFollowMore = findViewById(R.id.textViewFollowMore);
        textViewShareEarnUser = findViewById(R.id.textViewShareEarnUser);
        textViewFollowingUser = findViewById(R.id.textViewFollowingUser);
        account_delete = findViewById(R.id.account_delete);
        layoutAppDelete = findViewById(R.id.layoutAppDelete);
        layoutAppLogout = findViewById(R.id.layoutAppLogout);
        account_delete_name = findViewById(R.id.account_delete_name);

        textViewAppDelete = findViewById(R.id.textViewAppDelete);
        textViewAppLogout = findViewById(R.id.textViewAppLogout);
        profile_layout_rewards.setVisibility(
                DistrictPinVisibilityConfig.rewardsMasterEnabled(this)
                        ? View.VISIBLE
                        : View.GONE);
        // Parent of legacy rewards tile was GONE in XML — surface when master ON.
        if (profile_refer_list != null) {
            profile_refer_list.setVisibility(
                    DistrictPinVisibilityConfig.rewardsMasterEnabled(this)
                            ? View.VISIBLE
                            : View.GONE);
        }
        bindProfileRewardsRow(15f);
        profile_layout_shareToearn.setVisibility(View.VISIBLE);
        // Keep avatar tappable (guest → login; signed-in → pick/crop).
        img_upload_photo.setVisibility(View.VISIBLE);
        imageupload_progressbar.setVisibility(View.GONE);

        // Instant first paint: name / email / avatar / verified / achieved from prefs+cache
        // (do not wait for network / translator Gson / hub extras).
        paintIdentityFromLocalPrefs();
        boolean cachedReporter = SharedPrefsData.getBool(
                newProfileDetails.this, Constants.is_reporter, Constants.PREF_NAME);
        applyReporterChrome(cachedReporter);
        paintAchievedFromLocalPrefs();
        // Full last-known profile (stats + achieved) before deferred network soft-refresh.
        try {
            showCachedProfileInstant();
        } catch (Exception ignored) {
        }
        applyGuestProfileCardDefaults();
        bindProfileLoginPill();
        applyLoginGatedHubRows();

        check_box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(newProfileDetails.this, PrivacyPolicy.class);
                newProfileDetails.this.startActivity(intent);

            }
        });

        only_change_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(LanguageActivity.createChangeLanguageIntent(newProfileDetails.this));
            }
        });
        layoutChangeLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(LanguageActivity.createChangeLanguageIntent(newProfileDetails.this));
            }
        });
        if (layoutAppShare != null) {
            layoutAppShare.setVisibility(View.VISIBLE);
            layoutAppShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT,
                            "Hey check out my app at: https://play.google.com/store/apps/details?id="
                                    + BuildConfig.APPLICATION_ID);
                    sendIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sendIntent, ProfileHubLabels.shareApp(
                            newProfileDetails.this)));
                }
            });
        }
        // Follow / Discover was a duplicate of Following → Explore; hide the extra row.
        if (layoutFollowMore != null) {
            layoutFollowMore.setVisibility(View.GONE);
            layoutFollowMore.setOnClickListener(null);
        }
        if (layout_follow_more_list != null) {
            layout_follow_more_list.setVisibility(View.GONE);
            layout_follow_more_list.setOnClickListener(null);
        }
        if (writer_layout_followmore != null) {
            writer_layout_followmore.setVisibility(View.GONE);
            writer_layout_followmore.setOnClickListener(null);
        }
        layoutFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("layoutFollowing");
                Intent intent = new Intent(newProfileDetails.this, FolloweingMenders.class);
                startActivity(intent);
            }
        });
        user_profile_total_amount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Total Cash → article amount history (instant open; cache paint + soft refresh)
                openWalletHistory(1);
            }
        });
        profile_layout_shareToearn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT,
                        "Hey check out my app at: https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                //Toast.makeText(newProfileDetails.this,"Coming soon..",Toast.LENGTH_SHORT).show();
//
//                boolean is_login = SharedPrefsData.getBool(newProfileDetails.this, Constants.is_login, Constants.PREF_NAME);
//
//                if (is_login) {
//
//                    Intent intent = new Intent();
//                    intent.setAction(Intent.ACTION_SEND);
//                    intent.putExtra(Intent.EXTRA_TEXT, "" + refer_earn);
//                    intent.setType("text/plain");
//                    if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
//                        startActivity(intent);
//                    }
//
//                } else {
//
//                    Intent intent = new Intent(newProfileDetails.this, LoginActivityDetails.class);
//                    startActivity(intent);
//                }

            }
        });
        change_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(LanguageActivity.createChangeLanguageIntent(newProfileDetails.this));
            }
        });
        next_payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Enough for payout → redeem form. Otherwise open Payment History instantly
                // (was only a toast; now matches "Earnings → history" expectation with soft UI).
                if (AvailablePoints >= RedeemValue && RedeemValue > 0) {
                    Intent intent = new Intent(newProfileDetails.this, RedeemPageActivity.class);
                    startActivity(intent);
                } else {
                    openWalletHistory(2);
                }
            }
        });
        layout_articles_list = findViewById(R.id.layout_articles_list);
        if (layout_articles_list != null) {
            layout_articles_list.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openArticlesStatus(null);
                }
            });
        }
        layout_likes_list = findViewById(R.id.layout_likes_list);
        if (layout_likes_list != null) {
            layout_likes_list.setOnClickListener(v -> openArticlesStatus("likes"));
        }
        if (layout_follower_list != null) {
            layout_follower_list.setOnClickListener(v -> openFollowersList());
        }
        layout_following_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent intent = new Intent(newProfileDetails.this, FolloweingMenders.class);
                startActivity(intent);
            }
        });
        user_layout_following_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(newProfileDetails.this, FolloweingMenders.class);
                startActivity(intent);
            }
        });
        profile_layout_rewards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openRewardsFromProfile();
            }
        });


        try {

            String distLabel = DistrictLabelHelper.resolveDisplayLabel(newProfileDetails.this);
            local_distic_name.setText("" + distLabel);

            if (DistrictLabelHelper.isUsable(distLabel)) {
                DistrictLabelHelper.saveName(newProfileDetails.this, distLabel);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        if (btnNewsNav != null) {
            btnNewsNav.setOnClickListener(v -> returnToNewsFeed());
        }
        if (tvNewsNav != null) {
            tvNewsNav.setOnClickListener(v -> returnToNewsFeed());
        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openReporterLogin();
            }
        });
        if (login_neomorph_layout != null) {
            login_neomorph_layout.setOnClickListener(v -> openReporterLogin());
        }
        bindStatusVideosNewBadge();
        bindBecomeReporterNewBadge();
        bindBirthdaySpecialNewBadge();
        bindLocationRowText();

        if (neo_settings != null) {
            neo_settings.setOnClickListener(v ->
                    startActivity(new Intent(newProfileDetails.this, Settings_Scrren.class)));
        }

        View layoutBookmarks = findViewById(R.id.layout_bookmarks);
        if (layoutBookmarks != null) {
            layoutBookmarks.setOnClickListener(v -> {
                startActivity(new Intent(newProfileDetails.this, BookmarksActivity.class));
            });
        }

        View completeProfile = findViewById(R.id.layout_complete_profile);
        if (completeProfile != null) {
            completeProfile.setOnClickListener(v -> showEditNameDialog());
        }

        if (edit_profile_icon != null) {
            edit_profile_icon.setOnClickListener(v -> showEditNameDialog());
        }
        if (layoutAppDelete != null) {
            layoutAppDelete.setVisibility(View.GONE);
            layoutAppDelete.setOnClickListener(null);
        }
        if (layoutAppLogout != null) {
            layoutAppLogout.setOnClickListener(v -> showLogoutConfirmDialog());
        }
        if (account_delete != null) {
            account_delete.setVisibility(View.GONE);
            account_delete.setOnClickListener(null);
        }


        if (username_text != null) {
            username_text.setOnClickListener(v -> showEditNameDialog());
        }

        img_upload_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean loggedIn = SharedPrefsData.getBool(
                        newProfileDetails.this, Constants.is_login, Constants.PREF_NAME);
                if (!loggedIn) {
                    Intent intent = new Intent(newProfileDetails.this, LoginActivityDetails.class);
                    intent.putExtra(LoginActivityDetails.EXTRA_RETURN_TO_PROFILE, true);
                    startActivity(intent);
                    return;
                }
                // Privacy checkbox is collapsed in hub UI; default checked so upload still works.
                if (checkBox2 != null && !checkBox2.isChecked()) {
                    checkBox2.setChecked(true);
                }
                if (hasProfilePhoto()) {
                    showProfilePhotoFullscreen();
                } else {
                    beginChangeProfilePhoto();
                }
            }
        });

        // Heavy translator / hub / network after first frame so open feels instant.
        final View decor = getWindow() != null ? getWindow().getDecorView() : null;
        Runnable deferred = this::finishProfileFirstFrameWork;
        if (decor != null) {
            decor.post(deferred);
        } else {
            deferred.run();
        }

        // firebase events
        /*draft_id_firebase_profile = SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
        long current_time_firebase_profile = System.currentTimeMillis();
        String device_id_firebase = SharedPrefsData.getString(getApplicationContext(), Constants.DeviceID, Constants.PREF_NAME);

        writeProfilePageSelection("User",type_firebase_profile ,draft_id_firebase_profile ,device_id_firebase, current_time_firebase_profile);*/
    }

    /** Translator JSON, hub strip, network profile — safe after first paint. */
    private void finishProfileFirstFrameWork() {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        try {
            setStaticdata();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            getWriterProfileData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            setupProfileHubExtras();
            setupReporterDesk();
            setupReporterCreateFab();
            applyLoginGatedHubRows();
            setupReporterShortcutClicks();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    showLog("someActivityResultLauncher in home");
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        if(data!=null){
                            String emailID=data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                            showLog("EmailID"+emailID);
                            prefShare.setEmailID(emailID);
                            getWriterProfileData();

                        }else{
                            Toast.makeText(newProfileDetails.this,"Unable to pickup the account please try again later..",2000).show();
                        }
                    }
                }
            });


    private void showAccountDeletePopup() {
        {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(newProfileDetails.this);
            builder.setTitle("Delete your Account");
            builder.setMessage("This will erase all your existing data with us. Are you sure?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    callApiForDeleteAccount();


                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.show();
        }


    }

    private void showLogoutConfirmDialog() {
        String title = getString(R.string.profile_logout);
        String message = getString(R.string.profile_logout_confirm);
        String yes = getString(R.string.profile_logout_yes);
        String no = getString(R.string.profile_logout_no);
        new androidx.appcompat.app.AlertDialog.Builder(newProfileDetails.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(yes, (d, which) -> performLogout())
                .setNegativeButton(no, (d, which) -> d.dismiss())
                .show();
    }

    /**
     * Clears local login session and returns to splash as a logged-out guest.
     * No dedicated logout API exists; local state always clears.
     * Keeps device language, geography, settings, and FCM device token.
     */
    private void performLogout() {
        ForceLogoutHelper.clearAuthSession(this, false, false);

        // In-memory UI so FAB / header never flash logged-in after clear.
        showingReporterUi = false;
        appliedReporterDeskState = -1;
        reporterApplicantRefreshInFlight = false;
        currentUserEmail = "";
        ImageId = null;
        profileReporterFabExpanded = false;
        try {
            applyReporterChrome(false);
            paintLoggedOutIdentity();
            bindReporterCreateFab();
            applyLoginGatedHubRows();
        } catch (Exception ignored) {
        }

        Intent intent = new Intent(newProfileDetails.this, StartingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private JsonObject getUserDeleteData() {
        UserProfileRequest requestModel = new UserProfileRequest();
        requestModel.setDraftId(SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        showLog("API REQUEST E" + jsonString.toString());
        encrpt_data = encrypt(jsonString);

        EncritonRequestData Encriton_Request = new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }

    private void callApiForDeleteAccount() {
        {
            JsonObject otp = getUserDeleteData();
            APIService apiService = RestApiClient.getClient().create(APIService.class);
            Call<JsonObject> apiResponse = apiService.deleteUserAccount("v3", otp);
            apiResponse.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response){
                    if(response.body()!=null){
                        JsonObject jsonObject=response.body();
                        try {
                            JSONObject object=new JSONObject(jsonObject.toString());
                            showLog("Response::"+jsonObject);
                            boolean isSuccess=object.getBoolean("success");
                            showLog("Response:isSuccess:"+isSuccess);
                            if(isSuccess){
                                SharedPreferences settings = getSharedPreferences(Allsharedpreference.PREFS_NAME, Context.MODE_PRIVATE);
                                settings.edit().clear().commit();
                                SharedPrefsData.getInstance(newProfileDetails.this).ClearData(newProfileDetails.this);
                                SharedPreferences settings2 = getSharedPreferences("myapp", Context.MODE_PRIVATE);
                                settings2.edit().clear().commit();
                                SharedPreferences settings3 = getSharedPreferences("my_pref", Context.MODE_PRIVATE);
                                settings3.edit().clear().commit();
                                prefShare.setEmailID("");
                                SharedPrefsData.putBool(getApplicationContext(), Constants.is_login, false, Constants.PREF_NAME);
                                Intent intent = new Intent(newProfileDetails.this, StartingActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                //finish();
                                startActivity(intent);
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }





                      //  { success: true}
                    }



                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                }
            });
        }
    }

    /**
     * Open camera/gallery chooser without asking storage+camera together.
     * Gallery uses SAF / Photo Picker (no READ_MEDIA). Camera permission only when
     * the user picks "Camera" — one system dialog max per action.
     */
    private void requestStoragePermission() {
        showFileChooser();
    }


    private void showFileChooser() {
        // Never stack a second chooser. A double-tap on the profile photo (or a duplicate
        // permission callback) used to open two dialogs; picking from the top one left the
        // second showing, so it looked like the app "asked again" after the first upload.
        if (imagePickerDialog != null && imagePickerDialog.isShowing()) {
            return;
        }

        Gson gson = new Gson();
        String Translation = prefShare.getShared_String(Allsharedpreference.Translator);
        languageModelResponse = gson.fromJson(Translation, LanguageModelResponse.class);

        String takePhoto = "Camera";
        String chooseGallery = "Gallery";
        String cancel = "Cancel";
        try {
            if (languageModelResponse != null && languageModelResponse.getData() != null) {
                if (languageModelResponse.getData().getTakePhoto() != null) {
                    takePhoto = "" + languageModelResponse.getData().getTakePhoto();
                }
                if (languageModelResponse.getData().getChooseFromGallery() != null) {
                    chooseGallery = "" + languageModelResponse.getData().getChooseFromGallery();
                }
                if (languageModelResponse.getData().getCancel() != null) {
                    cancel = "" + languageModelResponse.getData().getCancel();
                }
            }
        } catch (Exception ignored) {
        }

        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(newProfileDetails.this);
        try {
            if (languageModelResponse != null && languageModelResponse.getData() != null
                    && languageModelResponse.getData().getAddPhoto() != null) {
                builder.setTitle("" + languageModelResponse.getData().getAddPhoto());
            } else {
                builder.setTitle("Add photo");
            }
        } catch (Exception e) {
            builder.setTitle("Add photo");
        }

        final CharSequence[] options = {takePhoto, chooseGallery, cancel};
        final String takePhotoFinal = takePhoto;
        final String chooseGalleryFinal = chooseGallery;
        final String cancelFinal = cancel;

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals(takePhotoFinal)) {
                    openCameraForProfilePhoto();
                } else if (options[item].equals(chooseGalleryFinal)) {
                    openGalleryForProfilePhoto();
                } else if (options[item].equals(cancelFinal)) {
                    dialog.dismiss();
                }
            }
        });
        imagePickerDialog = builder.create();
        imagePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                imagePickerDialog = null;
            }
        });
        imagePickerDialog.show();
    }

    private void openCameraForProfilePhoto() {
        Log.e(TAG, "openCameraForProfilePhoto");
        DeferredMediaPermissions.ensureCamera(this, new DeferredMediaPermissions.GrantCallback() {
            @Override
            public void onGranted() {
                launchCameraCapture();
            }
        });
    }

    private void launchCameraCapture() {
        File photoFile;
        try {
            photoFile = createProfileImageFile();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to create photo file", Toast.LENGTH_SHORT).show();
            return;
        }
        cameraOutputUri = FileProvider.getUriForFile(
                this, getPackageName() + ".provider", photoFile);
        Intent intent = DeferredMediaPermissions.buildCameraIntent(this, cameraOutputUri);
        if (intent == null) {
            cameraOutputUri = null;
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            if (cameraCaptureLauncher != null) {
                cameraCaptureLauncher.launch(intent);
            } else {
                startActivityForResult(intent, PICK_IMAGE_REQUEST_CAMARA);
            }
        } catch (ActivityNotFoundException e) {
            cameraOutputUri = null;
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGalleryForProfilePhoto() {
        Log.e(TAG, "openGalleryForProfilePhoto");
        // SAF / Photo Picker — no storage permission; use Activity Result launcher.
        DeferredMediaPermissions.openImageGallery(this, galleryPickLauncher, pickVisualMedia);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Legacy fallback only if launchers were unavailable on this device.
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                final int takeFlags = data.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                if (takeFlags != 0) {
                    getContentResolver().takePersistableUriPermission(data.getData(), takeFlags);
                }
            } catch (Exception ignored) {
            }
            onExternalProfileImagePicked(data.getData());
        } else if (requestCode == PICK_IMAGE_REQUEST_CAMARA && resultCode == RESULT_OK) {
            try {
                Uri capturedUri = null;
                if (cameraOutputUri != null) {
                    capturedUri = cameraOutputUri;
                } else if (data != null && data.getData() != null) {
                    capturedUri = data.getData();
                } else if (data != null && data.getExtras() != null
                        && data.getExtras().get("data") instanceof Bitmap) {
                    capturedUri = saveBitmapToProfileCache((Bitmap) data.getExtras().get("data"));
                }
                if (capturedUri != null) {
                    onExternalProfileImagePicked(capturedUri);
                } else {
                    Toast.makeText(this, "Error while capturing image", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error while capturing image", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Empty avatar: camera illustration + badge + localized "tap to add" hint.
     * With photo: hide badge/hint so the picture is primary.
     */
    private void bindEmptyProfilePhotoAffordance(boolean empty) {
        try {
            String hint = empty
                    ? ProfilePhotoLabels.tapToAdd(this)
                    : ProfilePhotoLabels.changePhoto(this);
            if (img_upload_photo != null) {
                img_upload_photo.setContentDescription(hint);
            }
            if (avatar_add_badge != null) {
                avatar_add_badge.setVisibility(empty ? View.VISIBLE : View.GONE);
                avatar_add_badge.setContentDescription(hint);
            }
            if (tv_avatar_add_hint != null) {
                tv_avatar_add_hint.setText(ProfilePhotoLabels.tapToAdd(this));
                tv_avatar_add_hint.setVisibility(empty ? View.VISIBLE : View.GONE);
            }
        } catch (Exception ignored) {
        }
    }

    /** Prefs / in-memory URL for the signed-in reporter avatar. */
    private String resolveCurrentProfilePhotoUrl() {
        try {
            if (ImageId != null && !ImageId.trim().isEmpty()
                    && !AvatarImageHelper.isMissing(ImageId)) {
                return ImageId.trim();
            }
        } catch (Exception ignored) {
        }
        try {
            String pic = SharedPrefsData.getString(this, Constants.PROFILE_PIC_URL, Constants.PREF_NAME);
            if (pic != null) {
                pic = pic.trim();
                if (!AvatarImageHelper.isMissing(pic)) {
                    return pic;
                }
            }
        } catch (Exception ignored) {
        }
        try {
            NewUserProfileResponse cached = ReporterProfileCache.load(getApplicationContext());
            if (cached != null && cached.getData() != null
                    && cached.getData().getProfilePic() != null) {
                String pic = cached.getData().getProfilePic().trim();
                if (!AvatarImageHelper.isMissing(pic)) {
                    return pic;
                }
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    private boolean hasProfilePhoto() {
        return !AvatarImageHelper.isUnusablePhoto(resolveCurrentProfilePhotoUrl());
    }

    /** Camera/gallery chooser used by empty avatar, fullscreen Change photo, and edit profile. */
    private void beginChangeProfilePhoto() {
        dismissProfilePhotoViewer();
        if (checkBox2 != null && !checkBox2.isChecked()) {
            checkBox2.setChecked(true);
        }
        requestStoragePermission();
    }

    /** WhatsApp-style dark full-bleed viewer with bottom-end Change photo. */
    private void showProfilePhotoFullscreen() {
        final String url = resolveCurrentProfilePhotoUrl();
        if (AvatarImageHelper.isMissing(url)) {
            beginChangeProfilePhoto();
            return;
        }
        if (profilePhotoViewerDialog != null && profilePhotoViewerDialog.isShowing()) {
            return;
        }

        View content = LayoutInflater.from(this)
                .inflate(R.layout.dialog_profile_photo_viewer, null, false);
        ImageView fullImage = content.findViewById(R.id.profile_photo_viewer_image);
        TextView changeBtn = content.findViewById(R.id.profile_photo_viewer_change);
        ImageButton closeBtn = content.findViewById(R.id.profile_photo_viewer_close);

        if (changeBtn != null) {
            changeBtn.setText(ProfilePhotoLabels.changePhoto(this));
        }

        try {
            Picasso.get()
                    .load(MediaUrl.resolve(url))
                    .placeholder(R.drawable.ic_profile_add_photo)
                    .error(R.drawable.ic_profile_add_photo)
                    .into(fullImage);
        } catch (Exception e) {
            if (fullImage != null) {
                fullImage.setImageResource(R.drawable.ic_profile_add_photo);
            }
        }

        profilePhotoViewerDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        profilePhotoViewerDialog.setContentView(content);
        profilePhotoViewerDialog.setCancelable(true);
        Window window = profilePhotoViewerDialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            window.setBackgroundDrawableResource(android.R.color.black);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            try {
                window.setStatusBarColor(0xFF000000);
                window.setNavigationBarColor(0xFF000000);
            } catch (Exception ignored) {
            }
        }
        profilePhotoViewerDialog.setOnDismissListener(dialog -> profilePhotoViewerDialog = null);

        if (closeBtn != null) {
            closeBtn.setOnClickListener(v -> dismissProfilePhotoViewer());
        }
        if (changeBtn != null) {
            changeBtn.setOnClickListener(v -> beginChangeProfilePhoto());
        }

        profilePhotoViewerDialog.show();
    }

    private void dismissProfilePhotoViewer() {
        try {
            if (profilePhotoViewerDialog != null && profilePhotoViewerDialog.isShowing()) {
                profilePhotoViewerDialog.dismiss();
            }
        } catch (Exception ignored) {
        }
        profilePhotoViewerDialog = null;
    }

    /**
     * Paint name / email / avatar from local prefs immediately so Profile never waits
     * on the network (email no longer appears “late”).
     * When logged out, always force Guest + Login CTA (never resurrect cache/prefs identity).
     */
    private void paintIdentityFromLocalPrefs() {
        try {
            boolean loggedIn = SharedPrefsData.getBool(this, Constants.is_login, Constants.PREF_NAME);
            if (!loggedIn) {
                paintLoggedOutIdentity();
                return;
            }

            String email = "";
            if (prefShare != null && prefShare.getEmailId() != null) {
                email = prefShare.getEmailId().trim();
            }
            if (email.isEmpty()) {
                try {
                    String prefEmail = SharedPrefsData.getString(
                            this, Constants.Email, Constants.PREF_NAME);
                    if (prefEmail != null) email = prefEmail.trim();
                } catch (Exception ignored) {
                }
            }
            String name = "";
            try {
                name = SharedPrefsData.getString(this, Constants.USER_NAME, Constants.PREF_NAME);
                if (name == null) name = "";
                name = name.trim();
            } catch (Exception ignored) {
            }
            String pic = "";
            try {
                pic = SharedPrefsData.getString(this, Constants.PROFILE_PIC_URL, Constants.PREF_NAME);
                if (pic == null) pic = "";
                pic = pic.trim();
            } catch (Exception ignored) {
            }

            // Prefer disk profile cache for richer fields when available (still instant).
            try {
                NewUserProfileResponse cached = ReporterProfileCache.load(getApplicationContext());
                if (cached != null && cached.getData() != null) {
                    NewUserProfileResponse.Data d = cached.getData();
                    if ((name == null || name.isEmpty()) && d.getUserName() != null) {
                        name = d.getUserName().trim();
                    }
                    if ((email == null || email.isEmpty()) && d.getEmail() != null) {
                        email = d.getEmail().trim();
                    }
                    if ((pic == null || pic.isEmpty()) && d.getProfilePic() != null) {
                        pic = d.getProfilePic().trim();
                    }
                }
            } catch (Exception ignored) {
            }

            boolean hasAccount = email != null && !email.isEmpty() && email.contains("@");
            if (hasAccount) {
                try {
                    if (prefShare != null) {
                        prefShare.setEmailID(email);
                    }
                    SharedPrefsData.putString(this, Constants.Email, email, Constants.PREF_NAME);
                } catch (Exception ignored) {
                }
            }
            if (username_text != null) {
                if (name != null && !name.isEmpty() && !"null".equalsIgnoreCase(name)) {
                    username_text.setText(name);
                    try {
                        SharedPrefsData.putString(this, Constants.USER_NAME, name, Constants.PREF_NAME);
                    } catch (Exception ignored) {
                    }
                } else if (!hasAccount) {
                    username_text.setText(ProfileHubLabels.guest(this));
                }
            }
            if (user_login_number != null) {
                if (hasAccount) {
                    user_login_number.setText(email);
                    user_login_number.setTextColor(
                            ContextCompat.getColor(this, R.color.profile_way2_guest_sub));
                    user_login_number.setVisibility(View.VISIBLE);
                    currentUserEmail = email;
                }
            }
            if (img_upload_photo != null) {
                if (!AvatarImageHelper.isMissing(pic)) {
                    AvatarImageHelper.loadProfilePicasso(img_upload_photo, pic);
                    bindEmptyProfilePhotoAffordance(false);
                    ImageId = pic;
                } else {
                    AvatarImageHelper.setProfileEmpty(img_upload_photo);
                    bindEmptyProfilePhotoAffordance(true);
                }
            }
            if (hasAccount && login_neomorph_layout != null) {
                login_neomorph_layout.setVisibility(View.GONE);
            }
            bindProfileLoginPill();
        } catch (Exception e) {
            e.printStackTrace();
            bindEmptyProfilePhotoAffordance(true);
            bindProfileLoginPill();
        }
    }

    /** Force Guest header + Login CTA; used after logout and when is_login is false. */
    private void paintLoggedOutIdentity() {
        showingReporterUi = false;
        currentUserEmail = "";
        ImageId = null;
        // Belt-and-suspenders: Profile Guest must not leave USER_NAME for comment compose.
        try {
            SharedPrefsData.putString(this, Constants.USER_NAME, "", Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
        if (username_text != null) {
            username_text.setText(ProfileHubLabels.guest(this));
        }
        if (user_login_number != null) {
            user_login_number.setVisibility(View.VISIBLE);
            user_login_number.setText(ProfileHubLabels.guestSubtitle(this));
            user_login_number.setTextColor(
                    ContextCompat.getColor(this, R.color.profile_way2_guest_sub));
        }
        if (img_upload_photo != null) {
            AvatarImageHelper.setProfileEmpty(img_upload_photo);
            bindEmptyProfilePhotoAffordance(true);
        }
        if (login_neomorph_layout != null) {
            login_neomorph_layout.setVisibility(View.VISIBLE);
        }
        if (login != null) {
            login.setVisibility(View.VISIBLE);
        }
        bindProfileLoginPill();
        collapseReporterCreateFab(false);
        bindReporterCreateFab();
    }

    /**
     * Copy external URI → app cache, then CanHub oval crop (same robustness as reporter posts).
     */
    private void onExternalProfileImagePicked(Uri sourceUri) {
        Log.e(TAG, "onExternalProfileImagePicked source=" + sourceUri);
        if (sourceUri == null) {
            return;
        }
        Uri localUri = copyUriToAppCache(sourceUri);
        if (localUri == null) {
            Toast.makeText(this, "Unable to read selected image", Toast.LENGTH_SHORT).show();
            return;
        }
        pendingLocalImageUri = localUri;
        needsCropAfterResume = true;
        if (getLifecycle().getCurrentState().isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) {
            needsCropAfterResume = false;
            final Uri cropUri = localUri;
            getWindow().getDecorView().post(new Runnable() {
                @Override
                public void run() {
                    if (isFinishing() || isDestroyed()) {
                        needsCropAfterResume = true;
                        return;
                    }
                    launchProfileCrop(cropUri);
                }
            });
        }
    }

    private void launchProfileCrop(Uri sourceUri) {
        if (sourceUri == null) {
            return;
        }
        Log.e(TAG, "launchProfileCrop sourceUri=" + sourceUri);
        try {
            cropImage.launch(CropHelper.buildProfileCropOptions(newProfileDetails.this, sourceUri));
        } catch (Exception e) {
            Log.e(TAG, "launchProfileCrop failed - using original", e);
            Toast.makeText(this, "Unable to open crop screen - using original photo",
                    Toast.LENGTH_SHORT).show();
            handleProfileCroppedImage(sourceUri);
        }
    }

    private void handleProfileCroppedImage(Uri selectedUri) {
        Log.e(TAG, "handleProfileCroppedImage uri=" + selectedUri);
        if (selectedUri == null) {
            return;
        }
        try {
            Uri durable = selectedUri;
            if (!isAppOwnedUri(selectedUri)) {
                Uri copied = copyUriToAppCache(selectedUri);
                if (copied != null) {
                    durable = copied;
                }
            }
            filePath = durable;
            pendingLocalImageUri = durable;
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), durable);
            if (img_upload_photo != null) {
                img_upload_photo.setImageBitmap(bitmap);
                img_upload_photo.setVisibility(View.VISIBLE);
            }
            bindEmptyProfilePhotoAffordance(false);
            if (imageupload_progressbar != null) {
                imageupload_progressbar.setVisibility(View.VISIBLE);
            }
            uploadImage();
        } catch (Exception e) {
            Log.e(TAG, "handleProfileCroppedImage failed", e);
            Toast.makeText(this, "Unable to use selected image", Toast.LENGTH_SHORT).show();
            if (imageupload_progressbar != null) {
                imageupload_progressbar.setVisibility(View.GONE);
            }
        }
    }

    private File createProfileImageFile() throws IOException {
        File cachePath = new File(getCacheDir(), "images");
        if (!cachePath.exists() && !cachePath.mkdirs()) {
            throw new IOException("Unable to create images cache directory");
        }
        return File.createTempFile("profile_" + System.currentTimeMillis() + "_", ".jpg", cachePath);
    }

    private Uri copyUriToAppCache(Uri sourceUri) {
        if (sourceUri == null) {
            return null;
        }
        if (isAppOwnedUri(sourceUri)) {
            return sourceUri;
        }
        java.io.InputStream input = null;
        java.io.OutputStream output = null;
        try {
            input = getContentResolver().openInputStream(sourceUri);
            if (input == null) {
                return null;
            }
            File outFile = createProfileImageFile();
            output = new java.io.FileOutputStream(outFile);
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();
            return FileProvider.getUriForFile(this, getPackageName() + ".provider", outFile);
        } catch (Exception e) {
            Log.e(TAG, "copyUriToAppCache exception", e);
            return null;
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (Exception ignored) {
            }
            try {
                if (output != null) {
                    output.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    private boolean isAppOwnedUri(Uri uri) {
        if (uri == null) {
            return false;
        }
        String auth = uri.getAuthority();
        return auth != null && (auth.equals(getPackageName() + ".provider")
                || auth.equals(getPackageName() + ".cropper.fileprovider"));
    }

    private Uri saveBitmapToProfileCache(Bitmap source) {
        if (source == null) {
            return null;
        }
        try {
            File cachePath = new File(getCacheDir(), "images");
            if (!cachePath.exists() && !cachePath.mkdirs()) {
                return null;
            }
            File file = new File(cachePath, "camera_" + System.currentTimeMillis() + ".jpg");
            java.io.FileOutputStream stream = new java.io.FileOutputStream(file);
            source.compress(Bitmap.CompressFormat.JPEG, 95, stream);
            stream.close();
            return FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void uploadImage() {

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST,
                CommonUtilities.URL + "" + APIService.URL_Upload_Image, new com.android.volley.Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                Logone.v("picresponse", response.toString());

                String resultResponse = new String(response.data);
                Logone.v("picresponse", resultResponse.toString());
                try {

                    JSONObject jObj = new JSONObject(resultResponse);
                    boolean error = jObj.getBoolean("success");
                    if (error) {
                        JSONObject extraMsgOBJ = jObj.getJSONObject("extras");
                        ImageId = extraMsgOBJ.getString("url");

                        if (layout_with_out_data != null) {
                            layout_with_out_data.setVisibility(View.GONE);
                        }
                        if (layout_with_data != null) {
                            layout_with_data.setVisibility(View.VISIBLE);
                        }

                        try {
                            if (languageModelResponse != null && languageModelResponse.getData() != null
                                    && languageModelResponse.getData().getImageUploadSuccessfully() != null) {
                                Toast.makeText(newProfileDetails.this,
                                        "" + languageModelResponse.getData().getImageUploadSuccessfully(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception ignored) {
                        }
                        if (img_upload_photo != null) {
                            img_upload_photo.setVisibility(View.VISIBLE);
                            if (ImageId != null) {
                                AvatarImageHelper.loadProfilePicasso(img_upload_photo, ImageId);
                                bindEmptyProfilePhotoAffordance(false);
                            }
                        }
                        // Keep spinner only until profileUpdateimage completes.
                        if (imageupload_progressbar != null) {
                            imageupload_progressbar.setVisibility(View.VISIBLE);
                        }

                        try {
                            SharedPrefsData.putString(getApplicationContext(),
                                    Constants.PROFILE_PIC_URL, ImageId, Constants.PREF_NAME);
                        } catch (Exception ignored) {
                        }

                        ProfileUpdateImage();

                    } else {
                        Toast.makeText(getContext(), "Image not Captured", Toast.LENGTH_SHORT).show();
                        if (imageupload_progressbar != null) {
                            imageupload_progressbar.setVisibility(View.GONE);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (imageupload_progressbar != null) {
                        imageupload_progressbar.setVisibility(View.GONE);
                    }
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                String errorMessage = "Unknown error";
                if (networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        errorMessage = "Request timeout";
                    } else if (error.getClass().equals(NoConnectionError.class)) {
                        errorMessage = "Failed to connect server";
                    }
                } else {
                    String result = new String(networkResponse.data);
                    try {
                        JSONObject response = new JSONObject(result);
                        String status = response.getString("status");
                        String message = response.getString("message");

                        Logone.e("Error Status", status);
                        Logone.e("Error Message", message);

                        if (networkResponse.statusCode == 404) {
                            errorMessage = "Resource not found";
                        } else if (networkResponse.statusCode == 401) {
                            errorMessage = message + " Please login again";
                        } else if (networkResponse.statusCode == 400) {
                            errorMessage = message + " Check your inputs";
                        } else if (networkResponse.statusCode == 500) {
                            errorMessage = message + " Something is getting wrong";
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (imageupload_progressbar != null) {
                    imageupload_progressbar.setVisibility(View.GONE);
                }
                mCommonmethods.showerror(errorMessage);
                error.printStackTrace();
            }
        }, new VolleyMultipartRequest.MultipartProgressListener() {
            @Override
            public void transferred(long transferred, final int progress) {
                newProfileDetails.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Upload progress optional UI
                    }
                });
            }
        }) {
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                byte[] bytes = null;
                try {
                    if (filePath != null) {
                        bytes = AppHelper.getFileDataFromDrawables(newProfileDetails.this, filePath);
                    }
                } catch (Exception ignored) {
                }
                if (bytes == null || bytes.length == 0) {
                    bytes = AppHelper.getFileDataFromDrawable(getContext(), img_upload_photo.getDrawable());
                }
                params.put("file", new DataPart(UUID.randomUUID().toString() + ".jpeg",
                        bytes, "image/jpeg"));
                return params;
            }
        };
        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                100000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue = Volley.newRequestQueue(newProfileDetails.this);
        requestQueue.add(multipartRequest);
    }

    private void ProfileUpdate(String User_Name) {

        JsonObject otp = UpdateOTP(User_Name);


        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.update_user_name("v3", otp);
        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {

                EncreptionRequest encreptionRequest = response.body();
                String encrpt_data = null;
                /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    encrpt_data = decrypt(encreptionRequest.getData());
                }*/
                encrpt_data = decrypt(encreptionRequest.getData());
                Gson gsonObj = new Gson();
                AddProfileResponse newUserProfileResponse = gsonObj.fromJson(encrpt_data, AddProfileResponse.class);

                if (newUserProfileResponse.getSuccess()) {

                    alertDialog.dismiss();
                    layout_with_out_data.setVisibility(View.GONE);
                    layout_with_data.setVisibility(View.VISIBLE);
                    Intent intent = new Intent(newProfileDetails.this, newProfileDetails.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                        startActivity(intent);
                    }
                }

            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {

            }
        });
    }


    /** Logged-in only: edit name + email + photo via update_reporter_profile / upload path. Guests skip. */
    private void showEditNameDialog() {
        boolean loggedIn = SharedPrefsData.getBool(
                newProfileDetails.this, Constants.is_login, Constants.PREF_NAME);
        if (!loggedIn) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(newProfileDetails.this);
        View dialogView = LayoutInflater.from(newProfileDetails.this)
                .inflate(R.layout.dialog_edit_profile, null, false);
        TextView title = dialogView.findViewById(R.id.edit_profile_title);
        CircleImageView editPhoto = dialogView.findViewById(R.id.edit_profile_photo);
        TextView changePhotoBtn = dialogView.findViewById(R.id.edit_profile_change_photo);
        EditText nameEt = dialogView.findViewById(R.id.edit_profile_name);
        EditText emailEt = dialogView.findViewById(R.id.edit_profile_email);
        ProgressBar progress = dialogView.findViewById(R.id.edit_profile_progress);
        TextView cancelBtn = dialogView.findViewById(R.id.edit_profile_cancel);
        TextView saveBtn = dialogView.findViewById(R.id.edit_profile_save);

        if (title != null) {
            title.setText(resolveCompleteProfileLabel());
        }
        if (changePhotoBtn != null) {
            changePhotoBtn.setText(ProfilePhotoLabels.changePhoto(this));
        }
        String editPicUrl = resolveCurrentProfilePhotoUrl();
        if (editPhoto != null) {
            if (!AvatarImageHelper.isMissing(editPicUrl)) {
                AvatarImageHelper.loadProfilePicasso(editPhoto, editPicUrl);
            } else {
                AvatarImageHelper.setProfileEmpty(editPhoto);
            }
            editPhoto.setOnClickListener(v -> {
                if (alertDialog != null && alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
                beginChangeProfilePhoto();
            });
        }
        if (changePhotoBtn != null) {
            changePhotoBtn.setOnClickListener(v -> {
                if (alertDialog != null && alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
                beginChangeProfilePhoto();
            });
        }

        String currentName = username_text != null && username_text.getText() != null
                ? username_text.getText().toString().trim() : "";
        if (currentName.isEmpty()) {
            currentName = SharedPrefsData.getString(this, Constants.USER_NAME, Constants.PREF_NAME);
            if (currentName == null) currentName = "";
        }
        nameEt.setText(currentName);
        if (nameEt.getText().length() > 0) {
            nameEt.setSelection(nameEt.getText().length());
        }

        String emailPrefill = currentUserEmail != null ? currentUserEmail.trim() : "";
        if (emailPrefill.isEmpty()) {
            try {
                if (prefShare != null && prefShare.getEmailId() != null) {
                    emailPrefill = prefShare.getEmailId().trim();
                }
            } catch (Exception ignored) {
            }
        }
        if (emailPrefill.isEmpty()) {
            String fromPrefs = SharedPrefsData.getString(this, Constants.Email, Constants.PREF_NAME);
            if (fromPrefs != null) emailPrefill = fromPrefs.trim();
        }
        emailEt.setText(emailPrefill);

        builder.setView(dialogView);
        alertDialog = builder.create();
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        alertDialog.show();

        cancelBtn.setOnClickListener(v -> {
            if (alertDialog != null && alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
        });

        saveBtn.setOnClickListener(v -> {
            String newName = nameEt.getText() != null ? nameEt.getText().toString().trim() : "";
            String newEmail = emailEt.getText() != null ? emailEt.getText().toString().trim() : "";
            if (newName.isEmpty()) {
                Toast.makeText(newProfileDetails.this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newEmail.isEmpty() && !newEmail.contains("@")) {
                Toast.makeText(newProfileDetails.this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }
            progress.setVisibility(View.VISIBLE);
            saveBtn.setEnabled(false);
            cancelBtn.setEnabled(false);
            // Optimistic header paint before API returns.
            SharedPrefsData.putString(newProfileDetails.this, Constants.USER_NAME, newName, Constants.PREF_NAME);
            if (username_text != null) {
                username_text.setText(newName);
            }
            if (!newEmail.isEmpty()) {
                currentUserEmail = newEmail;
                SharedPrefsData.putString(newProfileDetails.this, Constants.Email, newEmail, Constants.PREF_NAME);
                try {
                    if (prefShare != null) prefShare.setEmailID(newEmail);
                } catch (Exception ignored) {
                }
                if (user_login_number != null) {
                    user_login_number.setText(newEmail);
                }
            }
            refreshAchievedPctFromLocalIdentity();
            setTextSafe((TextView) findViewById(R.id.complete_profile_label), resolveCompleteProfileLabel());
            updateReporterProfile(newName, newEmail, alertDialog, progress, saveBtn, cancelBtn);
        });
    }

    /** Recompute "% profile completed" from prefs/identity after local edit or photo change. */
    private void refreshAchievedPctFromLocalIdentity() {
        try {
            int local = ProfileAchievementHelper.computePctFromPrefs(this);
            // Keep currentUserEmail / name edits that may not be flushed yet.
            NewUserProfileResponse.Data stub = new NewUserProfileResponse().new Data();
            ProfileAchievementHelper.mergeIdentityFromPrefs(this, stub);
            if (currentUserEmail != null && currentUserEmail.contains("@")) {
                stub.setEmail(currentUserEmail);
            }
            if (username_text != null && username_text.getText() != null) {
                String onScreen = username_text.getText().toString().trim();
                if (!onScreen.isEmpty() && !"guest".equalsIgnoreCase(onScreen)) {
                    stub.setUserName(onScreen);
                }
            }
            local = Math.max(local, ProfileAchievementHelper.computeLocalProfileCompletionPct(stub));
            bindApprovalProgress(String.valueOf(local));
            SharedPrefsData.putString(this, Constants.PROFILE_ACHIEVED_PCT,
                    String.valueOf(local), Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }

    /** Finish profile → Home news feed (under-activity kept for feed state). */
    private void returnToNewsFeed() {
        navigateHomeWithFooterTab(R.id.navigation_home);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            setIntent(intent);
        }
        // Warm re-show from Home footer — keep Profile tab selected, refresh light chrome.
        try {
            if (profileBottomNav != null) {
                profileBottomNav.setSelectedItemId(R.id.navigation_profile);
            }
            applyProfileFooterTabLabels();
            bindProfileLoginPill();
            applyLoginGatedHubRows();
        } catch (Exception ignored) {
        }
    }

    private JsonObject getEditProfileData(String name, String email) {
        UserProfileRequest requestModel = new UserProfileRequest();
        requestModel.setDraftId(SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
        requestModel.setUserName(name != null ? name.trim() : "");
        requestModel.setEmail(email != null ? email.trim() : "");
        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        showLog("Edit Profile REQUEST " + jsonString);
        String encrpt_data = encrypt(jsonString);

        EncritonRequestData Encriton_Request = new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }

    private void updateReporterProfile(final String name, final String email,
                                       final AlertDialog dialog, final ProgressBar progress,
                                       final TextView saveBtn, final TextView cancelBtn) {
        JsonObject body = getEditProfileData(name, email);
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.update_reporter_profile("v3", body);
        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                boolean success = false;
                try {
                    EncreptionRequest encreptionRequest = response.body();
                    if (encreptionRequest != null && encreptionRequest.getData() != null) {
                        String decrypted = decrypt(encreptionRequest.getData());
                        AddProfileResponse profileResponse = new Gson().fromJson(decrypted, AddProfileResponse.class);
                        success = profileResponse != null && Boolean.TRUE.equals(profileResponse.getSuccess());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                progress.setVisibility(View.GONE);
                saveBtn.setEnabled(true);
                cancelBtn.setEnabled(true);

                if (success) {
                    SharedPrefsData.putString(newProfileDetails.this, Constants.USER_NAME, name, Constants.PREF_NAME);
                    if (username_text != null) {
                        username_text.setText(name);
                    }
                    currentUserEmail = email != null ? email : "";
                    if (email != null && !email.isEmpty()) {
                        SharedPrefsData.putString(newProfileDetails.this, Constants.Email, email, Constants.PREF_NAME);
                        try {
                            if (prefShare != null) prefShare.setEmailID(email);
                        } catch (Exception ignored) {
                        }
                        if (user_login_number != null) {
                            user_login_number.setText(email);
                        }
                    }
                    refreshAchievedPctFromLocalIdentity();
                    setTextSafe((TextView) findViewById(R.id.complete_profile_label),
                            resolveCompleteProfileLabel());
                    Toast.makeText(newProfileDetails.this, "Profile updated", Toast.LENGTH_SHORT).show();
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    // Soft-refresh from backend so counts/name stay consistent.
                    getWriterProfileData();
                } else {
                    Toast.makeText(newProfileDetails.this, "Could not update profile. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                progress.setVisibility(View.GONE);
                saveBtn.setEnabled(true);
                cancelBtn.setEnabled(true);
                Toast.makeText(newProfileDetails.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (profileReporterFabExpanded) {
            collapseReporterCreateFab(true);
            return;
        }
        // System / gesture back — same reverse slide as News >
        super.onBackPressed();
    }

    /**
     * Opens wallet history immediately (no spin-wait on profile).
     * @param tab 1 = article amount transactions, 2 = payment / redeem history
     */
    private void openWalletHistory(int tab) {
        boolean is_login = SharedPrefsData.getBool(newProfileDetails.this, Constants.is_login, Constants.PREF_NAME);
        if (!is_login) {
            openReporterLogin();
            return;
        }
        Intent intent = new Intent(newProfileDetails.this, MainActivity.class);
        String amount = profile_total_amount != null ? profile_total_amount.getText().toString() : "";
        intent.putExtra(MainActivity.EXTRA_PAYMENT_HISTORY, amount);
        intent.putExtra(MainActivity.EXTRA_OPEN_TAB, tab);
        // Warm header cache for next opens
        WalletHistoryCache.saveTotalPointsDisplay(this, amount);
        startActivity(intent);
    }

    private void setStaticdata() {
        prefShare = new Allsharedpreference(newProfileDetails.this);
        Gson gson = new Gson();
        String Translation = prefShare.getShared_String(Allsharedpreference.Translator);
        languageModelResponse = gson.fromJson(Translation, LanguageModelResponse.class);

        // Stale Hindi (etc.) pack while LanguageID is Telugu — prefer LanguageID chrome
        // immediately and force a full get_static_data refresh.
        final boolean packOk = TranslatorPackHelper.packMatchesLanguage(this);
        if (!packOk) {
            TranslatorPackHelper.ensureMatchesSelectedLanguage(this, pack -> {
                if (isFinishing() || isDestroyed()) return;
                try {
                    if (pack != null) {
                        languageModelResponse = pack;
                    } else {
                        String t = prefShare.getShared_String(Allsharedpreference.Translator);
                        languageModelResponse = gson.fromJson(t, LanguageModelResponse.class);
                    }
                    applyAllProfileLanguageLabels();
                    bindReporterFabLabels();
                } catch (Exception ignored) {
                }
            });
        }

        applyAllProfileLanguageLabels();
    }

    /** Rebind every Profile chrome label from pack (if matched) or LanguageID helpers. */
    private void applyAllProfileLanguageLabels() {
        if (language_name != null) {
            language_name.setText(resolveCurrentLanguageDisplay());
        }
        if (textViewLanguageNameUser != null) {
            textViewLanguageNameUser.setText(resolveCurrentLanguageDisplay());
        }
        TextView langLabel = findViewById(R.id.profile_language_label);
        if (langLabel != null) {
            // Always English heading — values (తెలుగు / हिन्दी) stay localized below.
            langLabel.setText(ProfileHubLabels.selectLanguage(this));
        }
        if (textViewFollowingUser != null) {
            textViewFollowingUser.setText(" " + packString(
                    languageModelResponse != null && languageModelResponse.getData() != null
                            ? languageModelResponse.getData().getFollowing() : null,
                    ProfileHubLabels.following(this)));
        }
        if (textViewShareEarnUser != null) {
            textViewShareEarnUser.setText(ProfileHubLabels.shareApp(this));
        }
        if (textViewFollowMore != null
                && languageModelResponse != null
                && languageModelResponse.getData() != null
                && TranslatorPackHelper.packMatchesLanguage(this)) {
            textViewFollowMore.setText("" + languageModelResponse.getData().getFollowMore());
        }

        if (profile_article_name != null) {
            profile_article_name.setText(" " + packString(
                    languageModelResponse != null && languageModelResponse.getData() != null
                            ? languageModelResponse.getData().getArticles() : null,
                    ProfileHubLabels.articles(this)));
        }
        if (profile_like_name != null
                && languageModelResponse != null
                && languageModelResponse.getData() != null
                && TranslatorPackHelper.packMatchesLanguage(this)) {
            profile_like_name.setText(" " + languageModelResponse.getData().getLikes());
        }
        if (profile_followers_name != null) {
            profile_followers_name.setText(" " + packString(
                    languageModelResponse != null && languageModelResponse.getData() != null
                            ? languageModelResponse.getData().getFollowers() : null,
                    ProfileHubLabels.followers(this)));
        }
        if (profile_following_name != null) {
            profile_following_name.setText(" " + packString(
                    languageModelResponse != null && languageModelResponse.getData() != null
                            ? languageModelResponse.getData().getFollowing() : null,
                    ProfileHubLabels.following(this)));
        }
        if (writer_following_name != null) {
            writer_following_name.setText(" " + packString(
                    languageModelResponse != null && languageModelResponse.getData() != null
                            ? languageModelResponse.getData().getFollowing() : null,
                    ProfileHubLabels.following(this)));
        }
        if (profile_follow_more != null
                && languageModelResponse != null
                && languageModelResponse.getData() != null
                && TranslatorPackHelper.packMatchesLanguage(this)) {
            profile_follow_more.setText("" + languageModelResponse.getData().getFollowMore());
        }
        if (user_rewards != null
                && languageModelResponse != null
                && languageModelResponse.getData() != null
                && TranslatorPackHelper.packMatchesLanguage(this)) {
            user_rewards.setText("" + languageModelResponse.getData().getRewards());
        }
        if (user_share_to_earn != null
                && languageModelResponse != null
                && languageModelResponse.getData() != null
                && TranslatorPackHelper.packMatchesLanguage(this)) {
            user_share_to_earn.setText("" + languageModelResponse.getData().getShareToEarn());
        }
        if (user_follow_more_data != null
                && languageModelResponse != null
                && languageModelResponse.getData() != null
                && TranslatorPackHelper.packMatchesLanguage(this)) {
            user_follow_more_data.setText("" + languageModelResponse.getData().getFollowMore());
        }
        if (only_language_name != null) {
            only_language_name.setText(resolveCurrentLanguageDisplay());
        }
        if (total_amount != null
                && languageModelResponse != null
                && languageModelResponse.getData() != null
                && TranslatorPackHelper.packMatchesLanguage(this)) {
            total_amount.setText("" + languageModelResponse.getData().getTotalAmount());
        }
        if (profile_next_payment != null) {
            profile_next_payment.setText("Earnings");
        }
        if (logintext != null
                && languageModelResponse != null
                && languageModelResponse.getData() != null
                && TranslatorPackHelper.packMatchesLanguage(this)) {
            logintext.setText("" + languageModelResponse.getData().getLogin());
        }
        if (account_delete_name != null
                && languageModelResponse != null
                && languageModelResponse.getData() != null
                && TranslatorPackHelper.packMatchesLanguage(this)) {
            account_delete_name.setText("" + languageModelResponse.getData().getDelete_account());
        }
        if (textViewAppDelete != null) {
            String del = languageModelResponse != null && languageModelResponse.getData() != null
                    ? languageModelResponse.getData().getDelete_account() : null;
            if (TranslatorPackHelper.packMatchesLanguage(this)
                    && del != null && !del.equalsIgnoreCase("null")) {
                textViewAppDelete.setText(del);
            } else {
                textViewAppDelete.setText("Delete Account");
            }
        }
        if (textViewAppLogout != null) {
            textViewAppLogout.setText(R.string.profile_logout);
        }
        bindNewsNavLabel();
        applyProfileHubTranslatedLabels();
        bindProfileLoginPill();
        bindLocationRowText();
        bindStatusVideosNewBadge();
        bindBecomeReporterNewBadge();
        bindBirthdaySpecialNewBadge();
        bindReporterShortcutLabels();
        applyProfileFooterTabLabels();
    }

    /** Pack string only when translator LanguageID matches; else LanguageID fallback. */
    private String packString(@Nullable String packValue, @Nullable String fallback) {
        String v = TranslatorPackHelper.packOrFallback(this, packValue, fallback);
        return v != null ? v : "";
    }

    /**
     * Profile hub / Way2 menu labels from SessionPreferences translator JSON
     * ({@link Allsharedpreference#Translator} → {@link LanguageModelResponse}), with
     * LanguageID helpers when the pack is missing or mismatched (Hindi leftovers on Telugu).
     */
    private void applyProfileHubTranslatedLabels() {
        LanguageModelResponse.Data d =
                languageModelResponse != null ? languageModelResponse.getData() : null;
        boolean packOk = TranslatorPackHelper.packMatchesLanguage(this);

        TextView tagline = findViewById(R.id.profile_tagline);
        if (tagline != null) {
            // No translator field for marketing tagline — keep resource (may be English).
            tagline.setText(R.string.profile_tagline);
        }

        setHubLabel(R.id.profile_categories_label,
                packOk ? firstTranslator(d != null ? d.getCategory() : null,
                        d != null ? d.getDiscover() : null,
                        getString(R.string.profile_categories))
                        : getString(R.string.profile_categories));

        TextView viewAll = findViewById(R.id.profile_categories_view_all);
        if (viewAll != null) {
            String view = packOk && d != null ? d.getView() : null;
            if (notEmptyTranslator(view) && !"view".equalsIgnoreCase(view.trim())) {
                viewAll.setText(view.trim());
            } else {
                viewAll.setText(R.string.profile_categories_view_all);
            }
        }

        setHubLabel(R.id.profile_status_videos_label, ProfileHubLabels.statusVideos(this));
        setHubLabel(R.id.profile_become_a_reporter_label, ProfileHubLabels.becomeAReporter(this));
        setHubLabel(R.id.profile_advertise_label, ProfileHubLabels.advertiseOnAnyNews(this));
        setHubLabel(R.id.profile_birthday_label, ProfileHubLabels.birthdaySpecial(this));

        TextView locationCaption = findViewById(R.id.profile_location_label);
        if (locationCaption != null) {
            locationCaption.setText(ProfileHubLabels.selectedLocation(this));
        }

        setHubLabel(R.id.profile_settings_row_label,
                packString(d != null ? d.getSettings() : null, ProfileHubLabels.settings(this)));

        TextView feedbackLabel = findViewById(R.id.profile_feedback_label);
        if (feedbackLabel != null) {
            feedbackLabel.setText(packString(
                    d != null ? d.getFeedback() : null, ProfileHubLabels.feedback(this)));
        }

        TextView contactLabel = findViewById(R.id.profile_contact_label);
        if (contactLabel != null) {
            contactLabel.setText(packString(
                    d != null ? d.getContactUs() : null, getString(R.string.profile_contact_us)));
        }

        TextView bmLabel = findViewById(R.id.profile_bookmarks_label);
        if (bmLabel != null) {
            bmLabel.setText(HomeOfflineModeLabels.bookmarks(this));
        }

        if (textViewFollowingUser != null) {
            textViewFollowingUser.setText(" " + packString(
                    d != null ? d.getFollowing() : null, ProfileHubLabels.following(this)));
        }
        if (textViewShareEarnUser != null) {
            textViewShareEarnUser.setText(ProfileHubLabels.shareApp(this));
        }
        // Login CTA text is applied in bindProfileLoginPill (guest-only).
    }

    private void setHubLabel(int id, String text) {
        TextView tv = findViewById(id);
        if (tv != null && text != null) tv.setText(text);
    }

    private static boolean notEmptyTranslator(String s) {
        return s != null && !s.trim().isEmpty() && !"null".equalsIgnoreCase(s.trim());
    }

    private static String firstTranslator(String a, String b, String fallback) {
        if (notEmptyTranslator(a)) return a.trim();
        if (notEmptyTranslator(b)) return b.trim();
        return fallback;
    }

    /** Way2 top-right: always "News" (locale), never Home / Reload pack wording. */
    private void bindNewsNavLabel() {
        if (tvNewsNav == null) {
            return;
        }
        String label = com.anyNews.anynews.Utils.NewsFooterLabel.stripNavDecoration(
                com.anyNews.anynews.Utils.NewsFooterLabel.forCurrentLanguage(this));
        tvNewsNav.setText(label);
    }

    /** Native+English display for current language id (fallback when LanName not stored). */
    private String resolveCurrentLanguageDisplay() {
        int id = SharedPrefsData.getInt(this, Constants.LanguageID, Constants.PREF_NAME);
        // Prefer LanguageID over stale Constants.Lang (login used to leave "हिन्दी"
        // while LanguageID flipped to Telugu — mixed Hindi/Telugu Profile chrome).
        if (id > 0) {
            return languageDisplayNameForId(id);
        }
        String saved = SharedPrefsData.getString(this, Constants.Lang, Constants.PREF_NAME);
        if (saved != null && !saved.trim().isEmpty() && !"null".equalsIgnoreCase(saved.trim())) {
            return saved.trim();
        }
        if (languageModelResponse != null && languageModelResponse.getData() != null
                && languageModelResponse.getData().getLanguage() != null) {
            return languageModelResponse.getData().getLanguage();
        }
        return "Language";
    }

    private static String languageDisplayNameForId(int languageId) {
        switch (languageId) {
            case 1: return "മലയാളം";
            case 2: return "हिन्दी";
            case 3: return "తెలుగు";
            case 4: return "தமிழ்";
            case 5: return "ಕನ್ನಡ";
            case 6: return "ଓଡ଼ିଆ";
            default: return "Language";
        }
    }

    private void bindLocationRowText() {
        TextView locationValue = findViewById(R.id.profile_location_value);
        if (locationValue == null) return;
        try {
            DistrictLabelHelper.ensureSelectedMatchesLanguage(this);
        } catch (Exception ignored) {
        }
        String dist = DistrictLabelHelper.resolveDisplayLabel(this);
        if (dist == null || dist.isEmpty()) {
            if (local_distic_name != null && local_distic_name.getText() != null) {
                String t = local_distic_name.getText().toString().trim();
                if (DistrictLabelHelper.isUsable(t)
                        && DistrictLabelHelper.labelCompatibleWithLanguage(
                                t,
                                SharedPrefsData.getInt(this, Constants.LanguageID, Constants.PREF_NAME))) {
                    dist = t;
                }
            }
        }
        if (dist == null || dist.isEmpty()) {
            locationValue.setText(ProfileHubLabels.selectedLocation(this));
            locationValue.setTextColor(ContextCompat.getColor(this, R.color.profile_way2_value));
            return;
        }
        String change = resolveLocationChangeSuffix();
        String full = dist + change;
        android.text.SpannableString span = new android.text.SpannableString(full);
        int start = dist.length();
        int end = full.length();
        int orange = ContextCompat.getColor(this, R.color.brand_orange);
        int value = ContextCompat.getColor(this, R.color.profile_way2_value);
        span.setSpan(new android.text.style.ForegroundColorSpan(value), 0, start,
                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new android.text.style.ForegroundColorSpan(orange), start, end,
                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, start,
                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        locationValue.setText(span);
    }

    /** Localized " (Change)" suffix for the location tile; LanguageID when pack mismatches. */
    private String resolveLocationChangeSuffix() {
        LanguageModelResponse.Data d =
                languageModelResponse != null ? languageModelResponse.getData() : null;
        if (TranslatorPackHelper.packMatchesLanguage(this)
                && d != null && notEmptyTranslator(d.getEdit())) {
            return " (" + d.getEdit().trim() + ")";
        }
        return ProfileHubLabels.editSuffix(this);
    }

    /** Orange "New" badge on Status Videos — labeled via locale; hidden after first open. */
    private void bindStatusVideosNewBadge() {
        TextView badge = findViewById(R.id.profile_status_videos_new_badge);
        View statusRow = findViewById(R.id.layout_status_videos);
        if (badge == null) return;

        boolean statusVisible = statusRow != null
                && statusRow.getVisibility() == View.VISIBLE
                && ProfileHubConfigHelper.isStatusVideosEnabled(this);
        boolean seen = SharedPrefsData.getBool(
                this, Constants.STATUS_VIDEOS_BADGE_SEEN, Constants.PREF_NAME);

        if (!statusVisible || seen) {
            stopStatusVideosBadgePulse();
            badge.setVisibility(View.GONE);
            return;
        }

        int id = SharedPrefsData.getInt(this, Constants.LanguageID, Constants.PREF_NAME);
        String label;
        switch (id) {
            case 1: label = "പുതിയ"; break;
            case 2: label = "नया"; break;
            case 3: label = "కొత్త"; break;
            case 4: label = "புதிய"; break;
            case 5: label = "ಹೊಸ"; break;
            case 6: label = "ନୂଆ"; break;
            default: label = getString(R.string.profile_following_new); break;
        }
        badge.setText(label);
        badge.setVisibility(View.VISIBLE);
        startStatusVideosBadgePulse(badge);
    }

    private void startStatusVideosBadgePulse(View badge) {
        if (badge == null) return;
        if (statusVideosBadgePulse != null && statusVideosBadgePulse.isRunning()) {
            return;
        }
        stopStatusVideosBadgePulse();
        badge.setAlpha(1f);
        ObjectAnimator anim = ObjectAnimator.ofFloat(badge, View.ALPHA, 1f, 0.4f);
        anim.setDuration(1000);
        anim.setRepeatCount(ValueAnimator.INFINITE);
        anim.setRepeatMode(ValueAnimator.REVERSE);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        statusVideosBadgePulse = anim;
        anim.start();
    }

    private void stopStatusVideosBadgePulse() {
        if (statusVideosBadgePulse != null) {
            statusVideosBadgePulse.cancel();
            statusVideosBadgePulse = null;
        }
        TextView badge = findViewById(R.id.profile_status_videos_new_badge);
        if (badge != null) {
            badge.clearAnimation();
            badge.setAlpha(1f);
        }
    }

    private void markStatusVideosBadgeSeen() {
        SharedPrefsData.putBool(this, Constants.STATUS_VIDEOS_BADGE_SEEN, true, Constants.PREF_NAME);
        stopStatusVideosBadgePulse();
        TextView badge = findViewById(R.id.profile_status_videos_new_badge);
        if (badge != null) {
            badge.setVisibility(View.GONE);
        }
    }

    /** Orange "New" badge on Become a Reporter — all languages; hide after first open. */
    private void bindBecomeReporterNewBadge() {
        TextView badge = findViewById(R.id.profile_become_a_reporter_new_badge);
        View row = findViewById(R.id.layout_become_a_reporter);
        if (badge == null) return;

        boolean rowVisible = row != null && row.getVisibility() == View.VISIBLE;
        boolean seen = SharedPrefsData.getBool(
                this, Constants.BECOME_REPORTER_BADGE_SEEN, Constants.PREF_NAME);

        if (!rowVisible || seen) {
            badge.setVisibility(View.GONE);
            badge.clearAnimation();
            badge.setAlpha(1f);
            return;
        }

        int id = SharedPrefsData.getInt(this, Constants.LanguageID, Constants.PREF_NAME);
        String label;
        switch (id) {
            case 1: label = "പുതിയ"; break;
            case 2: label = "नया"; break;
            case 3: label = "కొత్త"; break;
            case 4: label = "புதிய"; break;
            case 5: label = "ಹೊಸ"; break;
            case 6: label = "ନୂଆ"; break;
            default: label = getString(R.string.profile_following_new); break;
        }
        badge.setText(label);
        badge.setVisibility(View.VISIBLE);
        badge.setAlpha(1f);
        ObjectAnimator anim = ObjectAnimator.ofFloat(badge, View.ALPHA, 1f, 0.4f);
        anim.setDuration(1000);
        anim.setRepeatCount(ValueAnimator.INFINITE);
        anim.setRepeatMode(ValueAnimator.REVERSE);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        badge.clearAnimation();
        anim.start();
        badge.setTag(anim);
    }

    private void markBecomeReporterBadgeSeen() {
        SharedPrefsData.putBool(
                this, Constants.BECOME_REPORTER_BADGE_SEEN, true, Constants.PREF_NAME);
        TextView badge = findViewById(R.id.profile_become_a_reporter_new_badge);
        if (badge != null) {
            Object tag = badge.getTag();
            if (tag instanceof ObjectAnimator) {
                ((ObjectAnimator) tag).cancel();
            }
            badge.clearAnimation();
            badge.setAlpha(1f);
            badge.setVisibility(View.GONE);
        }
    }

    /** Orange "New" badge on Birthday Special — hide after user opens it once. */
    private void bindBirthdaySpecialNewBadge() {
        TextView badge = findViewById(R.id.profile_birthday_new_badge);
        View row = findViewById(R.id.layout_birthday_special);
        if (badge == null) return;

        boolean rowVisible = row != null
                && row.getVisibility() == View.VISIBLE
                && ProfileHubConfigHelper.isBirthdaySpecialEnabled(this);
        boolean seen = SharedPrefsData.getBool(
                this, Constants.BIRTHDAY_SPECIAL_BADGE_SEEN, Constants.PREF_NAME);

        if (!rowVisible || seen) {
            Object tag = badge.getTag();
            if (tag instanceof ObjectAnimator) {
                ((ObjectAnimator) tag).cancel();
            }
            badge.clearAnimation();
            badge.setAlpha(1f);
            badge.setVisibility(View.GONE);
            return;
        }

        int id = SharedPrefsData.getInt(this, Constants.LanguageID, Constants.PREF_NAME);
        String label;
        switch (id) {
            case 1: label = "പുതിയ"; break;
            case 2: label = "नया"; break;
            case 3: label = "కొత్త"; break;
            case 4: label = "புதிய"; break;
            case 5: label = "ಹೊಸ"; break;
            case 6: label = "ନୂଆ"; break;
            default: label = getString(R.string.profile_following_new); break;
        }
        badge.setText(label);
        badge.setVisibility(View.VISIBLE);
        badge.setAlpha(1f);
        ObjectAnimator anim = ObjectAnimator.ofFloat(badge, View.ALPHA, 1f, 0.4f);
        anim.setDuration(1000);
        anim.setRepeatCount(ValueAnimator.INFINITE);
        anim.setRepeatMode(ValueAnimator.REVERSE);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        badge.clearAnimation();
        anim.start();
        badge.setTag(anim);
    }

    private void markBirthdaySpecialBadgeSeen() {
        SharedPrefsData.putBool(
                this, Constants.BIRTHDAY_SPECIAL_BADGE_SEEN, true, Constants.PREF_NAME);
        TextView badge = findViewById(R.id.profile_birthday_new_badge);
        if (badge != null) {
            Object tag = badge.getTag();
            if (tag instanceof ObjectAnimator) {
                ((ObjectAnimator) tag).cancel();
            }
            badge.clearAnimation();
            badge.setAlpha(1f);
            badge.setVisibility(View.GONE);
        }
    }

    /** Show orange Login → pill for guests (not logged in via reporter login). */
    private void bindProfileLoginPill() {
        boolean loggedIn = SharedPrefsData.getBool(this, Constants.is_login, Constants.PREF_NAME);
        if (login_neomorph_layout != null) {
            login_neomorph_layout.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
        }
        if (login != null) {
            login.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
        }
        if (logintext != null && !loggedIn) {
            LanguageModelResponse.Data d =
                    languageModelResponse != null ? languageModelResponse.getData() : null;
            String loginLabel = firstTranslator(d != null ? d.getLogin() : null, null, null);
            if (loginLabel != null) {
                logintext.setText(loginLabel + " →");
            } else {
                logintext.setText(ProfileHubLabels.loginCta(this));
            }
            logintext.setTextColor(ContextCompat.getColor(this, R.color.profile_way2_login_text));
        }
    }

    /** Guest card defaults matching Profile mock (before network paints real user). */
    private void applyGuestProfileCardDefaults() {
        boolean loggedIn = SharedPrefsData.getBool(this, Constants.is_login, Constants.PREF_NAME);
        if (!loggedIn) {
            paintLoggedOutIdentity();
            return;
        }
        bindProfileLoginPill();
    }

    private JsonObject set_writer_profile_data() {


        UserProfileRequest requestModel = new UserProfileRequest();
        if(prefShare.getEmailId()!=null){
            requestModel.setEmail(prefShare.getEmailId());
        }
        requestModel.setDraftId(SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        showLog("API REQUEST E" + jsonString.toString());
        encrpt_data = encrypt(jsonString);

        EncritonRequestData Encriton_Request = new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }

    private JsonObject UpdateOTP(String User_Name) {


        UserProfileRequest requestModel = new UserProfileRequest();
        requestModel.setDraftId(SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
        requestModel.setUserName(User_Name != null ? User_Name.trim() : "");
        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        showLog("API REQUEST E" + jsonString.toString());
        encrpt_data = encrypt(jsonString);

        EncritonRequestData Encriton_Request = new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }

    /**
     * Renders the last-known reporter profile from memory+disk cache so the screen
     * shows real content immediately on open instead of a blank blocking spinner.
     * Network soft-refresh updates values when ready.
     */
    private void showCachedProfileInstant() {
        try {
            if (loadingProgressbar != null) loadingProgressbar.setVisibility(View.GONE);
            if (loadingProfileView != null) loadingProfileView.setVisibility(View.VISIBLE);
            if (imageupload_progressbar != null) imageupload_progressbar.setVisibility(View.GONE);
            if (layout_with_out_data != null) layout_with_out_data.setVisibility(View.GONE);
            if (layout_with_data != null) layout_with_data.setVisibility(View.VISIBLE);
            if (img_upload_photo != null) img_upload_photo.setVisibility(View.VISIBLE);
        } catch (Exception ignored) {
        }

        boolean loggedIn = SharedPrefsData.getBool(this, Constants.is_login, Constants.PREF_NAME);
        if (!loggedIn) {
            paintLoggedOutIdentity();
            applyReporterChrome(false);
            bindReporterDesk();
            return;
        }

        // Always re-assert local identity (email/name) so empty cache still shows prefs.
        paintIdentityFromLocalPrefs();

        try {
            NewUserProfileResponse resp = ReporterProfileCache.load(getApplicationContext());
            if (resp == null || !Boolean.TRUE.equals(resp.getSuccess()) || resp.getData() == null) {
                boolean flag = SharedPrefsData.getBool(this, Constants.is_reporter, Constants.PREF_NAME);
                applyReporterChrome(flag);
                if (flag) {
                    try {
                        WalletHistoryPrefetcher.prefetchIfPossible(getApplicationContext());
                    } catch (Exception ignored) {
                    }
                }
                bindReporterDesk();
                bindReporterCreateFab();
                return;
            }
            applyProfileDataToUi(resp.getData(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Single paint path for cache + network. Safe across theme recreate. */
    private void applyProfileDataToUi(NewUserProfileResponse.Data d, boolean fromCache) {
        if (d == null) return;

        // After logout, draft-bound profile API must not resurrect name/email/reporter UI.
        if (!SharedPrefsData.getBool(this, Constants.is_login, Constants.PREF_NAME)) {
            paintLoggedOutIdentity();
            applyReporterChrome(false);
            bindReporterDesk();
            return;
        }

        if (local_distic_name != null) {
            local_distic_name.setVisibility(View.GONE);
            if (d.getLocation() != null) local_distic_name.setText(d.getLocation());
        }
        // Persist account geography from profile API so Profile location row
        // (prefs-backed) matches Writers/Users after login / admin location edit.
        try {
            int langId = d.getLanguageId() != null ? d.getLanguageId() : 0;
            int stateId = d.getStateId() != null ? d.getStateId() : 0;
            int distId = d.getDistId() != null ? d.getDistId() : 0;
            String loc = d.getLocation() != null ? d.getLocation().trim() : "";
            final int profileLangRaw = langId;
            int sessionLangBefore = SharedPrefsData.getInt(
                    getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
            if (langId > 0) {
                int prevLang = sessionLangBefore;
                // Session LanguageID (after Update_User_Language) is authoritative.
                // Stale Writers / ReporterProfileCache must not revert it.
                if (prevLang > 0 && langId != prevLang) {
                    langId = prevLang;
                } else if (langId != prevLang) {
                    SharedPrefsData.putInt(getApplicationContext(), Constants.LanguageID,
                            langId, Constants.PREF_NAME);
                    // Keep language row value in sync with LanguageID (avoid stale "हिन्दी").
                    SharedPrefsData.putString(getApplicationContext(), Constants.Lang,
                            languageDisplayNameForId(langId), Constants.PREF_NAME);
                    try {
                        LanguageFeedInvalidator.onLanguageChanged(
                                getApplicationContext(), langId, false);
                    } catch (Exception ignored) {
                    }
                    try {
                        CategoriesDiskCache.invalidate(getApplicationContext());
                    } catch (Exception ignored) {
                    }
                } else if (prevLang <= 0) {
                    SharedPrefsData.putInt(getApplicationContext(), Constants.LanguageID,
                            langId, Constants.PREF_NAME);
                    SharedPrefsData.putString(getApplicationContext(), Constants.Lang,
                            languageDisplayNameForId(langId), Constants.PREF_NAME);
                }
            }
            if (stateId > 0) {
                SharedPrefsData.putInt(getApplicationContext(), Constants.StateID,
                        stateId, Constants.PREF_NAME);
            }
            // Stale cache for another language must not overwrite the restored slot.
            boolean staleLangCache = fromCache
                    && profileLangRaw > 0
                    && sessionLangBefore > 0
                    && profileLangRaw != sessionLangBefore;
            if (!staleLangCache && (distId > 0 || DistrictLabelHelper.isUsable(loc))) {
                // Cache paint must not overwrite a fresher local district pick
                // (e.g. user just changed district in Daily Updates).
                if (fromCache) {
                    int prefDist = DistrictLabelHelper.getStoredDistId(getApplicationContext());
                    String prefLabel = DistrictLabelHelper.resolveDisplayLabel(getApplicationContext());
                    boolean prefsAlreadyFresh = prefDist > 0
                            && distId > 0
                            && prefDist != distId;
                    boolean prefsHaveLabel = DistrictLabelHelper.isUsable(prefLabel);
                    if (prefsAlreadyFresh && prefsHaveLabel) {
                        // Keep local selection; network soft-refresh will reconcile.
                    } else {
                        int langForSave = langId > 0
                                ? langId
                                : SharedPrefsData.getInt(this, Constants.LanguageID, Constants.PREF_NAME);
                        DistrictLabelHelper.saveFromServer(
                                getApplicationContext(), langForSave, distId,
                                DistrictLabelHelper.isUsable(loc) ? loc : null);
                    }
                } else {
                    int langForSave = langId > 0
                            ? langId
                            : SharedPrefsData.getInt(this, Constants.LanguageID, Constants.PREF_NAME);
                    // Rejects wrong-script labels (e.g. भिवानी under Telugu) and clears.
                    DistrictLabelHelper.saveFromServer(
                            getApplicationContext(), langForSave, distId,
                            DistrictLabelHelper.isUsable(loc) ? loc : null);
                }
            }
            DistrictLabelHelper.ensureSelectedMatchesLanguage(getApplicationContext());
        } catch (Exception ignored) {
        }
        // Prefer real name over draft "Guest" (login prefs often already have it).
        ProfileAchievementHelper.mergeIdentityFromPrefs(this, d);
        if (username_text != null && d.getUserName() != null
                && !d.getUserName().trim().isEmpty()
                && !"guest".equalsIgnoreCase(d.getUserName().trim())) {
            username_text.setText(d.getUserName().trim());
        }

        long articles = d.getArticles() != null ? d.getArticles() : 0L;
        long likes = d.getLikes() != null ? d.getLikes() : 0L;
        long followers = d.getFollowers() != null ? d.getFollowers() : 0L;
        long following = d.getFollowing() != null ? d.getFollowing() : 0L;
        long totalPts = 0L;
        try { totalPts = d.getTotalPoints(); } catch (Exception ignored) { }
        Long available = d.getAvailablePoints();
        long availablePts = available != null ? available : 0L;

        setTextSafe(profile_article_count, String.valueOf(articles));
        setTextSafe(profile_like_count, String.valueOf(likes));
        setTextSafe(profile_followers_count, String.valueOf(followers));
        setTextSafe(profile_following_count, String.valueOf(following));
        setTextSafe(writer_following_count, String.valueOf(following));
        setTextSafe(profile_total_amount, "\u20B9 " + totalPts);
        setTextSafe(earned_total_amount, "\u20B9 " + availablePts);

        setTextSafe((TextView) findViewById(R.id.tile_articles_count), formatCompactCount(articles));
        setTextSafe((TextView) findViewById(R.id.tile_followers_count), formatCompactCount(followers));
        setTextSafe((TextView) findViewById(R.id.tile_earnings_count), "\u20B9 " + totalPts);

        // Admin-controlled payment banner (not auto Notify / mark-paid).
        // Honour explicit active flag; never treat leftover text as "show".
        // Empty upcoming_payment_message from server means Off — do not fall back
        // to stale prefs (that kept the white box visible after Admin Off).
        TextView upcomingBanner = findViewById(R.id.tv_upcoming_payment_banner);
        if (upcomingBanner != null) {
            Boolean activeFlag = d.getReporterPaymentBannerActive();
            String msg = d.getUpcomingPaymentMessage();
            boolean active;
            String display;
            if (activeFlag != null) {
                active = Boolean.TRUE.equals(activeFlag);
                display = active && msg != null ? msg.trim() : "";
                if (active && TextUtils.isEmpty(display)) {
                    display = ReporterPaymentAppConfig.bannerText(this);
                    if (display == null) display = "";
                }
            } else if (msg != null) {
                // Field present: empty ⇒ Off (clears stale); non-empty ⇒ On.
                display = msg.trim();
                active = !TextUtils.isEmpty(display);
            } else {
                // Field absent (offline / old payload): prefs only.
                active = ReporterPaymentAppConfig.bannerActive(this);
                display = active ? ReporterPaymentAppConfig.bannerText(this) : "";
                if (display == null) display = "";
            }
            if (!active) {
                display = "";
            }
            boolean show = active
                    && !TextUtils.isEmpty(display)
                    && Boolean.TRUE.equals(d.getIsReporter());
            if (show) {
                upcomingBanner.setText(display);
                upcomingBanner.setVisibility(View.VISIBLE);
            } else {
                upcomingBanner.setVisibility(View.GONE);
            }
        }
        try {
            Boolean activeFlag = d.getReporterPaymentBannerActive();
            String profileMsg = d.getUpcomingPaymentMessage();
            boolean active;
            if (activeFlag != null) {
                active = Boolean.TRUE.equals(activeFlag);
            } else if (profileMsg != null) {
                active = !TextUtils.isEmpty(profileMsg.trim());
            } else {
                active = ReporterPaymentAppConfig.bannerActive(this);
            }
            String saveText = "";
            if (active) {
                saveText = profileMsg != null ? profileMsg.trim() : "";
                if (TextUtils.isEmpty(saveText)) {
                    saveText = ReporterPaymentAppConfig.bannerText(this);
                    if (saveText == null) saveText = "";
                }
            }
            Boolean histActive = d.getReporterPaymentHistoryNoticeActive();
            String histText = d.getReporterPaymentHistoryNoticeText();
            ReporterPaymentAppConfig.saveFromServer(
                    this,
                    d.getReporterArticlesFromDate(),
                    active,
                    saveText,
                    histActive,
                    histText != null ? histText : "");
        } catch (Exception ignored) {
        }

        String email = d.getEmail() != null ? d.getEmail().trim() : "";
        // Never wipe a known login email when soft-refresh payload omits it.
        if (email.contains("@")) {
            currentUserEmail = email;
        } else if (currentUserEmail == null || !currentUserEmail.contains("@")) {
            try {
                if (prefShare != null && prefShare.getEmailId() != null
                        && prefShare.getEmailId().contains("@")) {
                    currentUserEmail = prefShare.getEmailId().trim();
                    d.setEmail(currentUserEmail);
                    email = currentUserEmail;
                }
            } catch (Exception ignored) {
            }
        } else {
            d.setEmail(currentUserEmail);
            email = currentUserEmail;
        }
        if (user_login_number != null && email != null && email.contains("@")) {
            user_login_number.setText(email);
            user_login_number.setTextColor(
                    ContextCompat.getColor(this, R.color.profile_way2_guest_sub));
            user_login_number.setVisibility(View.VISIBLE);
        }

        if (d.getMobileNumber() != null
                && !d.getMobileNumber().equals("")
                && !d.getMobileNumber().equals("null")) {
            SharedPrefsData.putString(getApplicationContext(), Constants.Selected_Phone_Number,
                    "" + d.getMobileNumber(), Constants.PREF_NAME);
        }

        boolean isReporter = Boolean.TRUE.equals(d.getIsReporter());
        // Prefer immediate write so any concurrent open sees verified without apply() race.
        SharedPrefsData.putBoolImmediate(getApplicationContext(), Constants.is_reporter,
                isReporter, Constants.PREF_NAME);
        applyReporterChrome(isReporter);
        // max(API, prefs) so soft-refresh never resets bar to 0% when login identity exists.
        int achievedPct = ProfileAchievementHelper.resolveAchievedPctWithPrefs(this, d);
        bindApprovalProgress(String.valueOf(achievedPct));
        try {
            SharedPrefsData.putString(getApplicationContext(), Constants.PROFILE_ACHIEVED_PCT,
                    String.valueOf(achievedPct), Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
        setTextSafe((TextView) findViewById(R.id.complete_profile_label), resolveCompleteProfileLabel());
        bindLocationRowText();

        // Persist canonical user_id before wallet prefetch (cache paint used to skip this).
        if (d.getUserId() != null && d.getUserId() > 0) {
            try {
                SharedPrefsData.putLong(getApplicationContext(), Constants.USER_ID,
                        d.getUserId(), Constants.PREF_NAME);
            } catch (Exception ignored) {
            }
        }

        // Warm wallet page-0 while reporter is looking at profile (network free path).
        if (isReporter) {
            try {
                WalletHistoryCache.saveTotalPointsDisplay(this, "\u20B9" + totalPts);
            } catch (Exception ignored) {
            }
            try {
                final android.view.View decor = getWindow() != null ? getWindow().getDecorView() : null;
                if (decor != null) {
                    decor.post(new Runnable() {
                        @Override
                        public void run() {
                            WalletHistoryPrefetcher.prefetchIfPossible(getApplicationContext());
                        }
                    });
                } else {
                    WalletHistoryPrefetcher.prefetchIfPossible(getApplicationContext());
                }
            } catch (Exception ignored) {
                try {
                    WalletHistoryPrefetcher.prefetchIfPossible(getApplicationContext());
                } catch (Exception ignored2) {
                }
            }
        }

        if (!fromCache) {
            AvailablePoints = availablePts;
            RedeemValue = d.getRedeemValue() != null ? d.getRedeemValue() : 0L;
            refer_earn = d.getRefLink();
            SharedPrefsData.putString(getApplicationContext(), Constants.AVAILABLEPOINTS,
                    "" + availablePts, Constants.PREF_NAME);
            SharedPrefsData.putString(getApplicationContext(), Constants.REDEEMPOINTS,
                    "" + RedeemValue, Constants.PREF_NAME);
            if (d.getPanStatus() != null) {
                SharedPrefsData.putBool(getApplicationContext(), Constants.PAN_CARD_STATUS,
                        d.getPanStatus(), Constants.PREF_NAME);
            }
            if (androidProgressBar != null && RedeemValue > 0) {
                androidProgressBar.setMax((int) RedeemValue);
                androidProgressBar.setProgress((int) Math.min(AvailablePoints, RedeemValue));
            }
        }

        if (payment_layout != null) payment_layout.setVisibility(View.GONE);
        if (reportLayout != null) reportLayout.setVisibility(View.GONE);

        try {
            if (d.getProfilePic() != null && !d.getProfilePic().isEmpty()) {
                AvatarImageHelper.loadProfilePicasso(img_upload_photo, d.getProfilePic());
                bindEmptyProfilePhotoAffordance(false);
                try {
                    SharedPrefsData.putString(getApplicationContext(), Constants.PROFILE_PIC_URL,
                            d.getProfilePic(), Constants.PREF_NAME);
                } catch (Exception ignored) {
                }
            } else {
                AvatarImageHelper.setProfileEmpty(img_upload_photo);
                bindEmptyProfilePhotoAffordance(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Persist identity for next open (instant name/email/avatar).
        try {
            if (d.getUserName() != null && !d.getUserName().trim().isEmpty()) {
                SharedPrefsData.putString(getApplicationContext(), Constants.USER_NAME,
                        d.getUserName().trim(), Constants.PREF_NAME);
            }
            if (email != null && !email.isEmpty()) {
                SharedPrefsData.putString(getApplicationContext(), Constants.Email, email, Constants.PREF_NAME);
                if (prefShare != null) {
                    prefShare.setEmailID(email);
                }
            }
        } catch (Exception ignored) {
        }

        if (login_neomorph_layout != null) {
            if (prefShare != null && prefShare.getEmailId() != null
                    && !prefShare.getEmailId().equals("")) {
                login_neomorph_layout.setVisibility(View.GONE);
            }
        }
        applyLoginGatedHubRows();
        bindReporterDesk();
        bindReporterCreateFab();
    }

    private void setTextSafe(TextView tv, String text) {
        if (tv != null && text != null) tv.setText(text);
    }

    private static String formatCompactCount(long n) {
        if (n >= 1_000_000L) {
            return String.format(java.util.Locale.US, "%.1fM", n / 1_000_000d);
        }
        if (n >= 1000L) {
            double k = n / 1000d;
            if (Math.abs(k - Math.rint(k)) < 0.05) {
                return String.format(java.util.Locale.US, "%.0fK", k);
            }
            return String.format(java.util.Locale.US, "%.1fK", k);
        }
        return String.valueOf(n);
    }

    private void bindApprovalProgress(String progressRaw) {
        int pct = 0;
        Integer parsed = ProfileAchievementHelper.parsePct(progressRaw);
        if (parsed != null) {
            pct = parsed;
        }
        pct = Math.max(0, Math.min(100, pct));
        String progressLabel = ProfileHubLabels.profileCompleted(this, pct);
        TextView label = findViewById(R.id.reporter_progress_label);
        if (label != null) {
            label.setText(progressLabel);
        }
        View fill = findViewById(R.id.reporter_progress_fill);
        if (fill != null && fill.getParent() instanceof View) {
            final int finalPct = pct;
            View parent = (View) fill.getParent();
            parent.post(() -> {
                int w = parent.getWidth();
                if (w <= 0) return;
                ViewGroup.LayoutParams lp = fill.getLayoutParams();
                lp.width = Math.max(0, (int) (w * (finalPct / 100f)));
                fill.setLayoutParams(lp);
            });
        }
        try {
            if (progressViewApproval != null) {
                progressViewApproval.setLabelText(progressLabel);
                progressViewApproval.setProgress(pct);
            }
        } catch (Exception ignored) {
        }
    }

    /** Last-known achieved % from prefs so the bar never flashes 0% while network loads. */
    private void paintAchievedFromLocalPrefs() {
        try {
            if (!SharedPrefsData.getBool(this, Constants.is_login, Constants.PREF_NAME)) {
                return;
            }
            int local = ProfileAchievementHelper.computePctFromPrefs(this);
            String pct = SharedPrefsData.getString(
                    this, Constants.PROFILE_ACHIEVED_PCT, Constants.PREF_NAME);
            Integer stored = ProfileAchievementHelper.parsePct(pct);
            int best = local;
            if (stored != null) {
                best = Math.max(stored, local);
            }
            if (best > 0) {
                bindApprovalProgress(String.valueOf(best));
            }
            setTextSafe((TextView) findViewById(R.id.complete_profile_label),
                    resolveCompleteProfileLabel());
        } catch (Exception ignored) {
        }
    }

    private void applyReporterChrome(boolean isReporter) {
        // Never show reporter chrome while logged out (stale prefs / cache).
        if (!SharedPrefsData.getBool(this, Constants.is_login, Constants.PREF_NAME)) {
            isReporter = false;
        }
        showingReporterUi = isReporter;

        View star = findViewById(R.id.reporter_star_badge);
        View pill = findViewById(R.id.reporter_role_pill);
        View verified = findViewById(R.id.layout_profile_verified);
        View hero = findViewById(R.id.profile_hero_brand);
        View guestCard = findViewById(R.id.profile_guest_card);
        View shortcuts = findViewById(R.id.layout_reporter_shortcuts);
        View statusVideos = findViewById(R.id.layout_status_videos);
        View settingsRow = findViewById(R.id.layout_profile_settings_row);
        View contact = findViewById(R.id.layout_profile_contact);
        View menuLang = findViewById(R.id.layout_menu_change_language);
        TextView statusVideosBadge = findViewById(R.id.profile_status_videos_new_badge);

        int rep = isReporter ? View.VISIBLE : View.GONE;

        if (star != null) star.setVisibility(rep);
        if (pill != null) pill.setVisibility(rep);
        if (verified != null) verified.setVisibility(rep);
        if (progressMainLayout != null) progressMainLayout.setVisibility(rep);
        if (shortcuts != null) shortcuts.setVisibility(rep);
        if (menuLang != null) menuLang.setVisibility(rep);

        // Categories strip: guest / user / reporter — visibility owned by
        // setupProfileHubExtras / loadProfileCategories (not reporter chrome).
        // Status Videos is for everyone (guest / logged-in / reporter); admin flag only.
        if (statusVideos != null) {
            statusVideos.setVisibility(
                    ProfileHubConfigHelper.isStatusVideosEnabled(this)
                            ? View.VISIBLE : View.GONE);
        }
        View becomeReporter = findViewById(R.id.layout_become_a_reporter);
        if (becomeReporter != null) {
            // Guests + normal users always see the row; hide only for reporters.
            becomeReporter.setVisibility(isReporter ? View.GONE : View.VISIBLE);
        }
        if (settingsRow != null) {
            if (isReporter) {
                settingsRow.setVisibility(View.GONE);
            } else if (ProfileHubConfigHelper.isSettingsRowVisible(this)) {
                settingsRow.setVisibility(View.VISIBLE);
            }
        }
        // Contact Us — visible for everyone including reporters (WhatsApp / Mail chooser).
        if (contact != null) {
            contact.setVisibility(View.VISIBLE);
        }
        View layoutBirthdayChrome = findViewById(R.id.layout_birthday_special);
        if (layoutBirthdayChrome != null) {
            layoutBirthdayChrome.setVisibility(
                    ProfileHubConfigHelper.isBirthdaySpecialEnabled(this)
                            ? View.VISIBLE : View.GONE);
        }
        if (layoutAppShare != null) {
            layoutAppShare.setVisibility(View.VISIBLE);
        }
        if (hero != null) hero.setVisibility(isReporter ? View.GONE : View.VISIBLE);
        if (guestCard != null) {
            ViewGroup.MarginLayoutParams lp =
                    (ViewGroup.MarginLayoutParams) guestCard.getLayoutParams();
            if (lp != null) {
                float dens = getResources().getDisplayMetrics().density;
                lp.topMargin = Math.round((isReporter ? 48f : 108f) * dens);
                guestCard.setLayoutParams(lp);
            }
            if (isReporter) {
                guestCard.setBackgroundResource(android.R.color.transparent);
                guestCard.setElevation(0f);
                guestCard.setTranslationZ(0f);
            } else {
                guestCard.setBackgroundResource(R.drawable.bg_profile_guest_card);
                guestCard.setElevation(10f);
                guestCard.setTranslationZ(10f);
            }
        }

        if (reportLayout != null) reportLayout.setVisibility(View.GONE);
        if (payment_layout != null) payment_layout.setVisibility(View.GONE);

        // Guests / non-approved must never keep reporter chrome from stale memory.
        if (!SharedPrefsData.getBool(this, Constants.is_login, Constants.PREF_NAME)) {
            showingReporterUi = false;
        }

        bindReporterVerifiedLabel();
        bindReporterShortcutLabels();
        bindReporterDesk();
        bindReporterCreateFab();
        bindStatusVideosNewBadge();
        bindBecomeReporterNewBadge();
        bindBirthdaySpecialNewBadge();
    }

    /** Same reporter login screen that used to open from home center +. */
    private void openReporterLogin() {
        boolean loggedIn = SharedPrefsData.getBool(
                newProfileDetails.this, Constants.is_login, Constants.PREF_NAME);
        if (loggedIn) {
            return;
        }
        Intent intent = new Intent(newProfileDetails.this, LoginActivityDetails.class);
        intent.putExtra(LoginActivityDetails.EXTRA_RETURN_TO_PROFILE, true);
        startActivity(intent);
    }

    /**
     * Same News|Daily Updates|Special|Profile footer as HomePage (Panchangam tab product-hidden).
     * Profile content scrolls above; other tabs return to HomePage with the selected mode.
     */
    private void setupProfileFooterNav() {
        profileBottomNav = findViewById(R.id.profile_nav_view);
        if (profileBottomNav == null) {
            return;
        }
        try {
            FooterStyleHelper.apply(this, profileBottomNav);
        } catch (Exception ignored) {
        }
        try {
            android.view.Menu menu = profileBottomNav.getMenu();
            if (menu != null) {
                setFooterMenuVisible(menu.findItem(R.id.navigation_home),
                        ProfileHubConfigHelper.isFooterShowReload(this));
                setFooterMenuVisible(menu.findItem(R.id.navigation_catagiries),
                        ProfileHubConfigHelper.isFooterShowSpecial(this));
                setFooterMenuVisible(menu.findItem(R.id.navigation_stricker),
                        ProfileHubConfigHelper.isFooterShowCalendar(this));
                setFooterMenuVisible(menu.findItem(R.id.navigation_status),
                        ProfileHubConfigHelper.isFooterShowVideos(this));
                setFooterMenuVisible(menu.findItem(R.id.navigation_profile),
                        ProfileHubConfigHelper.isFooterShowProfile(this));
            }
        } catch (Exception ignored) {
        }
        rebalanceProfileFooterTabs();
        try {
            profileBottomNav.setSelectedItemId(R.id.navigation_profile);
        } catch (Exception ignored) {
        }
        applyProfileFooterTabLabels();
        profileBottomNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_profile) {
                return true;
            }
            navigateHomeWithFooterTab(id);
            return true;
        });
        profileBottomNav.setOnNavigationItemReselectedListener(item -> {
            if (item.getItemId() == R.id.navigation_profile) {
                View scroll = findViewById(R.id.profile_scroll);
                if (scroll instanceof android.widget.ScrollView) {
                    ((android.widget.ScrollView) scroll).smoothScrollTo(0, 0);
                }
            }
        });
    }

    /** Equal-weight visible footer slots (Panchangam stays GONE / weight 0). */
    private void rebalanceProfileFooterTabs() {
        if (profileBottomNav == null) {
            return;
        }
        try {
            View menuChild = profileBottomNav.getChildAt(0);
            if (!(menuChild instanceof BottomNavigationMenuView)) {
                return;
            }
            BottomNavigationMenuView menuView = (BottomNavigationMenuView) menuChild;
            for (int i = 0; i < menuView.getChildCount(); i++) {
                View child = menuView.getChildAt(i);
                if (!(child instanceof BottomNavigationItemView)) {
                    continue;
                }
                BottomNavigationItemView itemView = (BottomNavigationItemView) child;
                MenuItem data = itemView.getItemData();
                if (data == null) {
                    continue;
                }
                boolean wantVisible = data.isVisible();
                child.setVisibility(wantVisible ? View.VISIBLE : View.GONE);
                ViewGroup.LayoutParams lp = child.getLayoutParams();
                if (lp instanceof LinearLayout.LayoutParams) {
                    LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) lp;
                    llp.width = 0;
                    llp.weight = wantVisible ? 1f : 0f;
                    child.setLayoutParams(llp);
                }
            }
            menuView.requestLayout();
            profileBottomNav.requestLayout();
        } catch (Exception ignored) {
        }
    }

    /**
     * Match HomePage footer language: translator pack + LanguageID helpers
     * (News / Daily Updates / Special / Profile; Panchangam tab product-hidden).
     * Menu XML titles stay English until this runs — without it Profile looks English while Home is Telugu.
     */
    private void applyProfileFooterTabLabels() {
        LanguageModelResponse.Data d =
                languageModelResponse != null ? languageModelResponse.getData() : null;
        FooterTabLabels.apply(this, profileBottomNav, d);
    }

    private void setFooterMenuVisible(MenuItem item, boolean visible) {
        if (item != null) {
            item.setVisible(visible);
            item.setEnabled(visible);
        }
    }

    private void navigateHomeWithFooterTab(int footerTabId) {
        Intent intent = new Intent(newProfileDetails.this, HomePage.class);
        // Bring Home forward; keep this Profile instance warm under it so the next
        // Profile tap is REORDER_TO_FRONT (instant) instead of cold inflate.
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(HomePage.EXTRA_FOOTER_TAB, footerTabId);
        if (getApplicationContext() != null
                && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
        // Do not finish() — warm reuse for next Profile open.
    }

    private void setupReporterDesk() {
        // No second login CTA — guest uses the top Login → pill only.
        View loginCta = findViewById(R.id.reporter_desk_login_cta);
        if (loginCta != null) {
            loginCta.setOnClickListener(null);
            loginCta.setVisibility(View.GONE);
        }
        View article = findViewById(R.id.reporter_desk_post_article);
        if (article != null) {
            article.setOnClickListener(v -> openReporterPostArticle());
        }
        View video = findViewById(R.id.reporter_desk_post_video);
        if (video != null) {
            video.setOnClickListener(v -> openReporterPostVideo());
        }
        View full = findViewById(R.id.reporter_desk_post_full);
        if (full != null) {
            full.setOnClickListener(v -> openReporterPostFull());
        }
        bindReporterDesk();
    }

    /**
     * Reporter desk: show pending/rejected only after real submission.
     * Mere OTP login must not show "awaiting reporter approval".
     * <p>
     * Stable UX: paint last-known prefs state immediately; soft-refresh
     * {@code reporter_applicant_get} in the background and update only when
     * the resolved state actually changes (no GONE→VISIBLE blink).
     */
    private void bindReporterDesk() {
        View desk = findViewById(R.id.layout_reporter_desk);
        if (desk == null) {
            return;
        }

        boolean loggedIn = SharedPrefsData.getBool(this, Constants.is_login, Constants.PREF_NAME);
        boolean isReporter = SharedPrefsData.getBool(this, Constants.is_reporter, Constants.PREF_NAME)
                || showingReporterUi;

        TextView title = findViewById(R.id.reporter_desk_title);
        TextView status = findViewById(R.id.reporter_desk_status);
        View loginCta = findViewById(R.id.reporter_desk_login_cta);
        View postActions = findViewById(R.id.reporter_desk_post_actions);

        // Never show a second login — top Guest / Login → is the only entry.
        if (loginCta != null) {
            loginCta.setVisibility(View.GONE);
        }
        // Create actions live on the FAB, not the desk card.
        if (postActions != null) {
            postActions.setVisibility(View.GONE);
        }

        if (!loggedIn) {
            applyReporterDeskState(Constants.REPORTER_DESK_HIDDEN, desk, status, title, true);
            return;
        }

        // Approved: FAB owns create — hide desk card entirely.
        if (isReporter) {
            applyReporterDeskState(Constants.REPORTER_DESK_HIDDEN, desk, status, title, true);
            return;
        }

        // Instant paint from last known applicant state (no blank / GONE frame).
        int cached = SharedPrefsData.getInt(
                this, Constants.REPORTER_DESK_UI_STATE, Constants.PREF_NAME);
        if (cached != Constants.REPORTER_DESK_PENDING
                && cached != Constants.REPORTER_DESK_REJECTED) {
            cached = Constants.REPORTER_DESK_HIDDEN;
        }
        applyReporterDeskState(cached, desk, status, title, false);

        // Background soft-refresh; UI changes only if status actually changed.
        refreshReporterApplicantDesk(desk, status, title);
    }

    /**
     * Apply desk visibility + status text only when {@code state} differs from
     * what is already on screen (avoids layout tear-down flicker).
     *
     * @param persist when true, write state to prefs (logout / approved / API result)
     */
    private void applyReporterDeskState(int state, View desk, @Nullable TextView statusView,
            @Nullable TextView titleView, boolean persist) {
        if (desk == null) {
            return;
        }
        if (state != Constants.REPORTER_DESK_PENDING
                && state != Constants.REPORTER_DESK_REJECTED) {
            state = Constants.REPORTER_DESK_HIDDEN;
        }

        if (persist) {
            try {
                SharedPrefsData.putInt(getApplicationContext(), Constants.REPORTER_DESK_UI_STATE,
                        state, Constants.PREF_NAME);
            } catch (Exception ignored) {
            }
        }

        // Idempotent: same state already painted → leave the view alone.
        if (appliedReporterDeskState == state) {
            int wantVis = state == Constants.REPORTER_DESK_HIDDEN ? View.GONE : View.VISIBLE;
            if (desk.getVisibility() == wantVis) {
                return;
            }
        }

        if (titleView != null && state != Constants.REPORTER_DESK_HIDDEN) {
            LanguageModelResponse.Data d =
                    languageModelResponse != null ? languageModelResponse.getData() : null;
            String r = d != null ? d.getReporter() : null;
            titleView.setText(notEmptyTranslator(r) ? r.trim()
                    : getString(R.string.profile_reporter_desk_title));
        }

        if (state == Constants.REPORTER_DESK_PENDING) {
            if (statusView != null) {
                statusView.setText(R.string.profile_reporter_desk_pending);
            }
            desk.setVisibility(View.VISIBLE);
        } else if (state == Constants.REPORTER_DESK_REJECTED) {
            if (statusView != null) {
                statusView.setText(R.string.profile_reporter_desk_rejected);
            }
            desk.setVisibility(View.VISIBLE);
        } else {
            desk.setVisibility(View.GONE);
        }
        appliedReporterDeskState = state;
    }

    /** Fetch Become-a-Reporter applicant status for Profile desk card (soft refresh). */
    private void refreshReporterApplicantDesk(View desk, @Nullable TextView statusView,
            @Nullable TextView titleView) {
        if (reporterApplicantRefreshInFlight) {
            return;
        }
        try {
            reporterApplicantRefreshInFlight = true;
            APIService api = RestApiClient.getClient().create(APIService.class);
            JsonObject body = new JsonObject();
            body.addProperty("draft_id",
                    SharedPrefsData.getInt(this, Constants.DraftID, Constants.PREF_NAME));
            body.addProperty("source", "become_a_reporter");
            api.reporter_applicant_get("v3", body).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    reporterApplicantRefreshInFlight = false;
                    try {
                        if (isFinishing() || isDestroyed()) return;
                        if (!response.isSuccessful() || response.body() == null) return;
                        JsonObject root = response.body();
                        if (!root.has("success") || !root.get("success").getAsBoolean()) return;
                        if (!root.has("data") || !root.get("data").isJsonObject()) return;
                        JsonObject data = root.getAsJsonObject("data");
                        if (data.has("already_reporter")
                                && data.get("already_reporter").getAsBoolean()) {
                            applyReporterDeskState(Constants.REPORTER_DESK_HIDDEN,
                                    desk, statusView, titleView, true);
                            return;
                        }
                        if (!data.has("applicant") || data.get("applicant").isJsonNull()) {
                            applyReporterDeskState(Constants.REPORTER_DESK_HIDDEN,
                                    desk, statusView, titleView, true);
                            return;
                        }
                        JsonObject a = data.getAsJsonObject("applicant");
                        boolean showAwaiting = a.has("show_awaiting_approval")
                                && a.get("show_awaiting_approval").getAsBoolean();
                        boolean rejectVisible = a.has("reject_ui_visible")
                                && a.get("reject_ui_visible").getAsBoolean();
                        String st = a.has("status") ? a.get("status").getAsString() : "";
                        int next = Constants.REPORTER_DESK_HIDDEN;
                        if (rejectVisible || "rejected".equals(st)) {
                            next = rejectVisible
                                    ? Constants.REPORTER_DESK_REJECTED
                                    : Constants.REPORTER_DESK_HIDDEN;
                        } else if (showAwaiting) {
                            next = Constants.REPORTER_DESK_PENDING;
                        }
                        applyReporterDeskState(next, desk, statusView, titleView, true);
                    } catch (Exception ignored) {
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    reporterApplicantRefreshInFlight = false;
                    /* keep last painted / cached desk state on network error */
                }
            });
        } catch (Exception ignored) {
            reporterApplicantRefreshInFlight = false;
        }
    }

    /**
     * Wire Profile reporter + FAB once (Twitter-style end stack, brand orange).
     */
    private void setupReporterCreateFab() {
        profileReporterFabRoot = findViewById(R.id.profile_reporter_fab_root);
        profileReporterFabScrim = findViewById(R.id.profile_reporter_fab_scrim);
        profileReporterFabMenu = findViewById(R.id.profile_reporter_fab_menu);
        profileReporterFabMain = findViewById(R.id.profile_reporter_fab_main);
        profileFabActionArticle = findViewById(R.id.profile_fab_action_article);
        profileFabActionVideo = findViewById(R.id.profile_fab_action_video);
        profileFabActionFull = findViewById(R.id.profile_fab_action_full);

        if (profileReporterFabMain != null) {
            profileReporterFabMain.setOnClickListener(v -> {
                if (profileReporterFabExpanded) {
                    collapseReporterCreateFab(true);
                } else {
                    expandReporterCreateFab();
                }
            });
        }
        if (profileReporterFabScrim != null) {
            profileReporterFabScrim.setOnClickListener(v -> collapseReporterCreateFab(true));
        }
        View miniArticle = findViewById(R.id.profile_fab_mini_article);
        View miniVideo = findViewById(R.id.profile_fab_mini_video);
        View miniFull = findViewById(R.id.profile_fab_mini_full);
        View.OnClickListener articleClick = v -> {
            collapseReporterCreateFab(false);
            openReporterPostArticle();
        };
        View.OnClickListener videoClick = v -> {
            collapseReporterCreateFab(false);
            openReporterPostVideo();
        };
        View.OnClickListener fullClick = v -> {
            collapseReporterCreateFab(false);
            openReporterPostFull();
        };
        if (profileFabActionArticle != null) profileFabActionArticle.setOnClickListener(articleClick);
        if (miniArticle != null) miniArticle.setOnClickListener(articleClick);
        if (profileFabActionVideo != null) profileFabActionVideo.setOnClickListener(videoClick);
        if (miniVideo != null) miniVideo.setOnClickListener(videoClick);
        if (profileFabActionFull != null) profileFabActionFull.setOnClickListener(fullClick);
        if (miniFull != null) miniFull.setOnClickListener(fullClick);

        bindReporterCreateFab();
    }

    /**
     * Show + only for logged-in approved reporters. Mini actions honor admin
     * {@link Constants#ENABLE_ARTICLE_POST} / {@link Constants#ENABLE_VIDEO_POST} /
     * {@link Constants#ENABLE_FULL_POST} (same prefs as old home FAB satellites).
     */
    private void bindReporterCreateFab() {
        boolean loggedIn = SharedPrefsData.getBool(this, Constants.is_login, Constants.PREF_NAME);
        boolean isReporter = SharedPrefsData.getBool(this, Constants.is_reporter, Constants.PREF_NAME)
                || showingReporterUi;
        boolean showFab = loggedIn && isReporter;

        if (profileReporterFabRoot == null) {
            profileReporterFabRoot = findViewById(R.id.profile_reporter_fab_root);
        }
        if (profileReporterFabRoot == null) {
            return;
        }

        if (!showFab) {
            collapseReporterCreateFab(false);
            profileReporterFabRoot.setVisibility(View.GONE);
            return;
        }

        profileReporterFabRoot.setVisibility(View.VISIBLE);
        applyReporterFabPostTypeVisibility();
        bindReporterFabLabels();

        // If every admin post type is off, hide the whole FAB.
        if (!hasAnyReporterPostTypeEnabled()) {
            collapseReporterCreateFab(false);
            profileReporterFabRoot.setVisibility(View.GONE);
        }
    }

    private boolean hasAnyReporterPostTypeEnabled() {
        try {
            SharedPreferences sp = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
            return sp.getBoolean(Constants.ENABLE_ARTICLE_POST, true)
                    || sp.getBoolean(Constants.ENABLE_VIDEO_POST, true)
                    || sp.getBoolean(Constants.ENABLE_FULL_POST, true);
        } catch (Exception e) {
            return true;
        }
    }

    private void applyReporterFabPostTypeVisibility() {
        try {
            SharedPreferences sp = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
            if (profileFabActionArticle == null) {
                profileFabActionArticle = findViewById(R.id.profile_fab_action_article);
            }
            if (profileFabActionVideo == null) {
                profileFabActionVideo = findViewById(R.id.profile_fab_action_video);
            }
            if (profileFabActionFull == null) {
                profileFabActionFull = findViewById(R.id.profile_fab_action_full);
            }
            if (profileFabActionArticle != null) {
                profileFabActionArticle.setVisibility(
                        sp.getBoolean(Constants.ENABLE_ARTICLE_POST, true)
                                ? View.VISIBLE : View.GONE);
            }
            if (profileFabActionVideo != null) {
                profileFabActionVideo.setVisibility(
                        sp.getBoolean(Constants.ENABLE_VIDEO_POST, true)
                                ? View.VISIBLE : View.GONE);
            }
            if (profileFabActionFull != null) {
                profileFabActionFull.setVisibility(
                        sp.getBoolean(Constants.ENABLE_FULL_POST, true)
                                ? View.VISIBLE : View.GONE);
            }
        } catch (Exception ignored) {
        }
    }

    private void bindReporterFabLabels() {
        LanguageModelResponse.Data d =
                languageModelResponse != null ? languageModelResponse.getData() : null;
        TextView article = findViewById(R.id.profile_fab_label_article);
        TextView video = findViewById(R.id.profile_fab_label_video);
        TextView full = findViewById(R.id.profile_fab_label_full);
        if (article != null) {
            article.setText(packString(
                    d != null ? d.getArticle() : null, ProfileHubLabels.fabArticle(this)));
        }
        if (video != null) {
            video.setText(packString(
                    d != null ? d.getVideoPost() : null, ProfileHubLabels.fabVideoPost(this)));
        }
        if (full != null) {
            full.setText(packString(
                    d != null ? d.getFullPost() : null, ProfileHubLabels.fabFullPost(this)));
        }
    }

    private void expandReporterCreateFab() {
        if (!canOpenReporterPost()) {
            return;
        }
        applyReporterFabPostTypeVisibility();
        if (!hasAnyReporterPostTypeEnabled()) {
            return;
        }
        profileReporterFabExpanded = true;
        if (profileReporterFabMenu != null) {
            profileReporterFabMenu.setVisibility(View.VISIBLE);
        }
        if (profileReporterFabScrim != null) {
            profileReporterFabScrim.setVisibility(View.VISIBLE);
        }
        if (profileReporterFabMain != null) {
            profileReporterFabMain.animate().rotation(45f).setDuration(180).start();
        }
    }

    private void collapseReporterCreateFab(boolean animate) {
        profileReporterFabExpanded = false;
        if (profileReporterFabMenu != null) {
            profileReporterFabMenu.setVisibility(View.GONE);
        }
        if (profileReporterFabScrim != null) {
            profileReporterFabScrim.setVisibility(View.GONE);
        }
        if (profileReporterFabMain != null) {
            if (animate) {
                profileReporterFabMain.animate().rotation(0f).setDuration(180).start();
            } else {
                profileReporterFabMain.setRotation(0f);
            }
        }
    }

    private void openReporterPostArticle() {
        if (!canOpenReporterPost()) return;
        try {
            SharedPreferences sp = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
            if (!sp.getBoolean(Constants.ENABLE_ARTICLE_POST, true)) return;
        } catch (Exception ignored) {
        }
        Intent intent = new Intent(newProfileDetails.this, PostArticles.class);
        intent.putExtra("POST_VIDEO", "0");
        startActivity(intent);
    }

    /** Become-a-Reporter entry: require login; BAR page shows form or not-hiring. */
    private void openBecomeAReporterFlow() {
        markBecomeReporterBadgeSeen();
        boolean loggedIn = SharedPrefsData.getBool(this, Constants.is_login, Constants.PREF_NAME);
        if (!loggedIn) {
            SharedPrefsData.putBool(this, Constants.LOGIN_SOURCE_BECOME_REPORTER, true, Constants.PREF_NAME);
            Intent intent = new Intent(newProfileDetails.this, LoginActivityDetails.class);
            intent.putExtra(LoginActivityDetails.EXTRA_RETURN_TO_PROFILE, true);
            intent.putExtra(LoginActivityDetails.EXTRA_OPEN_BECOME_A_REPORTER, true);
            startActivity(intent);
            return;
        }
        // Always open BAR page — not-hiring dialog is shown there only (never on Profile).
        startActivity(new Intent(newProfileDetails.this, BecomeAReporterActivity.class));
    }

    /** Centered dialog with admin/splash localized not-hiring copy. */
    private void showNotHiringReporterDialog() {
        try {
            String msg = ProfileHubConfigHelper.getNotHiringMessage(this);
            new AlertDialog.Builder(this)
                    .setMessage(msg)
                    .setPositiveButton(android.R.string.ok, null)
                    .setCancelable(true)
                    .show();
        } catch (Exception e) {
            Toast.makeText(this,
                    ProfileHubConfigHelper.getNotHiringMessage(this),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void openReporterPostVideo() {
        if (!canOpenReporterPost()) return;
        try {
            SharedPreferences sp = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
            if (!sp.getBoolean(Constants.ENABLE_VIDEO_POST, true)) return;
        } catch (Exception ignored) {
        }
        Intent intent = new Intent(newProfileDetails.this, PostVideo.class);
        intent.putExtra("POST_VIDEO", "1");
        startActivity(intent);
    }

    private void openReporterPostFull() {
        if (!canOpenReporterPost()) return;
        try {
            SharedPreferences sp = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
            if (!sp.getBoolean(Constants.ENABLE_FULL_POST, true)) return;
        } catch (Exception ignored) {
        }
        startActivity(new Intent(newProfileDetails.this, FullPost.class));
    }

    private boolean canOpenReporterPost() {
        boolean loggedIn = SharedPrefsData.getBool(this, Constants.is_login, Constants.PREF_NAME);
        boolean isReporter = SharedPrefsData.getBool(this, Constants.is_reporter, Constants.PREF_NAME)
                || showingReporterUi;
        if (!loggedIn) {
            openReporterLogin();
            return false;
        }
        if (!isReporter) {
            Toast.makeText(this, R.string.profile_reporter_desk_pending, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (ReporterPostGateHelper.blockComposeIfIncomplete(this)) {
            return false;
        }
        return true;
    }

    private void bindReporterVerifiedLabel() {
        TextView tv = findViewById(R.id.profile_verified_label);
        if (tv == null) return;
        int id = SharedPrefsData.getInt(this, Constants.LanguageID, Constants.PREF_NAME);
        if (id == 3) {
            tv.setText("వెరిఫైడ్");
        } else {
            tv.setText(R.string.profile_verified);
        }
    }

    private void bindReporterShortcutLabels() {
        LanguageModelResponse.Data d =
                languageModelResponse != null ? languageModelResponse.getData() : null;
        setTextSafe((TextView) findViewById(R.id.tile_articles_label),
                packString(d != null ? d.getArticles() : null, ProfileHubLabels.articles(this)));
        setTextSafe((TextView) findViewById(R.id.tile_followers_label),
                packString(d != null ? d.getFollowers() : null, ProfileHubLabels.followers(this)));
        setTextSafe((TextView) findViewById(R.id.tile_earnings_label), resolveTotalEarningsLabel());
        setTextSafe((TextView) findViewById(R.id.reporter_shortcuts_title), resolveShortcutsTitle());
        setTextSafe((TextView) findViewById(R.id.complete_profile_label), resolveCompleteProfileLabel());
        // Re-apply completion % wording for current LanguageID (EN/TE/…).
        paintAchievedFromLocalPrefs();
        setTextSafe((TextView) findViewById(R.id.profile_menu_change_language_label),
                packString(d != null ? d.getSelectLanguage() : null,
                        ProfileHubLabels.selectLanguage(this)));
        TextView role = findViewById(R.id.reporter_role_pill);
        if (role != null) {
            String r = TranslatorPackHelper.packMatchesLanguage(this) && d != null
                    ? d.getReporter() : null;
            role.setText(notEmptyTranslator(r) ? r.trim() : getString(R.string.profile_role_reporter));
        }
    }

    private String resolveArticlesLabel() {
        return ProfileHubLabels.articles(this);
    }

    private String resolveFollowersShortcutLabel() {
        return ProfileHubLabels.followers(this);
    }

    private String resolveTotalEarningsLabel() {
        if (SharedPrefsData.getInt(this, Constants.LanguageID, Constants.PREF_NAME) == 3) {
            return "మొత్తం సంపాదన";
        }
        return getString(R.string.profile_total_earnings);
    }

    private String resolveShortcutsTitle() {
        if (SharedPrefsData.getInt(this, Constants.LanguageID, Constants.PREF_NAME) == 3) {
            return "రిపోర్టర్ షార్ట్‌కట్లు";
        }
        return getString(R.string.profile_reporter_shortcuts);
    }

    /**
     * Logged-in users who already gave name/email/mobile at login see "Edit profile";
     * otherwise "Complete profile".
     */
    private String resolveCompleteProfileLabel() {
        boolean loggedIn = SharedPrefsData.getBool(this, Constants.is_login, Constants.PREF_NAME);
        if (loggedIn && hasBasicIdentityForEditCta()) {
            return resolveEditProfileLabel();
        }
        return resolveCompleteProfileOnlyLabel();
    }

    private boolean hasBasicIdentityForEditCta() {
        String name = SharedPrefsData.getString(this, Constants.USER_NAME, Constants.PREF_NAME);
        if (name == null || name.trim().isEmpty() || "guest".equalsIgnoreCase(name.trim())) {
            if (username_text != null && username_text.getText() != null) {
                name = username_text.getText().toString().trim();
            }
        }
        boolean hasName = name != null && !name.trim().isEmpty()
                && !"guest".equalsIgnoreCase(name.trim())
                && !"null".equalsIgnoreCase(name.trim());

        String email = currentUserEmail != null ? currentUserEmail.trim() : "";
        if (email.isEmpty()) {
            try {
                if (prefShare != null && prefShare.getEmailId() != null) {
                    email = prefShare.getEmailId().trim();
                }
            } catch (Exception ignored) {
            }
        }
        if (email.isEmpty()) {
            String e = SharedPrefsData.getString(this, Constants.Email, Constants.PREF_NAME);
            if (e != null) email = e.trim();
        }
        boolean hasEmail = email.contains("@");

        String mobile = SharedPrefsData.getString(
                this, Constants.Selected_Phone_Number, Constants.PREF_NAME);
        if (mobile == null) mobile = "";
        String digits = mobile.replaceAll("\\D", "");
        boolean hasMobile = digits.length() >= 10;

        // Login already collected basics — any two (or name+mobile) is enough for Edit CTA.
        int basics = (hasName ? 1 : 0) + (hasEmail ? 1 : 0) + (hasMobile ? 1 : 0);
        return basics >= 2;
    }

    private String resolveEditProfileLabel() {
        LanguageModelResponse.Data d =
                languageModelResponse != null ? languageModelResponse.getData() : null;
        if (TranslatorPackHelper.packMatchesLanguage(this)
                && d != null && notEmptyTranslator(d.getEdit()) && notEmptyTranslator(d.getProfile())) {
            return d.getProfile().trim() + " " + d.getEdit().trim();
        }
        return ProfileHubLabels.editProfile(this);
    }

    private String resolveCompleteProfileOnlyLabel() {
        if (SharedPrefsData.getInt(this, Constants.LanguageID, Constants.PREF_NAME) == 3) {
            return "ప్రొఫైల్ పూర్తి చేయండి";
        }
        return getString(R.string.profile_complete_profile);
    }

    private void setupReporterShortcutClicks() {
        View articles = findViewById(R.id.tile_articles);
        if (articles != null) articles.setOnClickListener(v -> openArticlesStatus(null));
        View followers = findViewById(R.id.tile_followers);
        if (followers != null) {
            followers.setOnClickListener(v -> openFollowersList());
        }
        View earnings = findViewById(R.id.tile_earnings);
        // Total earnings → article amount history (page-0 get_article_transactions, cached)
        if (earnings != null) earnings.setOnClickListener(v -> openWalletHistory(1));
        View menuLang = findViewById(R.id.layout_menu_change_language);
        if (menuLang != null) {
            menuLang.setOnClickListener(v ->
                    startActivity(LanguageActivity.createChangeLanguageIntent(newProfileDetails.this)));
        }
    }

    /** Followers of this reporter — list_followers API + disk cache. */
    private void openFollowersList() {
        Intent intent = new Intent(newProfileDetails.this, FolloweingMenders.class);
        intent.putExtra(FolloweingMenders.EXTRA_MODE, FolloweingMenders.MODE_FOLLOWERS);
        startActivity(intent);
    }

    private void openArticlesStatus() {
        openArticlesStatus(null);
    }

    /**
     * @param metric null/empty = my posts status tabs; likes|comments|views = engagement sort list
     */
    private void openArticlesStatus(String metric) {
        Intent intent = new Intent(newProfileDetails.this, PostArticleStatus.class);
        if (metric != null && !metric.trim().isEmpty()) {
            intent.putExtra(PostArticleStatus.EXTRA_METRIC, metric.trim().toLowerCase());
        }
        String mobile = SharedPrefsData.getString(getApplicationContext(),
                Constants.Selected_Phone_Number, Constants.PREF_NAME);
        if (mobile == null || mobile.trim().isEmpty() || "null".equalsIgnoreCase(mobile.trim())) {
            try {
                NewUserProfileResponse cached = ReporterProfileCache.load(getApplicationContext());
                if (cached != null && cached.getData() != null
                        && cached.getData().getMobileNumber() != null) {
                    mobile = cached.getData().getMobileNumber();
                }
            } catch (Exception ignored) {
            }
        }
        if (mobile != null && !mobile.trim().isEmpty() && !"null".equalsIgnoreCase(mobile.trim())) {
            SharedPrefsData.putString(getApplicationContext(), Constants.Selected_Phone_Number,
                    mobile.trim(), Constants.PREF_NAME);
            intent.putExtra(PostArticleStatus.EXTRA_MOBILE_NUMBER, mobile.trim());
        }
        startActivity(intent);
    }

    public void getWriterProfileData() {
        userProfileResponse = new ArrayList<>();
        showCachedProfileInstant();

        // Logged-out guests: never fetch draft-bound profile (it resurrects name/reporter).
        if (!SharedPrefsData.getBool(this, Constants.is_login, Constants.PREF_NAME)) {
            return;
        }
        // Soft-refresh TTL 2–5 min (3 min): skip network when cache is fresh.
        if (ReporterProfileCache.isFresh(this)) {
            return;
        }

        JsonObject otp = set_writer_profile_data();
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.get_user_profile("v3", otp);
        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                EncreptionRequest encreptionRequest = response.body();
                if (encreptionRequest == null || encreptionRequest.getData() == null) {
                    return;
                }
                String encrpt_data = decrypt(encreptionRequest.getData());
                AppHelper.showLog("Encrypted Data:" + encrpt_data);

                Gson gsonObj = new Gson();
                NewUserProfileResponse newUserProfileResponse =
                        gsonObj.fromJson(encrpt_data, NewUserProfileResponse.class);

                if (newUserProfileResponse != null
                        && Boolean.TRUE.equals(newUserProfileResponse.getSuccess())
                        && newUserProfileResponse.getData() != null) {
                    if (!SharedPrefsData.getBool(newProfileDetails.this,
                            Constants.is_login, Constants.PREF_NAME)) {
                        paintLoggedOutIdentity();
                        applyReporterChrome(false);
                        return;
                    }
                    try {
                        ReporterProfileCache.save(getApplicationContext(), encrpt_data);
                    } catch (Exception ignored) {
                    }

                    if (imageupload_progressbar != null) imageupload_progressbar.setVisibility(View.GONE);
                    if (loadingProgressbar != null) loadingProgressbar.setVisibility(View.GONE);
                    if (loadingProfileView != null) loadingProfileView.setVisibility(View.VISIBLE);
                    if (img_upload_photo != null) img_upload_photo.setVisibility(View.VISIBLE);

                    applyProfileDataToUi(newUserProfileResponse.getData(), false);
                    WalletHistoryPrefetcher.prefetchIfPossible(getApplicationContext());

                    boolean stillLoggedIn = SharedPrefsData.getBool(newProfileDetails.this,
                            Constants.is_login, Constants.PREF_NAME);
                    if (!stillLoggedIn) {
                        paintLoggedOutIdentity();
                        applyReporterChrome(false);
                    } else {
                        bindProfileLoginPill();
                        if (prefShare != null && prefShare.getEmailId() != null
                                && !prefShare.getEmailId().equals("")) {
                            if (login_neomorph_layout != null) {
                                login_neomorph_layout.setVisibility(View.GONE);
                            }
                            if (user_login_number != null) {
                                user_login_number.setVisibility(View.VISIBLE);
                                if (newUserProfileResponse.getData().getEmail() != null
                                        && !newUserProfileResponse.getData().getEmail().isEmpty()) {
                                    user_login_number.setText(
                                            newUserProfileResponse.getData().getEmail());
                                } else {
                                    user_login_number.setText(prefShare.getEmailId());
                                }
                                user_login_number.setTextColor(ContextCompat.getColor(
                                        newProfileDetails.this, R.color.profile_way2_guest_sub));
                            }
                        }
                    }
                    bindLocationRowText();
                    applyLoginGatedHubRows();
                    applyProfileHubTranslatedLabels();
                    bindReporterShortcutLabels();
                    bindReporterCreateFab();
                }
            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                // Keep cache-painted UI.
            }
        });
    }

    private JsonObject setProfileUpdateImage() {

        ProfileUpdateRequiest profileUpdateRequiest = new ProfileUpdateRequiest();

        profileUpdateRequiest.setDraftId((long) SharedPrefsData.getInt(newProfileDetails.this, Constants.DraftID, Constants.PREF_NAME));
        profileUpdateRequiest.setImageUrl("" + ImageId);

        String jsonString = new com.google.gson.Gson().toJson(profileUpdateRequiest);
        String encrpt_data = null;
        showLog("API REQUEST E" + jsonString.toString());
        encrpt_data = encrypt(jsonString);

        EncritonRequestData Encriton_Request = new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();

    }

    private boolean ProfileUpdateImage() {

        JsonObject jsonObject = setProfileUpdateImage();

        APIService apiService = RestApiClient.getClient().create(APIService.class);

        Call<EncreptionRequest> otp_generation = apiService.profileUpdateimage("v3", jsonObject);

        otp_generation.enqueue(new Callback<EncreptionRequest>() {

            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {


                EncreptionRequest encreptionRequest = response != null ? response.body() : null;
                if (encreptionRequest == null || encreptionRequest.getData() == null) {
                    if (imageupload_progressbar != null) {
                        imageupload_progressbar.setVisibility(View.GONE);
                    }
                    Toast.makeText(newProfileDetails.this,
                            R.string.profile_photo_upload_failed, Toast.LENGTH_SHORT).show();
                    return;
                }
                String encrpt_data = null;
                /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    encrpt_data = decrypt(encreptionRequest.getData());
                }*/
                try {
                    encrpt_data = decrypt(encreptionRequest.getData());
                } catch (Exception e) {
                    if (imageupload_progressbar != null) {
                        imageupload_progressbar.setVisibility(View.GONE);
                    }
                    Toast.makeText(newProfileDetails.this,
                            R.string.profile_photo_upload_failed, Toast.LENGTH_SHORT).show();
                    return;
                }

                Gson gsonObj = new Gson();
                UpdateImageResponse userProfileUpdateResponse = gsonObj.fromJson(encrpt_data, UpdateImageResponse.class);


                if (userProfileUpdateResponse != null
                        && Boolean.TRUE.equals(userProfileUpdateResponse.getSuccess())) {


                    layout_with_out_data.setVisibility(View.GONE);
                    layout_with_data.setVisibility(View.VISIBLE);

                    imageupload_progressbar.setVisibility(View.GONE);
                    img_upload_photo.setVisibility(View.VISIBLE);

                    String finalUrl = ImageId;
                    try {
                        if (userProfileUpdateResponse.getImageUrl() != null
                                && !userProfileUpdateResponse.getImageUrl().isEmpty()) {
                            finalUrl = userProfileUpdateResponse.getImageUrl();
                            ImageId = finalUrl;
                        } else if (userProfileUpdateResponse.getExtras() != null
                                && userProfileUpdateResponse.getExtras().getImageUrl() != null
                                && !userProfileUpdateResponse.getExtras().getImageUrl().isEmpty()) {
                            finalUrl = userProfileUpdateResponse.getExtras().getImageUrl();
                            ImageId = finalUrl;
                        }
                    } catch (Exception ignored) {
                    }
                    if (finalUrl != null && !finalUrl.isEmpty()) {
                        AvatarImageHelper.loadProfilePicasso(img_upload_photo, finalUrl);
                        bindEmptyProfilePhotoAffordance(false);
                        try {
                            SharedPrefsData.putString(getApplicationContext(),
                                    Constants.PROFILE_PIC_URL, finalUrl, Constants.PREF_NAME);
                        } catch (Exception ignored) {
                        }
                        try {
                            ReporterPhotoReminderHelper.clearDismissAfterUpload(getApplicationContext());
                        } catch (Exception ignored) {
                        }
                        refreshAchievedPctFromLocalIdentity();
                    }

                    Toast.makeText(newProfileDetails.this, "Profile has been updated", Toast.LENGTH_SHORT).show();
                    // Refresh cache so reopen/splash keep the new avatar.
                    try {
                        getWriterProfileData();
                    } catch (Exception ignored) {
                    }


                } else {
                    if (imageupload_progressbar != null) {
                        imageupload_progressbar.setVisibility(View.GONE);
                    }
                    Toast.makeText(newProfileDetails.this,
                            R.string.profile_photo_upload_failed, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                if (imageupload_progressbar != null) {
                    imageupload_progressbar.setVisibility(View.GONE);
                }
                String msg = t != null && t.getMessage() != null
                        ? t.getMessage()
                        : getString(R.string.profile_photo_upload_failed);
                Toast.makeText(newProfileDetails.this, msg, Toast.LENGTH_LONG).show();
            }
        });

        return CHECK_VALUE;
    }

    public void createlink(String user_name, String share) {
        Logone.e("main", "create link ");
        DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://anynews.co.in/"))
                .setDynamicLinkDomain("anynews0.page.link")
                // Open links with this app on Android
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                // Open links with com.example.ios on iOS
                //.setIosParameters(new DynamicLink.IosParameters.Builder("com.example.ios").build())
                .buildDynamicLink();
//click -- link -- google play store -- inistalled/ or not  ----
        Uri dynamicLinkUri = dynamicLink.getUri();

        Logone.e("main", "  Long refer " + dynamicLink.getUri());
        //   https://referearnpro.page.link?apn=blueappsoftware.referearnpro&link=https%3A%2F%2Fwww.blueappsoftware.com%2F
        // apn  ibi link
        // manuall link   //

        createreferlink(user_name, share);
    }

    public void createreferlink(String custid, String proid) {


        String sharelinktext = "https://anynews0.page.link/?" +
                "link=https://anynews.co.in/myrefer.php?custid=" + custid + "-" + proid +
                "&apn=" + getPackageName() +
                "&st=" + "Any news Refferal link" +
                "&sd=" + "Reward Coins 20" +
                "&si=" + "https://www.blueappsoftware.com/logo-1.png";


        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                //.setLongLink(dynamicLink.getUri())
                .setLongLink(Uri.parse(sharelinktext))  // manually
                .buildShortDynamicLink()
                .addOnCompleteListener(this, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            // Short link created
                            Uri shortLink = task.getResult().getShortLink();
                            Uri flowchartLink = task.getResult().getPreviewLink();
                            Logone.e("main ", "short link " + shortLink.toString());

                            showLog("AKKISETTY NARASIMHA" + shortLink.toString());

                            // share app dialog
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_TEXT, "https://anynews0.page.link/aQjxDF8SwuHTf8d57");
                            intent.setType("text/plain");
                            if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                                startActivity(intent);
                            }
                        } else {
                            // Error
                            // ...
                            Logone.e("main", " error " + task.getException());
                        }
                    }
                });
    }

    /**
     * Way2-style Profile hub extras: categories strip, Status Videos, location,
     * settings/feedback/contact, admin visibility + menu text size.
     * Does not remove existing profile tiles (wallet, follow, language, bookmarks…).
     */
    private void setupProfileHubExtras() {
        int menuSp = ProfileHubConfigHelper.getMenuTextSizeSp(this);

        // Hub lives outside reporter-only reportLayout so every user sees it.
        View hubRoot = findViewById(R.id.layout_profile_hub);
        if (hubRoot != null) {
            hubRoot.setVisibility(View.VISIBLE);
        }

        // Glass-tinged root background (existing content kept).
        try {
            View root = findViewById(android.R.id.content);
            if (root != null) {
                root.setBackgroundColor(ContextCompat.getColor(this, R.color.profile_hub_scrim));
            }
        } catch (Exception ignored) {
        }

        // Location tile (Way2: bold blue "District (Change)")
        View layoutLocation = findViewById(R.id.layout_profile_location);
        bindLocationRowText();
        if (layoutLocation != null) {
            layoutLocation.setOnClickListener(v -> {
                Intent intent = new Intent(newProfileDetails.this, Districs.class);
                startActivity(intent);
            });
        }
        // Also allow tapping the header district line.
        if (local_distic_name != null) {
            local_distic_name.setOnClickListener(v -> {
                Intent intent = new Intent(newProfileDetails.this, Districs.class);
                startActivity(intent);
            });
        }

        // Status Videos
        View layoutStatus = findViewById(R.id.layout_status_videos);
        TextView statusLabel = findViewById(R.id.profile_status_videos_label);
        if (statusLabel != null) {
            statusLabel.setTextSize(menuSp);
        }
        if (layoutStatus != null) {
            boolean on = ProfileHubConfigHelper.isStatusVideosEnabled(this);
            layoutStatus.setVisibility(on ? View.VISIBLE : View.GONE);
            layoutStatus.setOnClickListener(v -> {
                markStatusVideosBadgeSeen();
                startActivity(new Intent(newProfileDetails.this, StatusVideosActivity.class));
            });
        }

        // Rewards — visible when admin rewards_master is ON (splash/session prefs).
        bindProfileRewardsRow(menuSp);

        // Become a Reporter — always visible for non-reporters; hiring controls form vs message
        View layoutBecome = findViewById(R.id.layout_become_a_reporter);
        TextView becomeLabel = findViewById(R.id.profile_become_a_reporter_label);
        if (becomeLabel != null) {
            becomeLabel.setTextSize(menuSp);
            becomeLabel.setText(ProfileHubLabels.becomeAReporter(this));
        }
        if (layoutBecome != null) {
            boolean alreadyReporter = SharedPrefsData.getBool(
                    this, Constants.is_reporter, Constants.PREF_NAME);
            layoutBecome.setVisibility(alreadyReporter ? View.GONE : View.VISIBLE);
            layoutBecome.setOnClickListener(v -> openBecomeAReporterFlow());
        }

        // Settings row
        View layoutSettings = findViewById(R.id.layout_profile_settings_row);
        TextView settingsLabel = findViewById(R.id.profile_settings_row_label);
        if (settingsLabel != null) settingsLabel.setTextSize(menuSp);
        if (layoutSettings != null) {
            boolean on = ProfileHubConfigHelper.isSettingsRowVisible(this);
            layoutSettings.setVisibility(on ? View.VISIBLE : View.GONE);
            layoutSettings.setOnClickListener(v ->
                    startActivity(new Intent(newProfileDetails.this, Settings_Scrren.class)));
        }

        // Feedback → same mailto path as Settings Feedback (legacy contact-dialog option)
        View layoutFeedback = findViewById(R.id.layout_profile_feedback);
        if (layoutFeedback != null) {
            layoutFeedback.setOnClickListener(v -> openHubMailto("Feedback — AnyNews"));
        }

        // Contact Us → chooser (WhatsApp from Configuration, or Mail)
        View layoutContact = findViewById(R.id.layout_profile_contact);
        if (layoutContact != null) {
            layoutContact.setVisibility(View.VISIBLE);
            layoutContact.setOnClickListener(
                    v -> ContactUsChooser.show(newProfileDetails.this, "Contact — AnyNews"));
        }

        // Advertise on AnyNews — all users
        View layoutAdvertise = findViewById(R.id.layout_advertise_on_anynews);
        TextView advertiseLabel = findViewById(R.id.profile_advertise_label);
        TextView advertiseBadge = findViewById(R.id.profile_advertise_new_badge);
        if (advertiseLabel != null) {
            advertiseLabel.setTextSize(menuSp);
            advertiseLabel.setText(ProfileHubLabels.advertiseOnAnyNews(this));
        }
        if (advertiseBadge != null) {
            advertiseBadge.setVisibility(View.VISIBLE);
        }
        if (layoutAdvertise != null) {
            layoutAdvertise.setVisibility(View.VISIBLE);
            layoutAdvertise.setOnClickListener(
                    v ->
                            startActivity(
                                    new Intent(
                                            newProfileDetails.this,
                                            AdvertiseOnAnyNewsActivity.class)));
        }

        // Share App — Play Store chooser (restored before Advertise in hub XML)
        if (layoutAppShare != null) {
            layoutAppShare.setVisibility(View.VISIBLE);
            if (textViewShareEarnUser != null) {
                textViewShareEarnUser.setTextSize(menuSp);
                textViewShareEarnUser.setText(ProfileHubLabels.shareApp(this));
            }
            layoutAppShare.setOnClickListener(v -> {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT,
                        "Hey check out my app at: https://play.google.com/store/apps/details?id="
                                + BuildConfig.APPLICATION_ID);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, ProfileHubLabels.shareApp(this)));
            });
        }

        // Birthday Special — photo / name / location → chat (admin master toggle)
        View layoutBirthday = findViewById(R.id.layout_birthday_special);
        TextView birthdayLabel = findViewById(R.id.profile_birthday_label);
        if (birthdayLabel != null) {
            birthdayLabel.setTextSize(menuSp);
            birthdayLabel.setText(ProfileHubLabels.birthdaySpecial(this));
        }
        if (layoutBirthday != null) {
            boolean birthdayOn = ProfileHubConfigHelper.isBirthdaySpecialEnabled(this);
            layoutBirthday.setVisibility(birthdayOn ? View.VISIBLE : View.GONE);
            layoutBirthday.setOnClickListener(
                    v -> {
                        markBirthdaySpecialBadgeSeen();
                        Intent i =
                                new Intent(
                                        newProfileDetails.this,
                                        AdvertiseOnAnyNewsActivity.class);
                        i.putExtra(
                                AdvertiseOnAnyNewsActivity.EXTRA_MODE,
                                AdvertiseOnAnyNewsActivity.MODE_BIRTHDAY);
                        startActivity(i);
                    });
        }

        // Bookmarks visibility
        View layoutBookmarks = findViewById(R.id.layout_bookmarks);
        if (layoutBookmarks != null) {
            layoutBookmarks.setVisibility(
                    ProfileHubConfigHelper.isBookmarksVisible(this) ? View.VISIBLE : View.GONE);
            TextView bmLabel = findViewById(R.id.profile_bookmarks_label);
            if (bmLabel != null) bmLabel.setTextSize(menuSp);
        }

        // Legacy Share-to-Earn grid tile stays hidden; hub Share App row is the entry.
        if (profile_layout_shareToearn != null) {
            profile_layout_shareToearn.setVisibility(View.GONE);
        }

        // Categories horizontal strip (same list as SearchActivity) — all roles
        // (guest / logged-in user / reporter). Not admin-gated; always load when present.
        View categoriesBlock = findViewById(R.id.layout_profile_categories);
        RecyclerView catList = findViewById(R.id.profile_categories_list);
        TextView catLabel = findViewById(R.id.profile_categories_label);
        TextView viewAll = findViewById(R.id.profile_categories_view_all);
        if (catLabel != null) catLabel.setTextSize(Math.max(14, menuSp));
        if (viewAll != null) {
            viewAll.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(newProfileDetails.this, SearchActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Intent intent = new Intent(newProfileDetails.this, HomePage.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            });
        }
        if (categoriesBlock != null && catList != null) {
            catList.setLayoutManager(
                    new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            loadProfileCategories(catList, categoriesBlock);
        }
        bindStatusVideosNewBadge();
        bindBecomeReporterNewBadge();
        bindBirthdaySpecialNewBadge();
        bindLocationRowText();
        applyGuestProfileCardDefaults();
        applyProfileHubTranslatedLabels();
        bindReporterShortcutLabels();
        bindReporterDesk();
        // Keep Follow / Discover row hidden — Following already embeds Explore.
        if (layoutFollowMore != null) {
            layoutFollowMore.setVisibility(View.GONE);
        }
    }

    /** Profile hub Contact / Feedback — chooser (WhatsApp or Mail). */
    private void openHubMailto(String subject) {
        ContactUsChooser.show(this, subject);
    }

    /**
     * Soft-refresh rewards_master + exclude_reporters from rewards_hub so
     * Profile reflects admin toggle without requiring a full splash restart.
     * TTL 5–15 min (10 min) — not every Profile resume.
     */
    private void softRefreshRewardsFlags() {
        try {
            long last =
                    SharedPrefsData.getLong(
                            this, Constants.REWARDS_HUB_FLAGS_FETCHED_AT, Constants.PREF_NAME);
            if (last > 0L && System.currentTimeMillis() - last < 10L * 60L * 1000L) {
                return;
            }
            APIService api = RestApiClient.getClient().create(APIService.class);
            JsonObject body = new JsonObject();
            try {
                body.addProperty(
                        "draft_id",
                        SharedPrefsData.getInt(this, Constants.DraftID, Constants.PREF_NAME));
                body.addProperty(
                        "language_id",
                        SharedPrefsData.getInt(this, Constants.LanguageID, Constants.PREF_NAME));
            } catch (Exception ignored) {
            }
            api.rewards_hub("v3", body)
                    .enqueue(
                            new Callback<JsonObject>() {
                                @Override
                                public void onResponse(
                                        Call<JsonObject> call, Response<JsonObject> response) {
                                    try {
                                        JsonObject root = response.body();
                                        if (root == null) return;
                                        com.anyNews.anynews.rewards.RewardsConfigHelper.applyFromRewardsPayload(
                                                newProfileDetails.this, root);
                                        try {
                                            SharedPrefsData.putLong(
                                                    newProfileDetails.this,
                                                    Constants.REWARDS_HUB_FLAGS_FETCHED_AT,
                                                    System.currentTimeMillis(),
                                                    Constants.PREF_NAME);
                                        } catch (Exception ignored) {
                                        }
                                        Boolean master = null;
                                        Boolean exclude = null;
                                        if (root.has("enabled") && !root.get("enabled").isJsonNull()) {
                                            master = root.get("enabled").getAsBoolean();
                                        }
                                        if (root.has("exclude_reporters")
                                                && !root.get("exclude_reporters").isJsonNull()) {
                                            exclude = root.get("exclude_reporters").getAsBoolean();
                                        }
                                        if (master != null || exclude != null) {
                                            DistrictPinVisibilityConfig.saveFromServer(
                                                    newProfileDetails.this,
                                                    null,
                                                    master,
                                                    null,
                                                    exclude);
                                        }
                                        runOnUiThread(
                                                () -> {
                                                    try {
                                                        bindProfileRewardsRow(15f);
                                                        applyLoginGatedHubRows();
                                                    } catch (Exception ignored) {
                                                    }
                                                });
                                    } catch (Exception ignored) {
                                    }
                                }

                                @Override
                                public void onFailure(Call<JsonObject> call, Throwable t) {}
                            });
        } catch (Exception ignored) {
        }
    }

    /**
     * Logout only for logged-in users. Account delete removed from the app.
     */
    private void applyLoginGatedHubRows() {
        boolean loggedIn = SharedPrefsData.getBool(
                newProfileDetails.this, Constants.is_login, Constants.PREF_NAME);
        int vis = loggedIn ? View.VISIBLE : View.GONE;
        if (layoutAppLogout != null) {
            layoutAppLogout.setVisibility(vis);
        }
        // Product: never show account delete.
        if (layoutAppDelete != null) {
            layoutAppDelete.setVisibility(View.GONE);
        }
        if (account_delete != null) {
            account_delete.setVisibility(View.GONE);
        }
        View divBeforeAuth = findViewById(R.id.divider_before_delete_logout);
        if (divBeforeAuth != null) {
            divBeforeAuth.setVisibility(vis);
        }
        // Pencil: name save only for logged-in users.
        if (edit_profile_icon != null) {
            edit_profile_icon.setVisibility(vis);
        }
        // Rewards row: gated by admin rewards_master + reporter exclusion.
        bindProfileRewardsRow(15f);
        if (profile_layout_rewards != null) {
            profile_layout_rewards.setVisibility(
                    DistrictPinVisibilityConfig.rewardsVisibleForUser(this)
                            ? View.VISIBLE
                            : View.GONE);
        }
        if (profile_refer_list != null) {
            profile_refer_list.setVisibility(
                    DistrictPinVisibilityConfig.rewardsVisibleForUser(this)
                            ? View.VISIBLE
                            : View.GONE);
        }
    }

    /** Profile hub Rewards entry — must show when rewards_master ON (not for excluded reporters). */
    private void bindProfileRewardsRow(float menuSp) {
        View layoutRewards = findViewById(R.id.layout_profile_rewards);
        TextView rewardsLabel = findViewById(R.id.profile_rewards_label);
        TextView rewardsSubtitle = findViewById(R.id.profile_rewards_subtitle);
        View layoutWallet = findViewById(R.id.layout_profile_rewards_wallet);
        TextView walletLabel = findViewById(R.id.profile_rewards_wallet_label);
        TextView walletSubtitle = findViewById(R.id.profile_rewards_wallet_subtitle);
        boolean on = DistrictPinVisibilityConfig.rewardsVisibleForUser(this);
        if (rewardsLabel != null) {
            rewardsLabel.setTextSize(menuSp);
            try {
                rewardsLabel.setText(
                        com.anyNews.anynews.rewards.RewardsLabels.joinedChip(this));
            } catch (Exception ignored) {
                rewardsLabel.setText("Rewards");
            }
        }
        if (rewardsSubtitle != null) {
            try {
                String coins =
                        SharedPrefsData.getString(
                                this, "rewards_period_coins", Constants.PREF_NAME);
                if (TextUtils.isEmpty(coins)) coins = "0";
                rewardsSubtitle.setText(
                        coins + " " + com.anyNews.anynews.rewards.RewardsLabels.coinsWord(this));
                rewardsSubtitle.setVisibility(View.VISIBLE);
            } catch (Exception ignored) {
                rewardsSubtitle.setVisibility(View.GONE);
            }
        }
        if (layoutRewards != null) {
            layoutRewards.setVisibility(on ? View.VISIBLE : View.GONE);
            layoutRewards.setOnClickListener(v -> openRewardsFromProfile());
        }
        if (walletLabel != null) {
            walletLabel.setTextSize(menuSp);
            try {
                walletLabel.setText(
                        com.anyNews.anynews.rewards.RewardsLabels.walletScreenTitle(this));
            } catch (Exception ignored) {
            }
        }
        if (walletSubtitle != null) {
            try {
                String wallet =
                        SharedPrefsData.getString(
                                this, "rewards_wallet_rupees", Constants.PREF_NAME);
                if (TextUtils.isEmpty(wallet)) wallet = "0";
                walletSubtitle.setText("₹" + wallet);
                walletSubtitle.setVisibility(View.VISIBLE);
            } catch (Exception ignored) {
                walletSubtitle.setVisibility(View.GONE);
            }
        }
        if (layoutWallet != null) {
            layoutWallet.setVisibility(on ? View.VISIBLE : View.GONE);
            layoutWallet.setOnClickListener(v -> openRewardsWalletFromProfile());
        }
    }

    private void openRewardsWalletFromProfile() {
        if (!DistrictPinVisibilityConfig.rewardsVisibleForUser(this)) {
            Toast.makeText(this, "Wallet is currently off", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean loggedIn =
                SharedPrefsData.getBool(this, Constants.is_login, Constants.PREF_NAME);
        if (!loggedIn) {
            Intent login = new Intent(this, LoginActivityDetails.class);
            login.putExtra(LoginActivityDetails.EXTRA_OPEN_REWARDS, true);
            startActivity(login);
            return;
        }
        startActivity(
                new Intent(this, com.anyNews.anynews.rewards.RewardsWalletActivity.class));
    }

    private void openRewardsFromProfile() {
        if (!DistrictPinVisibilityConfig.rewardsVisibleForUser(this)) {
            Toast.makeText(this, "Rewards is currently off", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean loggedIn =
                SharedPrefsData.getBool(this, Constants.is_login, Constants.PREF_NAME);
        if (!loggedIn) {
            Intent login = new Intent(this, LoginActivityDetails.class);
            login.putExtra(LoginActivityDetails.EXTRA_OPEN_REWARDS, true);
            startActivity(login);
            return;
        }
        startActivity(new Intent(this, com.anyNews.anynews.rewards.RewardsHubActivity.class));
    }

    private void loadProfileCategories(RecyclerView catList, View categoriesBlock) {
        final int langId =
                SharedPrefsData.getInt(
                        getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
        try {
            String cached = CategoriesDiskCache.loadIfMatches(this, langId);
            if (!TextUtils.isEmpty(cached)) {
                SearchModel model = new Gson().fromJson(cached, SearchModel.class);
                if (model != null && model.getSuccess() && model.getData() != null
                        && !model.getData().isEmpty()) {
                    ArrayList<SearchModel.Datum> cats = new ArrayList<>(model.getData());
                    catList.setAdapter(new ProfileCategoryAdapter(cats));
                    categoriesBlock.setVisibility(View.VISIBLE);
                    return;
                }
            }
        } catch (Exception ignored) {
        }

        CategoriesRequest requestModel = new CategoriesRequest();
        requestModel.setDraftId(SharedPrefsData.getInt(this, Constants.DraftID, Constants.PREF_NAME));
        requestModel.setLanguageId(langId);
        requestModel.setScreenType(0);
        String jsonString = new Gson().toJson(requestModel);
        String encrpt_data = encrypt(jsonString);
        EncritonRequestData body = new EncritonRequestData();
        body.setData(encrpt_data);
        JsonObject otp = new Gson().toJsonTree(body).getAsJsonObject();

        APIService apiService = RestApiClient.getClient().create(APIService.class);
        apiService.List_All_Categories("v3", otp).enqueue(new Callback<EncreptionRequest>() {
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                try {
                    if (response.body() == null || response.body().getData() == null) {
                        categoriesBlock.setVisibility(View.GONE);
                        return;
                    }
                    String plain = decrypt(response.body().getData());
                    CategoriesDiskCache.save(newProfileDetails.this, langId, plain);
                    SearchModel model = new Gson().fromJson(plain, SearchModel.class);
                    if (model != null && model.getSuccess() && model.getData() != null
                            && !model.getData().isEmpty()) {
                        ArrayList<SearchModel.Datum> cats = new ArrayList<>(model.getData());
                        catList.setAdapter(new ProfileCategoryAdapter(cats));
                        categoriesBlock.setVisibility(View.VISIBLE);
                    } else {
                        categoriesBlock.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    showLog("loadProfileCategories: " + e.getMessage());
                    categoriesBlock.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                categoriesBlock.setVisibility(View.GONE);
            }
        });
    }

    private class ProfileCategoryAdapter extends RecyclerView.Adapter<ProfileCategoryAdapter.VH> {
        private final ArrayList<SearchModel.Datum> data;
        private final int sizePx;
        private final int selectedId;
        private final int[] CAT_COLORS = {
                0xFFEF4923, // orange — news
                0xFFE53935, // red — politics
                0xFF00897B, // teal
                0xFF1E88E5, // blue
                0xFF7E57C2, // purple
                0xFFEC407A, // pink
                0xFF43A047, // green
                0xFFFB8C00  // amber
        };
        private final int[] CAT_SOFT = {
                0x33EF4923,
                0x33E53935,
                0x3300897B,
                0x331E88E5,
                0x337E57C2,
                0x33EC407A,
                0x3343A047,
                0x33FB8C00
        };
        private final int[] CAT_ICONS = {
                R.drawable.ic_cat_news,
                R.drawable.ic_cat_politics,
                R.drawable.ic_cat_business,
                R.drawable.ic_cat_sports,
                R.drawable.ic_cat_cinema,
                R.drawable.ic_cat_generic,
                R.drawable.ic_cat_news,
                R.drawable.ic_cat_business
        };

        ProfileCategoryAdapter(ArrayList<SearchModel.Datum> data) {
            this.data = data;
            float density = getResources().getDisplayMetrics().density;
            this.sizePx = Math.round(ProfileHubConfigHelper.getCategoryIconSizeDp(newProfileDetails.this) * density);
            this.selectedId = SharedPrefsData.getInt(newProfileDetails.this, Constants.CategoryID, Constants.PREF_NAME);
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_profile_category_chip, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            SearchModel.Datum cat = data.get(position);
            String name = cat.getName() != null ? cat.getName() : "";
            holder.name.setText(name);

            int color = CAT_COLORS[Math.floorMod(position, CAT_COLORS.length)];
            int soft = CAT_SOFT[Math.floorMod(position, CAT_SOFT.length)];
            int iconRes = CAT_ICONS[Math.floorMod(position, CAT_ICONS.length)];
            boolean selected = cat.getCategoryId() == selectedId && selectedId > 0;

            if (holder.circle != null) {
                ViewGroup.LayoutParams clp = holder.circle.getLayoutParams();
                if (clp != null) {
                    clp.width = sizePx;
                    clp.height = sizePx;
                    holder.circle.setLayoutParams(clp);
                }
                android.graphics.drawable.GradientDrawable squircle =
                        new android.graphics.drawable.GradientDrawable();
                squircle.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
                float r = 16f * getResources().getDisplayMetrics().density;
                squircle.setCornerRadius(r);
                if (selected) {
                    squircle.setColor(color);
                    if (holder.icon != null) {
                        holder.icon.setImageResource(iconRes);
                        holder.icon.setVisibility(View.VISIBLE);
                        holder.icon.setColorFilter(0xFFFFFFFF);
                    }
                    holder.name.setTextColor(ContextCompat.getColor(
                            newProfileDetails.this, R.color.brand_orange));
                } else {
                    squircle.setColor(soft);
                    if (holder.icon != null) {
                        holder.icon.setImageResource(iconRes);
                        holder.icon.setVisibility(View.VISIBLE);
                        holder.icon.setColorFilter(color);
                    }
                    holder.name.setTextColor(ContextCompat.getColor(
                            newProfileDetails.this, R.color.profile_way2_menu_text));
                }
                holder.circle.setBackground(squircle);
            }
            if (holder.letter != null) {
                holder.letter.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> {
                try {
                    SharedPrefsData.putInt(newProfileDetails.this, Constants.CategoryID,
                            cat.getCategoryId(), Constants.PREF_NAME);
                } catch (Exception ignored) {
                }
                Intent intent = new Intent(newProfileDetails.this, HomePage.class);
                intent.putExtra("EXTRA_SESSION_ID", "" + cat.getCategoryId());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                if (getApplicationContext() != null
                        && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                    startActivity(intent);
                }
                finish();
            });
        }

        @Override
        public int getItemCount() {
            return data != null ? data.size() : 0;
        }

        class VH extends RecyclerView.ViewHolder {
            final TextView letter;
            final TextView name;
            final View circle;
            final ImageView icon;

            VH(View itemView) {
                super(itemView);
                letter = itemView.findViewById(R.id.category_chip_letter);
                name = itemView.findViewById(R.id.category_chip_name);
                circle = itemView.findViewById(R.id.category_chip_circle);
                icon = itemView.findViewById(R.id.category_chip_icon);
            }
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int nightMask = newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightMask != appliedNightUiMask) {
            pendingNightModeRecreate = true;
            if (!isFinishing() && hasWindowFocus()) {
                maybeRecreateForNightModeChange();
            }
        }
    }

    private void maybeRecreateForNightModeChange() {
        boolean confNight =
                (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                        == Configuration.UI_MODE_NIGHT_YES;
        boolean outOfSync =
                pendingNightModeRecreate
                        || confNight != com.anyNews.anynews.Utils.NightModeHelper.isEnabled(this)
                        || ((getResources().getConfiguration().uiMode
                                        & Configuration.UI_MODE_NIGHT_MASK)
                                != appliedNightUiMask);
        if (!outOfSync) {
            return;
        }
        pendingNightModeRecreate = false;
        appliedNightUiMask =
                getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        com.anyNews.anynews.Utils.NightModeHelper.recreateWithFade(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (pendingNightModeRecreate
                    || ((getResources().getConfiguration().uiMode
                                    & Configuration.UI_MODE_NIGHT_MASK)
                            != appliedNightUiMask)) {
                maybeRecreateForNightModeChange();
                return;
            }
        } catch (Exception ignored) {
        }

        //    getWriterProfileData();
        try {
            applyLoginGatedHubRows();
            softRefreshRewardsFlags();
            bindProfileLoginPill();
            bindReporterDesk();
            // Re-read create-menu prefs (splash / feed poll) so OFF types stay hidden
            // after HomePage applied a live Configuration update.
            bindReporterCreateFab();
            bindLocationRowText();
            bindStatusVideosNewBadge();
            bindBecomeReporterNewBadge();
            bindBirthdaySpecialNewBadge();
            if (local_distic_name != null) {
                String dist = com.anyNews.anynews.Utils.DistrictLabelHelper.resolveDisplayLabel(this);
                if (dist != null && !dist.isEmpty()) {
                    local_distic_name.setText(dist);
                }
            }
            if (textViewLanguageNameUser != null) {
                textViewLanguageNameUser.setText(resolveCurrentLanguageDisplay());
            }
        } catch (Exception ignored) {
        }

        // After gallery/camera returns: launch CanHub crop once fully resumed.
        if (needsCropAfterResume && pendingLocalImageUri != null && cropImage != null) {
            needsCropAfterResume = false;
            final Uri cropUri = pendingLocalImageUri;
            Log.e(TAG, "onResume launching profile crop for " + cropUri);
            getWindow().getDecorView().post(new Runnable() {
                @Override
                public void run() {
                    if (isFinishing() || isDestroyed()) {
                        needsCropAfterResume = true;
                        return;
                    }
                    launchProfileCrop(cropUri);
                }
            });
        }

        // Soft-refresh stats after navigating back (not on first open — onCreate already fetches).
        if (profileResumeArmed) {
            try {
                getWriterProfileData();
            } catch (Exception ignored) {
            }
        }
        profileResumeArmed = true;

        // Soft photo reminder for approved reporters (dismissible; cooldown in prefs).
        try {
            final android.view.View decor = getWindow() != null ? getWindow().getDecorView() : null;
            if (decor != null) {
                decor.postDelayed(() -> {
                    try {
                        if (isFinishing() || isDestroyed()) return;
                        Intent launch = getIntent();
                        if (launch != null && launch.getBooleanExtra("open_photo_picker", false)) {
                            launch.removeExtra("open_photo_picker");
                            if (!hasProfilePhoto()) {
                                beginChangeProfilePhoto();
                            }
                            return;
                        }
                        ReporterPhotoReminderHelper.maybeShow(newProfileDetails.this);
                    } catch (Exception ignored2) {
                    }
                }, 900);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onDestroy() {
        stopStatusVideosBadgePulse();
        if (sAliveProfile != null && sAliveProfile.get() == this) {
            sAliveProfile.clear();
        }
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    public void writeProfilePageSelection(String userId, String type_firebase, int draft_id, String device_id, long current_time) {
        ProfilePage appOpen = new ProfilePage(type_firebase, draft_id, device_id, current_time);

        //myRef.child("Events").push().setValue(appOpen);
    }

    @IgnoreExtraProperties
    @Keep
    public class ProfilePage {

        public String type;
        public int draft_id;
        public long created_at;
        public String device_id;


        public ProfilePage() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public ProfilePage(String type_firebase, int draft_id, String device_id, long current_time) {
            this.type = type_firebase;
            this.draft_id = draft_id;
            this.device_id = device_id;
            this.created_at = current_time;
        }

    }

}



