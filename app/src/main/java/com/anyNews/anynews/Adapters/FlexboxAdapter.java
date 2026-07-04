package com.anyNews.anynews.Adapters;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.anyNews.anynews.Activities.HomePage;
import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.Pojo.NotificationAllNotificationsResponse;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Utils.AppHelper;
import com.anyNews.anynews.models.SearchModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import soup.neumorphism.NeumorphCardView;

public class FlexboxAdapter extends RecyclerView.Adapter<FlexboxAdapter.ViewHolder> {

    Context context;
    ArrayList<SearchModel.Datum> arrayList = new ArrayList<>();
    int index = -1;

    public FlexboxAdapter(Context context, ArrayList<SearchModel.Datum> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NotNull
    @Override
    public FlexboxAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FlexboxAdapter.ViewHolder holder, int position) {
        SearchModel.Datum searchModel=arrayList.get(position);
        holder.language_name.setText(" + "+searchModel.getName());
        holder.selected_language_name.setText(searchModel.getName());


        holder.selected_language_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.selected_search_text.setVisibility(View.GONE);
                holder.search_text.setVisibility(View.VISIBLE);
                index = position;
                notifyDataSetChanged();
            }
        });
        holder.search_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.selected_search_text.setVisibility(View.VISIBLE);
                holder.search_text.setVisibility(View.GONE);
                index = position;
                notifyDataSetChanged();
            }
        });

        if(index==position){

            // neomorph_background_color
            holder.selected_search_text.setVisibility(View.VISIBLE);
            holder.search_text.setVisibility(View.GONE);

            Intent intent=new Intent(context, HomePage.class);
            intent.putExtra("EXTRA_SESSION_ID", ""+searchModel.getCategoryId());
            if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)){
                context.startActivity(intent);
            }

         //   Intent intent=new Intent( S)

        }else{

            holder.selected_search_text.setVisibility(View.GONE);
            holder.search_text.setVisibility(View.VISIBLE);

        }
    }

    @Override
    public int getItemCount() {
        int a ;

        if(arrayList != null && !arrayList.isEmpty()) {

            a = arrayList.size();
        }
        else {

            a = 0;

        }

        return a;

        //return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView language_name,selected_language_name;
        NeumorphCardView  search_text,selected_search_text;
        public ViewHolder(View itemView) {
            super(itemView);
            language_name = itemView.findViewById(R.id.language_name);
            search_text=itemView.findViewById(R.id.search_text);
            selected_language_name=itemView.findViewById(R.id.selected_language_name);
            selected_search_text=itemView.findViewById(R.id.selected_search_text);

        }
    }
}
