package com.anyNews.anynews.testing;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anyNews.anynews.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostRecyclerAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_NORMAL = 1;
    private boolean isLoaderVisible = false;

    private ArrayList<PostItem.Datum> mPostItems;
    private Context context;

    public PostRecyclerAdapter(Context context, ArrayList<PostItem.Datum> postItems) {
        this.context = context;
        this.mPostItems = postItems;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {
            case VIEW_TYPE_NORMAL:
                return new ViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false));
            case VIEW_TYPE_LOADING:
                return new ProgressHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
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

    public class ViewHolder extends BaseViewHolder {
        @BindView(R.id.article_transaction_amount)
        TextView article_transaction_amount;
        @BindView(R.id.article_transaction_title)
        TextView article_transaction_title;
        @BindView(R.id.article_transaction_reason)
        TextView article_transaction_reason;
        @BindView(R.id.article_transaction_image)
        ImageView article_transaction_image;
        @BindView(R.id.article_transaction)
        LinearLayout article_transaction;
        @BindView(R.id.redeem_transaction)
        LinearLayout redeem_transaction;
        @BindView(R.id.redeem_transaction_month)
        TextView redeem_transaction_month;
        @BindView(R.id.redeem_transaction_date)
        TextView redeem_transaction_date;
        @BindView(R.id.redeem_transaction_year)
        TextView redeem_transaction_year;
        @BindView(R.id.redeem_transaction_order_id)
        TextView redeem_transaction_order_id;
        @BindView(R.id.redeem_transaction_paytm_id)
        TextView redeem_transaction_paytm_id;
        @BindView(R.id.redeem_transaction_points)
        TextView redeem_transaction_points;
        @BindView(R.id.payment_status_value)
        TextView payment_status_value;
        @BindView(R.id.payment_status_image_view)
        ImageView payment_status_image_view;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        protected void clear() {

        }

        public void onBind(int position) {
            super.onBind(position);
            PostItem.Datum item = mPostItems.get(position);


            redeem_transaction_points.setText("    "+item.getPoints());
            String item_image = "" + item.getImage();
            if (item_image.equals("") || item_image.equals("null") || item_image.equals(null)) {

                article_transaction.setVisibility(View.GONE);
                redeem_transaction.setVisibility(View.VISIBLE);

             //   showLog("NARASIMHARAO_AKKISETTY" + item.getTransactionStatus());

                if (item.getTransactionStatus() == 1) {

                    payment_status_image_view.setVisibility(View.VISIBLE);
                    payment_status_value.setVisibility(View.GONE);

                } else if (item.getTransactionStatus() == 2) {
                    payment_status_image_view.setVisibility(View.GONE);
                    payment_status_value.setVisibility(View.VISIBLE);

                    payment_status_value.setText("Success");

                } else {
                    payment_status_image_view.setVisibility(View.GONE);
                    payment_status_value.setVisibility(View.VISIBLE);

                    payment_status_value.setText("Failed");
                }


                redeem_transaction_order_id.setText("" + item.getTitle());
                redeem_transaction_paytm_id.setText("" + item.getDescription());

            } else {
                article_transaction.setVisibility(View.VISIBLE);
                redeem_transaction.setVisibility(View.GONE);

              if(item.getPoints()>1){

                article_transaction_amount.setTextColor(Color.GREEN);
                article_transaction_amount.setText("+"+item.getPoints());

              }else {

                article_transaction_amount.setText(""+item.getPoints());
                article_transaction_amount.setTextColor(Color.RED);
              }
            }

            article_transaction_title.setText("" + item.getTitle());
            article_transaction_reason.setText("" + item.getReason());
            if(item.getImage()!=null)
            Picasso.get().load(item.getImage()).into(article_transaction_image);


            String complete_date = ""+item.getCreatedAt();
            String date = complete_date.substring(0, 2);
            String month = complete_date.substring(3, 6);
            String year = complete_date.substring(7, 11);
            redeem_transaction_year.setText(year);
            redeem_transaction_month.setText(month);
            redeem_transaction_date.setText(date);




        }
    }

    public class ProgressHolder extends BaseViewHolder {
        ProgressHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        protected void clear() {
        }
    }

}
