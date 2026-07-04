package com.anyNews.anynews.Adapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anyNews.anynews.R;
import com.anyNews.anynews.models.PaginationModel;
import com.anyNews.anynews.models.PostItem;
import com.anyNews.anynews.models.ScratchCardModel;

import butterknife.BindView;
import butterknife.ButterKnife;
import java.util.List;

public class PostRecyclerAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_NORMAL = 1;
    private boolean isLoaderVisible = false;

    private List<ScratchCardModel.Datum> mPostItems;
    Context context;

    public PostRecyclerAdapter(Context context,List<ScratchCardModel.Datum> postItems) {
        this.mPostItems = postItems;
        this.context=context;
    }

    @NonNull @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_main, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }



    @Override
    public int getItemCount() {
        return  mPostItems.size();
    }

    public class ViewHolder extends BaseViewHolder {

        TextView textViewTitle;

        TextView textViewDescription;

        ViewHolder(View itemView) {
            super(itemView);
          ;
        }

        @Override
        protected void clear() {

        }


    }

}
