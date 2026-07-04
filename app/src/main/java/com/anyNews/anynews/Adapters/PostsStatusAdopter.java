package com.anyNews.anynews.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Utils.CommonMethods;
import com.anyNews.anynews.models.PostsModel;

import java.util.ArrayList;

public class PostsStatusAdopter extends RecyclerView.Adapter<PostsStatusAdopter.ViewHolder> {


    Context context;
    ArrayList<PostsModel.Datum>postsModels;
    int VALUE=0;
    View view;
    CommonMethods commonMethods;
    public PostsStatusAdopter(Activity activity,Context context, ArrayList<PostsModel.Datum> postsModels, int VALUE) {
        this.context = context;
        this.postsModels = postsModels;
        this.VALUE=VALUE;
        commonMethods = new CommonMethods(activity);

        Toast.makeText(context, " VVVV  "+postsModels.size(), Toast.LENGTH_SHORT).show();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater=LayoutInflater.from(context);


            view =layoutInflater.inflate(R.layout.post_status_rejected_item, parent, false);

        return new PostsStatusAdopter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        PostsModel.Datum datum=postsModels.get(position);

        Toast.makeText(context, "  FFF  "+postsModels.size(), Toast.LENGTH_SHORT).show();

        holder.post_discrepton.setText(datum.getDescription());
        holder.post_title.setText(datum.getTitle());
        holder.post_time.setText(""+commonMethods.getHrsFormatfromDate(datum.getCreatedAt()));
        holder.post_reject.setText( ""+datum.getReason());

        if(datum.getPost_status()==1){

            holder.reject.setBackgroundResource(R.drawable.post_green_detailes);

        }else if(datum.getPost_status()==2){

            holder.reject.setBackgroundResource(R.drawable.post_red_detailes);

        }else {

            holder.reject.setBackgroundResource(R.drawable.post_yellow_detailes);

        }

    }

    @Override
    public int getItemCount() {
        return postsModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout reject;
        TextView post_title,post_discrepton,post_time,post_reject;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            reject=itemView.findViewById(R.id.reject);
            post_title=itemView.findViewById(R.id.post_title);
            post_discrepton=itemView.findViewById(R.id.post_discrepton);
            post_time=itemView.findViewById(R.id.post_time);
            post_reject=itemView.findViewById(R.id.post_reject);
        }
    }
}
