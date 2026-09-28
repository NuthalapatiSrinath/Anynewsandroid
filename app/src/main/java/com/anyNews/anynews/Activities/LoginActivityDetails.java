package com.anyNews.anynews.Activities;

import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.Pojo.AddProfileResponse;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.GenerateOtpRequest;
import com.anyNews.anynews.Pojo.GenerateOtpResponse;
import com.anyNews.anynews.Pojo.LanguageModelResponse;
import com.anyNews.anynews.Pojo.LanguageSelectionRequest;
import com.anyNews.anynews.Pojo.UserProfileRequest;
import com.anyNews.anynews.Pojo.ValidateOtpRequest;
import com.anyNews.anynews.Pojo.ValidateOtpResponse;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.AppHelper;
import com.anyNews.anynews.Utils.AppSignatureHelper;
import com.anyNews.anynews.Utils.CommonMethods;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.DistrictLabelHelper;
import com.anyNews.anynews.Utils.LanguageFeedInvalidator;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.Utils.SmsBroadcastReceiver;
import com.anyNews.anynews.Utils.TranslatorPackHelper;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.common.AccountPicker;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import soup.neumorphism.NeumorphCardView;

public class LoginActivityDetails extends AppCompatActivity {

    /** When true, after OTP success reopen Profile (single login entry from Profile hub). */
    public static final String EXTRA_RETURN_TO_PROFILE = "extra_return_to_profile";
    /** When true, after OTP open Become-a-Reporter applicant flow (login source flag). */
    public static final String EXTRA_OPEN_BECOME_A_REPORTER = "extra_open_become_a_reporter";
    /** When true, after OTP open Rewards hub (Join rewards chip / Profile). */
    public static final String EXTRA_OPEN_REWARDS = "extra_open_rewards";

    private static final int STEP_MOBILE = 1;
    private static final int STEP_OTP = 2;
    private static final int STEP_PROFILE = 3;

    NeumorphCardView neumorph_next;
    EditText username_et, mobile_et,emailIdEdittext;
    EditText otp_1, otp_2, otp_3, otp_4;
    TextView post_login, otpnew;
    TextView txt_mobile_otp, txt_resend_otp;
    TextView otp_submit;
    View username_card, email_card, mobile_card;

    ProgressDialog pDialog;
    CommonMethods commonMethods;
    public StringBuilder finalOtp;
    private Allsharedpreference prefManager;
    Allsharedpreference prefShare;
    LanguageModelResponse languageModelResponse;
    private Allsharedpreference allsharedpreference;

