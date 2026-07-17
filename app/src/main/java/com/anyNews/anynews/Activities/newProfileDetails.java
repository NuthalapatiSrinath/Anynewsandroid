package com.anyNews.anynews.Activities;

import static com.anyNews.anynews.App.getContext;
import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.AppHelper;
import com.anyNews.anynews.Utils.CommonMethods;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.Logone;
import com.anyNews.anynews.Utils.MediaUrl;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.testing.MainActivity;
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
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class newProfileDetails extends AppCompatActivity {
    TextView local_distic_name;
    ImageView img_back;
    ImageView neo_settings;
    ImageView edit_profile_icon;
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

    AlertDialog alertDialog;
    private int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_IMAGE_REQUEST_CAMARA = 700;
    private static final int STORAGE_PERMISSION_CODE = 123;
    boolean CHECK_VALUE = false;
    CommonMethods mCommonmethods;
    private Uri filePath;
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

    LinearLayout reportLayout, userLayout,account_delete,layoutAppDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_new_profile_details);
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
        img_back = (ImageView) findViewById(R.id.img_back);
        neo_settings = (ImageView) findViewById(R.id.neo_settings);
        edit_profile_icon = (ImageView) findViewById(R.id.edit_profile_icon);
        local_distic_name = (TextView) findViewById(R.id.local_distic_name);
        login = (LinearLayout) findViewById(R.id.login);
        loadingProfileView = (LinearLayout) findViewById(R.id.loadingProfileView);
        loadingProfileView.setVisibility(View.GONE);
        loadingProgressbar = (LinearLayout) findViewById(R.id.loadingProgressbar);
        loadingProgressbar.setVisibility(View.VISIBLE);
        img_upload_photo = findViewById(R.id.img_upload_photo);
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
        account_delete_name = findViewById(R.id.account_delete_name);

        textViewAppDelete = findViewById(R.id.textViewAppDelete);
        profile_layout_rewards.setVisibility(View.GONE);
        profile_layout_shareToearn.setVisibility(View.VISIBLE);
        img_upload_photo.setVisibility(View.GONE);
        imageupload_progressbar.setVisibility(View.VISIBLE);

        getWriterProfileData();
        setStaticdata();
        boolean is_login = SharedPrefsData.getBool(newProfileDetails.this, Constants.is_login, Constants.PREF_NAME);
//        if (is_login) {
//            reportLayout.setVisibility(View.VISIBLE);
//            userLayout.setVisibility(View.GONE);
//            showLog("progressViewApproval VISIBLE");
//        } else {
//            reportLayout.setVisibility(View.GONE);
//            userLayout.setVisibility(View.VISIBLE);
//            showLog("progressViewApproval GONE");
//            progressMainLayout.setVisibility(View.GONE);
//
//        }


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
                Intent intent = new Intent(newProfileDetails.this, LanguageActivity.class);
                startActivity(intent);
            }
        });
        layoutChangeLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(newProfileDetails.this, LanguageActivity.class);
                startActivity(intent);
            }
        });
        layoutAppShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT,
                        "Hey check out my app at: https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });
        layoutFollowMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("layoutFollowMore");
                Intent intent = new Intent(newProfileDetails.this, FollowersProfileDetailes.class);
                startActivity(intent);
            }
        });
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

                boolean is_login = SharedPrefsData.getBool(newProfileDetails.this, Constants.is_login, Constants.PREF_NAME);

                if (is_login) {

                    Intent intent = new Intent(newProfileDetails.this, MainActivity.class);
                    intent.putExtra("PAYMENT_HISTORY", profile_total_amount.getText().toString());
                    if (getApplicationContext() != null && newProfileDetails.this.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                        startActivity(intent);
                    }

                } else {
                    Intent intent = new Intent(newProfileDetails.this, LoginActivityDetails.class);
                    startActivity(intent);
                }
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
                Intent intent = new Intent(newProfileDetails.this, LanguageActivity.class);
                startActivity(intent);
            }
        });
        next_payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (AvailablePoints >= RedeemValue) {

                    Intent intent = new Intent(newProfileDetails.this, RedeemPageActivity.class);
                    startActivity(intent);

                } else {

                    Toast.makeText(newProfileDetails.this, " "
                            + languageModelResponse.getData().getPleaseWaitUntilYouHaveReachedMinimumAmount() + RedeemValue, Toast.LENGTH_SHORT).show();

                }

            }
        });
        layout_articles_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Articles tile → submitted article status list (not wallet/history).
                Intent intent = new Intent(newProfileDetails.this, PostArticleStatus.class);
                startActivity(intent);
            }
        });
        layout_following_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent intent = new Intent(newProfileDetails.this, FolloweingMenders.class);
                startActivity(intent);
            }
        });
        layout_follow_more_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(newProfileDetails.this, FollowersProfileDetailes.class);
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
        writer_layout_followmore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent intent = new Intent(newProfileDetails.this, FollowersProfileDetailes.class);
                startActivity(intent);

            }
        });
        profile_layout_rewards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(newProfileDetails.this,"Coming soon..",Toast.LENGTH_SHORT).show();

