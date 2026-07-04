package com.anyNews.anynews.Activities;

import static com.anyNews.anynews.Utils.AppHelper.showLog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.Logone;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.goibibo.libs.views.ScratchTextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;


public class Testing extends AppCompatActivity {

    TextView createlink, share;
    EditText user_name,type;
    String user_name_id,type_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);
        user_name=findViewById(R.id.user_name);
        type=findViewById(R.id.type);


        createlink = (TextView) findViewById(R.id.createlink);
        share = (TextView) findViewById(R.id.share);


        createlink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(user_name.getText().toString().length()>=2){

                     if(type.getText().toString().length()>=2){

                         createlink(user_name.getText().toString(),type.getText().toString());

                     }else {

                         Toast.makeText(Testing.this, "Enter Share Id", Toast.LENGTH_SHORT).show();

                     }

                }else {
                    Toast.makeText(Testing.this, "Enter User Name", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
    public void createlink(String user_name,String share){
        Logone.e("main", "create link ");
        DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://anynews.co.in/"))
                .setDynamicLinkDomain("anynews0.page.link")
                // Open links with this app on Android
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                // Open links with com.example.ios on iOS
                //.setIosParameters(new DynamicLink.IosParameters.Builder("com.example.ios").build())
                .buildDynamicLink();
//click -- link -- google play store -- inistalled/ or not  ----
        Uri dynamicLinkUri = dynamicLink.getUri();
        Logone.e("main", "  Long refer "+ dynamicLink.getUri());
        //   https://referearnpro.page.link?apn=blueappsoftware.referearnpro&link=https%3A%2F%2Fwww.blueappsoftware.com%2F
        // apn  ibi link
        // manuall link   //

        createreferlink(user_name,share);
    }

    public void createreferlink(String custid, String proid){

        //https://play.google.com/store/apps/details?id=com.anyNews.anynews&user_id=123&type=referral

        String sharelinktext  = "https://anynews0.page.link/?" +
                "link=https://anynews.co.in/myrefer.php?custid="+custid+"-"+proid+
                "&apn="+ getPackageName()+
                "&st="+"Any news Refferal link"+
                "&sd="+"Reward Coins 20"+
                "&si="+"https://www.blueappsoftware.com/logo-1.png";

        showLog("ANYNEWS_DATA"+sharelinktext);
        // shorten the link
        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                //.setLongLink(dynamicLink.getUri())
                .setLongLink(Uri.parse(sharelinktext))  // manually
                .buildShortDynamicLink()
                .addOnCompleteListener(this, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            // Short link created
                            Uri shortLink = task.getResult().getShortLink();
                            Uri flowchartLink = task.getResult().getPreviewLink();
                            Logone.e("main ", "short link "+ shortLink.toString());
                            // share app dialog
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_TEXT,  shortLink.toString());
                            intent.setType("text/plain");
                            if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)){
                                startActivity(intent);
                            }
                        } else {
                            // Error
                            // ...
                            Logone.e("main", " error "+task.getException() );
                        }
                    }
                });
    }
}