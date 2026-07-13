package com.anyNews.anynews.Activities;

import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.anyNews.anynews.Utils.CropHelper;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageView;
import com.squareup.picasso.Picasso;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.LanguageModelResponse;
import com.anyNews.anynews.Pojo.ProfileUpdateRequiest;
import com.anyNews.anynews.Pojo.SingleArticleRequest;
import com.anyNews.anynews.Pojo.SingleArticleResponse;
import com.anyNews.anynews.Pojo.UpdateImageResponse;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.CommonUtilities;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.AppHelper;
import com.anyNews.anynews.Utils.CommonMethods;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.Logone;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.holder.GpsTracker;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.ExplainReasonCallback;
import com.permissionx.guolindev.callback.ForwardToSettingsCallback;
import com.permissionx.guolindev.callback.RequestCallback;
import com.permissionx.guolindev.request.ExplainScope;
import com.permissionx.guolindev.request.ForwardScope;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.App.getContext;
import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

public class PostArticles extends AppCompatActivity {

    ImageView img_upload_photo;
    private int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_IMAGE_REQUEST_CAMARA = 700;
    private static final int STORAGE_PERMISSION_CODE = 123;
    private Uri filePath;
    private Bitmap bitmap;
    private String ImageId = "";


    ImageView img_back;
    EditText edt_news_headlines;

    private GpsTracker gpsTracker;
    static double latitude;
    static double longitude;
    Button button;
    boolean CHECK_VALUE = false;
    CommonMethods mCommonmethods;
    LinearLayout btn_upload;
    EditText edt_news_description;
    CheckBox checked_terms_conditions;
    TextView txt_select_language, news_descrip, upload_image, post_button;
    ArrayAdapter<String> adapter_state;
    Allsharedpreference prefShare;
    LanguageModelResponse languageModelResponse;
    LinearLayout img_upload_photo_total;
    ProgressBar progressBarLarge, image_upload_progress_bar;
    TextView tv_agree_view;
    TextView txt_title_count, txt_desc_count;

    // Edit / resubmit rejected article support
    boolean editMode = false;
    int editUserPostId = 0;
    boolean imageChanged = false;

