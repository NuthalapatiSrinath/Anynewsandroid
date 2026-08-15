package com.anyNews.anynews.Activities;

import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anyNews.anynews.Adapters.LanguageAdopter;
import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.LanguageModelResponse;
import com.anyNews.anynews.Pojo.LanguageSelectionRequest;
import com.anyNews.anynews.Pojo.UpdateStateResponse;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.DistrictLabelHelper;
import com.anyNews.anynews.Utils.FontSettingsHelper;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.Utils.ThemeColorHelper;
import com.anyNews.anynews.models.LanguageModel;
import com.anyNews.anynews.models.RegionModel;
import com.anyNews.anynews.models.StatesModel;
import com.anyNews.anynews.Ui.NeomorphFrameLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AreaSelectionAdopter extends RecyclerView.Adapter<AreaSelectionAdopter.ViewHolder>{

    Context context;
    ArrayList<RegionModel.Datum> arrayList;
    private Allsharedpreference prefManager;
    CountDownTimer countDownTimer;
    int index = -1;
    public AreaSelectionAdopter(Context context, ArrayList<RegionModel.Datum> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        prefManager = new Allsharedpreference(context);
    }

    @NonNull
    @Override
    public AreaSelectionAdopter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater=LayoutInflater.from(context);
        View view=layoutInflater.inflate(R.layout.area_items_layout, parent, false);
        return new AreaSelectionAdopter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AreaSelectionAdopter.ViewHolder holder, int position) {
        int languageId = SharedPrefsData.getInt(context, Constants.LanguageID, Constants.PREF_NAME);
        FontSettingsHelper.applyAreaSelectionTypography(
                context,
                languageId,
                holder.language_name,
                holder.second_language_name
        );
        ThemeColorHelper.applySelectedNeomorphTree(context, holder.second_neomorphFrameLayout);
        ThemeColorHelper.applySelectedChip(context, holder.second_neomorphFrameLayout, holder.second_language_name);

        holder.language_name.setText(arrayList.get(position).getName());
        holder.second_language_name.setText(arrayList.get(position).getName());

        if(index==position){

            // neomorph_background_color

            holder.neomorphFrameLayout.setVisibility(View.GONE);
            holder.second_neomorphFrameLayout.setVisibility(View.VISIBLE);

        }else{

            holder.neomorphFrameLayout.setVisibility(View.VISIBLE);
            holder.second_neomorphFrameLayout.setVisibility(View.GONE);
        }

        holder.language_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                holder.neomorphFrameLayout.setShadowInner();
                holder.neomorphFrameLayout.setEnabled(false);
                holder.neomorphFrameLayout.setClickable(false);
                holder.second_neomorphFrameLayout.setClickable(false);

                holder.neomorphFrameLayout.setVisibility(View.GONE);
                holder.second_neomorphFrameLayout.setVisibility(View.VISIBLE);

                SharedPrefsData.putInt(context,Constants.RegionID,arrayList.get(position).getId(),Constants.PREF_NAME);

                LanguageSelection(arrayList.get(position).getId(),arrayList.get(position).getName());

            }
        });
        holder.second_language_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.neomorphFrameLayout.setEnabled(false);
                holder.neomorphFrameLayout.setClickable(false);
            }
        });

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
    private void LanguageSelection(int language_id, String name) {

        JsonObject otp = SetUpdateLanguage(language_id);

        showLog("API REQ   "+otp.toString());
        APIService apiService = RestApiClient.getClient().create(APIService.class);

        Call<EncreptionRequest> otp_generation = apiService.get_onboarding_regions_requiest("v3",otp);

        otp_generation.enqueue(new Callback<EncreptionRequest>() {

            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {

                EncreptionRequest encreptionRequest=response.body();
                showLog("RES"+response.body().getData());
                showLog("API REQUEST R"+decrypt(response.body().getData()));
                String encrpt_data =  decrypt(encreptionRequest.getData());
                Gson gsonObj = new Gson();

                UpdateStateResponse languageModel  = gsonObj.fromJson(encrpt_data, UpdateStateResponse.class);
                if (languageModel.getSuccess()) {

                    Intent intent = new Intent(context, HomePage.class);
                    // Region onboarding name is the display label (not always a district id).
                    DistrictLabelHelper.saveName(context, name);

                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("EXTRA_SESSION_ID", "0");
                    if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)){
                        context.startActivity(intent);
                    }
                }

            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {

                //    Toast.makeText(context, t.getMessage(), Toast.LENGTH_LONG).show();


            }
        });

    }

    private JsonObject  SetUpdateLanguage(int language_id) {

        LanguageSelectionRequest requestModel = new LanguageSelectionRequest();
        requestModel.setDraftId(SharedPrefsData.getInt(context, Constants.DraftID, Constants.PREF_NAME));   //
        requestModel.setRegion_id(language_id);

        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        encrpt_data = encrypt(jsonString);
        showLog("API REQUEST E" + jsonString.toString());
        EncritonRequestData Encriton_Request=new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);

   //     return new Gson().toJsonTree(requestModel).getAsJsonObject();
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }
}
