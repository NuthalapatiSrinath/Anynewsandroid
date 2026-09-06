package com.anyNews.anynews.Activities;

import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.anyNews.anynews.Utils.ProfileHubConfigHelper;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.Utils.SmsBroadcastReceiver;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
    private static final int REQ_USER_CONSENT = 200;
    private static final long RESEND_COOLDOWN_MS = 45_000L;
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

                StringBuilder otp = new StringBuilder();
                for (int i = 0; i < editTexts.length; i++) {
                    otp = otp.append(editTexts[i].getText().toString().trim());
                }
                if (otp.toString().length() == 4) {
                    finalOtp = otp;

                    ((InputMethodManager) LoginActivityDetails.this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(otp_1.getWindowToken(), 0);
                    ((InputMethodManager) LoginActivityDetails.this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(otp_2.getWindowToken(), 0);
                    ((InputMethodManager) LoginActivityDetails.this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(otp_3.getWindowToken(), 0);
                    ((InputMethodManager) LoginActivityDetails.this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(otp_4.getWindowToken(), 0);

                    Validate();

                } else {

                    Toast.makeText(LoginActivityDetails.this, "Enter a valid OTP", Toast.LENGTH_SHORT).show();

                }


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
        otp_submit.setText(" " + languageModelResponse.getData().getVerify());
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

    /** Step 3: name + email after OTP — pre-fill for existing mobiles. */
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
        username_et.setText(prefillName);
        emailIdEdittext.setText(prefillEmail);
        username_et.requestFocus();
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(username_et, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void confirmProfileAndFinish() {
        String name = username_et.getText() != null
                ? username_et.getText().toString().trim() : "";
        String email = emailIdEdittext.getText() != null
                ? emailIdEdittext.getText().toString().trim() : "";
        if (name.isEmpty()) {
            Toast.makeText(this, "Please Enter User Name", Toast.LENGTH_SHORT).show();
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
            if (!ProfileHubConfigHelper.isBecomeAReporterEnabled(this)) {
                // Hiring OFF: convert guest via OTP, then show not-hiring message — no BAR form.
                try {
                    String msg = ProfileHubConfigHelper.getNotHiringMessage(this);
                    new AlertDialog.Builder(this)
                            .setMessage(msg)
                            .setPositiveButton(android.R.string.ok, (d, w) -> {
                                Intent intent = new Intent(
                                        LoginActivityDetails.this, HomePage.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                                        | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                if (pendingReturnToProfile) {
                                    intent.putExtra(
                                            HomePage.EXTRA_FOOTER_TAB, R.id.navigation_profile);
                                }
                                startActivity(intent);
                                finish();
                            })
                            .setCancelable(false)
                            .show();
                } catch (Exception e) {
                    Toast.makeText(this,
                            ProfileHubConfigHelper.getNotHiringMessage(this),
                            Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(LoginActivityDetails.this, HomePage.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    if (pendingReturnToProfile) {
                        intent.putExtra(HomePage.EXTRA_FOOTER_TAB, R.id.navigation_profile);
                    }
                    startActivity(intent);
                    finish();
                }
                return;
            }
            Intent become = new Intent(LoginActivityDetails.this, BecomeAReporterActivity.class);
            become.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
        for (EditText et : editTexts) {
            if (et != null) {
                et.setText("");
            }
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
                    startSmsAutoRead();
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
                String encrpt_data = null;
                /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    encrpt_data = decrypt(encreptionRequest.getData());
                }*/
                encrpt_data = decrypt(encreptionRequest.getData());


                Gson gsonObj = new Gson();
                ValidateOtpResponse validateUserOtp = gsonObj.fromJson(encrpt_data, ValidateOtpResponse.class);


                if (validateUserOtp.getSuccess()) {

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
                            if (!sn.isEmpty()) {
                                SharedPrefsData.putString(getApplicationContext(),
                                        Constants.USER_NAME, sn, Constants.PREF_NAME);
                            }
                            if (se.contains("@")) {
                                allsharedpreference.setEmailID(se);
                                SharedPrefsData.putString(getApplicationContext(),
                                        Constants.Email, se, Constants.PREF_NAME);
                            }
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
                        if (!pendingOpenBecomeReporter) {
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

                    Toast.makeText(LoginActivityDetails.this, "Invalid Otp", Toast.LENGTH_LONG).show();

                }


            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {


                Toast.makeText(LoginActivityDetails.this, "Invalid Otp", Toast.LENGTH_LONG).show();
                otp_1.setText("");
                otp_2.setText("");
                otp_3.setText("");
                otp_4.setText("");
                otp_1.requestFocus();

            }
        });

    }


    @Override
    public void onBackPressed() {
        if (loginStep == STEP_OTP) {
            // Back to mobile entry without finishing.
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
        // Prefer SMS Retriever (auto-fill, no READ_SMS). Falls back to User Consent.
        SmsRetrieverClient client = SmsRetriever.getClient(this);
        client.startSmsRetriever()
                .addOnSuccessListener(aVoid -> Log.d(TAG_OTP, "SMS Retriever started"))
                .addOnFailureListener(e -> {
                    Log.w(TAG_OTP, "SMS Retriever failed, starting User Consent", e);
                    startSmsUserConsent();
                });
        // Also start User Consent so OTP can still be approved if SMS lacks app hash.
        startSmsUserConsent();
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

    private void startSmsUserConsent() {
        SmsRetrieverClient client = SmsRetriever.getClient(this);
        client.startSmsUserConsent(null).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG_OTP, "SMS User Consent started");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG_OTP, "SMS User Consent failed", e);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_USER_CONSENT) {
            if ((resultCode == RESULT_OK) && (data != null)) {
                String message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE);
                getOtpFromMessage(message);
            }
        }
    }
    private void getOtpFromMessage(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        Pattern pattern = Pattern.compile("(|^)\\d{4}");
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            String text = matcher.group(0);
            if (text != null && text.length() >= 4) {
                fillOtpDigits(text);
            }
        }
    }

    private void fillOtpDigits(String otp) {
        if (editTexts == null || otp == null) {
            return;
        }
        String digits = otp.replaceAll("\\D", "");
        if (digits.length() < 4) {
            return;
        }
        // Avoid recursive TextWatcher loops while distributing digits.
        otp_1.setText(String.valueOf(digits.charAt(0)));
        otp_2.setText(String.valueOf(digits.charAt(1)));
        otp_3.setText(String.valueOf(digits.charAt(2)));
        otp_4.setText(String.valueOf(digits.charAt(3)));
        otp_4.requestFocus();
        if (otp_4.getText() != null) {
            otp_4.setSelection(otp_4.getText().length());
        }
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
                    public void onConsentIntent(Intent intent) {
                        startActivityForResult(intent, REQ_USER_CONSENT);
                    }

                    @Override
                    public void onFailure() {
                        Log.d(TAG_OTP, "SMS auto-read timed out or failed");
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
        super.onDestroy();
    }


}