    private ActivityResultLauncher<CropImageContractOptions> cropImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_artical_image);
        edt_news_headlines = findViewById(R.id.edt_news_headlines);

        edt_news_description = findViewById(R.id.edt_news_description);
        img_back = findViewById(R.id.img_back);
        img_upload_photo = findViewById(R.id.img_upload_photo);
        img_upload_photo_total = findViewById(R.id.img_upload_photo_total);
        btn_upload = findViewById(R.id.btn_upload);
        checked_terms_conditions = findViewById(R.id.checked_terms_conditions);
        txt_select_language = findViewById(R.id.txt_select_language);
        news_descrip = findViewById(R.id.news_descrip);
        upload_image = findViewById(R.id.upload_image);
        post_button = findViewById(R.id.post_button);
        progressBarLarge = findViewById(R.id.progressBarLarge);
        image_upload_progress_bar = findViewById(R.id.image_upload_progress_bar);
        tv_agree_view = findViewById(R.id.tv_agree_view);
        txt_title_count = findViewById(R.id.txt_title_count);
        txt_desc_count = findViewById(R.id.txt_desc_count);
        mCommonmethods = new CommonMethods(PostArticles.this);

        // Register the image crop launcher (feature: crop while uploading photos).
        cropImage = registerForActivityResult(new CropImageContract(), new ActivityResultCallback<com.canhub.cropper.CropImageView.CropResult>() {
            @Override
            public void onActivityResult(com.canhub.cropper.CropImageView.CropResult result) {
                if (result != null && result.isSuccessful()) {
                    handleCroppedImage(result.getUriContent());
                } else if (result != null && result.getError() != null) {
                    Toast.makeText(PostArticles.this, "Crop failed: " + result.getError().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        String value = "" + getIntent().getStringExtra("POST_VIDEO");

        // Feature: reporters can freely edit their news content (selection / copy / paste
        // were previously disabled on both fields, preventing corrections before submit).
        edt_news_headlines.setLongClickable(true);
        edt_news_headlines.setTextIsSelectable(true);
        edt_news_headlines.setCustomSelectionActionModeCallback(null);
        edt_news_description.setLongClickable(true);
        edt_news_description.setTextIsSelectable(true);
        edt_news_description.setCustomSelectionActionModeCallback(null);

        setupLiveCounters();


        if (value.equals("1") || value.equals("null")) {

            img_upload_photo.setImageResource(R.drawable.ic_video_icon);
            //Glide.with(this).load(R.drawable.ic_video_icon).into(img_upload_photo);
            upload_image.setText("Upload Video");

        } else {


            img_upload_photo.setImageResource(R.drawable.ic_new_image_icon);
            upload_image.setText("Upload Image");
        }


        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        img_upload_photo_total.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppHelper.showLog("img_upload_photo_total onclick PERMISSION");

                requestStoragePermission();
            }
        });
        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (edt_news_headlines.getText().toString().length() == 0) {
                    edt_news_headlines.setTextIsSelectable(false);


                    Toast.makeText(PostArticles.this, "" + languageModelResponse.getData().getEnterTitle(), Toast.LENGTH_SHORT).show();
                    showLog(" " + languageModelResponse.getData().getWriteYourNewsTitleHere());

                } else {

                    if (edt_news_description.getText().toString().length() == 0) {
                        edt_news_description.setTextIsSelectable(false);

                        Toast.makeText(PostArticles.this, " " + languageModelResponse.getData().getEnterDescription(), Toast.LENGTH_SHORT).show();

                    } else {

                        if (ImageId == "") {

                            Toast.makeText(PostArticles.this, "" + languageModelResponse.getData().getUploadImage(), Toast.LENGTH_SHORT).show();

                        } else {

                            if (checked_terms_conditions.isChecked()) {

                                sendpostdata();


                            } else {
                                Toast.makeText(PostArticles.this, "" + languageModelResponse.getData().getTermsAndConditions(), Toast.LENGTH_SHORT).show();


                            }
                        }
                    }
                }
            }
        });
        tv_agree_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), ViewTermsAndCondition.class);
                startActivity(intent);
            }
        });

        getLocation();
        setStaticdata();
        prefillForEditMode();
    }


    private void setStaticdata() {
        prefShare = new Allsharedpreference(this);
        Gson gson = new Gson();
        String Translation = prefShare.getShared_String(Allsharedpreference.Translator);
        languageModelResponse = gson.fromJson(Translation, LanguageModelResponse.class);
        checked_terms_conditions.setText(" " + languageModelResponse.getData().getIAgreeToTermsAndConditions());
        txt_select_language.setText(" " + languageModelResponse.getData().getNewsHeadline());
        edt_news_headlines.setHint(" " + languageModelResponse.getData().getWriteYourNewsTitleHere());
        news_descrip.setText(" " + languageModelResponse.getData().getNewsDescription());
        edt_news_description.setHint(" " + languageModelResponse.getData().getDescriptionOverHere());
        upload_image.setText(" " + languageModelResponse.getData().getUploadImage());
        post_button.setText(" " + languageModelResponse.getData().getPost());
    }

    private void requestStoragePermission() {
        //Initialize Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AppHelper.showLog("PERMISSION");
            if (ContextCompat.checkSelfPermission(PostArticles.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(PostArticles.this,
                    Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(PostArticles.this,
                    Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED)  {
                //Location Permission already granted
                AppHelper.showLog("PERMISSION GRANTED +++++");
                showFileChooser();
            } else {
                //Request Location Permission
                AppHelper.showLog("NO PERMISSION");
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU){

                    PermissionX.init(PostArticles.this)
                            .permissions(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA)
                            .onExplainRequestReason(new ExplainReasonCallback() {
                                @Override
                                public void onExplainReason(@NonNull ExplainScope scope, @NonNull List<String> deniedList) {
                                    scope.showRequestReasonDialog(deniedList, "Please allow the below necessary permissions to upload photos and videos", "OK", "Cancel");
                                }
                            })
                            .onForwardToSettings(new ForwardToSettingsCallback() {
                                @Override
                                public void onForwardToSettings(@NonNull ForwardScope scope, @NonNull List<String> deniedList) {
                                    scope.showForwardToSettingsDialog(deniedList, "Please allow the below necessary permissions manually from settings page to upload photos and videos", "OK", "Cancel");

                                }
                            })
                            .request(new RequestCallback() {
                                @Override
                                public void onResult(boolean allGranted, @NonNull List<String> grantedList, @NonNull List<String> deniedList) {
                                    if (allGranted) {
                                        showFileChooser();
                                      //  Toast.makeText(PostArticles.this, "All permissions are granted", Toast.LENGTH_LONG).show();
                                    } else {
                                      //  Toast.makeText(PostArticles.this, "These permissions are denied___:"+deniedList, Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
//                    requestPermissions(
//                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
//                            STORAGE_PERMISSION_CODE);
                }else{

                    PermissionX.init(PostArticles.this)
                            .permissions(Manifest.permission.READ_MEDIA_IMAGES,Manifest.permission.CAMERA)
                            .onExplainRequestReason(new ExplainReasonCallback() {
                                @Override
                                public void onExplainReason(@NonNull ExplainScope scope, @NonNull List<String> deniedList) {
                                    scope.showRequestReasonDialog(deniedList, "Please allow the below necessary permissions to upload photos and videos", "OK", "Cancel");
                                }
                            })
                            .onForwardToSettings(new ForwardToSettingsCallback() {
                                @Override
                                public void onForwardToSettings(@NonNull ForwardScope scope, @NonNull List<String> deniedList) {
                                    scope.showForwardToSettingsDialog(deniedList, "Please allow the below necessary permissions manually from settings page to upload photos and videos", "OK", "Cancel");

                                }
                            })

                            .request(new RequestCallback() {
                                @Override
                                public void onResult(boolean allGranted, @NonNull List<String> grantedList, @NonNull List<String> deniedList) {
                                    if (allGranted) {
                                        showFileChooser();
                                        //Toast.makeText(PostArticles.this, "All permissions are granted", Toast.LENGTH_LONG).show();
                                    } else {
                                        //Toast.makeText(PostArticles.this, "These permissions are denied##:"+deniedList, Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
//                    requestPermissions(
//                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA},
//                            STORAGE_PERMISSION_CODE);
                }
            }
        } else {
            AppHelper.showLog("PERMISSION GRANTED ___");
            showFileChooser();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.length == 2
                && (grantResults[0] == PackageManager.PERMISSION_GRANTED) && (grantResults[1] == PackageManager.PERMISSION_GRANTED)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(PostArticles.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    //Location Permission already granted
                    showFileChooser();
                } else {
                    //Request Location Permission
                    ActivityCompat.requestPermissions(PostArticles.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                            STORAGE_PERMISSION_CODE);
                }
            }
        } else {
            //If the user denies the permission request, then display a toast with some more information
            //   Toast.makeText(PostArticalImage.this, "Oops you just denied the permission", Toast.LENGTH_LONG).show();
//            requestStoragePermission();
           // showPermissionDeniedePopup();
        }
    }

    private void showFileChooser() {

        prefShare = new Allsharedpreference(PostArticles.this);
        Gson gson = new Gson();
        String Translation = prefShare.getShared_String(Allsharedpreference.Translator);
        languageModelResponse = gson.fromJson(Translation, LanguageModelResponse.class);


        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(PostArticles.this);
        builder.setTitle("" + languageModelResponse.getData().getAddPhoto());
        final CharSequence[] options = {"" + languageModelResponse.getData().getTakePhoto(), "" + languageModelResponse.getData().getChooseFromGallery(), "" + languageModelResponse.getData().getCancel()};


        //   final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };
        //  AlertDialog.Builder builder = new AlertDialog.Builder(PostArticles.this);
        //  builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("" + languageModelResponse.getData().getTakePhoto())) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)){
                        startActivityForResult(intent, PICK_IMAGE_REQUEST_CAMARA);
                    }


                } else if (options[item].equals("" + languageModelResponse.getData().getChooseFromGallery())) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)){
                        startActivityForResult(intent, PICK_IMAGE_REQUEST);
                    }


                } else if (options[item].equals("" + languageModelResponse.getData().getCancel())) {

                    dialog.dismiss();
                }
            }
        });
        builder.show();

    }

    private void getLocation() {


        gpsTracker = new GpsTracker(PostArticles.this);
        if (gpsTracker.canGetLocation()) {
            latitude = gpsTracker.getLatitude();
            longitude = gpsTracker.getLongitude();
        } else {
            gpsTracker.showSettingsAlert();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Send the picked image to the crop screen before uploading.
            launchCrop(data.getData());
        } else if (requestCode == PICK_IMAGE_REQUEST_CAMARA && resultCode == RESULT_OK) {
            try {
                Bitmap capturedBitmap = (Bitmap) data.getExtras().get("data");
                Uri capturedUri = saveBitmapToCache(capturedBitmap);
                if (capturedUri != null) {
                    // Send the captured photo to the crop screen before uploading.
                    launchCrop(capturedUri);
                } else {
                    Toast.makeText(PostArticles.this, "Error while capturing image", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == 200 && resultCode == RESULT_OK) {


        }
    }

    private void launchCrop(Uri sourceUri) {
        try {
            cropImage.launch(CropHelper.buildCropOptions(sourceUri));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(PostArticles.this, "Unable to open crop screen", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleCroppedImage(Uri croppedUri) {
        if (croppedUri == null) {
            return;
        }
        try {
            filePath = croppedUri;
            bitmap = MediaStore.Images.Media.getBitmap(PostArticles.this.getContentResolver(), croppedUri);
            img_upload_photo.setImageBitmap(bitmap);
            img_upload_photo.setVisibility(View.GONE);
            image_upload_progress_bar.setVisibility(View.VISIBLE);
            imageChanged = true;
            uploadImage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Uri saveBitmapToCache(Bitmap source) {
        if (source == null) {
            return null;
        }
        try {
            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs();
            File file = new File(cachePath, "camera_" + System.currentTimeMillis() + ".jpg");
            java.io.FileOutputStream stream = new java.io.FileOutputStream(file);
            source.compress(Bitmap.CompressFormat.JPEG, 95, stream);
            stream.close();
            return Uri.fromFile(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setupLiveCounters() {
        edt_news_headlines.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                txt_title_count.setText(s.length() + " / 80");
            }
        });
        edt_news_description.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();
                int wordCount = text.isEmpty() ? 0 : text.split("\\s+").length;
                txt_desc_count.setText(wordCount + " words \u00b7 " + s.length() + " / 400");
            }
        });
    }

    private void prefillForEditMode() {
        Intent intent = getIntent();
        editMode = intent.getBooleanExtra("EDIT_MODE", false);
        if (!editMode) {
            return;
        }
        editUserPostId = intent.getIntExtra("USER_POST_ID", 0);
        String title = intent.getStringExtra("EDIT_TITLE");
        String description = intent.getStringExtra("EDIT_DESCRIPTION");
        String imageUrl = intent.getStringExtra("EDIT_IMAGE_URL");

        if (title != null) {
            edt_news_headlines.setText(title);
        }
        if (description != null) {
            edt_news_description.setText(description);
        }
        if (imageUrl != null && !imageUrl.isEmpty()) {
            ImageId = imageUrl;
            imageChanged = false;
            try {
                Picasso.get().load(imageUrl).into(img_upload_photo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // The reporter is resubmitting a rejected article, so pre-agree to terms.
        checked_terms_conditions.setChecked(true);
        post_button.setText("Resubmit");
    }

    private void uploadImage() {

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST,
                CommonUtilities.URL + "" + APIService.URL_Upload_Image, new com.android.volley.Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                Logone.v("picresponse", response.toString());

                String resultResponse = new String(response.data);
                try {

                    JSONObject jObj = new JSONObject(resultResponse);
                    boolean error = jObj.getBoolean("success");
                    if (error) {
                        JSONObject extraMsgOBJ = jObj.getJSONObject("extras");
                        ImageId = extraMsgOBJ.getString("url");
                        Toast.makeText(PostArticles.this, languageModelResponse.getData().getImageUploadSuccessfully(), Toast.LENGTH_SHORT).show();

                        img_upload_photo.setVisibility(View.VISIBLE);
                        image_upload_progress_bar.setVisibility(View.GONE);
                        //     ProfileUpdateImage();
/*

                        Picasso.with(getContext()).load(ImageId).into(img_upload_photo);
                        showLog(" NARASIMHARAO "+ImageId);*/


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
                PostArticles.this.runOnUiThread(new Runnable() {
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
        RequestQueue requestQueue = Volley.newRequestQueue(PostArticles.this);

        //Adding request to the queue
        requestQueue.add(multipartRequest);
        // VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(multipartRequest);
    }


    private JsonObject setProfileUpdateImage() {

        ProfileUpdateRequiest profileUpdateRequiest = new ProfileUpdateRequiest();

        profileUpdateRequiest.setDraftId((long) SharedPrefsData.getInt(PostArticles.this, Constants.DraftID, Constants.PREF_NAME));
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

                showLog("ENCRYPT" + encrpt_data);

                Gson gsonObj = new Gson();
                UpdateImageResponse userProfileUpdateResponse = gsonObj.fromJson(encrpt_data, UpdateImageResponse.class);


                if (userProfileUpdateResponse.getSuccess()) {


                    Toast.makeText(PostArticles.this, "Profile has been updated", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(PostArticles.this, HomePage.class);
                    startActivity(intent);

                } else {


                }


            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {


                Toast.makeText(PostArticles.this, t.getMessage(), Toast.LENGTH_LONG).show();


            }
        });

        return CHECK_VALUE;
    }

    private JsonObject set_normal_post_data() {
        SingleArticleRequest requestModel = new SingleArticleRequest();
        requestModel.setDraftId(SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
        requestModel.setTitle(edt_news_headlines.getText().toString());
        requestModel.setDescription(edt_news_description.getText().toString());
        requestModel.setLanguageId(SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME));
        requestModel.setCategoryId(SharedPrefsData.getInt(getApplicationContext(), Constants.StateID, Constants.PREF_NAME));
        requestModel.setPostType("normal_image");
        requestModel.setMediaType("1");
        requestModel.setImages("" + ImageId);
        requestModel.setLatitude("" + latitude);
        requestModel.setLongitude("" + longitude);

        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        showLog("API REQUEST E" + jsonString.toString());
        String encrpt_data = null;
        encrpt_data = encrypt(jsonString);
        EncritonRequestData Encriton_Request = new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }

    private JsonObject set_edit_post_data() {
        SingleArticleRequest requestModel = new SingleArticleRequest();
        requestModel.setDraftId(SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
        requestModel.setUserPostId(editUserPostId);
        requestModel.setTitle(edt_news_headlines.getText().toString());
        requestModel.setDescription(edt_news_description.getText().toString());
        requestModel.setLanguageId(SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME));
        requestModel.setCategoryId(SharedPrefsData.getInt(getApplicationContext(), Constants.StateID, Constants.PREF_NAME));
        requestModel.setPostType("normal_image");
        requestModel.setMediaType("1");
        requestModel.setImages("" + ImageId);
        requestModel.setImageChanged(imageChanged);
        requestModel.setLatitude("" + latitude);
        requestModel.setLongitude("" + longitude);

        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        showLog("EDIT API REQUEST E" + jsonString);
        String encrpt_data = encrypt(jsonString);
        EncritonRequestData Encriton_Request = new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }

    public void updatePostData() {
        btn_upload.setVisibility(View.VISIBLE);
        post_button.setVisibility(View.GONE);
        progressBarLarge.setVisibility(View.VISIBLE);

        JsonObject otp = set_edit_post_data();

        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.Edit_User_Post("v3", otp);

        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                try {
                    EncreptionRequest encreptionRequest = response.body();
                    String encrpt_data = decrypt(encreptionRequest.getData());
                    showLog(" Edit Playload " + encrpt_data);
                    Gson gsonObj = new Gson();
                    SingleArticleResponse languageModel = gsonObj.fromJson(encrpt_data, SingleArticleResponse.class);

                    if (languageModel.getSuccess()) {
                        Toast.makeText(PostArticles.this, "Your Article has been updated and resubmitted.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(PostArticles.this, HomePage.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(PostArticles.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                        progressBarLarge.setVisibility(View.GONE);
                        post_button.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(PostArticles.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                    progressBarLarge.setVisibility(View.GONE);
                    post_button.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                Toast.makeText(PostArticles.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                progressBarLarge.setVisibility(View.GONE);
                post_button.setVisibility(View.VISIBLE);
            }
        });
    }

    public void sendpostdata() {

        if (editMode) {
            updatePostData();
            return;
        }

        btn_upload.setVisibility(View.VISIBLE);
        post_button.setVisibility(View.GONE);
        progressBarLarge.setVisibility(View.VISIBLE);

        JsonObject otp = set_normal_post_data();

        showLog(" Playload "+otp);

        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.Add_Article_News("v3", otp);

        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {


                EncreptionRequest encreptionRequest = response.body();
                String encrpt_data = null;
                encrpt_data = decrypt(encreptionRequest.getData());
                showLog(" Playload "+encrpt_data);
                Gson gsonObj = new Gson();

                SingleArticleResponse languageModel = gsonObj.fromJson(encrpt_data, SingleArticleResponse.class);

                if (languageModel.getSuccess()) {
                    Toast.makeText(PostArticles.this, "Your Article Posted Succesfully.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(PostArticles.this, HomePage.class);
                    startActivity(intent);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressBarLarge.setVisibility(View.GONE);
                            post_button.setVisibility(View.VISIBLE);
                        }
                    },2000);
                } else {
                    Toast.makeText(PostArticles.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                    progressBarLarge.setVisibility(View.GONE);
                    post_button.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                Toast.makeText(PostArticles.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                progressBarLarge.setVisibility(View.GONE);
                post_button.setVisibility(View.VISIBLE);

            }
        });
    }
    private void showPermissionDeniedePopup() {
        {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(PostArticles.this);
            builder.setTitle("Need App Permission");
            builder.setMessage("This app requires this permission to function!");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i1) {
                    //requestStoragePermission();
                    Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    i.addCategory(Intent.CATEGORY_DEFAULT);
                    i.setData(uri);
                    startActivity(i);
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i1) {
                    dialogInterface.dismiss();

                }
            });
            builder.show();
        }


    }

}