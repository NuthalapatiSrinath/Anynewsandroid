package com.anyNews.anynews.Services;

import static com.anyNews.anynews.Utils.AppHelper.showLog;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.anyNews.anynews.Activities.HomePage;
import com.anyNews.anynews.Activities.StartingActivity;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.Logone;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    // firebase storage
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    String type_firebase = "push_receive";
    String device_id = "";
    long post_id_long, sent_at, draft_id = 0;
            String pn_color="#000000";
            boolean showPNTime=true;


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        try {
            showLog("PAYLOAD " + remoteMessage.getData());

            Map<String, String> params = remoteMessage.getData();
            if (params == null || params.isEmpty()) {
                if (remoteMessage.getNotification() != null) {
                    String title = remoteMessage.getNotification().getTitle();
                    if (title == null || title.isEmpty()) {
                        title = "AnyNews";
                    }
                    sendNotification(null, title, "0", "{}", pn_color, true);
                }
                return;
            }

            JSONObject object = new JSONObject(params);
            String image = object.optString("image", "");
            String title = object.optString("pn_title", object.optString("title", "AnyNews"));
            String post_id = object.optString("post_id", "0");
            if (object.has("pn_color")) {
                pn_color = object.getString("pn_color");
            }
            device_id = object.optString("device_id", "");
            int language_id = object.optInt("language_id", 0);

            try {
                post_id_long = Long.parseLong(post_id);
                sent_at = object.optLong("sent_at", System.currentTimeMillis());
                draft_id = object.optLong("draft_id", 0);
                showPNTime = !object.has("showPNTime") || object.optBoolean("showPNTime", true);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            // Always show the notification immediately — do not wait for image download.
            sendNotification(null, title, post_id, object.toString(), pn_color, showPNTime);

            Logone.e("NotificationData", object.toString());
            long current_time_firebase = System.currentTimeMillis();
            writePushReceive("User", type_firebase, post_id_long, current_time_firebase, language_id, sent_at, draft_id, device_id, showPNTime);

        } catch (Exception e) {
            e.printStackTrace();
            Logone.e("NotificationData", e.getMessage());
        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    private void fetchArticleDetails(final String body, String title, String post_id, String object, String pnColor) {

        new GetBitmapURL(body, title, post_id, object,pn_color).execute();

    }


    public void getBitmapFromURL(String src, String title, String post_id, String object,String pn_color,boolean showPNTime) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);

            sendNotification(myBitmap, title, post_id, object,pn_color,showPNTime);

        } catch (IOException e) {

            //   Log exception

        }
    }


    private void sendNotification(Bitmap imgUrl, String title, String post_id, String object,String pnColor,boolean showPNTime) {
        RemoteViews collapsedView = new RemoteViews(getPackageName(), R.layout.new_collapsed_request);
        RemoteViews expandedView = new RemoteViews(getPackageName(), R.layout.new_request);
        final Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Spannable pnTitle=new SpannableString(title);
        try {
            // String  content = title.substring(title.indexOf("#") + 1);
            Pattern p = Pattern.compile("\\[.*?\\]");
            Matcher matcher = p.matcher(title);
            if(matcher.find()){
                String  content = (matcher.group().subSequence(1, matcher.group().length()-1)).toString();
                showLog("Content:"+content);
                showLog("Content:"+content.length());
                showLog("Content:"+title.length());
                title= title.replace("[","").replace("]","");
                Spannable spannable = new SpannableString(title);
                String str = spannable.toString();
                int iStart = str.indexOf(content);
                int length = content.length();
                int iEnd = iStart + length;
                final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD);
                spannable.setSpan(new ForegroundColorSpan(Color.parseColor(pnColor)), iStart, iEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.setSpan(bss, iStart, iEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                pnTitle=spannable;
            }

        } catch (IndexOutOfBoundsException e    ) {
            e.printStackTrace();
        }

        String GROUP_KEY_WORK_EMAIL = "com.anyNews.anynews.Services.WORK_EMAIL";
        collapsedView.setTextViewText(R.id.notification_titleE,pnTitle);
        collapsedView.setTextViewText(R.id.notification_titleTEL,pnTitle);
        collapsedView.setTextViewText(R.id.notification_titleTa,pnTitle);

        int languageId = SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
        if (languageId == 4) {
            collapsedView.setViewVisibility(R.id.notification_titleE, View.GONE);
            collapsedView.setViewVisibility(R.id.notification_titleTEL,View.GONE);
            collapsedView.setViewVisibility(R.id.notification_titleTa,View.VISIBLE);
        } else if (languageId == 1) {
            collapsedView.setViewVisibility(R.id.notification_titleE,View.VISIBLE);
            collapsedView.setViewVisibility(R.id.notification_titleTEL,View.GONE);
            collapsedView.setViewVisibility(R.id.notification_titleTa,View.GONE);
        }else{
            collapsedView.setViewVisibility(R.id.notification_titleE,View.GONE);
            collapsedView.setViewVisibility(R.id.notification_titleTEL,View.VISIBLE);
            collapsedView.setViewVisibility(R.id.notification_titleTa,View.GONE);
        }


        expandedView.setTextViewText(R.id.notification_title,pnTitle);
        Intent resultIntent = new Intent(getApplicationContext(), StartingActivity.class);
        resultIntent.putExtra("notification", true);
        resultIntent.putExtra("TITILE", post_id);
        resultIntent.putExtra("TITLE_REWARDS",title);
        resultIntent.putExtra("OBJECT", object);

        // ary answe my call

        System.out.println("TITTIL_OBJECT:: " + post_id);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // add notification one by one in case of multiple
        //PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), Integer.parseInt(post_id), resultIntent, PendingIntent.FLAG_IMMUTABLE);
        // only updated one will show and over ride
        //final PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), Integer.parseInt(post_id), resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        /*PendingIntent resultPendingIntent = PendingIntent.getPendingIntent(Integer.parseInt(post_id)
                        , PendingIntent.FLAG_UPDATE_CURRENT);*/
        // flag mutable |
        PendingIntent pendingIntent;

        if (Build.VERSION.SDK_INT >= 31) {
            pendingIntent = PendingIntent.getActivity(
                    getApplicationContext(),
                    Integer.parseInt(post_id),
                    resultIntent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT // setting the mutability flag
            );


        } else {
            pendingIntent = PendingIntent.getActivity(this,
                    Integer.parseInt(post_id), resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        }

        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null
                        && notificationManager.getNotificationChannel(Constants.Channel_ID) == null) {
                    NotificationChannel channel = new NotificationChannel(
                            Constants.Channel_ID,
                            "Any News",
                            NotificationManager.IMPORTANCE_HIGH
                    );
                    channel.enableVibration(true);
                    notificationManager.createNotificationChannel(channel);
                }
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), Constants.Channel_ID)
                    // use xxxhdpi
                    .setSmallIcon(R.drawable.ic_arman_notificatio)
