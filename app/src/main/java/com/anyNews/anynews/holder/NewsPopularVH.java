package com.anyNews.anynews.holder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.Config;
import com.anyNews.anynews.interfaces.OnItemClickListener;
import com.anyNews.anynews.models.SearchModel;
import com.bumptech.glide.Glide;

public class NewsPopularVH {


    private TextView txtNewsTitle, txtNewsDate, txtNewsCatName, txtNewsVisited,txtNewsComments;
    private ImageView imgNews;
    private CardView cardView;
    private Context context;
    private int lastPosition = -1;
    private View parentLayout;

    public NewsPopularVH(@NonNull View itemView) {

        parentLayout = itemView;
        cardView = itemView.findViewById(R.id.cardView);

        context = itemView.getContext();

    }

    public void bind(final SearchModel news, OnItemClickListener mOnItemClickListener) {


    }
}
