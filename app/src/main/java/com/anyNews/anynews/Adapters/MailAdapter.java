package com.anyNews.anynews.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anyNews.anynews.Pojo.ListOfCommentsResponse;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.AppHelper;
import com.anyNews.anynews.Utils.CommonMethods;
import com.anyNews.anynews.Utils.MediaUrl;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MailAdapter extends RecyclerView.Adapter<MailAdapter.MailViewHolder> {

    private Context mContext;
    private List<ListOfCommentsResponse.Datum> mEmailData;
    CommonMethods commonMethods;
    ShowReportLayout showReportLayout;
    OnReplyListener onReplyListener;
    Allsharedpreference allsharedpreference;

    public MailAdapter(Context mContext, List<ListOfCommentsResponse.Datum> mEmailData,
                       ShowReportLayout showReportLayout, OnReplyListener onReplyListener) {
        this.mContext = mContext;
        this.mEmailData = mEmailData;
        this.showReportLayout = showReportLayout;
        this.onReplyListener = onReplyListener;
        commonMethods = new CommonMethods((Activity) mContext);
        allsharedpreference = new Allsharedpreference(mContext);
    }

    @NonNull
    @Override
    public MailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comments_items_list, parent, false);
        return new MailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MailViewHolder holder, int position) {
        ListOfCommentsResponse.Datum datum = mEmailData.get(position);

        holder.comment_time.setText(commonMethods.getHrsFormatfromDate(mEmailData.get(position).getCommentTime()));
        holder.comment_details.setText(datum.getComment());

        if ((mEmailData.get(position).getUser_profile_pic().isEmpty()
                || mEmailData.get(position).getUser_profile_pic().equals("")
                || mEmailData.get(position).getUser_profile_pic().equals(null)
                || mEmailData.get(position).getUser_profile_pic().equals("null"))) {
            Picasso.get().load(MediaUrl.resolve(mEmailData.get(position).getUser_profile_pic())).into(holder.circular_image);
        }
        AppHelper.showLog("EMAIL ID SERVER:" + datum.getEmail());
        AppHelper.showLog("EMAIL ID LOCAL:" + allsharedpreference.getEmailId());
        if (allsharedpreference.getEmailId() != null && datum.getEmail() != null
                && allsharedpreference.getEmailId().equalsIgnoreCase(datum.getEmail())) {
            holder.imageViewReport.setVisibility(View.GONE);
            holder.person_name.setText("Me");
        } else {
            holder.imageViewReport.setVisibility(View.VISIBLE);
            holder.person_name.setText(mEmailData.get(position).getUserName());
        }
        holder.imageViewReport.setOnClickListener(view -> showReportLayout.showReportLayout(position));
        holder.comment_reply_btn.setOnClickListener(view -> {
            if (onReplyListener != null) {
                onReplyListener.onReply(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mEmailData != null && !mEmailData.isEmpty()) {
            return mEmailData.size();
        }
        return 0;
    }

    public class MailViewHolder extends RecyclerView.ViewHolder {
        TextView comment_details;
        TextView person_name;
        TextView comment_time;
        TextView comment_reply_btn;
        CircleImageView circular_image;
        ImageView imageViewReport;

        public MailViewHolder(@NonNull View itemView) {
            super(itemView);

            comment_details = itemView.findViewById(R.id.comment_details);
            person_name = itemView.findViewById(R.id.person_name);
            comment_time = itemView.findViewById(R.id.comment_time);
            comment_reply_btn = itemView.findViewById(R.id.comment_reply_btn);
            circular_image = itemView.findViewById(R.id.circular_image);
            imageViewReport = itemView.findViewById(R.id.imageViewReport);
        }
    }

    public interface ShowReportLayout {
        void showReportLayout(int position);
    }

    public interface OnReplyListener {
        void onReply(int position);
    }
}
