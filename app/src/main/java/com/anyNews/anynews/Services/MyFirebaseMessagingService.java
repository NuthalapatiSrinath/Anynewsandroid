package com.anyNews.anynews.Services;

import static com.anyNews.anynews.Utils.AppHelper.showLog;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
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
import androidx.core.content.ContextCompat;

import com.anyNews.anynews.Activities.HomePage;
import com.anyNews.anynews.Activities.StartingActivity;
import com.anyNews.anynews.Pojo.AddLikeResponse;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.StartingAppRequest;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.Logone;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Utils.MyUtils.encrypt;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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

            // Targeted per-user alert: someone replied to this user's comment.
            String type = object.optString("type", "");
            if ("comment_reply".equals(type)) {
                sendCommentReplyNotification(object);
                return;
            }

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
        SharedPrefsData.putString(getApplicationContext(), Constants.FCM_TOKEN, s, Constants.PREF_NAME);
        int draftId = SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
        if (draftId != 0) {
            sendFcmTokenToServer(s);
        }
    }

    private void sendFcmTokenToServer(String token) {
        try {
            StartingAppRequest requestModel = new StartingAppRequest();
            requestModel.setDraftId(draftIdFromPrefs());
            requestModel.setFcmToken(token);
            String jsonString = new Gson().toJson(requestModel);
            String encrpt_data = encrypt(jsonString);
            EncritonRequestData encritonRequestData = new EncritonRequestData();
            encritonRequestData.setData(encrpt_data);
            JsonObject body = new Gson().toJsonTree(encritonRequestData).getAsJsonObject();
            String versionId = SharedPrefsData.getString(getApplicationContext(), Constants.Version_ID, Constants.PREF_NAME);
            if (versionId == null || versionId.isEmpty()) {
                versionId = "v3";
            }
            APIService apiService = RestApiClient.getClient().create(APIService.class);
            apiService.tokens(versionId, body).enqueue(new Callback<AddLikeResponse>() {
                @Override
                public void onResponse(Call<AddLikeResponse> call, Response<AddLikeResponse> response) {
                    showLog("FCM token updated on server");
                }

                @Override
                public void onFailure(Call<AddLikeResponse> call, Throwable t) {
                    Logone.e("FCM Token", "Failed to update token on server", t);
                }
            });
        } catch (Exception e) {
            Logone.e("FCM Token", "Error sending token to server", e);
        }
    }

    private int draftIdFromPrefs() {
        return SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
    }

    private int safeNotificationId(String postId) {
        try {
            return Integer.parseInt(postId);
        } catch (NumberFormatException e) {
            return postId != null ? Math.abs(postId.hashCode()) : 0;
        }
    }

    private int safeParseColor(String colorString) {
        try {
            return Color.parseColor(colorString);
        } catch (IllegalArgumentException e) {
            return Color.parseColor("#000000");
        }
    }

    private boolean canPostNotifications() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }
        return ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
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


    public SpannableStringBuilder makeSpannable(String text, String regex, String colorCode) {
        int colorIndex = 0;
        ArrayList<String> colorCodeList = new ArrayList<>();
        if (colorCode != null && colorCode.contains(",")) {
            colorCodeList = new ArrayList<>(Arrays.asList(colorCode.split(",")));
        } else if (colorCode != null) {
            colorCodeList.add(colorCode);
        } else {
            colorCodeList.add("#000000");
        }

        StringBuffer sb = new StringBuffer();
        SpannableStringBuilder spannable = new SpannableStringBuilder();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            sb.setLength(0);
            String group = matcher.group();
            String spanText = group.substring(1, group.length() - 1);
            matcher.appendReplacement(sb, spanText);
            spannable.append(sb.toString());
            int start = spannable.length() - spanText.length();
            final StyleSpan boldSpan = new StyleSpan(android.graphics.Typeface.BOLD);
            spannable.setSpan(new ForegroundColorSpan(safeParseColor(colorCodeList.get(colorIndex % colorCodeList.size()))), start, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(boldSpan, start, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            colorIndex++;
        }

        sb.setLength(0);
        matcher.appendTail(sb);
        spannable.append(sb.toString());
        return spannable;
    }

    private void sendNotification(Bitmap imgUrl, String title, String post_id, String object,String pnColor,boolean showPNTime) {
        RemoteViews collapsedView = new RemoteViews(getPackageName(), R.layout.new_collapsed_request);
        RemoteViews expandedView = new RemoteViews(getPackageName(), R.layout.new_request);
        final Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Spannable pnTitle = makeSpannable(title, "\\[.*?\\]", pnColor);

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
        int notificationId = safeNotificationId(post_id);

        if (Build.VERSION.SDK_INT >= 31) {
            pendingIntent = PendingIntent.getActivity(
                    getApplicationContext(),
                    notificationId,
                    resultIntent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT // setting the mutability flag
            );


        } else {
            pendingIntent = PendingIntent.getActivity(this,
                    notificationId, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        }

        try {
            if (!canPostNotifications()) {
                return;
            }

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

    // Comment-reply alerts show actor + snippet with a standard notification
    // layout (the news RemoteViews only render a bracket-styled title). Tapping
    // reuses the existing push-open path: StartingActivity routes to the article
    // by post_id, where the comments panel lives.
    private void sendCommentReplyNotification(JSONObject object) {
        try {
            if (!canPostNotifications()) {
                return;
            }

            String actor = object.optString("actor_name", object.optString("title", "AnyNews"));
            String snippet = object.optString("snippet", object.optString("body", ""));
            String post_id = object.optString("post_id", "0");

            Intent resultIntent = new Intent(getApplicationContext(), StartingActivity.class);
            resultIntent.putExtra("notification", true);
            resultIntent.putExtra("TITILE", post_id);
            resultIntent.putExtra("TITLE_REWARDS", actor);
            resultIntent.putExtra("OBJECT", object.toString());
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            int notificationId = safeNotificationId(post_id);
            PendingIntent pendingIntent;
            if (Build.VERSION.SDK_INT >= 31) {
                pendingIntent = PendingIntent.getActivity(
                        getApplicationContext(),
                        notificationId,
                        resultIntent,
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            } else {
                pendingIntent = PendingIntent.getActivity(
                        getApplicationContext(),
                        notificationId,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
            }

            final Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null
                        && notificationManager.getNotificationChannel(Constants.Channel_ID) == null) {
                    NotificationChannel channel = new NotificationChannel(
                            Constants.Channel_ID,
                            "Any News",
                            NotificationManager.IMPORTANCE_HIGH);
                    channel.enableVibration(true);
                    notificationManager.createNotificationChannel(channel);
                }
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), Constants.Channel_ID)
                    .setSmallIcon(R.drawable.ic_arman_notificatio)
                    .setContentTitle(actor)
                    .setContentText(snippet)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(snippet))
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setVibrate(new long[]{1000, 1000, 1000, 1000})
                    .setSound(uri)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setColor(getResources().getColor(R.color.colorPrimary));
            }

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
            int m = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
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
