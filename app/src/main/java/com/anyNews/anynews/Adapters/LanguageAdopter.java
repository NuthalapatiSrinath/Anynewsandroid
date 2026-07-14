package com.anyNews.anynews.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.anyNews.anynews.Activities.AreaSelection;
import com.anyNews.anynews.Activities.Districs;
import com.anyNews.anynews.Activities.HomePage;
import com.anyNews.anynews.Activities.States;
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
import com.anyNews.anynews.Utils.FontSettingsHelper;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.Utils.ThemeColorHelper;
import com.anyNews.anynews.models.LanguageModel;
import com.anyNews.anynews.Ui.NeomorphFrameLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

public class LanguageAdopter extends RecyclerView.Adapter<LanguageAdopter.ViewHolder>{

    Context context;
    ArrayList<LanguageModel.Datum>arrayList;
    private Allsharedpreference prefManager;
    CountDownTimer countDownTimer;
    int index = -1;
    public LanguageAdopter(Context context, ArrayList<LanguageModel.Datum> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        prefManager = new Allsharedpreference(context);
    }

    @NonNull
    @Override
    public LanguageAdopter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater=LayoutInflater.from(context);
        View view=layoutInflater.inflate(R.layout.language_items_selection, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull LanguageAdopter.ViewHolder holder, int position) {

        int languageId = arrayList.get(position).getId();
        FontSettingsHelper.applyLanguageSelectionTypography(
                context,
                languageId > 0 ? languageId : SharedPrefsData.getInt(context, Constants.LanguageID, Constants.PREF_NAME),
                holder.language_name,
                holder.second_language_name
        );
        ThemeColorHelper.applySelectedNeomorphTree(context, holder.second_neomorphFrameLayout);
        ThemeColorHelper.applySelectedChip(context, holder.second_neomorphFrameLayout, holder.second_language_name);

        holder.language_name.setText(arrayList.get(position).getName());
        holder.second_language_name.setText(arrayList.get(position).getName());
        holder.language_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                holder.neomorphFrameLayout.setShadowInner();
                holder.neomorphFrameLayout.setEnabled(false);
                holder.neomorphFrameLayout.setClickable(false);
                holder.second_neomorphFrameLayout.setClickable(false);

                holder.neomorphFrameLayout.setVisibility(View.GONE);
                holder.second_neomorphFrameLayout.setVisibility(View.VISIBLE);

                SharedPrefsData.putInt(context,Constants.LanguageID,arrayList.get(position).getId(),Constants.PREF_NAME);
                LanguageSelection(arrayList.get(position).getId(), arrayList.get(position).getName());

            }
        });
        holder.second_language_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.neomorphFrameLayout.setEnabled(false);
                holder.neomorphFrameLayout.setClickable(false);
            }
        });


        if(index==position){

            // neomorph_background_color

            holder.neomorphFrameLayout.setVisibility(View.GONE);
            holder.second_neomorphFrameLayout.setVisibility(View.VISIBLE);

        }else{

            holder.neomorphFrameLayout.setVisibility(View.VISIBLE);
            holder.second_neomorphFrameLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView language_name,second_language_name;
        NeomorphFrameLayout neomorphFrameLayout,second_neomorphFrameLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            language_name=itemView.findViewById(R.id.language_name);
            second_language_name=itemView.findViewById(R.id.second_language_name);
            neomorphFrameLayout=itemView.findViewById(R.id.neomorphFrameLayout);
            second_neomorphFrameLayout=itemView.findViewById(R.id.second_neomorphFrameLayout);
        }

    }


    private void LanguageSelection(int language_id, String language_name) {

        JsonObject otp = SetUpdateLanguage(language_id);

        showLog("API REQUEST E "+otp.toString());
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<JsonObject> otp_generation = apiService.Update_Language("v3",otp);
        otp_generation.enqueue(new Callback<JsonObject>() {

            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                if (response == null || !response.isSuccessful() || response.body() == null) {
                    SharedPrefsData.putInt(context, Constants.LanguageID, 0, Constants.PREF_NAME);
                    SharedPrefsData.putBool(context, Constants.IS_LANGUAGE_SELECTED, false, Constants.PREF_NAME);
                    Toast.makeText(context, "Unable to update language. Please try again.", Toast.LENGTH_SHORT).show();
                    index = -1;
                    notifyDataSetChanged();
                    return;
                }

                if (response.isSuccessful()) {

                    JsonObject jsonObject = response.body().getAsJsonObject();
                    showLog("API REQUEST R "+jsonObject);
                    Gson gson = new Gson();

                    LanguageModelResponse languageSelectionResponse = gson.fromJson(jsonObject, LanguageModelResponse.class);

                    showLog("API REQUEST R "+languageSelectionResponse.toString());
                    if (languageSelectionResponse != null && languageSelectionResponse.getSuccess()) {

                        prefManager.setShared_Booleen(Allsharedpreference.Language, true);

                        String json = gson.toJson(languageSelectionResponse); // myObject - instance of MyObject

                        prefManager.setShared_String(Allsharedpreference.Translator, json);
                        SharedPrefsData.putString(context, Constants.MOVE_NEXT_SCREEN,
                                languageSelectionResponse.getNextScreen(), Constants.PREF_NAME);
                        SharedPrefsData.putString(context, Constants.State_Name, languageSelectionResponse.getNextScreen(), Constants.PREF_NAME);

                        String nextScreen = languageSelectionResponse.getNextScreen();
                        if (nextScreen == null) {
                            nextScreen = "";
                        }

                        if ("main".equalsIgnoreCase(nextScreen)) {

                            SharedPrefsData.putString(context, Constants.MOVE_NEXT_SCREEN, "main", Constants.PREF_NAME);
                            SharedPrefsData.putBool(context, Constants.IS_LANGUAGE_SELECTED, true, Constants.PREF_NAME);
                            Intent intent = new Intent(context, AreaSelection.class);
                            SharedPrefsData.putInt(context, Constants.Back_Button_Move, 1, Constants.PREF_NAME);
                            SharedPrefsData.putString(context, Constants.Selected_Distric_Name,"India",  Constants.PREF_NAME);
                            SharedPrefsData.putString(context, Constants.Selected_Distric_Name1,"India",  Constants.PREF_NAME);

                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("EXTRA_SESSION_ID", "0");
                            if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)){
                                context.startActivity(intent);
                            }
                        } else if ("state".equalsIgnoreCase(nextScreen)) {
                            Intent intent = new Intent(context, States.class);
                            SharedPrefsData.putInt(context, Constants.Back_Button_Move, 2, Constants.PREF_NAME);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);

                            intent.putExtra("LANGUAGE_ID",language_id );
                            intent.putExtra("LANGUAGE_NAME", language_name);
                            if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)){
                                context.startActivity(intent);
                            }
                        } else if ("district".equalsIgnoreCase(nextScreen)) {
                            Intent intent = new Intent(context, Districs.class);
                            SharedPrefsData.putInt(context, Constants.StateID,0,Constants.PREF_NAME);
                            SharedPrefsData.putInt(context, Constants.Back_Button_Move, 3, Constants.PREF_NAME);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);

                            intent.putExtra("LANGUAGE_ID",language_id );
                            intent.putExtra("LANGUAGE_NAME", language_name);
                            if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)){
                                context.startActivity(intent);
                            }
                        } else {
                            // next_screen language (or unknown) — stay on picker
                            SharedPrefsData.putInt(context, Constants.LanguageID, 0, Constants.PREF_NAME);
                            SharedPrefsData.putBool(context, Constants.IS_LANGUAGE_SELECTED, false, Constants.PREF_NAME);
                            Toast.makeText(context,
                                    "This language is not ready yet. Please select another language.",
                                    Toast.LENGTH_LONG).show();
                            index = -1;
                            notifyDataSetChanged();
                        }


                    } else {
                        String msg = "Unable to update language. Please try again.";
                        try {
                            if (jsonObject != null && jsonObject.has("msg") && !jsonObject.get("msg").isJsonNull()) {
                                msg = jsonObject.get("msg").getAsString();
                            }
                        } catch (Exception ignored) {
                        }
                        SharedPrefsData.putInt(context, Constants.LanguageID, 0, Constants.PREF_NAME);
                        SharedPrefsData.putBool(context, Constants.IS_LANGUAGE_SELECTED, false, Constants.PREF_NAME);
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                        index = -1;
                        notifyDataSetChanged();
                    }

                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                SharedPrefsData.putInt(context, Constants.LanguageID, 0, Constants.PREF_NAME);
                SharedPrefsData.putBool(context, Constants.IS_LANGUAGE_SELECTED, false, Constants.PREF_NAME);
                Toast.makeText(context,
                        t != null && t.getMessage() != null ? t.getMessage() : "Network error. Please try again.",
                        Toast.LENGTH_LONG).show();
                index = -1;
                notifyDataSetChanged();
            }
        });

    }

    private JsonObject  SetUpdateLanguage(int language_id) {

        LanguageSelectionRequest requestModel = new LanguageSelectionRequest();
        requestModel.setDraftId(SharedPrefsData.getInt(context, Constants.DraftID, Constants.PREF_NAME));   //
        requestModel.setLanguageId(language_id);


        return new Gson().toJsonTree(requestModel).getAsJsonObject();

    }
}