//                boolean is_login = SharedPrefsData.getBool(newProfileDetails.this, Constants.is_login, Constants.PREF_NAME);
//
//                if (is_login) {
//
//                    Intent intent = new Intent(newProfileDetails.this, ScratchCardsList.class);
//                    intent.putExtra("REFER_LINK", "" + refer_earn);
//                    if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
//                        startActivity(intent);
//                    }
//                } else {
//                    Intent intent = new Intent(newProfileDetails.this, LoginActivityDetails.class);
//                    startActivity(intent);
//                }

            }
        });


        try {

            local_distic_name.setText("" + SharedPrefsData.getString(newProfileDetails.this, Constants.Selected_Distric_Name1, Constants.PREF_NAME));

            SharedPrefsData.putString(newProfileDetails.this, Constants.Selected_Distric_Name1, local_distic_name.getText().toString(), Constants.PREF_NAME);

        } catch (Exception e) {
            e.printStackTrace();
        }


        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(newProfileDetails.this, HomePage.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
//                    startActivity(intent);
//                }
                finish();
            }
        });


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(logintext.getText().toString().equalsIgnoreCase("Login")){
                    try {
                        Intent intent =
                                AccountPicker.newChooseAccountIntent(
                                        new AccountPicker.AccountChooserOptions.Builder()
                                                .setAllowableAccountsTypes(Arrays.asList("com.google"))
                                                .setAlwaysShowAccountPicker(true)
                                                .setTitleOverrideText("Choose Account For AnyNews")
                                                .build());
                        someActivityResultLauncher.launch(intent);
                    } catch (ActivityNotFoundException e) {
                        // TODO
                    }

                }



            }
        });

        neo_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(newProfileDetails.this, Settings_Scrren.class);
                startActivity(intent);
            }
        });

        edit_profile_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditProfileDialog();
            }
        });
        layoutAppDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAccountDeletePopup();
            }
        });
        account_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showAccountDeletePopup();
            }
        });


        username_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(newProfileDetails.this);
                ViewGroup viewGroup = view.findViewById(android.R.id.content);
                View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.customview, viewGroup, false);
                TextView button = dialogView.findViewById(R.id.sudmit_user_name);
                EditText enter_user_name = dialogView.findViewById(R.id.username_et);
                enter_user_name.setText(username_text.getText().toString());
                builder.setView(dialogView);
                alertDialog = builder.create();
                alertDialog.show();

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (enter_user_name.getText().toString().trim().length() >= 1) {
                            String newName = enter_user_name.getText().toString().trim();
                            SharedPrefsData.putString(newProfileDetails.this, Constants.USER_NAME, newName, Constants.PREF_NAME);
                            ProfileUpdate(newName);
                        }
                    }
                });
            }
        });

        img_upload_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkBox2.isChecked())
                    requestStoragePermission();
                else
                    Toast.makeText(newProfileDetails.this, "Please Select Privacy Policy", Toast.LENGTH_SHORT).show();
            }
        });

        // firebase events
        /*draft_id_firebase_profile = SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
        long current_time_firebase_profile = System.currentTimeMillis();
        String device_id_firebase = SharedPrefsData.getString(getApplicationContext(), Constants.DeviceID, Constants.PREF_NAME);

        writeProfilePageSelection("User",type_firebase_profile ,draft_id_firebase_profile ,device_id_firebase, current_time_firebase_profile);*/
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

    private void requestStoragePermission() {
        //Initialize Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(newProfileDetails.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(newProfileDetails.this,
                    Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted

                showFileChooser();
            } else {
                //Request Location Permission
                requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                        STORAGE_PERMISSION_CODE);
            }
        } else {
            showFileChooser();
        }
    }


    private void showFileChooser() {
        //   final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };
        //   AlertDialog.Builder builder = new AlertDialog.Builder(newProfileDetails.this);
        //  builder.setTitle("Add Photo!");


        Gson gson = new Gson();
        String Translation = prefShare.getShared_String(Allsharedpreference.Translator);
        languageModelResponse = gson.fromJson(Translation, LanguageModelResponse.class);


        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(newProfileDetails.this);
        builder.setTitle("" + languageModelResponse.getData().getAddPhoto());
        final CharSequence[] options = {"" + languageModelResponse.getData().getTakePhoto(), "" + languageModelResponse.getData().getChooseFromGallery(), "" + languageModelResponse.getData().getCancel()};


        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("" + languageModelResponse.getData().getTakePhoto())) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                    File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
