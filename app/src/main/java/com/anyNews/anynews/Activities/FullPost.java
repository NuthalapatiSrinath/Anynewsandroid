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

import android.Manifest;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import com.anyNews.anynews.Adapters.AlgorithmAdapter;
import com.anyNews.anynews.Application.VolleyMultipartRequest;
import com.anyNews.anynews.Pojo.CategoriesRequest;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.LanguageModelResponse;
import com.anyNews.anynews.Pojo.SingleArticleRequest;
import com.anyNews.anynews.Pojo.SingleArticleResponse;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.CommonUtilities;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.AppHelper;
import com.anyNews.anynews.Utils.CommonMethods;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.CropHelper;
import com.anyNews.anynews.Utils.DeferredMediaPermissions;
import com.anyNews.anynews.Utils.Logone;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.holder.GpsTracker;
import com.anyNews.anynews.models.SearchModel;
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
import java.io.IOException;
import java.util.ArrayList;
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

public class FullPost extends AppCompatActivity implements  AdapterView.OnItemSelectedListener  {

    private static final String TAG = "REPORTER_IMAGE";

    ImageView img_back,img_upload_photo;
    Spinner spinnernews_catgory;
    ArrayAdapter<String> adapter_state;
    ArrayList<SearchModel.Datum> arrayList;
    CommonMethods mCommonmethods;
    TextView txt_select_language,news_category,upload_image,postnews;
    private int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_IMAGE_REQUEST_CAMARA = 700;
    private static final int STORAGE_PERMISSION_CODE = 123;
    private Uri filePath;
    private Bitmap bitmap;
    private String ImageId="";
    /** FileProvider URI written by the camera app. */
    private Uri cameraOutputUri;
    /** App-owned local copy used for crop cancel fallback. */
    private Uri pendingLocalImageUri;
    private boolean needsCropAfterResume;
    private GpsTracker gpsTracker;
    static double latitude;
    static double longitude;
    LinearLayout post_full_image_artical;
    EditText full_image_edt_news_headlines;
    int changer=0;
    ArrayList<SearchModel.Datum> algorithmItems;
    AlgorithmAdapter adapter;
    CheckBox checked_terms_conditions;
    Allsharedpreference prefShare;
    LanguageModelResponse languageModelResponse;
    LinearLayout img_upload_photo_linner;
    int Caegire_Id;
    ProgressBar progressBarLarge,full_image_upload_progress_bar;
    TextView tv_agree_view;
    private ActivityResultLauncher<CropImageContractOptions> cropImage;

