package com.anyNews.anynews.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anyNews.anynews.Pojo.ListOfCommentsResponse;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.AppHelper;
import com.anyNews.anynews.Utils.CommonMethods;
import com.anyNews.anynews.Utils.MediaUrl;
import com.anyNews.anynews.models.EmailData;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class MailAdapter extends RecyclerView.Adapter<MailAdapter.MailViewHolder> {

    private Context mContext;
    private  List<ListOfCommentsResponse.Datum> mEmailData;
    CommonMethods commonMethods;
    ShowReportLayout showReportLayout;
    Allsharedpreference allsharedpreference;

    public MailAdapter(Context mContext, List<ListOfCommentsResponse.Datum> mEmailData,ShowReportLayout showReportLayout) {
        this.mContext = mContext;
        this.mEmailData = mEmailData;
        this.showReportLayout =  showReportLayout;
        commonMethods = new CommonMethods((Activity)mContext);
        allsharedpreference=new Allsharedpreference(mContext);
    }

    @NonNull
    @Override
    public MailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comments_items_list, parent, false);
        return new MailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MailViewHolder holder, int position) {

        ListOfCommentsResponse.Datum datum=mEmailData.get(position);


        holder.comment_time.setText(commonMethods.getHrsFormatfromDate(mEmailData.get(position).getCommentTime()));
        holder.comment_details.setText(datum.getComment());

        if((mEmailData.get(position).getUser_profile_pic().isEmpty()||mEmailData.get(position).getUser_profile_pic().equals("")||mEmailData.get(position).getUser_profile_pic().equals(null)||mEmailData.get(position).getUser_profile_pic().equals("null"))) {

            Picasso.get().load(MediaUrl.resolve(mEmailData.get(position).getUser_profile_pic())).into(holder.circular_image);    // mEmailData.get(position).getThumbnail()
        }
        AppHelper.showLog("EMAIL ID SERVER:"+datum.getEmail());
        AppHelper.showLog("EMAIL ID LOCAL:"+allsharedpreference.getEmailId());
        if(allsharedpreference.getEmailId()!=null && datum.getEmail()!=null && allsharedpreference.getEmailId().equalsIgnoreCase(datum.getEmail())){
            holder.imageViewReport.setVisibility(View.GONE);
            holder.person_name.setText("Me");
        }else{
            holder.imageViewReport.setVisibility(View.VISIBLE);
            holder.person_name.setText(mEmailData.get(position).getUserName());
        }
        holder.imageViewReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showReportLayout.showReportLayout(position);

            }
        });
    }

    @Override
    public int getItemCount() {
        int a ;

        if(mEmailData != null && !mEmailData.isEmpty()) {

            a = mEmailData.size();
        }
        else {

            a = 0;

        }

        return a;

        //return mEmailData.size();
    }

    public class MailViewHolder extends RecyclerView.ViewHolder {

        TextView comment_details;
        TextView person_name;
        TextView comment_time;
        CircleImageView circular_image;
        ImageView imageViewReport;

        public MailViewHolder(@NonNull View itemView) {
            super(itemView);

            comment_details = itemView.findViewById(R.id.comment_details);
            person_name = itemView.findViewById(R.id.person_name);
            comment_time = itemView.findViewById(R.id.comment_time);
            circular_image=itemView.findViewById(R.id.circular_image);
            imageViewReport=itemView.findViewById(R.id.imageViewReport);

        }
    }
    public interface ShowReportLayout {
        void showReportLayout(int position);
    }
}