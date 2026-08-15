package com.anyNews.anynews.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.LikeDownloadRespouse;
import com.anyNews.anynews.Pojo.LikeSelectionRequest;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.AvatarImageHelper;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.SelfFollowGuard;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.models.FollowingMenderModel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import soup.neumorphism.NeumorphCardView;

import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;


public class FollowingMemberAdopter extends RecyclerView.Adapter<FollowingMemberAdopter.ViewHolder> {

    Context context;
    ArrayList<FollowingMenderModel.Datum> followersProfileModel;

    public FollowingMemberAdopter(Context context,  ArrayList<FollowingMenderModel.Datum>  followersProfileModel) {
        this.context = context;
        this.followersProfileModel = followersProfileModel;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater=LayoutInflater.from(context);
        View view=layoutInflater.inflate(R.layout.followers_item_list, parent, false);
        return new FollowingMemberAdopter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FollowingMenderModel.Datum item = followersProfileModel.get(position);
        AvatarImageHelper.loadPicasso(holder.follower_images, item.getProfilePic());
        holder.follower_menber_name.setText(item.getWriterName());
        holder.follower_menber_designation.setText(item.getRole());
        holder.follower_menber_location.setText(item.getLocation());
        final boolean self = SelfFollowGuard.isSelf(context, item.getWriterId());
        if (self) {
            holder.neumorph_follow.setVisibility(View.GONE);
            holder.neumorph_follower.setVisibility(View.GONE);
            holder.follower_menber_follow.setOnClickListener(null);
            holder.follower_menber_follow_er.setOnClickListener(null);
            return;
        }
        bindFollowState(holder, item);

        holder.follower_menber_follow.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION || pos >= followersProfileModel.size()) return;
            FollowingMenderModel.Datum datum = followersProfileModel.get(pos);
            if (datum.getWriterId() == null || SelfFollowGuard.isSelf(context, datum.getWriterId())) return;
            datum.setIsFollow(1);
            bindFollowState(holder, datum);
            set_follow_un_follow(datum.getWriterId(), 1, pos, holder);
        });
        holder.follower_menber_follow_er.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION || pos >= followersProfileModel.size()) return;
            FollowingMenderModel.Datum datum = followersProfileModel.get(pos);
            if (datum.getWriterId() == null || SelfFollowGuard.isSelf(context, datum.getWriterId())) return;
            datum.setIsFollow(0);
            bindFollowState(holder, datum);
            set_follow_un_follow(datum.getWriterId(), 0, pos, holder);
        });
    }

    private void bindFollowState(ViewHolder holder, FollowingMenderModel.Datum item) {
        int follow = item.getIsFollow() != null ? item.getIsFollow() : 0;
        if (follow == 0) {
            holder.neumorph_follow.setVisibility(View.VISIBLE);
            holder.neumorph_follower.setVisibility(View.GONE);
        } else {
            holder.neumorph_follow.setVisibility(View.GONE);
            holder.neumorph_follower.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public int getItemCount() {

        return followersProfileModel.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        NeumorphCardView neumorph_follower,neumorph_follow;
        ImageView follower_images;
        TextView follower_menber_name,follower_menber_designation,follower_menber_location,follower_menber_follow,follower_menber_follow_er;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            follower_images=itemView.findViewById(R.id.follower_images);
            follower_menber_name=itemView.findViewById(R.id.follower_menber_name);
            follower_menber_designation=itemView.findViewById(R.id.follower_menber_designation);
            follower_menber_location=itemView.findViewById(R.id.follower_menber_location);
            follower_menber_follow=itemView.findViewById(R.id.follower_menber_follow);
            follower_menber_follow_er=itemView.findViewById(R.id.follower_menber_follow_er);
            neumorph_follower=itemView.findViewById(R.id.neumorph_follower);
            neumorph_follow=itemView.findViewById(R.id.neumorph_follow);
        }

    }

    private JsonObject set_follow_un_follow_request(Long writer_id,int is_follow) {

        LikeSelectionRequest requestModel = new LikeSelectionRequest();

        requestModel.setDraft_id((long) SharedPrefsData.getInt(context, Constants.DraftID, Constants.PREF_NAME));
        requestModel.setWriter_id(writer_id);
        requestModel.setIs_follow(is_follow);

        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            encrpt_data = encrypt(jsonString);
        }*/
        encrpt_data = encrypt(jsonString);
        showLog("API REQUEST E" + jsonString.toString());
        EncritonRequestData Encriton_Request=new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }

    private void set_follow_un_follow(long writer_id, int is_follow, final int position, final ViewHolder holder) {
        JsonObject jsonObject = set_follow_un_follow_request(writer_id, is_follow);
        showLog("API REQ   " + jsonObject.toString());
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.update_follow("v3", jsonObject);
        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                if (response.body() == null || response.body().getData() == null) {
                    revertFollowState(position, holder, is_follow);
                    return;
                }
                try {
                    String encrpt_data = decrypt(response.body().getData());
                    LikeDownloadRespouse likeDownloadRespouse = new Gson().fromJson(encrpt_data, LikeDownloadRespouse.class);
                    if (likeDownloadRespouse == null || !Boolean.TRUE.equals(likeDownloadRespouse.getSuccess())) {
                        revertFollowState(position, holder, is_follow);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    revertFollowState(position, holder, is_follow);
                }
            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                revertFollowState(position, holder, is_follow);
            }
        });
    }

    private void revertFollowState(int position, ViewHolder holder, int attemptedFollow) {
        if (position < 0 || position >= followersProfileModel.size()) return;
        FollowingMenderModel.Datum item = followersProfileModel.get(position);
        item.setIsFollow(attemptedFollow == 1 ? 0 : 1);
        bindFollowState(holder, item);
    }
}
