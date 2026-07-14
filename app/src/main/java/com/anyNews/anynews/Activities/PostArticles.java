package com.anyNews.anynews.Activities;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.squareup.picasso.Picasso;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.anyNews.anynews.Utils.CropHelper;
import com.anyNews.anynews.Utils.Logone;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.holder.GpsTracker;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageView;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

public class PostArticles extends AppCompatActivity {

    private static final String TAG = "REPORTER_IMAGE";

    ImageView img_upload_photo;
    private int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_IMAGE_REQUEST_CAMARA = 700;
    private static final int STORAGE_PERMISSION_CODE = 123;
    private Uri filePath;
    private Bitmap bitmap;
    private String ImageId = "";
    /** FileProvider URI written by the camera app (survives process death via savedInstanceState). */
    private Uri cameraOutputUri;
    /**
     * App-owned local copy of the selected image (FileProvider). Survives crop cancel/fail
     * and process death; used so CropImageActivity never depends on a temporary Photo Picker URI.
     */
    private Uri pendingLocalImageUri;
    private static final String STATE_CAMERA_OUTPUT_URI = "camera_output_uri";
    private static final String STATE_FILE_PATH = "file_path";
    private static final String STATE_IMAGE_ID = "image_id";
    private static final String STATE_PENDING_LOCAL_URI = "pending_local_uri";
    private static final String PREFS_REPORTER_DRAFT = "reporter_post_image_draft";
    private static final String PREF_PENDING_LOCAL_URI = "pending_local_uri";
    private static final String PREF_FILE_PATH = "file_path";
    private static final String PREF_IMAGE_ID = "image_id";


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
    private ActivityResultLauncher<PickVisualMediaRequest> pickVisualMedia;

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

        if (savedInstanceState != null) {
            String cameraUri = savedInstanceState.getString(STATE_CAMERA_OUTPUT_URI);
            if (cameraUri != null) {
                cameraOutputUri = Uri.parse(cameraUri);
            }
            String savedFilePath = savedInstanceState.getString(STATE_FILE_PATH);
            if (savedFilePath != null) {
                filePath = Uri.parse(savedFilePath);
            }
            String savedImageId = savedInstanceState.getString(STATE_IMAGE_ID);
            if (savedImageId != null) {
                ImageId = savedImageId;
            }
            String pendingUri = savedInstanceState.getString(STATE_PENDING_LOCAL_URI);
            if (pendingUri != null) {
                pendingLocalImageUri = Uri.parse(pendingUri);
            }
            Log.e(TAG, "restore from savedInstanceState filePath=" + filePath
                    + " pending=" + pendingLocalImageUri + " imageId=" + ImageId);
        } else {
            restoreDraftFromPrefs();
        }

        // CanHub crop after FileProvider copy (avoids Photo Picker grant bounce to Home).
        cropImage = registerForActivityResult(new CropImageContract(),
                new ActivityResultCallback<CropImageView.CropResult>() {
                    @Override
                    public void onActivityResult(CropImageView.CropResult result) {
                        // Always stay on PostArticles. Never finish() / navigate Home here.
                        if (result != null && result.isSuccessful() && result.getUriContent() != null) {
                            Log.e(TAG, "crop success uri=" + result.getUriContent());
                            handleSelectedImage(result.getUriContent());
                            return;
                        }
                        if (result != null && result.getError() != null) {
                            Log.e(TAG, "crop error - fallback to local copy", result.getError());
                            Toast.makeText(PostArticles.this,
                                    "Crop failed - using original photo",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "crop cancelled - fallback pending=" + pendingLocalImageUri);
                            Toast.makeText(PostArticles.this,
                                    "Crop cancelled - using selected photo",
                                    Toast.LENGTH_SHORT).show();
                        }
                        if (pendingLocalImageUri != null) {
                            handleSelectedImage(pendingLocalImageUri);
                        }
                    }
                });

