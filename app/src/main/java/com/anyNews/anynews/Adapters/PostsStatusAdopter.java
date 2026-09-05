package com.anyNews.anynews.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.anyNews.anynews.Activities.PostArticles;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Utils.CommonMethods;
import com.anyNews.anynews.models.PostsModel;

import java.util.ArrayList;

public class PostsStatusAdopter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_WEEK = 0;
    private static final int TYPE_POST = 1;

    Context context;
    ArrayList<PostsModel.Datum> postsModels;
    int VALUE = 0;
    /** likes | comments | views | empty = articles status list */
    String metricMode = "";
    CommonMethods commonMethods;

    public PostsStatusAdopter(Activity activity, Context context, ArrayList<PostsModel.Datum> postsModels, int VALUE) {
        this(activity, context, postsModels, VALUE, "");
    }

    public PostsStatusAdopter(Activity activity, Context context,
                              ArrayList<PostsModel.Datum> postsModels, int VALUE, String metricMode) {
        this.context = context != null ? context : activity;
        this.postsModels = postsModels != null ? postsModels : new ArrayList<PostsModel.Datum>();
        this.VALUE = VALUE;
        this.metricMode = metricMode != null ? metricMode : "";
        Activity host = activity;
        if (host == null && this.context instanceof Activity) {
            host = (Activity) this.context;
        }
        commonMethods = new CommonMethods(host);
    }

    public void setMetricMode(String metricMode) {
        this.metricMode = metricMode != null ? metricMode : "";
    }

    @Override
    public int getItemViewType(int position) {
        PostsModel.Datum d = postsModels.get(position);
        return d != null && d.isWeekHeader ? TYPE_WEEK : TYPE_POST;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        if (viewType == TYPE_WEEK) {
            View view = layoutInflater.inflate(R.layout.item_posts_week_header, parent, false);
            return new WeekHolder(view);
        }
        View view = layoutInflater.inflate(R.layout.post_status_rejected_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final PostsModel.Datum datum = postsModels.get(position);
        if (holder instanceof WeekHolder) {
            WeekHolder wh = (WeekHolder) holder;
            wh.label.setText(datum.headerLabel != null ? datum.headerLabel
                    : (datum.getWeekLabel() != null ? datum.getWeekLabel() : "Week"));
            wh.count.setText(datum.headerCount + (datum.headerCount == 1 ? " article" : " articles"));
            return;
        }

        ViewHolder vh = (ViewHolder) holder;
        vh.post_discrepton.setText(datum.getDescription() != null ? datum.getDescription() : "");
        vh.post_title.setText(datum.getTitle() != null ? datum.getTitle() : "");
        Long createdAt = datum.getCreatedAt();
        vh.post_time.setText(createdAt != null ? "" + commonMethods.getHrsFormatfromDate(createdAt) : "");

        boolean metricList = metricMode != null && !metricMode.isEmpty();
        if (metricList) {
            vh.post_reject.setText(formatMetricLine(datum));
            vh.post_reject.setTextColor(
                    ContextCompat.getColor(context, R.color.payment_history_border_title));
            vh.reject.setBackgroundResource(R.drawable.post_green_detailes);
            vh.btn_edit_resubmit.setVisibility(View.GONE);
            vh.btn_edit_resubmit.setOnClickListener(null);
            return;
        }

        vh.post_reject.setText(datum.getReason() != null ? datum.getReason() : "");

        long status = datum.getPost_status() == null ? -1 : datum.getPost_status();

        if (status == 1) {
            vh.reject.setBackgroundResource(R.drawable.post_green_detailes);
        } else if (status == 2) {
            vh.reject.setBackgroundResource(R.drawable.post_red_detailes);
        } else {
            vh.reject.setBackgroundResource(R.drawable.post_yellow_detailes);
        }

        boolean canEdit = status == 2 && "1".equals(datum.getMediaType());
        if (canEdit) {
            vh.btn_edit_resubmit.setVisibility(View.VISIBLE);
            vh.btn_edit_resubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, PostArticles.class);
                    intent.putExtra("EDIT_MODE", true);
                    intent.putExtra("USER_POST_ID", datum.getUserPostId() == null ? 0 : datum.getUserPostId());
                    intent.putExtra("EDIT_TITLE", datum.getTitle());
                    intent.putExtra("EDIT_DESCRIPTION", datum.getDescription());
                    intent.putExtra("EDIT_IMAGE_URL", datum.getImageUrl());
                    intent.putExtra("POST_VIDEO", "0");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            });
        } else {
            vh.btn_edit_resubmit.setVisibility(View.GONE);
            vh.btn_edit_resubmit.setOnClickListener(null);
        }
    }

    private String formatMetricLine(PostsModel.Datum datum) {
        int likes = datum.getLikes() != null ? datum.getLikes() : 0;
        int comments = datum.getComments() != null ? datum.getComments() : 0;
        int views = datum.getViews() != null ? datum.getViews() : 0;
        if ("likes".equalsIgnoreCase(metricMode)) {
            return likes + " likes";
        }
        if ("comments".equalsIgnoreCase(metricMode)) {
            return comments + " comments";
        }
        if ("views".equalsIgnoreCase(metricMode)) {
            return views + " views";
        }
        return likes + " likes · " + comments + " comments · " + views + " views";
    }

    @Override
    public int getItemCount() {
        return postsModels == null ? 0 : postsModels.size();
    }

    static class WeekHolder extends RecyclerView.ViewHolder {
        TextView label, count;

        WeekHolder(@NonNull View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.week_header_label);
            count = itemView.findViewById(R.id.week_header_count);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout reject;
        TextView post_title, post_discrepton, post_time, post_reject, btn_edit_resubmit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            reject = itemView.findViewById(R.id.reject);
            post_title = itemView.findViewById(R.id.post_title);
            post_discrepton = itemView.findViewById(R.id.post_discrepton);
            post_time = itemView.findViewById(R.id.post_time);
            post_reject = itemView.findViewById(R.id.post_reject);
            btn_edit_resubmit = itemView.findViewById(R.id.btn_edit_resubmit);
        }
    }
}
