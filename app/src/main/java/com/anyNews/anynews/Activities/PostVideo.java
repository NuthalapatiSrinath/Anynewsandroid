package com.anyNews.anynews.Activities;

import static com.anyNews.anynews.App.getContext;
import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
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

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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
import com.bumptech.glide.Glide;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostVideo extends AppCompatActivity {
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
    TextView tv_upload_percent;
    private Uri mVideoURI;
    private ActivityResultLauncher<String[]> locationPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_video);

        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                new ActivityResultCallback<Map<String, Boolean>>() {
                    @Override
                    public void onActivityResult(Map<String, Boolean> result) {
                        boolean granted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION))
                                || Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                        if (granted) {
                            applyLocationFromTracker(true);
                        } else {
                            latitude = 0.0;
                            longitude = 0.0;
                        }
                    }
                });

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
        tv_upload_percent = findViewById(R.id.tv_upload_percent);
        tv_agree_view = findViewById(R.id.tv_agree_view);
        mCommonmethods = new CommonMethods(PostVideo.this);


        String value = "" + getIntent().getStringExtra("POST_VIDEO");


        edt_news_headlines.setLongClickable(false);
        edt_news_headlines.setTextIsSelectable(false);
        edt_news_headlines.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {
            }

            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }
        });
        edt_news_headlines.setCustomSelectionActionModeCallback(new ActionMode.Callback() {

            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {
            }

            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }
        });
        edt_news_headlines.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {
            }

            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }
        });
        edt_news_description.setLongClickable(false);
        edt_news_description.setTextIsSelectable(false);
        edt_news_description.setCustomSelectionActionModeCallback(new ActionMode.Callback() {

            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {
            }

            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }
        });


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

                requestStoragePermission();
            }
        });
        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (edt_news_headlines.getText().toString().length() == 0) {
                    edt_news_headlines.setTextIsSelectable(false);


                    Toast.makeText(PostVideo.this, "" + languageModelResponse.getData().getEnterTitle(), Toast.LENGTH_SHORT).show();
                    showLog(" " + languageModelResponse.getData().getWriteYourNewsTitleHere());

                } else {

                    if (edt_news_description.getText().toString().length() == 0) {
                        edt_news_description.setTextIsSelectable(false);

                        Toast.makeText(PostVideo.this, " " + languageModelResponse.getData().getEnterDescription(), Toast.LENGTH_SHORT).show();

                    } else {
                        //ImageId = String.valueOf(mVideoURI);
                        if (TextUtils.isEmpty(ImageId)) {
                            Logone.e("VideoUpload",""+languageModelResponse.getData().getUploadVideo());

                            Toast.makeText(PostVideo.this, "" + languageModelResponse.getData().getUploadVideo(), Toast.LENGTH_SHORT).show();

                        } else {

                            if (checked_terms_conditions.isChecked()) {

                                sendpostdata();


                            } else {
                                Toast.makeText(PostVideo.this, "" + languageModelResponse.getData().getTermsAndConditions(), Toast.LENGTH_SHORT).show();


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
        upload_image.setText(" " + languageModelResponse.getData().getUploadVideo());
        post_button.setText(" " + languageModelResponse.getData().getPost());
    }

    private void requestStoragePermission() {
        AppHelper.showLog("requestStoragePermission");
        //Initialize Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AppHelper.showLog("PERMISSION");
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED)  {
                //Location Permission already granted
                AppHelper.showLog("PERMISSION GRANTED +++++");
                showFileChooser();
            } else {
                //Request Location Permission
                AppHelper.showLog("NO PERMISSION");
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU){

                    PermissionX.init(this)
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

                    PermissionX.init(this)
                            .permissions(Manifest.permission.READ_MEDIA_VIDEO,Manifest.permission.CAMERA)
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


//    @SuppressLint("MissingSuperCall")
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        AppHelper.showLog("onRequestPermissionsResult");
//        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.length == 2
//                && (grantResults[0] == PackageManager.PERMISSION_GRANTED) && (grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                if (ContextCompat.checkSelfPermission(PostVideo.this,
//                        Manifest.permission.READ_EXTERNAL_STORAGE)
//                        == PackageManager.PERMISSION_GRANTED) {
//                    //Location Permission already granted
//                    showFileChooser();
//                } else {
//                    //Request Location Permission
//                    ActivityCompat.requestPermissions(PostVideo.this,
//                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
//                            STORAGE_PERMISSION_CODE);
//                }
//            }
//        } else {
//            //If the user denies the permission request, then display a toast with some more information
//            //   Toast.makeText(PostArticalImage.this, "Oops you just denied the permission", Toast.LENGTH_LONG).show();
////            requestStoragePermission();
//        }
//    }

    private void showFileChooser() {
        AppHelper.showLog("showFileChooser");
        prefShare = new Allsharedpreference(PostVideo.this);
        Gson gson = new Gson();
        String Translation = prefShare.getShared_String(Allsharedpreference.Translator);
        languageModelResponse = gson.fromJson(Translation, LanguageModelResponse.class);


        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(PostVideo.this);
        builder.setTitle("" + languageModelResponse.getData().getVideoPost());
        final CharSequence[] options = {"" + languageModelResponse.getData().getChooseFromGallery(), "" + languageModelResponse.getData().getRecord_video(),"" + languageModelResponse.getData().getCancel()};


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
                    /*Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)){
                        startActivityForResult(intent, PICK_IMAGE_REQUEST);
                    }*/
                    Intent intent = new Intent();
                    intent.setType("video/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                        startActivityForResult(Intent.createChooser(intent, "Complete action using"), PICK_IMAGE_REQUEST);
                    }

                } else if (options[item].equals("" + languageModelResponse.getData().getRecord_video())) {

                    Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                   // takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);
                    takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Environment.getExternalStorageDirectory().getPath()+"videocapture_example.mp4");

                    startActivityForResult(takeVideoIntent, PICK_IMAGE_REQUEST);
                }else if (options[item].equals("" + languageModelResponse.getData().getCancel())) {

                    dialog.dismiss();
                }
            }
        });
        builder.show();

    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void getLocation() {
        AppHelper.showLog("getLocation");
        if (hasLocationPermission()) {
            applyLocationFromTracker(true);
        } else if (locationPermissionLauncher != null) {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        } else {
            latitude = 0.0;
            longitude = 0.0;
        }
    }

    /** Reads lat/long when possible. Settings dialog only if permission granted but providers off. */
    private void applyLocationFromTracker(boolean allowGpsSettingsDialog) {
        gpsTracker = new GpsTracker(PostVideo.this);
        if (gpsTracker.canGetLocation()) {
            latitude = gpsTracker.getLatitude();
            longitude = gpsTracker.getLongitude();
            return;
        }
        latitude = 0.0;
        longitude = 0.0;
        if (allowGpsSettingsDialog
                && gpsTracker.hasLocationPermission()
                && !gpsTracker.isLocationProviderEnabled()) {
            gpsTracker.showSettingsAlert();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        AppHelper.showLog("onActivityResult");
        if (resultCode != RESULT_OK) return;
        if (requestCode == PICK_IMAGE_REQUEST) {
            try {
                if (data == null || data.getData() == null) {
                    Toast.makeText(PostVideo.this, "No video selected", Toast.LENGTH_SHORT).show();
                    return;
                }
                mVideoURI = data.getData();
                showVideoPreview(mVideoURI);
                setUploadProgressVisible(true, 0);
                uploadImage();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(PostVideo.this, "Unable to use selected video", Toast.LENGTH_SHORT).show();
            }

        }

        /*if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(PostVideo.this.getContentResolver(), filePath);
                img_upload_photo.setImageBitmap(bitmap);

                showLog("NRAAAAAAA" + img_upload_photo.toString());

                img_upload_photo.setVisibility(View.GONE);
                image_upload_progress_bar.setVisibility(View.VISIBLE);
                uploadImage();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } */
        else if (requestCode == PICK_IMAGE_REQUEST_CAMARA && resultCode == RESULT_OK) {
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

                Toast.makeText(PostVideo.this, "Error while capturing image", Toast.LENGTH_LONG)
                        .show();
                return;
            }
            try {
                bitmap = (Bitmap) data.getExtras().get("data");
                img_upload_photo.setImageBitmap(bitmap);
                img_upload_photo.setVisibility(View.VISIBLE);

                setUploadProgressVisible(true, 0);
                uploadImage();
                //storeImageTosdCard(bitmap);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else if (requestCode == 200 && resultCode == RESULT_OK) {


        }
    }

    private void showVideoPreview(Uri videoUri) {
        if (videoUri == null || img_upload_photo == null) {
            return;
        }
        img_upload_photo.setVisibility(View.VISIBLE);
        // Prefer a local frame extract so ContentResolver video URIs preview reliably.
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(this, videoUri);
            Bitmap frame = retriever.getFrameAtTime(1_000_000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            if (frame != null) {
                img_upload_photo.setImageBitmap(frame);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (Exception ignored) {
            }
        }
        try {
            Glide.with(this)
                    .asBitmap()
                    .load(videoUri)
                    .apply(new com.bumptech.glide.request.RequestOptions()
                            .frame(1_000_000L)
                            .centerCrop()
                            .placeholder(R.drawable.ic_video_icon)
                            .error(R.drawable.ic_video_icon))
                    .into(img_upload_photo);
        } catch (Exception e) {
            img_upload_photo.setImageResource(R.drawable.ic_video_icon);
        }
    }

    private void setUploadProgressVisible(boolean visible, int progress) {
        int clamped = Math.max(0, Math.min(100, progress));
        if (image_upload_progress_bar != null) {
            image_upload_progress_bar.setVisibility(visible ? View.VISIBLE : View.GONE);
            image_upload_progress_bar.setIndeterminate(false);
            image_upload_progress_bar.setMax(100);
            image_upload_progress_bar.setProgress(clamped);
        }
        if (tv_upload_percent != null) {
            tv_upload_percent.setVisibility(visible ? View.VISIBLE : View.GONE);
            if (clamped >= 100) {
                tv_upload_percent.setText("Uploaded 100%");
            } else {
                tv_upload_percent.setText("Uploading " + clamped + "%");
            }
        }
    }

    private void setSubmitProcessing(boolean processing) {
        if (progressBarLarge != null) {
            progressBarLarge.setVisibility(processing ? View.VISIBLE : View.GONE);
        }
        if (tv_upload_percent != null) {
            if (processing) {
                tv_upload_percent.setVisibility(View.VISIBLE);
                tv_upload_percent.setText("Processing...");
            } else if (ImageId != null && !ImageId.trim().isEmpty()) {
                setUploadProgressVisible(true, 100);
            }
        }
        if (btn_upload != null) {
            btn_upload.setVisibility(View.VISIBLE);
        }
        if (post_button != null) {
            post_button.setVisibility(processing ? View.GONE : View.VISIBLE);
        }
    }

    private void uploadImage() {
        AppHelper.showLog("uploadImage");
        setUploadProgressVisible(true, 0);
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
                        if (languageModelResponse.getData().getVideoUploadSuccessfully() != null) {
                            Toast.makeText(PostVideo.this, "" + languageModelResponse.getData().getVideoUploadSuccessfully(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(PostVideo.this, "Video upload successful", Toast.LENGTH_SHORT).show();
                        }
                        img_upload_photo.setVisibility(View.VISIBLE);
                        // Keep 100% visible so the reporter sees upload completed.
                        setUploadProgressVisible(true, 100);
                        //     ProfileUpdateImage();
/*

                        Picasso.with(getContext()).load(ImageId).into(img_upload_photo);
                        showLog(" NARASIMHARAO "+ImageId);*/


                    } else {
                        Toast.makeText(getContext(), "Video not Captured", Toast.LENGTH_SHORT).show();
                        setUploadProgressVisible(false, 0);

                    }
                } catch (JSONException e) {
//                    mCropImageUri = null;
                    e.printStackTrace();
                    setUploadProgressVisible(false, 0);
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
                setUploadProgressVisible(false, 0);
            }
        }, new VolleyMultipartRequest.MultipartProgressListener() {
            @Override
            public void transferred(long transferred, final int progress) {
                PostVideo.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setUploadProgressVisible(true, progress);
                    }
                });
            }
        }) {
            /*@Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();

                params.put("file", new DataPart(UUID.randomUUID().toString() + ".jpeg", AppHelper.getFileDataFromDrawable(getContext(), img_upload_photo.getDrawable()), "image/jpeg"));


                return params;
            }*/
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                /// photo field in link
                if (mVideoURI != null) {
                    //params.put("video", new DataPart("file_avatar.mp4", AppHelper.getFileDataFromDrawables(getContext(), mVideoURI)));
                    //params.put("video", new DataPart("file_avatar.mp4", AppHelper.getFileDataFromDrawables(getContext(), mVideoURI)));
                    //Logone.e("videoparam",""+new DataPart(UUID.randomUUID().toString() + ".mp4", AppHelper.getFileDataFromDrawables(getApplicationContext(), ImageId)));
                    //params.put("file", new DataPart(UUID.randomUUID().toString() + ".mp4", AppHelper.getFileDataFromDrawables(getApplicationContext(), ""+ImageId), "mp4/3gp"));
                    // working for video
                    //params.put("file", new DataPart(UUID.randomUUID().toString() + ".mp4", AppHelper.getFileDataFromDrawables(getApplicationContext(),mVideoURI),"mp4/3gp"));
                    params.put("file", new DataPart(UUID.randomUUID().toString() + ".mp4", AppHelper.getFileDataFromDrawables(getApplicationContext(),mVideoURI),"mp4/3gp"));
                }
                return params;
            }
        };