    String num;
    String phone_number_data;
    EditText[] editTexts;
    LinearLayout login_detailes, otp_layout, verify_layout, resend_layout, post_layout, sendotpnew;
    private static final long RESEND_COOLDOWN_MS = 45_000L;
    private static final long OTP_LOADING_DOT_MS = 400L;
    private static final String TAG_OTP = "LoginOtp";
    SmsBroadcastReceiver smsBroadcastReceiver;
    private boolean smsReceiverRegistered = false;
    private CountDownTimer resendCountDownTimer;
    private String resendOtpLabel = "Resend OTP";
    private boolean resendEnabled = true;
    EditText otpNew;
    private int loginStep = STEP_MOBILE;
    private boolean pendingReturnToProfile = false;
    private boolean pendingOpenBecomeReporter = false;
    private boolean pendingOpenRewards = false;
    /** True while ReadOtp API is in flight — blocks double-submit. */
    private boolean otpVerifyInFlight = false;
    /** Skip PinTextWatcher side-effects while distributing autofilled digits. */
    private boolean otpProgrammaticUpdate = false;
    private String otpVerifyLabel = " Verify";
    private final Handler otpLoadingHandler = new Handler(Looper.getMainLooper());
    private Runnable otpLoadingDotsRunnable;
    private int otpLoadingDotStep = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_details);
        applyLoginSystemBars();
        neumorph_next = findViewById(R.id.neumorph_next);
        txt_resend_otp = findViewById(R.id.txt_resend_otp);
        emailIdEdittext = findViewById(R.id.emailId);
        username_card = findViewById(R.id.username_card);
        email_card = findViewById(R.id.email_card);
        mobile_card = findViewById(R.id.mobile_card);
        //otpNew = findViewById(R.id.otp_new);
        neumorph_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                neumorph_next.setShadowElevation(15);
            }
        });

        //     phone_number_data=mobile_et.getText().toString();


        otp_layout = (LinearLayout) findViewById(R.id.otp_layout);
        login_detailes = (LinearLayout) findViewById(R.id.login_detailes);
        verify_layout = (LinearLayout) findViewById(R.id.verify_layout);
        sendotpnew = (LinearLayout) findViewById(R.id.sendotpnew);
        resend_layout = findViewById(R.id.resend_layout);
        post_layout = findViewById(R.id.post_layout);
        otp_submit = (TextView) findViewById(R.id.otp_submit);
        username_et = (EditText) findViewById(R.id.username_et);
        mobile_et = (EditText) findViewById(R.id.mobile_et);
        post_login = (TextView) findViewById(R.id.post_login);
        otpnew = (TextView) findViewById(R.id.otpnew);
        allsharedpreference=new Allsharedpreference(this);

        prefManager = new Allsharedpreference(LoginActivityDetails.this);
        LoginActivityDetails.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        commonMethods = new CommonMethods(LoginActivityDetails.this);


        pDialog = new ProgressDialog(LoginActivityDetails.this);
        pDialog.setCancelable(false);

        // Step 1: mobile only — hide name/email until after OTP.
        if (username_card != null) username_card.setVisibility(View.GONE);
        if (email_card != null) email_card.setVisibility(View.GONE);
        if (mobile_card != null) mobile_card.setVisibility(View.VISIBLE);
        loginStep = STEP_MOBILE;


        mobile_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                ViewGroup.MarginLayoutParams layoutParams =
                        (ViewGroup.MarginLayoutParams) post_layout.getLayoutParams();

                //  layoutParams.setMargins(30, 50, 30, 100);
                post_layout.requestLayout();

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }

            @Override
            public void afterTextChanged(Editable s) {
                if (loginStep != STEP_MOBILE) {
                    return;
                }
                if (mobile_et.getText().toString().length() >= 10) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mobile_et.getWindowToken(), 0);


                    post_layout.setVisibility(View.GONE);
                    post_login.setVisibility(View.GONE);
                    sendotpnew.setVisibility(View.VISIBLE);
                } else {
                    post_layout.setVisibility(View.VISIBLE);
                    post_login.setVisibility(View.VISIBLE);
                    sendotpnew.setVisibility(View.GONE);

                }
            }
        });


        post_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (loginStep == STEP_PROFILE) {
                    confirmProfileAndFinish();
                    return;
                }
                // Step 1: mobile only → send OTP
                if (mobile_et.getText().toString().length() <= 0) {
                    Toast.makeText(LoginActivityDetails.this, "Please Enter Mobile Number", Toast.LENGTH_SHORT).show();
                } else if (mobile_et.getText().toString().length() < 10) {
                    Toast.makeText(LoginActivityDetails.this, "Please Enter Valid Mobile Number", Toast.LENGTH_SHORT).show();
                } else {
                    num = mobile_et.getText().toString();
                    GenerateOtp();
                    showOtpEntryUi();
                }
            }
        });


        otpnew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mobile_et.getText().toString().length() >= 10) {
                    num = mobile_et.getText().toString();
                    GenerateOtp();
                    showOtpEntryUi();
                } else {
                    Toast.makeText(LoginActivityDetails.this, "Please Enter Mobile Number", Toast.LENGTH_SHORT).show();
                }
            }
        });

        txt_resend_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!resendEnabled) {
                    return;
                }
                clearOtpBoxes();
                GenerateOtp();
            }
        });

        txt_mobile_otp = findViewById(R.id.txt_mobile_otp);
        otp_1 = findViewById(R.id.otp_1);
        otp_2 = findViewById(R.id.otp_2);
        otp_3 = findViewById(R.id.otp_3);
        otp_4 = findViewById(R.id.otp_4);

        txt_mobile_otp.setText("We have sent a " + "4 digit OTP" + "  \n" + "On " + num);

        editTexts = new EditText[]{otp_1, otp_2, otp_3, otp_4};

        otp_1.addTextChangedListener(new LoginActivityDetails.PinTextWatcher(0));
        otp_2.addTextChangedListener(new LoginActivityDetails.PinTextWatcher(1));
        otp_3.addTextChangedListener(new LoginActivityDetails.PinTextWatcher(2));
        otp_4.addTextChangedListener(new LoginActivityDetails.PinTextWatcher(3));

        otp_1.setOnKeyListener(new LoginActivityDetails.PinOnKeyListener(0));
        otp_2.setOnKeyListener(new LoginActivityDetails.PinOnKeyListener(1));
        otp_3.setOnKeyListener(new LoginActivityDetails.PinOnKeyListener(2));
        otp_4.setOnKeyListener(new LoginActivityDetails.PinOnKeyListener(3));
        emailIdEdittext.setText(allsharedpreference.getEmailId());

        // Focus/keyboard applied when OTP UI is shown (layout starts GONE).


        otp_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitOtpFromBoxes(false);
            }
        });
        emailIdEdittext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Optional Google account picker; field is also freely editable.
                String current = emailIdEdittext.getText() != null
                        ? emailIdEdittext.getText().toString().trim() : "";
                if (current.isEmpty() || !current.contains("@")) {
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

        setStaticdata();

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
                            emailIdEdittext.setText(emailID);
                            showLog("EmailID"+emailID);

                            //postComment();
                        }else{
                            Toast.makeText(LoginActivityDetails.this,"Unable to pickup the account please try again later..",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    private void setStaticdata() {
        prefShare = new Allsharedpreference(LoginActivityDetails.this);
        Gson gson = new Gson();
        String Translation = prefShare.getShared_String(Allsharedpreference.Translator);
        languageModelResponse = gson.fromJson(Translation, LanguageModelResponse.class);
        username_et.setHint(" " + languageModelResponse.getData().getUserName());
        mobile_et.setHint(" " + languageModelResponse.getData().getMobileNumber());
        post_login.setText(" " + languageModelResponse.getData().getSendOtp());
        otpnew.setText(" " + languageModelResponse.getData().getSendOtp());
        otpVerifyLabel = " " + languageModelResponse.getData().getVerify();
        if (!otpVerifyInFlight) {
            otp_submit.setText(otpVerifyLabel);
        }
        resendOtpLabel = " " + languageModelResponse.getData().getResendOtp();
        if (resendEnabled) {
            txt_resend_otp.setText(resendOtpLabel);
        }
        //Toast.makeText(LoginActivityDetails.this, ""+languageModelResponse.getData().getSendOtp(), Toast.LENGTH_SHORT).show();


    }

    /** Switches registration form → OTP digit entry and focuses first box + keyboard. */
    private void showOtpEntryUi() {
        loginStep = STEP_OTP;
        otp_layout.setVisibility(View.VISIBLE);
        resend_layout.setVisibility(View.VISIBLE);
        verify_layout.setVisibility(View.VISIBLE);
        login_detailes.setVisibility(View.GONE);
        if (num != null && !num.isEmpty()) {
            txt_mobile_otp.setText("We have sent a 4 digit OTP\nOn +91 " + num);
        }
        focusFirstOtpBox();
    }

    /**
     * On OTP success, overwrite SharedPreferences language/state/district from the
     * logged-in account (server Writers/Users). Logout intentionally keeps the prior
     * device geography — without this, Profile still shows the previous user's district.
     *
     * <p>Also syncs {@link Constants#Lang} display name and invalidates the prior
     * language's translator/feed packs (keeping the geo we just wrote). Otherwise
     * Profile can show stale "हिन्दी" while Telugu strings paint from LanguageID,
     * or Hindi chrome with a Telugu Hyderabad label.
     */
    private void applyAccountGeographyFromLogin(ValidateOtpResponse.Data loginData) {
        if (loginData == null) return;
        Context app = getApplicationContext();
        int previousLangId = SharedPrefsData.getInt(app, Constants.LanguageID, Constants.PREF_NAME);
        int langId = loginData.getLanguageId() != null ? loginData.getLanguageId() : 0;
        int stateId = loginData.getStateId() != null ? loginData.getStateId() : 0;
        int distId = loginData.getDistId() != null ? loginData.getDistId() : 0;
        String location = loginData.getLocation() != null ? loginData.getLocation().trim() : "";
        if (langId > 0) {
            // Keep geography: Writers state/district are applied below.
            LanguageFeedInvalidator.onLanguageChanged(app, langId, false);
            SharedPrefsData.putInt(app, Constants.reporter_language_id, langId, Constants.PREF_NAME);
            SharedPrefsData.putString(app, Constants.Lang, languageDisplayName(langId), Constants.PREF_NAME);
            SharedPrefsData.putBool(app, Constants.IS_LANGUAGE_SELECTED, true, Constants.PREF_NAME);
            if (langId != previousLangId) {
                refreshTranslatorPackForLanguage(langId);
            }
        }
        if (stateId > 0) {
            SharedPrefsData.putInt(app, Constants.StateID, stateId, Constants.PREF_NAME);
        }
        if (distId > 0 || DistrictLabelHelper.isUsable(location)) {
            int langForSave = langId > 0
                    ? langId
                    : SharedPrefsData.getInt(app, Constants.LanguageID, Constants.PREF_NAME);
            if (DistrictLabelHelper.isUsable(location)) {
                DistrictLabelHelper.saveFromServer(app, langForSave, distId, location);
            } else if (distId > 0) {
                // Id-only: still go through language-scoped save (rejects foreign ids when cache warm).
                DistrictLabelHelper.saveFromServer(app, langForSave, distId, null);
            }
        }
        // Final guard: never keep Hindi Bhiwani (etc.) under a Telugu LanguageID.
        DistrictLabelHelper.ensureSelectedMatchesLanguage(app);
    }

    /** Native language label for Profile "भाषा चुनें" / language row (matches LanguageID). */
    private static String languageDisplayName(int languageId) {
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

    /**
     * Pull get_static_data for the account language so footer/profile packs match
     * LanguageID after OTP (avoids Hindi chrome + Telugu half-pack leftovers).
     */
    private void refreshTranslatorPackForLanguage(int languageId) {
        if (languageId <= 0) return;
        try {
            LanguageSelectionRequest requestModel = new LanguageSelectionRequest();
            requestModel.setDraftId(SharedPrefsData.getInt(this, Constants.DraftID, Constants.PREF_NAME));
            requestModel.setLanguageId(languageId);
            JsonObject body = new Gson().toJsonTree(requestModel).getAsJsonObject();
            APIService apiService = RestApiClient.getClient().create(APIService.class);
            Call<JsonObject> call = apiService.get_static_data("v3", body);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                    try {
                        if (!response.isSuccessful() || response.body() == null) return;
                        Gson gson = new Gson();
                        LanguageModelResponse pack =
                                gson.fromJson(response.body(), LanguageModelResponse.class);
                        if (pack != null && Boolean.TRUE.equals(pack.getSuccess())) {
                            TranslatorPackHelper.savePack(
                                    getApplicationContext(), languageId, pack);
                            languageModelResponse = pack;
                        }
                    } catch (Exception ignored) {
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    // Home can still refresh via ensureFeedMatchesSelectedLanguage.
                }
            });
        } catch (Exception ignored) {
        }
    }

    /** Step 3: name + email after OTP — pre-fill for existing mobiles. Never "Guest". */
    private void showProfileDetailsUi(ValidateOtpResponse.Data loginData) {
        loginStep = STEP_PROFILE;
        otp_layout.setVisibility(View.GONE);
        resend_layout.setVisibility(View.GONE);
        verify_layout.setVisibility(View.GONE);
        login_detailes.setVisibility(View.VISIBLE);
        if (mobile_card != null) mobile_card.setVisibility(View.GONE);
        if (username_card != null) username_card.setVisibility(View.VISIBLE);
        if (email_card != null) email_card.setVisibility(View.VISIBLE);
        sendotpnew.setVisibility(View.GONE);
        post_layout.setVisibility(View.VISIBLE);
        post_login.setVisibility(View.VISIBLE);
        post_login.setText(getString(R.string.login_continue));

        String prefillName = "";
        String prefillEmail = "";
        if (loginData != null) {
            if (loginData.getUserName() != null) {
                prefillName = loginData.getUserName().trim();
            }
            if (loginData.getEmailId() != null) {
                prefillEmail = loginData.getEmailId().trim();
            }
        }
        if (isBlankOrGuestName(prefillName)) {
            prefillName = "";
        }
        username_et.setText(prefillName);
        emailIdEdittext.setText(prefillEmail);
        username_et.requestFocus();
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(username_et, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private static boolean isBlankOrGuestName(String name) {
        if (name == null) return true;
        String t = name.trim();
        return t.isEmpty() || "guest".equalsIgnoreCase(t);
    }

    private void confirmProfileAndFinish() {
        String name = username_et.getText() != null
                ? username_et.getText().toString().trim() : "";
        String email = emailIdEdittext.getText() != null
                ? emailIdEdittext.getText().toString().trim() : "";
        if (name.isEmpty() || isBlankOrGuestName(name)) {
            Toast.makeText(this, "Please Enter User Name", Toast.LENGTH_SHORT).show();
            username_et.setText("");
            username_et.requestFocus();
            return;
        }
        if (email.isEmpty() || !email.contains("@")) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        post_login.setEnabled(false);
        JsonObject body = buildProfileUpdateRequest(name, email);
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> call = apiService.update_reporter_profile("v3", body);
        call.enqueue(new Callback<EncreptionRequest>() {
            @Override
            public void onResponse(Call<EncreptionRequest> call, retrofit2.Response<EncreptionRequest> response) {
                post_login.setEnabled(true);
                boolean success = false;
                try {
                    EncreptionRequest enc = response.body();
                    if (enc != null && enc.getData() != null) {
                        String decrypted = decrypt(enc.getData());
                        AddProfileResponse profileResponse =
                                new Gson().fromJson(decrypted, AddProfileResponse.class);
                        success = profileResponse != null
                                && Boolean.TRUE.equals(profileResponse.getSuccess());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Persist locally even if sync soft-fails so Profile matches what user typed.
                persistLocalIdentity(name, email);
                if (!success) {
                    Toast.makeText(LoginActivityDetails.this,
                            "Logged in — name sync may retry from Profile",
                            Toast.LENGTH_SHORT).show();
                }
                finishLoginNavigation();
            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                post_login.setEnabled(true);
                persistLocalIdentity(name, email);
                Toast.makeText(LoginActivityDetails.this,
                        "Logged in — update name later from Profile if needed",
                        Toast.LENGTH_SHORT).show();
                finishLoginNavigation();
            }
        });
    }

    private JsonObject buildProfileUpdateRequest(String name, String email) {
        UserProfileRequest requestModel = new UserProfileRequest();
        requestModel.setDraftId(SharedPrefsData.getInt(
                LoginActivityDetails.this, Constants.DraftID, Constants.PREF_NAME));
        requestModel.setUserName(name);
        requestModel.setEmail(email);
        String jsonString = new Gson().toJson(requestModel);
        showLog("Login profile confirm REQUEST " + jsonString);
        String encrpt_data = encrypt(jsonString);
        EncritonRequestData Encriton_Request = new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }

    private void persistLocalIdentity(String name, String email) {
        try {
            allsharedpreference.setEmailID(email);
            SharedPrefsData.putString(getApplicationContext(), Constants.USER_NAME,
                    name, Constants.PREF_NAME);
            SharedPrefsData.putString(getApplicationContext(), Constants.Email,
                    email, Constants.PREF_NAME);
            int loginPct = com.anyNews.anynews.Utils.ProfileAchievementHelper
                    .computePctFromPrefs(getApplicationContext());
            if (loginPct > 0) {
                SharedPrefsData.putString(getApplicationContext(),
                        Constants.PROFILE_ACHIEVED_PCT,
                        String.valueOf(loginPct), Constants.PREF_NAME);
            }
        } catch (Exception ignored) {
        }
    }

    private void finishLoginNavigation() {
        try {
            com.anyNews.anynews.Utils.ReporterProfilePrefetcher.prefetchIfPossible(
                    getApplicationContext());
        } catch (Exception ignored) {
        }
        try {
            com.anyNews.anynews.Utils.WalletHistoryPrefetcher.prefetchIfPossible(
                    getApplicationContext());
        } catch (Exception ignored) {
        }

        if (languageModelResponse != null && languageModelResponse.getData() != null) {
            Toast.makeText(LoginActivityDetails.this,
                    "" + languageModelResponse.getData().getLoginSuccesfully(),
                    Toast.LENGTH_SHORT).show();
        }

        if (pendingOpenBecomeReporter) {
            // Always return to BAR page; not-hiring dialog lives on BAR only (never on login).
            Intent become = new Intent(LoginActivityDetails.this, BecomeAReporterActivity.class);
            become.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(become);
            finish();
            return;
        }
        if (pendingOpenRewards) {
            try {
                Intent rewards = new Intent(
                        LoginActivityDetails.this,
                        com.anyNews.anynews.rewards.RewardsHubActivity.class);
                startActivity(rewards);
            } catch (Exception ignored) {
            }
            finish();
            return;
        }
        Intent intent = new Intent(LoginActivityDetails.this, HomePage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (pendingReturnToProfile) {
            intent.putExtra(HomePage.EXTRA_FOOTER_TAB, R.id.navigation_profile);
        }

        if (getApplicationContext() != null
                && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
            startActivity(intent);
        }
        finish();
    }

    private void focusFirstOtpBox() {
        if (otp_1 == null) {
            return;
        }
        otp_1.post(() -> {
            otp_1.requestFocus();
            InputMethodManager imm =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(otp_1, InputMethodManager.SHOW_IMPLICIT);
            }
            // Some OEMs need a second pass after layout settles.
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isFinishing()) {
                    return;
                }
                otp_1.requestFocus();
                InputMethodManager imm2 =
                        (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm2 != null) {
                    imm2.showSoftInput(otp_1, InputMethodManager.SHOW_IMPLICIT);
                }
            }, 200);
        });
    }

    private void clearOtpBoxes() {
        if (editTexts == null) {
            return;
        }
        otpProgrammaticUpdate = true;
        try {
            for (EditText et : editTexts) {
                if (et != null) {
                    et.setText("");
                }
            }
        } finally {
            otpProgrammaticUpdate = false;
        }
    }

    /** Collect 4 digit boxes → verify once. Manual tap or SMS autofill. */
    private void submitOtpFromBoxes(boolean autoTriggered) {
        if (otpVerifyInFlight || loginStep != STEP_OTP || editTexts == null) {
            return;
        }
        StringBuilder otp = new StringBuilder();
        for (EditText et : editTexts) {
            if (et != null && et.getText() != null) {
                otp.append(et.getText().toString().trim());
            }
        }
        if (otp.length() != 4) {
            if (!autoTriggered) {
                Toast.makeText(LoginActivityDetails.this, "Enter a valid OTP", Toast.LENGTH_SHORT)
                        .show();
            }
            return;
        }
        finalOtp = otp;
        hideOtpSoftKeyboard();
        setOtpVerifyLoading(true);
        Validate();
    }

    private void hideOtpSoftKeyboard() {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }
        for (EditText et : new EditText[]{otp_1, otp_2, otp_3, otp_4}) {
            if (et != null) {
                imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
            }
        }
    }

    private void setOtpVerifyLoading(boolean loading) {
        otpVerifyInFlight = loading;
        if (otp_submit == null) {
            return;
        }
        otp_submit.setEnabled(!loading);
        otp_submit.setClickable(!loading);
        if (editTexts != null) {
            for (EditText et : editTexts) {
                if (et != null) {
                    et.setEnabled(!loading);
                }
            }
        }
        if (loading) {
            startOtpLoadingDots();
        } else {
            stopOtpLoadingDots();
            otp_submit.setText(otpVerifyLabel);
        }
    }

    private void startOtpLoadingDots() {
        stopOtpLoadingDots();
        otpLoadingDotStep = 0;
        otpLoadingDotsRunnable = new Runnable() {
            @Override
            public void run() {
                if (!otpVerifyInFlight || otp_submit == null) {
                    return;
                }
                otpLoadingDotStep = (otpLoadingDotStep % 3) + 1;
                StringBuilder dots = new StringBuilder();
                for (int i = 0; i < otpLoadingDotStep; i++) {
                    dots.append('.');
                }
                otp_submit.setText(dots.toString());
                otpLoadingHandler.postDelayed(this, OTP_LOADING_DOT_MS);
            }
        };
        otp_submit.setText(".");
        otpLoadingHandler.postDelayed(otpLoadingDotsRunnable, OTP_LOADING_DOT_MS);
    }

    private void stopOtpLoadingDots() {
        if (otpLoadingDotsRunnable != null) {
            otpLoadingHandler.removeCallbacks(otpLoadingDotsRunnable);
            otpLoadingDotsRunnable = null;
        }
    }

    private void startResendCooldown() {
        if (resendCountDownTimer != null) {
            resendCountDownTimer.cancel();
            resendCountDownTimer = null;
        }
        setResendEnabled(false);
        resendCountDownTimer = new CountDownTimer(RESEND_COOLDOWN_MS, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = Math.max(1, (millisUntilFinished + 999) / 1000);
                txt_resend_otp.setText(resendOtpLabel.trim() + " (" + seconds + "s)");
            }

            @Override
            public void onFinish() {
                setResendEnabled(true);
            }
        };
        resendCountDownTimer.start();
    }

    private void setResendEnabled(boolean enabled) {
        resendEnabled = enabled;
        txt_resend_otp.setEnabled(enabled);
        txt_resend_otp.setClickable(enabled);
        txt_resend_otp.setAlpha(enabled ? 1f : 0.55f);
        if (enabled) {
            txt_resend_otp.setText(resendOtpLabel);
        }
    }

    private void GenerateOtp() {
        // Prevent double-tap while request is in flight; cooldown restarts on success.
        setResendEnabled(false);
        if (resendCountDownTimer != null) {
            resendCountDownTimer.cancel();
            resendCountDownTimer = null;
        }
        txt_resend_otp.setText(resendOtpLabel);
        // Listen before the SMS is sent. Retriever does not see messages that
        // arrived earlier, and it does not show an Allow sheet.
        startSmsAutoRead();
        JsonObject otp = generateOtpRequest();

        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> otp_generation = apiService.SendOtp("v3", otp);
        otp_generation.enqueue(new Callback<EncreptionRequest>() {

            @Override
            public void onResponse(Call<EncreptionRequest> call, retrofit2.Response<EncreptionRequest> response) {

                EncreptionRequest encreptionRequest = response.body();
                String encrpt_data = null;
                encrpt_data = decrypt(encreptionRequest.getData());
                AppHelper.showLog("ENCRYPTED LOG:"+encrpt_data);

                Gson gsonObj = new Gson();
                GenerateOtpResponse OtpRequest = gsonObj.fromJson(encrpt_data, GenerateOtpResponse.class);


                if (OtpRequest.getSuccess()) {
                    startResendCooldown();
                    focusFirstOtpBox();
                } else {
                    setResendEnabled(true);
                }

            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                setResendEnabled(true);
            }
        });


    }

    private JsonObject generateOtpRequest() {

        GenerateOtpRequest requestModel = new GenerateOtpRequest();

        requestModel.setDraftId(SharedPrefsData.getInt(LoginActivityDetails.this, Constants.DraftID, Constants.PREF_NAME));
        requestModel.setCountryCode("+91");
        requestModel.setMobileNumber(mobile_et.getText().toString());
        // Name/email collected after OTP (step 3); older servers ignore empty values.
        requestModel.setUsername("");
        requestModel.setMessageId("4er54r");
        requestModel.setEmail("");
        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            encrpt_data = encrypt(jsonString);
        }*/
        encrpt_data = encrypt(jsonString);
        showLog("API REQUEST E" + jsonString.toString());
        EncritonRequestData Encriton_Request = new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }

    private void Validate() {
       /* pDialog.setTitle("Loading....");
        pDialog.show();*/

        JsonObject otp = ReadOTP();

        APIService apiService = RestApiClient.getClient().create(APIService.class);

        Call<EncreptionRequest> otp_generation = apiService.ReadOtp(SharedPrefsData.getString(LoginActivityDetails.this, Constants.Version_ID, Constants.PREF_NAME), otp);

        otp_generation.enqueue(new Callback<EncreptionRequest>() {

            @Override
            public void onResponse(Call<EncreptionRequest> call, retrofit2.Response<EncreptionRequest> response) {


                EncreptionRequest encreptionRequest = response.body();
                if (encreptionRequest == null || encreptionRequest.getData() == null) {
                    setOtpVerifyLoading(false);
                    Toast.makeText(LoginActivityDetails.this, "Invalid Otp", Toast.LENGTH_LONG).show();
                    return;
                }
                String encrpt_data = null;
                /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    encrpt_data = decrypt(encreptionRequest.getData());
                }*/
                encrpt_data = decrypt(encreptionRequest.getData());


                Gson gsonObj = new Gson();
                ValidateOtpResponse validateUserOtp = gsonObj.fromJson(encrpt_data, ValidateOtpResponse.class);


                if (validateUserOtp != null && Boolean.TRUE.equals(validateUserOtp.getSuccess())) {
                    setOtpVerifyLoading(false);

                    SharedPrefsData.putBool(getApplicationContext(), Constants.is_login, true, Constants.PREF_NAME);
                    SharedPrefsData.putBool(getApplicationContext(), Constants.FORCE_LOGGED_OUT, false, Constants.PREF_NAME);
                    // Persist phone at OTP success so get_user_posts has mobile after reinstall
                    // before profile API returns (Selected_Phone_Number used by Articles list).
                    String loginMobile = num != null ? String.valueOf(num).trim() : "";
                    if (loginMobile.isEmpty() && mobile_et != null) {
                        loginMobile = mobile_et.getText() != null
                                ? mobile_et.getText().toString().trim() : "";
                    }
                    if (!loginMobile.isEmpty() && !"null".equalsIgnoreCase(loginMobile)) {
                        SharedPrefsData.putString(getApplicationContext(), Constants.Selected_Phone_Number,
                                loginMobile, Constants.PREF_NAME);
                    }
                    // Instant Verified on Profile: persist is_reporter from OTP payload
                    // (do not wait for get_user_profile). Name/email wait for step 3.
                    ValidateOtpResponse.Data loginData = null;
                    try {
                        loginData = validateUserOtp.getData();
                        if (loginData != null && loginData.getUserId() != null && loginData.getUserId() > 0) {
                            SharedPrefsData.putLong(getApplicationContext(), Constants.USER_ID,
                                    loginData.getUserId().longValue(), Constants.PREF_NAME);
                        }
                        if (loginData != null && Boolean.TRUE.equals(loginData.getIsReporter())) {
                            SharedPrefsData.putBoolImmediate(getApplicationContext(),
                                    Constants.is_reporter, true, Constants.PREF_NAME);
                        } else if (loginData != null && Boolean.FALSE.equals(loginData.getIsReporter())) {
                            SharedPrefsData.putBoolImmediate(getApplicationContext(),
                                    Constants.is_reporter, false, Constants.PREF_NAME);
                        }
                        if (loginData != null && loginData.getProfilePic() != null
                                && !loginData.getProfilePic().trim().isEmpty()) {
                            SharedPrefsData.putString(getApplicationContext(), Constants.PROFILE_PIC_URL,
                                    loginData.getProfilePic().trim(), Constants.PREF_NAME);
                        }
                        // Seed local prefs from server prefills so Profile is not blank
                        // if user backs out before Continue (existing accounts).
                        if (loginData != null) {
                            String sn = loginData.getUserName() != null
                                    ? loginData.getUserName().trim() : "";
                            String se = loginData.getEmailId() != null
                                    ? loginData.getEmailId().trim() : "";
                            if (!sn.isEmpty() && !"guest".equalsIgnoreCase(sn)) {
                                SharedPrefsData.putString(getApplicationContext(),
                                        Constants.USER_NAME, sn, Constants.PREF_NAME);
                            }
                            if (se.contains("@")) {
                                allsharedpreference.setEmailID(se);
                                SharedPrefsData.putString(getApplicationContext(),
                                        Constants.Email, se, Constants.PREF_NAME);
                            }
                            // Switch device geography to this account (clear previous
                            // user's Kadapa / language prefs left behind after logout).
                            applyAccountGeographyFromLogin(loginData);
                        }
                    } catch (Exception ignored) {
                    }

                    pendingReturnToProfile = getIntent() != null
                            && getIntent().getBooleanExtra(EXTRA_RETURN_TO_PROFILE, false);
                    pendingOpenBecomeReporter = getIntent() != null
                            && getIntent().getBooleanExtra(EXTRA_OPEN_BECOME_A_REPORTER, false);
                    pendingOpenRewards = getIntent() != null
                            && getIntent().getBooleanExtra(EXTRA_OPEN_REWARDS, false);
                    try {
                        // Sticky BAR flag only when no other login source was set.
                        // Never let a prior BAR attempt leak into Profile/Rewards login.
                        if (!pendingOpenBecomeReporter
                                && !pendingOpenRewards
                                && !pendingReturnToProfile) {
                            pendingOpenBecomeReporter = SharedPrefsData.getBool(
                                    getApplicationContext(),
                                    Constants.LOGIN_SOURCE_BECOME_REPORTER,
                                    Constants.PREF_NAME);
                        }
                        SharedPrefsData.putBool(getApplicationContext(),
                                Constants.LOGIN_SOURCE_BECOME_REPORTER, false, Constants.PREF_NAME);
                    } catch (Exception ignored) {
                    }

                    // Step 3: collect / confirm name + email (pre-filled for existing users).
                    showProfileDetailsUi(loginData);


                } else {

                    setOtpVerifyLoading(false);
                    Toast.makeText(LoginActivityDetails.this, "Invalid Otp", Toast.LENGTH_LONG).show();

                }


            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {


                setOtpVerifyLoading(false);
                Toast.makeText(LoginActivityDetails.this, "Invalid Otp", Toast.LENGTH_LONG).show();
                clearOtpBoxes();
                if (otp_1 != null) {
                    otp_1.requestFocus();
                }

            }
        });

    }


    @Override
    public void onBackPressed() {
        if (loginStep == STEP_OTP) {
            // Back to mobile entry without finishing.
            setOtpVerifyLoading(false);
            loginStep = STEP_MOBILE;
            otp_layout.setVisibility(View.GONE);
            resend_layout.setVisibility(View.GONE);
            verify_layout.setVisibility(View.GONE);
            login_detailes.setVisibility(View.VISIBLE);
            if (mobile_card != null) mobile_card.setVisibility(View.VISIBLE);
            if (username_card != null) username_card.setVisibility(View.GONE);
            if (email_card != null) email_card.setVisibility(View.GONE);
            clearOtpBoxes();
            if (mobile_et.getText() != null && mobile_et.getText().toString().length() >= 10) {
                post_layout.setVisibility(View.GONE);
                post_login.setVisibility(View.GONE);
                sendotpnew.setVisibility(View.VISIBLE);
            } else {
                post_layout.setVisibility(View.VISIBLE);
                post_login.setVisibility(View.VISIBLE);
                sendotpnew.setVisibility(View.GONE);
                if (languageModelResponse != null && languageModelResponse.getData() != null) {
                    post_login.setText(" " + languageModelResponse.getData().getSendOtp());
                }
            }
            return;
        }
        if (loginStep == STEP_PROFILE) {
            // Already authenticated; leave with current (prefilled) identity.
            finishLoginNavigation();
            return;
        }
        super.onBackPressed();
        finish();
    }

    private void loadFragment(Fragment fragment) {


        Intent intent = new Intent("HOME_PAGR");
        intent.putExtra("message", false);//("message", click_event);
        LocalBroadcastManager.getInstance(LoginActivityDetails.this).sendBroadcast(intent);


    }

    private JsonObject ReadOTP() {

        ValidateOtpRequest requestModel = new ValidateOtpRequest();

        requestModel.setDraftId(SharedPrefsData.getInt(LoginActivityDetails.this, Constants.DraftID, Constants.PREF_NAME));
        requestModel.setCountryCode("+91");
        requestModel.setMobileNumber("" + num);
        requestModel.setOtp(finalOtp.toString());

        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            encrpt_data = encrypt(jsonString);
        }*/
        encrpt_data = encrypt(jsonString);
        showLog("API REQUEST E" + jsonString.toString());
        EncritonRequestData Encriton_Request = new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }

    public class PinTextWatcher implements TextWatcher {

        private int currentIndex;
        private boolean isFirst = false, isLast = false;
        private String newTypedString = "";

        PinTextWatcher(int currentIndex) {
            this.currentIndex = currentIndex;

            if (currentIndex == 0)
                this.isFirst = true;
            else if (currentIndex == editTexts.length - 1)
                this.isLast = true;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            newTypedString = s.subSequence(start, start + count).toString().trim();
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (otpProgrammaticUpdate || otpVerifyInFlight) {
                return;
            }

            String text = newTypedString;

            // Paste / autofill of full OTP into one box → distribute digits.
            if (text.length() >= 4) {
                fillOtpDigits(text);
                return;
            }
            if (text.length() > 1)
                text = String.valueOf(text.charAt(0));

            editTexts[currentIndex].removeTextChangedListener(this);
            editTexts[currentIndex].setText(text);
            editTexts[currentIndex].setSelection(text.length());
            editTexts[currentIndex].addTextChangedListener(this);

            if (text.length() == 1)
                moveToNext();
            else if (text.length() == 0)
                moveToPrevious();
        }

        private void moveToNext() {
            if (!isLast)
                editTexts[currentIndex + 1].requestFocus();

            if (isAllEditTextsFilled() && isLast) { // isLast is optional
                editTexts[currentIndex].setFocusable(true);
//                hideKeyboard();
            }
        }

        private void moveToPrevious() {
            if (!isFirst)
                editTexts[currentIndex - 1].requestFocus();
        }

        private boolean isAllEditTextsFilled() {
            for (EditText editText : editTexts)
                if (editText.getText().toString().trim().length() == 0)
                    return false;
            return true;
        }


    }

    public class PinOnKeyListener implements View.OnKeyListener {

        private int currentIndex;

        PinOnKeyListener(int currentIndex) {
            this.currentIndex = currentIndex;
        }

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (editTexts[currentIndex].getText().toString().isEmpty() && currentIndex != 0)
                    editTexts[currentIndex - 1].requestFocus();
            }
            return false;
        }

    }

    private void startSmsAutoRead() {
        logAppSignatureForSmsRetriever();
        // SMS Retriever reads one matching SMS silently (no Allow sheet).
        // The OTP body must end with this build's 11-character app hash.
        SmsRetrieverClient client = SmsRetriever.getClient(this);
        client.startSmsRetriever()
                .addOnSuccessListener(aVoid -> Log.d(TAG_OTP, "SMS Retriever started"))
                .addOnFailureListener(e ->
                        Log.w(TAG_OTP, "SMS Retriever failed; user can type the OTP", e));
    }

    private void logAppSignatureForSmsRetriever() {
        try {
            ArrayList<String> hashes = new AppSignatureHelper(this).getAppSignatures();
            for (String hash : hashes) {
                Log.i(TAG_OTP, "Append this hash to OTP SMS for Retriever autofill: " + hash);
                AppHelper.showLog("SMS_RETRIEVER_APP_HASH=" + hash);
            }
        } catch (Exception e) {
            Log.w(TAG_OTP, "Could not compute app signature hash", e);
        }
    }

    private void getOtpFromMessage(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        Log.d(TAG_OTP, "SMS received for OTP parse, length=" + message.length());
        // Prefer "Your OTP for login is 7535" style (avoids Message-ID digits).
        Matcher preferred = Pattern.compile("(?i)(?:otp)\\D{0,40}?(\\d{4})").matcher(message);
        if (preferred.find()) {
            fillOtpDigits(preferred.group(1));
            return;
        }
        Matcher fallback = Pattern.compile("\\b(\\d{4})\\b").matcher(message);
        if (fallback.find()) {
            fillOtpDigits(fallback.group(1));
        }
    }

    private void fillOtpDigits(String otp) {
        if (editTexts == null || otp == null || otpVerifyInFlight) {
            return;
        }
        String digits = otp.replaceAll("\\D", "");
        if (digits.length() < 4) {
            return;
        }
        otpProgrammaticUpdate = true;
        try {
            otp_1.setText(String.valueOf(digits.charAt(0)));
            otp_2.setText(String.valueOf(digits.charAt(1)));
            otp_3.setText(String.valueOf(digits.charAt(2)));
            otp_4.setText(String.valueOf(digits.charAt(3)));
        } finally {
            otpProgrammaticUpdate = false;
        }
        otp_4.requestFocus();
        if (otp_4.getText() != null) {
            otp_4.setSelection(otp_4.getText().length());
        }
        // Silent fill → auto-verify (same path as tapping Verify, including loading dots).
        otp_4.post(() -> submitOtpFromBoxes(true));
    }

    private void registerBroadcastReceiver() {
        if (smsReceiverRegistered) {
            return;
        }
        smsBroadcastReceiver = new SmsBroadcastReceiver();
        smsBroadcastReceiver.smsBroadcastReceiverListener =
                new SmsBroadcastReceiver.SmsBroadcastReceiverListener() {
                    @Override
                    public void onSmsMessageReceived(String message) {
                        getOtpFromMessage(message);
                    }

                    @Override
                    public void onFailure() {
                        Log.d(TAG_OTP, "SMS Retriever timed out or failed; manual OTP still available");
                    }
                };
        IntentFilter intentFilter = new IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION);
        // Must be exported so Play Services can deliver SMS_RETRIEVED_ACTION (API 33+).
        ContextCompat.registerReceiver(
                this,
                smsBroadcastReceiver,
                intentFilter,
                ContextCompat.RECEIVER_EXPORTED);
        smsReceiverRegistered = true;
    }
    @Override
    protected void onStart() {
        super.onStart();
        // Re-apply after AppController.onActivityCreated so light-mode status bar stays correct.
        applyLoginSystemBars();
        registerBroadcastReceiver();
    }

    /**
     * Keep content below the status bar and match bar colors/icons to light vs dark theme.
     * Re-applied after AppController so this screen stays correct if global bars change.
     */
    private void applyLoginSystemBars() {
        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, true);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        int bg = ContextCompat.getColor(this, R.color.background_color);
        window.setStatusBarColor(bg);
        window.setNavigationBarColor(bg);

        boolean lightMode = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES;

        WindowInsetsControllerCompat insetsController =
                WindowCompat.getInsetsController(window, window.getDecorView());
        if (insetsController != null) {
            insetsController.setAppearanceLightStatusBars(lightMode);
            insetsController.setAppearanceLightNavigationBars(lightMode);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (smsReceiverRegistered && smsBroadcastReceiver != null) {
            try {
                unregisterReceiver(smsBroadcastReceiver);
            } catch (IllegalArgumentException ignored) {
            }
            smsReceiverRegistered = false;
        }
    }

    @Override
    protected void onDestroy() {
        if (resendCountDownTimer != null) {
            resendCountDownTimer.cancel();
            resendCountDownTimer = null;
        }
        stopOtpLoadingDots();
        super.onDestroy();
    }


}