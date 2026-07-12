package com.anyNews.anynews.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.anyNews.anynews.Activities.Districs;
import com.anyNews.anynews.Activities.HomePage;
import com.anyNews.anynews.Activities.States;
import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.LanguageModelResponse;
import com.anyNews.anynews.Pojo.LanguageSelectionRequest;
import com.anyNews.anynews.Pojo.UpdateStateResponse;
import com.anyNews.anynews.Pojo.Update_district_respouse;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.AppLanguagesFonts;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.models.DistricsModel;
import com.anyNews.anynews.models.LanguageModel;
import com.anyNews.anynews.Ui.NeomorphFrameLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

public class DistricsAdopter extends RecyclerView.Adapter<DistricsAdopter.ViewHolder> {

    Context context;
    ArrayList<DistricsModel.Datum> arrayList;
    int index = -1;

    public DistricsAdopter(Context context, ArrayList<DistricsModel.Datum> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater=LayoutInflater.from(context);
        View view=layoutInflater.inflate(R.layout.districs_items_layout, parent, false);
        return new DistricsAdopter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.second_distics_name.setText(arrayList.get(position).getName());
        holder.distric_name.setText(arrayList.get(position).getName());

        holder.second_distics_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                holder.districs_neomorphFrameLayout.setVisibility(View.GONE);
                holder.second_districs_neomorphFrameLayout.setVisibility(View.VISIBLE);

                index = position;
                notifyDataSetChanged();
            }
        });

        holder.distric_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                holder.districs_neomorphFrameLayout.setVisibility(View.GONE);
                holder.second_districs_neomorphFrameLayout.setVisibility(View.VISIBLE);

                index = position;
                notifyDataSetChanged();


            }
        });


        if(index==position){

            // neomorph_background_color

            holder.districs_neomorphFrameLayout.setClickable(false);
            holder.second_districs_neomorphFrameLayout.setClickable(false);
            holder.districs_neomorphFrameLayout.setEnabled(false);
            holder.second_districs_neomorphFrameLayout.setEnabled(false);
            getDistrics(arrayList.get(position).getId(),arrayList.get(position).getName());
            holder.districs_neomorphFrameLayout.setVisibility(View.GONE);
            holder.second_districs_neomorphFrameLayout.setVisibility(View.VISIBLE);

        }else{
            holder.districs_neomorphFrameLayout.setClickable(false);
            holder.second_districs_neomorphFrameLayout.setClickable(false);
            holder.districs_neomorphFrameLayout.setEnabled(false);
            holder.second_districs_neomorphFrameLayout.setEnabled(false);
            holder.districs_neomorphFrameLayout.setVisibility(View.VISIBLE);
            holder.second_districs_neomorphFrameLayout.setVisibility(View.GONE);
        }

    }


    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder  extends RecyclerView.ViewHolder{
        NeomorphFrameLayout districs_neomorphFrameLayout,second_districs_neomorphFrameLayout;
        TextView distric_name,second_distics_name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            districs_neomorphFrameLayout=itemView.findViewById(R.id.districs_neomorphFrameLayout);
            second_districs_neomorphFrameLayout=itemView.findViewById(R.id.second_districs_neomorphFrameLayout);
            distric_name=itemView.findViewById(R.id.distric_name);
            second_distics_name=itemView.findViewById(R.id.second_distics_name);
            AppLanguagesFonts.setTypeFace(distric_name);
            AppLanguagesFonts.setTypeFace(second_distics_name);
        }
    }



    private void getDistrics(final int id,final String distric_name) {

        JsonObject otp = get_update_districs(id);
        showLog("API REQ   "+otp.toString());
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> otp_generation = apiService.Update_district("v3",otp);
        otp_generation.enqueue(new Callback<EncreptionRequest>() {

            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {


                EncreptionRequest encreptionRequest=response.body();
                showLog("API RES   "+response.body().getData());
                String encrpt_data =  decrypt(encreptionRequest.getData());
                Gson gsonObj = new Gson();
                UpdateStateResponse languageModel  = gsonObj.fromJson(encrpt_data, UpdateStateResponse.class);
                if (languageModel.getSuccess()) {

                    SharedPrefsData.putString(context, Constants.Selected_Distric_Name, distric_name, Constants.PREF_NAME);
                    SharedPrefsData.putString(context, Constants.Selected_Distric_Name1, distric_name, Constants.PREF_NAME);
                    SharedPrefsData.putString(context, Constants.MOVE_NEXT_SCREEN, "main", Constants.PREF_NAME);
                    SharedPrefsData.putBool(context, Constants.IS_LANGUAGE_SELECTED, true, Constants.PREF_NAME);

                    Intent intent=new Intent(context,HomePage.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("EXTRA_SESSION_ID", "0");
                    if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)){
                        context.startActivity(intent);
                        if (context instanceof Districs) {
                            ((Districs) context).finish();
                        }
                    }
                }

            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {

            }
        });

    }

    private JsonObject get_update_districs(int id) {

        LanguageSelectionRequest requestModel = new LanguageSelectionRequest();

        requestModel.setDraftId(SharedPrefsData.getInt(context, Constants.DraftID, Constants.PREF_NAME));   //
        requestModel.setDistId(id);

        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        encrpt_data = encrypt(jsonString);
        showLog("API REQUEST E" + jsonString.toString());
        EncritonRequestData Encriton_Request=new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }
}