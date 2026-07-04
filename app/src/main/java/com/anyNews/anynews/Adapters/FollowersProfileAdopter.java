package com.anyNews.anynews.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.MediaUrl;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.models.FollowersProfileModel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

public class FollowersProfileAdopter extends RecyclerView.Adapter<FollowersProfileAdopter.ViewHolder> {

    Context context;
    ArrayList<FollowersProfileModel.Datum> followersProfileModel;

    public FollowersProfileAdopter(Context context, ArrayList<FollowersProfileModel.Datum> followersProfileModel) {
        this.context = context;
        this.followersProfileModel = followersProfileModel;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.followers_profile_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FollowersProfileModel.Datum item = followersProfileModel.get(position);
        if (item.getProfilePic() != null) {
            Picasso.get().load(MediaUrl.resolve(item.getProfilePic())).into(holder.img_upload_photo);
        }
        holder.beat_writer_name.setText(item.getWriterName());
        holder.beat_writer_location.setText(item.getRole());
        holder.beat_writer_likes.setText(item.getLocation());
        bindFollowLabel(holder, item);

        holder.follow_button.setOnClickListener(v -> onFollowClicked(holder));
        holder.beat_writer_follow.setOnClickListener(v -> onFollowClicked(holder));
    }

    private void onFollowClicked(ViewHolder holder) {
        int pos = holder.getAdapterPosition();
        if (pos == RecyclerView.NO_POSITION || pos >= followersProfileModel.size()) return;

        FollowersProfileModel.Datum item = followersProfileModel.get(pos);
        if (item.getWriterId() == null) return;

        int currentFollow = item.getIsFollow() != null ? item.getIsFollow() : 0;
        if (currentFollow == 0) {
            item.setIsFollow(1);
            bindFollowLabel(holder, item);
            set_follow_un_follow(item.getWriterId().longValue(), 1, pos, holder);
        } else {
            item.setIsFollow(0);
            bindFollowLabel(holder, item);
            set_follow_un_follow(item.getWriterId().longValue(), 0, pos, holder);
        }
    }

    private void bindFollowLabel(ViewHolder holder, FollowersProfileModel.Datum item) {
        int follow = item.getIsFollow() != null ? item.getIsFollow() : 0;
        holder.beat_writer_follow.setText(follow == 0 ? "Follow" : "Following");
    }

    @Override
    public int getItemCount() {
        return followersProfileModel.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView img_upload_photo;
        TextView beat_writer_name, beat_writer_location, beat_writer_likes, beat_writer_follow;
        LinearLayout follow_button;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            follow_button = itemView.findViewById(R.id.follow_button);
            beat_writer_follow = itemView.findViewById(R.id.beat_writer_follow);
            beat_writer_likes = itemView.findViewById(R.id.beat_writer_likes);
            beat_writer_location = itemView.findViewById(R.id.beat_writer_location);
            beat_writer_name = itemView.findViewById(R.id.beat_writer_name);
            img_upload_photo = itemView.findViewById(R.id.img_upload_photo);
            follow_button.setClickable(true);
        }
    }

    private JsonObject set_follow_un_follow_request(Long writer_id, int is_follow) {
        LikeSelectionRequest requestModel = new LikeSelectionRequest();
        requestModel.setDraft_id((long) SharedPrefsData.getInt(context, Constants.DraftID, Constants.PREF_NAME));
        requestModel.setWriter_id(writer_id);
        requestModel.setIs_follow(is_follow);

        String jsonString = new Gson().toJson(requestModel);
        String encrpt_data = encrypt(jsonString);
        showLog("API REQUEST E" + jsonString);
        EncritonRequestData Encriton_Request = new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }

    private void set_follow_un_follow(Long writer_id, int is_follow, final int position, final ViewHolder holder) {
        JsonObject jsonObject = set_follow_un_follow_request(writer_id, is_follow);
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        apiService.update_follow("v3", jsonObject).enqueue(new Callback<EncreptionRequest>() {
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                if (response.body() == null || response.body().getData() == null) {
                    revertFollowState(position, holder);
                    return;
                }
                try {
                    String encrpt_data = decrypt(response.body().getData());
                    LikeDownloadRespouse likeDownloadRespouse = new Gson().fromJson(encrpt_data, LikeDownloadRespouse.class);
                    if (likeDownloadRespouse != null && Boolean.TRUE.equals(likeDownloadRespouse.getSuccess())) {
                        // Discover list only shows writers not yet followed — remove after follow.
                        if (is_follow == 1 && position >= 0 && position < followersProfileModel.size()) {
                            followersProfileModel.remove(position);
                            notifyItemRemoved(position);
                            if (position < followersProfileModel.size()) {
                                notifyItemRangeChanged(position, followersProfileModel.size() - position);
                            }
                        }
                    } else {
                        revertFollowState(position, holder);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    revertFollowState(position, holder);
                }
            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                showLog("update_follow failed: " + t.getMessage());
                revertFollowState(position, holder);
            }
        });
    }

    private void revertFollowState(int position, ViewHolder holder) {
        if (position < 0 || position >= followersProfileModel.size()) return;
        FollowersProfileModel.Datum item = followersProfileModel.get(position);
        item.setIsFollow(0);
        bindFollowLabel(holder, item);
    }
}