        pickVisualMedia = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null) {
                            onExternalImagePicked(uri);
                        }
                    }
                });

        // Restore preview after process death / recreate (do not finish / reset form).
        if (filePath != null) {
            showImagePreview(filePath);
        } else if (pendingLocalImageUri != null) {
            showImagePreview(pendingLocalImageUri);
        }

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
                    openCameraForPhoto();
                } else if (options[item].equals("" + languageModelResponse.getData().getChooseFromGallery())) {
                    openGalleryForPhoto();
                } else if (options[item].equals("" + languageModelResponse.getData().getCancel())) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();

    }

    private void openCameraForPhoto() {
        Log.e(TAG, "openCameraForPhoto");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile;
        try {
            photoFile = createCameraImageFile();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to create photo file", Toast.LENGTH_SHORT).show();
            return;
        }
        cameraOutputUri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".provider",
                photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraOutputUri);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        List<ResolveInfo> resInfoList = getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            grantUriPermission(
                    resolveInfo.activityInfo.packageName,
                    cameraOutputUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        try {
            // Do not rely on resolveActivity alone — some OEMs (e.g. Vivo) hide camera apps
            // from package visibility even when capture still works.
            startActivityForResult(intent, PICK_IMAGE_REQUEST_CAMARA);
        } catch (ActivityNotFoundException e) {
            cameraOutputUri = null;
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGalleryForPhoto() {
        Log.e(TAG, "openGalleryForPhoto");
        // Prefer ACTION_GET_CONTENT / chooser over Photo Picker when possible:
        // Photo Picker frequently destroys PostArticles on low-RAM OEMs (Vivo), and the
        // returned content URI only has a temporary grant that CropImageActivity cannot read
        // (URI is passed in extras, not Intent data — no FLAG_GRANT_READ_URI_PERMISSION).
        Intent getContent = new Intent(Intent.ACTION_GET_CONTENT);
        getContent.setType("image/*");
        getContent.addCategory(Intent.CATEGORY_OPENABLE);
        getContent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent pick = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pick.setType("image/*");

        Intent openDocument = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        openDocument.addCategory(Intent.CATEGORY_OPENABLE);
        openDocument.setType("image/*");
        openDocument.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        Intent chooser = Intent.createChooser(getContent, "Select Image");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pick, openDocument});
        try {
            startActivityForResult(chooser, PICK_IMAGE_REQUEST);
            return;
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }

        // Last resort: system Photo Picker (still OK because we copy to cache before crop).
        if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(this)) {
            try {
                pickVisualMedia.launch(
                        new PickVisualMediaRequest.Builder()
                                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                                .build());
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Toast.makeText(this, "No gallery app available on this device", Toast.LENGTH_LONG).show();
    }

    private File createCameraImageFile() throws IOException {
        File cachePath = new File(getCacheDir(), "images");
        if (!cachePath.exists() && !cachePath.mkdirs()) {
            throw new IOException("Unable to create images cache directory");
        }
        return File.createTempFile("camera_" + System.currentTimeMillis() + "_", ".jpg", cachePath);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (cameraOutputUri != null) {
            outState.putString(STATE_CAMERA_OUTPUT_URI, cameraOutputUri.toString());
        }
        if (filePath != null) {
            outState.putString(STATE_FILE_PATH, filePath.toString());
        }
        if (ImageId != null && !ImageId.isEmpty()) {
            outState.putString(STATE_IMAGE_ID, ImageId);
        }
        if (pendingLocalImageUri != null) {
            outState.putString(STATE_PENDING_LOCAL_URI, pendingLocalImageUri.toString());
        }
        persistDraftImageState();
        Log.e(TAG, "onSaveInstanceState camera=" + cameraOutputUri
                + " pending=" + pendingLocalImageUri + " filePath=" + filePath);
    }

    private void restoreDraftFromPrefs() {
        SharedPreferences sp = getSharedPreferences(PREFS_REPORTER_DRAFT, MODE_PRIVATE);
        String pending = sp.getString(PREF_PENDING_LOCAL_URI, null);
        String savedFile = sp.getString(PREF_FILE_PATH, null);
        String savedImageId = sp.getString(PREF_IMAGE_ID, null);
        if (pending != null) {
            pendingLocalImageUri = Uri.parse(pending);
        }
        if (savedFile != null) {
            filePath = Uri.parse(savedFile);
        }
        if (savedImageId != null && !savedImageId.isEmpty()) {
            ImageId = savedImageId;
        }
        Log.e(TAG, "restoreDraftFromPrefs filePath=" + filePath
                + " pending=" + pendingLocalImageUri + " imageId=" + ImageId);
    }

    private void persistDraftImageState() {
        SharedPreferences.Editor ed = getSharedPreferences(PREFS_REPORTER_DRAFT, MODE_PRIVATE).edit();
        if (pendingLocalImageUri != null) {
            ed.putString(PREF_PENDING_LOCAL_URI, pendingLocalImageUri.toString());
        } else {
            ed.remove(PREF_PENDING_LOCAL_URI);
        }
        if (filePath != null) {
            ed.putString(PREF_FILE_PATH, filePath.toString());
        } else {
            ed.remove(PREF_FILE_PATH);
        }
        if (ImageId != null && !ImageId.isEmpty()) {
            ed.putString(PREF_IMAGE_ID, ImageId);
        } else {
            ed.remove(PREF_IMAGE_ID);
        }
        ed.apply();
    }

    private void clearDraftImageState() {
        getSharedPreferences(PREFS_REPORTER_DRAFT, MODE_PRIVATE).edit().clear().apply();
        Log.e(TAG, "clearDraftImageState");
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
            try {
                final int takeFlags = data.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                if (takeFlags != 0) {
                    getContentResolver().takePersistableUriPermission(data.getData(), takeFlags);
                }
            } catch (Exception ignored) {
                // Not all providers support persistable grants (Photo Picker / some OEMs).
            }
            onExternalImagePicked(data.getData());
        } else if (requestCode == PICK_IMAGE_REQUEST_CAMARA && resultCode == RESULT_OK) {
            try {
                Uri capturedUri = null;
                if (cameraOutputUri != null) {
                    capturedUri = cameraOutputUri;
                } else if (data != null && data.getData() != null) {
                    capturedUri = data.getData();
                } else if (data != null && data.getExtras() != null && data.getExtras().get("data") instanceof Bitmap) {
                    // Thumbnail-only camera apps (no EXTRA_OUTPUT write).
                    capturedUri = saveBitmapToCache((Bitmap) data.getExtras().get("data"));
                }
                if (capturedUri != null) {
                    onExternalImagePicked(capturedUri);
                } else {
                    Toast.makeText(PostArticles.this, "Error while capturing image", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(PostArticles.this, "Error while capturing image", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == 200 && resultCode == RESULT_OK) {


        }
    }

    /**
     * Copy any external/content URI into app cache, then open crop with an app-owned URI.
     * Prevents CropImageActivity SecurityException (picker URI grants do not propagate)
     * and keeps PostArticles alive if crop cancels/fails.
     */
    private void onExternalImagePicked(Uri sourceUri) {
        Log.e(TAG, "onExternalImagePicked source=" + sourceUri);
        if (sourceUri == null) {
            return;
        }
        Uri localUri = copyUriToAppCache(sourceUri);
        if (localUri == null) {
            Log.e(TAG, "copyUriToAppCache failed for " + sourceUri);
            Toast.makeText(this, "Unable to read selected image", Toast.LENGTH_SHORT).show();
            return;
        }
        pendingLocalImageUri = localUri;
        persistDraftImageState();
        Log.e(TAG, "copied to app cache localUri=" + localUri);
        // Immediate preview so the form never looks empty if crop is slow/cancelled.
        showImagePreview(localUri);
        // Defer crop until after we are fully resumed (OEM ActivityResult flakiness).
        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                launchCrop(localUri);
            }
        });
    }

    private void launchCrop(Uri sourceUri) {
        if (sourceUri == null) {
            return;
        }
        Log.e(TAG, "launchCrop sourceUri=" + sourceUri);
        try {
            cropImage.launch(CropHelper.buildCropOptions(PostArticles.this, sourceUri));
        } catch (Exception e) {
            Log.e(TAG, "launchCrop failed - using original", e);
            Toast.makeText(PostArticles.this,
                    "Unable to open crop screen - using original photo",
                    Toast.LENGTH_SHORT).show();
            handleSelectedImage(sourceUri);
        }
    }

    private void handleSelectedImage(Uri selectedUri) {
        Log.e(TAG, "handleSelectedImage uri=" + selectedUri);
        if (selectedUri == null) {
            return;
        }
        try {
            // Prefer a durable app-owned copy for upload.
            Uri durable = selectedUri;
            if (!isAppOwnedUri(selectedUri)) {
                Uri copied = copyUriToAppCache(selectedUri);
                if (copied != null) {
                    durable = copied;
                }
            }
            filePath = durable;
            pendingLocalImageUri = durable;
            imageChanged = true;
            persistDraftImageState();
            showImagePreview(durable);
            img_upload_photo.setVisibility(View.VISIBLE);
            image_upload_progress_bar.setVisibility(View.VISIBLE);
            uploadImage();
        } catch (Exception e) {
            Log.e(TAG, "handleSelectedImage failed", e);
            Toast.makeText(this, "Unable to use selected image", Toast.LENGTH_SHORT).show();
            image_upload_progress_bar.setVisibility(View.GONE);
            img_upload_photo.setVisibility(View.VISIBLE);
        }
    }

    private void showImagePreview(Uri uri) {
        if (uri == null || img_upload_photo == null) {
            return;
        }
        try {
            bitmap = decodeSampledBitmap(uri, 1024, 1024);
            if (bitmap != null) {
                img_upload_photo.setImageBitmap(bitmap);
            } else {
                img_upload_photo.setImageURI(uri);
            }
            img_upload_photo.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                img_upload_photo.setImageURI(uri);
                img_upload_photo.setVisibility(View.VISIBLE);
            } catch (Exception ignored) {
            }
        }
    }

    private Bitmap decodeSampledBitmap(Uri uri, int reqWidth, int reqHeight) {
        android.graphics.BitmapFactory.Options bounds = new android.graphics.BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (java.io.InputStream in = getContentResolver().openInputStream(uri)) {
            if (in == null) {
                return null;
            }
            android.graphics.BitmapFactory.decodeStream(in, null, bounds);
        } catch (Exception e) {
            return null;
        }
        android.graphics.BitmapFactory.Options opts = new android.graphics.BitmapFactory.Options();
        opts.inSampleSize = calculateInSampleSize(bounds, reqWidth, reqHeight);
        try (java.io.InputStream in = getContentResolver().openInputStream(uri)) {
            if (in == null) {
                return null;
            }
            return android.graphics.BitmapFactory.decodeStream(in, null, opts);
        } catch (Exception e) {
            return null;
        }
    }

    private static int calculateInSampleSize(android.graphics.BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return Math.max(1, inSampleSize);
    }

    /**
     * Stream any content/file URI into cache/images and return a FileProvider URI.
     * Same-app FileProvider URIs are readable by CropImageActivity without extra grants.
     */
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
            File outFile = createCameraImageFile();
            output = new java.io.FileOutputStream(outFile);
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();
            Uri outUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", outFile);
            Log.e(TAG, "copyUriToAppCache ok outUri=" + outUri + " size=" + outFile.length());
            return outUri;
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

    private Uri saveBitmapToCache(Bitmap source) {
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
            // Content URI required by cropper / FileUriExposedException guards.
            return FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
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
        Log.e(TAG, "uploadImage start filePath=" + filePath + " url=" + CommonUtilities.URL + APIService.URL_Upload_Image);

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST,
                CommonUtilities.URL + "" + APIService.URL_Upload_Image, new com.android.volley.Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                Logone.v("picresponse", response.toString());
                Log.e(TAG, "uploadImage response bytes=" + (response.data != null ? response.data.length : 0));

                String resultResponse = new String(response.data);
                try {

                    JSONObject jObj = new JSONObject(resultResponse);
                    boolean error = jObj.getBoolean("success");
                    if (error) {
                        JSONObject extraMsgOBJ = jObj.getJSONObject("extras");
                        ImageId = extraMsgOBJ.getString("url");
                        persistDraftImageState();
                        Log.e(TAG, "uploadImage success ImageId=" + ImageId);
                        Toast.makeText(PostArticles.this, languageModelResponse.getData().getImageUploadSuccessfully(), Toast.LENGTH_SHORT).show();

                        img_upload_photo.setVisibility(View.VISIBLE);
                        image_upload_progress_bar.setVisibility(View.GONE);
                        //     ProfileUpdateImage();
/*

                        // Picasso.with(PostArticles.this).load(ImageId).into(img_upload_photo);
                        showLog(" NARASIMHARAO "+ImageId);*/


                    } else {
                        Toast.makeText(PostArticles.this, "Image not Captured", Toast.LENGTH_SHORT).show();
                        img_upload_photo.setVisibility(View.VISIBLE);
                        image_upload_progress_bar.setVisibility(View.GONE);
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
                img_upload_photo.setVisibility(View.VISIBLE);
                image_upload_progress_bar.setVisibility(View.GONE);
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
                byte[] imageBytes = null;
                // Prefer durable URI bytes — App.getContext() is null (AppController is the Application)
                // and drawable cast fails for vectors / after GONE visibility tricks.
                if (filePath != null) {
                    imageBytes = AppHelper.getFileDataFromDrawables(PostArticles.this, filePath);
                }
                if (imageBytes == null || imageBytes.length == 0) {
                    Drawable drawable = img_upload_photo.getDrawable();
                    if (drawable instanceof android.graphics.drawable.BitmapDrawable) {
                        imageBytes = AppHelper.getFileDataFromDrawable(PostArticles.this, drawable);
                    }
                }
                if (imageBytes == null || imageBytes.length == 0) {
                    PostArticles.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            image_upload_progress_bar.setVisibility(View.GONE);
                            img_upload_photo.setVisibility(View.VISIBLE);
                            Toast.makeText(PostArticles.this, "Unable to read image for upload", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return params;
                }
                params.put("file", new DataPart(UUID.randomUUID().toString() + ".jpeg", imageBytes, "image/jpeg"));
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
                        clearDraftImageState();
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
                    clearDraftImageState();
                    Intent intent = new Intent(PostArticles.this, HomePage.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(PostArticles.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                    progressBarLarge.setVisibility(View.GONE);
                    post_button.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                String message = t != null && t.getMessage() != null ? t.getMessage() : "Request failed";
                if (message.toLowerCase().contains("timeout") || message.toLowerCase().contains("timed out")) {
                    message = "Request timed out. Please try again.";
                }
                Toast.makeText(PostArticles.this, message, Toast.LENGTH_LONG).show();
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