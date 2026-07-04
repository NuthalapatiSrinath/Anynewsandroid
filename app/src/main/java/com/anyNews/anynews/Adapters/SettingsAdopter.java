package com.anyNews.anynews.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anyNews.anynews.Activities.LanguageActivity;
import com.anyNews.anynews.Activities.Settings_Scrren;
import com.anyNews.anynews.Activities.TermsAndCondition;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Ui.NeomorphFrameLayout;

import java.util.ArrayList;

class SettingsAdopter extends RecyclerView.Adapter<SettingsAdopter.ViewHolder>{

    Context context;
    ArrayList<String> stringArrayList;
    public SettingsAdopter(Context context,ArrayList<String>stringArrayList) {

        this.context = context;
        this.stringArrayList=stringArrayList;

    }

    @NonNull
    @Override
    public SettingsAdopter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.settings_notification_items,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingsAdopter.ViewHolder holder, int position) {


    }

    @Override
    public int getItemCount() {
        return stringArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView setings_name;
        Switch switchNotification;
        NeomorphFrameLayout neomorphFrameLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            setings_name=itemView.findViewById(R.id.setings_name);
            switchNotification=itemView.findViewById(R.id.switchNotification);
            neomorphFrameLayout=itemView.findViewById(R.id.neomorphFrameLayout);
        }
    }
}