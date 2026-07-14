package com.anyNews.anynews.fragments;

import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anyNews.anynews.Activities.HomePage;
import com.anyNews.anynews.Activities.ReportCommentScreenActivity;
import com.anyNews.anynews.Pojo.BaseResponse;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.ListOfCommentsResponse;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CommentsBottomSheetDialog extends BottomSheetDialogFragment implements View.OnClickListener {


    public static final String TAG = "ActionBottomDialog";
    ListOfCommentsResponse.Datum lDatum;

    Allsharedpreference allsharedpreference;
    private ItemClickListener mListener;

    public static CommentsBottomSheetDialog newInstance() {
        return new CommentsBottomSheetDialog();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comments_bottom_sheet_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            assert getArguments() != null;
            lDatum = (ListOfCommentsResponse.Datum) getArguments().getSerializable("lDatum");
            showLog("lData" + lDatum.getComment());
        } catch (Exception e) {
            e.printStackTrace();
        }
        allsharedpreference = new Allsharedpreference(getActivity());



        view.findViewById(R.id.textViewReport).setOnClickListener(this);
        view.findViewById(R.id.textViewBlockUser).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.textViewBlockUser:
                blockUser();
                break;
            case R.id.textViewReport:
                startActivity(new Intent(getActivity(), ReportCommentScreenActivity.class).putExtra("data", lDatum));
                dismiss();
                break;
        }
    }


    private void blockUser() {

        APIService apiService = RestApiClient.getClient().create(APIService.class);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("comment_id", lDatum.getId());
        jsonObject.addProperty("draft_id", SharedPrefsData.getInt(getActivity(), Constants.DraftID, Constants.PREF_NAME));
        jsonObject.addProperty("email", lDatum.getUserName());
        jsonObject.addProperty("reported_email", allsharedpreference.getEmailId());
        jsonObject.addProperty("blocked", "1");
        jsonObject.addProperty("problem_id", "0");
        showLog("REPORT FLAG REQUEST:" + jsonObject.toString());
        String encrpt_data = null;
        encrpt_data = encrypt(jsonObject.toString());
        EncritonRequestData encritonRequestData = new EncritonRequestData();
        encritonRequestData.setData(encrpt_data);
        Call<EncreptionRequest> postReportFlag = apiService.postReportFlag("v3", new Gson().toJsonTree(encritonRequestData).getAsJsonObject());
        postReportFlag.enqueue(new Callback<EncreptionRequest>() {
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {

                if (response == null || !response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                    Toast.makeText(getActivity(), "Unable to process the request please try again later", Toast.LENGTH_SHORT).show();
                    if (mListener != null) mListener.reportBottomSheetDismiss();
                    dismiss();
                    return;
                }
                EncreptionRequest encreptionRequest = response.body();
                String encrpt_data = null;
                encrpt_data = decrypt(encreptionRequest.getData());
                showLog("REPORT FLAG REQUEST:" + encrpt_data);
                Gson gsonObj = new Gson();
                BaseResponse listOfCommentsResponse = gsonObj.fromJson(encrpt_data, BaseResponse.class);
                showLog("REPORT FLAG STATUS CODE:" + (listOfCommentsResponse != null ? listOfCommentsResponse.getStatusCode() : -1));
                try {
                    if (listOfCommentsResponse != null && listOfCommentsResponse.getStatusCode() == 200) {
                        Toast.makeText(getActivity(), "Request Submitted Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Unable to process the request please try again later", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (mListener != null) mListener.reportBottomSheetDismiss();
                dismiss();
            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                mListener.reportBottomSheetDismiss();

                Toast.makeText(getActivity(), "Unable to process the request please try again later", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });

    }
    public interface ItemClickListener {
        void reportBottomSheetDismiss();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ItemClickListener) {
            mListener = (ItemClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ItemClickListener");
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


}