//                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                        startActivityForResult(intent, PICK_IMAGE_REQUEST_CAMARA);
                    }


                } else if (options[item].equals("" + languageModelResponse.getData().getChooseFromGallery())) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                        startActivityForResult(intent, PICK_IMAGE_REQUEST);
                    }


                } else if (options[item].equals("" + languageModelResponse.getData().getCancel())) {

                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(newProfileDetails.this.getContentResolver(), filePath);
                img_upload_photo.setVisibility(View.GONE);
                img_upload_photo.setImageBitmap(bitmap);

                imageupload_progressbar.setVisibility(View.VISIBLE);

                uploadImage();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == PICK_IMAGE_REQUEST_CAMARA && resultCode == RESULT_OK) {
            File f = new File(Environment.getExternalStorageDirectory()
                    .toString());
            for (File temp : f.listFiles()) {
                if (temp.getName().equals("temp.jpg")) {
                    f = temp;
                    break;
                }
            }
            if (!f.exists()) {
                showLog("NRAAAAAAA" + img_upload_photo.toString());
                Toast.makeText(newProfileDetails.this,
                                "Error while capturing image", Toast.LENGTH_LONG)
                        .show();
                return;
            }
            try {
                bitmap = (Bitmap) data.getExtras().get("data");
                img_upload_photo.setImageBitmap(bitmap);
                img_upload_photo.setVisibility(View.GONE);
                imageupload_progressbar.setVisibility(View.VISIBLE);

                uploadImage();
                //storeImageTosdCard(bitmap);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else if (requestCode == 200 && resultCode == RESULT_OK) {


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

                        layout_with_out_data.setVisibility(View.GONE);
                        layout_with_data.setVisibility(View.VISIBLE);

                        Toast.makeText(newProfileDetails.this, "" + languageModelResponse.getData().getImageUploadSuccessfully(), Toast.LENGTH_SHORT).show();
                        img_upload_photo.setVisibility(View.GONE);
                        imageupload_progressbar.setVisibility(View.VISIBLE);
                        if (ImageId != null)
                            Picasso.get().load(MediaUrl.resolve(ImageId)).into(img_upload_photo);


                        ProfileUpdateImage();

                    } else {
                        Toast.makeText(getContext(), "Image not Captured", Toast.LENGTH_SHORT).show();

                    }
                } catch (JSONException e) {
//                    mCropImageUri = null;
                    e.printStackTrace();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                String errorMessage = "Unknown error";
//                mCropImageUri = null;
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
                mCommonmethods.showerror(errorMessage);
                error.printStackTrace();
            }
        }, new VolleyMultipartRequest.MultipartProgressListener() {
            @Override
            public void transferred(long transferred, final int progress) {
                newProfileDetails.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String progresscontent = String.valueOf(progress);
//                        pDialog.setTitle("Uploading.." + progresscontent+" %");

                    }
                });
//
            }
        }) {
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                params.put("file", new DataPart(UUID.randomUUID().toString() + ".jpeg", AppHelper.getFileDataFromDrawable(getContext(), img_upload_photo.getDrawable()), "image/jpeg"));
                return params;
            }
        };