    /**
     * Single live reference to the "Add Photo" chooser. Guards against the dialog being
     * shown twice (rapid double-tap on the upload card / double permission callback),
     * which previously left a second chooser stacked behind the first — so it re-appeared
     * ("asks again") right after the first photo was picked/uploaded.
     */
    private androidx.appcompat.app.AlertDialog imagePickerDialog;
    private ActivityResultLauncher<PickVisualMediaRequest> pickVisualMedia;
    private ActivityResultLauncher<String[]> locationPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image2);

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

        cropImage = registerForActivityResult(new CropImageContract(),
                new ActivityResultCallback<CropImageView.CropResult>() {
                    @Override
                    public void onActivityResult(CropImageView.CropResult result) {
                        if (result != null && result.isSuccessful() && result.getUriContent() != null) {
                            Log.e(TAG, "FullPost crop success uri=" + result.getUriContent());
                            handleSelectedImage(result.getUriContent());
                            return;
                        }
                        if (result != null && result.getError() != null) {
                            Log.e(TAG, "FullPost crop error - fallback", result.getError());
                            Toast.makeText(FullPost.this,
                                    "Crop failed - using original photo",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "FullPost crop cancelled - fallback pending=" + pendingLocalImageUri);
                            Toast.makeText(FullPost.this,
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
        img_back=findViewById(R.id.img_back);
        spinnernews_catgory=findViewById(R.id.spinnernews_catgory);
        img_upload_photo=findViewById(R.id.img_upload_photo);
        post_full_image_artical=findViewById(R.id.post_full_image_artical);
        full_image_edt_news_headlines=findViewById(R.id.full_image_edt_news_headlines);
        checked_terms_conditions=findViewById(R.id.checked_terms_conditions);
        img_upload_photo_linner=findViewById(R.id.img_upload_photo_linner);
        mCommonmethods = new CommonMethods(FullPost.this);
        spinnernews_catgory.setEnabled(true);
        progressBarLarge=findViewById(R.id.progressBarLarge);
        full_image_upload_progress_bar=findViewById(R.id.full_image_upload_progress_bar);
        txt_select_language = findViewById(R.id.txt_select_language);
        news_category = findViewById(R.id.news_category);
        upload_image = findViewById(R.id.upload_image);
        postnews= findViewById(R.id.postnews);
        tv_agree_view = findViewById(R.id.tv_agree_view);

        full_image_edt_news_headlines.setCustomSelectionActionModeCallback(new ActionMode.Callback() {

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

        post_full_image_artical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (full_image_edt_news_headlines.getText().toString().length()==0){

                    Toast.makeText(FullPost.this, ""+languageModelResponse.getData().getEnterTitle(), Toast.LENGTH_SHORT).show();

                } else {


                    if(spinnernews_catgory.getSelectedItem() == null
                            || TextUtils.isEmpty(String.valueOf(spinnernews_catgory.getSelectedItem()))
                            || "null".equals(String.valueOf(spinnernews_catgory.getSelectedItem()))){
                        Toast.makeText(FullPost.this, ""+languageModelResponse.getData().getChooseThreeOrMoreCategories(), Toast.LENGTH_SHORT).show();
                    }
                    else{
                        if (TextUtils.isEmpty(ImageId)){
                            Toast.makeText(FullPost.this, ""+languageModelResponse.getData().getUploadImage(), Toast.LENGTH_SHORT).show();
                        } else {

                            if (checked_terms_conditions.isChecked()){

                                sendpostdata();
                            } else {
                                Toast.makeText(FullPost.this, ""+languageModelResponse.getData().getTermsAndConditions(), Toast.LENGTH_SHORT).show();
                            }
                            String text = spinnernews_catgory.getSelectedItem().toString();

                        }



                    }
                }


            }
        });

        img_upload_photo_linner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestStoragePermission();
            }
        });
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        tv_agree_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ViewTermsAndCondition.class);
                startActivity(intent);
            }
        });

        LanguageSelection();
        setStaticdata();
        getLocation();
    }
    private void setStaticdata() {
        prefShare = new Allsharedpreference(this);
        Gson gson = new Gson();
        String Translation = prefShare.getShared_String(Allsharedpreference.Translator);
        languageModelResponse = gson.fromJson(Translation, LanguageModelResponse.class);
        String headline = null;
        String writeTitle = null;
        String category = null;
        String upload = null;
        String agree = null;
        String post = null;
        if (languageModelResponse != null && languageModelResponse.getData() != null) {
            headline = languageModelResponse.getData().getNewsHeadline();
            writeTitle = languageModelResponse.getData().getWriteYourNewsTitleHere();
            category = languageModelResponse.getData().getNewsCategory();
            upload = languageModelResponse.getData().getUploadImage();
            agree = languageModelResponse.getData().getIAgreeToTermsAndConditions();
            post = languageModelResponse.getData().getPost();
        }
        String languageName = getIntent().getStringExtra("LANGUAGE_NAME");
        if (TextUtils.isEmpty(headline)) {
            headline = !TextUtils.isEmpty(languageName) ? languageName : "News Headline";
        }
        if (TextUtils.isEmpty(writeTitle)) writeTitle = "Write your news title here";
        if (TextUtils.isEmpty(category)) category = "News Category";
        if (TextUtils.isEmpty(upload)) upload = "Upload Image";
        if (TextUtils.isEmpty(agree)) agree = "I agree to Terms and Conditions";
        if (TextUtils.isEmpty(post)) post = "Post";
        txt_select_language.setText(" " + headline);
        full_image_edt_news_headlines.setHint(" " + writeTitle);
        news_category.setText(" " + category);
        upload_image.setText(" " + upload);
        checked_terms_conditions.setText(" " + agree);
        postnews.setText(" " + post);
    }

    private void requestStoragePermission() {
        // Chooser first — no dual CAMERA+storage system dialog before pick.
        showFileChooser();
    }


    private JsonObject sendRequest(){
        CategoriesRequest requestModel = new CategoriesRequest();
        requestModel.setDraftId(SharedPrefsData.getInt(FullPost.this, Constants.DraftID, Constants.PREF_NAME));
        requestModel.setLanguageId(SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME));
        requestModel.setScreenType(1);
        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        showLog("API REQUEST E" + jsonString.toString());
        encrpt_data = encrypt(jsonString);
        EncritonRequestData Encriton_Request=new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }

    private void LanguageSelection() {

        arrayList=new ArrayList<SearchModel.Datum>();


        APIService apiService = RestApiClient.getClient().create(APIService.class);
        JsonObject otp = sendRequest();
        showLog("API REQ   "+otp.toString());
        Call<EncreptionRequest> otp_generation = apiService.List_All_Categories("v3",otp);
        otp_generation.enqueue(new Callback<EncreptionRequest>() {

            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {

                if (response == null || !response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                    return;
                }
                EncreptionRequest encreptionRequest=response.body();
                showLog("RES"+encreptionRequest.getData());
                showLog("API REQUEST R"+decrypt(encreptionRequest.getData()));
                String encrpt_data = null;
                encrpt_data = decrypt(encreptionRequest.getData());
                Gson gsonObj = new Gson();
                SearchModel language = gsonObj.fromJson(encrpt_data, SearchModel.class);


                List<SearchModel.Datum> datumList = language.getData();
                ArrayList statesList1 = new ArrayList();
                for(int i=0;i<language.getData().size();i++){
                    try {
                        statesList1.add(datumList.get(i).getName());
                        showLog(datumList.get(i).getName()+ "srewre");
                        adapter = new AlgorithmAdapter(FullPost.this, datumList);
                        spinnernews_catgory.setAdapter(adapter);
                        spinnernews_catgory.setOnItemSelectedListener(
                                new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent,
                                                               View view, int position, long id)
                                    {
                                        // It returns the clicked item.
                                        SearchModel.Datum clickedItem = (SearchModel.Datum)
                                                parent.getItemAtPosition(position);
                                        String name = clickedItem.getName();
                                        Caegire_Id=clickedItem.getCategoryId();
                                    }
                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent)
                                    {
                                    }
                                });

                    }catch (NullPointerException e){

                    }

                }

            }



            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {


            }
        });

    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        spinnernews_catgory.setSelection(position);


        //    LanguageSelection();

        spinnernews_catgory.setEnabled(true);

    }

    private JsonObject ReadOTP() {

        SingleArticleRequest requestModel = new SingleArticleRequest();
        requestModel.setDraftId(SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
        requestModel.setTitle(full_image_edt_news_headlines.getText().toString());
        requestModel.setLanguageId(SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME));
        requestModel.setCategoryId(Caegire_Id);
        requestModel.setPostType("full_image");
        requestModel.setMediaType("1");
        requestModel.setImages(""+ImageId);
        requestModel.setLatitude(""+latitude);
        requestModel.setLongitude(""+longitude);

        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        showLog("API REQUEST E" + jsonString.toString());
        encrpt_data = encrypt(jsonString);
        EncritonRequestData Encriton_Request=new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }
    public void sendpostdata(){
        post_full_image_artical.setVisibility(View.VISIBLE );
        postnews.setVisibility(View.GONE);
        progressBarLarge.setVisibility(View.VISIBLE);

        JsonObject otp = ReadOTP();
        showLog("API REQ   "+otp.toString());
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.Add_Article_News("v3",otp);

        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {

                postnews.setVisibility(View.VISIBLE);
                progressBarLarge.setVisibility(View.GONE);

                if (response == null || !response.isSuccessful() || response.body() == null) {
                    Toast.makeText(FullPost.this, "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show();
                    return;
                }

                EncreptionRequest encreptionRequest = response.body();
                if (encreptionRequest.getData() == null) {
                    Toast.makeText(FullPost.this, "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show();
                    return;
                }
                showLog("RES" + encreptionRequest.getData());
                String encrpt_data = decrypt(encreptionRequest.getData());
                showLog("API REQUEST R" + encrpt_data);
                Gson gsonObj = new Gson();
                SingleArticleResponse languageModel = gsonObj.fromJson(encrpt_data, SingleArticleResponse.class);

                showLog(" VVVVVVVVV VVV " + encrpt_data);
                if (languageModel != null && Boolean.TRUE.equals(languageModel.getSuccess())) {
                    Toast.makeText(FullPost.this, "Your Full Image Posted Successfully.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(FullPost.this, HomePage.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(FullPost.this, "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                postnews.setVisibility(View.VISIBLE);
                progressBarLarge.setVisibility(View.GONE);
                Toast.makeText(FullPost.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


    private void showFileChooser() {
        // Never stack a second chooser. A double-tap on the upload card (or a duplicate
        // permission callback) used to open two dialogs; picking from the top one left the
        // second showing, so it looked like the app "asked again" after the first upload.
        if (imagePickerDialog != null && imagePickerDialog.isShowing()) {
            Log.e(TAG, "showFileChooser skipped - chooser already showing");
            return;
        }

        prefShare = new Allsharedpreference(FullPost.this);
        Gson gson = new Gson();
        String Translation = prefShare.getShared_String(Allsharedpreference.Translator);
        languageModelResponse = gson.fromJson(Translation, LanguageModelResponse.class);

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(FullPost.this);
        builder.setTitle(""+languageModelResponse.getData().getAddPhoto());
        final CharSequence[] options = {""+languageModelResponse.getData().getTakePhoto(), ""+languageModelResponse.getData().getChooseFromGallery(), ""+languageModelResponse.getData().getCancel()};


        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals(""+languageModelResponse.getData().getTakePhoto()))
                {
                    DeferredMediaPermissions.ensureCamera(FullPost.this,
                            new DeferredMediaPermissions.GrantCallback() {
                                @Override
                                public void onGranted() {
                                    openCameraForPhoto();
                                }
                            });
                }
                else if (options[item].equals(""+languageModelResponse.getData().getChooseFromGallery()))
                {
                    openGalleryForPhoto();
                }
                else if (options[item].equals(""+languageModelResponse.getData().getCancel())) {

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

    private void openCameraForPhoto() {
        Log.e(TAG, "FullPost openCameraForPhoto");
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
            startActivityForResult(intent, PICK_IMAGE_REQUEST_CAMARA);
        } catch (ActivityNotFoundException e) {
            cameraOutputUri = null;
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGalleryForPhoto() {
        Log.e(TAG, "FullPost openGalleryForPhoto");
        DeferredMediaPermissions.openImageGallery(this, null, pickVisualMedia);
    }

    private File createCameraImageFile() throws IOException {
        File cachePath = new File(getCacheDir(), "images");
        if (!cachePath.exists() && !cachePath.mkdirs()) {
            throw new IOException("Unable to create images cache directory");
        }
        return File.createTempFile("camera_" + System.currentTimeMillis() + "_", ".jpg", cachePath);
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void getLocation() {
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
        gpsTracker = new GpsTracker(FullPost.this);
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
    protected void onResume() {
        super.onResume();
        if (needsCropAfterResume && pendingLocalImageUri != null && cropImage != null) {
            needsCropAfterResume = false;
            final Uri cropUri = pendingLocalImageUri;
            Log.e(TAG, "FullPost onResume launching crop for " + cropUri);
            getWindow().getDecorView().post(new Runnable() {
                @Override
                public void run() {
                    if (isFinishing() || isDestroyed()) {
                        needsCropAfterResume = true;
                        return;
                    }
                    launchCrop(cropUri);
                }
            });
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
            }
            onExternalImagePicked(data.getData());
        } else if (requestCode == PICK_IMAGE_REQUEST_CAMARA && resultCode == RESULT_OK) {
            try {
                Uri capturedUri = null;
                if (cameraOutputUri != null) {
                    capturedUri = cameraOutputUri;
                } else if (data != null && data.getData() != null) {
                    capturedUri = data.getData();
                } else if (data != null && data.getExtras() != null
                        && data.getExtras().get("data") instanceof Bitmap) {
                    capturedUri = saveBitmapToCache((Bitmap) data.getExtras().get("data"));
                }
                if (capturedUri != null) {
                    onExternalImagePicked(capturedUri);
                } else {
                    Toast.makeText(FullPost.this, "Error while capturing image", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(FullPost.this, "Error while capturing image", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == 200 && resultCode == RESULT_OK) {


        }
    }

    private void onExternalImagePicked(Uri sourceUri) {
        Log.e(TAG, "FullPost onExternalImagePicked source=" + sourceUri);
        if (sourceUri == null) {
            return;
        }
        Uri localUri = copyUriToAppCache(sourceUri);
        if (localUri == null) {
            Log.e(TAG, "FullPost copyUriToAppCache failed for " + sourceUri);
            Toast.makeText(this, "Unable to read selected image", Toast.LENGTH_SHORT).show();
            return;
        }
        pendingLocalImageUri = localUri;
        needsCropAfterResume = true;
        Log.e(TAG, "FullPost copied localUri=" + localUri + " — will crop on resume");
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
                    launchCrop(cropUri);
                }
            });
        }
    }

    private void launchCrop(Uri sourceUri) {
        if (sourceUri == null) {
            return;
        }
        Log.e(TAG, "FullPost launchCrop sourceUri=" + sourceUri);
        try {
            cropImage.launch(CropHelper.buildCropOptions(FullPost.this, sourceUri));
        } catch (Exception e) {
            Log.e(TAG, "FullPost launchCrop failed - using original", e);
            Toast.makeText(FullPost.this,
                    "Unable to open crop screen - using original photo",
                    Toast.LENGTH_SHORT).show();
            handleSelectedImage(sourceUri);
        }
    }

    private void handleSelectedImage(Uri selectedUri) {
        Log.e(TAG, "FullPost handleSelectedImage uri=" + selectedUri);
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
            img_upload_photo.setImageBitmap(bitmap);
            img_upload_photo.setVisibility(View.GONE);
            full_image_upload_progress_bar.setVisibility(View.VISIBLE);
            uploadImage();
        } catch (Exception e) {
            Log.e(TAG, "FullPost handleSelectedImage failed", e);
            Toast.makeText(this, "Unable to use selected image", Toast.LENGTH_SHORT).show();
            full_image_upload_progress_bar.setVisibility(View.GONE);
            img_upload_photo.setVisibility(View.VISIBLE);
        }
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
            File outFile = createCameraImageFile();
            output = new java.io.FileOutputStream(outFile);
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();
            return FileProvider.getUriForFile(this, getPackageName() + ".provider", outFile);
        } catch (Exception e) {
            Log.e(TAG, "FullPost copyUriToAppCache exception", e);
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
            return FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void uploadImage(){

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST,
                CommonUtilities.URL+""+ APIService.URL_Upload_Image, new com.android.volley.Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {


                String resultResponse = new String(response.data);
                try {

                    JSONObject jObj = new JSONObject(resultResponse);
                    boolean error = jObj.getBoolean("success");
                    if (error) {
                        JSONObject extraMsgOBJ = jObj.getJSONObject("extras");
                        ImageId = extraMsgOBJ.getString("url");
                        // Toast.makeText(FullPost.this, " Image Uploaded Succesfully ", Toast.LENGTH_SHORT).show();
                        Toast.makeText(FullPost.this, " "+languageModelResponse.getData().getImageUploadSuccessfully(), Toast.LENGTH_SHORT).show();
                        img_upload_photo.setVisibility(View.VISIBLE);
                        full_image_upload_progress_bar.setVisibility(View.GONE);


                    } else {
                        Toast.makeText(getContext(), "Image not Captured", Toast.LENGTH_SHORT).show();
                        //   Toast.makeText(getContext(), " "+languageModelResponse.getData().getimage(), Toast.LENGTH_SHORT).show();

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
                FullPost.this.runOnUiThread(new Runnable() {
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

                params.put("file", new DataPart(UUID.randomUUID().toString()+".jpeg", AppHelper.getFileDataFromDrawable(getContext(), img_upload_photo.getDrawable()), "image/jpeg"));


                return params;
            }
        };
//        // Adding request to request queue
        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                100000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(FullPost.this);

        //Adding request to the queue
        requestQueue.add(multipartRequest);
        // VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(multipartRequest);
    }
}