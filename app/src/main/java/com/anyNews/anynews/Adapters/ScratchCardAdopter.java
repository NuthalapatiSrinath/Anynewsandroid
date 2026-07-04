//package com.anyNews.anynews.Adapters;
//
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.app.Dialog;
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.Color;
//import android.graphics.drawable.ColorDrawable;
//import android.os.Build;
//import android.transition.Fade;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.ViewGroupOverlay;
//import android.view.WindowManager;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.RequiresApi;
//import androidx.core.app.ActivityOptionsCompat;
//import androidx.core.view.ViewCompat;
//import androidx.localbroadcastmanager.content.LocalBroadcastManager;
//import androidx.recyclerview.widget.GridLayoutManager;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.anyNews.anynews.Activities.DailogBox;
//import com.anyNews.anynews.Activities.ScratchCardsList;
//import com.anyNews.anynews.BuildConfig;
//import com.anyNews.anynews.Pojo.EncreptionRequest;
//import com.anyNews.anynews.Pojo.EncritonRequestData;
//import com.anyNews.anynews.Pojo.ScratchCardListRequiest;
//import com.anyNews.anynews.R;
//import com.anyNews.anynews.Services.APIService;
//import com.anyNews.anynews.Services.RestApiClient;
//import com.anyNews.anynews.Utils.CommonMethods;
//import com.anyNews.anynews.Utils.Constants;
//import com.anyNews.anynews.Utils.SharedPrefsData;
//import com.anyNews.anynews.models.ScratchCardModel;
//import com.goibibo.libs.views.ScratchRelativeLayoutView;
//import com.google.gson.Gson;
//import com.google.gson.JsonObject;
//
//import java.util.ArrayList;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//import static com.anyNews.anynews.Utils.AppHelper.showLog;
//import static com.anyNews.anynews.Utils.MyUtils.decrypt;
//import static com.anyNews.anynews.Utils.MyUtils.encrypt;
//
//public class ScratchCardAdopter extends RecyclerView.Adapter<ScratchCardAdopter.MyviewData> {
//
//    Context context;
//    ArrayList<ScratchCardModel.Datum> scratchCardRespounses;
//    ViewGroup viewGroup;
//    ScratchRelativeLayoutView scratchRelativeLayoutView;
//    LinearLayout parent_layout;
//    Dialog dialog12;
//    CommonMethods commonMethods;
//    String REFER_LINK;
//     boolean single_time_open=false;
//
//    public ScratchCardAdopter(Activity activity,Context context, ArrayList<ScratchCardModel.Datum> scratchCardRespounses,String REFER_LINK) {
//        this.context = context;
//        this.scratchCardRespounses = scratchCardRespounses;
//        this.REFER_LINK=REFER_LINK;
//        commonMethods = new CommonMethods(activity);
//    }
//
//    @NonNull
//    @Override
//    public MyviewData onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        LayoutInflater layoutInflater=LayoutInflater.from(context);
//        View view=layoutInflater.inflate(R.layout.scratch_items_list, parent, false);
//        return new ScratchCardAdopter.MyviewData(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull MyviewData holder, int position) {
//
//        ScratchCardModel.Datum scratchCardModel=scratchCardRespounses.get(position);
//
//        holder.scartch_card_amount.setText("\u20B9"+scratchCardModel.getPoints());
//        holder.processing_text_view_amount.setText(""+commonMethods.getHrsFormatfromDateForMonth(scratchCardModel.getActiveTime()));
//
//
//
//        if(scratchCardModel.getStatus()){
//
//
//            if(scratchCardModel.getScratchStatus()){
//
//                holder.one.setVisibility(View.VISIBLE);
//                holder.three.setVisibility(View.GONE);
//                holder.two.setVisibility(View.GONE);
//
//            }
//
//            else {
//
//                holder.two.setVisibility(View.VISIBLE);
//                holder.three.setVisibility(View.GONE);
//                holder.one.setVisibility(View.GONE);
//            }
//
//        }else {
//
//            holder.one.setVisibility(View.GONE);
//            holder.three.setVisibility(View.VISIBLE);
//            holder.two.setVisibility(View.GONE);
//        }
//
//
//
//
//        holder.one.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                single_time_open=true;
//
//                open_ScartchCards(scratchCardModel.getUserName(),scratchCardModel.getPoints(),scratchCardModel.getCardId());
//            }
//        });
//        holder.three.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                single_time_open=true;
//                Before_ShowCustomDialog(scratchCardModel.getUserName(),scratchCardModel.getPoints(),scratchCardModel.getCardId(),commonMethods.getHrsFormatfromDateForMonth(scratchCardModel.getActiveTime()));
//            }
//        });
//        holder.two.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                single_time_open=true;
//             showCustomDialog(scratchCardModel.getUserName(),scratchCardModel.getPoints(),scratchCardModel.getCardId());
//            }
//        });
//
//    }
//
//    @Override
//    public int getItemCount() {
//
//        return    scratchCardRespounses.size();
//    }
//
//    public class MyviewData extends RecyclerView.ViewHolder {
//        LinearLayout two,one,three;
//        TextView scartch_card_amount,processing_text_view_amount;
//
//        public MyviewData(@NonNull View itemView) {
//            super(itemView);
//            two=itemView.findViewById(R.id.two);
//            one=itemView.findViewById(R.id.one);
//            three=itemView.findViewById(R.id.three);
//            processing_text_view_amount=itemView.findViewById(R.id.disble_time_for_scartch);
//            scartch_card_amount=itemView.findViewById(R.id.scartch_card_amount);
//            viewGroup = itemView.findViewById(android.R.id.content);
//        }
//    }
//
//    //Congratulations You've Won 3
//
//    private  void open_ScartchCards(String refered_person,long scratch_value,long card_id){
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        View dialogView = LayoutInflater.from(context).inflate(R.layout.custom_dialog, viewGroup, false);
//        builder.setView(dialogView);
//        final AlertDialog alertDialog = builder.create();
//        alertDialog.show();
//        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        scratchRelativeLayoutView = dialogView.findViewById(R.id.scratch_card);
//        LinearLayout scratch_card_refer=dialogView.findViewById(R.id.scratch_card_refer);
//        TextView refered_user_name=dialogView.findViewById(R.id.refered_user_name);
//        TextView after_revel_scartch_card=dialogView.findViewById(R.id.after_revel_scartch_card);
//        parent_layout=dialogView.findViewById(R.id.parent_layout);
//        refered_user_name.setText("Earned For Referring  "+refered_person);
//        after_revel_scartch_card.setText("Congratulations!");
//
//        scratch_card_refer.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                Intent intent = new Intent();
//                intent.setAction(Intent.ACTION_SEND);
//                intent.putExtra(Intent.EXTRA_TEXT,  ""+REFER_LINK);
//                intent.setType("text/plain");
//                if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)){
//                    context.startActivity(intent);
//                }
//
//            }
//        });
//
//
//        ImageView back_button=dialogView.findViewById(R.id.back_button);
//        TextView offer_amount_rupess=dialogView.findViewById(R.id.offer_amount_rupess);
//        offer_amount_rupess.setText("\u20B9"+scratch_value);
//        alertDialog.setCanceledOnTouchOutside(false);
//
//        scratchRelativeLayoutView.setScratchView(R.layout.open_scartch_cards);
//        TextView scartch_card_opended_amount=scratchRelativeLayoutView.findViewById(R.id.scartch_card_opended_amount);
//        scartch_card_opended_amount.setText("\u20B9"+scratch_value);
//      //  offer_amount_rupess.setText("\u20B9"+scratch_value+" dd");
//        scratchRelativeLayoutView.setStrokeWidth(10);
//
//        back_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                alertDialog.dismiss();
//
//            }
//        });
//        parent_layout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                alertDialog.dismiss();
//            }
//        });
//
//
//        alertDialog.show();
//    }
//
//    private void Before_ShowCustomDialog(String refered_person,long scratch_value,long card_id,String date){
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        View dialogView = LayoutInflater.from(context).inflate(R.layout.loacked_scartch_card_item, viewGroup, false);
//        builder.setView(dialogView);
//        final AlertDialog alertDialog = builder.create();
//        alertDialog.show();
//        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        scratchRelativeLayoutView = dialogView.findViewById(R.id.scratch_card);
//        parent_layout=dialogView.findViewById(R.id.parent_layout);
//        TextView refered_user_name=dialogView.findViewById(R.id.refered_user_name);
//        TextView after_revel_scartch_card=dialogView.findViewById(R.id.after_revel_scartch_card);
//         LinearLayout scratch_card_refer=dialogView.findViewById(R.id.scratch_card_refer);
//        ImageView back_button=dialogView.findViewById(R.id.back_button);
//        TextView locked_scartch_card_value=dialogView.findViewById(R.id.locked_scartch_card_value);
//        TextView offer_amount_rupess=dialogView.findViewById(R.id.offer_amount_rupess);
//      //  offer_amount_rupess.setText("\u20B9"+scratch_value);
//        refered_user_name.setText("Earned For Referring  "+refered_person);
//
//        after_revel_scartch_card.setText("Come back on "+date);
//        locked_scartch_card_value.setText(date);
//        scratchRelativeLayoutView.setStrokeWidth(10);
//        alertDialog.setCanceledOnTouchOutside(false);
//
//
//        scratch_card_refer.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                Intent intent = new Intent();
//                intent.setAction(Intent.ACTION_SEND);
//                intent.putExtra(Intent.EXTRA_TEXT,  ""+REFER_LINK);
//                intent.setType("text/plain");
//                if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)){
//                    context.startActivity(intent);
//                }
//
//            }
//        });
//
//
//        scratchRelativeLayoutView.setScratchView(R.layout.before_scartch_card_open);
//        TextView before_locked_scartch_card_value=scratchRelativeLayoutView.findViewById(R.id.before_locked_scartch_card_value);
//        before_locked_scartch_card_value.setText(date);
//
//        back_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                alertDialog.dismiss();
//            }
//        });
//        parent_layout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                alertDialog.dismiss();
//            }
//        });
//
//        scratchRelativeLayoutView.setRevealListener(new ScratchRelativeLayoutView.IRevealListener() {
//            @Override
//            public void onRevealed(ScratchRelativeLayoutView tv) {
//                scratchRelativeLayoutView.reveal();
//
//            }
//
//            @Override
//            public void onRevealPercentChangedListener(ScratchRelativeLayoutView siv, float percent) {
//
//
//                scratchRelativeLayoutView.reveal();
//
//
//
//            }
//        });
//        alertDialog.show();
//    }
//
//    private void showCustomDialog(String refered_person,long scratch_value,long card_id) {
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        View dialogView = LayoutInflater.from(context).inflate(R.layout.custom_dialog, viewGroup, false);
//        builder.setView(dialogView);
//        final AlertDialog alertDialog = builder.create();
//        alertDialog.show();
//        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        scratchRelativeLayoutView = dialogView.findViewById(R.id.scratch_card);
//        LinearLayout scratch_card_refer=dialogView.findViewById(R.id.scratch_card_refer);
//        TextView refered_user_name=dialogView.findViewById(R.id.refered_user_name);
//        TextView after_revel_scartch_card=dialogView.findViewById(R.id.after_revel_scartch_card);
//        ImageView after_scartch_card_opended=dialogView.findViewById(R.id.after_scartch_card_opended);
//        parent_layout=dialogView.findViewById(R.id.parent_layout);
//        refered_user_name.setText("Earned For Referring  "+refered_person);
//
//        scratch_card_refer.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                Intent intent = new Intent();
//                intent.setAction(Intent.ACTION_SEND);
//                intent.putExtra(Intent.EXTRA_TEXT,  ""+REFER_LINK);
//                intent.setType("text/plain");
//                if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)){
//                    context.startActivity(intent);
//                }
//
//            }
//        });
//       ImageView back_button=dialogView.findViewById(R.id.back_button);
//       TextView offer_amount_rupess=dialogView.findViewById(R.id.offer_amount_rupess);
//
//       if(scratch_value==0){
//           after_scartch_card_opended.setImageResource(R.drawable.ic_better_lock_icon);
//           offer_amount_rupess.setVisibility(View.GONE);
//       }else {
//           offer_amount_rupess.setVisibility(View.VISIBLE);
//           after_scartch_card_opended.setImageResource(R.drawable.ic_scartch_card_after_icon);
//       }
//        offer_amount_rupess.setText("\u20B9"+scratch_value);
//        scratchRelativeLayoutView.setStrokeWidth(10);
//        alertDialog.setCanceledOnTouchOutside(false);
//
//
//        scratchRelativeLayoutView.setScratchView(R.layout.lyt_scratch);
//
//        back_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                alertDialog.dismiss();
//            }
//        });
//        parent_layout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                alertDialog.dismiss();
//            }
//        });
//
//        scratchRelativeLayoutView.setRevealListener(new ScratchRelativeLayoutView.IRevealListener() {
//            @Override
//            public void onRevealed(ScratchRelativeLayoutView tv) {
//
//            }
//
//            @Override
//            public void onRevealPercentChangedListener(ScratchRelativeLayoutView siv, float percent) {
//
//                if(percent>0.3){
//
//                    scratchRelativeLayoutView.reveal();
//                    if( single_time_open){
//
//
//                        single_time_open=false;
//                        set_update_scratch_cards(card_id);
//
//                    }
//
//                    if(scratch_value==0){
//                        after_revel_scartch_card.setText(" ");
//                    }else {
//                        after_revel_scartch_card.setText("Congratulations!");
//
//                    }
//                }
//
//            }
//        });
//        alertDialog.show();
//    }
//
//    private JsonObject get_update_scratch_cards(long card_id) {
//
//        ScratchCardListRequiest requestModel = new ScratchCardListRequiest();
//
//        requestModel.setDraftId(SharedPrefsData.getInt(context, Constants.DraftID, Constants.PREF_NAME));
//        requestModel.setUserId(SharedPrefsData.getLong(context, Constants.USER_ID, Constants.PREF_NAME));
//        requestModel.setCardId((long)card_id);
//
//
//        String jsonString = new com.google.gson.Gson().toJson(requestModel);
//        String encrpt_data = null;
//        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            encrpt_data = encrypt(jsonString);
//        }*/
//        encrpt_data = encrypt(jsonString);
//        showLog("API REQUEST E" + jsonString.toString());
//        EncritonRequestData Encriton_Request=new EncritonRequestData();
//        Encriton_Request.setData(encrpt_data);
//        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
//    }
//
//    public void set_update_scratch_cards(long card_id){
//        JsonObject otp = get_update_scratch_cards(card_id);
//
//        APIService apiService = RestApiClient.getClient().create(APIService.class);
//        Call<EncreptionRequest> apiResponse = apiService.get_update_scratch_cards("v3",otp);
//
//        apiResponse.enqueue(new Callback<EncreptionRequest>() {
//            //@RequiresApi(api = Build.VERSION_CODES.O)
//            @Override
//            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
//
//
//                EncreptionRequest encreptionRequest=response.body();
//                showLog("API RES   "+response.body().getData());
//                String encrpt_data =  decrypt(encreptionRequest.getData());
//
//                Intent comment_open = new Intent("SCARTCH_CARD_ACTIVE");
//                LocalBroadcastManager.getInstance(context).sendBroadcast(comment_open);
//
//            }
//
//
//            @Override
//            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
//
//            }
//        });
//    }
//}