//        // Adding request to request queue
        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                100000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(newProfileDetails.this);

        //Adding request to the queue
        requestQueue.add(multipartRequest);
        // VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(multipartRequest);


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


    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(newProfileDetails.this);
        View dialogView = LayoutInflater.from(newProfileDetails.this)
                .inflate(R.layout.dialog_edit_profile, null, false);

        final EditText nameField = dialogView.findViewById(R.id.edit_profile_name);
        final EditText emailField = dialogView.findViewById(R.id.edit_profile_email);
        final ProgressBar progress = dialogView.findViewById(R.id.edit_profile_progress);
        final TextView saveBtn = dialogView.findViewById(R.id.edit_profile_save);
        final TextView cancelBtn = dialogView.findViewById(R.id.edit_profile_cancel);

        // Pre-fill with current values.
        String prefillName = username_text.getText() != null ? username_text.getText().toString().trim() : "";
        nameField.setText(prefillName);
        nameField.setSelection(nameField.getText().length());
        emailField.setText(currentUserEmail != null ? currentUserEmail.trim() : "");

        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = nameField.getText().toString().trim();
                String newEmail = emailField.getText().toString().trim();

                if (newName.isEmpty()) {
                    nameField.setError("Name cannot be empty");
                    return;
                }
                if (!newEmail.isEmpty()
                        && !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                    emailField.setError("Enter a valid email");
                    return;
                }

                progress.setVisibility(View.VISIBLE);
                saveBtn.setEnabled(false);
                cancelBtn.setEnabled(false);
                updateReporterProfile(newName, newEmail, dialog, progress, saveBtn, cancelBtn);
            }
        });
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
                    // Update local cache + UI immediately.
                    SharedPrefsData.putString(newProfileDetails.this, Constants.USER_NAME, name, Constants.PREF_NAME);
                    username_text.setText(name);
                    currentUserEmail = email;
                    if (email != null && !email.isEmpty()) {
                        user_login_number.setText(" " + email);
                    }
                    Toast.makeText(newProfileDetails.this, "Profile updated", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    // Refresh from backend so counts/name stay consistent.
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
          super.onBackPressed();
//        Intent intent = new Intent(newProfileDetails.this, HomePage.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
//            startActivity(intent);
//        }

    }

    private void setStaticdata() {
        prefShare = new Allsharedpreference(newProfileDetails.this);
        Gson gson = new Gson();
        String Translation = prefShare.getShared_String(Allsharedpreference.Translator);
        languageModelResponse = gson.fromJson(Translation, LanguageModelResponse.class);
        language_name.setText(languageModelResponse.getData().getLanguage());
        textViewLanguageNameUser.setText(languageModelResponse.getData().getLanguage());
        textViewFollowingUser.setText(" " + languageModelResponse.getData().getFollowing());
        textViewShareEarnUser.setText("" + languageModelResponse.getData().getShareToEarn());
        textViewFollowMore.setText("" + languageModelResponse.getData().getFollowMore());

        profile_article_name.setText(" " + languageModelResponse.getData().getArticles());
        profile_like_name.setText(" " + languageModelResponse.getData().getLikes());
        profile_followers_name.setText(" " + languageModelResponse.getData().getFollowers());
        profile_following_name.setText(" " + languageModelResponse.getData().getFollowing());
        writer_following_name.setText(" " + languageModelResponse.getData().getFollowing());
        profile_follow_more.setText("" + languageModelResponse.getData().getFollowMore());
        user_rewards.setText("" + languageModelResponse.getData().getRewards());
        user_share_to_earn.setText("" + languageModelResponse.getData().getShareToEarn());
        user_follow_more_data.setText("" + languageModelResponse.getData().getFollowMore());
        only_language_name.setText("" + languageModelResponse.getData().getLanguage());
        total_amount.setText("" + languageModelResponse.getData().getTotalAmount());
        profile_next_payment.setText("" + languageModelResponse.getData().getNextPayment());
        logintext.setText("" + languageModelResponse.getData().getLogin());
        account_delete_name.setText("" + languageModelResponse.getData().getDelete_account());
        if(languageModelResponse.getData().getDelete_account()!=null && !languageModelResponse.getData().getDelete_account().equalsIgnoreCase("null")){
            textViewAppDelete.setText("" + languageModelResponse.getData().getDelete_account());
        }else{
            textViewAppDelete.setText("Delete Account");
        }





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
     * Renders the last-known reporter profile from SharedPrefs cache so the screen
     * shows real content immediately on open (name, location, counts, points, email
     * and avatar) instead of a blank blocking "Loading..." spinner. The live network
     * response then updates these values when it returns. No-op when there is no
     * cached profile yet (first ever open).
     */
    private void showCachedProfileInstant() {
        try {
            String cached = SharedPrefsData.getString(getApplicationContext(), Constants.PROFILE_CACHE, Constants.PREF_NAME);
            if (cached == null || cached.isEmpty()) {
                return;
            }
            NewUserProfileResponse resp = new Gson().fromJson(cached, NewUserProfileResponse.class);
            if (resp == null || !Boolean.TRUE.equals(resp.getSuccess()) || resp.getData() == null) {
                return;
            }
            NewUserProfileResponse.Data d = resp.getData();

            // Reveal content, hide the blocking spinner.
            loadingProgressbar.setVisibility(View.GONE);
            loadingProfileView.setVisibility(View.VISIBLE);
            imageupload_progressbar.setVisibility(View.GONE);
            img_upload_photo.setVisibility(View.VISIBLE);
            local_distic_name.setVisibility(View.VISIBLE);

            username_text.setText(d.getUserName());
            local_distic_name.setText(d.getLocation());
            profile_article_count.setText("" + d.getArticles());
            profile_like_count.setText("" + d.getLikes());
            profile_followers_count.setText("" + d.getFollowers());
            profile_following_count.setText("" + d.getFollowing());
            writer_following_count.setText("" + d.getFollowing());
            profile_total_amount.setText("\u20B9 " + d.getTotalPoints());
            earned_total_amount.setText("\u20B9 " + d.getAvailablePoints());
            user_login_number.setText(" " + d.getEmail());
            currentUserEmail = d.getEmail() != null ? d.getEmail() : "";
            payment_layout.setVisibility(View.VISIBLE);

            boolean isReporter = Boolean.TRUE.equals(d.getIsReporter())
                    && d.getMobileNumber() != null
                    && !d.getMobileNumber().equals("")
                    && !d.getMobileNumber().equals("null");
            if (isReporter) {
                profile_articales_list.setVisibility(View.VISIBLE);
                profile_following_list.setVisibility(View.VISIBLE);
                progressMainLayout.setVisibility(View.VISIBLE);
                user_articales_list.setVisibility(View.GONE);
                profile_follower_list.setVisibility(View.VISIBLE);
                profile_refer_list.setVisibility(View.VISIBLE);
                only_profile_follower_list.setVisibility(View.VISIBLE);
                profile_one.setVisibility(View.GONE);
            } else {
                reportLayout.setVisibility(View.GONE);
                userLayout.setVisibility(View.VISIBLE);
                progressMainLayout.setVisibility(View.GONE);
            }

            try {
                if (d.getProfilePic() != null) {
                    Picasso.get().load(MediaUrl.resolve(d.getProfilePic())).into(img_upload_photo);
                    layout_with_out_data.setVisibility(View.GONE);
                    layout_with_data.setVisibility(View.VISIBLE);
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getWriterProfileData() {

        userProfileResponse = new ArrayList<>();

        // Paint cached profile immediately (avatar/name/counts) so the screen shows
        // content right away while the network request refreshes it in the background.
        showCachedProfileInstant();

        JsonObject otp = set_writer_profile_data();


        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.get_user_profile("v3", otp);
        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {


                EncreptionRequest encreptionRequest = response.body();
                String encrpt_data = null;
                /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    encrpt_data = decrypt(encreptionRequest.getData());
                }*/
                AppHelper.showLog("Encrypted GETData:"+encreptionRequest.getData());
                encrpt_data = decrypt(encreptionRequest.getData());
                AppHelper.showLog("Encrypted Data:"+encrpt_data);
                AppHelper.showLog("Encrypted Data:"+encreptionRequest.getCards_display());

                Gson gsonObj = new Gson();
                NewUserProfileResponse newUserProfileResponse = gsonObj.fromJson(encrpt_data, NewUserProfileResponse.class);

                if (newUserProfileResponse.getSuccess()) {
                    // Cache the decrypted profile so the next open can paint instantly
                    // (from cache) instead of showing a blank "Loading..." spinner.
                    try {
                        SharedPrefsData.putString(getApplicationContext(), Constants.PROFILE_CACHE, encrpt_data, Constants.PREF_NAME);
                    } catch (Exception ignored) {
                    }
                    local_distic_name.setVisibility(View.VISIBLE);
                 //   username_text.setTextColor(getResources().getColor(R.color.white));
                    username_text.setText(newUserProfileResponse.getData().getUserName());
                    local_distic_name.setText(newUserProfileResponse.getData().getLocation());
                    profile_article_count.setText("" + newUserProfileResponse.getData().getArticles());
                    profile_like_count.setText("" + newUserProfileResponse.getData().getLikes());
                    profile_followers_count.setText("" + newUserProfileResponse.getData().getFollowers());
                    profile_following_count.setText("" + newUserProfileResponse.getData().getFollowing());
                    writer_following_count.setText("" + newUserProfileResponse.getData().getFollowing());
                    profile_total_amount.setText("\u20B9 " + newUserProfileResponse.getData().getTotalPoints());
                    earned_total_amount.setText("\u20B9 " + newUserProfileResponse.getData().getAvailablePoints());
                    user_login_number.setText(" " + newUserProfileResponse.getData().getEmail());
                    currentUserEmail = newUserProfileResponse.getData().getEmail() != null
                            ? newUserProfileResponse.getData().getEmail() : "";
                    String progress = newUserProfileResponse.getData().getApproval_rates();
                    Logone.e("ApprovalRates", "" + newUserProfileResponse.getData().getApproval_rates());
                    // set approval rates for freelancer profile




                    if (newUserProfileResponse.getData().getMobileNumber().equals("") || newUserProfileResponse.getData().getMobileNumber().equals("null")) {

                        payment_layout.setVisibility(View.VISIBLE);
                        profile_total_amount.setText("\u20B9 " + "0");

                    } else {
                        payment_layout.setVisibility(View.VISIBLE);
                    }
                    refer_earn = newUserProfileResponse.getData().getRefLink();

                    AvailablePoints = newUserProfileResponse.getData().getAvailablePoints();
                    RedeemValue = newUserProfileResponse.getData().getRedeemValue();

                    SharedPrefsData.putString(getApplicationContext(), Constants.Selected_Phone_Number, "" + newUserProfileResponse.getData().getMobileNumber(), Constants.PREF_NAME);

                    SharedPrefsData.putString(getApplicationContext(), Constants.AVAILABLEPOINTS, "" + newUserProfileResponse.getData().getAvailablePoints(), Constants.PREF_NAME);
                    SharedPrefsData.putString(getApplicationContext(), Constants.REDEEMPOINTS, "" + newUserProfileResponse.getData().getRedeemValue(), Constants.PREF_NAME);
                    SharedPrefsData.putBool(getApplicationContext(), Constants.PAN_CARD_STATUS, newUserProfileResponse.getData().getPanStatus(), Constants.PREF_NAME);


                    int AvailablePoints_here = (int) AvailablePoints;
                    int RedeemValue_here = (int) RedeemValue;

                    androidProgressBar.setMax(RedeemValue_here);

                    if (AvailablePoints_here >= RedeemValue_here) {

                        new Thread(new Runnable() {
                            public void run() {
                                while (progressStatusCounter < AvailablePoints_here) {
                                    progressStatusCounter += 2;
                                    progressHandler.post(new Runnable() {
                                        public void run() {
                                            androidProgressBar.setProgress(RedeemValue_here);

                                        }
                                    });
                                    try {
                                        Thread.sleep(10);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }).start();

                    } else {

                        new Thread(new Runnable() {
                            public void run() {
                                while (progressStatusCounter < AvailablePoints_here) {
                                    progressStatusCounter += 2;
                                    progressHandler.post(new Runnable() {
                                        public void run() {
                                            androidProgressBar.setProgress(AvailablePoints_here);

                                        }
                                    });
                                    try {
                                        Thread.sleep(10);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }).start();

                    }


                    SharedPrefsData.putLong(getApplicationContext(), Constants.USER_ID, newUserProfileResponse.getData().getUserId(), Constants.PREF_NAME);

                    imageupload_progressbar.setVisibility(View.GONE);
                    loadingProgressbar.setVisibility(View.GONE);
                    loadingProfileView.setVisibility(View.VISIBLE);

                    img_upload_photo.setVisibility(View.VISIBLE);
                    showLog("Reporter Login::"+newUserProfileResponse.getData().getIsReporter());
                    // Cache the approved-reporter flag so HomePage's center FAB can reflect
                    // an approval change on resume without requiring a reinstall.
                    SharedPrefsData.putBool(getApplicationContext(), Constants.is_reporter,
                            Boolean.TRUE.equals(newUserProfileResponse.getData().getIsReporter()), Constants.PREF_NAME);
                    boolean b=newUserProfileResponse.getData().getIsReporter() && !newUserProfileResponse.getData().getMobileNumber().equals("") && !newUserProfileResponse.getData().getMobileNumber().equals("null");
                    if (b) {
                        showLog("Reporter is logged in");
                        profile_articales_list.setVisibility(View.VISIBLE);
                        profile_following_list.setVisibility(View.VISIBLE);
                        progressMainLayout.setVisibility(View.VISIBLE);
                        user_articales_list.setVisibility(View.GONE);
                        profile_follower_list.setVisibility(View.VISIBLE);
                        profile_refer_list.setVisibility(View.VISIBLE);
                        only_profile_follower_list.setVisibility(View.GONE);
                        only_profile_follower_list.setVisibility(View.VISIBLE);
                        profile_one.setVisibility(View.GONE);
                        if(progress!=null){
                            progressViewApproval.setLabelText(progress + "% achieved");
                            progressViewApproval.setProgress(Integer.parseInt(progress));
                        }else{
                            progressViewApproval.setLabelText("0% achieved");
                            progressViewApproval.setProgress(Integer.parseInt("0"));
                        }
                        try {
                            if(encreptionRequest.getCards_display()!=null){
                                if(!encreptionRequest.getCards_display().isNext_payment()|| !encreptionRequest.getCards_display().isWallet_balance()){
                                    payment_layout.setVisibility(View.GONE);
                                }else{
                                    payment_layout.setVisibility(View.VISIBLE);
                                }
                                if(!encreptionRequest.getCards_display().isArticles()){
                                    layout_articles_list.setVisibility(View.GONE);
                                }
                                if(!encreptionRequest.getCards_display().isLikes()){
                                    layout_likes_list.setVisibility(View.GONE);
                                }
                                if(!encreptionRequest.getCards_display().isFollowers()){
                                    layout_follower_list.setVisibility(View.GONE);
                                }

                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    } else {
                        showLog("USER is logged in");
                        reportLayout.setVisibility(View.GONE);
                        userLayout.setVisibility(View.VISIBLE);
                        progressMainLayout.setVisibility(View.GONE);

//                        profile_refer_list.setVisibility(View.VISIBLE);
//                        user_articales_list.setVisibility(View.VISIBLE);
//                        profile_articales_list.setVisibility(View.GONE);
//                        profile_follower_list.setVisibility(View.GONE);
//                        profile_following_list.setVisibility(View.GONE);
//                        only_profile_follower_list.setVisibility(View.VISIBLE);
//                        progressViewApproval.setVisibility(View.GONE);
//
//                        profile_one.setVisibility(View.INVISIBLE);
                    }
                    try {
                        if (newUserProfileResponse.getData().getProfilePic() != null) {
                            Picasso.get().load(MediaUrl.resolve(newUserProfileResponse.getData().getProfilePic().toString())).into(img_upload_photo);
                            layout_with_out_data.setVisibility(View.GONE);
                            layout_with_data.setVisibility(View.VISIBLE);
                        }
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                    showLog("Display Email Id:" + prefShare.getEmailId());
                    if (prefShare.getEmailId().equals("")) {
                        login_neomorph_layout.setVisibility(View.VISIBLE);
                        logintext.setText("Login");
                        user_login_number.setVisibility(View.GONE);
                    } else {
                        login_neomorph_layout.setVisibility(View.VISIBLE);
                        logintext.setText("Logged-in");
                    }

                }

            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {

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


                EncreptionRequest encreptionRequest = response.body();
                String encrpt_data = null;
                /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    encrpt_data = decrypt(encreptionRequest.getData());
                }*/
                encrpt_data = decrypt(encreptionRequest.getData());


                Gson gsonObj = new Gson();
                UpdateImageResponse userProfileUpdateResponse = gsonObj.fromJson(encrpt_data, UpdateImageResponse.class);


                if (userProfileUpdateResponse.getSuccess()) {


                    layout_with_out_data.setVisibility(View.GONE);
                    layout_with_data.setVisibility(View.VISIBLE);

                    imageupload_progressbar.setVisibility(View.GONE);
                    img_upload_photo.setVisibility(View.VISIBLE);

                    Toast.makeText(newProfileDetails.this, "Profile has been updated", Toast.LENGTH_SHORT).show();


                } else {

                }
            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {


                Toast.makeText(newProfileDetails.this, t.getMessage(), Toast.LENGTH_LONG).show();


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

    @Override
    protected void onResume() {
        super.onResume();

        //    getWriterProfileData();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //   getWriterProfileData();
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



