package com.anyNews.anynews.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import com.anyNews.anynews.BuildConfig;
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
import com.anyNews.anynews.Utils.Logone;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.holder.GpsTracker;
import com.anyNews.anynews.models.SearchModel;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image2);
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

        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

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


                    if(spinnernews_catgory.getSelectedItem().toString() == ""||spinnernews_catgory.getSelectedItem().equals("null")){
                        Toast.makeText(FullPost.this, ""+languageModelResponse.getData().getChooseThreeOrMoreCategories(), Toast.LENGTH_SHORT).show();
                    }
                    else{
                        if (ImageId==""){
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
        txt_select_language.setText(" "+languageModelResponse.getData().getNewsHeadline());
        full_image_edt_news_headlines.setHint(" "+languageModelResponse.getData().getWriteYourNewsTitleHere());
        news_category.setText(" "+languageModelResponse.getData().getNewsCategory());
        upload_image.setText(" "+languageModelResponse.getData().getUploadImage());
        checked_terms_conditions.setText(" "+languageModelResponse.getData().getIAgreeToTermsAndConditions());
        postnews.setText(" "+languageModelResponse.getData().getPost());
    }

    private void requestStoragePermission() {
        //Initialize Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(FullPost.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED&&ContextCompat.checkSelfPermission(FullPost.this,
                    Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(FullPost.this,
                    Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {

                showFileChooser();
            } else {
                //Request Location Permission
                AppHelper.showLog("NO PERMISSION");
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU){

                    PermissionX.init(FullPost.this)
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

                    PermissionX.init(FullPost.this)
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
        }
        else {
            showFileChooser();
        }
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

                EncreptionRequest encreptionRequest=response.body();
                showLog("RES"+response.body().getData());
                showLog("API REQUEST R"+decrypt(response.body().getData()));
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

                Toast.makeText(FullPost.this, "Your Full Image Posted Successfully.", Toast.LENGTH_SHORT).show();


                EncreptionRequest encreptionRequest=response.body();
                showLog("RES"+response.body().getData());
                showLog("API REQUEST R"+decrypt(response.body().getData()));
                String encrpt_data = null;
                /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    encrpt_data = decrypt(encreptionRequest.getData());
                }*/
                encrpt_data = decrypt(encreptionRequest.getData());
                Gson gsonObj = new Gson();
                SingleArticleResponse languageModel = gsonObj.fromJson(encrpt_data, SingleArticleResponse.class);



                showLog(" VVVVVVVVV VVV "+encrpt_data);
                if (languageModel.getSuccess()) {

                    Intent intent=new Intent(FullPost.this,HomePage.class);
                    startActivity(intent);

                }else {
                    Toast.makeText(FullPost.this, "Something went wrong..." + call, Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                Toast.makeText(FullPost.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


    private void showFileChooser() {
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
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)){
                        startActivityForResult(intent, PICK_IMAGE_REQUEST_CAMARA);
                    }
                }
                else if (options[item].equals(""+languageModelResponse.getData().getChooseFromGallery()))
                {
                    Intent intent = new   Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)){
                        startActivityForResult(intent, PICK_IMAGE_REQUEST);
                    }

                }
                else if (options[item].equals(""+languageModelResponse.getData().getCancel())) {

                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void getLocation() {



        gpsTracker = new GpsTracker(FullPost.this);
        if(gpsTracker.canGetLocation()){
            latitude = gpsTracker.getLatitude();
            longitude = gpsTracker.getLongitude();



        }else{
            gpsTracker.showSettingsAlert();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(FullPost.this.getContentResolver(), filePath);
                img_upload_photo.setImageBitmap(bitmap);

                showLog("NRAAAAAAA"+img_upload_photo.toString());

                img_upload_photo.setVisibility(View.GONE);
                full_image_upload_progress_bar.setVisibility(View.VISIBLE);

                uploadImage();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(requestCode == PICK_IMAGE_REQUEST_CAMARA && resultCode == RESULT_OK ){
            File f = new File(Environment.getExternalStorageDirectory()
                    .toString());
            for (File temp : f.listFiles()) {
                if (temp.getName().equals("temp.jpg")) {
                    f = temp;


                    break;
                }
            }
            if (!f.exists()) {
                showLog("NRAAAAAAA"+img_upload_photo.toString());

                Toast.makeText(FullPost.this,
                        "Error while capturing image", Toast.LENGTH_LONG)
                        .show();
                return;
            }
            try {
                bitmap = (Bitmap) data.getExtras().get("data");
                img_upload_photo.setImageBitmap(bitmap);
                img_upload_photo.setVisibility(View.GONE);
                full_image_upload_progress_bar.setVisibility(View.VISIBLE);
                uploadImage();
                //storeImageTosdCard(bitmap);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }   else if (requestCode==200&&resultCode==RESULT_OK){


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