//        // Adding request to request queue
        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                100000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(PostVideo.this);

        //Adding request to the queue
        requestQueue.add(multipartRequest);
        // VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(multipartRequest);
    }


    private JsonObject setProfileUpdateImage() {

        ProfileUpdateRequiest profileUpdateRequiest = new ProfileUpdateRequiest();

        profileUpdateRequiest.setDraftId((long) SharedPrefsData.getInt(PostVideo.this, Constants.DraftID, Constants.PREF_NAME));
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


                    Toast.makeText(PostVideo.this, "Profile has been updated", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(PostVideo.this, HomePage.class);
                    startActivity(intent);

                } else {


                }


            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {


                Toast.makeText(PostVideo.this, t.getMessage(), Toast.LENGTH_LONG).show();


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
        requestModel.setPostType("full_video");
        requestModel.setMediaType("3");
        requestModel.setVideo("" + ImageId);
        requestModel.setLatitude("" + latitude);
        requestModel.setLongitude("" + longitude);

        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        showLog("API REQUEST E" + jsonString.toString());
        encrpt_data = encrypt(jsonString);
        EncritonRequestData Encriton_Request = new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }

    public void sendpostdata() {
        if (ImageId == null || ImageId.trim().isEmpty()) {
            Toast.makeText(this, "Please wait for video upload to finish", Toast.LENGTH_SHORT).show();
            return;
        }

        setSubmitProcessing(true);

        JsonObject otp = set_normal_post_data();

        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.Add_Article_News("v3", otp);

        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                try {
                    if (response == null || response.body() == null) {
                        setSubmitProcessing(false);
                        Toast.makeText(PostVideo.this, "Server returned an empty response. Please try again.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    EncreptionRequest encreptionRequest = response.body();
                    String encrpt_data = decrypt(encreptionRequest.getData());
                    Gson gsonObj = new Gson();
                    SingleArticleResponse languageModel = gsonObj.fromJson(encrpt_data, SingleArticleResponse.class);

                    if (languageModel != null && languageModel.getSuccess()) {
                        Toast.makeText(PostVideo.this, "Your Article Posted Successfully.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(PostVideo.this, HomePage.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                    } else {
                        setSubmitProcessing(false);
                        String msg = languageModel != null && languageModel.getMsg() != null
                                ? languageModel.getMsg()
                                : "Something went wrong...";
                        Toast.makeText(PostVideo.this, msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    setSubmitProcessing(false);
                    Toast.makeText(PostVideo.this, "Unable to process server response", Toast.LENGTH_SHORT).show();
                    Logone.e("VideoUpload1", "" + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                setSubmitProcessing(false);
                String message = t != null && t.getMessage() != null ? t.getMessage() : "Request failed";
                if (message.toLowerCase().contains("timeout") || message.toLowerCase().contains("timed out")) {
                    message = "Request timed out. Video may still be processing — check My Articles, or try again.";
                }
                Toast.makeText(PostVideo.this, message, Toast.LENGTH_LONG).show();
                Logone.e("VideoUpload1", "" + message);
            }
        });
    }
}