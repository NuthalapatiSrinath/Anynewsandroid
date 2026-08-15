package com.anyNews.anynews.Adapters;

import android.annotation.SuppressLint;
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

import com.anyNews.anynews.R;
import com.anyNews.anynews.Utils.CommonMethods;
import com.anyNews.anynews.holder.PaymentViewHolder;
import com.anyNews.anynews.models.PaymentHistoryModel;
import com.anyNews.anynews.testing.BaseViewHolder;
import com.anyNews.anynews.testing.PostItem;
import com.anyNews.anynews.testing.PostRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PaymentHistoryAdopter  extends RecyclerView.Adapter<PaymentViewHolder> {


    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_NORMAL = 1;
    private boolean isLoaderVisible = false;

    private ArrayList<PostItem.Datum> mPostItems;
    private Context context;

    public PaymentHistoryAdopter(Context context, ArrayList<PostItem.Datum> postItems) {
        this.context = context;
        this.mPostItems = postItems;
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {
            case VIEW_TYPE_NORMAL:
                return new PaymentHistoryAdopter.ViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false));
            case VIEW_TYPE_LOADING:
                return new PaymentHistoryAdopter.ProgressHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
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
        if (postItems == null || postItems.isEmpty()) return;
        mPostItems.addAll(postItems);
        notifyDataSetChanged();
    }

    public void setItems(List<PostItem.Datum> postItems) {
        isLoaderVisible = false;
        mPostItems.clear();
        if (postItems != null && !postItems.isEmpty()) {
            mPostItems.addAll(postItems);
        }
        notifyDataSetChanged();
    }

    public void addLoading() {
        if (isLoaderVisible) return;
        isLoaderVisible = true;
        mPostItems.add(new PostItem.Datum());
        notifyItemInserted(mPostItems.size() - 1);
    }

    public void removeLoading() {
        if (!isLoaderVisible) return;
        isLoaderVisible = false;
        int position = mPostItems.size() - 1;
        if (position < 0) return;
        PostItem.Datum item = getItem(position);
        if (item != null) {
            mPostItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        isLoaderVisible = false;
        mPostItems.clear();
        notifyDataSetChanged();
    }

    PostItem.Datum getItem(int position) {
        return mPostItems.get(position);
    }

    public class ViewHolder extends PaymentViewHolder {
        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        protected void clear() {

        }

        public void onBind(int position) {
            super.onBind(position);
        }
    }

    public class ProgressHolder extends PaymentViewHolder {
        ProgressHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        protected void clear() {

        }
    }


}