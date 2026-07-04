package com.anyNews.anynews.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anyNews.anynews.Pojo.HelpResponse;
import com.anyNews.anynews.R;

import java.util.List;

public class AllItemsAdapter extends  RecyclerView.Adapter<AllItemsAdapter.MyViewHolder> {
    private Context context;
    List<HelpResponse.Datum> datumList;

    public AllItemsAdapter(Context context, List<HelpResponse.Datum> datumList) {
        this.context = context;
        this.datumList = datumList;

    }

    @Override
    public AllItemsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.test_expandable,parent,false);
        return new AllItemsAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final AllItemsAdapter.MyViewHolder holder, final int position) {

        holder.linearHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (datumList.get(position).getTouched()){
                    datumList.get(position).setTouched(false);
                    holder.imgArrow.setRotation(90);
                    holder.txt_individual.setVisibility(View.GONE);
                    notifyItemChanged(position,datumList.get(position));
                } else {
                    holder.imgArrow.setRotation(270);
                    datumList.get(position).setTouched(true);
                    holder.txt_individual.setVisibility(View.VISIBLE);
                    notifyItemChanged(position,datumList.get(position));
                }
            }
        });


        holder.txt_individual.setText(datumList.get(position).getDescription());
        holder.helpTitle.setText(datumList.get(position).getTitle());

    }

    @Override
    public int getItemCount() {
        return datumList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imgArrow;
        TextView helpTitle,txt_individual;
        LinearLayout linearHelp;

        public MyViewHolder(View itemView) {
            super(itemView);

            txt_individual = itemView.findViewById(R.id.txt_individual);
            helpTitle = itemView.findViewById(R.id.helpTitle);
            imgArrow = itemView.findViewById(R.id.imgArrow);
            linearHelp = itemView.findViewById(R.id.linearHelp);

        }
    }

    public interface Available{
        void available(String Id, Integer position, Boolean stock);
    }

}
