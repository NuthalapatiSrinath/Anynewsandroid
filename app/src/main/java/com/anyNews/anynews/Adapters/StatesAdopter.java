package com.anyNews.anynews.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anyNews.anynews.Activities.Districs;
import com.anyNews.anynews.Activities.HomePage;
import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.LanguageSelectionRequest;
import com.anyNews.anynews.Pojo.UpdateStateResponse;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.AppLanguagesFonts;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.FontSettingsHelper;
import com.anyNews.anynews.Utils.MediaUrl;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.Utils.ThemeColorHelper;
import com.anyNews.anynews.models.StatesModel;
import com.anyNews.anynews.Ui.NeomorphFrameLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

public class StatesAdopter extends RecyclerView.Adapter<StatesAdopter.ViewHolder> {

    Context context;
    ArrayList<StatesModel.Datum> arrayList;
    int index = -1;

    public StatesAdopter(Context context, ArrayList<StatesModel.Datum> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }


    @NonNull
    @Override
    public StatesAdopter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater=LayoutInflater.from(context);
        View view=layoutInflater.inflate(R.layout.states_items_layout, parent, false);
        return new StatesAdopter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatesAdopter.ViewHolder holder, int position) {
        int languageId = SharedPrefsData.getInt(context, Constants.LanguageID, Constants.PREF_NAME);
        FontSettingsHelper.applyStateSelectionTypography(
                context,
                languageId,
                holder.new_states_name,
                holder.second_new_states_name
        );
        ThemeColorHelper.applySelectedNeomorphTree(context, holder.second_states_neomorphFrameLayout);
        ThemeColorHelper.applySelectedChip(context, holder.second_states_neomorphFrameLayout, holder.second_new_states_name);

        StatesModel.Datum state = arrayList.get(position);
        holder.new_states_name.setText(state.getName());
        holder.second_new_states_name.setText(state.getName());

        bindLandmark(holder.state_landmark, state, false);
        bindLandmark(holder.second_state_landmark, state, true);

        View.OnClickListener rowClick = v -> onStateSelected(holder.getBindingAdapterPosition());
        holder.states_neomorphFrameLayout.setOnClickListener(rowClick);
        holder.second_states_neomorphFrameLayout.setOnClickListener(rowClick);
        holder.new_states_name.setOnClickListener(rowClick);
        holder.second_new_states_name.setOnClickListener(rowClick);
        holder.state_landmark.setOnClickListener(rowClick);
        holder.second_state_landmark.setOnClickListener(rowClick);

        if(index==position){

            if(state.getId() != null && state.getId() == -1){

                UpdateStates(state.getId());

            }
            holder.states_neomorphFrameLayout.setVisibility(View.GONE);
            holder.second_states_neomorphFrameLayout.setVisibility(View.VISIBLE);

        }else{

            holder.states_neomorphFrameLayout.setVisibility(View.VISIBLE);
            holder.second_states_neomorphFrameLayout.setVisibility(View.GONE);
        }
    }

    private void onStateSelected(int position) {
        if (position < 0 || position >= arrayList.size()) {
            return;
        }
        StatesModel.Datum state = arrayList.get(position);

        index = position;
        notifyDataSetChanged();

        SharedPrefsData.putInt(context, Constants.StateID, state.getId(), Constants.PREF_NAME);

        if (state.getId() != null && state.getId() == -1) {
            UpdateStates(state.getId());
        } else {
            SharedPrefsData.putInt(context, Constants.Back_Button_Move, 2, Constants.PREF_NAME);
            SharedPrefsData.putString(context, Constants.MOVE_NEXT_SCREEN, "district", Constants.PREF_NAME);
            Intent intent = new Intent(context, Districs.class);
            context.startActivity(intent);
        }
    }

    private void bindLandmark(ImageView imageView, StatesModel.Datum state, boolean selected) {
        String iconUrl = state.getIconUrl();
        if (!TextUtils.isEmpty(iconUrl)) {
            imageView.clearColorFilter();
            Picasso.get()
                    .load(MediaUrl.resolve(iconUrl))
                    .placeholder(getLandmarkDrawable(state.getName(), state.getId()))
                    .error(getLandmarkDrawable(state.getName(), state.getId()))
                    .fit()
                    .centerInside()
                    .into(imageView);
            return;
        }

        int landmark = getLandmarkDrawable(state.getName(), state.getId());
        imageView.setImageResource(landmark);
        if (selected) {
            imageView.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        } else {
            imageView.clearColorFilter();
            ThemeColorHelper.applyAccentTint(context, imageView);
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder  extends RecyclerView.ViewHolder{
        NeomorphFrameLayout states_neomorphFrameLayout,second_states_neomorphFrameLayout;
        TextView new_states_name,second_new_states_name;
        ImageView state_landmark,second_state_landmark;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            states_neomorphFrameLayout=itemView.findViewById(R.id.states_neomorphFrameLayout);
            second_states_neomorphFrameLayout=itemView.findViewById(R.id.second_states_neomorphFrameLayout);
            new_states_name=itemView.findViewById(R.id.new_states_name);
            second_new_states_name=itemView.findViewById(R.id.second_new_states_name);
            state_landmark=itemView.findViewById(R.id.state_landmark);
            second_state_landmark=itemView.findViewById(R.id.second_state_landmark);
            AppLanguagesFonts.setTypeFace(new_states_name);
            AppLanguagesFonts.setTypeFace(second_new_states_name);

        }
    }

    /**
     * Fallback landmark icons when no admin-uploaded icon_url is available.
     */
    private int getLandmarkDrawable(String name, Integer id) {
        if (id != null && id == -1) {
            return R.drawable.ic_landmark_all;
        }
        if (name == null) {
            return R.drawable.ic_landmark_monument;
        }
        String n = name.toLowerCase();
        if (n.contains("telangana") || n.contains("తెలంగాణ") || n.contains("तेलंगाना")) {
            return R.drawable.ic_landmark_charminar;
        }
        if (n.contains("andhra") || n.contains("ఆంధ్ర") || n.contains("आंध्र")) {
            return R.drawable.ic_landmark_buddha;
        }
        if (n.contains("all") || n.contains("అన్ని") || n.contains("सभी")) {
            return R.drawable.ic_landmark_all;
        }
        return R.drawable.ic_landmark_monument;
    }

    private JsonObject set_update_states() {

        LanguageSelectionRequest requestModel = new LanguageSelectionRequest();

        requestModel.setDraftId(SharedPrefsData.getInt(context, Constants.DraftID, Constants.PREF_NAME));
        requestModel.setStateId(-1);
        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        encrpt_data = encrypt(jsonString);
        showLog("API REQUEST E" + requestModel.toString());
        EncritonRequestData Encriton_Request=new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }
    public void UpdateStates(int id){

        JsonObject otp = set_update_states();

        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.update_state("v3",otp);

        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {

                arrayList.clear();
                EncreptionRequest encreptionRequest=response.body();
                showLog("API RES   "+response.body().getData());
                String encrpt_data =  decrypt(encreptionRequest.getData());



                Gson gsonObj = new Gson();
                UpdateStateResponse languageModel  = gsonObj.fromJson(encrpt_data, UpdateStateResponse.class);

                if (languageModel.getSuccess()) {

                    SharedPrefsData.putString(context, Constants.Selected_Distric_Name, languageModel.getDist_name(), Constants.PREF_NAME);
                    SharedPrefsData.putString(context, Constants.Selected_Distric_Name1, languageModel.getDist_name(), Constants.PREF_NAME);
                    SharedPrefsData.putString(context, Constants.MOVE_NEXT_SCREEN, "main", Constants.PREF_NAME);
                    SharedPrefsData.putBool(context, Constants.IS_LANGUAGE_SELECTED, true, Constants.PREF_NAME);

                    Intent intent=new Intent(context, HomePage.class);
                     intent.putExtra("EXTRA_SESSION_ID", "0");
                    if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)){
                        context.startActivity(intent);
                    }


                }
            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {


            }
        });
    }
}
