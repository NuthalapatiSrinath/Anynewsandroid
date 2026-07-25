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
import android.text.Editable;
import android.text.TextWatcher;
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
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.GenerateOtpRequest;
import com.anyNews.anynews.Pojo.GenerateOtpResponse;
import com.anyNews.anynews.Pojo.LanguageModelResponse;
import com.anyNews.anynews.Pojo.ValidateOtpRequest;
import com.anyNews.anynews.Pojo.ValidateOtpResponse;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.AppHelper;
import com.anyNews.anynews.Utils.CommonMethods;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.Utils.SmsBroadcastReceiver;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import soup.neumorphism.NeumorphCardView;

public class LoginActivityDetails extends AppCompatActivity {

    NeumorphCardView neumorph_next;
    EditText username_et, mobile_et,emailIdEdittext;
    EditText otp_1, otp_2, otp_3, otp_4;
    TextView post_login, otpnew;
    TextView txt_mobile_otp, txt_resend_otp;
    TextView otp_submit;

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
    SmsBroadcastReceiver smsBroadcastReceiver;
    EditText otpNew;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_details);
        applyLoginSystemBars();
        neumorph_next = findViewById(R.id.neumorph_next);
        txt_resend_otp = findViewById(R.id.txt_resend_otp);
        emailIdEdittext = findViewById(R.id.emailId);
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
                if(username_et.getText().toString().length()<=0){
                    Toast.makeText(LoginActivityDetails.this, "Please Enter User Name", Toast.LENGTH_SHORT).show();
                }else if (mobile_et.getText().toString().length() <=0) {
                    Toast.makeText(LoginActivityDetails.this, "Please Enter Mobile Number", Toast.LENGTH_SHORT).show();
                }else if (mobile_et.getText().toString().length() <10) {
                    Toast.makeText(LoginActivityDetails.this, "Please Enter Valid Mobile Number", Toast.LENGTH_SHORT).show();
                }else if(emailIdEdittext.getText().toString().length()<=0){
                    Toast.makeText(LoginActivityDetails.this, "EmailId should not be empty", Toast.LENGTH_SHORT).show();
                }else{
                    num = mobile_et.getText().toString();
                    GenerateOtp();
                    otp_layout.setVisibility(View.VISIBLE);
                    resend_layout.setVisibility(View.VISIBLE);
                    verify_layout.setVisibility(View.VISIBLE);
                    login_detailes.setVisibility(View.GONE);
                }
            }
        });


        otpnew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (username_et.getText().toString().length() >= 1) {


                    if (mobile_et.getText().toString().length() >= 10) {
                        if(emailIdEdittext.getText().toString().length()<=0){
                            Toast.makeText(LoginActivityDetails.this, "EmailId should not be empty", Toast.LENGTH_SHORT).show();
                        }else{
                            num = mobile_et.getText().toString();
                            GenerateOtp();
                            otp_layout.setVisibility(View.VISIBLE);
                            resend_layout.setVisibility(View.VISIBLE);
                            verify_layout.setVisibility(View.VISIBLE);
                            login_detailes.setVisibility(View.GONE);
                        }



                    } else {

                        Toast.makeText(LoginActivityDetails.this, "Plese Enter User Name", Toast.LENGTH_SHORT).show();

                    }


                } else {
                    if (mobile_et.getText().toString().length() >= 10) {

                        if (username_et.getText().toString().length() >= 1) {

                            num = mobile_et.getText().toString();
                            GenerateOtp();
                            otp_layout.setVisibility(View.VISIBLE);
                            resend_layout.setVisibility(View.VISIBLE);
                            verify_layout.setVisibility(View.VISIBLE);
                            login_detailes.setVisibility(View.GONE);

                        } else {

                            Toast.makeText(LoginActivityDetails.this, "Plese Enter User Name", Toast.LENGTH_SHORT).show();

                        }


                        // post_login.setBackgroundColor(Color.BLUE);

                    } else if (mobile_et.getText().toString().length() >= 10) {

                        Toast.makeText(LoginActivityDetails.this, "Please Enter Mobile Number", Toast.LENGTH_SHORT).show();


                        //          commonMethods.showerror("Please enter mobile number");
                    } else {
                        Toast.makeText(LoginActivityDetails.this, "Plese Enter User Name", Toast.LENGTH_SHORT).show();

//                        commonMethods.showerror("Mobile number must be atleast 10 digits");
                    }
                }

            }
        });

        txt_resend_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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


        otp_1.requestFocus();

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
                if(allsharedpreference.getEmailId().equals("")){
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
        txt_resend_otp.setText(" " + languageModelResponse.getData().getResendOtp());
        //Toast.makeText(LoginActivityDetails.this, ""+languageModelResponse.getData().getSendOtp(), Toast.LENGTH_SHORT).show();


    }

    private void GenerateOtp() {
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

                    startSmsUserConsent();

                } else {


                }

            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {

            }
        });


    }

    private JsonObject generateOtpRequest() {

        GenerateOtpRequest requestModel = new GenerateOtpRequest();

        requestModel.setDraftId(SharedPrefsData.getInt(LoginActivityDetails.this, Constants.DraftID, Constants.PREF_NAME));
        requestModel.setCountryCode("+91");
        requestModel.setMobileNumber(mobile_et.getText().toString());
        requestModel.setUsername(username_et.getText().toString());
        requestModel.setMessageId("4er54r");
        requestModel.setEmail(emailIdEdittext.getText().toString().trim());
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

                    allsharedpreference.setEmailID(emailIdEdittext.getText().toString());
                    SharedPrefsData.putBool(getApplicationContext(), Constants.is_login, true, Constants.PREF_NAME);

                    Toast.makeText(LoginActivityDetails.this, "" + languageModelResponse.getData().getLoginSuccesfully(), Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivityDetails.this, HomePage.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)){
                       startActivity(intent);
                    }
                    finish();


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
        super.onBackPressed();


       /* Intent intent = new Intent(LoginActivityDetails.this, HomePage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(intent);*/
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

            //    Detect paste event and set first char
            if (text.length() > 1)
                text = String.valueOf(text.charAt(0)); // TODO: We can fill out other EditTexts

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

    private void startSmsUserConsent() {
        //otpNew.setVisibility(View.VISIBLE);
        //otpNew.requestFocus();

        SmsRetrieverClient client = SmsRetriever.getClient(this);
        //We can add sender phone number or leave it blank
        // I'm adding null here
        client.startSmsUserConsent(null).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //Toast.makeText(getApplicationContext(), "On Success", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "On OnFailure", Toast.LENGTH_LONG).show();
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_USER_CONSENT) {
            if ((resultCode == RESULT_OK) && (data != null)) {
                //That gives all message to us.
                // We need to get the code from inside with regex
                String message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE);
                //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                /*textViewMessage.setText(
                        String.format("%s - %s", getString(R.string.received_message), message));*/
                getOtpFromMessage(message);
            }
        }
    }
    private void getOtpFromMessage(String message) {
        // This will match any 6 digit number in the message
        int currentIndex = 0;
        Pattern pattern = Pattern.compile("(|^)\\d{4}");
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            String text = matcher.group(0);
            //otp_1.setText(matcher.group(0));
            assert text != null;
            otp_1.setText(String.valueOf(text.charAt(0)));
            otp_2.setText(String.valueOf(text.charAt(1)));
            otp_3.setText(String.valueOf(text.charAt(2)));
            otp_4.setText(String.valueOf(text.charAt(3)));
        }
    }

    private void registerBroadcastReceiver() {
        smsBroadcastReceiver = new SmsBroadcastReceiver();
        smsBroadcastReceiver.smsBroadcastReceiverListener =
                new SmsBroadcastReceiver.SmsBroadcastReceiverListener() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, REQ_USER_CONSENT);
                    }
                    @Override
                    public void onFailure() {
                    }
                };
        IntentFilter intentFilter = new IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION);
        registerReceiver(smsBroadcastReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
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
        unregisterReceiver(smsBroadcastReceiver);
    }


}