package com.anyNews.anynews.Activities;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.anyNews.anynews.Pojo.LanguageSelectionResponse;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.google.gson.Gson;

public class NotificationsActivity extends AppCompatActivity {

    ImageView img_back;
    TextView title;
    Allsharedpreference allsharedpreference;
    LanguageSelectionResponse languageSelectionResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_notifications);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        allsharedpreference = new Allsharedpreference(this);
        img_back = findViewById(R.id.img_back);
        title = findViewById(R.id.title);


        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });



        setStaicdata();
    }

    private void setStaicdata() {
        Gson gson = new Gson();
        String Translation = allsharedpreference.getShared_String(Allsharedpreference.Translator);
        languageSelectionResponse = gson.fromJson(Translation, LanguageSelectionResponse.class);

//        title.setText(languageSelectionResponse.getExtras().getData().getNotifications());

    }
}
