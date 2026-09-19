package com.anyNews.anynews.holder;

import static com.anyNews.anynews.Utils.AppHelper.showLog;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anyNews.anynews.Activities.FullBaseViewHolder;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Utils.MediaUrl;
import com.anyNews.anynews.Utils.CommonMethods;
import com.anyNews.anynews.testing.BaseViewHolder;
import com.anyNews.anynews.testing.PostItem;
import com.anyNews.anynews.testing.PostRecyclerAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FullPostRecyclerAdopter extends RecyclerView.Adapter<FullBaseViewHolder> {

    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_NORMAL = 1;
    private boolean isLoaderVisible = false;

    private ArrayList<PostItem.Datum> mPostItems;
    private Context context;
    CommonMethods commonMethods;

    public FullPostRecyclerAdopter(Activity activity, Context context, ArrayList<PostItem.Datum> postItems) {
        commonMethods = new CommonMethods(activity);
        this.context = context;
        this.mPostItems = postItems;
    }

    @NonNull
    @Override
    public FullBaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {
            case VIEW_TYPE_NORMAL:
                return new FullPostRecyclerAdopter.ViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.post_status_rejected_item, parent, false));
            case VIEW_TYPE_LOADING:
                return new FullPostRecyclerAdopter.ProgressHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull FullBaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemViewType(int position) {


        if (isLoaderVisible) {
            return position == mPostItems.size() - 1 ? VIEW_TYPE_LOADING : VIEW_TYPE_NORMAL;
        } else {
            return VIEW_TYPE_NORMAL;
        }
    }

    @Override
    public int getItemCount() {
        return mPostItems == null ? 0 : mPostItems.size();

    }

    public void addItems(List<PostItem.Datum> postItems) {
        mPostItems.addAll(postItems);
        notifyDataSetChanged();
    }

    public void addLoading() {

        showLog("VIEW_TYPE_NORMAL"+isLoaderVisible);

        isLoaderVisible = true;
        mPostItems.add(new PostItem.Datum());
        notifyItemInserted(mPostItems.size() - 1);
    }

    public void removeLoading() {
        isLoaderVisible = false;
        int position = mPostItems.size() - 1;
        PostItem.Datum item = getItem(position);
        if (item != null) {
            mPostItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        mPostItems.clear();
        notifyDataSetChanged();
    }

    PostItem.Datum getItem(int position) {
        return mPostItems.get(position);
    }

    public class ViewHolder extends FullBaseViewHolder {
        @BindView(R.id.post_title)
        TextView post_title;
        @BindView(R.id.reject)
        LinearLayout reject;
        @BindView(R.id.post_discrepton)
        TextView post_discrepton;
        @BindView(R.id.post_reject)
        TextView post_reject;
        @BindView(R.id.post_time)
        TextView post_time;
        @BindView(R.id.profile_articales_image)
        ImageView profile_articales_image;


        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        protected void clear() {

        }

        public void onBind(int position) {
            super.onBind(position);
            PostItem.Datum item = null;
            try {
                item = mPostItems.get(position);
            } catch (Exception e){
                e.printStackTrace();
            }
            
            long Created_at = 0;

            post_title.setText("" + item.getTitle());
            post_discrepton.setText("" + item.getDescription());
            showLog("ImageURL: "+item.getImageUrl());
            if(item.getImageUrl()!=null && !item.getImageUrl().equalsIgnoreCase("")){
                Picasso.get().load(MediaUrl.resolve(item.getImageUrl())).into(profile_articales_image);
            }else {
                Picasso.get().load(R.drawable.ic_launcher).into(profile_articales_image);
            }

            try {
                Created_at=Long.parseLong(item.getCreatedAt());
            } catch (NumberFormatException e){
                e.printStackTrace();
            }
            
            String relative = commonMethods.getHrsFormatfromDate(Created_at);
            if (relative == null || relative.isEmpty() || "null".equalsIgnoreCase(relative.trim())) {
                post_time.setText("");
            } else {
                post_time.setText(relative);
            }


            if (item.getPostStatus() == 1) {

                reject.setBackgroundResource(R.drawable.post_green_detailes);
                post_reject.setVisibility(View.GONE);
            } else if (item.getPostStatus() == 2) {

                reject.setBackgroundResource(R.drawable.post_red_detailes);
                post_reject.setVisibility(View.VISIBLE);
                post_reject.setText("" + item.getReason());

            } else {

                reject.setBackgroundResource(R.drawable.post_yellow_detailes);
                post_reject.setVisibility(View.GONE);
            }


        }
    }

    public class ProgressHolder extends FullBaseViewHolder {
        ProgressHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        protected void clear() {
        }
    }

}
