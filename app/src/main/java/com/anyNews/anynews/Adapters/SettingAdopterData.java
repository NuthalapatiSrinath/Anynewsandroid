package com.anyNews.anynews.Adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.anyNews.anynews.Activities.TermsAndCondition;
import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.LanguageModelResponse;
import com.anyNews.anynews.Pojo.LanguageSelectionRequest;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.models.SettingsModel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

public class SettingAdopterData extends RecyclerView.Adapter<SettingAdopterData.ViewHolder> {

    private ClipboardManager clipboardManager;
    private ClipData clipData;

    Context context;
    ArrayList<SettingsModel> stringArrayList;
    boolean value;
    CountDownTimer countDownTimer;

    LanguageModelResponse languageModelResponse;

    Allsharedpreference prefShare;

    public SettingAdopterData(Context context, ArrayList<SettingsModel> stringArrayList, boolean value) {

        this.context = context;
        this.stringArrayList = stringArrayList;
        this.value = value;
// office mobile edi
    }

    @NonNull
    @Override
    public SettingAdopterData.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.settings_notification_items, parent, false);
        return new SettingAdopterData.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingAdopterData.ViewHolder holder, int position) {

        SettingsModel settingsModel = stringArrayList.get(position);
        holder.setings_name.setText(settingsModel.getTitle());
        if (settingsModel.getId() == 2) {
            holder.switchNotification.setChecked(value);
            holder.switchNotification.setVisibility(View.VISIBLE);
        } else {
            holder.switchNotification.setVisibility(View.GONE);

        }


        holder.switchNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (holder.switchNotification.isChecked()) {
                    getSettingsApi(true);


                } else
                    getSettingsApi(false);

            }
        });
        holder.neomorphFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (settingsModel.getId() == 2) {


                } else if (settingsModel.getId() == 3) {

                    final String appPackageName = context.getPackageName(); // getPackageName() from Context or Activity object
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)){
                            context.startActivity(intent);
                        }
                    } catch (android.content.ActivityNotFoundException anfe) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)){
                            context.startActivity(intent);
                        }

                    }

                } else if (settingsModel.getId() == 4) {

                    Intent intent = new Intent(context, TermsAndCondition.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("EXTRA_SESSION_ID", "1");
                    if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)){
                        context.startActivity(intent);
                    }

                } else if (settingsModel.getId() == 5) {

                    Intent intent = new Intent(context, TermsAndCondition.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("EXTRA_SESSION_ID", "2");
                    if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)){
                        context.startActivity(intent);
                    }


                } else if (settingsModel.getId() == 6) {

                    simplelistDialog();

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return stringArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView setings_name;
        Switch switchNotification;
        LinearLayout neomorphFrameLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            setings_name = itemView.findViewById(R.id.setings_name);
            switchNotification = itemView.findViewById(R.id.switchNotification);
            neomorphFrameLayout = itemView.findViewById(R.id.neomorphFrameLayout);
        }
    }
    private JsonObject ReadOTP(boolean value) {

        LanguageSelectionRequest requestModel = new LanguageSelectionRequest();

        requestModel.setDraftId(SharedPrefsData.getInt(context, Constants.DraftID, Constants.PREF_NAME));
        requestModel.setProperty("notification_flag");
        requestModel.setValue(value);

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

    public void getSettingsApi(boolean value) {

        JsonObject otp = ReadOTP(value);

        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.update_settings("v3", otp);

        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            //@RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {



            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {


            }
        });
    }

    public void simplelistDialog() {

        prefShare = new Allsharedpreference(context);
        Gson gson = new Gson();
        String Translation = prefShare.getShared_String(Allsharedpreference.Translator);
        languageModelResponse = gson.fromJson(Translation, LanguageModelResponse.class);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(""+languageModelResponse.getData().getContactUs());
        // add a list
        String[] options = {""+languageModelResponse.getData().getReportCrash(), ""+languageModelResponse.getData().getLanguageIssue(), ""+languageModelResponse.getData().getFeedback()};
        //Pass the array list in Alert dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = com.anyNews.anynews.Utils.ProfileHubConfigHelper.getContactEmail(context);
                Intent mailto = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + email));
                mailto.putExtra(Intent.EXTRA_SUBJECT, "your_subject");
                mailto.putExtra(Intent.EXTRA_TEXT, "your_text");
                switch (which) {
                    case 0:
                    case 1:
                    case 2:
                        if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)){
                            context.startActivity(mailto);
                        }
                        break;
                    case 3:
                  //      SkypeDialog();
                        break;
                }
            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void SkypeDialog() {
        clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Our Skype id is - anynewsmobile");

        // add the buttons
        builder.setPositiveButton("OK", null);
        builder.setNegativeButton("COPY", null);

        builder.setNegativeButton("Copy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                clipData = ClipData.newPlainText("text", "18882168a465570f");
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(context, " Copied to Clipboard", Toast.LENGTH_SHORT).show();

            }
        });

    }
}