//                    .setContentTitle("" + title)
//                    .setContentText(body)
                    .setCustomContentView(collapsedView)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setVibrate(new long[]{1000, 1000, 1000, 1000})
                    .setSound(uri)
                    .setShowWhen(showPNTime)
                    .setGroup(GROUP_KEY_WORK_EMAIL)
                    .setGroupSummary(false)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setSmallIcon(R.drawable.ic_arman_notificatio);
                builder.setColor(getResources().getColor(R.color.colorPrimary));
            } else {
                builder.setSmallIcon(R.drawable.ic_arman_notificatio);
            }
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

            int m = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
            //notificationManager.notify(random.nextInt(), builder.build());
            notificationManager.notify(m, builder.build());
        } catch (Exception e) {
            Log.v("NotificationData", String.valueOf(e.getMessage()));
        }


    }

    public class GetBitmapURL extends AsyncTask<Void, Void, Void> {
        String src;
        String title;
        String post_id;
        String object;
        String pnColor;

        public GetBitmapURL(String src, String title, String post_id, String object,String pnColor) {
            this.src = src;
            this.title = title;
            this.post_id = post_id;
            this.object = object;
            this.pnColor = pnColor;

        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                getBitmapFromURL(src, title, post_id, object,pnColor,showPNTime);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public void writePushReceive(String userId, String type_firebase, long post_id, long current_time, int language_id, long sent_at, long draft_id, String device_id,boolean showPNTime) {
        PushReceive appOpen = new PushReceive(type_firebase, post_id, current_time, language_id, sent_at, draft_id, device_id,showPNTime);

        myRef.child("Events").push().setValue(appOpen);
    }

    @IgnoreExtraProperties
    @Keep
    public class PushReceive {

        public String type;
        public long post_id;
        public long created_at;
        public int language_id;
        public long sent_at;
        public long draft_id;
        public String device_id;
        public boolean showPNTime;


        public PushReceive() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public PushReceive(String type_firebase, long post_id, long current_time, int language_id, long sent_at, long draft_id, String device_id,boolean showPNTime) {
            this.type = type_firebase;
            this.post_id = post_id;
            this.created_at = current_time;
            this.language_id = language_id;
            this.sent_at = sent_at;
            this.draft_id = draft_id;
            this.device_id = device_id;
            this.showPNTime = showPNTime;
        }

    }
}
